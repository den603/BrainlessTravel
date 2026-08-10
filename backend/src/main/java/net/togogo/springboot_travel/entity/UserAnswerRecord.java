package net.togogo.springboot_travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_answer_record")
public class UserAnswerRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String scenicName;
    private Integer totalQuestions;
    private Integer score;
    private String evaluateName;
    private String evaluateDesc;
    private LocalDateTime answerTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDelete;
}