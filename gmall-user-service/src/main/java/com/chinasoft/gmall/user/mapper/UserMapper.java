package com.chinasoft.gmall.user.mapper;

import com.chinasoft.gmall.entity.UmsMember;
import com.chinasoft.gmall.entity.User;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UserMapper extends Mapper<UmsMember> {
    List<User> selectAllUser();

    User selectUserById(int id);
}
