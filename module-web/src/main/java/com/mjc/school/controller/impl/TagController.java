package com.mjc.school.controller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.mjc.school.controller.BaseController;
import com.mjc.school.service.TagService;
import com.mjc.school.service.dto.NewsResponseDTO;
import com.mjc.school.service.dto.TagRequestDTO;
import com.mjc.school.service.dto.TagResponseDTO;
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
@RequestMapping("/api/v1/tags")
@Api(produces = "application/json", value = "Operations for creating, updating, retrieving and deleting tags in the application")
public class TagController implements BaseController<TagRequestDTO, TagResponseDTO, Long> {

    private final TagService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public TagController(TagService service) {
        this.service = service;
    }

    @Override
    @GetMapping()
    @ApiOperation(value = "View all tags", response = List.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved all tags"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<List<TagResponseDTO>> readAll(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "limit", required = false, defaultValue = "5") int limit,
            @RequestParam(value = "sort_by", required = false, defaultValue = "name") String sortBy)
    {
        List<TagResponseDTO> tags = service.readAll(page, limit, sortBy);
        return new ResponseEntity<>(tags, HttpStatus.OK);
    }

    @Override
    @GetMapping("/{id:\\d+}")
    @ApiOperation(value = "Retrieve specific tags with the supplied id", response = TagResponseDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved the tag with supplied id"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<TagResponseDTO> readById(@PathVariable("id") Long id) {
        TagResponseDTO response = service.readById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    @PostMapping
    @ApiOperation(value = "Create a new tag", response = TagResponseDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created a tag"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TagResponseDTO> create(@RequestBody TagRequestDTO createRequest) {
        TagResponseDTO response = service.create(createRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    @PutMapping("/{id:\\d+}")
    @ApiOperation(value = "Update specific tag with supplied id", response = TagResponseDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated the tag with the supplied id"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<TagResponseDTO> update(@PathVariable("id") Long id, @RequestBody TagRequestDTO updateRequest) {
        TagResponseDTO response = service.update(updateRequest);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @PatchMapping(path = "/{id:\\d+}", consumes = "application/json-patch+json")
    @ApiOperation(value = "Partly update specific tags with the supplied id", response = TagResponseDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated the tag with the supplied id"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<TagResponseDTO> updatePart(@PathVariable("id") Long id, @RequestBody JsonPatch patch) {
        try {
            TagResponseDTO tag = service.readById(id);
            TagRequestDTO requestTag = new TagRequestDTO(tag.name());
            TagRequestDTO patchedTag = applyPatchToTag(patch, requestTag);
            return new ResponseEntity<>(service.update(patchedTag),HttpStatus.OK);
        }
        catch (JsonPatchException | JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    @DeleteMapping("/{id:\\d+}")
    @ApiOperation(value = "Delete specific tags with the supplied id")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successfully deleted the tag with supplied id"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable("id") Long id) {
        service.deleteById(id);
    }

    private TagRequestDTO applyPatchToTag(JsonPatch patch, TagRequestDTO tag) throws JsonPatchException, JsonProcessingException {
        JsonNode patched = patch.apply(objectMapper.convertValue(tag, JsonNode.class));
        return objectMapper.treeToValue(patched, TagRequestDTO.class);
    }
}
