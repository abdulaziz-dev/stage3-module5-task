import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mjc.school.service.dto.AuthorRequestDTO;
import com.mjc.school.service.dto.AuthorResponseDTO;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import static io.restassured.RestAssured.delete;
import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.get;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class RestAssuredAPITest {

    private final ObjectMapper mapper = new ObjectMapper();

    {
        mapper.registerModule(new JavaTimeModule());
    }

    @BeforeAll
    public static void setup() {
        String port = System.getProperty("server.port");
        if (port == null) {
            RestAssured.port = 8080;
        }
        else{
            RestAssured.port = Integer.parseInt(port);
        }

        String basePath = System.getProperty("server.base");
        if(basePath == null){
            basePath = "/api/v1";
        }
        RestAssured.basePath = basePath;

        String baseHost = System.getProperty("server.host");
        if(baseHost == null){
            baseHost = "http://localhost/module-web-plain";
        }
        RestAssured.baseURI = baseHost;

    }


//    @BeforeEach
//    void setUp() {
//        //Create a piece of news for the testing purpose.
//        News news = new News(EXPECTED_NEWS_CONTENT);
//        News createdNews = newsService.create(news);
//        newsID = createdNews.getId();
//    }

    @Test
    void CreateAuthorTest() throws JsonProcessingException {
        final int EXPECTED_STATUS_CODE = 201;
        Response response = given()
                .contentType(ContentType.JSON)
                .body(new AuthorRequestDTO("Test User"))
                .when()
                .post("/authors");

        String responseAsString = response.asString();
        AuthorResponseDTO createdAuthor = mapper.readValue(responseAsString, AuthorResponseDTO.class);
        assertEquals(EXPECTED_STATUS_CODE, response.getStatusCode());
        assertNotNull(responseAsString);

//        delete author
        delete("/authors/"+createdAuthor.id())
                .then()
                .statusCode(204);
    }

    @Test
    void GetAuthorsTest(){
        final int EXPECTED_STATUS_CODE = 200;
        Response response = get("/authors");

        assertEquals(EXPECTED_STATUS_CODE, response.getStatusCode());
        assertNotNull(response);
    }

    @Test
    void GetAuthorTest() throws JsonProcessingException {
        final int EXPECTED_STATUS_CODE = 200;

        //create new Author
        Response testUser = given()
                .contentType(ContentType.JSON)
                .body(new AuthorRequestDTO("Test user"))
                .when()
                .post("/authors");

        String createdUserString = testUser.asString();
        AuthorResponseDTO createdAuthor = mapper.readValue(createdUserString, AuthorResponseDTO.class);

        // request created Author
        Response response = get("authors/" + createdAuthor.id());
        String requestedUserString = response.asString();
        AuthorResponseDTO requestedAuthor = mapper.readValue(requestedUserString, AuthorResponseDTO.class);

        assertEquals(EXPECTED_STATUS_CODE, response.getStatusCode());
        assertNotNull(requestedAuthor);

        //delete author
        delete("/authors/"+createdAuthor.id())
                .then()
                .statusCode(204);

    }

}