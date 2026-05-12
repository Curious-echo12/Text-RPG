package com.textrpg.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data @TableName("mail")
public class Mail {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId;
    private String title;
    private String content;
    private String itemJson;
    private Integer isRead = 0;
    private Integer isClaimed = 0;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
}
