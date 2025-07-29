package gift.service;

import gift.dto.OrderRequestDto;
import gift.dto.OrderResponseDto;
import gift.entity.Member;
import gift.entity.Option;
import gift.entity.Order;
import gift.exception.OptionNotFoundException;
import gift.exception.UnAuthenticationException;
import gift.repository.MemberRepository;
import gift.repository.OptionRepository;
import gift.repository.OrderRepository;
import gift.repository.WishRepository;
import gift.util.CurrentMemberContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final OptionRepository optionRepository;
    private final WishRepository wishRepository;

    public OrderService(OrderRepository orderRepository, MemberRepository memberRepository,
            OptionRepository optionRepository, WishRepository wishRepository) {
        this.orderRepository = orderRepository;
        this.memberRepository = memberRepository;
        this.optionRepository = optionRepository;
        this.wishRepository = wishRepository;
    }

    @Transactional
    public OrderResponseDto placeOrder(OrderRequestDto orderRequestDto) {
        Long memberId = CurrentMemberContext.getAuthenticatedMemberId();

        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new UnAuthenticationException("인증되지 않은 사용자입니다"));

        Option option = optionRepository.findById(orderRequestDto.optionId()).orElseThrow(
                () -> new OptionNotFoundException(orderRequestDto.optionId()));
        option.subtract(orderRequestDto.quantity());

        Order order = new Order(member, option, orderRequestDto.quantity(),
                orderRequestDto.message());
        Order savedOrder = orderRepository.save(order);

        wishRepository.deleteByMemberIdAndProductId(member.getId(), option.getProduct().getId());

        return OrderResponseDto.from(savedOrder);
    }
}
