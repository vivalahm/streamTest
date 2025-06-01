package hm.streamtest.dto

/**
 * 평면화된 프로세서 데이터를 위한 DTO
 * 스트림 기반 접근 방식에서 사용됩니다.
 * DTO for flattened processor data
 * This is used for the stream-based approach
 */
data class FlatProcessorData(
    // Kotlin의 데이터 클래스는 equals(), hashCode(), toString() 메서드를 자동으로 생성합니다.
    // 모든 필드는 nullable(String?)로 선언되어 null 값을 허용합니다.
    var processorId: String? = null,
    var processorName: String? = null,
    var partnerCode: String? = null,
    var paymentType: String? = null,
    var schemeCode: String? = null,
    var schemeName: String? = null
)
