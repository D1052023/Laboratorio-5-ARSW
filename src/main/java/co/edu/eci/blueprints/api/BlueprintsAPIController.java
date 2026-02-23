package co.edu.eci.blueprints.api;

import co.edu.eci.blueprints.dto.ApiResponse;
import co.edu.eci.blueprints.model.Blueprint;
import co.edu.eci.blueprints.model.Point;
import co.edu.eci.blueprints.persistence.BlueprintNotFoundException;
import co.edu.eci.blueprints.persistence.BlueprintPersistenceException;
import co.edu.eci.blueprints.services.BlueprintsServices;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.Set;

@Tag(name = "Blueprints", description = "Operations related to blueprints")
@RestController
@RequestMapping("/api/v1/blueprints")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) {
        this.services = services;
    }

    // GET /blueprints
    @Operation(summary = "Get all blueprints", description = "Returns the complete list of blueprints stored in the system")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blueprints retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Set<Blueprint>>> getAll() {

        Set<Blueprint> data = services.getAllBlueprints();

        return ResponseEntity.ok(
                new ApiResponse<>(200, "execute ok", data));
    }

    // GET /blueprints/{author}
    @Operation(summary = "Get blueprints by author", description = "Returns all blueprints created by the specified author")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blueprints retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Author not found")
    })
    @GetMapping("/{author}")
    public ResponseEntity<ApiResponse<Set<Blueprint>>> byAuthor(@PathVariable String author) {

        try {
            Set<Blueprint> data = services.getBlueprintsByAuthor(author);

            return ResponseEntity.ok(
                    new ApiResponse<>(200, "execute ok", data));

        } catch (BlueprintNotFoundException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        }
    }

    // GET /blueprints/{author}/{bpname}
    @Operation(summary = "Get blueprint by author and name", description = "Returns a specific blueprint identified by its author and name")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blueprint retrieved successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint not found")
    })
    @GetMapping("/{author}/{bpname}")
    public ResponseEntity<ApiResponse<Blueprint>> byAuthorAndName(
            @PathVariable String author,
            @PathVariable String bpname) {

        try {

            Blueprint data = services.getBlueprint(author, bpname);

            return ResponseEntity.ok(
                    new ApiResponse<>(200, "execute ok", data));

        } catch (BlueprintNotFoundException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        }
    }

    // POST /blueprints
    @Operation(summary = "Create a new blueprint", description = "Creates a new blueprint with the provided author, name and list of points")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Blueprint created successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Blueprint already exists or persistence error")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> add(
            @Valid @RequestBody NewBlueprintRequest req) {

        try {

            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(201, "Blueprint created", null));

        } catch (BlueprintPersistenceException e) {

            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse<>(403, e.getMessage(), null));
        }
    }

    // PUT /blueprints/{author}/{bpname}/points
    @Operation(summary = "Add a point to a blueprint", description = "Adds a new point (x,y) to an existing blueprint identified by author and name")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Point added successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint not found")
    })
    @PutMapping("/{author}/{bpname}/points")
    public ResponseEntity<ApiResponse<Void>> addPoint(
            @PathVariable String author,
            @PathVariable String bpname,
            @RequestBody Point p) {

        try {

            services.addPoint(author, bpname, p.x(), p.y());

            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(new ApiResponse<>(202, "Point added", null));

        } catch (BlueprintNotFoundException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(404, e.getMessage(), null));
        }
    }

    public record NewBlueprintRequest(
            @NotBlank String author,
            @NotBlank String name,
            @Valid java.util.List<Point> points) {
    }
}