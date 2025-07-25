klase32/
├── top/		#L0 독립적 Top (klase32의 최상위 TOP 계층)
│   │   ├── design/               # 전용 RTL (bundles, parameters, interfaces, submodules 기타 등등, 아마 testharness?)
│   │   ├── spec/                 # 전용 Spec (mirrors design)
│   ├── topL1A/                     # L1 독립적 TOP
│   │   ├── design/               # 전용 RTL (bundles, parameters, interfaces, submodules 기타 등등)
│   │   ├── spec/                 # 전용 Spec (mirrors design)
│   │   ├── topL2A/                 # L2 독립적 Top
│   │   │   ├── design/               # 전용 RTL (bundles, parameters, interfaces, submodules 기타 등등)
│   │   │   ├── spec/                 # 전용 Spec (mirrors design)
│   │   │   ├── TopL3A/                 # L3 독립적 Top
│   └── topL1B/                     # 다른 독립적 Top
└── lib/ 		# Top 들에 공통적으로 필요한 것들 <- 이름 바꿀까? lib말고 다른디렉토리들도 넣을까? 등등... 공통적으로 필요한 것들이 뭐임? 등등
...

** 이름은 klase32, top, design, spec 제외하고는 예시임

네이밍 컨벤션
** <이름>Params, <이름>Bundles, <이름>Interafaces 는 필수로 포함하게 할까? 아니면 알아서 하게 할까?
** <이름>Top은 필수로 할까? 아님 이거도 알아서 하게 할까?
** 디렉토리로 만드는 경우는 파일이 2개 이상이 되면 만들어야 함 (ex: module들 모음 뭐 그런거)
** frontend는 지금 design/{modules, top} 을 디렉토리로 만들고 있는데 top 디렉토리 말고 걍 파일만 쓸까?
** Params, Bundles, Interfaces를 하나의 카테고리로 봐야 할까? 아니면 별개의 카테고리로 봐야 할까? 하나의 카테고리면 디렉토리를 하나 만들어서 넣으면 되고, 아니면 지금처럼 놔둘까?
** top 승격 조건: 파일 갯수 >=2, 독립적/반복적 검증 필요, 재사용 가능성 높음, 복잡하거나 김.
-> 재사용 가능성 높음? 근데 이미 top들을 하이어라키컬하게 패키지를 만들었는데, 재사용을 다른곳에서 하게 한다고? 좀 이상하지 않아? 그런 개념이면 shared로 보내던가. 근데 shared로 너무 많이 보내면 그것도 관리가 개빡셔질걸? 아니면 top들, module들을 계층으로 나누지 말고 top 만들때 가져다 쓰게 한다? 근데 이러면 코드 구조가 너무 안보이겠지? 흠. 쉽지 않군.

top/		#L0 독립적 Top (klase32의 최상위 TOP 계층)
|─ design/               # 전용 RTL (bundles, parameters, interfaces, submodules 기타 등등)
|─ spec/                 # 전용 Spec (mirrors design)
|─ (다른 탑들...)

이 구조를 기본으로 잡는건 좋은 것 같다.

그리고
 top(n+1) = top(n)s + modules(n+1)s + shared 
이렇게 써볼까?


각 Top이 필요로 하는 유틸/인프라/ 등등등 들은? 다 따로 있을수도 있고 같이 쓰는게 있을수도 있고?
-> 최상위에 둔다
어떤 Top* 디렉토리든 그 디렉토리 자체와 lib이 있으면 개별 rtl elab/test 수행 가능(하나의 완결된 ip개념, 실제로 이걸 strict하게 할 필요는 없고, 컴파일은 됐으면 좋겠음)
Top들이 필요로 하는 하드웨어들은? (유틸이든 모듈이든) 이런건 소프트웨어 유틸이나 인프라랑 분리해야하나?
-> 몰루?
design과 spec은 서로를 1대1로 미러링하지만 design만 필요하거나 spec만 필요한 경우는 하나만 쓴다

top의 인터페이스란? top의 IO들
top의 번들이란? 방향이 없으며, top 내부에서 쓰는 번들들 + 하위 top들의 인터페이스들의 번들들 (하위 top들의 인터페이스는 상위 top의 번들로 무조건 사용되기 때문)
top의 파라미터란? 하위 top들에게 propagate 되어야 하는 파라미터들 + 자기만 쓰는 파라미터들
-> propagate는 어떻게 함? 하위 top만 똑 떼어내면 그 propagate는 어떻게 관리함?
  -> propagate parameter들은 top에서 explicit하게 주입한다.
  -> 하위 top만 떼어내더라도 인스턴스화 할 때 parameter들을 주입해야 하는건 마찬가지임
-> 그럼 cde같이 전역 Parameters를 implicit하게 쓰는건 안함?
  -> 각 Top마다 Key를 할당함. 그리고 각 Top들은 자신의 parameter들을 implicit하게 사용. 최상위 계층보다 더 위의 wrapper 계층에서만 implicit p: Parameters 로 cde parameter들을 받고, klase32 최상위 TOP에게도 explicit하게 넘겨줘야 함.
  -> implicit 갯수가 그럼 top 갯수마다 늘어나는거 아님? 이거 위험하지 않음?
    -> 그러게. 어떡하지?? config은 쓰고싶은데
==>> 여기서 알 수 있는 것. Bundles, Parameters 들은 하위로 넘겨줄 수 있어야 함. Interfaces, Bundles는 상위에서 받아올 수 있어야 함. 
-> 이거 맞나? 

근데 지금 코드베이스는 design, specs 라고 쓰는데, design, spec으로 할까 아니면 designs, specs로 할까 아니면 design, spec으로 할까?

테스트 디렉토리는 src 디렉토리랑 미러링 관계로 가져가는걸 강하게 권장하자

이 구조는 보기도 좋고 생각없이 쓸수도 있고 다 좋은데, 실제로 사용해보면 또 불편한게 많이 생기겠지? 그래도 일단 이거로 픽스해서 리팩토링 할까? AI한테 시킬거니까 패키지 경로나 이름이나 그런건 괜찮겠지만 구조 자체 때문에 생기는 뭐 디펜던시나 implicit한 문제들 생기진 않겠지? 너무 복잡해지진 않으려나?
