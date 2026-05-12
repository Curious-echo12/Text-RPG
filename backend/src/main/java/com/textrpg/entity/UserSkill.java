package com.textrpg.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data @TableName("user_skill")
public class UserSkill {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId;
    private String skillKey;
    private Integer level = 1;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
}
