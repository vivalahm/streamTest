package hm.streamtest.dto;

import lombok.Data;
import java.util.List;

/**
 * DTO for Processor information
 */
@Data
public class Processor {
    private String processorId;
    private String processorName;
    private String partnerCode;
    private List<PaymentType> paymentTypes;
}