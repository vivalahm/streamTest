package hm.streamtest.service;

import hm.streamtest.dto.Processor;
import hm.streamtest.mapper.ProcessorMapper;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service that uses MyBatis resultMap approach to retrieve processor data
 */
@Service
public class ProcessorResultMapService {
    private final ProcessorMapper processorMapper;
    
    /**
     * Constructor with dependency injection
     * @param processorMapper The processor mapper
     */
    public ProcessorResultMapService(ProcessorMapper processorMapper) {
        this.processorMapper = processorMapper;
    }
    
    /**
     * Get all processors with their payment types and schemes
     * @return List of processors with nested payment types and schemes
     */
    public List<Processor> getProcessors() {
        return processorMapper.findAllProcessorsWithDetails();
    }
}