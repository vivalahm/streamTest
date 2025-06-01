package hm.streamtest.service

import hm.streamtest.dto.*
import hm.streamtest.mapper.ProcessorMapper
import org.springframework.stereotype.Service
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * 평면 데이터를 계층 구조(프로세서 → 결제유형 → 스키마)로 변환하는 서비스
 */
@Service
class ProcessorStreamService(private val processorMapper: ProcessorMapper) {

    /**
     * DB에서 평면 데이터를 가져와,
     * processorId → paymentType → scheme 계층으로 구성된 Processor 리스트 반환
     */
    fun getProcessors(): List<Processor> {
        // 1. DB에서 평면(Flat) 데이터 가져오기
        val flatList = processorMapper.findAllProcessorsAsFlatData()

        // 2. processorId 기준으로 그룹화(Map)
        return flatList
            .groupBy { it.processorId }
            .map { (_, processorGroup) -> // (processorId, 동일 id 가진 데이터 리스트)
                // 3. 그룹의 첫 번째 항목에서 processor 기본정보 추출
                val first = processorGroup.first()

                Processor(
                    processorId = first.processorId,
                    processorName = first.processorName,
                    partnerCode = first.partnerCode,
                    // 4. paymentType으로 2차 그룹화하여 PaymentType 리스트 생성
                    paymentTypes = processorGroup
                        .filter { it.paymentType != null } // 결제유형 없는 경우 제외
                        .groupBy { it.paymentType }
                        .map { (paymentTypeKey, paymentGroup) ->
                            PaymentType(
                                paymentType = paymentTypeKey,
                                // 5. SkemeCode로 중복제거 하여 Scheme 리스트 생성
                                schemes = paymentGroup
                                    .filter { it.schemeCode != null } // 스키마 없는 경우 제외
                                    .map { row ->
                                        Scheme(
                                            schemeCode = row.schemeCode,
                                            schemeName = row.schemeName
                                        )
                                    }
                                    .distinctBy { it.schemeCode } // schemeCode 기준 중복 제거
                            )
                        }
                )
            }
    }
}