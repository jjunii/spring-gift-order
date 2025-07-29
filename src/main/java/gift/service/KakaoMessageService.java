package gift.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gift.dto.KakaoMessageDto;
import gift.dto.KakaoMessageDto.Content;
import gift.entity.Member;
import gift.entity.Order;
import gift.exception.UnAuthenticationException;
import gift.repository.MemberRepository;
import gift.util.CurrentMemberContext;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.http.HttpHeaders;
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

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, member.getKakaoAccessToken());

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("template_object", createMessage(order));

        restClient.post()
                  .uri(url)
                  .header("Authorization",
                          "Bearer " + member.getKakaoAccessToken())
                  .body(body)
                  .retrieve()
                  .toBodilessEntity();
    }

    private String createMessage(Order order) {
        KakaoMessageDto.Link link = new KakaoMessageDto.Link("http://localhost:8080",
                "http://localhost:8080");
        KakaoMessageDto.Content content = new Content(
                "주문이 완료되었습니다!",
                "주문 내역을 확인하세요.",
                order.getOption().getProduct().getImageUrl(),
                link
        );

        List<KakaoMessageDto.Item> items = List.of(
                new KakaoMessageDto.Item(
                        order.getOption().getProduct().getName(),
                        order.getOption().getName() + " / " + order.getQuantity() + "개"
                ),
                new KakaoMessageDto.Item(
                        "주문 시간",
                        DateTimeFormatter
                                .ofPattern("yyyy-MM-dd HH:mm:ss")
                                .format(order.getOrderDateTime())
                ),
                new KakaoMessageDto.Item(
                        "메세지",
                        order.getMessage()
                )
        );

        KakaoMessageDto.ItemContent itemContent = new KakaoMessageDto.ItemContent(
                items
        );

        KakaoMessageDto kakaoMessageDto = new KakaoMessageDto(content, itemContent);

        try {
            return objectMapper.writeValueAsString(kakaoMessageDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("카카오 메세지 템플릿 생성에 실패했습니다.");
        }
    }
}
