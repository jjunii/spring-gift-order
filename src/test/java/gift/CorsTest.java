package gift;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import gift.config.WebConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CorsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void cors() throws Exception {
        // 프론트엔드 서버 가정
        String clientOrigin = "http://localhost:3000";

        mockMvc.perform(
                       options("/api/products")
                               .header(HttpHeaders.ORIGIN, clientOrigin)
                               .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
               )
               .andExpect(status().isOk())
               .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, clientOrigin))
               .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                       WebConfig.ALLOWED_METHOD_NAMES))
               .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                       HttpHeaders.LOCATION))
               .andDo(print());
    }
}