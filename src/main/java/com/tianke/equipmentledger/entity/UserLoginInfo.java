package com.tianke.equipmentledger.entity;

public class UserLoginInfo {
    private Integer id;
    private String user_code;
    private String user_name;
    private String dept_code;
    private Integer user_rights;
    private Integer job_title_status;

    public UserLoginInfo(){

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUser_code() {
        return user_code;
    }

    public void setUser_code(String user_code) {
        this.user_code = user_code;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getDept_code() {
        return dept_code;
    }

    public void setDept_code(String dept_code) {
        this.dept_code = dept_code;
    }

    public Integer getJob_title_status() {
        return job_title_status;
    }

    public void setJob_title_status(Integer job_title_status) {
        this.job_title_status = job_title_status;
    }

    public Integer getUser_rights() {
        return user_rights;
    }

    public void setUser_rights(Integer user_rights) {
        this.user_rights = user_rights;
    }

    @Override
    public String toString() {
        return "UserLoginInfo{" +
                "id=" + id +
                ", user_code='" + user_code + '\'' +
                ", user_name='" + user_name + '\'' +
                ", dept_code='" + dept_code + '\'' +
                ", user_rights=" + user_rights +
                ", job_title_status=" + job_title_status +
                '}';
    }
}
