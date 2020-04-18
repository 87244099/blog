package com.jser.blog.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;

public class Blog {
    int id;
    String title;
    String description;
    String content;
    User user;
    @JsonIgnore
    int userId;
    String createdAt;
    String updateAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(String updateAt) {
        this.updateAt = updateAt;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Blog blog = (Blog) o;
        return id == blog.id &&
                userId == blog.userId &&
                Objects.equals(title, blog.title) &&
                Objects.equals(description, blog.description) &&
                Objects.equals(content, blog.content) &&
                Objects.equals(createdAt, blog.createdAt) &&
                Objects.equals(updateAt, blog.updateAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, content, userId, createdAt, updateAt);
    }
}
