package hm.streamtest.service

import hm.streamtest.dto.Processor
import hm.streamtest.mapper.ProcessorMapper
import org.springframework.stereotype.Service

/**
 * 프로세서 데이터를 검색하기 위해 MyBatis resultMap 접근 방식을 사용하는 서비스
 * Service that uses MyBatis resultMap approach to retrieve processor data
 */
@Service
class ProcessorResultMapService(private val processorMapper: ProcessorMapper) {

    /**
     * 모든 프로세서와 해당 결제 유형 및 스키마를 가져옵니다.
     * @return 중첩된 결제 유형 및 스키마가 있는 프로세서 목록
     */
    fun getProcessors(): List<Processor> {
        // Kotlin에서는 단일 표현식 함수에서 return 키워드를 생략할 수 있습니다.
        // 이 함수는 MyBatis의 resultMap을 사용하여 이미 계층적으로 구성된 데이터를 직접 가져옵니다.
        // ProcessorStreamService와 달리 데이터 변환 로직이 필요하지 않습니다.
        return processorMapper.findAllProcessorsWithDetails()
    }
}
