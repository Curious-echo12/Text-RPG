package com.textrpg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.textrpg.common.BusinessException;
import com.textrpg.entity.Announcement;
import com.textrpg.mapper.AnnouncementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AnnouncementService {
    private final AnnouncementMapper announcementMapper;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public List<Map<String, Object>> getActiveAnnouncements() {
        List<Announcement> list = announcementMapper.selectList(
            new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getIsActive, 1)
                .orderByDesc(Announcement::getCreateTime));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Announcement a : list) {
            Map<String, Object> info = new HashMap<>();
            info.put("id", a.getId());
            info.put("title", a.getTitle());
            info.put("content", a.getContent());
            info.put("time", a.getCreateTime() != null ? a.getCreateTime().format(FMT) : "");
            result.add(info);
        }
        return result;
    }

    public List<Map<String, Object>> getAll() {
        List<Announcement> list = announcementMapper.selectList(
            new LambdaQueryWrapper<Announcement>().orderByDesc(Announcement::getCreateTime));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Announcement a : list) {
            Map<String, Object> info = new HashMap<>();
            info.put("id", a.getId());
            info.put("title", a.getTitle());
            info.put("content", a.getContent());
            info.put("isActive", a.getIsActive());
            info.put("createTime", a.getCreateTime() != null ? a.getCreateTime().format(FMT) : "");
            result.add(info);
        }
        return result;
    }

    public void create(String title, String content) {
        if (title == null || title.trim().isEmpty()) throw new BusinessException("标题不能为空");
        if (content == null || content.trim().isEmpty()) throw new BusinessException("内容不能为空");
        Announcement a = new Announcement();
        a.setTitle(title.trim());
        a.setContent(content.trim());
        a.setIsActive(1);
        announcementMapper.insert(a);
    }

    public void update(Long id, String title, String content, Integer isActive) {
        Announcement a = announcementMapper.selectById(id);
        if (a == null) throw new BusinessException("公告不存在");
        if (title != null && !title.trim().isEmpty()) a.setTitle(title.trim());
        if (content != null && !content.trim().isEmpty()) a.setContent(content.trim());
        if (isActive != null) a.setIsActive(isActive);
        announcementMapper.updateById(a);
    }

    public void delete(Long id) {
        announcementMapper.deleteById(id);
    }

    public void toggleActive(Long id) {
        Announcement a = announcementMapper.selectById(id);
        if (a == null) throw new BusinessException("公告不存在");
        a.setIsActive(a.getIsActive() == 1 ? 0 : 1);
        announcementMapper.updateById(a);
    }
}
