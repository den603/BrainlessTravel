package net.togogo.springboot_travel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;
//题库实体类
@Data
@TableName("question")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String scenicName;
    private String difficulty;
    private String content;
    private String options; // JSON数组字符串
    private String correctAnswer;
    private String analysis;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDelete;
}