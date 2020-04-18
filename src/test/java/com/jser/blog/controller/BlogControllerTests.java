package com.jser.blog.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import com.jser.blog.entities.Blog;
import com.jser.blog.entities.User;
import com.jser.blog.service.BlogService;
import com.jser.blog.service.UserService;
import com.jser.blog.utils.AjaxResult;
import jdk.nashorn.internal.parser.JSONParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
public class BlogControllerTests {

    @Mock
    private BlogService blogService;
    @Mock
    private UserService userService;
    @Mock
    private AuthenticationManager authenticationManager;

    private MockMvc mvc;

    @BeforeEach
    void setUp(){
        mvc = MockMvcBuilders.standaloneSetup(new BlogController(blogService, userService)).build();
    }


    @Test
    void testGetBlogListArgs() throws Exception {

        testGetBlogListUserIdArg(-1);
        testGetBlogListUserIdArg(3);
    }

    void testGetBlogListUserIdArg(int userId) throws Exception {

        int page = 1;
        int size = 10;

        Blog blog = new Blog();

        PageInfo<Blog> somePage = new PageInfo<Blog>(IntStream.range(1, 10).mapToObj(i->blog).collect(Collectors.toList()));
        somePage.setPageNum(page);
        somePage.setPages(1);
        somePage.setTotal(10);
        somePage.setPageSize(size);
        PageInfo<Blog> allPage = new PageInfo<Blog>(IntStream.range(1, 20).mapToObj(i->blog).collect(Collectors.toList()));
        allPage.setPageNum(page);
        allPage.setPages(2);
        allPage.setTotal(20);
        allPage.setPageSize(size);

        if(userId == -1){
            Mockito.when(blogService.getBlogListWithPage(page, size)).thenReturn(allPage);
        }else{
            Mockito.when(blogService.getBlogListWithPage(userId, page, size)).thenReturn(somePage);
        }
        //参数要用字符串键值对
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("page", page+"");
        params.add("userId", userId+"");
        params.add("size", size+"");

//        new ObjectMapper().writeValueAsString(params)
        mvc.perform(
                MockMvcRequestBuilders.get("/blog").params(params)
        ).andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(mvcResult -> {

                mvcResult.getResponse().setCharacterEncoding("utf-8");
                String content = mvcResult.getResponse().getContentAsString();

                ObjectMapper mapper = new ObjectMapper();
                Map map = mapper.readValue(content, Map.class);
                PageInfo pageInfo = allPage;
                if(userId!=-1){
                    pageInfo = somePage;
                }


                Assertions.assertEquals(map.get("total"), (int)pageInfo.getTotal(), ("测试博客列表;getTotal;map=" + map));
                Assertions.assertEquals(map.get("page"), pageInfo.getPageNum(), "测试博客列表;getPageNum;map=" + map);
                Assertions.assertEquals(map.get("totalPage"), pageInfo.getPages(), "测试博客列表;getPages;map=" + map);

            });

        Mockito.reset(blogService);
    }

    @Test
    void testGetBlog() throws Exception {
        int blogId = 1;
        Blog blog = new Blog();
        Mockito.when(blogService.getBlog(blogId)).thenReturn(blog);
        mvc.perform(
                MockMvcRequestBuilders.get("/blog/"+blogId)
        ).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        mvcResult -> {

                            String contentAsString = mvcResult.getResponse().getContentAsString();
                            Assertions.assertTrue(contentAsString.contains("ok"));

                        }
                );

        blogId = 2;
        Mockito.when(blogService.getBlog(blogId)).thenReturn(null);
        mvc.perform(
                MockMvcRequestBuilders.get("/blog/"+blogId)
        ).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(
                        mvcResult -> {

                            String contentAsString = mvcResult.getResponse().getContentAsString();
                            Assertions.assertTrue(contentAsString.contains("fail"));

                        }
                );
    }

    @Test
    void testCreateBlogWhenLoginArg() throws Exception {

//        title : 博客标题, 博客标题不能为空，且不超过100个字符
//        content : 博客内容, 博客内容不能为空，且不超过10000个字符
//        description: 博客内容简要描述,可为空，如果为空则后台自动从content 中提取

        String rightTitle = "aaaaaaaaaaaa";
        String rightContent = "bbbbbbbbbbbbbb";
        String rightDesc = "ccccccc";

        String emptyTitle = "";
        testCreateBlogWhenLogin(emptyTitle, rightContent, rightDesc, mvcResult -> {
            mvcResult.getResponse().setCharacterEncoding("utf-8");
            String contentAsString = mvcResult.getResponse().getContentAsString();
            Assertions.assertTrue(contentAsString.contains("fail"), "empty err;status err;contentAsString="+contentAsString);
            Assertions.assertTrue(contentAsString.contains("博客标题, 博客标题不能为空，且不超过100个字符"), "title err:status err;contentAsString="+contentAsString);
        });

        StringBuilder bigTitle = new StringBuilder();
        for(int i=0; i<1000; i++){
            bigTitle.append(i);
        }
        testCreateBlogWhenLogin(bigTitle.toString(), rightContent, rightDesc, mvcResult -> {
            mvcResult.getResponse().setCharacterEncoding("utf-8");
            String contentAsString = mvcResult.getResponse().getContentAsString();
            Assertions.assertTrue(contentAsString.contains("fail"), "bigTitle err;status err;contentAsString="+contentAsString);
            Assertions.assertTrue(contentAsString.contains("博客标题, 博客标题不能为空，且不超过100个字符"), "bigTitle err:status err;contentAsString="+contentAsString);
        });


        String emptyContent = "";
        testCreateBlogWhenLogin(rightTitle, emptyContent, rightDesc, mvcResult -> {
            mvcResult.getResponse().setCharacterEncoding("utf-8");
            String contentAsString = mvcResult.getResponse().getContentAsString();
            Assertions.assertTrue(contentAsString.contains("fail"), "emptyContent err;status err;contentAsString="+contentAsString);
            Assertions.assertTrue(contentAsString.contains("博客内容, 博客内容不能为空，且不超过10000个字符"), "emptyContent err:status err;contentAsString="+contentAsString);
        });

        StringBuilder bigContent = new StringBuilder();
        for(int i=0; i<20000; i++){
            bigContent.append(i);
        }
        testCreateBlogWhenLogin(rightTitle, bigContent.toString(), rightDesc, mvcResult -> {
            mvcResult.getResponse().setCharacterEncoding("utf-8");
            String contentAsString = mvcResult.getResponse().getContentAsString();
            Assertions.assertTrue(contentAsString.contains("fail"), "bigContent err;status err;contentAsString="+contentAsString);
            Assertions.assertTrue(contentAsString.contains("博客内容, 博客内容不能为空，且不超过10000个字符"), "bigContent err:status err;contentAsString="+contentAsString);
        });

        String emptyDesc = "";
        testCreateBlogWhenLogin(rightTitle, rightContent, emptyDesc, mvcResult -> {
            mvcResult.getResponse().setCharacterEncoding("utf-8");
            String contentAsString = mvcResult.getResponse().getContentAsString();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(contentAsString);
            JsonNode data = jsonNode.get("data");

            ObjectMapper dataMapper = new ObjectMapper();
            Map map = dataMapper.readValue(data.toString(), Map.class);

            Assertions.assertTrue(contentAsString.contains("ok"), "emptyDesc err;status err;contentAsString="+contentAsString);
            Assertions.assertEquals(map.get("description"), rightContent, "emptyDesc err;status err;contentAsString="+contentAsString);
        });

        testCreateBlogWhenLogin(rightTitle, rightContent, rightDesc, mvcResult -> {
            mvcResult.getResponse().setCharacterEncoding("utf-8");
            String contentAsString = mvcResult.getResponse().getContentAsString();
            Assertions.assertTrue(contentAsString.contains("ok"));
        });
    }

    void testCreateBlogWhenLogin(String title, String content, String description, ResultMatcher resultMatcher) throws Exception {
        String username = "jser";
        String password = "fai123";
        setLogin(username, password);

        Map<String, String> info = new HashMap<>();
        info.put("title", title);
        info.put("content", content);
        info.put("description", description);
        ObjectMapper mapper = new ObjectMapper();
        String requestContent = mapper.writeValueAsString(info);

        int tmpUserId=1;
        User mockUser = new User();
        mockUser.setId(tmpUserId);
        Blog mockBlog = new Blog();

        Mockito.when(userService.getUserByUsername(username)).thenReturn(mockUser);

        if("".equals(description)){
            description = content;
        }

        mockBlog.setTitle(title);
        mockBlog.setContent(content);
        mockBlog.setDescription(description);

        Mockito.when(blogService.addBlogWithBlog(tmpUserId, title, content, description)).thenReturn(mockBlog);

        mvc.perform(
                MockMvcRequestBuilders.post("/blog").content(requestContent).contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        ).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(resultMatcher);

        Mockito.reset(userService);
        Mockito.reset(blogService);
    }
    //mock只对get方法进行模拟，set方法的逻辑还是会调用到
    void setLogin(String username, String password){
        UserDetails mockUserDetails = new org.springframework.security.core.userdetails.User(username, password, Collections.emptyList());
        //接下来比对密码是否正确
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                mockUserDetails,
                password,
                mockUserDetails.getAuthorities()
        );
        authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    @Test
    void testCreateBlogWhenLogout() throws Exception {
        String rightTitle = "aaaaaaaaaaaa";
        String rightContent = "bbbbbbbbbbbbbb";
        String rightDesc = "ccccccc";

        testCreateBlogWhenLogout(rightTitle, rightContent, rightDesc, mvcResult -> {
            mvcResult.getResponse().setCharacterEncoding("utf-8");
            String contentAsString = mvcResult.getResponse().getContentAsString();
            Assertions.assertTrue(contentAsString.contains("fail"), "testCreateBlogWhenLogout err;status err;contentAsString="+contentAsString);
            Assertions.assertTrue(contentAsString.contains("登录后才能操作"), "testCreateBlogWhenLogout err;msg err;contentAsString="+contentAsString);
        });
    }


    void testCreateBlogWhenLogout(String title, String content, String description, ResultMatcher resultMatcher) throws Exception {


        Map<String, String> info = new HashMap<>();
        info.put("title", title);
        info.put("content", content);
        info.put("description", description);
        ObjectMapper mapper = new ObjectMapper();
        String requestContent = mapper.writeValueAsString(info);


        int tmpUserId = 1;
        User mockUser = new User();
        mockUser.setId(tmpUserId);

        Blog mockBlog = new Blog();

        if("".equals(description)){
            description = content;
        }

        mockBlog.setTitle(title);
        mockBlog.setContent(content);
        mockBlog.setDescription(description);

        Mockito.when(blogService.addBlogWithBlog(tmpUserId, title, content, description)).thenReturn(mockBlog);

        mvc.perform(
                MockMvcRequestBuilders.post("/blog").content(requestContent).contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
        ).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(resultMatcher);

        Mockito.reset(userService);
        Mockito.reset(blogService);
    }
}
