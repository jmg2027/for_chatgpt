UDA(통합 데이터플로우 아키텍처) 설계 방법론 v2.0 (최종 출판본) 📜
Rev. 2.0 (2025-07-28)
1. 개요 및 철학 (Overview & Philosophy)
본 문서는 복잡하고 유연한 디지털 시스템, 특히 프로세서 코어를 설계하기 위한 UDA(Unified Dataflow Architecture)의 핵심 원칙과 패턴을 정의한다. UDA는 시스템의 모든 동작을 자율적인 노드(Node) 간의 데이터 토큰(Token) 흐름으로 모델링하여 설계의 모듈성, 검증 용이성, 확장성을 극대화하는 것을 목표로 한다.
1.1. 5대 설계 원칙
원칙
한 줄 요약
"Why it works": 이 원칙의 가치
단일 계산 모델
시스템 전체를 하나의 통합된 데이터플로우 그래프로 구성한다.
전체 시스템의 경로 지연 시간을 하나의 DAG로 노출시켜, GA 스크립트가 전역적인 타이밍/IPC 트레이드오프를 알고리즘적으로 분석하고 최적화할 수 있게 한다.
완전한 분산 및 자율성
모든 노드는 자신의 지역적 규칙에 따라서만 자율적으로 동작한다.
노드의 인터페이스가 내부 지연 시간을 숨겨주므로, DIV 유닛을 더 빠른 버전으로 교체해도 Dispatch 로직을 수정할 필요가 없다.
제어의 데이터화
flush, stall 등은 데이터 토큰의 '상태'와 '문맥' 정보의 흐름으로 표현된다.
형식 검증(Formal Verification)이 복잡한 상태 머신 증명이 아닌, ¬(token.epoch ≠ current_epoch)와 같은 단순한 데이터 비교 문제로 귀결된다.
일관된 인터페이스
모든 노드 간 통신은 DecoupledIO 프로토콜을 사용한다.
단일 FIRRTL 패스로 전체 파이프라인 구조(e.g., RelayStation 삽입/제거)를 안전하게 변경할 수 있다.
제로 오버헤드 추상화
본 방법론에 따른 설계는 수작업 RTL과 동등하거나 더 나은 PPA를 목표로 한다.
추상화로 인한 비용("abstraction tax") 없이 설계 생산성과 유지보수성을 향상시킨다.
PPA 평가 규칙: UDA는 다음 조건을 만족할 때 '제로 오버헤드'로 간주한다: 동일한 CoreConfig, 공정 라이브러리, EDA 툴 버전 하에서, 숙련된 엔지니어가 작성한 RTL 대비 Area ±5%, Fmax ±5%, IPC ±2% 이내의 결과.
1.2. 비포함 목표 (Non-Goals)
주제
제외 이유
향후 계획
CDC
UDA의 핵심인 단일 클럭 데이터플로우에 집중하기 위함.
v2.0 "다중 도메인 UDA": RelayCDC 프리미티브와 형식적 메타안정성 증명.
UPF / 전력 의도
UPF 의미론이 벤더마다 달라, 벤더 중립적인 FIRRTL을 목표로 함.
v2.1: 자동 클럭 게이팅 삽입 및 UPF 3.0 export 기능 추가 예정.
캐시 일관성
SoC 레벨의 프로토콜과 메모리 순서 모델 명세가 필요함.
별도의 "UDA-CC" 백서에서 다룰 예정 (Q4/2026).
메모리 성능 모델
프리페처 등은 마이크로아키텍처의 특성이므로, 핵심 방법론은 일반성을 유지.
향후 "UDA-Mem" 애드온 라이브러리로 제공 예정.
RelayCDC (v2.0 preview)[^1]: 대상 클럭 도메인에 플립플롭이 위치하는 1-깊이 릴레이. 에포크 토글을 통해 도메인 간 플러시 일관성을 보장.
2. 형식 계산 모델 (Formal Model of Computation) ⚖️
UDA는 노드(Node), 토큰(Token), **채널(Channel)**이라는 세 가지 기본 요소로 구성된다. 이 모델은 전통적인 파이프라인 구조를 더 유연한 그래프 형태로 표현한다.
flowchart LR
    subgraph Frontend
        PC[PC Gen<br/>Source] --> RS_IF[Relay]
        RS_IF --> IF[Fetch Node]
    end
    IF --> ID[Decode Node]
    ID --> EX[Execute (ALU/Branch)]
    EX --> MEM[Mem Access]
    MEM --> RS_CM[Relay]
    RS_CM --> CM[Commit Sink]
    
    CM -- FlushToken(epoch++) --> ES[EpochSplitter]
    ES -- epoch --> IF
    ES -- epoch --> ID
    ES -- epoch --> EX

    classDef relay fill:#D0E7FF,stroke:#1e88e5;
    class RS_IF,RS_CM relay;

그림 1: 5단계 인오더 코어의 UDA 표현. 파란색 노드는 StageEverything 패스에 의해 삽입된 RelayStation이며, 주황색 경로는 EpochSplitter에 의해 방송되는 제어 경로를 나타낸다.
	•	토큰(Token): 채널을 통해 흐르는 데이터와 문맥 정보의 단위. (§3 참조)
	•	채널(Channel): 노드 간의 DecoupledIO 연결. back-pressure가 구조적으로 불필요한 채널의 핸드셰이크 로직은 합성 과정에서 제거된다.
	•	노드(Node): 단일 기능을 수행하는 자율적인 하드웨어 모듈. 모든 노드는 표준 NodeIO 인터페이스를 갖는다.
2.1. Back-pressure 제거 조건
채널의 ready 신호는 다음 조건 하에서 true로 고정(tie-high)될 수 있으며, 이 경우 관련 핸드셰이크 로직은 합성 시 제거된다.
	•	생산자-주도 (Producer-dominant): 생산자가 데이터를 드물게 생성하여 소비자가 항상 처리할 수 있는 경우.
	•	크레딧 기반 (Credit-based): 상위 프로토콜이 크레딧을 통해 흐름을 제어하여 채널이 절대 막히지 않음을 보장하는 경우.
	•	프로토콜 보장 (Protocol-guaranteed): 쓰기 전용 MMIO 포트와 같이, 하위 시스템이 프로토콜상 항상 요청을 수용한다고 보장하는 경우.
Lint 규칙 BP-0: ready가 true로 고정된 채널의 소비자가 유한한 깊이(>1)의 큐를 포함하는 경우, 오버플로우를 정적으로 배제할 수 없으므로 Lint 경고를 발생시켜야 한다.
{ "signal": [
  { "name": "clock",          "wave": "p.......|..." },
  { "name": "producer.valid", "wave": "10101.0.|1..", "data": ["A","B","C"] },
  { "name": "consumer.ready", "wave": "1.0.1.1.|1..", "phase": 0.5 },
  { "name": "tieReady",       "wave": "11111111|1..", "node":"....T"}
],
 "config": { "hscale": 1.4 } }

그림 2: Ready/Valid 핸드셰이크. 위쪽 채널은 ready가 false가 되어 토큰 B를 한 사이클 지연시키는 정상적인 back-pressure를 보여준다. 아래쪽 채널은 ready가 항상 true로 고정되어, valid 신호가 지연 없이 흐르는 것을 보여준다.
3. 토큰 필드 명세 (Token Field Specification) 🎟️
모든 토큰은 표준화된 context 필드를 포함하여, 시스템 전체에서 일관된 제어를 가능하게 한다.
// Generic Token
case class Token[D <: Data](
  payload: D,      // 사용자 데이터 (e.g., 명령어, 연산 결과)
  epoch  : UInt,   // 제어 흐름의 실행 세대 ID
  meta   : UInt    // 기타 문맥 정보 (아래 표 참조)
)

meta 필드 상세 내역:
| Field | Bits | Owner | Purpose |
| :--- | :--- | :--- | :--- |
| seq_id | ceil(log₂(ROBDepth)) | Decode | Program-order identifier |
| spec_tag| ceil(log₂(BrTags)) | Branch Unit (BPU) | Distinguish speculative paths |
| exc_class| 5 bits | FUs | RISC-V exception class |
| priv_lvl| 2 bits | CSR | Current privilege level |
3.1. Epoch 생명주기 및 태그 무효화
    dateFormat  x
    axisFormat  %L
    
    section Core epoch
    Epoch=N     :a1, 0,12
    Epoch=N+1   :after a1, 14
    
    section Token A (good path)
    valid (epoch=N)   :d1, 0,10
    killed            :crit, 10,2
    -- gap --
    resubmitted (N+1) :d2, 14,4
    
    section Token B (mispredict path)
    exec (epoch=N)    :e1, 0,12
    unused            :des, 12,6

그림 3: 에포크 플러시 타임라인. 사이클 12에 예측 실패가 감지되고, EpochSplitter가 N+1 에포크를 발행한다. epoch=N을 가진 기존 토큰들은 이후 사이클에서 EpochFilter에 의해 폐기된다.
4. 핵심 설계 패턴 라이브러리 (Core Design Pattern Library) 🧱
Pattern (§ 4)
Problem Solved
Nodes / Rules
State-Tracker
RAW data hazard
SrcReadyNode keeps busy bits; Dispatch uses Query / Reserve / Release
Epoch-Coherence
Flush consistency
EpochFilter drops stale tokens; EpochSplitter fans out new epoch
Contention
Structural stalls
Arbiter (ArbiterPolicy) arbitrates; back-pressure propagates upstream
Final-Authority
Ordered commit
Commit retires in seq_id order; emits flush / interrupt tokens
5. 어노테이션 API 및 설계 원칙 (Annotation API & Design Rules) ✍️
'조합적 경로는 빚'이라는 사고방식을 코드에 명시적으로 표현한다.
	•	@CombCritical: 해당 경로는 반드시 조합적으로 연결되어야 함. (0-사이클 경로)
	•	@CombPreferred: 타이밍 여유가 있다면 조합적으로 연결되길 선호함.
// Annotation API Snippet
object CombCritical  extends firrtl.annotations.NoTargetAnnotation
object CombPreferred extends firrtl.annotations.NoTargetAnnotation

implicit class EdgeTagger[T <: Data](x: DecoupledIO[T]) {
  def critical : DecoupledIO[T] = { x.addAnnotation(CombCritical);  x }
  def preferred: DecoupledIO[T] = { x.addAnnotation(CombPreferred); x }
}

6. 자동화 패스 (Automation Passes) 🤖
'안전 우선, 성능은 회복' 원칙을 구현하는 컴파일러 변환 단계.
6.1. 자동화 워크플로우
graph TD
    A([Start: Pure Node RTL]) --> B{StageEverything Pass};
    B -- All edges get a Relay --> C([Cycle-Free RTL]);
    C --> D{Run STA};
    D -- Timing Slack Report (JSON) --> E{DebtOptimizer Pass};
    E -- Removes affordable Relays --> F([PPA-Optimized RTL]);
    F --> G([Final Verification]);

	•	StageEverything: 모든 모듈 경계에 RelayStation을 삽입하여 조합 루프를 원천적으로 방지.
	•	DebtOptimizer: STA 결과를 바탕으로, CombCritical 태그가 있거나 타이밍 여유(slack > margin)가 충분하고 IPC 저하(ΔIPC < ε)가 미미한 경로의 RelayStation을 자동으로 제거.
7. 물리 설계 가이드라인 (Physical Design Guidelines) 📐
	•	타이밍 제약: EpochSplitter에서 나온 epoch 신호넷은 높은 팬아웃을 가지므로, 제약 조건 파일(.sdc)에서 버퍼 트리 합성을 유도해야 한다. # 예시 SDC/TCL 스니펫
	•	set_max_fanout 50 [get_nets {*epochsplitter/out*}]
	•	
	•	
	•	클럭 게이팅: RelayStation과 같은 버퍼 노드는 fire 신호를 클럭 게이팅 조건으로 사용하여 동적 전력을 최소화한다. 합성 툴이 게이팅 로직을 제거하지 않도록 /* syn_keep */과 같은 속성을 사용할 수 있다.
	•	Floorplanning: Commit 유닛과 PC Gen 유닛은 물리적으로 가깝게 배치하여 리다이렉트 경로의 지연 시간을 줄이는 것이 유리하다.
8. 검증 전략 (Verification Strategy) ✔️
	•	Unit (단위): ChiselTest를 사용하여 각 노드가 자신의 지역 규칙을 완벽히 따르는지 검증.
	•	Formal (형식): SymbiYosys 등을 사용하여 교착 상태 없음(Deadlock-freedom)과 플러시 정확성(Flush Correctness)과 같은 전역 속성을 수학적으로 증명.
	•	Coverage (커버리지): riscv-formal 및 랜덤 인스트림 테스트를 통해 ISA 호환성(RV32I-M-C 등)과 모든 코너 케이스를 검증.
9. 파라미터화 (CoreConfig) 🔩
case class CoreConfig(
  isaExtensions: Set[String],
  robDepth: Int,
  issueWidth: Int,
  epochScheme: EpochScheme.Type,
  epochWidth: Int = 1,
  relayMaxDepth: Int = 1,
  criticalEdges: Set[String] = Set.empty
)

10. 용어집 (Glossary) 📖
	•	UDA: Unified Dataflow Architecture. 본 문서에서 정의하는 설계 방법론.
	•	LI (Latency-Insensitive): 모듈의 기능적 정확성이 연결 채널의 지연 시간에 영향을 받지 않는 설계 방식.
	•	PPA: Power, Performance, Area. 반도체 칩 설계의 3대 최적화 목표.
	•	STA: Static Timing Analysis. 정적 타이밍 분석.
	•	GA: Genetic Algorithm. 유전 알고리즘. 자동화된 탐색에 사용될 수 있는 휴리스틱.
	•	back-pressure: 후단 노드가 데이터를 받을 수 없을 때, ready 신호를 통해 상위 노드로 정체 상태를 전파하는 것.
	•	SCC: Strongly Connected Component. 강한 연결 요소. 조합 루프를 의미.
	•	DecoupledIO: Chisel에서 ready/valid 핸드셰이크를 구현하는 표준 인터페이스.
	•	RelayStation: UDA에서 조합 경로를 끊기 위해 사용하는 1-깊이 flow-through 큐.
	•	FIRRTL: Chisel 코드가 Verilog로 변환되기 전의 중간 표현.
부록 A: RelayStation Verilog 예시
/* AUTOINST */
module RelayStation #(
  parameter WIDTH = 32
) (
  input  logic clk,
  input  logic reset,
  // Input Channel
  output logic in_ready,
  input  logic in_valid,
  input  logic [WIDTH-1:0] in_bits,
  // Output Channel
  input  logic out_ready,
  output logic out_valid,
  output logic [WIDTH-1:0] out_bits
);

  reg [WIDTH-1:0] buf;
  reg             full;

  always_ff @(posedge clk or posedge reset) begin
    if (reset) begin
      full <= 1'b0;
    end else begin
      if (in_ready && in_valid) begin
        buf <= in_bits;
        full <= 1'b1;
      end else if (out_ready && out_valid) begin
        full <= 1'b0;
      end
    end
  end

  assign in_ready = !full;
  assign out_valid = full;
  assign out_bits = buf;

endmodule

