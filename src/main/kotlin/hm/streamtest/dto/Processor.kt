package hm.streamtest.dto

/**
 * 프로세서 정보를 위한 DTO
 * DTO for Processor information
 */
data class Processor(
    // 프로세서의 기본 정보를 저장하는 필드
    var processorId: String? = null,
    var processorName: String? = null,
    var partnerCode: String? = null,
    // 계층적 구조를 위한 중첩 객체 목록
    // PaymentType 객체의 리스트를 포함하여 계층적 구조를 형성합니다.
    var paymentTypes: List<PaymentType>? = null
)
