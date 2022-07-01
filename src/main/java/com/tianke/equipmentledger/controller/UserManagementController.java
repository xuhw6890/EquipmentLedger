package com.tianke.equipmentledger.controller;

import com.tianke.equipmentledger.context.MessageType;
import com.tianke.equipmentledger.context.ResponseUtils;
import com.tianke.equipmentledger.context.ReturnResult;
import com.tianke.equipmentledger.context.ToolUtil;
import com.tianke.equipmentledger.dao.OperationRecordDAO;
import com.tianke.equipmentledger.dao.UserInfoDAO;
import com.tianke.equipmentledger.entity.UserLoginInfo;
import net.sf.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/Manage")
public class UserManagementController {
    @Resource
    private UserInfoDAO userInfoDAO;
    @Resource
    private OperationRecordDAO operationRecordDAO;

    /**
     * 用户管理，获取所有的用户信息
     * @param request
     * @return
     */
    @GetMapping("/FindAllUserInfo")
    public ReturnResult FindAllUserInfo(HttpServletRequest request){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        List<Map<String,Object>> userList = userInfoDAO.FindAllUserInfo();
        /*
        //处理一下职位信息，将状态码转换成中文职位
        List<Map<String,Object>> jobList = userInfoDAO.FindCompanyJobInfo(MessageType.EMPTYSTR);
        Map<String,String> jobMap = new HashMap<>();
        for(Map<String,Object> map : jobList){
            jobMap.put(map.get("id").toString(),map.get("job_title").toString());
        }
        for(Map<String,Object> map : userList){
            map.put("job_title",jobMap.get(map.get("job_title_status").toString()));
        }
        */
        for(Map<String,Object> map : userList){
            if(MessageType.ZERO == Integer.parseInt(map.get("job_title_status").toString())){
                map.put("job_title","员工");
            }else{
                map.put("job_title","负责人");
            }
        }
        result.setObject(userList);
        return result;
    }

    /**
     * 调整用户权限（调整或删除停用）
     * @param request
     * @return
     */
    @GetMapping("/AdjustUserPermissions")
    public ReturnResult AdjustUserPermissions(HttpServletRequest request){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        if(MessageType.ONE != Integer.parseInt(userMap.get("user_rights").toString())){
            result = ReturnResult.error();
            result.setMessage("操作失败，您没有该权限");
            return result;
        }
        JSONObject jsonObject = JSONObject.fromObject(request.getParameter("json"));
        Map<String,Object> OldUserMap = userInfoDAO.FindUserInfoById(jsonObject.get("id").toString());
        //获取当前操作是账号停用还是权限修改
        String operation_type = jsonObject.get("operation_type").toString();//操作类型,1修改，2删除
        String operation_record = MessageType.EMPTYSTR;
        if(MessageType.STRONE.equals(operation_type)){
            //修改
            //调整一下审批权限顺序
            String approval_authority = jsonObject.get("approval_authority").toString();
            if(!MessageType.EMPTYSTR.equals(approval_authority)){
                if(approval_authority.split(",").length > 1){
                    String[] approval_authority_arr = ToolUtil.NumericalSorting(approval_authority.split(","));
                    String new_approval_authority = MessageType.EMPTYSTR;
                    for(int i = 0;i<approval_authority_arr.length;i++){
                        if(i == approval_authority_arr.length-1){
                            new_approval_authority += approval_authority_arr[i];
                        }else {
                            new_approval_authority += approval_authority_arr[i]+",";
                        }
                    }
                    jsonObject.put("approval_authority",new_approval_authority);
                }
            }
            userInfoDAO.UpdateUserRightsInfo(jsonObject);
            operation_record = "修改了用户："+OldUserMap.get("user_name")+"的操作权限";
        }else{
            userInfoDAO.DeleteUserInfo(jsonObject.get("id").toString());
            operation_record = "删除/停用了用户："+OldUserMap.get("user_name");
        }
        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",JSONObject.fromObject(OldUserMap).toString());
        operationMap.put("operation_record",operation_record);
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",ToolUtil.GetNowDateString());
        operationRecordDAO.SaveUserOperationRecord(operationMap);
        return result;
    }

    /**
     * 获取部门架构及审批架构
     * @param request
     * @return
     */
    @GetMapping("/FindJobiInfo")
    public ReturnResult FindJobiInfo(HttpServletRequest request){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        /*
        String approval_type = request.getParameter("approval_type");//审批权限的职位
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        List<Map<String,Object>> jobList = userInfoDAO.FindCompanyJobInfo(approval_type);
        result.setObject(jobList);
         */
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        //获取部门架构
        List<Map<String,Object>> dept_list = userInfoDAO.FindDepartmentalStructure();
        //获取审批架构
        List<Map<String,Object>> approval_list = userInfoDAO.FindApprovalFramework();
        Map<String,Object> mapData = new HashMap<>();
        mapData.put("dept_list",dept_list);
        mapData.put("approval_list",approval_list);
        result.setObject(mapData);
        return result;
    }

}
