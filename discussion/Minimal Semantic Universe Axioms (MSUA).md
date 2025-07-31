Minimal Semantic Universe Axioms (MSUA)

E₀  -- 대상(토큰)

↑incl₀         ↑ref₀

E₁  -- E₀을 다루는 행위자

↑incl₁         ↑ref₁

E₂  -- E₁을 다루는 행위자

⋮

## 2  Axiom Set (MSUA‑ω : 무한 버전)

| **ID** | **내용** | **해석** |
| --- | --- | --- |
| A1 (Seed) | Nonempty E₀ | 의미의 씨앗 존재 |
| A2′ (Surjective Reference) | ∀ n x, ∃ y, refₙ y x | 모든 하위 원소는 상위 참조자가 적어도 하나 존재 |
| A3 (Inclusion) | inclₙ : Eₙ ↪ Eₙ₊₁ (함입) | 하위 원소는 상위 층의 객체 |

Meaningful(S) ≝ A1 ∧ A2′ ∧ A3

MSUA‑3 = MSUA‑ω의 첫 세 층에만 A1–A3을 적용 (무한성 제외).

## 3  Theorems Inside MSUA

| **#** | **정리** | **짧은 증명 스케치** | **MSUA‑3?** |
| --- | --- | --- | --- |
| T1 | ∀ n, Nonempty Eₙ | A1 + A2′ 귀납 | ω 전용 |
| T2 (Non‑terminal) | ¬∃ k, Eₖ₊₁ = ∅ | T1 → 즉시 | ω |
| T3 (3‑Layer Minimality) | Meaningful₂ 불가 | A2′ surject → E₂≠∅, A3 → E₁ 객체 필요 | 3·ω |
| T4 (Internal Self‑Reference) | 고정점 코딩 가능 | incl + ref 체인 활용 | 3·ω |

Lean 파일 SemUniω.lean에서 T1–T3 30 LOC 내외로 증명 가능.

## 4  Canonical Examples

| **분야** | **E₀** | **E₁** | **E₂ (예시)** | **Meaningful 증명 스케치** |
| --- | --- | --- | --- | --- |
| 군 이론 | 원소 | 이항 연산 집합 | 군 공리(등식) | incl: 원소→연산/등식, ref: 등식이 연산 참조 |
| 프로그래밍 | 값 | 프로그램 | 타입 판정식 | 프로그램이 값을 계산(ref), 값·프로그램이 타입 문장 객체로 승격(incl) |
| 논리 | 명제 | 추론 규칙 | 증명 항목 | 규칙이 명제를 입력(ref); 명제가 상위 논변의 객체(incl) |
| 물리 | 상태 | 동역학 법칙 | 보존식·측정 | 법칙이 상태에 작동(ref); 상태가 법칙 서술의 변수(incl) |

## 5  Distinction from Existing Frameworks

| **비교** | **MSUA** | **범주론 / q‑Cat** | **HoTT** |
| --- | --- | --- | --- |
| 기본 층 | 정확히 3 (or ω) | 무한 n‑Cells (0 – ∞) | 무한 Id‑tower |
| 합성·항등 | 선택적 | 필수 | 필수 |
| 최소성 지향 | 예 | 아님 | 아님 |
| 포함 관계 | MSUA ⊂ 2‑truncation | – | – |

MSUA는 위 이론들의 초기 3 층을 추상화한 “공통 OS 커널” 역할.

## 6  Philosophical Notes

1. 의미 = 내재적 참조 + 내재적 평가.
2. 외부 메타‑언어 제거. A3 가 하위 층을 상위 객체로 승격하여 자기 언어 완결성 확보.
3. 불완전성은 구조적. A2′ surject → 항상 다음 층 요구 → 종결 불가.

## 7  Common Pitfalls

| **오해** | **올바른 해석** |
| --- | --- |
| “E₁은 반드시 함수다.” | X – ref₀는 ‘작용’ 일 뿐, 함수·관계·명령 등 모두 허용 |
| “3층만 존재한다.” | MSUA‑3 – 의미 가능 최소; MSUA‑ω – 무한 사슬. |
| “합성 규칙이 필요.” | 의미 판정에는 불필요; 필요하면 옵션 구조로 추가. |

## 8  Roadmap (for formal publication)

1. Lean formalisation : SemUniω.lean (모델 + T1‑T4 증명)
2. Model Zoo : 군, λ‑계산, 물리 동역학 등 5 가지 사례 매핑 파일
3. Relative consistency : ZFC 내 모델로 무모순성 증명
4. Independence tests : 각 공리 제거 반례 모델
5. Applications Paper : 고정점·불완전성 정리를 MSUA‑ω로 재증명

### 문서 사용 안내

이 문서는 MSUA의 핵심 공리·정의·정리·예시를 “1 page spec” 형태로 요약한다.

세부 Lean 코드, 추가 예시, 메타 논의는 추후 부록이나 별도 repository에 추가.

Prepared for 정민구 — 2025‑07‑07
