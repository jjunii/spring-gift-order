package gift.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gift.config.KakaoApiConstants;
import gift.dto.KakaoMessageRequestDto;
import gift.dto.KakaoOrderMessageDto;
import gift.dto.KakaoTokenResponseDto;
import gift.entity.Member;
import gift.entity.Order;
import gift.exception.UnAuthenticationException;
import gift.repository.MemberRepository;
import gift.util.CurrentMemberContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        KakaoMessageRequestDto requestBody = new KakaoMessageRequestDto(orderMessage);

        try {
            sendRequestToKakao(member.getKakaoAccessToken(), requestBody);
        } catch (HttpClientErrorException.Unauthorized e) {
            KakaoTokenResponseDto newToken = kakaoAuthService.refreshAccessToken(
                    member.getKakaoRefreshToken());

            member.updateKakaoTokens(
                    newToken.accessToken(),
                    newToken.refreshToken()
            );
            memberRepository.save(member);

            sendRequestToKakao(newToken.accessToken(), requestBody);
        }
    }

    private void sendRequestToKakao(String accessToken, KakaoMessageRequestDto requestBody) {
        String url = KakaoApiConstants.KAPI_BASE_URL + "/v2/api/talk/memo/default/send";

        restClient.post()
                  .uri(url)
                  .header("Authorization",
                          "Bearer " + accessToken)
                  .body(requestBody.toMultiValueMap(objectMapper))
                  .retrieve()
                  .toBodilessEntity();
    }
}
