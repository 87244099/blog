package com.jser.blog.service;

import com.jser.blog.entities.Blog;
import com.jser.blog.entities.User;
import com.jser.blog.mapper.UserMapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

@Service
public class UserService implements UserDetailsService {

    UserMapper userMapper;
    BCryptPasswordEncoder bCryptPasswordEncoder;
    SqlSessionFactory sqlSessionFactory;

    @Inject
    public UserService(UserMapper userMapper, BCryptPasswordEncoder bCryptPasswordEncoder, SqlSessionFactory sqlSessionFactory){
        this.userMapper = userMapper;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.sqlSessionFactory = sqlSessionFactory;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = getUserByUsername(username);
        if(user == null){
            throw new UsernameNotFoundException("user is not exist");
        }

        return new org.springframework.security.core.userdetails.User(username, user.getPassword(), Collections.emptyList());
    }

    public User getUserByUsername(String username){
        User user = userMapper.getUserByUsername(username);

        if(user!=null){
            initUser(user);
        }

        return user;
    }

    public User getUserById(int id){
        User user = userMapper.getUserById(id);

        if(user!=null){
            initUser(user);
        }

        return user;
    }

    public List<User> getUserByIds(List<Integer> userIdList){

        try (SqlSession session = sqlSessionFactory.openSession()) {
            List<User> userList = session.selectList(
                    "com.jser.blog.mapper.UserExtMapper.getUserByIds", userIdList);

            if(userList!=null){
                initUser(userList);
            }

            return userList;
        }
    }

    public boolean addUser(String username, String password){
        return userMapper.addUser(username, password)>0;
    }

    public void save(String username, String password){
        userMapper.save(username, bCryptPasswordEncoder.encode(password));
    }

    public void initUser(User user){
        int userId = user.getId();
        String avatar = "/static/image/avatar/"+(userId%86)+".jpg";
        user.setAvatar(avatar);
    }
    public void initUser(List<User> userList){

        for(User user : userList){
            initUser(user);
        }
    }

}
