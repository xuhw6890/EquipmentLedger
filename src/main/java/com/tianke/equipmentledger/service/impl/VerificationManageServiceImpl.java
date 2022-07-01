package com.tianke.equipmentledger.service.impl;

import com.tianke.equipmentledger.context.MessageType;
import com.tianke.equipmentledger.context.ReturnResult;
import com.tianke.equipmentledger.context.ToolUtil;
import com.tianke.equipmentledger.dao.*;
import com.tianke.equipmentledger.entity.DataTable;
import com.tianke.equipmentledger.entity.UserLoginInfo;
import com.tianke.equipmentledger.service.VerificationManageService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("verificationManageService")
public class VerificationManageServiceImpl implements VerificationManageService {
    @Resource
    private UserInfoDAO userInfoDAO;
    @Resource
    private VerificationManageDAO verificationManageDAO;
    @Resource
    private SerialNumberDAO serialNumberDAO;
    @Resource
    private EquipmentLedgerDAO equipmentLedgerDAO;
    @Resource
    private OperationRecordDAO operationRecordDAO;


    public ReturnResult AddDeviceVerificationInfo(HttpServletRequest request, HttpServletResponse response){
        ReturnResult result = ReturnResult.error();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        //判断是否为设备管理员，只有设备管理员才有该权限
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        int device_admin = Integer.parseInt(userMap.get("device_admin").toString());
        String department = userMap.get("department").toString();
        if(MessageType.ONE != device_admin){
            result.setMessage("操作失败，您不是设备管理员没有该权限操作");
            return result;
        }
        JSONObject jsonObject = JSONObject.fromObject(request.getParameter("json"));
        //是否需要验证该设备已经存在核查计划，核查计划是否会失效
        Map<String,Object> verificationMap = verificationManageDAO.FindOngoingPlanInfoById2(jsonObject.get("id").toString());
        if(null != verificationMap){
            result.setMessage("该设备已存在核查计划，请勿重复添加");
            return result;
        }
        //保存核查计划信息
        String now_time = ToolUtil.GetNowDateString();
        jsonObject.put("create_time",now_time);
        verificationManageDAO.SaveDeviceVerificationInfo(jsonObject);
        //判断当前设备数据是否有下次计划时间，无则需要将该计划的计划时间代入
        Map<String,Object> ledgerMap = equipmentLedgerDAO.FindEquipmentLedgerById(jsonObject.get("id").toString());
        String next_verification_date = ledgerMap.get("next_verification_date").toString();
        if(MessageType.EMPTYSTR.equals(next_verification_date)){
            equipmentLedgerDAO.UpdateNextVerificationDateById(jsonObject.get("planning_time").toString(),jsonObject.get("id").toString());
        }
        //添加流水号(暂不明确具体需求，流水号暂不生成)
        /*
        String now_year = ToolUtil.GetNowDateYearString();
        int total = serialNumberDAO.FindCurrentYearSerialNumber(now_year);
        Map<String,Object> serial_number_map = new HashMap<>();
        serial_number_map.put("approval_form_id",id);
        serial_number_map.put("user_code",userInfo.getUser_code());
        serial_number_map.put("serial_number",now_year+"-"+ToolUtil.GetStringNumberForValue(total+1, 3));
        serial_number_map.put("create_time",now_time);
        serial_number_map.put("table_status",MessageType.SIX);//审批表状态，0采购申请表，1报废审批表，2维修申请表，3外出申请表,4校准计划表，5校准核查跟踪记录表，6期间核查表
        serialNumberDAO.AddApprovalSerialNumber(serial_number_map);
        */
        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",jsonObject.get("id"));
        operationMap.put("raw_data",MessageType.EMPTYSTR);
        operationMap.put("operation_record","创建了设备编号为："+jsonObject.get("device_id")+"的期间核查计划");
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);
        return ReturnResult.success();


    }

    public ReturnResult FindDeviceVerificationData(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        String finished_status = request.getParameter("finished_status");//计划进行状态,0查看未完成的，1查看已完成的
        JSONArray jsonarray = JSONArray.fromObject(request.getParameter("aoData"));
        DataTable dataTable = ToolUtil.GetDataTableValue(jsonarray);
        int ruleNumber = dataTable.getiSortCol();
        String key_name ="id";
        switch (ruleNumber) {
            case 1: key_name="id";break;
        }
        dataTable.setSortTable(key_name);
        Map<String,Object> mapData = new HashMap<>();
        mapData.put("finished_status",finished_status);
        mapData.put("dataTable",dataTable);
        List<Map<String,Object>> listMap = verificationManageDAO.FindAllDeviceVerificationData(mapData);
        int total = verificationManageDAO.FindAllDeviceVerificationDataTotal(mapData);
        JSONObject getObj = new JSONObject();
        getObj.put("sEcho", dataTable.getsEcho());//
        getObj.put("iTotalRecords", total);//实际的行数
        getObj.put("iTotalDisplayRecords", total);//显示的行数,这个要和上面写的一样
        getObj.put("aaData", listMap);//要以JSON格式返回
        result.setObject(getObj.toString());
        return result;

    }

    @Override
    public ReturnResult ConfirmVerificationPlan(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.error();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        String now_time = ToolUtil.GetNowDateString();
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        //首先判断是否是设备管理员
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        int device_admin = Integer.parseInt(userMap.get("device_admin").toString());
        String department = userMap.get("department").toString();
        if(MessageType.ONE != device_admin){
            result.setMessage("操作失败，您不是设备管理员没有该权限操作");
            return result;
        }
        //判断该计划是否是已完成的状态
        JSONObject jsonObject = JSONObject.fromObject(request.getParameter("json"));
        String id = jsonObject.get("id").toString();
        Map<String,Object> planMap = verificationManageDAO.FindDeviceVerificationInfoById(id);
        if(MessageType.ONE == Integer.parseInt(planMap.get("finished_status").toString())){
            result.setMessage("操作失败，该计划已完成");
            return result;
        }
        //是否存在计划已逾期的情况（线上不会出现）

        //获取该计划核查频次数及已完成核查数
        Map<String,Object> map_data = new HashMap<>();
        //该计划最后一次核查数，完成后需要修改其状态码，并生成一条新的核查计划
        int finished_status = MessageType.ONE;
        map_data.put("id",id);
        map_data.put("executor",jsonObject.get("executor"));
        map_data.put("remark",jsonObject.get("remark"));
        map_data.put("finished_status",finished_status);
        verificationManageDAO.SaveConfirmVerificationPlanInfo(map_data);
        //添加一条新的设备计划 (注：如果用户输入的是空该如何操作)
        JSONObject planJson = JSONObject.fromObject(planMap);
        planJson.put("id",planJson.get("id2"));
        planJson.put("planning_time",jsonObject.get("valid_until"));//计划完成，用户填写该计划下一次的有效截止日期
        planJson.put("create_time",now_time);
        verificationManageDAO.SaveDeviceVerificationInfo(planJson);
        //同步修改设备台账中该设备的期间核查下次校验日期
        Map<String,Object> ledgerMap = equipmentLedgerDAO.FindEquipmentLedgerById(planMap.get("id2").toString());
        JSONObject ledgerJson = JSONObject.fromObject(ledgerMap);
        ledgerJson.put("end_verification_date",ToolUtil.GetNowDateYMDString());
        ledgerJson.put("next_verification_date",jsonObject.get("valid_until"));
        ledgerJson.put("update_user_name",userInfo.getUser_name());
        ledgerJson.put("update_user_code",userInfo.getUser_code());
        ledgerJson.put("update_time",now_time);
        equipmentLedgerDAO.UpdateEquipmentLedgerInfo(ledgerJson);
        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",MessageType.EMPTYSTR);
        operationMap.put("operation_record","确认了id为："+id+"的期间核查计划，并依据下次计划时间，系统自动创建了一条该设备期间核查计划");
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);

        return ReturnResult.success();

    }

}
