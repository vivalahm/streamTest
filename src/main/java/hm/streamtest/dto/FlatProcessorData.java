package hm.streamtest.dto;

import lombok.Data;

/**
 * DTO for flattened processor data
 * This is used for the stream-based approach
 */
@Data
public class FlatProcessorData {
    private String processorId;
    private String processorName;
    private String partnerCode;
    private String paymentType;
    private String schemeCode;
    private String schemeName;
}