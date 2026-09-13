package net.togogo.travel.user.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import java.util.List;

@Data
public class SubmitAnswerRequest {
    @NotBlank(message = "景点名称不能为空")
    private String scenicName;

    @NotEmpty(message = "答案列表不能为空")
    private List<AnswerItem> answers;

    @Data
    public static class AnswerItem {
        @NotNull(message = "题目ID不能为空")
        private Long questionId;
        @NotBlank(message = "用户答案不能为空")
        private String userAnswer;
    }
}