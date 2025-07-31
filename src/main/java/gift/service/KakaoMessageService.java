package gift.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gift.dto.KakaoOrderMessageDto;
import gift.dto.KakaoTokenResponseDto;
import gift.entity.Member;
import gift.entity.Order;
import gift.exception.UnAuthenticationException;
import gift.repository.MemberRepository;
import gift.util.CurrentMemberContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Service
public class KakaoMessageService {

    private final MemberRepository memberRepository;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final KakaoAuthService kakaoAuthService;

    public KakaoMessageService(MemberRepository memberRepository, ObjectMapper objectMapper,
            KakaoAuthService kakaoAuthService) {
        this.memberRepository = memberRepository;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();
        this.kakaoAuthService = kakaoAuthService;
    }

    @Transactional
    public void sendOrderMessage(Order order) {
        Long memberId = CurrentMemberContext.getAuthenticatedMemberId();

        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new UnAuthenticationException("인증되지 않은 사용자입니다"));

        KakaoOrderMessageDto orderMessage = KakaoOrderMessageDto.from(order);

        try {
            sendRequestToKakao(member.getKakaoAccessToken(), orderMessage);
        } catch (HttpClientErrorException.Unauthorized e) {
            KakaoTokenResponseDto newToken = kakaoAuthService.refreshAccessToken(
                    member.getKakaoRefreshToken());

            member.updateKakaoTokens(
                    newToken.accessToken(),
                    newToken.refreshToken()
            );
            memberRepository.save(member);

            sendRequestToKakao(newToken.accessToken(), orderMessage);
        }
    }

    private void sendRequestToKakao(String accessToken, KakaoOrderMessageDto orderMessage) {
        String url = "https://kapi.kakao.com/v2/api/talk/memo/default/send";

        restClient.post()
                  .uri(url)
                  .header("Authorization",
                          "Bearer " + accessToken)
                  .body(createMessageBody(orderMessage))
                  .retrieve()
                  .toBodilessEntity();
    }

    private MultiValueMap<String, String> createMessageBody(KakaoOrderMessageDto orderMessage) {
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
