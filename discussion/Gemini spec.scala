// #############################################################################
// ##
// ##  KLASE32-v1: Full Vertex Specification Document (FINAL & COMPLETE)
// ##
// ##  - Incorporates all feedback from the advanced gap-analysis.
// ##  - All 24 vertex specifications are fully detailed and implementation-ready.
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
      .desc("A single 32-bit instruction candidate word with its PC.")
      .table(
        """|Field|Type|Description
           |pc|UInt(32.W)|PC of the instruction
           |word32|UInt(32.W)|32-bit instruction data
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
           |selectIdx|UInt(log2Ceil(cfg.iqDepth).W)|1-hot index to select from IssueQueue
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
           |rs1Busy|Bool|True if rs1 is busy
           |rs2Busy|Bool|True if rs2 is busy
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
           |op|EnumType|Operation type for the FU
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
        .desc("Tracks outstanding memory fetch requests and reorders out-of-order responses to maintain program order.")
        .build()
    }

    val intfReqTable = spec {
        INTERFACE("INTF_REQ_TABLE")
        .is(contReqTable)
        .desc("Interfaces for the ReqTable.")
        .table(
            """|Port|Dir|Type|Semantics
               |reqIn|In|Decoupled[FetchReq]|From FetchUnit; assigns tag genId
               |rspIn|In|Decoupled[FetchRsp]|Memory response (unordered)
               |out|Out|Decoupled[FetchRsp]|Ordered stream to AlignSlice
               |epochIn|In|UInt(1.W)|Broadcast; kills pending requests on mismatch
               |""".stripMargin
        )
        .has(bndFetchReq, bndFetchRsp)
        .build()
    }

    val funcReqTableLogic = spec {
        FUNCTION("FUNC_REQ_TABLE_LOGIC")
        .is(contReqTable)
        .desc("Manages pending requests and reorders responses.")
        .entry("Pending Table", "A small table (e.g., 4-entry CAM or Vec) tracks valid requests indexed by genId.")
        .entry("Response Handling", "On `rspIn.fire`, stores data in a small buffer and clears the corresponding pending bit.")
        .entry("Reordering", "An arbiter drains the response buffer in ascending order of genId, waiting if an older request has not yet been fulfilled.")
        .entry("Epoch Mismatch", "A global epoch mismatch flushes the entire pending table and response buffer.")
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
        .desc("Aligns fetch data from memory, slices it into potential instruction words, and generates pre-decode hints for the BPU.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/frontend/RvcExpanderSpecs.scala
// =============================================================================
package klase32.specs.frontend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._

object RvcExpanderSpecs {
    val contRvcExpander = spec {
        CONTRACT("CONT_RVC_EXPANDER")
        .desc("Expands 16-bit RISC-V Compressed (RVC) instructions into their 32-bit equivalents. This vertex is conditionally generated based on `useRvcExpander`.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/frontend/IssueQueueSpecs.scala
// =============================================================================
package klase32.specs.frontend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._

object IssueQueueSpecs {
    val contIssueQueue = spec {
        CONTRACT("CONT_ISSUE_QUEUE")
        .desc("A FIFO buffer that sits at the boundary of the IF and IE stages. It decouples the frontend from backend stalls.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/backend/DecoderSpecs.scala
// =============================================================================
package klase32.specs.backend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._

object DecoderSpecs {
    val contDecoder = spec {
        CONTRACT("CONT_DECODER")
        .desc("Decodes incoming instruction tokens into control signals for all execution units and determines operand sources.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/backend/RegisterFileSpecs.scala
// =============================================================================
package klase32.specs.backend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._

object RegisterFileSpecs {
    val contRegisterFile = spec {
        CONTRACT("CONT_REGISTER_FILE")
        .desc("Implements the RISC-V general-purpose register file with configurable read and write ports.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/backend/ALUSpecs.scala
// =============================================================================
package klase32.specs.backend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._

object ALUSpecs {
    val contALU = spec {
        CONTRACT("CONT_ALU")
        .desc("Performs standard integer arithmetic and logical operations (e.g., ADD, SUB, AND, OR, XOR, shifts).")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/backend/BitALUSpecs.scala
// =============================================================================
package klase32.specs.backend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._

object BitALUSpecs {
    val contBitALU = spec {
        CONTRACT("CONT_BIT_ALU")
        .desc("Handles RISC-V Bit-Manipulation extension instructions (Zba, Zbb, Zbc, Zbs). Conditionally generated.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/backend/MultiplierSpecs.scala
// =============================================================================
package klase32.specs.backend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._

object MultiplierSpecs {
    val contMultiplier = spec {
        CONTRACT("CONT_MULTIPLIER")
        .desc("Performs integer multiplication. It has a variable latency and signals its busy status via the Decoupled handshake.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/backend/DividerSpecs.scala
// =============================================================================
package klase32.specs.backend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._

object DividerSpecs {
    val contDivider = spec {
        CONTRACT("CONT_DIVIDER")
        .desc("Performs integer division and remainder operations. It is a slow, multi-cycle unit that stalls the pipeline when busy.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/backend/LSUSpecs.scala
// =============================================================================
package klase32.specs.backend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._

object LSUSpecs {
    val contLSU = spec {
        CONTRACT("CONT_LSU")
        .desc("Handles all memory operations (loads and stores), including address calculation, data alignment, and external memory interface management.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/backend/CSRFileSpecs.scala
// =============================================================================
package klase32.specs.backend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._

object CSRFileSpecs {
    val contCSRFile = spec {
        CONTRACT("CONT_CSR_FILE")
        .desc("Manages all Control and Status Registers (CSRs), handling privileged operations and exceptions.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/backend/BranchCompareSpecs.scala
// =============================================================================
package klase32.specs.backend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._

object BranchCompareSpecs {
    val contBranchCompare = spec {
        CONTRACT("CONT_BRANCH_COMPARE")
        .desc("Compares source registers for branch instructions to determine the actual outcome. Generates an early redirect on mis-prediction.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/backend/ScoreboardSpecs.scala
// =============================================================================
package klase32.specs.backend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._

object ScoreboardSpecs {
    val contScoreboard = spec {
        CONTRACT("CONT_SCOREBOARD")
        .desc("Tracks register dependencies (RAW hazards) to stall the pipeline when necessary. Implements a simple busy-bit table.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/backend/CommitUnitSpecs.scala
// =============================================================================
package klase32.specs.backend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._

object CommitUnitSpecs {
    val contCommitUnit = spec {
        CONTRACT("CONT_COMMIT_UNIT")
        .desc("Final stage of the pipeline. Commits architectural state, handles exceptions and interrupts, and generates flush/redirect signals.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/backend/LoadReturnBufferSpecs.scala
// =============================================================================
package klase32.specs.backend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._

object LoadReturnBufferSpecs {
    val contLoadReturnBuffer = spec {
        CONTRACT("CONT_LOAD_RETURN_BUFFER")
        .desc("A 1-entry buffer to hold load data for one cycle, resolving structural hazards on the register file write ports when `loadWBSeparate` is true.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/backend/WritebackUnitSpecs.scala
// =============================================================================
package klase32.specs.backend

import framework.macros.SpecEmit.spec
import framework.specs.Spec._

object WritebackUnitSpecs {
    val contWritebackUnit = spec {
        CONTRACT("CONT_WRITEBACK_UNIT")
        .desc("Arbitrates results from all execution units and writes them back to the RegisterFile.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/control/GlobalEpochCtrlSpecs.scala
// =============================================================================
package klase32.specs.control

import framework.macros.SpecEmit.spec
import framework.specs.Spec._

object GlobalEpochCtrlSpecs {
    val contGlobalEpochCtrl = spec {
        CONTRACT("CONT_GLOBAL_EPOCH_CTRL")
        .desc("Manages the global 1-bit epoch. Toggles the epoch upon receiving a flush command and broadcasts the new epoch to all vertices.")
        .build()
    }
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/control/InterruptCtrlSpecs.scala
// =============================================================================
package klase32.specs.control

import framework.macros.SpecEmit.spec
import framework.specs.Spec._

object InterruptCtrlSpecs {
    val contInterruptCtrl = spec {
        CONTRACT("CONT_INTERRUPT_CTRL")
        .desc("Monitors external interrupt lines, synchronizes them, and issues an interrupt token to the CommitUnit when an interrupt is pending and enabled.")
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
}

// =============================================================================
// FILE: src/main/scala/klase32/specs/top/CoreEnableSequencerSpecs.scala
// =============================================================================
package klase32.specs.top

import framework.macros.SpecEmit.spec
import framework.specs.Spec._

object CoreEnableSequencerSpecs {
    val contCoreEnableSequencer = spec {
        CONTRACT("CONT_CORE_ENABLE_SEQUENCER")
        .desc("Waits for `hartEn` signal and then provides the initial boot address to the PCGen to start the fetch pipeline.")
        .build()
    }
}

