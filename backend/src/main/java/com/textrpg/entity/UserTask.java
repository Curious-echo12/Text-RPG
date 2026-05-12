package com.textrpg.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data @TableName("user_task")
public class UserTask {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId;
    private String taskKey;
    private Integer progress = 0, target = 1, isComplete = 0, isClaimed = 0;
    private LocalDate taskDate;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
}
