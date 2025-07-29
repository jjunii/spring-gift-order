package gift.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record KakaoMessageDto(
        @JsonProperty("object_type")
        String objectType, // "feed"로 고정
        Content content,
        @JsonProperty("item_content")
        ItemContent itemContent
) {

    public KakaoMessageDto(Content content, ItemContent itemContent) {
        this("feed", content, itemContent);
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