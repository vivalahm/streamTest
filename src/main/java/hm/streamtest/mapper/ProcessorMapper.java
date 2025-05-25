package hm.streamtest.mapper;

import hm.streamtest.dto.Processor;
import hm.streamtest.dto.FlatProcessorData;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * Mapper interface for Processor-related database operations
 */
@Mapper
public interface ProcessorMapper {
    /**
     * Find all processors with their payment types and schemes using the resultMap approach
     * @return List of processors with nested payment types and schemes
     */
    List<Processor> findAllProcessorsWithDetails();
    
    /**
     * Find all processors with their payment types and schemes as flat data
     * @return List of flat processor data
     */
    List<FlatProcessorData> findAllProcessorsAsFlatData();
}