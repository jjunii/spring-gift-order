package gift.controller;

import gift.dto.KakaoMemberResponseDto;
import gift.service.KakaoAuthService;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;

    @Value("${kakao.rest-api-key}")
    private String kakaoRestApiKey;

    @Value("${kakao.redirect-url}")
    private String redirectUrl;

    public KakaoAuthController(KakaoAuthService kakaoAuthService) {
        this.kakaoAuthService = kakaoAuthService;
    }

    @GetMapping("/auth/kakao")
    public ResponseEntity<Void> redirectToKakao() {
        URI uri = UriComponentsBuilder.fromUriString("https://kauth.kakao.com")
                                      .path("/oauth/authorize")
                                      .queryParam("response_type", "code")
                                      .queryParam("client_id", kakaoRestApiKey)
                                      .queryParam("redirect_uri", redirectUrl)
                                      .build().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uri);

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .headers(headers)
                .build();
    }

    @GetMapping("/auth/kakao/callback")
    public ResponseEntity<KakaoMemberResponseDto> kakaoLogin(@RequestParam("code") String code) {

        String accessToken = kakaoAuthService.getAccessToken(code);

        return ResponseEntity.ok(kakaoAuthService.getMemberInfo(accessToken));
    }
}
