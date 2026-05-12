package com.textrpg.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data @TableName("farm_plot")
public class FarmPlot {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId;
    private Integer plotIndex;
    private String seedKey;
    private LocalDateTime plantTime;
    private LocalDateTime harvestTime;
    private String status = "EMPTY";
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updateTime;
}
