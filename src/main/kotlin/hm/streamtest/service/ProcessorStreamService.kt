package hm.streamtest.service

import hm.streamtest.dto.*
import hm.streamtest.mapper.ProcessorMapper
import org.springframework.stereotype.Service
import java.util.*

/**
 * 평면 데이터를 계층적 구조로 변환하기 위해 Kotlin 컬렉션 API를 사용하는 서비스
 * Service that uses Kotlin collections API to transform flat data into hierarchical structure
 */
@Service
class ProcessorStreamService(private val processorMapper: ProcessorMapper) {

    /**
     * 모든 프로세서와 해당 결제 유형 및 스키마를 가져옵니다.
     * @return 중첩된 결제 유형 및 스키마가 있는 프로세서 목록
     */
    fun getProcessors(): List<Processor> {
        // 데이터베이스에서 평면 데이터 가져오기
        // Kotlin에서는 val 키워드로 불변 변수를 선언합니다.
        val flatList = processorMapper.findAllProcessorsAsFlatData()

        // processorId로 그룹화
        // groupBy 함수는 컬렉션을 지정된 키 함수에 따라 그룹화하여 Map을 반환합니다.
        // 여기서 람다 표현식 { it.processorId }는 각 항목의 processorId를 키로 사용합니다.
        val processorMap = flatList.groupBy { it.processorId }

        // 변경 가능한 리스트 생성 - Kotlin에서는 mutableListOf()로 변경 가능한 리스트를 생성합니다.
        val processors = mutableListOf<Processor>()

        // 각 프로세서 그룹 처리
        // Kotlin의 for 루프에서 구조 분해 할당을 사용하여 키와 값을 직접 가져올 수 있습니다.
        for ((processorId, processorGroup) in processorMap) {
            // 그룹의 첫 번째 항목 가져오기
            val first = processorGroup.first()

            // 프로세서 객체 생성
            // Kotlin의 명명된 매개변수를 사용하여 객체를 생성합니다.
            val processor = Processor(
                processorId = first.processorId,
                processorName = first.processorName,
                partnerCode = first.partnerCode
            )

            // paymentType으로 그룹화
            // 함수형 프로그래밍 스타일로 체이닝된 연산을 사용합니다.
            // filter는 조건에 맞는 항목만 선택하고, groupBy는 결과를 그룹화합니다.
            val paymentMap = processorGroup
                .filter { it.paymentType != null } // null이 아닌 paymentType만 필터링
                .groupBy { it.paymentType }        // paymentType으로 그룹화

            val paymentTypes = mutableListOf<PaymentType>()

            // 각 결제 유형 그룹 처리
            for ((paymentTypeKey, paymentGroup) in paymentMap) {
                // PaymentType 객체 생성
                val paymentType = PaymentType(
                    paymentType = paymentTypeKey
                )

                // 이 결제 유형에 대한 스키마 생성
                // 함수형 프로그래밍 체인을 사용하여 데이터를 변환합니다.
                val schemes = paymentGroup
                    .filter { it.schemeCode != null }  // null이 아닌 schemeCode만 필터링
                    .map { row ->                      // 각 행을 Scheme 객체로 변환
                        Scheme(
                            schemeCode = row.schemeCode,
                            schemeName = row.schemeName
                        )
                    }
                    .distinctBy { it.schemeCode }      // 중복 제거 (schemeCode 기준)

                // 생성된 스키마 목록을 PaymentType에 할당
                paymentType.schemes = schemes
                // 결제 유형 목록에 추가
                paymentTypes.add(paymentType)
            }

            // 생성된 결제 유형 목록을 프로세서에 할당
            processor.paymentTypes = paymentTypes
            // 프로세서 목록에 추가
            processors.add(processor)
        }

        // 최종 결과 반환
        return processors
    }
}
