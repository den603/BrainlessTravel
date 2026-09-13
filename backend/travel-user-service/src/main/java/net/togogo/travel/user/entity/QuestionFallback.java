package net.togogo.travel.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("question_fallback")
public class QuestionFallback {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String scenicName;
    private String difficulty;
    private Integer questionCount;
    private Integer optionCount;
    private String questions; // JSON数组字符串
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDelete;
}