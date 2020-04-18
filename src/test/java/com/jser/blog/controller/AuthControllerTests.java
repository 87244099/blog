package com.jser.blog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jser.blog.entities.User;
import com.jser.blog.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.IntStream;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
public class AuthControllerTests {

    private MockMvc mvc;

    @Mock
    private UserService userService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @BeforeEach
    void setUp(){
        mvc = MockMvcBuilders.standaloneSetup(new AuthController(userService, authenticationManager,  bCryptPasswordEncoder)).build();
    }


    @Test
    void testRegister() throws Exception {
        String username = "jser";
        String password = "fai123";
        String encodePassword = "encodeFai123";
        //普通的注册成功
        Map<String, Object> usernamePassword = new HashMap<>();
        usernamePassword.put("username", username);
        usernamePassword.put("password", password);

        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername(username);
        mockUser.setPassword(encodePassword);
        mockUser.setAvatar("http://avatar.com/1.png");
        mockUser.setUpdatedAt("2017-12-27T07:40:09.697Z");
        mockUser.setCreatedAt("2017-12-27T07:40:09.697Z");

        Mockito.when(userService.getUserByUsername(username)).thenReturn(null).thenReturn(mockUser);
        Mockito.when(bCryptPasswordEncoder.encode(password)).thenReturn(encodePassword);
        Mockito.when(userService.addUser(username, encodePassword)).thenReturn(true);
        mvc.perform(
                MockMvcRequestBuilders.post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(new ObjectMapper().writeValueAsString(usernamePassword))
        ).andExpect(mvcResult -> {
            MockHttpServletResponse response = mvcResult.getResponse();
            response.setCharacterEncoding("utf-8");//解决返回结果中文乱码
            String contentAsString = response.getContentAsString();
            System.out.println(contentAsString);
            Assertions.assertTrue(contentAsString.contains("注册成功"));
            Assertions.assertTrue(contentAsString.contains("ok"));
            Assertions.assertTrue(contentAsString.contains(username));
            Assertions.assertFalse(contentAsString.contains(encodePassword));
        });
    }

    @Test
    void testRegisterUsernameLength(){
        //非法字符长度检测账号
        IntStream.range(0, 20).forEach((len)->{
            StringBuilder username = new StringBuilder();
            for(int i=0; i<len; i++){
                username.append("j");
            }

            if(len<1 || len>15){
                testRegisterArgs(username.toString(), "1111111", mvcResult -> {
                    MockHttpServletResponse response = mvcResult.getResponse();
                    response.setCharacterEncoding("utf-8");//解决返回结果中文乱码
                    String contentAsString = response.getContentAsString();
                    Assertions.assertTrue(contentAsString.contains("fail"));
                });
            }

        });
    }

    @Test
    void testRegisterUsernameFormat(){
        //非法字符检测账号
        List<String> usernameList = Arrays.asList("~@#$%^&*()+`-=[]\\{}|/*.<>?,./".split(""));
        usernameList.forEach(new Consumer<String>() {
            @Override
            public void accept(String s) {
                String username = s+s;
                testRegisterArgs(username, "111111", mvcResult -> {
                    MockHttpServletResponse response = mvcResult.getResponse();
                    response.setCharacterEncoding("utf-8");//解决返回结果中文乱码
                    String contentAsString = response.getContentAsString();
                    Assertions.assertTrue(contentAsString.contains("fail"));
                });
            }
        });


        testRegisterArgs("testtest", "111111", mvcResult -> {
            MockHttpServletResponse response = mvcResult.getResponse();
            response.setCharacterEncoding("utf-8");//解决返回结果中文乱码
            String contentAsString = response.getContentAsString();
            Assertions.assertTrue(contentAsString.contains("ok"));
        });
    }

    @Test
    void testRegisterPasswordLength(){
        //密码格式检测:长度6到16个任意字符
        IntStream.range(0, 20).forEach((len)->{
            StringBuilder password = new StringBuilder();
            for(int i=0; i<len; i++){
                password.append("j");
            }
            if(len<5 || len>16){
                testRegisterArgs("jser", password.toString(), mvcResult -> {
                    MockHttpServletResponse response = mvcResult.getResponse();
                    response.setCharacterEncoding("utf-8");//解决返回结果中文乱码
                    String contentAsString = response.getContentAsString();
                    Assertions.assertTrue(contentAsString.contains("fail"));
                });
            }
        });
    }
    //预期用户是新用户，输入账号密码会成功
    void testRegisterArgs(String username, String password, ResultMatcher resultMatcher){


        Map<String, Object> usernamePassword = new HashMap<>();
        usernamePassword.put("username", username);
        usernamePassword.put("password", password);


        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername("jser");
        mockUser.setPassword("encodeFai123");
        mockUser.setAvatar("http://avatar.com/1.png");
        mockUser.setUpdatedAt("2017-12-27T07:40:09.697Z");
        mockUser.setCreatedAt("2017-12-27T07:40:09.697Z");

        String encodePassword = "ecd"+password;
        Mockito.when(userService.getUserByUsername(username)).thenReturn(null).thenReturn(mockUser);
        Mockito.when(userService.addUser(username, encodePassword)).thenReturn(true);

        Mockito.when(bCryptPasswordEncoder.encode(password)).thenReturn(encodePassword);
        try {

            mvc.perform(
                    MockMvcRequestBuilders.post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(new ObjectMapper().writeValueAsString(usernamePassword))
            ).andExpect(resultMatcher);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //重复调用的，要进行清除
        Mockito.reset(userService);
        Mockito.reset(bCryptPasswordEncoder);
    }


    @Test
    void testNoLogin() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/auth"))
                .andExpect(status().isOk())
                .andExpect(new ResultMatcher() {
                    @Override
                    public void match(MvcResult mvcResult) throws Exception {
                        System.out.println(mvcResult.getResponse().getContentAsString());
                    }
                });
    }

    @Test
    void testLoginUsernameLength() throws Exception{

        //非法字符长度检测账号
        IntStream.range(0, 20).forEach((len)->{
            StringBuilder username = new StringBuilder();
            for(int i=0; i<len; i++){
                username.append("j");
            }

            if(len<1 || len>15){
                testLoginArgs(username.toString(), "1111111", mvcResult -> {
                    MockHttpServletResponse response = mvcResult.getResponse();
                    response.setCharacterEncoding("utf-8");//解决返回结果中文乱码
                    String contentAsString = response.getContentAsString();
                    Assertions.assertTrue(contentAsString.contains("fail"), "登录账号长度非法；状态错误; username="+username+"; contentAsString="+contentAsString);
                    Assertions.assertTrue(contentAsString.contains("用户不存在"), "登录账号长度非法；返回语错误; username="+username+"; contentAsString="+contentAsString);
                });
            }

        });
    }

    @Test
    void testLoginUsernameFormat(){
        //非法字符检测账号
        List<String> usernameList = Arrays.asList("~@#$%^&*()+`-=[]\\{}|/*.<>?,./".split(""));
        usernameList.forEach(new Consumer<String>() {
            @Override
            public void accept(String s) {
                String username = s+s;
                testLoginArgs(username, "111111", mvcResult -> {
                    MockHttpServletResponse response = mvcResult.getResponse();
                    response.setCharacterEncoding("utf-8");//解决返回结果中文乱码
                    String contentAsString = response.getContentAsString();
                    Assertions.assertTrue(contentAsString.contains("fail"), "登录账号格式错误;状态异常;usernamee="+username+"; contentAsString="+contentAsString);
                    Assertions.assertTrue(contentAsString.contains("用户不存在"), "登录账号格式错误;返回语错误;usernamee="+username+"; contentAsString="+contentAsString);
                });
            }
        });
    }

    @Test
    void testLoginWhenUserNotEixst(){

        String username = "jser";
        String password = "fai123";


        Map<String, Object> usernamePassword = new HashMap<>();
        usernamePassword.put("username", username);
        usernamePassword.put("password", password);


        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername("jser");
        mockUser.setPassword("encodeFai123");
        mockUser.setAvatar("http://avatar.com/1.png");
        mockUser.setUpdatedAt("2017-12-27T07:40:09.697Z");
        mockUser.setCreatedAt("2017-12-27T07:40:09.697Z");

        String encodePassword = "ecd"+password;

        Mockito.when(userService.loadUserByUsername(username)).thenThrow(new UsernameNotFoundException(username));

        try {

            mvc.perform(
                    MockMvcRequestBuilders.post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(new ObjectMapper().writeValueAsString(usernamePassword))
            ).andExpect(resultMatcher->{
                resultMatcher.getResponse().setCharacterEncoding("utf-8");
                String contentAsString = resultMatcher.getResponse().getContentAsString();
                Assertions.assertTrue(contentAsString.contains("fail"), "测试登录功能;账号正常但没注册过;状态异常;content="+contentAsString);
                Assertions.assertTrue(contentAsString.contains("用户不存在"), "测试登录功能;账号正常但没注册过;返回语错误;content="+contentAsString);
            });

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //重复调用的，要进行清除
        Mockito.reset(userService);
        Mockito.reset(bCryptPasswordEncoder);
    }

    @Test
    void testLoginPasswordLength(){
        //密码格式检测:长度6到16个任意字符
        IntStream.range(0, 20).forEach((len)->{
            StringBuilder password = new StringBuilder();
            for(int i=0; i<len; i++){
                password.append("j");
            }
            if(len<5 || len>16){
                testLoginArgs("jser", password.toString(), mvcResult -> {
                    MockHttpServletResponse response = mvcResult.getResponse();
                    response.setCharacterEncoding("utf-8");//解决返回结果中文乱码
                    String contentAsString = response.getContentAsString();
                    Assertions.assertTrue(contentAsString.contains("fail"), "登录密码长度异常;状态错误;password="+password+";contentAsString"+contentAsString);
                    Assertions.assertTrue(contentAsString.contains("密码不正确"), "登录密码长度异常;返回语错误;password="+password+";contentAsString"+contentAsString);
                });
            }
        });
    }

    @Test
    void testLoginSession() throws Exception {

        String username = "jser";
        String password = "fai123";
        String encodePassword = "encodeFai123";
        Map<String, Object> usernamePassword = new HashMap<>();
        usernamePassword.put("username", username);
        usernamePassword.put("password", password);



        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername(username);
        mockUser.setPassword(encodePassword);
        mockUser.setAvatar("http://avatar.com/1.png");
        mockUser.setUpdatedAt("2017-12-27T07:40:09.697Z");
        mockUser.setCreatedAt("2017-12-27T07:40:09.697Z");

        Mockito.when(userService.loadUserByUsername(username)).thenReturn(
                new org.springframework.security.core.userdetails.User(username, encodePassword, Collections.emptyList())
        ).thenReturn(
                new org.springframework.security.core.userdetails.User(username, encodePassword, Collections.emptyList())
        );
//        Mockito.when(bCryptPasswordEncoder.encode(password)).thenReturn(encodePassword);
        Mockito.when(userService.getUserByUsername(username)).thenReturn(mockUser).thenReturn(mockUser);

        //测试登录流程能否拿到正常的会话信息
        mvc.perform(
                MockMvcRequestBuilders.post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(new ObjectMapper().writeValueAsString(usernamePassword))
        ).andExpect(status().isOk())
                .andExpect(loginMvcResult -> {
                    loginMvcResult.getResponse().setCharacterEncoding("utf-8");//解决返回结果中文乱码
                    String contentAsString = loginMvcResult.getResponse().getContentAsString();
                    Assertions.assertTrue(contentAsString.contains("ok"), "登录会话测试;状态错误;");

                    HttpSession session = loginMvcResult.getRequest().getSession();

                    Assertions.assertNotEquals(session, null, "登录会话测试;会话为空");



                    mvc.perform(
                            MockMvcRequestBuilders.get("/auth")
                                    .session((MockHttpSession) session)
                    ).andExpect(status().isOk())
                            .andExpect(authMvcResult -> {
                                String content = authMvcResult.getResponse().getContentAsString();

                                Assertions.assertTrue(content.contains("ok"),"登录会话测试;获取权限;状态码错误;content="+content);
                                Assertions.assertTrue(content.contains("true"), "登录会话测试;登录状态错误;content="+content);

                            });
                });
    }

    void testLoginArgs(String username, String password, ResultMatcher resultMatcher){


        Map<String, Object> usernamePassword = new HashMap<>();
        usernamePassword.put("username", username);
        usernamePassword.put("password", password);


        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername("jser");
        mockUser.setPassword("encodeFai123");
        mockUser.setAvatar("http://avatar.com/1.png");
        mockUser.setUpdatedAt("2017-12-27T07:40:09.697Z");
        mockUser.setCreatedAt("2017-12-27T07:40:09.697Z");

        String encodePassword = "ecd"+password;

        Mockito.when(userService.getUserByUsername(username)).thenReturn(null).thenReturn(mockUser);
        Mockito.when(userService.addUser(username, encodePassword)).thenReturn(true);
        Mockito.when(bCryptPasswordEncoder.encode(password)).thenReturn(encodePassword);
        Mockito.when(userService.loadUserByUsername(username)).thenAnswer(
                (Answer) invocationOnMock -> {
                    return (UserDetails) new org.springframework.security.core.userdetails.User(username, encodePassword, Collections.emptyList());
                }
        );

        try {

            mvc.perform(
                    MockMvcRequestBuilders.post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON_UTF8)
                            .content(new ObjectMapper().writeValueAsString(usernamePassword))
            ).andExpect(resultMatcher);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //重复调用的，要进行清除
        Mockito.reset(userService);
        Mockito.reset(bCryptPasswordEncoder);
    }

    @Test
    void testLogout() throws Exception {
        //成功 {"status": "fail", "msg": "用户尚未登录"}
        //失败 {"status": "ok", "msg": "注销成功"}

        String username = "jser";
        String password = "fai123";
        String encodePassword = "encodeFai123";

        User mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername(username);
        mockUser.setPassword(encodePassword);
        mockUser.setAvatar("http://avatar.com/1.png");
        mockUser.setUpdatedAt("2017-12-27T07:40:09.697Z");
        mockUser.setCreatedAt("2017-12-27T07:40:09.697Z");

        Map<String, Object> usernamePassword = new HashMap<>();
        usernamePassword.put("username", username);
        usernamePassword.put("password", password);

        Mockito.when(userService.getUserByUsername(null)).thenReturn(null).thenReturn(null);
        Mockito.when(userService.getUserByUsername(username)).thenReturn(mockUser).thenReturn(mockUser);
        Mockito.when(userService.loadUserByUsername(username)).thenReturn(
                new org.springframework.security.core.userdetails.User(username, encodePassword, Collections.emptyList())
        );
        //先测试未登录时访问
        mvc.perform(
                MockMvcRequestBuilders.get("/auth/logout")
        ).andExpect(status().isOk())
                .andExpect(mvcResult -> {
                    mvcResult.getResponse().setCharacterEncoding("utf-8");
                    String contentAsString = mvcResult.getResponse().getContentAsString();
                    Assertions.assertTrue(contentAsString.contains("fail"), "测试登出功能;未登录时测试;状态异常;contentAsString="+contentAsString);
                    Assertions.assertTrue(contentAsString.contains("用户尚未登录"), "测试登出功能;未登录时测试;返回语错误;contentAsString="+contentAsString);
                });

        AtomicReference<HttpSession> session = new AtomicReference<>();

        //登录后访问
        mvc.perform(
          MockMvcRequestBuilders.post("/auth/login")
                  .contentType(MediaType.APPLICATION_PROBLEM_JSON_UTF8)
                  .content(new ObjectMapper().writeValueAsString(usernamePassword))
        ).andExpect(status().isOk())
            .andExpect(loginMvcResult -> {
                loginMvcResult.getResponse().setCharacterEncoding("utf-8");
                String contentAsString = loginMvcResult.getResponse().getContentAsString();
                Assertions.assertTrue(contentAsString.contains("ok"), "测试登出功能;登录异常;contentAsString="+contentAsString);

                session.set(loginMvcResult.getRequest().getSession());

            });

        Assertions.assertNotEquals(session.get(), null, "获取会话失败");

        mvc.perform(
                MockMvcRequestBuilders.get("/auth")
                        .contentType(MediaType.APPLICATION_PROBLEM_JSON_UTF8)
        ).andExpect(status().isOk()).andExpect(authMvcResult->{

            authMvcResult.getResponse().setCharacterEncoding("utf-8");
            String authContent = authMvcResult.getResponse().getContentAsString();
            Assertions.assertTrue(authContent.contains("ok"), "测试登出功能;登录状态异常;authContent="+authContent);

        });

        //访问退出接口
        mvc.perform(
                MockMvcRequestBuilders.get("/auth/logout")
                        .session((MockHttpSession) session.get())
        ).andExpect(status().isOk())
                .andExpect(logoutMvcResult->{
                    logoutMvcResult.getResponse().setCharacterEncoding("utf-8");
                    String content = logoutMvcResult.getResponse().getContentAsString();
                    Assertions.assertTrue(content.contains("ok"), "测试登出功能;退出异常;状态异常;content="+content);
                    Assertions.assertTrue(content.contains("注销成功"), "测试登出功能;退出异常;返回语错误;content="+content);

                });


        mvc.perform(
                MockMvcRequestBuilders.get("/auth")
                        .contentType(MediaType.APPLICATION_PROBLEM_JSON_UTF8)
        ).andExpect(status().isOk()).andExpect(authMvcResult->{

            authMvcResult.getResponse().setCharacterEncoding("utf-8");
            String authContent = authMvcResult.getResponse().getContentAsString();
            Assertions.assertTrue(authContent.contains("fail"), "测试登出功能;登录状态异常;authContent="+authContent);

        });


    }


}
