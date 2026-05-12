package com.textrpg.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.textrpg.common.BusinessException;
import com.textrpg.config.security.JwtUtil;
import com.textrpg.entity.SysUser;
import com.textrpg.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public Map<String, Object> register(String username, String password) {
        // 用户名校验：至少3个字符或汉字
        if (username == null || username.trim().length() < 3) {
            throw new BusinessException("用户名至少需要3个字符");
        }
        if (username.length() > 20) {
            throw new BusinessException("用户名不能超过20个字符");
        }
        // 密码校验：至少6位数字
        if (password == null || !password.matches(".*\\d{6,}.*")) {
            throw new BusinessException("密码至少需要包含6位数字");
        }
        if (password.length() < 6) {
            throw new BusinessException("密码长度至少6位");
        }

        if (userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)) != null) {
            throw new BusinessException("用户名已存在");
        }

        // 生成唯一6位玩家ID
        String playerId = generateUniquePlayerId();

        SysUser user = new SysUser();
        user.setPlayerId(playerId);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmailVerified(0);
        user.setIsAdmin(0);
        userMapper.insert(user);

        Map<String, Object> result = new HashMap<>();
        result.put("msg", "注册成功");
        result.put("playerId", playerId);
        return result;
    }

    public Map<String, Object> login(String account, String password) {
        // 支持用户名或玩家ID登录
        SysUser user = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, account)
                .or()
                .eq(SysUser::getPlayerId, account));
        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("playerId", user.getPlayerId());
        result.put("email", user.getEmail());
        return result;
    }

    /** 生成唯一6位数字玩家ID */
    private String generateUniquePlayerId() {
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            String id = String.format("%06d", random.nextInt(900000) + 100000);
            Long count = userMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getPlayerId, id));
            if (count == 0) return id;
        }
        throw new BusinessException("生成玩家ID失败，请重试");
    }
}
