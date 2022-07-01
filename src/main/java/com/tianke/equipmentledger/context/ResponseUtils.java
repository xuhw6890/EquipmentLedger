package com.tianke.equipmentledger.context;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ResponseUtils {
    //返回json
    public static void renderJson(HttpServletResponse response, String text){
        render(response,"text/plain;charset=UTF-8",text);
        return;
    }

    //返回文本
    public static void renderText(HttpServletResponse response,String text){
        render(response,"text/plain;charset=UTF-8",text);
    }

    //发送内容
    public static void render(HttpServletResponse response,String contentType,String text){
        response.setContentType(contentType);
        response.setCharacterEncoding("utf-8");
        response.setHeader("Pragma","No-cache");
        response.setHeader("Cache-Control","No-cache");
        response.setDateHeader("Expires",0);
        try {
            response.getWriter().write(text);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
