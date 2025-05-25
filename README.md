# 스트림 테스트 프로젝트

이 프로젝트는 Spring Boot와 MyBatis를 이용해 계층형 데이터를 처리하는 두 가지 방법을 보여줍니다.

1. MyBatis ResultMap 사용
2. Java Stream API 사용

## 문서

이 프로젝트에는 설명서가 포함되어 있어, 애플리케이션을 이해하고, 설정하고, 사용하는 데 도움이 됩니다.

- [설치 가이드](SETUP.md): 개발 환경 설치에 대한 자세한 안내
- [기술 문서](TECHNICAL.md): 두 가지 접근법에 대한 심도 있는 설명
- [성능 비교](README-PERFORMANCE.md): 성능 테스트에 대한 자세한 분석

## 데이터베이스 구조

이 프로젝트는 세 개의 테이블을 갖는 데이터베이스를 사용합니다.

- **Processor**: 결제 프로세서 정보
- **Payment**: 프로세서와 스키마를 연결하는 결제 트랜잭션
- **Scheme**: 결제 스키마 (예: 신용카드, 모바일 결제 등)

## 프로젝트 구성

- **DTO 클래스**: 데이터 모델을 나타냅니다
  - `Processor`: 프로세서 정보와 결제 방식 리스트 포함
  - `PaymentType`: 결제 방식 정보 및 스키마 리스트 포함
  - `Scheme`: 스키마 정보
  - `FlatProcessorData`: 계층 데이터 구조의 평면 표현
- **Mapper**: MyBatis 매퍼 - DB 작업 담당
  - `ProcessorMapper`: 프로세서 데이터 조회 메서드 보유 인터페이스
  - `ProcessorMapper.xml`: SQL 쿼리 및 결과 매핑 XML 설정
- **Service**: 비즈니스 로직
  - `ProcessorResultMapService`: MyBatis ResultMap을 통한 계층 데이터 조회
  - `ProcessorStreamService`: Java Stream API로 평면 데이터를 계층 구조로 변환
- **Controller**: REST API 엔드포인트 제공
  - `ProcessorController`: 두 방법과 성능 테스트 엔드포인트 노출

## API 엔드포인트

- `GET /api/processors/resultMap`: ResultMap 방식으로 프로세서 조회
- `GET /api/processors/stream`: Stream 방식으로 프로세서 조회
- `GET /api/processors/perf`: 두 방식의 성능 비교 결과 조회

## API 문서

이 프로젝트는 Swagger/OpenAPI 기반 REST API 설명서를 포함하고 있습니다. 아래에서 문서를 볼 수 있습니다.

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/api-docs

Swagger UI는 API 엔드포인트를 쉽게 탐색하고 테스트할 수 있는 인터페이스를 제공합니다.

## CORS 설정

API는 CORS(Cross-Origin Resource Sharing)를 지원하여, 서로 다른 도메인이나 포트에서 접근할 수 있습니다. `WebConfig` 클래스에서 아래와 같이 설정합니다.

- 모든 출처(`*`) 허용
- 일반적인 HTTP 메서드(GET, POST, PUT, DELETE, OPTIONS) 허용
- 모든 헤더 허용
- preflight 요청 캐싱 3600초(1시간) 유지

이 설정으로 다양한 도메인에서 API 사용이 가능합니다.

## 빠른 시작 가이드

프로젝트를 빠르게 실행하기 위한 단계는 다음과 같습니다.

1. **사전 준비**:
  - Java 17 이상
  - MySQL 8.0 이상
  - Gradle 7.6 이상(또는 포함된 래퍼 사용)
2. **데이터베이스 설정**:

```
   CREATE DATABASE HM;
   CREATE USER 'HM'@'localhost' IDENTIFIED BY 'testHM';
   GRANT ALL PRIVILEGES ON HM.* TO 'HM'@'localhost';
   FLUSH PRIVILEGES;
```

1. **애플리케이션 실행**:

```
   ./gradlew bootRun
```

1. **API 접속**:
  - ResultMap 방식: http://localhost:8080/api/processors/resultMap
  - Stream 방식: http://localhost:8080/api/processors/stream
  - 성능 비교: http://localhost:8080/api/processors/perf
  - Swagger UI: http://localhost:8080/swagger-ui.html
2. **성능 테스트 실행**:

```
   ./gradlew test --tests "hm.streamtest.LoadTest"
```

자세한 설치 방법은 [설치 가이드](SETUP.md)를 참고하세요.

## 성능 테스트

`/api/processors/perf` 엔드포인트를 통해 두 방식의 성능을 비교할 수 있습니다. 각 방식의 실행 시간을 밀리초(ms) 단위로 반환합니다.

대량 데이터 테스트가 필요하다면, `src/main/resources/sql/data.sql`의 SQL문을 활성화하여 더 많은 데이터를 생성할 수 있습니다.

## 샘플 응답

```
[
  {
    "processorId": "PROC_A",
    "processorName": "프로세서 A",
    "partnerCode": "PARTNER_001",
    "paymentTypes": [
      {
        "paymentType": "MOBILE",
        "schemes": [
          { "schemeCode": "NAVER_PAY", "schemeName": "네이버페이" },
          { "schemeCode": "TOSS_PAY", "schemeName": "토스페이" }
        ]
      },
      {
        "paymentType": "CREDIT_CARD",
        "schemes": [
          { "schemeCode": "HYUNDAI_CARD", "schemeName": "현대카드" },
          { "schemeCode": "SAMSUNG_CARD", "schemeName": "삼성카드" }
        ]
      }
    ]
  },
  {
    "processorId": "PROC_B",
    "processorName": "프로세서 B",
    "partnerCode": "PARTNER_002",
    "paymentTypes": [
      {
        "paymentType": "MOBILE",
        "schemes": [
          { "schemeCode": "KAKAO_PAY", "schemeName": "카카오페이" }
        ]
      }
    ]
  }
]
```

------