package hm.streamtest.dto

/**
 * 결제 유형 정보를 위한 DTO
 * DTO for PaymentType information
 */
data class PaymentType(
    // 결제 유형 식별자
    var paymentType: String? = null,
    // 이 결제 유형에 속하는 스키마 목록
    // 계층적 구조의 두 번째 수준을 형성합니다.
    var schemes: List<Scheme>? = null
)
