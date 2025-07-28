package gift.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrderRequestDto(
        @NotNull(message = "옵션 ID는 필수입니다.")
        Long optionId,

        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
        Integer quantity,

        @Size(max = 500, message = "메세지는 최대 500자까지 입력할 수 있습니다.")
        String message
) {

}