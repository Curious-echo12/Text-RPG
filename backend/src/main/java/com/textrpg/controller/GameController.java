package com.textrpg.controller;

import com.textrpg.common.Result;
import com.textrpg.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController {
    private final AnnouncementService announcementService;

    @GetMapping("/announcements")
    public Result<?> announcements() {
        Map<String, Object> data = new HashMap<>();
        data.put("list", announcementService.getActiveAnnouncements());
        return Result.ok(data);
    }
}
