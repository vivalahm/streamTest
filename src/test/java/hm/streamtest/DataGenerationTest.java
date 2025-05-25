package hm.streamtest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Random;

@SpringBootTest
public class DataGenerationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final int PROCESSOR_COUNT = 100;
    private static final int SCHEME_COUNT = 1000;
    private static final int PAYMENT_COUNT = 10000;
    private static final Random random = new Random();

    @Test
    public void generateLargeDataset() {
        System.out.println("Generating large dataset for performance testing...");

        try {
            // Generate processors
            System.out.println("Generating " + PROCESSOR_COUNT + " processors...");
            for (int i = 1; i <= PROCESSOR_COUNT; i++) {
                try {
                    String processorId = "P" + String.format("%04d", i);
                    String processorName = "프로세서" + i;
                    String partnerCode = "PARTNER_" + String.format("%03d", i);

                    jdbcTemplate.update(
                        "INSERT INTO Processor (processor_id, processor_name, partner_code) VALUES (?, ?, ?)",
                        processorId, processorName, partnerCode
                    );
                } catch (DataAccessException e) {
                    System.err.println("Error inserting processor " + i + ": " + e.getMessage());
                    // Continue with the next processor
                }
            }

            // Generate schemes
            System.out.println("Generating " + SCHEME_COUNT + " schemes...");
            for (int i = 1; i <= SCHEME_COUNT; i++) {
                try {
                    String schemeCode = "SCHEME_" + String.format("%05d", i);
                    String schemeName = "스킴" + i;

                    jdbcTemplate.update(
                        "INSERT INTO Scheme (scheme_code, scheme_name) VALUES (?, ?)",
                        schemeCode, schemeName
                    );
                } catch (DataAccessException e) {
                    System.err.println("Error inserting scheme " + i + ": " + e.getMessage());
                    // Continue with the next scheme
                }
            }

            // Generate payments
            System.out.println("Generating " + PAYMENT_COUNT + " payments...");
            String[] paymentTypes = {"MOBILE", "CREDIT_CARD", "BANK_TRANSFER", "VIRTUAL_ACCOUNT", "GIFT_CARD"};

            int successCount = 0;
            for (int i = 1; i <= PAYMENT_COUNT; i++) {
                try {
                    // Select random processor
                    int processorIndex = random.nextInt(PROCESSOR_COUNT) + 1;
                    String processorId = "P" + String.format("%04d", processorIndex);
                    String partnerCode = "PARTNER_" + String.format("%03d", processorIndex);

                    // Select random payment type
                    String paymentType = paymentTypes[random.nextInt(paymentTypes.length)];

                    // Select random scheme
                    int schemeIndex = random.nextInt(SCHEME_COUNT) + 1;
                    String schemeCode = "SCHEME_" + String.format("%05d", schemeIndex);

                    jdbcTemplate.update(
                        "INSERT INTO Payment (processor_id, partner_code, payment_type, scheme_code) VALUES (?, ?, ?, ?)",
                        processorId, partnerCode, paymentType, schemeCode
                    );

                    successCount++;
                    if (i % 1000 == 0) {
                        System.out.println("Generated " + i + " payments...");
                    }
                } catch (DataAccessException e) {
                    System.err.println("Error inserting payment " + i + ": " + e.getMessage());
                    // Continue with the next payment
                }
            }

            // Count records
            int processorCount = 0;
            int schemeCount = 0;
            int paymentCount = 0;

            try {
                processorCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Processor", Integer.class);
            } catch (DataAccessException e) {
                System.err.println("Error counting processors: " + e.getMessage());
            }

            try {
                schemeCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Scheme", Integer.class);
            } catch (DataAccessException e) {
                System.err.println("Error counting schemes: " + e.getMessage());
            }

            try {
                paymentCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Payment", Integer.class);
            } catch (DataAccessException e) {
                System.err.println("Error counting payments: " + e.getMessage());
            }

            System.out.println("Data generation completed.");
            System.out.println("Total processors: " + processorCount);
            System.out.println("Total schemes: " + schemeCount);
            System.out.println("Total payments: " + paymentCount);

        } catch (Exception e) {
            System.err.println("Unexpected error during data generation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
