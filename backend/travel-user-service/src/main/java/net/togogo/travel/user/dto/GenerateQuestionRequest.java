package net.togogo.travel.user.dto;

import lombok.Data;
import jakarta.validation.constraints.*;


@Data
public class GenerateQuestionRequest {
    @NotBlank(message = "景点名称不能为空")
    private String scenicName;

    @NotNull(message = "题数不能为空")
    @Min(value = 5, message = "题数最少5题")
    @Max(value = 20, message = "题数最多20题")
    private Integer questionCount;

    @NotNull(message = "选项数不能为空")
    @Min(value = 2, message = "选项数最少2个")
    @Max(value = 4, message = "选项数最多4个")
    private Integer optionCount;

    @NotBlank(message = "难度不能为空")
    @Pattern(regexp = "简单|中等|困难|噩梦", message = "难度必须为简单/中等/困难/噩梦")
    private String difficulty;
}