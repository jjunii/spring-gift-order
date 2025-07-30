package gift.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gift.dto.KakaoOrderMessageDto;
import gift.entity.Member;
import gift.entity.Order;
import gift.exception.UnAuthenticationException;
import gift.repository.MemberRepository;
import gift.util.CurrentMemberContext;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class KakaoMessageService {

    private final MemberRepository memberRepository;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public KakaoMessageService(MemberRepository memberRepository, ObjectMapper objectMapper) {
        this.memberRepository = memberRepository;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();
    }

    public void sendOrderMessage(Order order) {
        Long memberId = CurrentMemberContext.getAuthenticatedMemberId();

        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new UnAuthenticationException("인증되지 않은 사용자입니다"));

        String url = "https://kapi.kakao.com/v2/api/talk/memo/default/send";

        KakaoOrderMessageDto orderMessage = KakaoOrderMessageDto.from(order);

        String templateObjectJson;
        try {
            templateObjectJson = objectMapper.writeValueAsString(orderMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("카카오 메세지 템플릿 생성에 실패했습니다.");
        }

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("template_object", templateObjectJson);

        restClient.post()
                  .uri(url)
                  .header("Authorization",
                          "Bearer " + member.getKakaoAccessToken())
                  .body(body)
                  .retrieve()
                  .toBodilessEntity();
    }
}
