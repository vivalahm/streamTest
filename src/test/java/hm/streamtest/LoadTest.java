package hm.streamtest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LoadTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private static final int TOTAL_REQUESTS = 1000;
    private static final int CONCURRENCY = 10;
    private static final int WARMUP_REQUESTS = 10;
    private static final String OUTPUT_DIR = "load-test-results";

    @Test
    public void comparePerformance() throws Exception {
        // Create output directory
        Path outputPath = Paths.get(OUTPUT_DIR);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        System.out.println("Starting load tests with " + TOTAL_REQUESTS + " requests and concurrency level " + CONCURRENCY);

        // Warm-up
        System.out.println("Warming up...");
        for (int i = 0; i < WARMUP_REQUESTS; i++) {
            restTemplate.getForEntity(getUrl("/api/processors/resultMap"), String.class);
            restTemplate.getForEntity(getUrl("/api/processors/stream"), String.class);
        }

        // Test ResultMap approach
        System.out.println("Testing ResultMap approach...");
        TestResult resultMapResult = runLoadTest("/api/processors/resultMap");

        // Wait between tests
        Thread.sleep(5000);

        // Test Stream approach
        System.out.println("Testing Stream approach...");
        TestResult streamResult = runLoadTest("/api/processors/stream");

        // Generate comparison report
        generateReport(resultMapResult, streamResult);

        System.out.println("Load tests completed. Results are available in the " + OUTPUT_DIR + " directory.");
        System.out.println("See " + OUTPUT_DIR + "/comparison.txt for a summary of the results.");
    }

    private TestResult runLoadTest(String endpoint) throws Exception {
        String url = getUrl(endpoint);
        List<Long> responseTimes = new ArrayList<>();
        long startTime = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENCY);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int i = 0; i < TOTAL_REQUESTS; i++) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                long requestStart = System.currentTimeMillis();
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                long requestEnd = System.currentTimeMillis();
                long responseTime = requestEnd - requestStart;
                
                synchronized (responseTimes) {
                    responseTimes.add(responseTime);
                }
                
                if (!response.getStatusCode().is2xxSuccessful()) {
                    System.err.println("Request failed with status: " + response.getStatusCode());
                }
            }, executor);
            
            futures.add(future);
        }

        // Wait for all requests to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        // Calculate metrics
        responseTimes.sort(Long::compare);
        double meanResponseTime = responseTimes.stream().mapToLong(Long::valueOf).average().orElse(0);
        long p95ResponseTime = responseTimes.get((int) (responseTimes.size() * 0.95));
        double requestsPerSecond = 1000.0 * TOTAL_REQUESTS / totalTime;

        return new TestResult(requestsPerSecond, meanResponseTime, p95ResponseTime, responseTimes);
    }

    private void generateReport(TestResult resultMapResult, TestResult streamResult) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(OUTPUT_DIR + "/comparison.txt"))) {
            writer.println("Performance Comparison: ResultMap vs Stream");
            writer.println();
            writer.println("Test Configuration:");
            writer.println("- Requests: " + TOTAL_REQUESTS);
            writer.println("- Concurrency: " + CONCURRENCY);
            writer.println();
            writer.println("ResultMap Approach:");
            writer.println("- Requests per second: " + String.format("%.2f", resultMapResult.getRequestsPerSecond()));
            writer.println("- Mean response time: " + String.format("%.2f", resultMapResult.getMeanResponseTime()) + " ms");
            writer.println("- 95th percentile response time: " + resultMapResult.getP95ResponseTime() + " ms");
            writer.println();
            writer.println("Stream Approach:");
            writer.println("- Requests per second: " + String.format("%.2f", streamResult.getRequestsPerSecond()));
            writer.println("- Mean response time: " + String.format("%.2f", streamResult.getMeanResponseTime()) + " ms");
            writer.println("- 95th percentile response time: " + streamResult.getP95ResponseTime() + " ms");
            writer.println();
            writer.println("Performance Difference:");
            
            double rpsPercentage = (streamResult.getRequestsPerSecond() - resultMapResult.getRequestsPerSecond()) 
                                  / resultMapResult.getRequestsPerSecond() * 100;
            double meanRtPercentage = (streamResult.getMeanResponseTime() - resultMapResult.getMeanResponseTime()) 
                                    / resultMapResult.getMeanResponseTime() * 100;
            double p95RtPercentage = (streamResult.getP95ResponseTime() - resultMapResult.getP95ResponseTime()) 
                                   / resultMapResult.getP95ResponseTime() * 100;
            
            writer.println("- Requests per second: " + String.format("%.2f", rpsPercentage) + "% (Stream vs ResultMap)");
            writer.println("- Mean response time: " + String.format("%.2f", meanRtPercentage) + "% (Stream vs ResultMap)");
            writer.println("- 95th percentile response time: " + String.format("%.2f", p95RtPercentage) + "% (Stream vs ResultMap)");
            
            // Add conclusion
            writer.println();
            writer.println("Conclusion:");
            if (streamResult.getRequestsPerSecond() > resultMapResult.getRequestsPerSecond()) {
                writer.println("The Stream approach has higher throughput than the ResultMap approach.");
            } else {
                writer.println("The ResultMap approach has higher throughput than the Stream approach.");
            }
            
            if (streamResult.getMeanResponseTime() < resultMapResult.getMeanResponseTime()) {
                writer.println("The Stream approach has lower average response time than the ResultMap approach.");
            } else {
                writer.println("The ResultMap approach has lower average response time than the Stream approach.");
            }
            
            writer.println();
            writer.println("Recommendation:");
            if (streamResult.getRequestsPerSecond() > resultMapResult.getRequestsPerSecond() && 
                streamResult.getMeanResponseTime() < resultMapResult.getMeanResponseTime()) {
                writer.println("The Stream approach is recommended for better performance.");
            } else if (resultMapResult.getRequestsPerSecond() > streamResult.getRequestsPerSecond() && 
                       resultMapResult.getMeanResponseTime() < streamResult.getMeanResponseTime()) {
                writer.println("The ResultMap approach is recommended for better performance.");
            } else {
                writer.println("Consider the trade-off between throughput and response time based on your specific requirements.");
                if (streamResult.getRequestsPerSecond() > resultMapResult.getRequestsPerSecond()) {
                    writer.println("- Use Stream approach if throughput is more important.");
                } else {
                    writer.println("- Use ResultMap approach if throughput is more important.");
                }
                
                if (streamResult.getMeanResponseTime() < resultMapResult.getMeanResponseTime()) {
                    writer.println("- Use Stream approach if response time is more important.");
                } else {
                    writer.println("- Use ResultMap approach if response time is more important.");
                }
            }
        }
    }

    private String getUrl(String endpoint) {
        return "http://localhost:" + port + endpoint;
    }

    private static class TestResult {
        private final double requestsPerSecond;
        private final double meanResponseTime;
        private final long p95ResponseTime;
        private final List<Long> responseTimes;

        public TestResult(double requestsPerSecond, double meanResponseTime, long p95ResponseTime, List<Long> responseTimes) {
            this.requestsPerSecond = requestsPerSecond;
            this.meanResponseTime = meanResponseTime;
            this.p95ResponseTime = p95ResponseTime;
            this.responseTimes = responseTimes;
        }

        public double getRequestsPerSecond() {
            return requestsPerSecond;
        }

        public double getMeanResponseTime() {
            return meanResponseTime;
        }

        public long getP95ResponseTime() {
            return p95ResponseTime;
        }

        public List<Long> getResponseTimes() {
            return responseTimes;
        }
    }
}