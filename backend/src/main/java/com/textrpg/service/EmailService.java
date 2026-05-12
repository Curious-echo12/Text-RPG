package com.textrpg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.textrpg.common.BusinessException;
import com.textrpg.entity.EmailVerification;
import com.textrpg.entity.SysUser;
import com.textrpg.mapper.EmailVerificationMapper;
import com.textrpg.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final EmailVerificationMapper verificationMapper;
    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    /** 发送邮箱验证码 */
    public Map<String, Object> sendVerificationCode(Long userId, String email, String purpose) {
        if (!isValidEmail(email)) throw new BusinessException("邮箱格式不正确");

        // 1分钟内不能重复发送
        EmailVerification recent = verificationMapper.selectOne(
            new LambdaQueryWrapper<EmailVerification>()
                .eq(EmailVerification::getUserId, userId)
                .eq(EmailVerification::getPurpose, purpose)
                .eq(EmailVerification::getUsed, 0)
                .gt(EmailVerification::getExpireTime, LocalDateTime.now())
                .orderByDesc(EmailVerification::getCreateTime)
                .last("LIMIT 1"));
        if (recent != null) {
            long secondsLeft = java.time.Duration.between(LocalDateTime.now(), recent.getExpireTime()).getSeconds();
            if (secondsLeft > 240) throw new BusinessException("验证码已发送，请1分钟后再试");
        }

        String code = String.format("%06d", new Random().nextInt(1000000));

        EmailVerification ev = new EmailVerification();
        ev.setUserId(userId);
        ev.setEmail(email);
        ev.setCode(code);
        ev.setPurpose(purpose);
        ev.setUsed(0);
        ev.setExpireTime(LocalDateTime.now().plusMinutes(5));
        verificationMapper.insert(ev);

        String purposeText;
        switch (purpose) {
            case "BIND": purposeText = "绑定邮箱"; break;
            case "CHANGE": purposeText = "更换绑定邮箱"; break;
            case "RESET": purposeText = "重置密码"; break;
            default: purposeText = "验证";
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(mailFrom);
            message.setTo(email);
            message.setSubject("【文字江湖】" + purposeText + "验证码");
            message.setText("尊敬的玩家您好：\n\n"
                + "您正在进行" + purposeText + "操作，验证码为：" + code + "\n\n"
                + "验证码有效期为5分钟，请勿泄露给他人。\n"
                + "如非本人操作，请忽略此邮件。\n\n"
                + "—— 文字江湖");
            mailSender.send(message);
            log.info("验证码邮件已发送至 {}", email);
        } catch (Exception e) {
            log.error("发送邮件失败: {}", e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("msg", "验证码已发送至 " + email + "，有效期5分钟");
        return result;
    }

    /** 验证邮箱验证码 */
    public boolean verifyCode(Long userId, String email, String code, String purpose) {
        EmailVerification ev = verificationMapper.selectOne(
            new LambdaQueryWrapper<EmailVerification>()
                .eq(EmailVerification::getUserId, userId)
                .eq(EmailVerification::getEmail, email)
                .eq(EmailVerification::getCode, code)
                .eq(EmailVerification::getPurpose, purpose)
                .eq(EmailVerification::getUsed, 0)
                .gt(EmailVerification::getExpireTime, LocalDateTime.now())
                .orderByDesc(EmailVerification::getCreateTime)
                .last("LIMIT 1"));
        if (ev == null) return false;
        ev.setUsed(1);
        verificationMapper.updateById(ev);
        return true;
    }

    /** 绑定邮箱 */
    public Map<String, Object> bindEmail(Long userId, String email, String code) {
        if (!verifyCode(userId, email, code, "BIND")) throw new BusinessException("验证码无效或已过期");
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        SysUser existing = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, email).ne(SysUser::getId, userId));
        if (existing != null) throw new BusinessException("该邮箱已被其他账号绑定");

        user.setEmail(email);
        user.setEmailVerified(1);
        userMapper.updateById(user);

        Map<String, Object> result = new HashMap<>();
        result.put("msg", "邮箱绑定成功");
        result.put("email", email);
        return result;
    }

    /** 更换绑定邮箱 */
    public Map<String, Object> changeEmail(Long userId, String newEmail, String code) {
        if (!verifyCode(userId, newEmail, code, "CHANGE")) throw new BusinessException("验证码无效或已过期");
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        SysUser existing = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getEmail, newEmail).ne(SysUser::getId, userId));
        if (existing != null) throw new BusinessException("该邮箱已被其他账号绑定");

        user.setEmail(newEmail);
        user.setEmailVerified(1);
        userMapper.updateById(user);

        Map<String, Object> result = new HashMap<>();
        result.put("msg", "邮箱更换成功");
        result.put("email", newEmail);
        return result;
    }

    /** 发送重置密码验证码（通过用户名/玩家ID查找） */
    public Map<String, Object> sendResetCode(String account) {
        SysUser user = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, account)
                .or()
                .eq(SysUser::getPlayerId, account));
        if (user == null) throw new BusinessException("账号不存在");
        if (user.getEmail() == null || user.getEmailVerified() == null || user.getEmailVerified() != 1) {
            throw new BusinessException("该账号未绑定邮箱，无法重置密码");
        }
        return sendVerificationCode(user.getId(), user.getEmail(), "RESET");
    }

    /** 通过邮箱验证码重置密码 */
    public void resetPasswordByEmail(String account, String code, String newPassword) {
        SysUser user = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, account)
                .or()
                .eq(SysUser::getPlayerId, account));
        if (user == null) throw new BusinessException("账号不存在");
        if (user.getEmail() == null || user.getEmailVerified() == null || user.getEmailVerified() != 1) {
            throw new BusinessException("该账号未绑定邮箱");
        }

        EmailVerification ev = verificationMapper.selectOne(
            new LambdaQueryWrapper<EmailVerification>()
                .eq(EmailVerification::getUserId, user.getId())
                .eq(EmailVerification::getEmail, user.getEmail())
                .eq(EmailVerification::getCode, code)
                .eq(EmailVerification::getPurpose, "RESET")
                .eq(EmailVerification::getUsed, 0)
                .gt(EmailVerification::getExpireTime, LocalDateTime.now())
                .orderByDesc(EmailVerification::getCreateTime)
                .last("LIMIT 1"));
        if (ev == null) throw new BusinessException("验证码无效或已过期");
        ev.setUsed(1);
        verificationMapper.updateById(ev);

        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    /** 已登录用户通过旧密码重置密码 */
    public void resetPasswordByOldPassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("新密码长度至少6位");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }
}
