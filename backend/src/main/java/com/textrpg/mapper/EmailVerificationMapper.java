package com.textrpg.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.textrpg.entity.EmailVerification;
import org.apache.ibatis.annotations.Mapper;
@Mapper
public interface EmailVerificationMapper extends BaseMapper<EmailVerification> {
}
