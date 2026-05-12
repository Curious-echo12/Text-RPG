package com.textrpg.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.textrpg.entity.AdminUser;
import com.textrpg.mapper.AdminUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final AdminUserMapper adminMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // 确保默认管理员账号存在且密码正确
        AdminUser admin = adminMapper.selectOne(
            new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, "admin"));
        String encodedPassword = passwordEncoder.encode("admin123456");
        if (admin == null) {
            admin = new AdminUser();
            admin.setUsername("admin");
            admin.setPassword(encodedPassword);
            adminMapper.insert(admin);
            log.info("默认管理员账号已创建: admin / admin123456");
        } else {
            // 每次启动重置密码，确保可用
            admin.setPassword(encodedPassword);
            adminMapper.updateById(admin);
            log.info("管理员密码已重置: admin / admin123456");
        }
    }
}
