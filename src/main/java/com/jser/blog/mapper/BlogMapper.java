package com.jser.blog.mapper;

import com.jser.blog.entities.Blog;
import com.jser.blog.entities.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BlogMapper {

    @Select("SELECT * FROM blog where userId=#{userId}")
    List<Blog> getBlogList(@Param("userId") int userId);

    @Select("SELECT * FROM blog")
    List<Blog> getAllBlogList();

    @Select("SELECT count(*) FROM blog where userId=#{userId};")
    int getBlogListTotal(@Param("userId") int userId, @Param("start") int start, @Param("end") int end);

    @Select("SELECT * FROM blog where id=#{id}")
    Blog getBlog(@Param("id") int id);

    @Insert("INSERT INTO blog(userId, title, content, description) VALUES(#{userId}, #{title}, #{content}, #{description})")
    @Options(useGeneratedKeys=true, keyColumn="id")
    int addBlog(@Param("userId") int userId, @Param("title") String title, @Param("content") String content, @Param("description") String description);

    @Insert("UPDATE blog SET title=#{title}, content=#{content}, description=#{description} WHERE id=#{id}")
    int updateBlog(
            @Param("title") String title,
            @Param("content") String content,
            @Param("description") String description,
            @Param("atIndex") boolean atIndex,
            @Param("id") int id);

    @Delete("delete from blog where id=#{id}")
    int deleteBlog(@Param("id") int id);
}
