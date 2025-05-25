package hm.streamtest.controller;

import hm.streamtest.dto.Processor;
import hm.streamtest.service.ProcessorResultMapService;
import hm.streamtest.service.ProcessorStreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * REST controller for processor-related endpoints
 */
@RestController
@RequestMapping("/api/processors")
@Tag(name = "Processor API", description = "API to retrieve processor data using different approaches")
public class ProcessorController {
    private final ProcessorResultMapService resultMapService;
    private final ProcessorStreamService streamService;

    /**
     * Constructor with dependency injection
     * @param resultMapService The resultMap-based service
     * @param streamService The stream-based service
     */
    public ProcessorController(ProcessorResultMapService resultMapService, ProcessorStreamService streamService) {
        this.resultMapService = resultMapService;
        this.streamService = streamService;
    }

    /**
     * Get processors using the resultMap approach
     * @return List of processors with nested payment types and schemes
     */
    @Operation(
        summary = "Get processors using ResultMap approach",
        description = "Retrieves a list of processors with their payment types and schemes using MyBatis ResultMap"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved processors",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Processor.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content
        )
    })
    @GetMapping("/resultMap")
    public List<Processor> getByResultMap() {
        return resultMapService.getProcessors();
    }

    /**
     * Get processors using the stream approach
     * @return List of processors with nested payment types and schemes
     */
    @Operation(
        summary = "Get processors using Stream approach",
        description = "Retrieves a list of processors with their payment types and schemes using Java Stream API"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved processors",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Processor.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content
        )
    })
    @GetMapping("/stream")
    public List<Processor> getByStream() {
        return streamService.getProcessors();
    }

    /**
     * Performance test endpoint that compares the execution time of both approaches
     * @return Performance test results
     */
    @Operation(
        summary = "Compare performance of both approaches",
        description = "Executes both ResultMap and Stream approaches and returns their execution times in milliseconds"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully executed performance test",
            content = @Content(
                mediaType = "text/plain",
                schema = @Schema(type = "string", example = "[resultMap] ms: 42, [stream] ms: 56")
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content
        )
    })
    @GetMapping("/perf")
    public String perfTest() {
        long t1 = System.currentTimeMillis();
        resultMapService.getProcessors();
        long t2 = System.currentTimeMillis();
        streamService.getProcessors();
        long t3 = System.currentTimeMillis();
        return "[resultMap] ms: " + (t2-t1) + ", [stream] ms: " + (t3-t2);
    }
}
