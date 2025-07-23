// CODEX: I'LL PROVIDE MULTIPLE FILES AS SINGLE FILE HERE FOR CONVINIENCE
// CODEX: PLEASE THINK SEPARATELY EACH FILE AS // src/main/ ... /*.scala

// src/main/scala/klase32/frontend/specs/BundlesSpecs.scala
package klase32.frontend.specs

import framework.macros.SpecEmit.spec
import framework.specs.Spec._

/**
 * Specifications for src/main/scala/klase32/frontend/design/Bundles.scala in design
 */

object BundlesSpecs {
  val bndFetchTag = spec {
    BUNDLE("BND_FETCH_TAG")
      .desc("Unique identifier for a fetch request, combining index and generation ID.")
      .entry("idx = UInt(pInternalIdxWidth)", "Internal index within the outstanding request buffer.")
      .entry("gen = UInt(pGenIdWidth)", "Generation ID for flush coherence.")
      .build()
  }

  val bndFetchRequest = spec {
    BUNDLE("BND_FETCH_REQUEST")
      .desc("Data structure for a fetch request to external program memory.")
      // CODEX: THIS IS NOT WRITE WAY TO USE ENTRY. ENTRY ALLOWS ONLY TWO STRING ARGS
      // CODEX: BTW, NEED FOR DESCRIPTION OF TYPE AND PARAMETER FOR EACH ELEMENT IS DEFINITE
      // CODEX: SAME. WHAT CAN BE A GOOD IDEA TO REPRESENT THIS?
      // CODEX: AND WHAT CAN BE A GOOD IDEA TO REPRESENT EACH ELEMENT IS RELATED TO PARAMETER SPEC?
      .entry("addr", "Fetch address.", "UInt", "p.addrWidth")
      .entry("size", "Burst size (e.g., log2(p.fetchWidth/8)).", "UInt", "p.sizeWidth")
      .entry("tag", "Unique request tag.", "Bundle", "bndFetchTag")
      .build()
  }

  val bndFetchResponse = spec {
    BUNDLE("BND_FETCH_RESPONSE")
      .desc("Data structure for a fetch response from external program memory.")
      .entry("data", "Fetched instruction data.", "UInt", "p.fetchWidth")
      .entry("error", "Memory access error.", "Bool")
      .entry("tag", "Matching request tag.", "Bundle", "bndFetchTag")
      .build()
  }

  val bndIssuePacket = spec {
    BUNDLE("BND_ISSUE_PACKET")
      .desc("Packet of expanded 32-bit RVI instructions issued to the Backend.")
      .entry("insts", "Array of 32-bit RVI instructions.", "Vec(UInt(32.W), p.issueWidth)")
      .entry("pc", "PC of the first instruction in the packet.", "UInt", "p.addrWidth")
      .entry("mask", "Valid mask for each slot (1=valid).", "UInt", "p.issueWidth")
      .entry("gen", "Generation ID of issued packet.", "UInt", "p.genIdWidth")
      .entry("tag_id", "Sequential ID for tracking order.", "UInt", "p.seqIdWidth") // Assuming SeqIDWidth is derived
      .build()
  }

  val bndRedirect = spec {
    BUNDLE("BND_REDIRECT")
      .desc("Redirect request from Backend or exception unit.")
      .entry("pc", "Target PC for redirect.", "UInt", "p.addrWidth")
      .entry("flush", "Indicates a full pipeline flush.", "Bool")
      .build()
  }

  val bndBootAddr = spec {
    BUNDLE("BND_BOOT_ADDR")
      .desc("Boot address from Boot Sequencer.")
      .entry("pc", "Initial program counter address.", "UInt", "p.addrWidth")
      .build()
  }

  val bndReqTableEntry = spec {
    BUNDLE("BND_REQ_TABLE_ENTRY")
      .desc("Metadata stored for each outstanding fetch request in the Request Table.")
      .entry("pc", "PC of the fetch request.", "UInt", "p.addrWidth")
      .entry("tag", "Unique request tag.", "Bundle", "bndFetchTag")
      .entry("beatRemain", "Remaining beats for multi-beat bursts (conditionally generated).", "UInt", "p.log2BurstBeatsWidth")
      .entry("valid", "Indicates if this entry is valid.", "Bool")
      .build()
  }

  val bndSlicePacket = spec {
    BUNDLE("BND_SLICE_PACKET")
      .desc("Packet of 16-bit or 32-bit instruction candidates from Align/Slice stage.")
      .entry("slices", "Array of 16-bit instruction candidates.", "Vec(UInt(16.W), p.SLICES_PER_FETCH)") // MODIFIED: Use constant
      .entry("is_rvc", "Mask, 1 if corresponding slice is RVC.", "UInt", "p.SLICES_PER_FETCH") // MODIFIED: Use constant
      .entry("pc", "PC of the first slice in packet.", "UInt", "p.addrWidth")
      .entry("gen", "Generation ID.", "UInt", "p.genIdWidth")
      .build()
  }

  val bndExpandedPacket = spec {
    BUNDLE("BND_EXPANDED_PACKET")
      .desc("Packet of expanded 32-bit RVI instructions from RVC Expander.")
      .entry("insts", "Array of 32-bit RVI instructions.", "Vec(UInt(32.W), p.SLICES_PER_FETCH)") // MODIFIED: Use constant
      .entry("pc", "PC of the first instruction.", "UInt", "p.addrWidth")
      .entry("mask", "Valid mask for each instruction.", "UInt", "p.SLICES_PER_FETCH") // MODIFIED: Use constant
      .entry("gen", "Generation ID.", "UInt", "p.genIdWidth")
      .entry("seq_id", "Sequential ID for tracking order.", "UInt", "p.seqIdWidth")
      .build()
  }
}

// src/main/scala/klase32/frontend/specs/BundlesSpecs.scala
package klase32.frontend.specs

import framework.macros.SpecEmit.spec
import framework.specs.Spec._

/**
 * Specifications for src/main/scala/klase32/frontend/design/Parameters.scala in design
 */

object ParametersSpecs {
  val paramFetchWidth = spec {
    PARAMETER("PARAM_FETCH_WIDTH")
      .desc("Bits per fetch beat for memory accesses.")
      .entry("default", "32")
      .entry("constraint", "Power-of-two multiple of 32 (e.g., 32, 64, 128).")
      .note("Directly impacts parallel fetch capability and bus width requirements.")
      .build()
  }

  val paramSlicesPerFetch = spec { // NEW: Derived constant for Vec lengths
    PARAMETER("P_SLICES_PER_FETCH")
      .desc("Number of 16-bit slices derived from each fetch beat (PARAM_FETCH_WIDTH / 16).")
      .entry("default", "2") // For FETCH_WIDTH = 32
      .note("Used for static sizing of Vec lengths in Chisel. Must be a constant.")
      .build()
  }

  val paramBurstBeats = spec {
    PARAMETER("PARAM_BURST_BEATS")
      .desc("Number of beats per single fetch request.")
      .entry("default", "1")
      .note("Influences Req-Table's `beatRemain` logic for multi-beat bursts.")
      .build()
  }

  val paramMaxOutstanding = spec {
    PARAMETER("PARAM_MAX_OUTSTANDING")
      .desc("Maximum simultaneous outstanding fetch requests.")
      .entry("default", "4")
      .note("Defines the capacity of the Request Table.")
      .build()
  }

  val paramRedirectLatencyCycles = spec {
    PARAMETER("PARAM_REDIRECT_LATENCY_CYCLES")
      .desc("Cycles from redirect detection to actual fetch PC update.")
      .entry("default", "0")
      .note("Supports configurable redirect latency, including 0-cycle redirect.")
      .build()
  }

  val paramReqAlignLatency = spec {
    PARAMETER("PARAM_REQ_ALIGN_LATENCY")
      .desc("Cycles of pipeline latency between Req-Table output and Align/Slice stage input.")
      .entry("default", "0")
      .note("0: Direct combinatorial connection. 1: Inserts a register stage. Affects Fmax.")
      .build()
  }

  val paramRvcExpLatency = spec { // NEW: RVC Expander Latency
    PARAMETER("PARAM_RVC_EXP_LATENCY")
      .desc("Cycles of pipeline latency within or after the RVC Expander for packing.")
      .entry("default", "0")
      .note("0: Combinatorial packing. 1: Inserts a register stage. Affects Fmax for complex RVC expansion/packing.")
      .build()
  }

  val paramUseGenId = spec {
    PARAMETER("PARAM_USE_GEN_ID")
      .desc("Enables Generation-ID for robust flush coherence across pipeline stages.")
      .entry("default", "false")
      .note("Introduces hardware overhead but strengthens correctness guarantees during complex flush scenarios.")
      .build()
  }

  val paramGenIdWidth = spec {
    PARAMETER("PARAM_GEN_ID_WIDTH")
      .desc("Bit width for the Generation-ID.")
      .entry("default", "1")
      .note("Determines the frequency of Generation-ID wrap-around.")
      .build()
  }

  val paramIssueWidth = spec {
    PARAMETER("PARAM_ISSUE_WIDTH")
      .desc("Number of instruction slots issued to the Backend per cycle.")
      .entry("default", "1") // MODIFIED: Default changed to 1
      .note("Crucial for Frontend's IPC contribution and Backend throughput.")
      .build()
  }

  val paramIqDepth = spec {
    PARAMETER("PARAM_IQ_DEPTH")
      .desc("Instruction Queue depth (number of instruction slots).")
      .entry("default", "8")
      .note("Buffering capacity for fetched instructions awaiting Backend consumption.")
      .build()
  }

  val paramUseRvcExpander = spec {
    PARAMETER("PARAM_USE_RVC_EXPANDER")
      .desc("Enables the RVC (Compressed) to RVI (Standard) instruction expansion logic.")
      .entry("default", "true")
      .note("If false, RVC instructions are not supported or handled by the Backend.")
      .build()
  }

  val paramDirPredictMode = spec {
    PARAMETER("PARAM_DIR_PREDICT_MODE")
      .desc("Branch prediction mode for the BPU.")
      .entry("options", "0=AlwaysNT, 1=FwdNT/BwdT, 2=1-entry BHT, 3=N-entry BHT, 4=Gshare")
      .entry("default", "3") // Assuming N-entry BHT as a common default
      .build()
  }

  val paramBhtEntries = spec {
    PARAMETER("PARAM_BHT_ENTRIES")
      .desc("Number of entries in the Branch History Table (BHT).")
      .entry("default", "64")
      .note("Applicable for prediction modes 3 and 4.")
      .build()
  }

  val paramBtbEntries = spec {
    PARAMETER("PARAM_BTB_ENTRIES")
      .desc("Number of entries in the Branch Target Buffer (BTB).")
      .entry("default", "32")
      .build()
  }

  val paramRasDepth = spec {
    PARAMETER("PARAM_RAS_DEPTH")
      .desc("Number of entries in the Return Address Stack (RAS).")
      .entry("default", "4")
      .build()
  }
}

// src/main/scala/klase32/frontend/specs/BundlesSpecs.scala
package klase32.frontend.specs

import framework.macros.SpecEmit.spec
import framework.specs.Spec._

/**
 * Specifications for src/main/scala/klase32/frontend/design/Parameters.scala in design
 */

object ParametersSpecs {
  val paramFetchWidth = spec {
    PARAMETER("PARAM_FETCH_WIDTH")
      .desc("Bits per fetch beat for memory accesses.")
      .entry("default", "32")
      .entry("constraint", "Power-of-two multiple of 32 (e.g., 32, 64, 128).")
      .note("Directly impacts parallel fetch capability and bus width requirements.")
      .build()
  }

  val paramSlicesPerFetch = spec { // NEW: Derived constant for Vec lengths
    PARAMETER("P_SLICES_PER_FETCH")
      .desc("Number of 16-bit slices derived from each fetch beat (PARAM_FETCH_WIDTH / 16).")
      .entry("default", "2") // For FETCH_WIDTH = 32
      .note("Used for static sizing of Vec lengths in Chisel. Must be a constant.")
      .build()
  }

  val paramBurstBeats = spec {
    PARAMETER("PARAM_BURST_BEATS")
      .desc("Number of beats per single fetch request.")
      .entry("default", "1")
      .note("Influences Req-Table's `beatRemain` logic for multi-beat bursts.")
      .build()
  }

  val paramMaxOutstanding = spec {
    PARAMETER("PARAM_MAX_OUTSTANDING")
      .desc("Maximum simultaneous outstanding fetch requests.")
      .entry("default", "4")
      .note("Defines the capacity of the Request Table.")
      .build()
  }

  val paramRedirectLatencyCycles = spec {
    PARAMETER("PARAM_REDIRECT_LATENCY_CYCLES")
      .desc("Cycles from redirect detection to actual fetch PC update.")
      .entry("default", "0")
      .note("Supports configurable redirect latency, including 0-cycle redirect.")
      .build()
  }

  val paramReqAlignLatency = spec {
    PARAMETER("PARAM_REQ_ALIGN_LATENCY")
      .desc("Cycles of pipeline latency between Req-Table output and Align/Slice stage input.")
      .entry("default", "0")
      .note("0: Direct combinatorial connection. 1: Inserts a register stage. Affects Fmax.")
      .build()
  }

  val paramRvcExpLatency = spec { // NEW: RVC Expander Latency
    PARAMETER("PARAM_RVC_EXP_LATENCY")
      .desc("Cycles of pipeline latency within or after the RVC Expander for packing.")
      .entry("default", "0")
      .note("0: Combinatorial packing. 1: Inserts a register stage. Affects Fmax for complex RVC expansion/packing.")
      .build()
  }

  val paramUseGenId = spec {
    PARAMETER("PARAM_USE_GEN_ID")
      .desc("Enables Generation-ID for robust flush coherence across pipeline stages.")
      .entry("default", "false")
      .note("Introduces hardware overhead but strengthens correctness guarantees during complex flush scenarios.")
      .build()
  }

  val paramGenIdWidth = spec {
    PARAMETER("PARAM_GEN_ID_WIDTH")
      .desc("Bit width for the Generation-ID.")
      .entry("default", "1")
      .note("Determines the frequency of Generation-ID wrap-around.")
      .build()
  }

  val paramIssueWidth = spec {
    PARAMETER("PARAM_ISSUE_WIDTH")
      .desc("Number of instruction slots issued to the Backend per cycle.")
      .entry("default", "1") // MODIFIED: Default changed to 1
      .note("Crucial for Frontend's IPC contribution and Backend throughput.")
      .build()
  }

  val paramIqDepth = spec {
    PARAMETER("PARAM_IQ_DEPTH")
      .desc("Instruction Queue depth (number of instruction slots).")
      .entry("default", "8")
      .note("Buffering capacity for fetched instructions awaiting Backend consumption.")
      .build()
  }

  val paramUseRvcExpander = spec {
    PARAMETER("PARAM_USE_RVC_EXPANDER")
      .desc("Enables the RVC (Compressed) to RVI (Standard) instruction expansion logic.")
      .entry("default", "true")
      .note("If false, RVC instructions are not supported or handled by the Backend.")
      .build()
  }

  val paramDirPredictMode = spec {
    PARAMETER("PARAM_DIR_PREDICT_MODE")
      .desc("Branch prediction mode for the BPU.")
      .entry("options", "0=AlwaysNT, 1=FwdNT/BwdT, 2=1-entry BHT, 3=N-entry BHT, 4=Gshare")
      .entry("default", "3") // Assuming N-entry BHT as a common default
      .build()
  }

  val paramBhtEntries = spec {
    PARAMETER("PARAM_BHT_ENTRIES")
      .desc("Number of entries in the Branch History Table (BHT).")
      .entry("default", "64")
      .note("Applicable for prediction modes 3 and 4.")
      .build()
  }

  val paramBtbEntries = spec {
    PARAMETER("PARAM_BTB_ENTRIES")
      .desc("Number of entries in the Branch Target Buffer (BTB).")
      .entry("default", "32")
      .build()
  }

  val paramRasDepth = spec {
    PARAMETER("PARAM_RAS_DEPTH")
      .desc("Number of entries in the Return Address Stack (RAS).")
      .entry("default", "4")
      .build()
  }
}
