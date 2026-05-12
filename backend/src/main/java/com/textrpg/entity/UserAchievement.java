package com.textrpg.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data @TableName("user_achievement")
public class UserAchievement {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId;
    private String achieveKey;
    private Integer progress = 0, target = 1, isComplete = 0, isClaimed = 0;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
}
