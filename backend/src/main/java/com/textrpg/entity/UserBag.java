package com.textrpg.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data @TableName("user_bag")
public class UserBag {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId;
    private String itemKey;
    private Integer count = 1;
    private Integer isBind = 0;
    private String extraJson;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updateTime;
}
