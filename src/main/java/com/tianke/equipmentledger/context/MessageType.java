package com.tianke.equipmentledger.context;

import java.io.Serializable;

public class MessageType implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int ZERO=0;
    public static final int ONE=1;
    public static final int TWO=2;
    public static final int THREE=3;
    public static final int FOUR=4;
    public static final int FIVE=5;
    public static final int SIX=6;
    public static final int SEVEN=7;
    public static final int EIGHT=8;
    public static final int NINE=9;
    public static final int TEN=10;
    public static final int ELEVEN=11;
    public static final int TWELVE=12;
    public static final int TWENTY=100;
    public static final int TWENTYTWO=22;

    public static final String STRZERO="0";
    public static final String STRONE="1";
    public static final String STRTWO="2";
    public static final String STRTHREE="3";
    public static final String STRFOUR="4";
    public static final String STRFIVE="5";
    public static final String STRSIX="6";
    public static final String STRSEVEN="7";
    public static final String STREIGTH="8";
    public static final String STRNINE="9";
    public static final String STRTEN="10";
    public static final String STRELEVEN="11";
    public static final String STRTWELVE="12";
    public static final String STRTHIRTEEN="13";
    public static final String STRFOURTEEN="14";
    public static final String STRFIFTEEN="15";
    public static final String TEWNTY="20";

    public static final String MSG_ZERO="操作成功";
    public static final String MSG_ONE="验证码不正确，请重新填写";
    public static final String MSG_TWO="账号或者密码错误";
    public static final String MSG_THREE="输入密码不一致";
    public static final String MSG_FOUR="该账号已存在";
    public static final String MSG_FIVE="邮箱格式不合法";
    public static final String MSG_SIX="操作失败";
    public static final String MSG_CAPTCHA="验证码错误";
    public static final String MSG_QUANXIAN="您没有该权限，请确认";
    public static final String MSG_PROJECTNAME="项目名称不能为空或空格";
    public static final String MSG_PROJECTCODE="项目编号不能为空或空格";

    public static final String MSG_TEN="该账号不存在";
    public static final String MSG_PHONE="账号格式不正确";
    public static final String MSG_PASSWORDFORMAT="密码格式不正确";
    public static final String sessionKey="LoginInfo";

    public static final String TONGGUO="通过";
    public static final String NOTTONGGUO="不通过";
    public static final String DELETESTR="删除";


    /**
     * 空字符串
     */
    public static final String EMPTYSTR="";
    public static final String XIEGANG="/";
    public static final String INDEX="index.jsp";
    public static final String LOGIN="login.jsp";

    public static final String COOKIENAMEONE = "test1";
    public static final String COOKIENAMETWO = "test2";

    public static final String TIANKE = "浙江天科高新技术发展有限公司";
    public static final String ZONGHEGUANLI = "综合管理部";
    public static final String SHIYANBU = "实验部";
    public static final String WEISHENGWUJIANCE = "微生物检测室";
    public static final String FENZIJIANCE = "分子检测室";
}
