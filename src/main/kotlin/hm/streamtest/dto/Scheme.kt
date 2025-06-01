package hm.streamtest.dto

/**
 * 스키마 정보를 위한 DTO
 * DTO for Scheme information
 */
data class Scheme(
    // 스키마 코드 식별자
    var schemeCode: String? = null,
    // 스키마 이름
    var schemeName: String? = null
    // 이 클래스는 계층적 구조의 마지막 수준(리프 노드)을 형성합니다.
)
