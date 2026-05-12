package com.textrpg.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data @TableName("game_config")
public class GameConfigEntity {
    @TableId(type = IdType.AUTO) private Long id;
    private String configKey;
    private String configJson;
    private String description;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updateTime;
}
