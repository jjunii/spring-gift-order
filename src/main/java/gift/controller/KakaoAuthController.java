package gift.controller;

import gift.dto.KakaoMemberResponseDto;
import gift.dto.KakaoTokenResponseDto;
import gift.dto.TokenResponseDto;
import gift.service.KakaoAuthService;
import gift.service.MemberService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KakaoAuthController {

    private final KakaoAuthService kakaoAuthService;
    private final MemberService memberService;

    public KakaoAuthController(KakaoAuthService kakaoAuthService, MemberService memberService) {
        this.kakaoAuthService = kakaoAuthService;
        this.memberService = memberService;
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
    public ResponseEntity<TokenResponseDto> kakaoLogin(@RequestParam("code") String code) {
        KakaoTokenResponseDto kakaoTokenResponseDto = kakaoAuthService.getToken(code);
        KakaoMemberResponseDto kakaoMemberDto = kakaoAuthService.getMemberInfo(
                kakaoTokenResponseDto.accessToken());

        return ResponseEntity.ok(
                memberService.kakaoLogin(
                        kakaoMemberDto.kakaoAccount().email(),
                        kakaoTokenResponseDto.accessToken(),
                        kakaoTokenResponseDto.refreshToken()
                ));
    }
}
