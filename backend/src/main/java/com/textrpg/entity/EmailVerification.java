package com.textrpg.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data @TableName("email_verification")
public class EmailVerification {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId;
    private String email;
    private String code;
    private String purpose;
    private Integer used;
    private LocalDateTime expireTime;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
}
