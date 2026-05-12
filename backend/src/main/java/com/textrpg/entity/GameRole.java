package com.textrpg.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
@Data @TableName("game_role")
public class GameRole {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId;
    private String name;
    private String job;
    private Integer level = 1;
    private Long exp = 0L;
    private Integer hp = 100, maxHp = 100, attack = 10, defense = 5, speed = 5;
    private Double critRate = 0.05, critDmg = 1.5;
    private Long fightPower = 0L;
    private Integer rebornCount = 0;
    private Long gold = 100L;
    private Integer diamond = 10;
    private Integer energy = 100, maxEnergy = 100, spirit = 50, maxSpirit = 50;
    private Integer mineLevel = 1, mineExp = 0, farmLevel = 1, farmExp = 0, forgeLevel = 1, forgeExp = 0;
    private Integer pvpScore = 0, pvpStar = 0, skillPoint = 0;
    private LocalDateTime lastEnergyTime, lastSpiritTime;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updateTime;
}
