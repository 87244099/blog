package com.jser.blog.utils;

public class AjaxResult {
    String status;
    String msg;
    Object data;
    Boolean isLogin;

    Integer total;
    Integer page;
    Integer totalPage;

    String avatar;

    public AjaxResult(String status, String msg, Object data, Boolean isLogin, Integer total, Integer page, Integer totalPage){
        this.status = status;
        this.msg = msg;
        this.data = data;
        this.isLogin = isLogin;
        this.total = total;
        this.page = page;
        this.totalPage = totalPage;
    }


    public AjaxResult(String status, String msg, Object data, Boolean isLogin){
        this.status=status;
        this.msg=msg;
        this.data=data;
        this.isLogin = isLogin;
    }

    public static AjaxResult getInstance(String status, String msg, Object data, Boolean isLogin){
        return new AjaxResult(status, msg, data, isLogin);
    }

    public static AjaxResult getInstance(boolean status, String msg, Object data, Boolean isLogin){
        return new AjaxResult(status ? "ok" : "fail", msg, data, isLogin);
    }

    public String getStatus() {
        return status;
    }

    public AjaxResult setStatus(String status) {
        this.status = status;
        return this;
    }

    public AjaxResult setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public Object getData() {
        return data;
    }

    public AjaxResult setData(Object data) {
        this.data = data;
        return this;
    }

    public Boolean getIsLogin() {
        return isLogin;
    }

    public AjaxResult setLogin(Boolean isLogin) {
        this.isLogin = isLogin;
        return this;
    }

    public AjaxResult setPage(Integer page) {
        this.page = page;
        return this;
    }

    public Integer getPage() {
        return page;
    }

    public AjaxResult setTotal(Integer total) {
        this.total = total;
        return this;
    }

    public Integer getTotal() {
        return total;
    }

    public AjaxResult setTotalPage(Integer totalPage) {
        this.totalPage = totalPage;
        return this;
    }

    public Integer getTotalPage() {
        return totalPage;
    }

    public String getAvatar() {
        return avatar;
    }

    public AjaxResult setAvatar(String avatar) {
        this.avatar = avatar;
        return this;
    }
}
