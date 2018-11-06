package com.ktnw.mapper;

import com.ktnw.entity.User;
import org.apache.ibatis.annotations.Insert;

public interface UserMapper {

    @Insert("insert into t_user(user_id,name,mobile,address,create_time)" +
            "values(#{userId},#{name},#{mobile},#{address},now())")
    boolean createUser(User user);
}
