package se.magnus.api.composite.detection;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import reactor.core.publisher.Mono;

public interface DetectionCompositeService {
    @Operation(
    summary =
        "${api.detection-composite.get-detection-aggregate.description}",
    description =
        "${api.detection-composite.get-detection-aggregate.notes}")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description =
            "${api.responseCodes.ok.description}"),
        @ApiResponse(responseCode = "400", description =
            "${api.responseCodes.badRequest.description}"),
        @ApiResponse(responseCode = "404", description =
            "${api.responseCodes.notFound.description}"),
        @ApiResponse(responseCode = "422", description =
            "${api.responseCodes.unprocessableEntity.description}")
    })
    @GetMapping(
        value = "/detection-composite/{sourceId}",
        produces = "application/json")
    Mono<DetectionAggregate> getDetectionAggregate(@PathVariable String sourceId);
}

