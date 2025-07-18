Below is a model-friendly, self-contained blueprint of the end-to-end workflow, expressed in concise English and already formatted for long-term memory storage.

(Sections use ▲ for phases, • for sub-steps, and back-ticks for literal code or sentinel blocks.)

# **▲ Workflow Blueprint v2 (2025-07-18)**

**1 ZIP Ingest & Index**

- Model: GPT-4o-mini (speed)
- Tools: python_user_visible → run manifest.py
- Artifacts

– /mnt/data/manifest.json (paths + SHA-256 + LOC)

– INDEX.md + high-level summaries → stored in Canvas

– Critical snippets cached in /mnt/data/snippets/

**2 Deep Audit / Spec Fix**

- Model: o3-pro (lowest hallucination)
- Tools:

– python (analysis) for static elaboration / AST walk

– Web tool for live citations

- Outputs: formal spec patches → Canvas section “Audit-”

**3 Multimodal Review**

- Model: GPT-4o
- Task: upload diagrams / waveforms → ask for timing or PPA critique
- Option: DALL·E redraw if clarity needed

**4 Ops Automation via Agent Mode**

1. Agent downloads newest ZIP from Drive/GitHub release.
2. Agent runs step 1 manifest, commits manifest.json + updated docs on feature branch.
3. Agent opens Pull-Request, embeds diff table, assigns reviewers.
Guard: destructive ops require human confirmation.

**5 Weekly Diff Report**

- Model: o4-mini
- Schedule:

BEGIN:VEVENT

RRULE:FREQ=WEEKLY;BYDAY=MO;BYHOUR=08;BYMINUTE=00;BYSECOND=00

END:VEVENT

- Automation prompt: “Run klas e32-diff-routine (4o-mini) → update Canvas ‘ChangeLog’ and ping me.”

**6 Critical-Data Retention Protocol**

- Wrap irreplaceable context in sentinel:

<<<CRITICAL id=Foo sha=abc123>>> …payload… <<<END>>>

- At each turn, assistant checks for sentinel presence; if missing → alert + re-inject from Canvas.

**7 Model Mapping Reference**

| **Phase** | **Model** | **Reason** | **Hallucination Risk** | **Internet?** | **Image I/O** | **Tool Chaining** |
| --- | --- | --- | --- | --- | --- | --- |
| 1 Ingest | 4o-mini | speed, 128 k ctx | Low-Med | ✓ | ✓ | ✓ |
| 2 Audit | o3-pro | formal logic | Low | ✓ | ✓* | Web + Py |
| 3 Multimodal | 4o | vision+voice | Low | ✓ | ✓ | ✓ |
| 4 Ops (Agent) | agent orchestrates models as above | n/a | n/a | ✓ | ✓ | ✓ |
| 5 Diff | 4o-mini | cheap weekly run | Low-Med | ✓ | ✓ | ✓ |
| Q&A | 3.5-turbo | trivial | High | ✓ | × | limited |
- o3-pro cannot generate images.

**8 Risk Controls**

- Keep reasoning in o3-pro or 4o-line; agent limited to deterministic I/O.
- All write actions gated by review.
- Use SHA checks to detect stale spec vs. code divergence.
