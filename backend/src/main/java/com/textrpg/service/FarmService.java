package com.textrpg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.textrpg.common.BusinessException;
import com.textrpg.common.GameConfig;
import com.textrpg.entity.*;
import com.textrpg.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FarmService {
    private final FarmPlotMapper plotMapper;
    private final GameRoleMapper roleMapper;
    private final RoleService roleService;
    private final BagService bagService;

    /** 根据种植等级计算最大菜地数量 */
    public static int getMaxPlots(int farmLevel) {
        if (farmLevel >= 40) return 9;
        if (farmLevel >= 30) return 8;
        if (farmLevel >= 20) return 7;
        if (farmLevel >= 15) return 6;
        if (farmLevel >= 10) return 5;
        if (farmLevel >= 5) return 4;
        return 3;
    }

    /** 获取下一等级解锁的菜地信息 */
    public static String getNextPlotUnlockDesc(int farmLevel) {
        if (farmLevel < 5) return "种植等级5级时解锁第4块菜地";
        if (farmLevel < 10) return "种植等级10级时解锁第5块菜地";
        if (farmLevel < 15) return "种植等级15级时解锁第6块菜地";
        if (farmLevel < 20) return "种植等级20级时解锁第7块菜地";
        if (farmLevel < 30) return "种植等级30级时解锁第8块菜地";
        if (farmLevel < 40) return "种植等级40级时解锁第9块菜地（最大）";
        return "已解锁全部9块菜地";
    }

    public List<Map<String, Object>> getPlots(Long userId) {
        GameRole role = roleService.getByUserId(userId);
        int maxPlots = getMaxPlots(role.getFarmLevel());

        // 自动解锁新菜地
        List<FarmPlot> existingPlots = plotMapper.selectList(
            new LambdaQueryWrapper<FarmPlot>().eq(FarmPlot::getUserId, userId).orderByAsc(FarmPlot::getPlotIndex));
        int currentCount = existingPlots.size();
        if (currentCount < maxPlots) {
            for (int i = currentCount; i < maxPlots; i++) {
                FarmPlot fp = new FarmPlot();
                fp.setUserId(userId); fp.setPlotIndex(i); fp.setStatus("EMPTY");
                plotMapper.insert(fp);
            }
            existingPlots = plotMapper.selectList(
                new LambdaQueryWrapper<FarmPlot>().eq(FarmPlot::getUserId, userId).orderByAsc(FarmPlot::getPlotIndex));
        }

        List<Map<String, Object>> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (FarmPlot p : existingPlots) {
            Map<String, Object> info = new HashMap<>();
            info.put("id", p.getId()); info.put("index", p.getPlotIndex());
            info.put("status", p.getStatus()); info.put("seedKey", p.getSeedKey());
            if ("GROWING".equals(p.getStatus()) && p.getHarvestTime() != null) {
                if (now.isAfter(p.getHarvestTime())) {
                    p.setStatus("READY"); plotMapper.updateById(p); info.put("status", "READY");
                } else {
                    long remainSec = java.time.Duration.between(now, p.getHarvestTime()).getSeconds();
                    info.put("remainSec", remainSec);
                }
            }
            // 显示作物信息
            if (p.getSeedKey() != null) {
                GameConfig.CropDef crop = GameConfig.CROPS.stream()
                    .filter(c -> c.cropKey.equals(p.getSeedKey())).findFirst().orElse(null);
                if (crop != null) {
                    info.put("cropName", crop.name);
                    info.put("growMinutes", crop.growMinutes);
                }
            }
            result.add(info);
        }
        return result;
    }

    /** 获取可种植作物列表和菜地信息 */
    public Map<String, Object> getCropList(Long userId) {
        GameRole role = roleService.getByUserId(userId);
        int maxPlots = getMaxPlots(role.getFarmLevel());
        List<Map<String, Object>> crops = new ArrayList<>();
        for (GameConfig.CropDef c : GameConfig.CROPS) {
            Map<String, Object> info = new HashMap<>();
            info.put("cropKey", c.cropKey);
            info.put("name", c.name);
            info.put("growMinutes", c.growMinutes);
            info.put("baseYield", c.baseYield);
            info.put("desc", c.desc);
            crops.add(info);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("crops", crops);
        result.put("maxPlots", maxPlots);
        result.put("farmLevel", role.getFarmLevel());
        result.put("nextUnlock", getNextPlotUnlockDesc(role.getFarmLevel()));
        result.put("farmBonus", role.getFarmLevel() / 3);
        return result;
    }

    /** 种植（无需种子，直接选择作物） */
    public Map<String, Object> plant(Long userId, int plotIndex, String cropKey) {
        FarmPlot plot = plotMapper.selectOne(new LambdaQueryWrapper<FarmPlot>()
            .eq(FarmPlot::getUserId, userId).eq(FarmPlot::getPlotIndex, plotIndex));
        if (plot == null) throw new BusinessException("菜地不存在");
        if (!"EMPTY".equals(plot.getStatus())) throw new BusinessException("菜地不是空闲状态");
        GameConfig.CropDef crop = GameConfig.CROPS.stream().filter(c -> c.cropKey.equals(cropKey)).findFirst()
            .orElseThrow(() -> new BusinessException("作物不存在"));
        plot.setSeedKey(cropKey); plot.setPlantTime(LocalDateTime.now());
        plot.setHarvestTime(LocalDateTime.now().plusMinutes(crop.growMinutes)); plot.setStatus("GROWING");
        plotMapper.updateById(plot);
        Map<String, Object> result = new HashMap<>();
        result.put("msg", "种植" + crop.name + "成功！预计" + crop.growMinutes + "分钟后成熟");
        return result;
    }

    public Map<String, Object> harvest(Long userId, int plotIndex) {
        FarmPlot plot = plotMapper.selectOne(new LambdaQueryWrapper<FarmPlot>()
            .eq(FarmPlot::getUserId, userId).eq(FarmPlot::getPlotIndex, plotIndex));
        if (plot == null) throw new BusinessException("菜地不存在");
        if (!"GROWING".equals(plot.getStatus()) && !"READY".equals(plot.getStatus()))
            throw new BusinessException("没有可收获的作物");
        if (LocalDateTime.now().isBefore(plot.getHarvestTime())) throw new BusinessException("作物尚未成熟");
        GameConfig.CropDef crop = GameConfig.CROPS.stream().filter(c -> c.cropKey.equals(plot.getSeedKey())).findFirst().orElse(null);
        if (crop == null) throw new BusinessException("作物配置异常");
        int yield = crop.baseYield;
        GameRole role = roleService.getByUserId(userId);
        int bonus = role.getFarmLevel() / 3;
        yield += bonus;
        bagService.addItem(userId, crop.cropKey, yield);
        role.setFarmExp(role.getFarmExp() + 10);
        if (role.getFarmExp() >= role.getFarmLevel() * 100) {
            role.setFarmExp(role.getFarmExp() - role.getFarmLevel() * 100);
            role.setFarmLevel(role.getFarmLevel() + 1);
        }
        roleMapper.updateById(role);
        plot.setStatus("EMPTY"); plot.setSeedKey(null); plot.setPlantTime(null); plot.setHarvestTime(null);
        plotMapper.updateById(plot);
        Map<String, Object> result = new HashMap<>();
        GameConfig.ItemDef cropItem = GameConfig.ITEMS.get(crop.cropKey);
        result.put("msg", "收获成功！获得 " + (cropItem != null ? cropItem.name : crop.cropKey) + " x" + yield
                + (bonus > 0 ? "（种植等级加成: +" + bonus + "）" : ""));
        result.put("role", role);
        return result;
    }
}
