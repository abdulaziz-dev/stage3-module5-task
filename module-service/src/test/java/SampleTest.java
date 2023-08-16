import com.mjc.school.service.dto.AuthorRequestDTO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SampleTest {
    @Test
    void doTest(){
        AuthorRequestDTO dto = new AuthorRequestDTO("John");
        Assertions.assertEquals(dto.name(), "John");
    }
}
