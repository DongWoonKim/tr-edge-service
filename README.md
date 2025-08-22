## 🌐 tr-edge-service (API Gateway)

`tr-edge-service`는 **Spring Cloud Gateway** 기반으로 구축된 API Gateway이며,  
백엔드 서비스들에 접근하기 위한 **단일 진입점 (Single Entry Point)** 역할을 합니다.  

이를 통해 사용자는 여러 백엔드 서비스에 직접 접근할 필요 없이, Gateway를 통해 일관된 방식으로 서비스에 접근할 수 있습니다.

---

### 🔑 주요 특징

- **API Gateway 역할**
  - 모든 외부 요청은 `tr-edge-service`를 통해 내부 서비스로 라우팅됩니다.
  - 인증, 인가, 로깅, 모니터링 등 공통 기능을 Gateway에서 처리합니다.

- **PreGatewayFilter 적용**
  - `catalog-service`와 `search-service`에 대해 **차등적으로 필터를 적용**합니다.
  - 서비스별 요구사항에 따라 Gateway 레벨에서 동적으로 처리됩니다.

- **Access Token 검증**
  - 요청에 포함된 **Access Token을 검증**하여 유효한 사용자인지 판단합니다.
  - 유효하지 않은 토큰의 경우 요청은 서비스로 전달되지 않고, Gateway에서 차단됩니다.

---

### ⚙️ 기대 효과

- 각 서비스가 개별적으로 인증/인가를 처리하지 않아도 되어, **서비스 간 결합도를 낮춤**.  
- 중앙집중식 토큰 검증을 통해 **보안 강화**.  
- 필터를 동적으로 적용하여 서비스별 요구사항을 **유연하게 반영** 가능.

---

### ⚙️ 내부 아키텍처

```bash
com.trevari.spring.tredgeservice
├─ client
│  └─ AuthServiceClient.java
├─ config
│  ├─ CorsConfig.java
│  └─ WebClientConfig.java
├─ dto
│  └─ ValidTokenRequestDTO.java
├─ filter
│  └─ PreGatewayFilter.java
└─ TrEdgeServiceApplication.java

resources
└─ application.yml
```
### 설계 원칙  
Edge Service는 **라우팅 기능 외에는 최대한 어떠한 책임도 갖지 않는다**.  
- **application.yml** : Edge Service의 라우팅 및 필터 설정이 정의되어 있으며, Auth, Catalog, Search 서비스로의 라우팅이 포함됩니다.  
- **PreGatewayFilter** : 인증 관련 요청을 제외한 도서 목록 조회, 검색 요청 시 동작하여 Access Token을 검증합니다.  
  이는 단일지점에서 체크하는게 효율적이기 때문에 edge service가 담당합니다.  
- **WebClientConfig** : 토큰 검증은 Auth Service에서 담당하며, 이를 위해 WebClient 요청 관련 설정을 관리합니다.  
  검증하는 실질 로직은 auth service가 담당하고 요청하여 검증 결과를 받아옵니다. 
- **AuthServiceClient** : 실제 Auth Service에 토큰 검증을 요청하고 응답을 받아 상태 코드를 반환합니다. 
