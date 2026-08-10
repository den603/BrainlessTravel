package net.togogo.springboot_travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_answer_detail")
public class UserAnswerDetail {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long recordId;
    private Long questionId;
    private String userAnswer;
    private Integer isCorrect;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDelete;
}