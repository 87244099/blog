package com.jser.blog.controller;

import com.jser.blog.entities.User;
import com.jser.blog.service.UserService;
import com.jser.blog.utils.AjaxResult;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Controller
public class AuthController {

    private UserService userService;
    private AuthenticationManager authenticationManager;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Inject
    public AuthController(UserService userService, AuthenticationManager authenticationManager, BCryptPasswordEncoder bCryptPasswordEncoder){
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @PostMapping("/auth/register")
    @ResponseBody
    public Object register(@RequestBody Map<String, Object> requestBody){

        String username = requestBody.get("username").toString();
        String password = requestBody.get("password").toString();

        if(username.length()<1 || username.length()>15){
            return AjaxResult.getInstance(false, "用户名, 长度1到15个字符，只能是字母数字下划线中文", null, null);
        }
        String all  = "^[\\u4E00-\\u9FA5\\uF900-\\uFA2D\\w]{1,15}$";//字母、数字、下划线、中文
        Pattern pattern = Pattern.compile(all);
        if(!pattern.matcher(username).matches()){
            return AjaxResult.getInstance(false, "用户名, 长度1到15个字符，只能是字母数字下划线中文", null, null);
        }


        if(password.length()<6 || password.length()>16){
            return AjaxResult.getInstance(false, " 密码, 长度6到16个任意字符", null, null);
        }

        User user = userService.getUserByUsername(username);
        if(user!=null){
            return AjaxResult.getInstance(false, "该账号已经注册过了", null, null);
        }
        String encodePassword = bCryptPasswordEncoder.encode(password);
        boolean success = userService.addUser(username, encodePassword);
        user = userService.getUserByUsername(username);

        if(success){
            user.setUsername(username);
            return AjaxResult.getInstance(true, "注册成功", user, null);
        }else{
            return AjaxResult.getInstance(false, "注册失败", null, null);
        }
    }

    @PostMapping("/auth/login")
    @ResponseBody
    public Object login(@RequestBody Map<String, Object> requestBody){

        String username = requestBody.get("username").toString();
        String password = requestBody.get("password").toString();


        if("".equals(username)){
            return AjaxResult.getInstance(false, "用户不存在", null, false);
        }

        if(username.length()<1 || username.length()>15){
            return AjaxResult.getInstance(false, "用户不存在", null, null);
        }

        String all  = "^[\\u4E00-\\u9FA5\\uF900-\\uFA2D\\w]{1,6}$";
        Pattern pattern = Pattern.compile(all);
        if(!pattern.matcher(username).matches()){
            return AjaxResult.getInstance(false, "用户不存在", null, null);
        }

        if(password.length()<6 || password.length()>16){
            return AjaxResult.getInstance(false, "密码不正确", null, null);
        }

        UserDetails userDetails = null;

        try{
            userDetails = userService.loadUserByUsername(username);//这里面会验证用户是否存在
        }catch (UsernameNotFoundException exp){
            return AjaxResult.getInstance(false, "用户不存在", null, null);
        }


        //如果密码不对，设置toker的过程会报错
        try{

            //接下来比对密码是否正确
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    password,
                    userDetails.getAuthorities()
            );

            authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(token);

            User user = userService.getUserByUsername(username);

            return AjaxResult.getInstance(true, "登录成功", user, true);
        }catch (BadCredentialsException ignored){
            return AjaxResult.getInstance(false, "密码不正确", null, false);
        }
    }

    @GetMapping("/auth")
    @ResponseBody
    public Object auth(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByUsername(authentication==null?null:authentication.getName());

        if(user==null){
            return AjaxResult.getInstance(false, null, null, false);
        }

        return AjaxResult.getInstance(true, "登录成功", user, true).setAvatar(user.getAvatar());
    }

    @GetMapping("/auth/logout")
    @ResponseBody
    public Object logout(){

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByUsername(authentication==null?null:authentication.getName());
        if(user == null){
            return AjaxResult.getInstance(false, "用户尚未登录", null, false);
        }

        SecurityContextHolder.clearContext();

        return AjaxResult.getInstance(true, "注销成功", null, false);

    }
}
