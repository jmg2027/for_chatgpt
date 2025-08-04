// #############################################################################
// ##
// ##  KLASE32-v1: Full Vertex Specification Document (FINAL - Rev. 4)
// ##
// ##  - This version is feature-complete and frozen.
// ##  - Ready for hand-off to a code generation agent.
// ##
// #############################################################################

// =============================================================================
// FILE: src/main/scala/klase32/specs/common/PayloadsSpecs.scala
// =============================================================================
package klase32.specs.common

import framework.macros.SpecEmit.spec
import framework.specs.Spec._

object PayloadsSpecs {

  // --- Constants ---
  val genIdWidth = 2
  val fetchWidth = 64

  // --- Basic Types ---
  val bndPc = spec {
    BUNDLE("BND_PC")
      .desc("Program Counter bundle, containing a 32-bit address.")
      .table(
        """|Field|Type|Description
           |pc|UInt(32.W)|32-bit program counter address
           |""".stripMargin
      )
      .build()
  }

  val bndRedirect = spec {
    BUNDLE("BND_REDIRECT")
      .desc(
        "Redirect token used for pipeline flushes and corrections. Carries the new target PC and the epoch of the instruction causing the redirect."
      )
      .table(
        """|Field|Type|Description
           |targetPc|UInt(32.W)|The new target PC to jump to
           |epoch|UInt(1.W)|The epoch of the redirecting instruction
           |""".stripMargin
      )
      .build()
  }
  
  val bndBoot = spec {
    BUNDLE("BND_BOOT")
      .desc("Carries the initial boot address and hartId from CoreEnableSequencer.")
      .table(
        """|Field|Type|Description
           |bootAddr|UInt(32.W)|Initial PC value
           |hartId|UInt(4.W)|Hart ID
           |""".stripMargin
      )
      .build()
  }

  // --- Frontend Payloads ---
  val bndPred = spec {
    BUNDLE("BND_PRED")
      .desc("Prediction result from BranchPredictor to PCGen.")
      .table(
        """|Field|Type|Description
           |targetPc|UInt(32.W)|Predicted target PC
           |taken|Bool|Predicted direction (True for taken)
           |""".stripMargin
      )
      .build()
  }

  val bndHint = spec {
    BUNDLE("BND_HINT")
      .desc("Pre-decode hint from AlignSlice to BranchPredictor.")
      .table(
        """|Field|Type|Description
           |isBranch|Bool|True if the instruction is likely a branch or jump
           |isBackward|Bool|True if the branch target is backwards (for loops)
           |""".stripMargin
      )
      .build()
  }

  val bndFetchReq = spec {
    BUNDLE("BND_FETCH_REQ")
      .desc("Fetch request from FetchUnit to ReqTable/Memory.")
      .table(
        s"""|Field|Type|Description
           |addr|UInt(32.W)|Aligned memory address for fetch
           |genId|UInt(${genIdWidth}.W)|Generation ID for flush coherence
           |epoch|UInt(1.W)|Epoch tag for flush coherence
           |""".stripMargin
      )
      .build()
  }

  val bndFetchRsp = spec {
    BUNDLE("BND_FETCH_RSP")
      .desc("Fetch response from memory/ReqTable.")
      .table(
        s"""|Field|Type|Description
           |data|UInt(${fetchWidth}.W)|Fetched instruction data
           |genId|UInt(${genIdWidth}.W)|Matching generation ID
           |""".stripMargin
      )
      .build()
  }

  val bndInstrWord = spec {
    BUNDLE("BND_INSTR_WORD")
      .desc("A single 32-bit instruction candidate word with its PC and epoch.")
      .table(
        """|Field|Type|Description
           |pc|UInt(32.W)|PC of the instruction
           |word32|UInt(32.W)|32-bit instruction data
           |epoch|UInt(1.W)|Instruction epoch (passed through AlignSlice)
           |""".stripMargin
      )
      .build()
  }

  val bndInstrToken = spec {
    BUNDLE("BND_INSTR_TOKEN")
      .desc(
        "The final instruction packet sent from Frontend to Backend, containing all necessary information for decode and execution."
      )
      .table(
        """|Field|Type|Description
           |pc|UInt(32.W)|PC of the instruction
           |word32|UInt(32.W)|32-bit instruction data
           |epoch|UInt(1.W)|Epoch of the instruction
           |pred|Pred|BPU prediction info for this instruction
           |""".stripMargin
      )
      .has(bndPred)
      .build()
  }

  val bndSelTok = spec {
    BUNDLE("BND_SEL_TOK")
      .desc("Selection token from BranchPredictor to IssueQueue.")
      .table(
        """|Field|Type|Description
           |selectIdx|UInt(log2Ceil(4).W)|Index to select from IssueQueue (using placeholder depth)
           |""".stripMargin
      )
      .build()
  }

  // --- Backend Payloads ---
  val bndBranchResult = spec {
    BUNDLE("BND_BRANCH_RESULT")
      .desc("Actual branch outcome from BranchCompare/CommitUnit to BPU for update.")
      .table(
        """|Field|Type|Description
           |pc|UInt(32.W)|PC of the branch instruction
           |taken|Bool|Actual outcome (True for taken)
           |targetPc|UInt(32.W)|Actual target PC if taken
           |epoch|UInt(1.W)|Epoch of the branch instruction
           |""".stripMargin
      )
      .build()
  }
  
  val bndBusyQuery = spec {
    BUNDLE("BND_BUSY_QUERY")
      .desc("Query from Decoder to Scoreboard to check for RAW hazards.")
      .table(
        """|Field|Type|Description
           |rs1|UInt(5.W)|Source register 1 index
           |rs2|UInt(5.W)|Source register 2 index
           |rdIdx|UInt(5.W)|Destination register index for WAW check
           |doWrite|Bool|True if the instruction writes to a register
           |""".stripMargin
      )
      .build()
  }

  val bndFuReq = spec {
    BUNDLE("BND_FU_REQ")
      .desc("Generic request bundle from Decoder to an execution unit (FU).")
      .table(
        """|Field|Type|Description
           |op|UInt(5.W)|Operation type for the FU (was EnumType)
           |rs1|UInt(32.W)|First source operand value
           |rs2|UInt(32.W)|Second source operand value
           |rd|UInt(5.W)|Destination register index
           |epoch|UInt(1.W)|Instruction epoch
           |""".stripMargin
      )
      .build()
  }

  val bndFuResp = spec {
    BUNDLE("BND_FU_RESP")
      .desc("Generic response bundle from an execution unit to WritebackUnit.")
      .table(
        """|Field|Type|Description
           |rd|UInt(5.W)|Destination register index
           |data|UInt(32.W)|Result data
           |epoch|UInt(1.W)|Instruction epoch
           |except|Bool|True if an exception occurred
           |""".stripMargin
      )
      .build()
  }

  val bndLoadData = spec {
    BUNDLE("BND_LOAD_DATA")
      .desc("Load data from LSU to LoadReturnBuffer.")
      .table(
        """|Field|Type|Description
           |rd|UInt(5.W)|Destination register index
           |data|UInt(32.W)|Data loaded from memory
           |epoch|UInt(1.W)|Instruction epoch
           |""".stripMargin
      )
      .build()
  }

  val bndRegWrite = spec {
    BUNDLE("BND_REG_WRITE")
      .desc("Final write port bundle for the RegisterFile.")
      .table(
        """|Field|Type|Description
           |rd|UInt(5.W)|Destination register index
           |data|UInt(32.W)|Data to be written
           |epoch|UInt(1.W)|Epoch of the writing instruction
           |""".stripMargin
      )
      .build()
  }

  val bndFlushCmd = spec {
    BUNDLE("BND_FLUSH_CMD")
      .desc("Command from CommitUnit to GlobalEpochCtrl to request a flush.")
      .table(
        """|Field|Type|Description
           |reason|UInt(4.W)|Reason for the flush (e.g., exception, mispredict)
           |""".stripMargin
      )
      .build()
  }

  val bndIrqTok = spec {
    BUNDLE("BND_IRQ_TOK")
      .desc("Token from InterruptCtrl to CommitUnit indicating a pending interrupt.")
      .table(
        """|Field|Type|Description
           |cause|UInt(4.W)|Interrupt cause (e.g., MEIP, MTIP)
           |""".stripMargin
      )
      .build()
  }
  
  val bndLsuReq = spec {
    BUNDLE("BND_LSU_REQ")
      .desc(
        """Load/Store request issued by the Decoder to the LSU.
          |A single bundle shape is used for both loads and stores; the `isStore`
          |flag distinguishes the operation. All byte-lane enable logic is
          |implicitly encoded by the <size,isStore,addr(1:0)> combination.
          |""".stripMargin)
      .table(
        """|Field     |Type         |Description
           |addr      |UInt(32.W)   |Aligned byte address of the access
           |wdata     |UInt(32.W)   |Write data (stores only); ignored for loads
           |rd        |UInt(5.W)    |Destination register index (loads); 'x0' for stores
           |size      |UInt(2.W)    |00=byte, 01=half, 10=word, 11=reserved
           |isStore   |Bool         |1 = store, 0 = load
           |signExt   |Bool         |For loads: 1 = sign-extend, 0 = zero-extend
           |epoch     |UInt(1.W)    |Epoch of the issuing instruction
           |""".stripMargin)
      .build()
  }

  val bndRegReadReq = spec {
    BUNDLE("BND_REG_READ_REQ")
    .desc("Stub for Register File read port request.")
    .table(
        """|Field|Type|Description
           |addr|UInt(5.W)|Register address to read
           |""".stripMargin)
    .build()
  }

  val bndDataReq = spec {
    BUNDLE("BND_DATA_REQ")
    .desc("Stub for an AXI-like memory data request from LSU.")
    .table(
        """|Field|Type|Description
           |addr|UInt(32.W)|Memory address
           |wdata|UInt(32.W)|Write data
           |wstrb|UInt(4.W)|Write strobe / byte enable
           |isWrite|Bool|Indicates a write transaction
           |""".stripMargin)
    .build()
  }

  val bndDataRsp = spec {
    BUNDLE("BND_DATA_RSP")
    .desc("Stub for an AXI-like memory data response to LSU.")
    .table(
        """|Field|Type|Description
           |rdata|UInt(32.W)|Read data
           |resp|UInt(2.W)|Response code (e.g., OKAY, ERROR)
           |""".stripMargin)
    .build()
  }

  val bndCsrReq = spec {
    BUNDLE("BND_CSR_REQ")
    .desc("Stub for a request to the CSRFile.")
    .table(
        """|Field|Type|Description
           |addr|UInt(12.W)|CSR address
           |wdata|UInt(32.W)|Write data
           |op|UInt(2.W)|CSR operation type (RW, RS, RC)
           |epoch|UInt(1.W)|Instruction epoch
           |""".stripMargin)
    .build()
  }

  val bndCsrResp = spec {
    BUNDLE("BND_CSR_RESP")
    .desc("Stub for a response from the CSRFile.")
    .table(
        """|Field|Type|Description
           |rdata|UInt(32.W)|Data read from CSR
           |except|Bool|True if access caused an exception
           |""".stripMargin)
    .build()
  }

  val bndIrqMask = spec {
    BUNDLE("BND_IRQ_MASK")
    .desc("Stub for interrupt masks sent from CSRFile to InterruptCtrl.")
    .table(
        """|Field|Type|Description
           |mask|UInt(3.W)|Interrupt enable mask (MEIE, MTIE, MSIE)
           |""".stripMargin)
    .build()
  }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/common/CommonTypes.scala
// =============================================================================
package klase32.specs.common

import framework.macros.SpecEmit.spec
import framework.specs.Spec._

object CommonTypes {
    val fuOps = spec {
        ENUM("FU_OP_CODES")
        .desc("Defines common operation codes for Functional Units.")
        .entry("ADD", "0", "Addition")
        .entry("SUB", "1", "Subtraction")
        .entry("SLL", "2", "Shift Left Logical")
        .entry("SRL", "3", "Shift Right Logical")
        .entry("SRA", "4", "Shift Right Arithmetic")
        .entry("AND", "5", "Bitwise AND")
        .entry("OR",  "6", "Bitwise OR")
        .entry("XOR", "7", "Bitwise XOR")
        .entry("SLT", "8", "Set if Less Than (signed)")
        .entry("SLTU", "9", "Set if Less Than (unsigned)")
        .build()
    }

    val loadSize = spec {
        ENUM("LOAD_STORE_SIZE")
        .desc("Defines size specifiers for LSU operations.")
        .entry("B", "0", "Byte (8-bit)")
        .entry("H", "1", "Half-word (16-bit)")
        .entry("W", "2", "Word (32-bit)")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/backend/common/FUCommon.scala
// =============================================================================
package klase32.specs.backend.common
import framework.macros.SpecEmit.spec
import framework.specs.Spec._
import klase32.specs.common.PayloadsSpecs._

object FUCommon {
  val intfFU = spec {
    INTERFACE("INTF_FU_GENERIC")
      .desc("Common two-port ready/valid interface for every execution unit.")
      .table(
        """|Port  |Dir|Type        |Semantics
           |reqIn |In |Decoupled[FUReq] |Request from Decoder
           |respOut|Out|Decoupled[FUResp]|Response to WritebackUnit
           |""".stripMargin)
      .has(bndFuReq, bndFuResp)
      .build()
  }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/frontend/PCGenSpecs.scala
// =============================================================================
package klase32.specs.frontend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._
import klase32.specs.common.PayloadsSpecs._

object PCGenSpecs {

  val contPCGen = spec {
    CONTRACT("CONT_PCGEN")
      .desc("Generates the next fetch PC based on sequential, predicted, and redirected sources. Acts as the central authority for PC updates.")
      .build()
  }

  val intfPcGen = spec {
    INTERFACE("INTF_PCGEN")
      .is(contPCGen)
      .desc("Defines all input and output ports for the PCGen vertex.")
      .table(
        """|Port|Dir|Type|Semantics
           |predIn|In|Decoupled[Pred]|BranchPredictor's prediction
           |earlyRedirectIn|In|Decoupled[Redirect]|0-cycle redirect from BranchCompare
           |lateRedirectIn|In|Decoupled[Redirect]|1-cycle flush from CommitUnit
           |bootIn|In|Decoupled[Boot]|Initial boot address
           |pcOut|Out|Decoupled[PC]|Final selected PC for fetching
           |epochIn|In|UInt(1.W)|Global current epoch
           |""".stripMargin
      )
      .has(bndPred, bndRedirect, bndBoot, bndPc)
      .build()
  }

  val funcSelectNextPc = spec {
    FUNCTION("FUNC_PCGEN_SELECT_NEXT_PC")
      .is(contPCGen)
      .desc("Selects the next PC based on a strict priority scheme. The selection logic is purely combinational.")
      .entry("Priority Order", "1. `bootIn` (highest) > 2. `lateRedirectIn` > 3. `earlyRedirectIn` > 4. `predIn` (if taken) > 5. Sequential PC+4 (lowest)")
      .entry("Sequential PC Calc", "`pcReg + 4`, with 32-bit wrap-around.")
      .build()
  }

  val funcUpdatePcRegister = spec {
    FUNCTION("FUNC_PCGEN_UPDATE_PC_REG")
      .is(contPCGen)
      .desc("Contains the master PC register, which is updated only when the next PC is successfully sent downstream (`pcOut.fire`).")
      .entry("State", "A single 32-bit register `pcReg` initialized to the boot address.")
      .entry("Update Condition", "Updates only on `pcOut.fire` to prevent losing PC values on stalls.")
      .build()
  }

  val propRedirectPriority = spec {
    PROPERTY("PROP_PCGEN_REDIRECT_PRIORITY")
      .is(contPCGen)
      .desc("A valid `lateRedirectIn` signal must always take precedence over a simultaneously valid `earlyRedirectIn` signal.")
      .code("assert(! (io.lateRedirectIn.valid && io.earlyRedirectIn.valid && io.pcOut.bits.pc =/= io.lateRedirectIn.bits.targetPc));")
      .build()
  }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/frontend/BranchPredSpecs.scala
// =============================================================================
package klase32.specs.frontend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._
import klase32.specs.common.PayloadsSpecs._

object BranchPredSpecs {
    val contBranchPred = spec {
        CONTRACT("CONT_BRANCH_PRED")
        .desc("Predicts branch outcomes and targets using PC and optional pre-decode hints. Its behavior is heavily configured by `CoreConfig.bpu`.")
        .build()
    }

    val intfBranchPred = spec {
        INTERFACE("INTF_BRANCH_PRED")
        .is(contBranchPred)
        .desc("Defines all input and output ports for the BranchPred vertex.")
        .table(
            """|Port|Dir|Type|Semantics
               |pcIn|In|Decoupled[PC]|Current PC from PCGen
               |hintIn|In|Decoupled[Hint]|(PRE_DECODE only) Hint from AlignSlice
               |updateIn|In|Valid[BranchResult]|Actual branch outcome from CommitUnit
               |predOut|Out|Decoupled[Pred]|Prediction result to PCGen
               |slotSelOut|Out|Decoupled[SelTok]|(PRE_DECODE only) Slot selection for IssueQueue
               |epochIn|In|UInt(1.W)|Global current epoch
               |""".stripMargin
        )
        .has(bndPc, bndHint, bndBranchResult, bndPred, bndSelTok)
        .build()
    }

    val funcBpuPrediction = spec {
        FUNCTION("FUNC_BPU_PREDICTION")
        .is(contBranchPred)
        .desc("Generates a branch prediction based on the configured mode (`bpu.kind`).")
        .entry("`kind = NONE`", "Always predicts not-taken (taken=false), predOut.valid is tied low.")
        .entry("`kind = SIMPLE`", "Uses a 2-bit saturating counter indexed by PC to predict direction.")
        .entry("`kind = PRE_DECODE`", "Uses `hint_in` to improve prediction. If `hint.isBranch` is false, predicts not-taken. Otherwise, uses BHT and static rules (backward-taken/forward-not-taken).")
        .build()
    }
    
    val funcBpuUpdate = spec {
        FUNCTION("FUNC_BPU_UPDATE")
        .is(contBranchPred)
        .desc("Updates the internal Branch History Table (BHT) based on actual branch outcomes from the Commit stage.")
        .entry("Update Logic", "On `updateIn.fire`, the 2-bit counter at the corresponding PC index is incremented if taken, and decremented if not-taken.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/frontend/FetchUnitSpecs.scala
// =============================================================================
package klase32.specs.frontend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._
import klase32.specs.common.PayloadsSpecs._

object FetchUnitSpecs {
    val contFetchUnit = spec {
        CONTRACT("CONT_FETCH_UNIT")
        .desc("Receives the final PC from PCGen and issues a memory fetch request to the ReqTable.")
        .build()
    }

    val intfFetchUnit = spec {
        INTERFACE("INTF_FETCH_UNIT")
        .is(contFetchUnit)
        .desc("Interfaces for the FetchUnit.")
        .table(
            """|Port|Dir|Type|Semantics
               |pcIn|In|Decoupled[PC]|Final PC from PCGen
               |fetchReqOut|Out|Decoupled[FetchReq]|Fetch request to ReqTable
               |epochIn|In|UInt(1.W)|Global current epoch
               |""".stripMargin
        )
        .has(bndPc, bndFetchReq)
        .build()
    }

    val funcGenerateFetchReq = spec {
        FUNCTION("FUNC_FETCH_GEN_REQ")
        .is(contFetchUnit)
        .desc("Creates a FetchReq bundle based on the input PC.")
        .entry("Address Alignment", "Aligns the incoming PC to the memory fetch width boundary (e.g., 64-bit).")
        .entry("Epoch Tagging", "Tags the outgoing `fetchReqOut` with the current `epochIn` value.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/frontend/ReqTableSpecs.scala
// =============================================================================
package klase32.specs.frontend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._
import klase32.specs.common.PayloadsSpecs._

object ReqTableSpecs {
    val contReqTable = spec {
        CONTRACT("CONT_REQ_TABLE")
        .desc("Tracks outstanding memory fetch requests and reorders out-of-order responses to maintain program order. Guarantees in-PC-order on output; drops responses when epoch mismatched. Latency is queue(2) as the head-of-line may wait for an older response.")
        .build()
    }

    val intfReqTable = spec {
        INTERFACE("INTF_REQ_TABLE")
        .is(contReqTable)
        .desc("Interfaces for the ReqTable. Parameter Hooks: none (two outstanding requests fixed by genIdWidth=2).")
        .table(
            """|Port|Dir|Type|Semantics
               |reqIn|In|Decoupled[FetchReq]|From FetchUnit; assigns tag genId.
               |rspIn|In|Decoupled[FetchRsp]|Memory response (unordered).
               |out|Out|Decoupled[FetchRsp]|Ordered stream to AlignSlice.
               |epochIn|In|UInt(1.W)|Broadcast; kill pending requests on mismatch.
               |""".stripMargin
        )
        .has(bndFetchReq, bndFetchRsp)
        .build()
    }

    val funcReqTableLogic = spec {
        FUNCTION("FUNC_REQ_TABLE_LOGIC")
        .is(contReqTable)
        .desc("Manages pending requests and reorders responses. PPA Hint: Pending table can be implemented as LUT-RAM; RAM depth <= 4 suggests registers on FPGA.")
        .entry("Pending Table", "A register `Reg(Vec(4, Bool))` indexed by `genId`. On `reqIn.fire`, the corresponding entry is marked pending.")
        .entry("Response Handling", "On `rspIn.fire`, stores data in a 2-slot RAM and clears the corresponding pending bit.")
        .entry("Reordering", "An arbiter drains the response RAM in ascending `genId` order. It waits if an earlier `genId` has not yet received its data.")
        .entry("Epoch Mismatch", "A global epoch mismatch flushes the entire pending table and response RAM.")
        .build()
    }
    
    val propReqTableFormal = spec {
        PROPERTY("PROP_REQ_TABLE_FORMAL")
        .is(contReqTable)
        .desc("Formal/DV obligations for the ReqTable.")
        .entry("Assertion", "Never two active pending lines with the same `genId` tag.")
        .entry("Coverage", "Cover the case where memory responses arrive in reverse order of request and are still retired in the correct order.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/frontend/AlignSliceSpecs.scala
// =============================================================================
package klase32.specs.frontend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._
import klase32.specs.common.PayloadsSpecs._

object AlignSliceSpecs {
    val contAlignSlice = spec {
        CONTRACT("CONT_ALIGN_SLICE")
        .desc("Aligns fetch data from memory, slices it into potential instruction words, and generates pre-decode hints for the BPU. Latency is direct (combinational).")
        .build()
    }

    val intfAlignSlice = spec {
        INTERFACE("INTF_ALIGN_SLICE")
        .is(contAlignSlice)
        .desc("Interfaces for AlignSlice. Emits exactly two InstrWord per 64-bit input line.")
        .table(
            """|Port|Dir|Type|Semantics
               |in|In|Decoupled[FetchRsp]|Input fetch data from ReqTable.
               |hintOut|Out|Decoupled[Hint]|Pre-decode hint to BranchPredictor.
               |out|Out|Decoupled[InstrWord]|Sliced 32-bit instruction words.
               |epochIn|In|UInt(1.W)|Global current epoch.
               |""".stripMargin
        )
        .has(bndFetchRsp, bndHint, bndInstrWord)
        .build()
    }
    
    val funcAlignSliceLogic = spec {
        FUNCTION("FUNC_ALIGN_SLICE_LOGIC")
        .is(contAlignSlice)
        .desc("Slices data and generates hints.")
        .entry("Word Selection", "Selects the upper or lower 32-bit word from the 64-bit input data via `pc(2)`.")
        .entry("Hint Generation", "`isBranch` is true if opcode is in {BRANCH, JAL, JALR}. `isBackward` is true if the branch target immediate is negative.")
        .entry("Exception Handling", "Asserts an exception flag (to be handled in a later stage) if a 32-bit instruction fetch is misaligned (`pc(1)` is high).")
        .build()
    }
    
    val propAlignSliceFormal = spec {
        PROPERTY("PROP_ALIGN_SLICE_FORMAL")
        .is(contAlignSlice)
        .desc("Assert that the fetched word matches memory data when the PC is aligned.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/frontend/RvcExpanderSpecs.scala
// =============================================================================
package klase32.specs.frontend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._
import klase32.specs.common.PayloadsSpecs._

object RvcExpanderSpecs {
    val contRvcExpander = spec {
        CONTRACT("CONT_RVC_EXPANDER")
        .desc("Expands 16-bit RISC-V Compressed (RVC) instructions into their 32-bit equivalents. This vertex is conditionally generated. Latency is direct (combinational).")
        .build()
    }

    val intfRvcExpander = spec {
        INTERFACE("INTF_RVC_EXPANDER")
        .is(contRvcExpander)
        .desc("Interfaces for the RvcExpander. Parameter Hook: `cfg.useRvcExpander:Boolean`. If false, this vertex acts as a pass-through.")
        .table(
            """|Port|Dir|Type|Semantics
               |in|In|Decoupled[InstrWord]|Input instruction word.
               |out|Out|Decoupled[InstrWord]|Expanded 32-bit instruction word.
               |""".stripMargin
        )
        .has(bndInstrWord)
        .build()
    }
    
    val funcRvcExpanderLogic = spec {
        FUNCTION("FUNC_RVC_EXPANDER_LOGIC")
        .is(contRvcExpander)
        .desc("Detects and expands RVC instructions.")
        .entry("Detection", "Detects a 16-bit compressed instruction pattern via `word(1:0) =/= 2'b11`.")
        .entry("Expansion", "Uses a combinational lookup-table (LUT) to expand the 16-bit instruction to its 32-bit equivalent, as defined by the RISC-V ISA.")
        .entry("Exception Handling", "Raises an `except` bit for any illegal or reserved RVC encodings.")
        .build()
    }
    
    val propRvcExpanderFormal = spec {
        PROPERTY("PROP_RVC_EXPANDER_FORMAL")
        .is(contRvcExpander)
        .desc("Cover every valid compressed opcode at least once during verification.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/frontend/IssueQueueSpecs.scala
// =============================================================================
package klase32.specs.frontend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._
import klase32.specs.common.PayloadsSpecs._

object IssueQueueSpecs {
    val contIssueQueue = spec {
        CONTRACT("CONT_ISSUE_QUEUE")
        .desc("A FIFO buffer that sits at the boundary of the IF and IE stages. It decouples the frontend from backend stalls. Latency is queue(`cfg.iqDepth`).")
        .build()
    }
    
    val intfIssueQueue = spec {
        INTERFACE("INTF_ISSUE_QUEUE")
        .is(contIssueQueue)
        .desc("Interfaces for the IssueQueue.")
        .table(
            """|Port|Dir|Type|Semantics
               |in|In|Decoupled[InstrWord]|Instruction word from RvcExpander.
               |selIn|In|Decoupled[SelTok]|(PRE_DECODE only) Selects which entry to issue.
               |out|Out|Decoupled[InstrToken]|Final token to Decoder.
               |epochIn|In|UInt(1.W)|Global current epoch.
               |""".stripMargin
        )
        .has(bndInstrWord, bndSelTok, bndInstrToken)
        .build()
    }
    
    val funcIssueQueueLogic = spec {
        FUNCTION("FUNC_ISSUE_QUEUE_LOGIC")
        .is(contIssueQueue)
        .desc("Manages the instruction buffer and token creation.")
        .entry("Buffering", "Standard FIFO logic. Exerts back-pressure (`in.ready` low) to the frontend when full.")
        .entry("Selection", "If `selIn` is valid (in PRE_DECODE mode), the entry at `selIn.bits.selectIdx` is issued. Otherwise, standard FIFO pop from head.")
        .entry("Tokenization", "Adds the current `epoch` and BPU prediction info (`pred`) to the output `InstrToken`. The `pred` info is assumed to be passed piggy-back alongside the `InstrWord` through the prior stage.")
        .entry("Flush", "Flushes all entries if the internal epoch of an entry does not match the global `epochIn`.")
        .build()
    }
    
    val propIssueQueueFormal = spec {
        PROPERTY("PROP_ISSUE_QUEUE_FORMAL")
        .is(contIssueQueue)
        .desc("Formal/DV obligations.")
        .entry("Assertion", "Assert that the queue depth never exceeds `cfg.iqDepth`.")
        .entry("Coverage", "Cover the case where `selIn` is used to select and issue an entry that is not at the head of the queue.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/backend/DecoderSpecs.scala
// =============================================================================
package klase32.specs.backend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._
import klase32.specs.common.PayloadsSpecs._

object DecoderSpecs {
    val contDecoder = spec {
        CONTRACT("CONT_DECODER")
        .desc("Decodes incoming instruction tokens into control signals for all execution units and determines operand sources. Latency is direct (combinational) for decode, but can stall. Reads the Register File via two combinational read ports.")
        .build()
    }
    
    val intfDecoder = spec {
        INTERFACE("INTF_DECODER")
        .is(contDecoder)
        .desc("Interfaces for the Decoder. Parameter Hooks: `hazardPolicy`, `bypassSet`.")
        .table(
            """|Port|Dir|Type|Semantics
               |in|In|Decoupled[InstrToken]|Instruction token from IssueQueue.
               |rfReq0/1|Out|Decoupled[RegReadReq]|Read requests to RegisterFile for rs1/rs2.
               |rfData0/1|In|Decoupled[UInt(32.W)]|Read data from RegisterFile.
               |fuReq(alu,bit,mul,div)|Out|Decoupled[FUReq]|Requests to various functional units.
               |lsuReq|Out|Decoupled[LSUReq]|Request to Load-Store Unit.
               |csrReq|Out|Decoupled[CSRReq]|Request to CSR File.
               |busyQuery|Out|Decoupled[BusyQuery]|Query to Scoreboard for RAW hazards.
               |epochIn|In|UInt(1.W)|Global current epoch.
               |""".stripMargin
        )
        .has(bndInstrToken, bndRegReadReq, bndFuReq, bndLsuReq, bndCsrReq, bndBusyQuery)
        .build()
    }
    
    val funcDecoderLogic = spec {
        FUNCTION("FUNC_DECODER_LOGIC")
        .is(contDecoder)
        .desc("Decodes instructions and handles hazards. PPA Hint: Decode ROM can be combinational LUTs. For higher Fmax, add a register stage (increasing latency to 1).")
        .entry("Decode", "A Decode ROM (implemented as combinational logic) maps the instruction opcode to internal control signals for all other units.")
        .entry("Hazard Handling", "Stalls the input (`in.ready = 0`) if `busyQuery` response indicates a busy source register (`rs1Busy || rs2Busy`).")
        .entry("Bypass Logic", "Generates a bypass hit vector to control forwarding paths based on results from later pipeline stages.")
        .build()
    }
    
    val propDecoderFormal = spec {
        PROPERTY("PROP_DECODER_FORMAL")
        .is(contDecoder)
        .desc("Formal/DV obligations.")
        .entry("Assertion", "Assert that an illegal instruction correctly sets the exception flag in the `csrReq` bundle.")
        .entry("Coverage", "Cover the case where a RAW hazard is correctly resolved via a pipeline stall.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/backend/RegisterFileSpecs.scala
// =============================================================================
package klase32.specs.backend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._
import klase32.specs.common.PayloadsSpecs._

object RegisterFileSpecs {
    val contRegisterFile = spec {
        CONTRACT("CONT_REGISTER_FILE")
        .desc("Implements the RISC-V general-purpose register file with configurable read and write ports. Features direct (combinational) read and synchronous write.")
        .build()
    }
    
    val intfRegisterFile = spec {
        INTERFACE("INTF_REGISTER_FILE")
        .is(contRegisterFile)
        .desc("Defines the read and write ports of the register file.")
        .table(
            """|Port|Dir|Type|Semantics
               |rd0/1|In|Decoupled[RegReadReq]|Read port requests from Decoder.
               |rdata0/1|Out|Decoupled[UInt(32.W)]|Read data response to Decoder.
               |wr0/1|In|Valid[RegWrite]|Write port requests from WritebackUnit/CommitUnit.
               |epochIn|In|UInt(1.W)|Global current epoch for write validation.
               |""".stripMargin
        )
        .has(bndRegReadReq, bndRegWrite)
        .build()
    }
    
    val funcRegisterFileLogic = spec {
        FUNCTION("FUNC_REGISTER_FILE_LOGIC")
        .is(contRegisterFile)
        .desc("Core logic for reading and writing registers.")
        .entry("Read Logic", "A memory (LUT) that maps read address to register data. Reads to address `x0` always return 0.")
        .entry("Write Logic", "Writes are masked by epoch. A write is only performed if the `RegWrite` bundle's epoch matches the current global epoch, preventing writes from flushed instructions.")
        .build()
    }

    val propRegisterFileFormal = spec {
        PROPERTY("PROP_REGISTER_FILE_FORMAL")
        .is(contRegisterFile)
        .desc("Assert that a write request with `rd=0` does not change the state of the register file.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/backend/ALUSpecs.scala
// =============================================================================
package klase32.specs.backend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._
import klase32.specs.backend.common.FUCommon.intfFU

object ALUSpecs {
    val contALU = spec {
        CONTRACT("CONT_ALU")
        .desc("Performs standard integer arithmetic and logical operations.")
        .build()
    }

    val intfALU = intfFU.is(contALU)

    val funcALUOps = spec {
        FUNCTION("FUNC_ALU_OPS")
        .is(contALU)
        .desc("Implements the base integer instruction set operations.")
        .entry("Supported Operations", "ADD, SUB, AND, OR, XOR, SLL, SRL, SRA, SLT, SLTU.")
        .entry("Parameter Hooks", "`bypassSet.xy` gates the output `ready` signal for dependency handling.")
        .build()
    }

    val propAluFormal = spec {
        PROPERTY("PROP_ALU_FORMAL")
        .is(contALU)
        .desc("Placeholder for ALU formal properties. Ensures every opcode is covered and results match a golden model.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/backend/BitALUSpecs.scala
// =============================================================================
package klase32.specs.backend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._
import klase32.specs.backend.common.FUCommon.intfFU

object BitALUSpecs {
    val contBitALU = spec {
        CONTRACT("CONT_BIT_ALU")
        .desc("Handles RISC-V Bit-Manipulation extension instructions. Conditionally generated.")
        .build()
    }

    val intfBitALU = intfFU.is(contBitALU)

    val funcBitALUOps = spec {
        FUNCTION("FUNC_BIT_ALU_OPS")
        .is(contBitALU)
        .desc("Implements bit manipulation operations from Zba/Zbb/Zbc/Zbs extensions.")
        .entry("Supported Operations", "CLZ, CTZ, BSET, BCLR, BINV, etc.")
        .entry("Parameter Hooks", "`bypassSet.xy` gates the output `ready` signal for dependency handling.")
        .build()
    }

    val propBitAluFormal = spec {
        PROPERTY("PROP_BITALU_FORMAL")
        .is(contBitALU)
        .desc("Placeholder for BitALU formal properties. Covers all bit-manipulation opcodes.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/backend/MultiplierSpecs.scala
// =============================================================================
package klase32.specs.backend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._
import klase32.specs.backend.common.FUCommon.intfFU

object MultiplierSpecs {
    val contMultiplier = spec {
        CONTRACT("CONT_MULTIPLIER")
        .desc("Performs integer multiplication. It has a variable latency and signals its busy status via the Decoupled handshake.")
        .build()
    }
    
    val intfMultiplier = intfFU.is(contMultiplier)

    val propMultiplierFormal = spec {
        PROPERTY("PROP_MULTIPLIER_FORMAL")
        .is(contMultiplier)
        .desc("Placeholder for Multiplier formal properties. Covers early-out paths and multi-cycle stalls.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/backend/DividerSpecs.scala
// =============================================================================
package klase32.specs.backend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._
import klase32.specs.backend.common.FUCommon.intfFU

object DividerSpecs {
    val contDivider = spec {
        CONTRACT("CONT_DIVIDER")
        .desc("Performs integer division and remainder operations. It is a slow, multi-cycle unit that stalls the pipeline when busy.")
        .build()
    }

    val intfDivider = intfFU.is(contDivider)
    
    val propDividerFormal = spec {
        PROPERTY("PROP_DIVIDER_FORMAL")
        .is(contDivider)
        .desc("Placeholder for Divider formal properties. Must cover divide-by-zero exception.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/backend/LSUSpecs.scala
// =============================================================================
package klase32.specs.backend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._
import klase32.specs.common.PayloadsSpecs._

object LSUSpecs {
    val contLSU = spec {
        CONTRACT("CONT_LSU")
        .desc("Handles all memory operations (loads and stores), including address calculation, data alignment, and external memory interface management.")
        .build()
    }

    val intfLSU = spec {
        INTERFACE("INTF_LSU")
        .is(contLSU)
        .desc("Interfaces for the Load-Store Unit. Parameter Hook: an optional data-cache can be added later.")
        .table(
            """|Port|Dir|Type|Semantics
               |reqIn|In|Decoupled[LSUReq]|Load/Store request from Decoder.
               |axiReqOut|Out|Decoupled[DataReq]|Request to external memory via AXI.
               |axiRspIn|In|Decoupled[DataRsp]|Response from external memory via AXI.
               |loadDataOut|Out|Decoupled[LoadData]|Load data to Writeback stage.
               |epochIn|In|UInt(1.W)|Global current epoch.
               |""".stripMargin
        )
        .has(bndLsuReq, bndDataReq, bndDataRsp, bndLoadData)
        .build()
    }

    val funcLsuLogic = spec {
        FUNCTION("FUNC_LSU_LOGIC")
        .is(contLSU)
        .desc("Implements the LSU operation flow and memory interface handling.")
        .entry("Request Generation", "For loads, issues a read on `axiReqOut` (isWrite=false) with the given address and size. For stores, issues a write (isWrite=true) with `wdata` and `wstrb` derived from `size` and address alignment.")
        .entry("Response Handling", "On a load response (`axiRspIn.fire`), forwards the data to `loadDataOut` (with the original `rd`). Store responses (write acknowledgments) are consumed internally without external output.")
        .entry("Alignment & Extension", "Ensures correct alignment and byte enable signals. If a load is sign-extended (`signExt=true`), extends the loaded data to 32 bits; otherwise, zero-extends.")
        .entry("Flush Handling", "If a response arrives with an epoch that does not match the current `epochIn` (i.e., from a flushed operation), that response is ignored (dropped) and not sent to `loadDataOut`.")
        .build()
    }

    val propLsuFormal = spec {
        PROPERTY("PROP_LSU_FORMAL")
        .is(contLSU)
        .desc("Placeholder for LSU formal properties. Covers all access sizes, sign extensions, and misaligned exceptions.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/backend/CSRFileSpecs.scala
// =============================================================================
package klase32.specs.backend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._
import klase32.specs.common.PayloadsSpecs._

object CSRFileSpecs {
    val contCSRFile = spec {
        CONTRACT("CONT_CSR_FILE")
        .desc("Manages all Control and Status Registers (CSRs), handling privileged operations and exceptions.")
        .build()
    }

    val intfCSRFile = spec {
        INTERFACE("INTF_CSR_FILE")
        .is(contCSRFile)
        .desc("Interfaces for the CSR File.")
        .table(
            """|Port|Dir|Type|Semantics
               |csrReq|In|Decoupled[CSRReq]|Request from Decoder.
               |csrResp|Out|Decoupled[CSRResp]|Response to Writeback/Commit.
               |irqMask|Out|Valid[IrqMask]|Interrupt enable masks to InterruptCtrl.
               |""".stripMargin
        )
        .has(bndCsrReq, bndCsrResp, bndIrqMask)
        .build()
    }

    val funcCsrFileLogic = spec {
        FUNCTION("FUNC_CSR_FILE_LOGIC")
        .is(contCSRFile)
        .desc("Describes the behavior of the CSR file on CSR requests.")
        .entry("CSR Register Map", "Maintains all required CSRs (e.g., mstatus, mie, mtvec, mepc, etc.) as internal state.")
        .entry("Read/Write Operation", "On `csrReq.fire`, if the operation is RW (write), updates the targeted CSR with `wdata`. If RS (set bits) or RC (clear bits), modifies the CSR by setting/clearing the bits provided in `wdata`. The old value (or unmodified value for pure reads) is returned in `csrResp.rdata`.")
        .entry("Privilege Check", "Verifies that the requested CSR can be accessed (correct privilege level and existence). If not, sets `csrResp.except` to true (signaling an illegal access exception) and ignores any write.")
        .entry("Interrupt Enables", "If a CSR write affects interrupt enable bits (e.g., the MIE register or similar), updates the `irqMask` outputs accordingly to reflect the new enable bits.")
        .build()
    }
    
    val propCsrFileFormal = spec {
        PROPERTY("PROP_CSRFILE_FORMAL")
        .is(contCSRFile)
        .desc("Placeholder for CSRFile formal properties. Covers privilege checks and illegal access exceptions.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/backend/BranchCompareSpecs.scala
// =============================================================================
package klase32.specs.backend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._
import klase32.specs.common.PayloadsSpecs._

object BranchCompareSpecs {
    val contBranchCompare = spec {
        CONTRACT("CONT_BRANCH_COMPARE")
        .desc("Compares source registers for branch instructions to determine the actual outcome. Generates an early redirect on mis-prediction.")
        .build()
    }

    val intfBranchCompare = spec {
        INTERFACE("INTF_BRANCH_COMPARE")
        .is(contBranchCompare)
        .desc("Interfaces for BranchCompare.")
        .table(
            """|Port|Dir|Type|Semantics
               |in|In|Decoupled[FUReq]|Branch instruction details from Decoder.
               |redirectOut|Out|Decoupled[Redirect]|Early redirect signal to PCGen on mispredict.
               |""".stripMargin
        )
        .has(bndFuReq, bndRedirect)
        .build()
    }
    
    val funcBranchCompareLogic = spec {
        FUNCTION("FUNC_BRANCH_COMPARE_LOGIC")
        .is(contBranchCompare)
        .desc("Evaluates the branch condition and triggers a pipeline redirect if the prediction was wrong.")
        .entry("Condition Evaluation", "Checks `in.bits.op` to perform the appropriate comparison (e.g., equal, not-equal, less-than) on the source operands `rs1` and `rs2`.")
        .entry("Branch Outcome", "Determines the actual branch outcome (taken or not taken) and calculates the actual target PC (for taken branches).")
        .entry("Redirect on Mispredict", "If the actual outcome differs from the BPU prediction, or if a taken branch's actual target differs from the predicted target, asserts `redirectOut.valid` with `targetPc` set to the correct next PC (branch target or sequential PC).")
        .build()
    }
    
    val propBranchCompareFormal = spec {
        PROPERTY("PROP_BRANCH_COMPARE_FORMAL")
        .is(contBranchCompare)
        .desc("Cover a backward-taken loop that is initially mis-predicted as not-taken, verifying the early redirect mechanism.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/backend/ScoreboardSpecs.scala
// =============================================================================
package klase32.specs.backend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._
import klase32.specs.common.PayloadsSpecs._

object ScoreboardSpecs {
    val contScoreboard = spec {
        CONTRACT("CONT_SCOREBOARD")
        .desc("Tracks register dependencies (RAW hazards) to stall the pipeline when necessary. Implements a simple busy-bit table.")
        .build()
    }

    val intfScoreboard = spec {
        INTERFACE("INTF_SCOREBOARD")
        .is(contScoreboard)
        .desc("Interfaces for the Scoreboard.")
        .table(
            """|Port|Dir|Type|Semantics
               |wrIn|In|Valid[RegWrite]|From Commit stage to clear busy bit.
               |queryIn|In|Decoupled[BusyQuery]|From Decoder to check for hazards.
               |stallOut|Out|Bool|Stall signal to Decoder.
               |""".stripMargin
        )
        .has(bndRegWrite, bndBusyQuery)
        .build()
    }
    
    val funcScoreboardLogic = spec {
        FUNCTION("FUNC_SCOREBOARD_LOGIC")
        .is(contScoreboard)
        .desc("Implements the busy-bit tracking and hazard detection logic.")
        .entry("Busy-bit Table", "Maintains an array of busy flags for registers (e.g., 32 entries, where x0 is always not busy).")
        .entry("Hazard Check", "On a query (`queryIn.fire`), if either source register index (`rs1` or `rs2`) is marked busy, asserts `stallOut` to prevent issuing the instruction.")
        .entry("Marking Busy", "If no hazard is detected and the instruction will write a register (`doWrite` true and `rdIdx != 0`), sets the busy flag for `rdIdx` to indicate a pending write.")
        .entry("Clearing", "On commit (`wrIn.fire`), clears the busy flag for the completed instruction's destination register (if not x0). On a pipeline flush (epoch change), all busy flags are cleared to avoid false dependencies.")
        .build()
    }
    
    val propScoreboardFormal = spec {
        PROPERTY("PROP_SCOREBOARD_FORMAL")
        .is(contScoreboard)
        .desc("Assert that `stallOut` is high when `queryIn` indicates a dependency on a register whose busy bit is set.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/backend/CommitUnitSpecs.scala
// =============================================================================
package klase32.specs.backend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._
import klase32.specs.common.PayloadsSpecs._

object CommitUnitSpecs {
    val contCommitUnit = spec {
        CONTRACT("CONT_COMMIT_UNIT")
        .desc("Final stage of the pipeline. Commits architectural state, handles exceptions and interrupts, and generates flush/redirect signals.")
        .build()
    }

    val intfCommitUnit = spec {
      INTERFACE("INTF_COMMIT_UNIT")
        .is(contCommitUnit)
        .desc("Consumes RegWrite, handles traps, issues flush/redirect.")
        .table(
          """|Port         |Dir|Type             |Semantics
             |regWriteIn   |In |Decoupled[RegWrite]|From WritebackUnit
             |irqTokIn     |In |Valid[IrqTok]    |From InterruptCtrl
             |csrRespIn    |In |Valid[CSRResp]   |For CSR side-effects
             |flushCmdOut  |Out|Valid[FlushCmd]  |To GlobalEpochCtrl
             |lateRedirectOut|Out|Valid[Redirect]  |Exception/IRQ redirect
             |regWriteBcast|Out|Valid[RegWrite]  |Pass-through to RegFile & Scoreboard
             |""".stripMargin)
        .has(bndRegWrite, bndIrqTok, bndFlushCmd, bndRedirect, bndCsrResp)
        .build()
    }
    
    val propCommitUnitFormal = spec {
        PROPERTY("PROP_COMMIT_UNIT_FORMAL")
        .is(contCommitUnit)
        .desc("Assert that at most one flush command is generated per cycle.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/backend/LoadReturnBufferSpecs.scala
// =============================================================================
package klase32.specs.backend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._
import klase32.specs.common.PayloadsSpecs._

object LoadReturnBufferSpecs {
    val contLoadReturnBuffer = spec {
        CONTRACT("CONT_LOAD_RETURN_BUFFER")
        .desc("A 1-entry buffer to hold load data for one cycle, resolving structural hazards on the register file write ports when `loadWBSeparate` is true.")
        .build()
    }
    
    val intfLRB = spec {
      INTERFACE("INTF_LOAD_RETURN_BUFFER")
        .is(contLoadReturnBuffer)
        .desc("Optional 1-deep queue between LSU and Writeback.")
        .table(
          """|Port |Dir|Type           |Semantics
             |in   |In |Decoupled[LoadData]|From LSU
             |out  |Out|Decoupled[LoadData]|To WritebackUnit
             |""".stripMargin)
        .has(bndLoadData)
        .build()
    }
    
    val propLoadReturnBufferFormal = spec {
        PROPERTY("PROP_LRB_FORMAL")
        .is(contLoadReturnBuffer)
        .desc("Cover an overwrite hazard that would occur if this buffer were disabled or not present, ensuring its functionality is necessary under the specified configuration.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/backend/WritebackUnitSpecs.scala
// =============================================================================
package klase32.specs.backend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._
import klase32.specs.common.PayloadsSpecs._

object WritebackUnitSpecs {
    val contWritebackUnit = spec {
        CONTRACT("CONT_WRITEBACK_UNIT")
        .desc("Arbitrates results from all execution units and writes them back to the RegisterFile.")
        .build()
    }

    val intfWriteback = spec {
      INTERFACE("INTF_WRITEBACK_UNIT")
        .is(contWritebackUnit)
        .desc("Receives FU responses, arbitrates, emits canonical RegWrite.")
        .table(
          """|Port        |Dir|Type              |Semantics
             |aluIn       |In |Decoupled[FUResp]|From ALU
             |bitIn       |In |Decoupled[FUResp]|From Bit-ALU (optional)
             |mulIn       |In |Decoupled[FUResp]|From Multiplier
             |divIn       |In |Decoupled[FUResp]|From Divider
             |lsuIn       |In |Decoupled[LoadData]|From LoadReturnBuffer/LSU
             |csrIn       |In |Decoupled[CSRResp]|CSR read/modify result
             |regWriteOut |Out|Decoupled[RegWrite]|To CommitUnit
             |""".stripMargin)
        .has(bndFuResp, bndLoadData, bndCsrResp, bndRegWrite)
        .build()
    }
    
    val propWritebackFormal = spec {
        PROPERTY("PROP_WRITEBACK_FORMAL")
        .is(contWritebackUnit)
        .desc("Assert there are no lost `valid` signals; i.e., all valid input responses are eventually granted by the arbiter and fired.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/control/GlobalEpochCtrlSpecs.scala
// =============================================================================
package klase32.specs.control

import framework.macros.SpecEmit.spec
import framework.specs.Spec._
import klase32.specs.common.PayloadsSpecs._

object GlobalEpochCtrlSpecs {
    val contGlobalEpochCtrl = spec {
        CONTRACT("CONT_GLOBAL_EPOCH_CTRL")
        .desc("Manages the global 1-bit epoch. Toggles the epoch upon receiving a flush command and broadcasts the new epoch to all vertices.")
        .build()
    }
    
    val intfGlobalEpochCtrl = spec {
        INTERFACE("INTF_GLOBAL_EPOCH_CTRL")
        .is(contGlobalEpochCtrl)
        .desc("Interfaces for GlobalEpochCtrl.")
        .table(
            """|Port|Dir|Type|Semantics
               |flushIn|In|Valid[FlushCmd]|Flush command from CommitUnit.
               |quiescentIn|In|Bool|Flag indicating pipeline is empty (AND of IssueQ.empty & ReqTable.pending=0).
               |epochOut|Out|UInt(1.W)|Global epoch signal (always valid).
               |""".stripMargin
        )
        .has(bndFlushCmd)
        .build()
    }

    val funcGlobalEpochLogic = spec {
        FUNCTION("FUNC_GLOBAL_EPOCH_LOGIC")
        .is(contGlobalEpochCtrl)
        .desc("Controls the toggling of the global epoch bit on flush events.")
        .entry("Epoch Register", "Maintains the current epoch bit (0 or 1). Initialized to 0 after reset.")
        .entry("Flush Trigger", "When a flush command is received (`flushIn.valid`), the controller waits until the pipeline is quiescent (`quiescentIn=true`).")
        .entry("Toggle", "Once quiescent, flips the epoch bit (0->1 or 1->0) exactly once, and then releases the flush (new epoch value is driven on `epochOut`).")
        .build()
    }

    val propGlobalEpochFormal = spec {
        PROPERTY("PROP_GLOBAL_EPOCH_FORMAL")
        .is(contGlobalEpochCtrl)
        .desc("Assert that the epoch only toggles if the pipeline is quiescent.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/control/InterruptCtrlSpecs.scala
// =============================================================================
package klase32.specs.control

import framework.macros.SpecEmit.spec
import framework.specs.Spec._
import klase32.specs.common.PayloadsSpecs._

object InterruptCtrlSpecs {
    val contInterruptCtrl = spec {
        CONTRACT("CONT_INTERRUPT_CTRL")
        .desc("Monitors external interrupt lines, synchronizes them, and issues an interrupt token to the CommitUnit when an interrupt is pending and enabled.")
        .build()
    }

    val intfInterruptCtrl = spec {
        INTERFACE("INTF_INTERRUPT_CTRL")
        .is(contInterruptCtrl)
        .desc("Interfaces for InterruptCtrl.")
        .table(
            """|Port|Dir|Type|Semantics
               |meip|In|Bool|Raw machine external interrupt pin.
               |mtip|In|Bool|Raw machine timer interrupt pin.
               |msip|In|Bool|Raw machine software interrupt pin.
               |irqOut|Out|Valid[IrqTok]|Interrupt token to CommitUnit.
               |""".stripMargin
        )
        .has(bndIrqTok)
        .build()
    }

    val funcInterruptCtrlLogic = spec {
        FUNCTION("FUNC_INTERRUPT_CTRL_LOGIC")
        .is(contInterruptCtrl)
        .desc("Detects and prioritizes interrupts and notifies the CommitUnit.")
        .entry("Masking", "Only consider an interrupt line if its corresponding enable bit (from `IrqMask`) is set. All raw interrupt inputs are synchronized before use.")
        .entry("Priority Scheme", "If multiple interrupts are asserted simultaneously, external (MEIP) is given highest priority, then timer (MTIP), then software (MSIP).")
        .entry("Issuing Interrupt", "When a pending enabled interrupt is detected, `irqOut.valid` is asserted with the `IrqTok.cause` field set (e.g., 11 for external, 7 for timer, 3 for software). The token remains asserted until accepted by CommitUnit.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/control/DebugTraceSpecs.scala
// =============================================================================
package klase32.specs.control

import framework.macros.SpecEmit.spec
import framework.specs.Spec._

object DebugTraceSpecs {
    val contDebugTrace = spec {
        CONTRACT("CONT_DEBUG_TRACE")
        .desc("Passively taps the commit stage to provide instruction retirement information for debugging and formal verification (RVFI).")
        .build()
    }

    val propDebugTraceFormal = spec {
        PROPERTY("PROP_DEBUG_TRACE_FORMAL")
        .is(contDebugTrace)
        .desc("Assert `rvfi_valid == commit.fire`, ensuring trace validity matches instruction retirement.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/control/ForceStallGateSpecs.scala
// =============================================================================
package klase32.specs.control

import framework.macros.SpecEmit.spec
import framework.specs.Spec._

object ForceStallGateSpecs {
    val contForceStallGate = spec {
        CONTRACT("CONT_FORCE_STALL_GATE")
        .desc("A simple logic gate that combines external stall requests with the internal pipeline stall signals to control the `ready` signal of the IssueQueue.")
        .build()
    }
    
    val intfForceStall = spec {
      INTERFACE("INTF_FORCE_STALL_GATE")
        .is(contForceStallGate)
        .desc("Combines external and internal stall conditions.")
        .table(
          """|Port        |Dir|Type|Semantics
             |extStallIn  |In |Bool|External force-stall pin
             |iqReadyIn   |In |Bool|IssueQueue ready status
             |stallOut    |Out|Bool|Final stall signal driving IQ.ready
             |""".stripMargin)
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/top/CoreEnableSequencerSpecs.scala
// =============================================================================
package klase32.specs.top

import framework.macros.SpecEmit.spec
import framework.specs.Spec._
import klase32.specs.common.PayloadsSpecs._

object CoreEnableSequencerSpecs {
    val contCoreEnableSequencer = spec {
        CONTRACT("CONT_CORE_ENABLE_SEQUENCER")
        .desc("Waits for `hartEn` signal and then provides the initial boot address to the PCGen to start the fetch pipeline.")
        .build()
    }
    
    val intfCoreEnableSequencer = spec {
        INTERFACE("INTF_CORE_ENABLE_SEQUENCER")
        .is(contCoreEnableSequencer)
        .desc("Interfaces for CoreEnableSequencer.")
        .table(
          """|Port   |Dir|Type           |Semantics
             |hartEn |In |Bool           |Core enable signal (from top level or testbench)
             |bootOut|Out|Decoupled[Boot]|Boot address and Hart ID to PCGen (pulsed on core enable)
             |""".stripMargin)
        .has(bndBoot)
        .build()
    }
    
    val funcCoreEnableLogic = spec {
        FUNCTION("FUNC_CORE_ENABLE_LOGIC")
        .is(contCoreEnableSequencer)
        .desc("Handles the generation of the one-time boot sequence.")
        .entry("Enable Wait", "Remains idle until `hartEn` is asserted (after reset).")
        .entry("Boot Pulse", "When `hartEn` goes high, issues a single-cycle `bootOut` with the configured `bootAddr` and `hartId`. This starts the core fetching instructions.")
        .entry("One-shot", "Ensures the boot token is sent only once; further assertions of `hartEn` have no effect until a reset occurs.")
        .build()
    }
    
    val propCoreEnableFormal = spec {
        PROPERTY("PROP_CORE_ENABLE_FORMAL")
        .is(contCoreEnableSequencer)
        .desc("Cover that the boot pulse is generated exactly once after reset and after `hartEn` is asserted.")
        .build()
    }
}
