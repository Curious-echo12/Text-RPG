package com.textrpg.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data @TableName("sys_user")
public class SysUser {
    @TableId(type = IdType.AUTO) private Long id;
    private String playerId;
    private String username;
    private String password;
    private String email;
    private Integer emailVerified;
    private Integer isAdmin;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updateTime;
}
