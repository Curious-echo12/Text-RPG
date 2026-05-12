package com.textrpg.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data @TableName("user_sign")
public class UserSign {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId;
    private LocalDate signDate;
    private Integer consecutiveDays = 1;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
}
