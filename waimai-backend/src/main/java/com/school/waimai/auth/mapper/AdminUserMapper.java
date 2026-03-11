package com.school.waimai.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.school.waimai.auth.entity.AdminUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;

@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {
    AdminUser selectByUsernameNotDeleted(@Param("username") String username);

    int updateLastLoginTime(@Param("id") Long id, @Param("lastLoginTime") LocalDateTime lastLoginTime);
}
