package gift.service;

import gift.dto.KakaoMemberResponseDto;
import gift.dto.KakaoTokenResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class KakaoAuthService {

    @Value("${kakao.rest-api-key}")
    private String kakaoRestApiKey;

    @Value("${kakao.redirect-url}")
    private String redirectUrl;

    private final RestClient restClient = RestClient.create();

    public String getAccessToken(String code) {
        String url = "https://kauth.kakao.com/oauth/token";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("client_id", kakaoRestApiKey);
        body.add("redirect_uri", redirectUrl);
        body.add("code", code);

        KakaoTokenResponseDto kakaoTokenResponseDto = restClient.post()
                                                                .uri(url)
                                                                .contentType(
                                                                        MediaType.APPLICATION_FORM_URLENCODED)
                                                                .body(body)
                                                                .retrieve()
                                                                .body(KakaoTokenResponseDto.class);

        if (kakaoTokenResponseDto == null) {
            throw new RuntimeException("카카오 액세스 토큰 발급 실패");
        }
        return kakaoTokenResponseDto.accessToken();
    }

    public KakaoMemberResponseDto getMemberInfo(String accessToken) {
        String url = "https://kapi.kakao.com/v2/user/me";

        return restClient.get()
                         .uri(url)
                         .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                         .retrieve()
                         .body(KakaoMemberResponseDto.class);
    }

}
