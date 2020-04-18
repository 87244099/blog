package com.jser.blog.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jser.blog.entities.Blog;
import com.jser.blog.mapper.BlogMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class BlogService {
    BlogMapper blogMapper;


    @Inject
    public BlogService(BlogMapper blogMapper){
        this.blogMapper = blogMapper;
    }

    public Blog getBlog(int id){
        return blogMapper.getBlog(id);
    }

    public List<Blog> getBlogList(int userId){
        return blogMapper.getBlogList(userId);
    }

    public PageInfo<Blog> getBlogListWithPage(int userId, int page, int size){
        PageHelper.startPage(page, size, true);
        List<Blog> blogList = blogMapper.getBlogList(userId);
        return new PageInfo<Blog>(blogList);
    }

    public PageInfo<Blog> getBlogListWithPage(int page, int size){
        PageHelper.startPage(page, size, true);
        List<Blog> blogList = blogMapper.getAllBlogList();
        return new PageInfo<Blog>(blogList);
    }

    public int getBlogListTotal(int userId, int page, int size){
        return blogMapper.getBlogListTotal(userId, page*size, size);
    }

    public int addBlog(int userId, String title, String content, String description){
        return blogMapper.addBlog(userId, title, content, description );
    }

    public Blog addBlogWithBlog(int userId, String title, String content, String description){
        int addedBlogId = this.addBlog(userId, title, content, description);
        return this.getBlog(addedBlogId);
    }

    public boolean updateBlog(
            int id,
            String title,
            String content,
            String description,
            boolean atIndex){

        return blogMapper.updateBlog(title, content, description, atIndex, id)>0;
    }

    public boolean deleteBlog(int id){
        return blogMapper.deleteBlog(id)>0;
    }
}
