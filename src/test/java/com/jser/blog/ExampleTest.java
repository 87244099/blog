package com.jser.blog;

import com.jser.blog.controller.AuthController;
import com.jser.blog.mapper.UserMapper;
import com.jser.blog.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class ExampleTest {
    @Mock
    BCryptPasswordEncoder mockEncoder;
    @Mock
    UserMapper mockMapper;
    @InjectMocks
    UserService userService;

    @Test
    public void testSave(){
        //调用userService
        //验证userService将请求转发给了userMapper

        Mockito.when(mockEncoder.encode("8682")).thenReturn("2333");
        userService.save("wgd", "8682");
        Mockito.verify(mockMapper).save("wgd", "2333");

    }

    @Test
    public void testLoadUserByUsername(){
        Mockito.when(mockMapper.getUserByUsername("jser")).thenReturn(null);
        //测试其一定会抛出异常
        Assertions.assertThrows(UsernameNotFoundException.class, ()->{
            userService.loadUserByUsername("jser");
        });


    }
}
