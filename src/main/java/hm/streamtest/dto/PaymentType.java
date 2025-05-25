package hm.streamtest.dto;

import lombok.Data;
import java.util.List;

/**
 * DTO for PaymentType information
 */
@Data
public class PaymentType {
    private String paymentType;
    private List<Scheme> schemes;
}