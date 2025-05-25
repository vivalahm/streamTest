## 기술 문서

이 문서는 Stream Test 프로젝트에서 계층적 데이터를 처리하기 위해 사용된 두 가지 접근 방식에 대한 상세한 기술적 설명을 제공합니다:

1. **MyBatis ResultMap 접근 방식**: MyBatis의 내장 ResultMap 기능을 사용하여 계층적 데이터 매핑
2. **Java Stream API 접근 방식**: Java Stream API를 사용하여 플랫 데이터를 계층적 구조로 변환

## 데이터베이스 스키마

각 접근 방식에 대해 자세히 알아보기 전에 데이터베이스 스키마를 이해해 보겠습니다:

```
┌───────────────┐       ┌───────────────┐       ┌───────────────┐
│   Processor   │       │    Payment    │       │    Scheme     │
├───────────────┤       ├───────────────┤       ├───────────────┤
│ processor_id  │◄──────│ processor_id  │       │ scheme_id     │
│ processor_name│       │ partner_code  │       │ scheme_code   │◄─────┐
│ partner_code  │       │ payment_type  │       │ scheme_name   │      │
└───────────────┘       │ scheme_code   │───────┘               │      │
                        └───────────────┘       └───────────────┘      │
                                                                       │
                                                                       │
```

- **Processor**: 결제 프로세서에 대한 정보를 포함합니다.
- **Payment**: 프로세서를 결제 유형 및 스킴과 연결합니다.
- **Scheme**: 결제 스킴(예: 신용카드, 모바일 결제 방법)에 대한 정보를 포함합니다.

목표는 다음과 같은 계층적 구조로 데이터를 검색하는 것입니다:

```
Processor
├── PaymentType 1
│   ├── Scheme 1
│   └── Scheme 2
└── PaymentType 2
    ├── Scheme 3
    └── Scheme 4
```

## 접근 방식 1: MyBatis ResultMap

### 개요

ResultMap 접근 방식은 MyBatis의 내장 기능을 사용하여 SQL 쿼리 결과를 계층적 객체 구조에 직접 매핑합니다. 이는 XML 매퍼 파일에서 선언적으로 수행됩니다.

### 구현 상세

#### 1. DTO 클래스

먼저, 계층적 구조를 나타내는 DTO 클래스를 정의합니다:

Java

```java
// Processor.java
@Data
public class Processor {
    private String processorId;
    private String processorName;
    private String partnerCode;
    private List<PaymentType> paymentTypes;
}

// PaymentType.java
@Data
public class PaymentType {
    private String paymentType;
    private List<Scheme> schemes;
}

// Scheme.java
@Data
public class Scheme {
    private String schemeCode;
    private String schemeName;
}
```

#### 2. XML 매퍼 설정

XML 매퍼 파일에서 SQL 쿼리 결과를 DTO 클래스에 매핑하는 ResultMap을 정의합니다:

XML

```xml
<resultMap id="SchemeResultMap" type="hm.streamtest.dto.Scheme">
    <result property="schemeCode" column="scheme_code"/>
    <result property="schemeName" column="scheme_name"/>
</resultMap>

<resultMap id="PaymentTypeResultMap" type="hm.streamtest.dto.PaymentType">
    <result property="paymentType" column="payment_type"/>
    <collection property="schemes" ofType="hm.streamtest.dto.Scheme" resultMap="SchemeResultMap"/>
</resultMap>

<resultMap id="ProcessorResultMap" type="hm.streamtest.dto.Processor">
    <id property="processorId" column="processor_id"/>
    <result property="processorName" column="processor_name"/>
    <result property="partnerCode" column="partner_code"/>
    <collection property="paymentTypes" ofType="hm.streamtest.dto.PaymentType" resultMap="PaymentTypeResultMap"/>
</resultMap>
```

#### 3. SQL 쿼리

SQL 쿼리는 세 개의 테이블을 조인하고 필요한 모든 열을 선택합니다:

XML

```xml
<select id="findAllProcessorsWithDetails" resultMap="ProcessorResultMap">
    SELECT
        proc.processor_id AS processor_id,
        proc.processor_name AS processor_name,
        proc.partner_code AS partner_code,
        pay.payment_type AS payment_type,
        s.scheme_code AS scheme_code,
        s.scheme_name AS scheme_name
    FROM
        Processor proc
    LEFT JOIN
        Payment pay ON proc.processor_id = pay.processor_id
                     AND proc.partner_code = pay.partner_code
    LEFT JOIN
        Scheme s ON pay.scheme_code = s.scheme_code
    ORDER BY
        proc.processor_id, pay.payment_type, s.scheme_code
</select>
```

#### 4. 서비스 구현

서비스 구현은 간단합니다 - 매퍼 메서드를 호출하기만 하면 됩니다:

Java

```java
@Service
public class ProcessorResultMapService {
    private final ProcessorMapper processorMapper;

    public ProcessorResultMapService(ProcessorMapper processorMapper) {
        this.processorMapper = processorMapper;
    }

    public List<Processor> getProcessors() {
        return processorMapper.findAllProcessorsWithDetails();
    }
}
```

### 작동 방식

1. MyBatis는 SQL 쿼리를 실행하여 프로세서, 결제 유형 및 스킴 정보가 포함된 플랫 결과 집합을 반환합니다.
2. MyBatis는 ResultMap 구성을 사용하여 행을 그룹화하고 계층적 객체 구조를 구성합니다.
3. 각 고유한 프로세서에 대해 MyBatis는 새 Processor 객체를 생성합니다.
4. 프로세서 내의 각 고유한 결제 유형에 대해 MyBatis는 새 PaymentType 객체를 생성하고 프로세서의 `paymentTypes` 목록에 추가합니다.
5. 결제 유형 내의 각 고유한 스킴에 대해 MyBatis는 새 Scheme 객체를 생성하고 결제 유형의 `schemes` 목록에 추가합니다.

### 장점

- **선언적 접근 방식**: 매핑이 XML에 선언적으로 정의되어 이해하고 유지 관리하기 쉽습니다.
- **더 적은 Java 코드**: MyBatis가 변환을 처리하므로 최소한의 Java 코드만 필요합니다.
- **성능**: MyBatis는 변환 프로세스를 최적화하여 복잡한 계층 구조에서 더 나은 성능을 제공할 수 있습니다.

### 단점

- **제한된 유연성**: 매핑이 컴파일 타임에 고정되며 런타임에 쉽게 변경할 수 없습니다.
- **XML 설정**: 장황할 수 있는 XML 설정을 작성하고 유지 관리해야 합니다.
- **학습 곡선**: MyBatis의 ResultMap 기능을 이해해야 합니다.

## 접근 방식 2: Java Stream API

### 개요

Stream 접근 방식은 데이터베이스에서 플랫 데이터를 검색한 다음 Java Stream API를 사용하여 계층적 구조로 변환합니다. 이는 Java 코드에서 프로그래밍 방식으로 수행됩니다.

### 구현 상세

#### 1. DTO 클래스

계층적 DTO 클래스(Processor, PaymentType, Scheme) 외에도 플랫 DTO 클래스를 정의합니다:

Java

```java
// FlatProcessorData.java
@Data
public class FlatProcessorData {
    private String processorId;
    private String processorName;
    private String partnerCode;
    private String paymentType;
    private String schemeCode;
    private String schemeName;
}
```

#### 2. XML 매퍼 설정

플랫 DTO 클래스에만 매핑하므로 XML 매퍼 설정이 더 간단합니다:

XML

```xml
<select id="findAllProcessorsAsFlatData" resultType="hm.streamtest.dto.FlatProcessorData">
    SELECT
        proc.processor_id AS processorId,
        proc.processor_name AS processorName,
        proc.partner_code AS partnerCode,
        pay.payment_type AS paymentType,
        s.scheme_code AS schemeCode,
        s.scheme_name AS schemeName
    FROM
        Processor proc
    LEFT JOIN
        Payment pay ON proc.processor_id = pay.processor_id
                     AND proc.partner_code = pay.partner_code
    LEFT JOIN
        Scheme s ON pay.scheme_code = s.scheme_code
    ORDER BY
        proc.processor_id, pay.payment_type, s.scheme_code
</select>
```

#### 3. 서비스 구현

플랫 데이터를 계층적 구조로 변환해야 하므로 서비스 구현이 더 복잡합니다:

Java

```java
@Service
public class ProcessorStreamService {
    private final ProcessorMapper processorMapper;

    public ProcessorStreamService(ProcessorMapper processorMapper) {
        this.processorMapper = processorMapper;
    }

    public List<Processor> getProcessors() {
        // 데이터베이스에서 플랫 데이터 가져오기
        List<FlatProcessorData> flatList = processorMapper.findAllProcessorsAsFlatData();

        // processorId로 그룹화
        Map<String, List<FlatProcessorData>> processorMap =
                flatList.stream()
                        .collect(Collectors.groupingBy(FlatProcessorData::getProcessorId, LinkedHashMap::new, Collectors.toList()));

        List<Processor> processors = new ArrayList<>();

        // 각 프로세서 그룹 처리
        for (Map.Entry<String, List<FlatProcessorData>> processorEntry : processorMap.entrySet()) {
            List<FlatProcessorData> processorGroup = processorEntry.getValue();
            FlatProcessorData first = processorGroup.get(0);

            // 프로세서 생성
            Processor processor = new Processor();
            processor.setProcessorId(first.getProcessorId());
            processor.setProcessorName(first.getProcessorName());
            processor.setPartnerCode(first.getPartnerCode());

            // paymentType으로 그룹화
            Map<String, List<FlatProcessorData>> paymentMap =
                    processorGroup.stream()
                            .filter(row -> row.getPaymentType() != null) // paymentType이 null이 아닌 경우 필터링
                            .collect(Collectors.groupingBy(FlatProcessorData::getPaymentType, LinkedHashMap::new, Collectors.toList()));

            List<PaymentType> paymentTypes = new ArrayList<>();

            // 각 결제 유형 그룹 처리
            for (Map.Entry<String, List<FlatProcessorData>> paymentEntry : paymentMap.entrySet()) {
                PaymentType paymentType = new PaymentType();
                paymentType.setPaymentType(paymentEntry.getKey());

                // 이 결제 유형에 대한 스킴 생성
                List<Scheme> schemes = paymentEntry.getValue().stream()
                        .filter(row -> row.getSchemeCode() != null) // schemeCode가 null이 아닌 경우 필터링
                        .map(row -> {
                            Scheme scheme = new Scheme();
                            scheme.setSchemeCode(row.getSchemeCode());
                            scheme.setSchemeName(row.getSchemeName());
                            return scheme;
                        })
                        .collect(Collectors.collectingAndThen( // 중복 제거를 위해 Map을 거친 후 List로 변환
                                Collectors.toMap(Scheme::getSchemeCode, s -> s, (a, b) -> a, LinkedHashMap::new),
                                m -> new ArrayList<>(m.values())
                        ));

                paymentType.setSchemes(schemes);
                paymentTypes.add(paymentType);
            }

            processor.setPaymentTypes(paymentTypes);
            processors.add(processor);
        }

        return processors;
    }
}
```

### 작동 방식

1. 서비스는 매퍼를 사용하여 데이터베이스에서 플랫 데이터를 검색합니다.
2. Java Stream API를 사용하여 데이터를 프로세서 ID별로 그룹화합니다.
3. 각 프로세서 그룹에 대해 Processor 객체를 생성합니다.
4. 그런 다음 프로세서의 데이터를 결제 유형별로 그룹화합니다.
5. 각 결제 유형 그룹에 대해 PaymentType 객체를 생성합니다.
6. 그런 다음 결제 유형의 데이터를 Scheme 객체에 매핑합니다.
7. 마지막으로 적절한 관계를 설정하여 계층적 구조를 조립합니다.

### 장점

- **유연성**: Java 코드에서 변환 로직을 쉽게 수정하거나 확장할 수 있습니다.
- **제어**: 변환 프로세스를 완전히 제어할 수 있어 사용자 지정 로직 및 최적화가 가능합니다.
- **XML 설정 없음**: 복잡한 XML 설정이 필요하지 않습니다.

### 단점

- **더 많은 Java 코드**: 더 많은 Java 코드를 작성하고 유지 관리해야 합니다.
- **복잡성**: 변환 로직이 복잡하고 이해하기 어려울 수 있습니다.
- **잠재적인 성능 영향**: 변환이 Java 코드에서 수행되므로 MyBatis의 최적화된 구현보다 효율성이 떨어질 수 있습니다.

## 접근 방식 비교

### 코드 복잡성

- **ResultMap 접근 방식**: Java 코드는 적지만 XML 설정이 필요합니다.
- **Stream 접근 방식**: Java 코드는 더 많지만 XML 설정은 없습니다.

### 성능

부하 테스트([성능 비교 보고서](https://www.google.com/search?q=README-PERFORMANCE.md&authuser=1) 참조)에 따르면 Stream 접근 방식이 일반적으로 더 나은 성능을 보입니다:

- **처리량**: Stream 접근 방식의 처리량이 약 9% 더 높습니다.
- **응답 시간**: Stream 접근 방식의 평균 응답 시간이 약 8.5% 더 낮습니다.

### 유지보수성

- **ResultMap 접근 방식**: 선언적 특성으로 인해 한눈에 이해하기 쉽지만 변경하려면 XML을 수정해야 합니다.
- **Stream 접근 방식**: 프로그래밍 방식 특성으로 인해 유연성은 더 높지만 코드가 더 복잡할 수 있습니다.

### 확장성

- **ResultMap 접근 방식**: MyBatis는 변환을 효율적으로 처리하지만 복잡한 변환에는 유연성이 떨어집니다.
- **Stream 접근 방식**: Java Stream API는 더 많은 제어 기능을 제공하여 특정 사용 사례에 대한 최적화가 가능합니다.

## 각 접근 방식 사용 시기

### ResultMap 접근 방식 사용 시기:

- 계층적 구조가 비교적 단순하고 안정적일 때
- Java 코드가 적은 선언적 접근 방식을 선호할 때
- 프로젝트에서 이미 MyBatis를 광범위하게 사용하고 있을 때
- 팀이 MyBatis ResultMap에 익숙할 때

### Stream 접근 방식 사용 시기:

- 계층적 구조가 복잡하거나 사용자 지정 변환 로직이 필요할 때
- 변환 프로세스에 대한 더 많은 제어가 필요할 때
- 성능이 중요한 요소일 때
- 팀이 MyBatis ResultMap보다 Java Stream API에 더 익숙할 때

## 결론

두 접근 방식 모두 장단점이 있으며, 어떤 것을 선택할지는 특정 요구 사항과 선호도에 따라 달라집니다. ResultMap 접근 방식은 더 선언적이며 Java 코드가 덜 필요한 반면, Stream 접근 방식은 더 많은 유연성과 제어 기능을 제공합니다.

성능 테스트에 따르면 일반적으로 Stream 접근 방식이 더 나은 성능을 보이지만, 데이터 세트가 작거나 계층 구조가 덜 복잡한 경우에는 그 차이가 크지 않을 수 있습니다.

자세한 성능 비교는 [성능 비교 리포트](README-PERFORMANCE.md)를 참고하세요.