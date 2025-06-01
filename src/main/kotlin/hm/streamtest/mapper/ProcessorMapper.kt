package hm.streamtest.mapper

import hm.streamtest.dto.Processor
import hm.streamtest.dto.FlatProcessorData
import org.apache.ibatis.annotations.Mapper

/**
 * Mapper interface for Processor-related database operations
 */
@Mapper
interface ProcessorMapper {
    /**
     * Find all processors with their payment types and schemes using the resultMap approach
     * @return List of processors with nested payment types and schemes
     */
    fun findAllProcessorsWithDetails(): List<Processor>
    
    /**
     * Find all processors with their payment types and schemes as flat data
     * @return List of flat processor data
     */
    fun findAllProcessorsAsFlatData(): List<FlatProcessorData>
}