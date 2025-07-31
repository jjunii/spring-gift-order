package gift.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import gift.entity.Order;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record KakaoOrderMessageDto(
        @JsonProperty("object_type")
        String objectType, // "feed"로 고정
        Content content,
        @JsonProperty("item_content")
        ItemContent itemContent
) {

    public KakaoOrderMessageDto(Content content, ItemContent itemContent) {
        this("feed", content, itemContent);
    }

    public static KakaoOrderMessageDto from(Order order) {
        Link link = new Link("http://localhost:8080", "http://localhost:8080");
        Content content = new Content(
                "주문이 완료되었습니다!",
                "주문 내역을 확인하세요.",
                order.getOption().getProduct().getImageUrl(),
                link
        );

        List<Item> items = List.of(
                new Item(
                        order.getOption().getProduct().getName(),
                        order.getOption().getName() + " / " + order.getQuantity() + "개"
                ),
                new Item(
                        "주문 시간",
                        DateTimeFormatter
                                .ofPattern("yyyy-MM-dd HH:mm:ss")
                                .format(order.getOrderDateTime())
                ),
                new Item(
                        "메세지",
                        order.getMessage()
                )
        );

        ItemContent itemContent = new ItemContent(items);

        return new KakaoOrderMessageDto(content, itemContent);
    }

    public record Content(
            String title,
            String description,
            @JsonProperty("image_url")
            String imageUrl,
            Link link
    ) {

    }

    public record Link(
            @JsonProperty("web_url")
            String webUrl,
            @JsonProperty("mobile_web_url")
            String mobileWebUrl
    ) {

    }

    public record ItemContent(
            List<Item> items
    ) {

    }

    public record Item(
            String item,
            @JsonProperty("item_op")
            String itemOp
    ) {

    }
}