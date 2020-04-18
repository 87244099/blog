package com.jser.blog.mapper;

import com.jser.blog.entities.User;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface UserMapper {
    @Select("SELECT * FROM `user` WHERE username = #{username}")
    User getUserByUsername(@Param("username") String username);

    @Select("SELECT * FROM `user` where id = #{id}")
    User getUserById(@Param("id") int id);

    @Update("UPDATE `user` SET password = #{password} WHERE username = #{username} ")
    void save(@Param("username") String username, @Param("password") String password);

    @Insert("INSERT INTO `user`(username, password) VALUES(#{username}, #{password})")
    int addUser(@Param("username")String username, @Param("password") String password);

}
