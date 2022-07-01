package com.tianke.equipmentledger.context;

import com.tianke.equipmentledger.entity.UserLoginInfo;

import java.util.concurrent.ConcurrentHashMap;

public class LoginSessionCheck {
    public static ConcurrentHashMap<String, UserLoginInfo> tokenSession = new ConcurrentHashMap<>();

    /**
     * 删除
     * @param token
     */
    public static void removeToken(String token){
        if(null != tokenSession.get(token)){
            if(tokenSession.containsKey(token)){
                tokenSession.remove(token);
            }
        }
    }

    /**
     * 添加
     * @param token
     * @param userLoginInfo
     */
    public static void AddToken(String token,UserLoginInfo userLoginInfo){
        tokenSession.put(token,userLoginInfo);
    }

}
