package com.tianke.equipmentledger.context;

import com.tianke.equipmentledger.entity.UserLoginInfo;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        /*
        String url = request.getRequestURI();
        //排除登陆及登出的请求，其余的请求需要验证是否处于登陆状态
        if(url.contains("/FindUserInformation") || url.contains("/UserVerification") ||
                url.contains("/UserLogin") || url.contains("/UserOut")){
            return true;
        }else{
            String token = request.getParameter("token");
            UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
            if(null != userInfo){
                return true;
            }else{
                return false;
            }
        }
        */
        System.out.println("拦截器已生效");
        return true;

    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // System.out.println("后置方法执行..");
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // System.out.println("完成方法执行..");
    }

}
