package gift.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public record KakaoMessageRequestDto(
        KakaoOrderMessageDto orderMessage
) {

    public MultiValueMap<String, String> toMultiValueMap(ObjectMapper objectMapper) {
        String templateObjectJson;
        try {
            templateObjectJson = objectMapper.writeValueAsString(orderMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("카카오 메세지 템플릿 생성에 실패했습니다.");
        }

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("template_object", templateObjectJson);
        return body;
    }
}