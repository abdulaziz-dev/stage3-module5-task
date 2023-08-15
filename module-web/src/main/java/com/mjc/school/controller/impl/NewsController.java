package com.mjc.school.controller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;
import com.mjc.school.controller.BaseController;
import com.mjc.school.service.AuthorService;
import com.mjc.school.service.CommentService;
import com.mjc.school.service.NewsService;
import com.mjc.school.service.TagService;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/news")
@Api(produces = "application/json", value = "Operations for creating, updating, retrieving and deleting news in the application")
public class NewsController implements BaseController<NewsRequestDTO, NewsResponseDTO, Long> {
    private final NewsService service;
    private final TagService tagService;
    private final CommentService commentService;
    private final AuthorService authorService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public NewsController(NewsService newsService, TagService tagService, CommentService commentService, AuthorService authorService) {
        this.service = newsService;
        this.tagService = tagService;
        this.commentService = commentService;
        this.authorService = authorService;
    }

    @Override
    @GetMapping
    @ApiOperation(value = "View all news", response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved all news"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request"),
    })
    public ResponseEntity<List<NewsResponseDTO>> readAll(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "limit", required = false, defaultValue = "5") int limit,
            @RequestParam(value = "sort_by", required = false, defaultValue = "title") String sortBy)
    {
        List<NewsResponseDTO> news = service.readAll(page, limit, sortBy);
        return new ResponseEntity<>(news, HttpStatus.OK);
    }

    @Override
    @GetMapping("/{id:\\d+}")
    @ApiOperation(value = "Retrieve specific news with the supplied id", response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved the news with the supplied id"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<NewsResponseDTO> readById(@PathVariable("id") Long id) {
        NewsResponseDTO newsDTO = service.readById(id);
        return new ResponseEntity<>(newsDTO, HttpStatus.OK);
    }

    @Override
    @PostMapping
    @ApiOperation(value = "Create a piece of news", response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Successfully created a piece of news"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<NewsResponseDTO> create(@RequestBody NewsRequestDTO createRequest) {
        NewsResponseDTO newsDTO = service.create(createRequest);
        return new ResponseEntity<>(newsDTO, HttpStatus.CREATED);
    }

    @Override
    @PutMapping("/{id:\\d+}")
    @ApiOperation(value = "Update news with provided id", response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated news information"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<NewsResponseDTO> update(@PathVariable Long id, @RequestBody NewsRequestDTO updateRequest) {
        NewsResponseDTO newsDTO = service.update(updateRequest);
        return new ResponseEntity<>(newsDTO, HttpStatus.OK);
    }

    @Override
    @DeleteMapping("/{id:\\d+}")
    @ApiOperation(value = "Delete the specific news by provided id")
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successfully deleted the specific news"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable("id") Long id) {
        service.deleteById(id);
    }

    @GetMapping("/search")
    @ApiOperation(value = "Retrieve news with provided parameters", response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved the news with given parameters"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<List<NewsResponseDTO>> readByParams(
                            @RequestParam(name = "tag_id", required = false) Long tagId,
                            @RequestParam(name = "tag_name", required = false) String tagName,
                            @RequestParam(name = "author_name", required = false) String authorName,
                            @RequestParam(name = "title", required = false) String title,
                            @RequestParam(name = "content", required = false) String content){
        return new ResponseEntity<>(service.readByParams(tagId, tagName, authorName, title, content), HttpStatus.OK);
    }

    @GetMapping("/{id:\\d+}/tags")
    @ApiOperation(value = "Retrieve tags of specified news", response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved tags of specific news"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<List<TagResponseDTO>> readTagsByNewsId(@PathVariable Long id) {
        List<TagResponseDTO> tags = tagService.getTagsByNewsId(id);
        return new ResponseEntity<>(tags, HttpStatus.OK);
    }

    @GetMapping("/{id:\\d+}/comments")
    @ApiOperation(value = "Retrieve comments of specified news", response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved comments of specific news"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<List<CommentResponseDTO>> readCommentsByNewsId(@PathVariable Long id) {
        List<CommentResponseDTO> commentsDTO = commentService.getCommentsByNewsId(id);
        return new ResponseEntity<>(commentsDTO, HttpStatus.OK);
    }

    @GetMapping("/{id:\\d+}/author")
    @ApiOperation(value = "Retrieve the author of specified news", response = ResponseEntity.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully retrieved the author of specific news"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<AuthorResponseDTO> readAuthorByNewsId(@PathVariable Long id) {
        AuthorResponseDTO authorDTO = authorService.getAuthorByNewsId(id);
        return new ResponseEntity<>(authorDTO, HttpStatus.OK);
    }

    @PatchMapping(path = "/{id:\\d+}", consumes = "application/json-patch+json")
    @ApiOperation(value = "Partly update news information", response = AuthorResponseDTO.class)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successfully updated news information"),
            @ApiResponse(code = 401, message = "You are not authorized to view the resource"),
            @ApiResponse(code = 404, message = "The resource you were trying to reach is not found"),
            @ApiResponse(code = 500, message = "Application failed to process the request")
    })
    public ResponseEntity<NewsResponseDTO> updatePart(@PathVariable("id") Long id, @RequestBody JsonPatch patch) {
        try {
            NewsResponseDTO news = service.readById(id);
            NewsRequestDTO request = new NewsRequestDTO(news.title(), news.content(), news.authorId(),
                                                        news.tagsSet().stream().map(x-> x.id()).collect(Collectors.toSet()));
            NewsRequestDTO patchedNews = applyPatch(patch, request);
            return new ResponseEntity<>(service.update(patchedNews),HttpStatus.OK);
        }
        catch (JsonPatchException | JsonProcessingException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private NewsRequestDTO applyPatch(JsonPatch patch, NewsRequestDTO dto) throws JsonPatchException, JsonProcessingException {
        JsonNode patched = patch.apply(objectMapper.convertValue(dto, JsonNode.class));
        return objectMapper.treeToValue(patched, NewsRequestDTO.class);
    }

}
