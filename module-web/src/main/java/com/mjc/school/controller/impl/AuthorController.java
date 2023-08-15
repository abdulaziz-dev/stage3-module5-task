package com.mjc.school.controller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.mjc.school.controller.BaseController;
import com.mjc.school.service.AuthorService;
import com.mjc.school.service.dto.AuthorRequestDTO;
import com.mjc.school.service.dto.AuthorResponseDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/authors")
@Api(produces = "application/json", value = "Operations for creating, updating, retrieving and deleting authors in the application")
public class AuthorController implements BaseController<AuthorRequestDTO, AuthorResponseDTO, Long> {
    private final AuthorService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthorController(AuthorService service) {
        this.service = service;
    }

    @Override
    @GetMapping
    @ApiOperation(value = "View all authors", response = List.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved all authors"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request"),
    })
    public ResponseEntity<List<AuthorResponseDTO>> readAll(
                            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
                            @RequestParam(value = "limit", required = false, defaultValue = "5") int limit,
                            @RequestParam(value = "sort_by", required = false, defaultValue = "name") String sortBy)
    {
        List<AuthorResponseDTO> authors = service.readAll(page, limit, sortBy);
        return new ResponseEntity<>(authors, HttpStatus.OK);
    }

    @Override
    @GetMapping("/{id:\\d+}")
    @ApiOperation(value = "Retrieve specific author with the supplied id", response = AuthorResponseDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved the author with the supplied id"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<AuthorResponseDTO> readById(@PathVariable Long id) {
        AuthorResponseDTO authorDTO = service.readById(id);
        return new ResponseEntity<>(authorDTO, HttpStatus.OK);
    }

    @Override
    @PostMapping
    @ApiOperation(value = "Create a new author", response = AuthorResponseDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created an author"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AuthorResponseDTO> create(@RequestBody AuthorRequestDTO createRequest) {
        AuthorResponseDTO authorDTO = service.create(createRequest);
        return new ResponseEntity<>(authorDTO, HttpStatus.CREATED);
    }

    @Override
    @PutMapping("/{id:\\d+}")
    @ApiOperation(value = "Update an author", response = AuthorResponseDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated author information"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    }    )
    public ResponseEntity<AuthorResponseDTO> update(@PathVariable Long id,
                                                    @RequestBody AuthorRequestDTO updateRequest) {
        AuthorResponseDTO authorDTO = service.update(updateRequest);
        return new ResponseEntity<>(authorDTO, HttpStatus.OK);
    }

    @PatchMapping(path = "/{id:\\d+}", consumes = "application/json-patch+json")
    @ApiOperation(value = "Partly update author information", response = AuthorResponseDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated author information"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<AuthorResponseDTO> updatePart(@PathVariable("id") Long id, @RequestBody JsonPatch patch) {
        try {
            AuthorResponseDTO author = service.readById(id);
            AuthorRequestDTO request = new AuthorRequestDTO(author.name());
            AuthorRequestDTO patchedAuthor = applyPatch(patch, request);

            return new ResponseEntity<>(service.update(patchedAuthor),HttpStatus.OK);
        }
        catch (JsonPatchException | JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @DeleteMapping("/{id:\\d+}")
    @ApiOperation(value = "Delete the specific author by given id")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successfully deleted the specific author"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) {
        service.deleteById(id);
    }

    private AuthorRequestDTO applyPatch(JsonPatch patch, AuthorRequestDTO dto) throws JsonPatchException, JsonProcessingException {
        JsonNode patched = patch.apply(objectMapper.convertValue(dto, JsonNode.class));
        return objectMapper.treeToValue(patched, AuthorRequestDTO.class);
    }
}
