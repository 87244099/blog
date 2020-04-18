package com.jser.blog.controller;

import com.github.pagehelper.PageInfo;
import com.jser.blog.entities.Blog;
import com.jser.blog.entities.User;
import com.jser.blog.service.BlogService;
import com.jser.blog.service.UserService;
import com.jser.blog.utils.AjaxResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class BlogController {
    private BlogService blogService;
    private UserService userService;

    @Inject
    public BlogController(BlogService blogService, UserService userService){
        this.blogService = blogService;
        this.userService = userService;
    }

    @GetMapping("/blog")
    @ResponseBody
    public Object getBlogList(@RequestParam Map<String, Object> requestBody){

        int page = Integer.parseInt(requestBody.getOrDefault("page", "1").toString());
        int userId = Integer.parseInt(requestBody.getOrDefault("userId", "-1").toString());
        boolean atIndex = Boolean.parseBoolean(requestBody.getOrDefault("atIndex", "false").toString());

        int size = 10;
        PageInfo<Blog> blogPageInfo = null;
        if(userId==-1){
            blogPageInfo = blogService.getBlogListWithPage(page, size);
        }else{
            blogPageInfo = blogService.getBlogListWithPage(userId, page, size);
        }

        if(blogPageInfo==null){
            return AjaxResult.getInstance(false, "系统异常", null, null);
        }

        initUser4Blog(blogPageInfo.getList());

        return AjaxResult.getInstance(true, "获取成功", blogPageInfo.getList(), null)
                .setTotal((int) blogPageInfo.getTotal())
                .setTotalPage(blogPageInfo.getPages())
                .setPage(blogPageInfo.getPageNum());
    }

    @GetMapping("/blog/{blogId}")
    @ResponseBody
    public Object getBlog(@PathVariable(value = "blogId") int blogId){


        Blog blog = blogService.getBlog(blogId);

        if(blog==null){
            return AjaxResult.getInstance(false, "系统异常", null, null);
        }

        initUser4Blog(blog);

        return AjaxResult.getInstance(true, "获取成功", blog, null);
    }


    @PostMapping("/blog")
    @ResponseBody
    public Object createBlog(
            @RequestBody Map<String, Object> requestBody
    ){
        String title = requestBody.getOrDefault("title", "").toString();
        String content = requestBody.getOrDefault("content", "").toString();
        String description = requestBody.getOrDefault("description", "").toString();

        if(title == null || title.length()<=0 || title.length()>100){
            return AjaxResult.getInstance(false, "博客标题, 博客标题不能为空，且不超过100个字符", null, null);
        }
        if(content == null || content.length()<=0 || content.length()>100){
            return AjaxResult.getInstance(false, "博客内容, 博客内容不能为空，且不超过10000个字符", null, null);
        }

        if("".equals(description)){
            description = content;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByUsername(authentication==null ? null : authentication.getName());
        if(user == null){
            return AjaxResult.getInstance(false, "登录后才能操作", null, null);
        }


        Blog blog = blogService.addBlogWithBlog(user.getId(), title, content, description);
        initUser4Blog(blog);


        if(blog!=null){
            return AjaxResult.getInstance(true, "创建成功", blog, null);
        }else{
            return AjaxResult.getInstance(false, "创建失败", null, null);
        }

    }

    @PatchMapping("/blog/{blogId}")
    @ResponseBody
    public Object updateBlog(@PathVariable("blogId") int blogId, @RequestBody Map<String, Object> requestBody){

//        {"status": "fail", "msg": "登录后才能操作"}
//        {"status": "fail", "msg": "博客不存在"}
//        {"status": "fail", "msg": "无法修改别人的博客"}


        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByUsername(username);
        if(user == null){
            return AjaxResult.getInstance(false, "登录后才能操作", null, null);
        }

        Blog blog = blogService.getBlog(blogId);
        if(blog==null){
            return AjaxResult.getInstance(false, "博客不存在", null, null);
        }

        if(blog.getUserId() != user.getId()){
            return AjaxResult.getInstance(false, "无法修改别人的博客", null, null);
        }


        String title = requestBody.getOrDefault("title", blog.getTitle()).toString();
        String content = requestBody.getOrDefault("content", blog.getContent()).toString();
        String description = requestBody.getOrDefault("description", blog.getDescription()).toString();
        boolean atIndex = Boolean.parseBoolean(requestBody.getOrDefault("atIndex", "false").toString());

        boolean success = blogService.updateBlog(blogId, title, content, description, atIndex);
        if(success){
            Blog updatedBlog = blogService.getBlog(blogId);
            initUser4Blog(updatedBlog);
            return AjaxResult.getInstance(true, "修改成功", updatedBlog, null);
        }else{
            return AjaxResult.getInstance(false, "修改失败", null, null);
        }


    }

    @DeleteMapping("/blog/{blogId}")
    @ResponseBody
    public Object deleteBlog(@PathVariable("blogId") int blogId){

//        {"status": "fail", "msg": "登录后才能操作"}
//        {"status": "fail", "msg": "博客不存在"}
//        {"status": "fail", "msg": "无法删除别人的博客"}

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByUsername(username);
        if(user == null){
            return AjaxResult.getInstance(false, "登录后才能操作", null, null);
        }

        Blog blog = blogService.getBlog(blogId);
        if(blog==null){
            return AjaxResult.getInstance(false, "博客不存在", null, null);
        }

        if(blog.getUserId() != user.getId()){
            return AjaxResult.getInstance(false, "无法修改别人的博客", null, null);
        }

        boolean success = blogService.deleteBlog(blogId);
        if(success){
            return AjaxResult.getInstance(true, "删除成功", null, null);
        }else{
            return AjaxResult.getInstance(false, "删除失败", null, null);
        }
    }

    void initUser4Blog(Blog blog){
        List<Blog> blogList = new ArrayList<>();
        blogList.add(blog);
        initUser4Blog(blogList);
    }

    void initUser4Blog(List<Blog> blogList){

        List<Integer> userIdList = blogList.stream().map(Blog::getUserId).collect(Collectors.toList());

        List<User> userList = userService.getUserByIds(userIdList);
        Map<Integer, List<User>> userMap = userList.stream().collect(Collectors.groupingBy(User::getId));
        blogList.forEach(blog -> {
            int id = blog.getUserId();
            List<User> users = userMap.get(id);
            if(users!=null && !users.isEmpty()){
                blog.setUser(users.get(0));
            }
        });
    }

}
