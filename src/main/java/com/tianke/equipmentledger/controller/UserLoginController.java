package com.tianke.equipmentledger.controller;

import com.tianke.equipmentledger.context.*;
import com.tianke.equipmentledger.dao.OperationRecordDAO;
import com.tianke.equipmentledger.dao.UserInfoDAO;
import com.tianke.equipmentledger.entity.UserLoginInfo;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/Login")
public class UserLoginController {
    @Resource
    private UserInfoDAO userInfoDAO;
    @Resource
    private OperationRecordDAO operationRecordDAO;

    @Value("${emp.login.verification}")
    private String oa_login_verification;


    @GetMapping("/FindUserInformation")
    public ReturnResult VerifyUserInformation(HttpServletRequest request, HttpServletResponse response){
        JSONObject jsonObject = JSONObject.fromObject(request.getParameter("json"));
        String sessionId = jsonObject.get("sessionId").toString();
        String user_code = jsonObject.get("user_code").toString();
        String user_name = jsonObject.get("user_name").toString();
        String dept_code = jsonObject.get("dept_code").toString();
        String dept_name = jsonObject.get("dept_name").toString();
        String user_phone = jsonObject.get("user_phone").toString();
        String user_signature = jsonObject.get("user_signature").toString();
        //处理实验部的部门编号，默认都为0201
        if("02".equals(user_code)){
            user_code = "0201";
            dept_name = "微生物检测室";
        }
        try {
            user_name = URLDecoder.decode(user_name, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        //根据用户账号查询该用户的appkey
        String appkey = "";
        //appsecret
        String appsecret = "";
        //根据用户的sessionId判断是否在OA项目中登陆
        if(FindUserOALoginStatus(sessionId)){
            //String timestamp = jsonObject.get("timestamp").toString();
            Map<String,Object> mapKay = userInfoDAO.FindUserAppKeyByUserCode(user_code);
            if(null != mapKay && null != mapKay.get("appkey")){
                appkey = mapKay.get("appkey").toString();
                appsecret = mapKay.get("appsecret").toString();
            }else{
                //不存在则代表是第一次访问，需要生成对应的appkey及appsecret
                try {
                    appkey = Hex.encodeHexString(generateDESKey());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String str = UUID.randomUUID().toString().substring(0,14);
                try {
                    appsecret = Hex.encodeHexString(encryptByDES(generateDESKey(),str)).toUpperCase();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //将该用户访问的验证秘钥进行保存
                Map<String, Object> mapData = new HashMap<>();
                mapData.put("user_code", user_code);
                mapData.put("user_name", user_name);
                mapData.put("dept_code",dept_code);
                mapData.put("department",dept_name);
                mapData.put("user_phone",user_phone);
                mapData.put("user_signature",user_signature);
                mapData.put("appkey", appkey);
                mapData.put("appsecret", appsecret);
                mapData.put("create_time", ToolUtil.GetNowDateString());
                userInfoDAO.AddLegderUserInfo(mapData);
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("appkey", appkey);
        map.put("appsecret", appsecret);
        //返回数据
        ReturnResult result = ReturnResult.success();
        result.setObject(map);
        return result;
        //ResponseUtils.renderJson(response,JSONObject.fromObject(result).toString());

    }

    //无用，暂作废
    @GetMapping("/UserVerification")
    public ReturnResult UserLoginVerification(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        JSONObject jsonObject = JSONObject.fromObject(request.getParameter("json"));
        String token = jsonObject.get("token").toString();
        String appkey = jsonObject.getString("appkey");
        String appsecret = jsonObject.getString("appsecret");
        String user_code = jsonObject.getString("user_code");
        String user_name = jsonObject.getString("user_name");
        String timestamp = jsonObject.getString("timestamp");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null != userInfo){
            ResponseUtils.renderJson(response,JSONObject.fromObject(result).toString());
        }
        Map<String,Object> mapData = userInfoDAO.FindUserInfo(appkey);
        if(null != mapData){
            String sign = DigestUtils.md5Hex(mapData.get("appsecret").toString()+"user_code"+user_code+"user_name"+user_name+"timestamp"+timestamp);
            if(appsecret.equals(sign)){
                //判断时间戳，大于15秒证明已失效
                Long s = (System.currentTimeMillis() - Long.valueOf(timestamp))/1000;
                if(s > 15){
                    result = ReturnResult.error();
                }
            }else{
                result = ReturnResult.error();
            }
            return result;
            //ResponseUtils.renderJson(response,JSONObject.fromObject(result).toString());

        }else{
            result = ReturnResult.error();
            return result;
            //ResponseUtils.renderJson(response,JSONObject.fromObject(result).toString());
        }

    }

    /**
     * 用户登陆
     * @param request
     * @param response
     */
    @GetMapping("/UserLogin")
    public ReturnResult UserLogin(HttpServletRequest request,HttpServletResponse response){
        String appkey = request.getParameter("appkey");
        String sessionId = request.getParameter("sessionId");
        if(true){//FindUserOALoginStatus(sessionId)
            Map<String,Object> user_info_map = userInfoDAO.FindUserInfo(appkey);
            if(null != user_info_map){
                //用户存在,判断
                UserLoginInfo userLoginInfo = new UserLoginInfo();
                userLoginInfo.setId(Integer.parseInt(user_info_map.get("id").toString()));
                userLoginInfo.setUser_code(user_info_map.get("user_code").toString());
                userLoginInfo.setUser_name(user_info_map.get("user_name").toString());
                userLoginInfo.setDept_code(user_info_map.get("dept_code").toString());
                userLoginInfo.setUser_rights(Integer.parseInt(user_info_map.get("user_rights").toString()));
                userLoginInfo.setJob_title_status(Integer.parseInt(user_info_map.get("job_title_status").toString()));
                //String token = ToolUtil.GenerateToken(user_info_map.get("user_code").toString());

                //对接阶段，只用一个用户对接，token暂时写死
                String token = "6F0908B178B1F444395E4F94D1110192";
                if("TK03019".equals(user_info_map.get("user_code"))){
                    token = "55A62487185964EC044E515497A00F7A";
                }

                ToolUtil.setUserToken(token,userLoginInfo);
                //添加用户登陆操作记录
                Map<String,Object> operationMap = new HashMap<>();
                operationMap.put("id2",MessageType.EMPTYSTR);
                operationMap.put("raw_data",MessageType.EMPTYSTR);
                operationMap.put("operation_record","用户登陆设备台账系统");
                operationMap.put("user_code",user_info_map.get("user_code"));
                operationMap.put("user_name",user_info_map.get("user_name"));
                operationMap.put("create_time",ToolUtil.GetNowDateString());
                operationRecordDAO.SaveUserOperationRecord(operationMap);
                ReturnResult result = ReturnResult.success();
                result.setObject(token);
                return result;
                //ResponseUtils.renderJson(response,JSONObject.fromObject(result).toString());
            }else{
                //用户不存在
                ReturnResult result = ReturnResult.error();
                result.setMessage("登陆失败，没有该用户");
                return result;
                //ResponseUtils.renderJson(response,JSONObject.fromObject(result).toString());
            }
        }else{
            ReturnResult result = ReturnResult.error();
            result.setMessage("登陆失败，该用户OA未登陆");
            return result;
        }

    }

    /**
     * 用户退出登陆
     * @param request
     * @param response
     */
    @GetMapping("/UserOut")
    public ReturnResult UserOut(HttpServletRequest request,HttpServletResponse response){
        String token = request.getParameter("token");
        LoginSessionCheck.removeToken(token);
        ReturnResult result = ReturnResult.success();
        return result;
        //ResponseUtils.renderJson(response,JSONObject.fromObject(result).toString());
    }


    public static byte[] generateDESKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("DES");
        keyGenerator.init(56);
        SecretKey secretKey = keyGenerator.generateKey();
        byte[] encodedKey = secretKey.getEncoded();
        // String encodeHexString = Hex.encodeHexString(encodedKey);
        //System.err.println(Hex.encodeHexString(encodedKey));
        return encodedKey;
    }

    public static byte[] encryptByDES(byte[] encodedKey,String dataBytes) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(encodedKey, "DES");
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedData = cipher.doFinal(dataBytes.getBytes());
        //System.out.println("加密结果 : " + new HexBinaryAdapter().marshal(encryptedData));
        return encryptedData;
    }

    /**
     * 获取用户权限，权限不同展示模块不同
     * @param request
     * @return
     */
    @GetMapping("/FindUserRightsInfo")
    public ReturnResult FindUserRightsInfo(HttpServletRequest request){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
           return result;
        }
        String user_code = userInfo.getUser_code();
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(user_code);
        result.setObject(userMap);
        return result;
    }

    /**
     * 验证用户是否在OA端登陆
     * @param sessionId
     * @return
     */
    public Boolean FindUserOALoginStatus(String sessionId){
        Boolean bool = false;
        String url = oa_login_verification;
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(sessionId);
        String result = CallInterface.postJsonTest(url,jsonArray);//json.toString()
        //System.out.println("result:"+result);
        if(null != result && !"".equals(result)){
            JSONObject jsonObject = JSONObject.fromObject(result);
            if(null != jsonObject.get("object") && !"".equals(jsonObject.get("object"))){
                JSONObject json = JSONObject.fromObject(jsonObject.get("object"));
                if(0 == Integer.parseInt(json.get(sessionId).toString())){
                    bool = true;
                }
            }
        }
        return bool;
    }


}
