package com.textrpg.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data @TableName("pvp_record")
public class PvpRecord {
    @TableId(type = IdType.AUTO) private Long id;
    private Long attackerId;
    private Long defenderId;
    private String attackerName;
    private String defenderName;
    private String result;
    private Integer scoreChange;
    private String battleLog;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
}
