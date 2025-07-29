package gift.controller;

import gift.dto.OrderRequestDto;
import gift.dto.OrderResponseDto;
import gift.entity.Order;
import gift.entity.SignupType;
import gift.service.KakaoMessageService;
import gift.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;
    private final KakaoMessageService kakaoMessageService;

    public OrderController(OrderService orderService, KakaoMessageService kakaoMessageService) {
        this.orderService = orderService;
        this.kakaoMessageService = kakaoMessageService;
    }

    // 주문하기
    @PostMapping
    public ResponseEntity<OrderResponseDto> placeOrder(
            @Valid @RequestBody OrderRequestDto orderRequestDto) {

        Order savedOrder = orderService.placeOrder(orderRequestDto);

        if (savedOrder.getMember().getSignupType() == SignupType.KAKAO) {
            try {
                kakaoMessageService.sendOrderMessage(savedOrder);
            } catch (Exception e) {
                log.error("카카오 메시지 전송 실패. 주문 ID: {}", savedOrder.getId(), e);
            }
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(OrderResponseDto.from(savedOrder));
    }
}
