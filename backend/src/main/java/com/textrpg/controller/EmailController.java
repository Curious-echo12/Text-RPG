package com.textrpg.controller;

import com.textrpg.common.Result;
import com.textrpg.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {
    private final EmailService emailService;

    /** 发送绑定邮箱验证码 */
    @PostMapping("/sendBindCode")
    public Result<?> sendBindCode(Authentication auth, @RequestBody Map<String, String> body) {
        return Result.ok(emailService.sendVerificationCode(
            (Long) auth.getPrincipal(), body.get("email"), "BIND"));
    }

    /** 绑定邮箱 */
    @PostMapping("/bind")
    public Result<?> bind(Authentication auth, @RequestBody Map<String, String> body) {
        return Result.ok(emailService.bindEmail(
            (Long) auth.getPrincipal(), body.get("email"), body.get("code")));
    }

    /** 发送更换邮箱验证码 */
    @PostMapping("/sendChangeCode")
    public Result<?> sendChangeCode(Authentication auth, @RequestBody Map<String, String> body) {
        return Result.ok(emailService.sendVerificationCode(
            (Long) auth.getPrincipal(), body.get("email"), "CHANGE"));
    }

    /** 更换绑定邮箱 */
    @PostMapping("/change")
    public Result<?> changeEmail(Authentication auth, @RequestBody Map<String, String> body) {
        return Result.ok(emailService.changeEmail(
            (Long) auth.getPrincipal(), body.get("email"), body.get("code")));
    }

    /** 发送重置密码验证码（公开接口，通过用户名/玩家ID） */
    @PostMapping("/sendResetCode")
    public Result<?> sendResetCode(@RequestBody Map<String, String> body) {
        return Result.ok(emailService.sendResetCode(body.get("account")));
    }

    /** 通过邮箱验证码重置密码（公开接口） */
    @PostMapping("/resetPassword")
    public Result<?> resetPassword(@RequestBody Map<String, String> body) {
        emailService.resetPasswordByEmail(
            body.get("account"), body.get("code"), body.get("newPassword"));
        return Result.ok("密码重置成功");
    }

    /** 已登录用户通过旧密码重置密码 */
    @PostMapping("/changePassword")
    public Result<?> changePassword(Authentication auth, @RequestBody Map<String, String> body) {
        emailService.resetPasswordByOldPassword(
            (Long) auth.getPrincipal(), body.get("oldPassword"), body.get("newPassword"));
        return Result.ok("密码修改成功");
    }
}
