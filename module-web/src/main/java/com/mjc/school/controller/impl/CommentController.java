package com.mjc.school.controller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.mjc.school.controller.BaseController;
import com.mjc.school.service.CommentService;
import com.mjc.school.service.dto.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/v1/comments")
@Api(produces = "application/json", value = "Operations for creating, updating, retrieving and deleting comments in the application")
public class CommentController implements BaseController<CommentRequestDTO, CommentResponseDTO, Long> {

    private final CommentService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public CommentController(CommentService service) {
        this.service = service;
    }

    @Override
    @GetMapping
    @ApiOperation(value = "View all comments", response = List.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved all comments"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request"),
    })
    public ResponseEntity<List<CommentResponseDTO>> readAll(
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(name = "limit", required = false, defaultValue = "5") int limit,
            @RequestParam(name = "sort_by", required = false, defaultValue = "content") String sortBy) {
        List<CommentResponseDTO> comments = service.readAll(page, limit, sortBy);
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }

    @Override
    @GetMapping("/{id:\\d+}")
    @ApiOperation(value = "Retrieve specific comment with the supplied id", response = CommentResponseDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved the comment with the supplied id"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<CommentResponseDTO> readById(@PathVariable Long id) {
        CommentResponseDTO commentDTO = service.readById(id);
        return new ResponseEntity<>(commentDTO, HttpStatus.OK);
    }


    @Override
    @PostMapping
    @ApiOperation(value = "Create a new comment", response = CommentResponseDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created a comment"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CommentResponseDTO> create(@RequestBody CommentRequestDTO createRequest) {
        CommentResponseDTO commentDTO = service.create(createRequest);
        return new ResponseEntity<>(commentDTO, HttpStatus.CREATED);
    }

    @Override
    @PutMapping("/{id}")
    @ApiOperation(value = "Update specific comment by supplied id", response = CommentResponseDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated specific comment"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<CommentResponseDTO> update(@PathVariable("id") Long id,
                                                     @RequestBody CommentRequestDTO updateRequest) {
        CommentResponseDTO commentDTO = service.update(updateRequest);
        return new ResponseEntity<>(commentDTO, HttpStatus.OK);
    }

    @PatchMapping(path = "/{id:\\d+}", consumes = "application/json-patch+json")
    @ApiOperation(value = "Partly update comment information", response = CommentResponseDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated author information"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<CommentResponseDTO> updatePart(@PathVariable("id") Long id, @RequestBody JsonPatch patch) {
        try {
            CommentResponseDTO comment = service.readById(id);
            CommentRequestDTO request = new CommentRequestDTO(comment.content(), comment.newsId());
            CommentRequestDTO patchedComment = applyPatch(patch, request);

            return new ResponseEntity<>(service.update(patchedComment),HttpStatus.OK);
        }
        catch (JsonPatchException | JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @DeleteMapping("/{id}")
    @ApiOperation(value = "Delete the specific comment by given id")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successfully deleted the specific comment"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable("id") Long id) {
        service.deleteById(id);
    }

    private CommentRequestDTO applyPatch(JsonPatch patch, CommentRequestDTO dto) throws JsonPatchException, JsonProcessingException {
        JsonNode patched = patch.apply(objectMapper.convertValue(dto, JsonNode.class));
        return objectMapper.treeToValue(patched, CommentRequestDTO.class);
    }
}
