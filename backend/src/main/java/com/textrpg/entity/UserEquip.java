package com.textrpg.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data @TableName("user_equip")
public class UserEquip {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId;
    private String itemKey;
    private String part;
    private String quality = "WHITE";
    private Integer strengthenLevel = 0;
    private String baseAttrJson;
    private String extraAttrJson;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updateTime;
}
