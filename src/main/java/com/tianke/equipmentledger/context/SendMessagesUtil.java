package com.tianke.equipmentledger.context;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.IOException;
import java.util.List;

public class SendMessagesUtil {

    public static void SendActionSMS(List<String> user_phone_list, String sms_content){
        Boolean bool = true;
        String phone_list_str = MessageType.EMPTYSTR;
        String phone="^[1](([3|5|8][\\d])|([4][4,5,6,7,8,9])|([6][2,5,6,7])|([7][^9])|([9][1,8,9]))[\\d]{8}$";// 验证手机号
        for(String user_phone : user_phone_list){
            phone_list_str += user_phone+",";
            if(!user_phone.matches(phone)) {
                bool = false;
                //return bool;
            }
        }
        if(!MessageType.EMPTYSTR.equals(phone_list_str)){
            phone_list_str = phone_list_str.substring(0,phone_list_str.length()-1);
        }

        HttpClient client = new HttpClient();
        PostMethod post = new PostMethod("http://utf8.api.smschinese.cn");
        post.addRequestHeader("Content-Type","application/x-www-form-urlencoded;charset=utf8");//在头文件中设置转码
        NameValuePair[] data ={ new NameValuePair("Uid", "wangtz"),new NameValuePair("Key", "d41d8cd98f00b204e980"),new NameValuePair("smsMob",phone_list_str),new NameValuePair("smsText","浙江天科设备台账管理系统，当前"+sms_content+"。请登陆设备台账系统进行查看。")};
        post.setRequestBody(data);
        try {
            client.executeMethod(post);
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        post.releaseConnection();
    }

}
