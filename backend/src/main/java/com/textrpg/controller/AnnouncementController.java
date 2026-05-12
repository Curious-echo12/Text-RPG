package com.textrpg.controller;

import com.textrpg.common.Result;
import com.textrpg.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/announcement")
@RequiredArgsConstructor
public class AnnouncementController {
    private final AnnouncementService announcementService;

    /** 公开接口：获取有效公告 */
    @GetMapping("/list")
    public Result<?> list() {
        return Result.ok(announcementService.getActiveAnnouncements());
    }

    /** 管理接口：获取所有公告 */
    @GetMapping("/all")
    public Result<?> all() {
        return Result.ok(announcementService.getAll());
    }

    /** 管理接口：创建公告 */
    @PostMapping("/create")
    public Result<?> create(@RequestBody Map<String, String> body) {
        announcementService.create(body.get("title"), body.get("content"));
        return Result.ok("公告发布成功");
    }

    /** 管理接口：更新公告 */
    @PostMapping("/update")
    public Result<?> update(@RequestBody Map<String, Object> body) {
        Long id = Long.parseLong(body.get("id").toString());
        String title = (String) body.get("title");
        String content = (String) body.get("content");
        Integer isActive = body.containsKey("isActive") ? Integer.parseInt(body.get("isActive").toString()) : null;
        announcementService.update(id, title, content, isActive);
        return Result.ok("更新成功");
    }

    /** 管理接口：删除公告 */
    @PostMapping("/delete")
    public Result<?> delete(@RequestBody Map<String, Long> body) {
        announcementService.delete(body.get("id"));
        return Result.ok("删除成功");
    }

    /** 管理接口：切换公告状态 */
    @PostMapping("/toggle")
    public Result<?> toggle(@RequestBody Map<String, Long> body) {
        announcementService.toggleActive(body.get("id"));
        return Result.ok("状态已切换");
    }
}
