package hm.streamtest.service;

import hm.streamtest.dto.*;
import hm.streamtest.mapper.ProcessorMapper;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that uses Java Stream API to transform flat data into hierarchical structure
 */
@Service
public class ProcessorStreamService {
    private final ProcessorMapper processorMapper;
    
    /**
     * Constructor with dependency injection
     * @param processorMapper The processor mapper
     */
    public ProcessorStreamService(ProcessorMapper processorMapper) {
        this.processorMapper = processorMapper;
    }

    /**
     * Get all processors with their payment types and schemes
     * @return List of processors with nested payment types and schemes
     */
    public List<Processor> getProcessors() {
        // Get flat data from the database
        List<FlatProcessorData> flatList = processorMapper.findAllProcessorsAsFlatData();

        // Group by processorId
        Map<String, List<FlatProcessorData>> processorMap =
                flatList.stream()
                        .collect(Collectors.groupingBy(FlatProcessorData::getProcessorId, LinkedHashMap::new, Collectors.toList()));

        List<Processor> processors = new ArrayList<>();

        // Process each processor group
        for (Map.Entry<String, List<FlatProcessorData>> processorEntry : processorMap.entrySet()) {
            List<FlatProcessorData> processorGroup = processorEntry.getValue();
            FlatProcessorData first = processorGroup.get(0);

            // Create processor
            Processor processor = new Processor();
            processor.setProcessorId(first.getProcessorId());
            processor.setProcessorName(first.getProcessorName());
            processor.setPartnerCode(first.getPartnerCode());

            // Group by paymentType
            Map<String, List<FlatProcessorData>> paymentMap =
                    processorGroup.stream()
                            .filter(row -> row.getPaymentType() != null)
                            .collect(Collectors.groupingBy(FlatProcessorData::getPaymentType, LinkedHashMap::new, Collectors.toList()));

            List<PaymentType> paymentTypes = new ArrayList<>();
            
            // Process each payment type group
            for (Map.Entry<String, List<FlatProcessorData>> paymentEntry : paymentMap.entrySet()) {
                PaymentType paymentType = new PaymentType();
                paymentType.setPaymentType(paymentEntry.getKey());

                // Create schemes for this payment type
                List<Scheme> schemes = paymentEntry.getValue().stream()
                        .filter(row -> row.getSchemeCode() != null)
                        .map(row -> {
                            Scheme scheme = new Scheme();
                            scheme.setSchemeCode(row.getSchemeCode());
                            scheme.setSchemeName(row.getSchemeName());
                            return scheme;
                        })
                        .collect(Collectors.collectingAndThen(
                                Collectors.toMap(Scheme::getSchemeCode, s -> s, (a, b) -> a, LinkedHashMap::new),
                                m -> new ArrayList<>(m.values())
                        ));
                
                paymentType.setSchemes(schemes);
                paymentTypes.add(paymentType);
            }
            
            processor.setPaymentTypes(paymentTypes);
            processors.add(processor);
        }
        
        return processors;
    }
}