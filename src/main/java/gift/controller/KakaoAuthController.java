package gift.controller;

import gift.dto.KakaoMemberResponseDto;
import gift.dto.KakaoTokenResponseDto;
import gift.service.KakaoAuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;

    public KakaoAuthController(KakaoAuthService kakaoAuthService) {
        this.kakaoAuthService = kakaoAuthService;
    }

    @GetMapping("/auth/kakao")
    public ResponseEntity<Void> redirectToKakao() {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(kakaoAuthService.getKakaoAuthUrl());

        return ResponseEntity
                .status(HttpStatus.FOUND)
                .headers(headers)
                .build();
    }

    @GetMapping("/auth/kakao/callback")
    public ResponseEntity<KakaoMemberResponseDto> kakaoLogin(@RequestParam("code") String code) {
        KakaoTokenResponseDto kakaoTokenResponseDto = kakaoAuthService.getToken(code);

        return ResponseEntity.ok(
                kakaoAuthService.getMemberInfo(kakaoTokenResponseDto.accessToken()));
    }
}
