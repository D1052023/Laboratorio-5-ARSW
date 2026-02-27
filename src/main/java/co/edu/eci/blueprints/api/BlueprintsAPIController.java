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

@Tag(name = "Blueprints", description = "Operaciones relacionadas con la gestión de blueprints (planos)")
@RestController
@RequestMapping("/api/v1/blueprints")
public class BlueprintsAPIController {

        private final BlueprintsServices services;

        public BlueprintsAPIController(BlueprintsServices services) {
                this.services = services;
        }

        // GET /blueprints
        @Operation(summary = "Obtener todos los blueprints", description = "Retorna la lista completa de blueprints almacenados en el sistema")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blueprints obtenidos correctamente")
        })
        public ResponseEntity<ApiResponse<Set<Blueprint>>> getAll() {

                Set<Blueprint> data = services.getAllBlueprints();

                return ResponseEntity.ok(
                                new ApiResponse<>(200, "execute ok", data));
        }

        // GET /blueprints/{author}
        @Operation(summary = "Obtener blueprints por autor", description = "Retorna todos los blueprints creados por el autor especificado")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blueprints obtenidos correctamente"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Autor no encontrado")
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
        @Operation(summary = "Obtener blueprint por autor y nombre", description = "Retorna un blueprint específico identificado por su autor y nombre")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Blueprint obtenido correctamente"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint no encontrado")
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
        @Operation(summary = "Crear un nuevo blueprint", description = "Permite registrar un nuevo blueprint con autor, nombre y lista de puntos")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Blueprint creado correctamente"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "El blueprint ya existe o error de persistencia")
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
        @Operation(summary = "Agregar punto a un blueprint", description = "Agrega un nuevo punto (x,y) a un blueprint existente")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "Punto agregado correctamente"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Blueprint no encontrado")
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