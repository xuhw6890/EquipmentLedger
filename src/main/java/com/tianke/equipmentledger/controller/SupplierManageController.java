package com.tianke.equipmentledger.controller;

import com.tianke.equipmentledger.context.MessageType;
import com.tianke.equipmentledger.context.ReturnResult;
import com.tianke.equipmentledger.context.ToolUtil;
import com.tianke.equipmentledger.dao.OperationRecordDAO;
import com.tianke.equipmentledger.dao.SupplierManageDAO;
import com.tianke.equipmentledger.entity.UserLoginInfo;
import net.sf.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/Supplier")
public class SupplierManageController {
    @Resource
    private SupplierManageDAO supplierManageDAO;
    @Resource
    private OperationRecordDAO operationRecordDAO;

    /**
     * 保存供应商信息
     * @param request
     * @param response
     */
    @GetMapping("/SaveSupplierCompanyInfo")
    public ReturnResult SaveSupplierCompanyInfo(HttpServletRequest request, HttpServletResponse response){
        ReturnResult result = ReturnResult.error();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        JSONObject jsonObject = JSONObject.fromObject(request.getParameter("json"));
        String operation_type = request.getParameter("operation_type");//操作类型，0添加，1修改
        String now_time = ToolUtil.GetNowDateString();
        //添加供应商是否需要特定人员进行添加
        String company_name = jsonObject.get("company_name").toString();
        if(MessageType.STRZERO.equals(operation_type)){
            //判断新增的公司是否已存在
            Map<String,Object> companyInfo = supplierManageDAO.FindSupplierCompanyInfoByName(company_name);
            if(null != companyInfo){
                result.setMessage("操作失败，该公司已存在，请检查");
                return result;
            }
            jsonObject.put("create_time",now_time);
            supplierManageDAO.SaveSupplierCompanyInfo(jsonObject);
            //保存用户操作记录
            Map<String,Object> operationMap = new HashMap<>();
            operationMap.put("id2",MessageType.EMPTYSTR);
            operationMap.put("raw_data",MessageType.EMPTYSTR);
            operationMap.put("operation_record","添加了供应商："+company_name);
            operationMap.put("user_code",userInfo.getUser_code());
            operationMap.put("user_name",userInfo.getUser_name());
            operationMap.put("create_time",ToolUtil.GetNowDateString());
            operationRecordDAO.SaveUserOperationRecord(operationMap);
            //添加成功后需要返回该对应id号用做上传资质文件参数
            Map<String,Object> companyMap = supplierManageDAO.FindSupplierCompanyInfoByName(company_name);
            result = ReturnResult.success();
            result.setObject(companyMap);
            return result;
        }else{
            //修改
            int total = supplierManageDAO.FindCompanyNameTotal(company_name);
            if(total > 1){
                result.setMessage("操作失败，该公司名已存在，请检查");
                return result;
            }
            Map<String,Object> companyMap = supplierManageDAO.FindSupplierCompanyInfoById(jsonObject.get("id").toString());
            supplierManageDAO.UpdateSupplierCompanyData(jsonObject);
            //保存用户操作记录
            Map<String,Object> operationMap = new HashMap<>();
            operationMap.put("id2",MessageType.EMPTYSTR);
            operationMap.put("raw_data",JSONObject.fromObject(companyMap).toString());
            operationMap.put("operation_record","添加了供应商："+company_name);
            operationMap.put("user_code",userInfo.getUser_code());
            operationMap.put("user_name",userInfo.getUser_name());
            operationMap.put("create_time",ToolUtil.GetNowDateString());
            operationRecordDAO.SaveUserOperationRecord(operationMap);
        }
        return ReturnResult.success();

    }

    /**
     * 展示所有符合条件的供应商
     * @param request
     * @param response
     */
    @GetMapping("/FindSupplierCompanyData")
    public ReturnResult FindSupplierCompanyData(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        String search_info = request.getParameter("search_info");
        /*
        try{
            search_info = URLDecoder.decode(jsonObject.get("search_info").toString(),"UTF-8");
        }catch (Exception e){
            //e.printStackTrace();
        }
         */
        List<Map<String,Object>> listInfo = supplierManageDAO.FindSupplierCompanyData(search_info);
        result.setObject(listInfo);
        return result;

    }

    /**
     * 删除供应商
     * @param request
     * @param response
     * @return
     */
    @GetMapping("/DeleteSupplierCompanyInfo")
    public ReturnResult DeleteSupplierCompanyInfo(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        String id = request.getParameter("id");
        String company_name = request.getParameter("company_name");
        supplierManageDAO.DeleteSupplierCompanyData(id);
        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",MessageType.EMPTYSTR);
        operationMap.put("operation_record","删除了供应商："+company_name);
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",ToolUtil.GetNowDateString());
        operationRecordDAO.SaveUserOperationRecord(operationMap);
        return result;
    }

    @GetMapping("/FindSupplierNameData")
    public ReturnResult FindSupplierNameData(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        List<String> company_name_list = supplierManageDAO.FindSupplierNameAll();
        result.setObject(company_name_list);
        return result;
    }

}
