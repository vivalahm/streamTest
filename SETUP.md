# 환경 설정 가이드

이 가이드는 Spring Boot와 MyBatis를 이용해 계층적 데이터를 처리하는 두 가지 접근 방식을 시연하는 Stream Test 프로젝트의 개발 환경을 설정하는 방법에 대해 자세히 안내합니다.



## 사전 준비

시작하기 전에 다음이 설치되어 있어야 합니다:

- Java Development Kit(JDK) 17 이상
- MySQL 8.0 이상
- Gradle 7.6 이상(또는 포함된 Gradle wrapper 사용)
- Git(선택, 버전 관리용)
- 선호하는 IDE(IntelliJ IDEA, Eclipse, VS Code 등)

## 데이터베이스 설정

### 1. MySQL 설치

MySQL이 아직 설치되어 있지 않다면 [공식 MySQL 웹사이트](https://dev.mysql.com/downloads/)에서 다운로드하여 설치하세요.

### 2. 데이터베이스 및 사용자 생성

root로 MySQL에 로그인한 후, 다음 명령어를 실행하세요:

sql CREATE DATABASE HM; CREATE USER 'HM'@'localhost' IDENTIFIED BY 'testHM'; GRANT ALL PRIVILEGES ON HM.* TO 'HM'@'localhost'; FLUSH PRIVILEGES;

### 3. 데이터베이스 연결 확인

생성한 사용자로 데이터베이스에 접속이 되는지 확인합니다:

```
bash mysql -u HM -p
```



비밀번호로 `testHM`을 입력하세요.

## 프로젝트 설정

### 1. 프로젝트 클론 또는 다운로드

Git을 사용하는 경우, 다음과 같이 저장소를 클론합니다:

```
bash git clone https://github.com/yourusername/streamTest.git cd streamTest
```

또는, 프로젝트 ZIP 파일을 다운로드 후 압축을 해제하세요.

### 2. 애플리케이션 설정 파일 수정

프로젝트에는 다음과 같이 미리 설정된 `application.yml` 파일이 포함되어 있습니다:

```
yaml spring:  application:    name: streamTest  datasource:    driver-class-name: com.mysql.cj.jdbc.Driver    url: jdbc:mysql://localhost:3306/HM?useSSL=false&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true    username: HM    password: testHM    hikari:      maximum-pool-size: 10  sql:    init:      mode: never  # 커스텀 DatabaseInitializer를 사용합니다.
```

# Swagger/OpenAPI 설정

```
springdoc:  api-docs:    path: /api-docs  swagger-ui:    path: /swagger-ui.html    operationsSorter: method    tagsSorter: alpha    display-request-duration: true  packages-to-scan: hm.streamtest.controller

mybatis:  mapper-locations: classpath:mapper/**/*.xml  configuration:    map-underscore-to-camel-case: true  type-aliases-package: hm.streamtest.dto
```

연결 정보 등 설정을 변경해야 한다면, `src/main/resources/application.yml` 파일을 편집하세요.

## 애플리케이션 빌드 및 실행

### 1. 프로젝트 빌드

Gradle wrapper를 이용하여 프로젝트를 빌드합니다:

```
bash ./gradlew build
```

코드를 컴파일하고, 테스트를 실행하며, 실행 가능한 JAR 파일을 생성합니다.

### 2. 애플리케이션 실행

Gradle wrapper로 애플리케이션을 시작합니다:

```
bash ./gradlew bootRun
```

또는, JAR 파일을 직접 실행할 수도 있습니다:

```
bash java -jar build/libs/streamTest-0.0.1-SNAPSHOT.jar
```

### 3. 애플리케이션 실행 확인

웹 브라우저에서 다음 주소로 이동하세요:

http://localhost:8080/swagger-ui.html

API 문서가 포함된 Swagger UI 화면이 나타나야 합니다.

## 데이터베이스 초기화

애플리케이션이 시작되면 데이터베이스 스키마와 샘플 데이터가 자동으로 초기화됩니다.  
이는 `DatabaseInitializer` 클래스가 다음 파일의 SQL 스크립트를 실행하여 처리합니다:

- `src/main/resources/sql/schema.sql`: 데이터베이스 테이블 생성
- `src/main/resources/sql/data.sql`: 샘플 데이터 삽입

성능 테스트를 위한 더 큰 데이터셋을 생성하려면 다음 명령을 실행하세요:

```
bash ./gradlew test --tests "hm.streamtest.DataGenerationTest"
```

실행 시 다음과 같이 데이터가 생성됩니다:

- 프로세서 100개
- 스킴 1,000개
- 결제 10,000건

## API 테스트

### 1. Swagger UI 사용

Swagger UI를 통해 가장 쉽게 API를 테스트할 수 있습니다:

http://localhost:8080/swagger-ui.html

여기서 다음을 할 수 있습니다:

- 사용 가능한 엔드포인트 탐색
- ResultMap 및 Stream 방식 모두 요청 실행
- 두 방식의 성능 비교

### 2. cURL 사용

cURL을 이용해 API를 테스트할 수도 있습니다:

# ResultMap 방식 테스트

```
curl -X GET "http://localhost:8080/api/processors/resultMap" -H "accept: application/json"
```



# Stream 방식 테스트

```
curl -X GET "http://localhost:8080/api/processors/stream" -H "accept: application/json"
```



# 성능 비교

```
curl -X GET "http://localhost:8080/api/processors/perf" -H "accept: text/plain"
```



### 3. 부하 테스트 실행

부하 테스트를 실행하고 성능 비교 보고서를 생성하려면:

```
bash ./gradlew test --tests "hm.streamtest.LoadTest"
```



이 테스트는 두 API 엔드포인트 각각에 대해 10명의 사용자로 1,000건의 요청을 실행하고,  
비교 보고서를 `load-test-results` 디렉터리에 생성합니다.

## Docker 지원

프로젝트에는 Docker로 실행할 수 있는 `docker-compose.yml` 파일이 포함되어 있습니다:

```
bash docker-compose up
```



이 명령을 실행하면 다음이 시작됩니다:

- 필요한 데이터베이스와 사용자가 적용된 MySQL 컨테이너
- Spring Boot 애플리케이션 컨테이너

## 문제 해결

### 데이터베이스 연결 문제

연결 문제가 있을 경우:

1. MySQL이 실행 중인지 확인:

   ```bash
   sudo service mysql status  # Linux
   brew services list         # macOS
   ```

2. 데이터베이스가 존재하는지 확인:

   ```sql
   SHOW DATABASES;
   ```

3. 사용자 권한 확인:

   ```sql
   SHOW GRANTS FOR 'HM'@'localhost';
   ```

4. `application.yml`의 연결 정보가 MySQL 설정과 일치하는지 확인

### 애플리케이션 실행 문제

애플리케이션이 시작되지 않는 경우:

1. 로그를 확인하여 오류 메시지 확인
2. 필요한 포트(애플리케이션: 8080, MySQL: 3306)가 사용 가능한지 확인
3. Java 17이 기본 자바 버전으로 설정되어 있는지 확인

## 다음 단계

환경 구성을 마쳤다면 다음을 진행할 수 있습니다:

1. [기술 문서](TECHNICAL.md)를 참고해 두 가지 접근 방식을 상세히 이해하세요.
2. [성능 비교 보고서](README-PERFORMANCE.md)를 검토하여 부하 테스트 결과를 확인하세요.
3. 코드를 수정해 다양한 방법이나 최적화를 실험해보세요.