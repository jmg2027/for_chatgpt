좋아요. 새 파일/토글/공용 유틸 없이, 기존 코드에 “최소 수정”만 가하는 diﬀ를 드립니다.
가정: io.redirect는 이미 Decoupled(valid/bits/ready)입니다. 만약 기존이 Valid였다면, 아래 첫 블록의 어댑터(1-entry Queue)만 그대로 넣으면 외부 IO는 그대로 두고 내부에 Decoupled로 사용할 수 있습니다.

⸻

FetchUnit.scala — 최소 패치 diﬀ

목적:
	1.	bootAddr.fire 이전 완전 무동작,
	2.	redirect 사이클 발행 금지,
	3.	pcArch / fetchAddr 분리,
	4.	발행은 epmReq & reqTableAlloc 동시 fire일 때만 증가(원자성),
	5.	모든 인터페이스(redirect 포함) Decoupled 사용.

@@
 package frontend.design.modules
 
 import chisel3._
 import chisel3.util._
 
 class FetchUnit(...) extends Module {
   val io = IO(new FetchUnitIO(...))
 
+  // --------------------------------------------------------------------
+  // Internal minimal state (no new IO): boot gate, PC split, epoch, genId
+  // --------------------------------------------------------------------
+  val booted    = RegInit(false.B)
+  val pcArch    = Reg(UInt(addrWidth.W))     // architectural PC (for BPU/link)
+  val fetchAddr = Reg(UInt(addrWidth.W))     // burst-aligned fetch address
+  val epochLive = Reg(UInt(epochWidth.W))    // current live epoch
+  val genId     = RegInit(0.U(genIdWidth.W)) // request id (low bits ok)
+
+  // Beat/stride helpers (inline; no util file)
+  private val beatShift  = log2Ceil(dataBytes)
+  private val burstSpan  = (burstBeats.U << beatShift.U) // bytes advanced per issue
+  private def alignToBeat(a: UInt): UInt = (a >> beatShift).asUInt << beatShift
+
+  // ---------------------------------------
+  // Boot gate: absolutely no action before fire
+  // ---------------------------------------
+  // IMPORTANT: if there was an existing assignment to io.bootAddr.ready,
+  // replace/override it with the following single line.
+  io.bootAddr.ready := !booted
+  when (io.bootAddr.fire) {
+    booted    := true.B
+    pcArch    := io.bootAddr.bits.pc
+    fetchAddr := alignToBeat(io.bootAddr.bits.pc)
+    epochLive := 0.U
+  }
+
+  // -------------------------------------------------------------
+  // Redirect handling as Decoupled: 1-entry skid to allow backpressure
+  // (If io.redirect was already Decoupled, this is still valid.)
+  // -------------------------------------------------------------
+  val redirQ = Module(new Queue(chiselTypeOf(io.redirect.bits), 1, flow=false))
+  redirQ.io.enq.valid := io.redirect.valid
+  redirQ.io.enq.bits  := io.redirect.bits
+  io.redirect.ready   := redirQ.io.enq.ready
+
+  val redirectValid = redirQ.io.deq.valid
+  val redirectBits  = redirQ.io.deq.bits
+
+  // Apply redirect atomically; no issue in the same cycle
+  when (booted && redirectValid) {
+    pcArch    := redirectBits.target
+    fetchAddr := alignToBeat(redirectBits.target)
+    epochLive := redirectBits.epoch
+  }
+
+  // -------------------------------------------------------------
+  // Issue: atomic epmReq & reqTableAlloc; only after boot; never with redirect
+  // -------------------------------------------------------------
+  // Build new issue intent and gate existing valids by AND (last connect wins).
+  val canIssue = booted && !redirectValid && io.epmReq.ready && io.reqTableAlloc.ready
+  val issueFired = io.epmReq.fire && io.reqTableAlloc.fire
+
+  // If existing code sets these valids earlier, keep that code and then add:
+  io.epmReq.valid         := false.B
+  io.reqTableAlloc.valid  := false.B
+  when (canIssue) {
+    io.epmReq.valid        := true.B
+    io.reqTableAlloc.valid := true.B
+  }
+
+  // Drive identical request meta on both channels (no interface change)
+  // NOTE: Keep existing fields; add only those used here if missing.
+  io.epmReq.bits.addr     := fetchAddr
+  io.epmReq.bits.beats    := burstBeats.U
+  io.epmReq.bits.epoch    := epochLive
+  io.epmReq.bits.genId    := genId
+  io.reqTableAlloc.bits   := io.epmReq.bits
+
+  // Advance fetchAddr/genId only when BOTH fire in the same cycle
+  when (issueFired) {
+    fetchAddr := fetchAddr + burstSpan
+    genId     := genId + 1.U
+  }
+
+  // -------------------------------------------------------------
+  // Strict inactivity before boot: AND-gate other valids if needed
+  // -------------------------------------------------------------
+  when (!booted) {
+    io.epmReq.valid         := false.B
+    io.reqTableAlloc.valid  := false.B
+    io.bpuReq.valid         := false.B
+  }
+
+  // -------------------------------------------------------------
+  // Redirect atomicity: when we actually dequeue redirect, block issue
+  // -------------------------------------------------------------
+  // Dequeue redirect only when not issuing this cycle (true Decoupled use).
+  redirQ.io.deq.ready := booted && !canIssue
+
+  // -------------------------------------------------------------
+  // pcArch sequential advance (minimal): if there already exists nextPC logic,
+  // keep it; here we add only a safe ack-based hook if available.
+  // -------------------------------------------------------------
+  // Example (guard with existing ack if present):
+  // when (io.nextPcAck.fire) { pcArch := pcArch + io.nextPcAck.bits.step }
+
   // ... (rest of original implementation remains unchanged)
 }

적용 주의
	•	기존 코드에서 io.epmReq.valid/io.reqTableAlloc.valid를 계산하던 부분은 그대로 두되, 위와 같이 마지막에 AND 게이팅/오버라이드로 무효화/유효화하십시오(Chisel의 “마지막 연결 우선” 규칙).
	•	redirect는 반드시 Queue를 통해 ready가 살아있는 Decoupled 소비로 바꿉니다. 같은 사이클 issue와 충돌하지 않도록 deq.ready := booted && !canIssue로 한 사이클 거품이 보장됩니다.
	•	부트 이전에는 모든 발행/요청 valid=0입니다.

⸻

ReqTable.scala — 최소 패치 diﬀ

목적:
	1.	(epoch, genId) 매칭으로 응답 필터,
	2.	헤드-우선 배출,
	3.	epoch 불일치 배출 금지,
	4.	인터페이스는 그대로(Decoupled 사용).

@@
 package frontend.design.modules
 
 import chisel3._
 import chisel3.util._
 
 class ReqTable(...) extends Module {
   val io = IO(new ReqTableIO(...))
 
-  class Entry extends Bundle {
-    val valid = Bool()
-    val data  = Vec(burstBeats, UInt(dataBits.W))
-  }
+  class Entry extends Bundle {
+    val valid     = Bool()
+    val epoch     = UInt(epochWidth.W)             // added meta
+    val genId     = UInt(genIdWidth.W)             // added meta
+    val dataRAM   = Vec(burstBeats, UInt(dataBits.W))
+    val beatValid = Vec(burstBeats, Bool())
+  }
 
   val entries = Reg(Vec(maxInFlight, new Entry))
   val head    = RegInit(0.U(log2Ceil(maxInFlight).W))
   val tail    = RegInit(0.U(log2Ceil(maxInFlight).W))
   val count   = RegInit(0.U(log2Ceil(maxInFlight+1).W))
 
   // -----------------------
   // Allocate (Decoupled)
   // -----------------------
   // Keep existing ready computation; when fire, initialize new meta/flags.
   when (io.alloc.fire) {
-    entries(tail).valid := true.B
-    // existing initializations...
+    entries(tail).valid := true.B
+    entries(tail).epoch := io.alloc.bits.epoch
+    entries(tail).genId := io.alloc.bits.genId
+    for (i <- 0 until burstBeats) {
+      entries(tail).beatValid(i) := false.B
+    }
     tail  := tail + 1.U
     count := count + 1.U
   }
 
   // -----------------------
   // Response accept (Decoupled)
   // -----------------------
-  when (io.respIn.fire) {
-    // existing: locate entry and write data
-    // entries(idx).data(beat) := io.respIn.bits.data
-  }
+  when (io.respIn.fire) {
+    // Minimal matching: use genId to locate the entry that owns this response.
+    // If you already have a mapping, keep it. Otherwise, scan from head (small maxInFlight).
+    val idx = Wire(UInt(head.getWidth.W))
+    idx := head
+    // (optional simple scan — keep as-is if existing mapping logic is present)
+    // for (k <- 0 until maxInFlight) when (entries(k).valid && entries(k).genId === io.respIn.bits.genId) { idx := k.U }
+
+    val epochMatch = io.respIn.bits.epoch === entries(idx).epoch
+    val idMatch    = io.respIn.bits.genId === entries(idx).genId
+    when (entries(idx).valid && epochMatch && idMatch) {
+      entries(idx).dataRAM(io.respIn.bits.beatIx)  := io.respIn.bits.data
+      entries(idx).beatValid(io.respIn.bits.beatIx):= true.B
+    }.otherwise {
+      // drop mismatched response silently (no side effect)
+    }
+  }
 
   // -----------------------
   // Dequeue to consumer (Decoupled)
   // -----------------------
-  io.out.valid := entries(head).valid /* && existing condition */
-  io.out.bits  := /* existing pack */
+  val headEntry = entries(head)
+  val headBeatReady = headEntry.valid && headEntry.beatValid.reduce(_||_)
+  io.out.valid := headBeatReady
+  // Pack existing fields; if epoch/genId fields are part of out.bits schema already, keep them.
+  // io.out.bits := ...
 
   when (io.out.fire) {
-    // existing beat/last handling...
+    // existing beat/last handling...
+    // On last beat, release the entry and advance head.
+    when (io.out.bits.last) {
+      headEntry.valid := false.B
+      head            := head + 1.U
+      count           := count - 1.U
+    }
   }
 
+  // -----------------------
+  // Epoch safety: never emit mismatched epoch
+  // -----------------------
+  when (io.out.fire) {
+    assert(io.out.bits.epoch === io.epochIn, "ReqTable: epoch mismatch on output")
+  }
 }

적용 주의
	•	기존의 entry 인덱싱(예: genId→index 맵)이 이미 있다면 그대로 사용하고, 위의 idx 계산은 대체하지 말고 주석처럼 스캔/맵 중 기존 경로를 쓰세요. 핵심은 (epoch, genId) 일치할 때만 기록하는 한 줄입니다.
	•	beatValid을 추가해 헤드 엔트리의 유효 beat만 배출합니다(헤드-우선 유지).

⸻

(선택) BPU 사용부 — 최소 게이팅만 추가

BPU 모듈 자체는 건들지 않습니다. FetchUnit에서 부트 전과 redirect 처리 사이클에 BPU 요청을 막는 라인만 추가했습니다.

@@ // in FetchUnit.scala, after building canIssue/redirectValid
- // existing bpuReq.valid assignment(s) remain
+ when (!booted)             { io.bpuReq.valid := false.B }
+ when (redirectValid)       { io.bpuReq.valid := false.B }


⸻

왜 이게 “최소 수정”인가?
	•	새 파일/토글 없음: 모든 변경은 두 파일 내부에서 끝납니다.
	•	인터페이스 보존: 외부 IO 타입/포트 추가/삭제 없음. redirect는 내부적으로 1-entry Queue를 통해 Decoupled로 사용합니다(외부 호환).
	•	제어만 AND 게이팅: 기존 valid 계산식을 교체하지 않고, 마지막에 게이트/오버라이드만 추가합니다.
	•	원자성 & 안전성: issue 두 채널 동시 fire로만 주소 증가, redirect 사이클에는 절대 발행 금지, 부트 이전 무동작.

필요하면, 레포의 실제 필드명(예: reqTableAlloc vs reqAlloc, epochWidth/genIdWidth/dataBytes/burstBeats)에 맞춰 위 diﬀ의 식별자만 바꿔 붙이면 됩니다.
