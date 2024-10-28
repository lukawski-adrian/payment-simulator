package pl.varlab.payment;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles("test")
public class BaseSpringContextTest {

    protected static final ObjectMapper MAPPER = new ObjectMapper();
    protected static final String EMPTY = "";

}
