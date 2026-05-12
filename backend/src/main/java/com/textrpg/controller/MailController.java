package com.textrpg.controller;
import com.textrpg.common.Result;
import com.textrpg.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController @RequestMapping("/api/mail") @RequiredArgsConstructor
public class MailController {
    private final MailService mailService;

    @GetMapping("/list")
    public Result<?> list(Authentication auth) { return Result.ok(mailService.getMails((Long) auth.getPrincipal())); }

    @PostMapping("/read")
    public Result<?> read(Authentication auth, @RequestBody Map<String, Long> body) {
        return Result.ok(mailService.readMail((Long) auth.getPrincipal(), body.get("mailId")));
    }

    @PostMapping("/claim")
    public Result<?> claim(Authentication auth, @RequestBody Map<String, Long> body) {
        return Result.ok(mailService.claimMail((Long) auth.getPrincipal(), body.get("mailId")));
    }

    @PostMapping("/delete")
    public Result<?> delete(Authentication auth, @RequestBody Map<String, Long> body) {
        return Result.ok(mailService.deleteMail((Long) auth.getPrincipal(), body.get("mailId")));
    }

    @GetMapping("/hasUnread")
    public Result<?> hasUnread(Authentication auth) {
        Map<String, Object> result = new HashMap<>();
        result.put("hasUnread", mailService.hasUnreadMail((Long) auth.getPrincipal()));
        return Result.ok(result);
    }
}
