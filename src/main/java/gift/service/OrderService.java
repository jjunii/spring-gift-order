package gift.service;

import gift.dto.OrderRequestDto;
import gift.dto.OrderResponseDto;
import gift.entity.Member;
import gift.entity.Option;
import gift.entity.Order;
import gift.entity.SignupType;
import gift.exception.OptionNotFoundException;
import gift.exception.UnAuthenticationException;
import gift.repository.MemberRepository;
import gift.repository.OptionRepository;
import gift.repository.OrderRepository;
import gift.repository.WishRepository;
import gift.util.CurrentMemberContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final OptionRepository optionRepository;
    private final WishRepository wishRepository;
    private final KakaoMessageService kakaoMessageService;

    public OrderService(OrderRepository orderRepository, MemberRepository memberRepository,
            OptionRepository optionRepository, WishRepository wishRepository,
            KakaoMessageService kakaoMessageService) {
        this.orderRepository = orderRepository;
        this.memberRepository = memberRepository;
        this.optionRepository = optionRepository;
        this.wishRepository = wishRepository;
        this.kakaoMessageService = kakaoMessageService;
    }

    @Transactional
    public OrderResponseDto placeOrder(OrderRequestDto orderRequestDto) {
        Long memberId = CurrentMemberContext.getAuthenticatedMemberId();

        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new UnAuthenticationException("인증되지 않은 사용자입니다"));

        Option option = optionRepository.findById(orderRequestDto.optionId()).orElseThrow(
                () -> new OptionNotFoundException(orderRequestDto.optionId()));
        option.subtract(orderRequestDto.quantity());

        wishRepository.deleteByMemberIdAndProductId(member.getId(), option.getProduct().getId());

        Order savedOrder = orderRepository.save(
                new Order(member,
                        option,
                        orderRequestDto.quantity(),
                        orderRequestDto.message()
                ));

        if (savedOrder.getMember().getSignupType() == SignupType.KAKAO) {
            try {
                kakaoMessageService.sendOrderMessage(savedOrder);
            } catch (Exception e) {
                log.error("카카오 메시지 전송 실패. 주문 ID: {}", savedOrder.getId(), e);
            }
        }

        return OrderResponseDto.from(savedOrder);
    }
}
