package gift.service;

import gift.dto.KakaoMemberResponseDto;
import gift.dto.KakaoTokenResponseDto;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class KakaoAuthService {

    private final String kakaoRestApiKey;
    private final String redirectUrl;
    private final URI kakaoAuthUrl;
    private final RestClient restClient;

    public KakaoAuthService(
            @Value("${kakao.rest-api-key}") String kakaoRestApiKey,
            @Value("${kakao.redirect-url}") String redirectUrl
    ) {
        this.kakaoRestApiKey = kakaoRestApiKey;
        this.redirectUrl = redirectUrl;
        this.kakaoAuthUrl = UriComponentsBuilder.fromUriString("https://kauth.kakao.com")
                                                .path("/oauth/authorize")
                                                .queryParam("response_type", "code")
                                                .queryParam("client_id", kakaoRestApiKey)
                                                .queryParam("redirect_uri", redirectUrl)
                                                .build().toUri();
        this.restClient = RestClient.create();
    }

    public URI getKakaoAuthUrl() {
        return kakaoAuthUrl;
    }

    public KakaoTokenResponseDto getToken(String code) {
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
        return kakaoTokenResponseDto;
    }

    public KakaoMemberResponseDto getMemberInfo(String accessToken) {
        String url = "https://kapi.kakao.com/v2/user/me";

        return restClient.get()
                         .uri(url)
                         .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                         .retrieve()
                         .body(KakaoMemberResponseDto.class);
    }

    public KakaoTokenResponseDto refreshAccessToken(String refreshToken) {
        String url = "https://kauth.kakao.com/oauth/token";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "refresh_token");
        body.add("client_id", kakaoRestApiKey);
        body.add("refresh_token", refreshToken);

        return restClient.post()
                         .uri(url)
                         .body(body)
                         .retrieve()
                         .body(KakaoTokenResponseDto.class);
    }

}
