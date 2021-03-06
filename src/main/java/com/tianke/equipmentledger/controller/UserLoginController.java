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
        //?????????????????????????????????????????????0201
        if("02".equals(user_code)){
            user_code = "0201";
            dept_name = "??????????????????";
        }
        try {
            user_name = URLDecoder.decode(user_name, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        //????????????????????????????????????appkey
        String appkey = "";
        //appsecret
        String appsecret = "";
        //???????????????sessionId???????????????OA???????????????
        if(FindUserOALoginStatus(sessionId)){
            //String timestamp = jsonObject.get("timestamp").toString();
            Map<String,Object> mapKay = userInfoDAO.FindUserAppKeyByUserCode(user_code);
            if(null != mapKay && null != mapKay.get("appkey")){
                appkey = mapKay.get("appkey").toString();
                appsecret = mapKay.get("appsecret").toString();
            }else{
                //????????????????????????????????????????????????????????????appkey???appsecret
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
                //?????????????????????????????????????????????
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
        //????????????
        ReturnResult result = ReturnResult.success();
        result.setObject(map);
        return result;
        //ResponseUtils.renderJson(response,JSONObject.fromObject(result).toString());

    }

    //??????????????????
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
                //????????????????????????15??????????????????
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
     * ????????????
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
                //????????????,??????
                UserLoginInfo userLoginInfo = new UserLoginInfo();
                userLoginInfo.setId(Integer.parseInt(user_info_map.get("id").toString()));
                userLoginInfo.setUser_code(user_info_map.get("user_code").toString());
                userLoginInfo.setUser_name(user_info_map.get("user_name").toString());
                userLoginInfo.setDept_code(user_info_map.get("dept_code").toString());
                userLoginInfo.setUser_rights(Integer.parseInt(user_info_map.get("user_rights").toString()));
                userLoginInfo.setJob_title_status(Integer.parseInt(user_info_map.get("job_title_status").toString()));
                //String token = ToolUtil.GenerateToken(user_info_map.get("user_code").toString());

                //??????????????????????????????????????????token????????????
                String token = "6F0908B178B1F444395E4F94D1110192";
                if("TK03019".equals(user_info_map.get("user_code"))){
                    token = "55A62487185964EC044E515497A00F7A";
                }

                ToolUtil.setUserToken(token,userLoginInfo);
                //??????????????????????????????
                Map<String,Object> operationMap = new HashMap<>();
                operationMap.put("id2",MessageType.EMPTYSTR);
                operationMap.put("raw_data",MessageType.EMPTYSTR);
                operationMap.put("operation_record","??????????????????????????????");
                operationMap.put("user_code",user_info_map.get("user_code"));
                operationMap.put("user_name",user_info_map.get("user_name"));
                operationMap.put("create_time",ToolUtil.GetNowDateString());
                operationRecordDAO.SaveUserOperationRecord(operationMap);
                ReturnResult result = ReturnResult.success();
                result.setObject(token);
                return result;
                //ResponseUtils.renderJson(response,JSONObject.fromObject(result).toString());
            }else{
                //???????????????
                ReturnResult result = ReturnResult.error();
                result.setMessage("??????????????????????????????");
                return result;
                //ResponseUtils.renderJson(response,JSONObject.fromObject(result).toString());
            }
        }else{
            ReturnResult result = ReturnResult.error();
            result.setMessage("????????????????????????OA?????????");
            return result;
        }

    }

    /**
     * ??????????????????
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
        //System.out.println("???????????? : " + new HexBinaryAdapter().marshal(encryptedData));
        return encryptedData;
    }

    /**
     * ???????????????????????????????????????????????????
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
     * ?????????????????????OA?????????
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
