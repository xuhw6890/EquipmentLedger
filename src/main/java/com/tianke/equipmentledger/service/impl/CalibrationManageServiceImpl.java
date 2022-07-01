package com.tianke.equipmentledger.service.impl;

import com.tianke.equipmentledger.context.MessageType;
import com.tianke.equipmentledger.context.ResponseUtils;
import com.tianke.equipmentledger.context.ReturnResult;
import com.tianke.equipmentledger.context.ToolUtil;
import com.tianke.equipmentledger.dao.*;
import com.tianke.equipmentledger.entity.DataTable;
import com.tianke.equipmentledger.entity.UserLoginInfo;
import com.tianke.equipmentledger.service.CalibrationManageService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("calibrationManageService")
public class CalibrationManageServiceImpl implements CalibrationManageService {
    @Resource
    private CalibrationManageDAO calibrationManageDAO;
    @Resource
    private UserInfoDAO userInfoDAO;
    @Resource
    private SerialNumberDAO serialNumberDAO;
    @Resource
    private EquipmentLedgerDAO equipmentLedgerDAO;
    @Resource
    private OperationRecordDAO operationRecordDAO;

    @Override
    public ReturnResult CreateCalibrationPlan(HttpServletRequest request, HttpServletResponse response){
        //前端需要验证用户填写的校准信息是否完善，不得为空
        ReturnResult result = ReturnResult.error();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        //判断该操作人员是否有创建设备校准计划的权限即是否为设备管理员
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        String department = userMap.get("department").toString();
        int device_admin = Integer.parseInt(userMap.get("device_admin").toString());
        if(MessageType.ONE != device_admin){
            result.setMessage("操作失败，您没有该权限，请联系设备管理员进行操作");
            return result;
        }
        JSONObject jsonObject = JSONObject.fromObject(request.getParameter("json"));
        String id2 = jsonObject.get("id").toString();
        //创建校准计划前需要确认该设备当前没有正在进行的校准计划
        Map<String,Object> mapData = calibrationManageDAO.FindOngoingCalibrationProgramById2(id2);
        if(null != mapData){
            result.setMessage("操作失败，该设备当前有正在进行的校准计划");
            return result;
        }
        //开始创建校准计划
        String now_time = ToolUtil.GetNowDateString();
        jsonObject.put("create_user_name",userInfo.getUser_name());
        jsonObject.put("create_user_code",userInfo.getUser_code());
        jsonObject.put("create_time",now_time);
        calibrationManageDAO.SaveCalibrationProgramInfo(jsonObject);
        /*
        //添加流水号
        String now_year = ToolUtil.GetNowDateYearString();
        int total = serialNumberDAO.FindCurrentYearSerialNumber(now_year);
        //获取该校准计划的id
        Map<String,Object> map_data = calibrationManageDAO.FindCalibrationPlanDataById2(id2);
        Map<String,Object> serial_number_map = new HashMap<>();
        serial_number_map.put("approval_form_id",map_data.get("id"));
        serial_number_map.put("user_code",userInfo.getUser_code());
        serial_number_map.put("serial_number",now_year+"-"+ToolUtil.GetStringNumberForValue(total+1, 3));
        serial_number_map.put("create_time",now_time);
        serial_number_map.put("table_status",MessageType.FOUR);//审批表状态，0采购申请表，1报废审批表，2维修申请表，3外出申请表,4校准计划表
        serialNumberDAO.AddApprovalSerialNumber(serial_number_map);
        result = ReturnResult.success();
         */
        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",id2);
        operationMap.put("raw_data",MessageType.EMPTYSTR);
        operationMap.put("operation_record","提交了设备编号为："+jsonObject.get("device_id")+"的校准计划");
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);
        return ReturnResult.success();
    }

    @Override
    public ReturnResult FindCreateCalibrationPlanData(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        JSONArray jsonarray = JSONArray.fromObject(request.getParameter("aoData"));
        DataTable dataTable = ToolUtil.GetDataTableValue(jsonarray);
        int ruleNumber = dataTable.getiSortCol();
        String key_name ="id";
        switch (ruleNumber) {
            case 1: key_name="id";break;
        }
        dataTable.setSortTable(key_name);
        List<Map<String,Object>> listMap = calibrationManageDAO.FindAllCalibrationPlanData(dataTable);
        int total = calibrationManageDAO.FindAllCalibrationPlanDataTotal(dataTable);
        JSONObject getObj = new JSONObject();
        getObj.put("sEcho", dataTable.getsEcho());//
        getObj.put("iTotalRecords", total);//实际的行数
        getObj.put("iTotalDisplayRecords", total);//显示的行数,这个要和上面写的一样
        getObj.put("aaData", listMap);//要以JSON格式返回
        result.setObject(getObj.toString());
        return result;

    }

    @Override
    public ReturnResult FillCalibrationResult(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.error();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        //判断是否为设备管理员
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        int device_admin = Integer.parseInt(userMap.get("device_admin").toString());
        String department = userMap.get("department").toString();
        if(MessageType.ONE != device_admin){
            result.setMessage("操作失败，您没有该权限，请联系设备管理员进行操作");
            return result;
        }
        JSONObject jsonObject = JSONObject.fromObject(request.getParameter("json"));
        //判断校准计划是否处于当前填写状态
        String id = jsonObject.get("id").toString();
        Map<String,Object> mapData = calibrationManageDAO.FindOngoingCalibrationProgramById(id);
        int planning_process = Integer.parseInt(mapData.get("planning_process").toString());
        if(MessageType.ZERO != planning_process){
            result.setMessage("操作失败，该校准计划当前不处于该步骤");
            return result;
        }
        String now_time = ToolUtil.GetNowDateString();
        jsonObject.put("register_person",userInfo.getUser_name());
        jsonObject.put("register_person_code",userInfo.getUser_code());
        jsonObject.put("check_in_time",now_time);
        jsonObject.put("planning_process",MessageType.ONE);//校准计划进度
        calibrationManageDAO.SaveCalibrationConfirmationResult(jsonObject);
        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",MessageType.EMPTYSTR);
        operationMap.put("operation_record","填写了校准计划id为："+id+"的校准后的数据");
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);
        return ReturnResult.success();
    }

    @Override
    public ReturnResult ReviewCalibrationResult(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.error();
        String token = request.getParameter("token");
        String id = request.getParameter("id").toString();
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        //判断是否为设备使用人员，只有设备的使用人员才有复核的权限
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        String department = userMap.get("department").toString();
        //判断校准计划是否处于当前待复核状态
        Map<String,Object> mapData = calibrationManageDAO.FindOngoingCalibrationProgramById(id);
        //依据设备设备使用部门判断该用户是否属于该部门，不是则没有权限操作
        String use_department = mapData.get("use_department").toString();
        Boolean bool = false;
        if(!department.equals(use_department)){
            bool = true;
        }
        if(bool){
            result.setMessage("操作失败，您不是该设备的使用人员，没有该权限复核");
            return result;
        }

        int planning_process = Integer.parseInt(mapData.get("planning_process").toString());
        if(MessageType.ONE != planning_process){
            result.setMessage("操作失败，该校准计划当前不处于该步骤");
            return result;
        }
        String now_time = ToolUtil.GetNowDateString();
        Map<String,Object> map_data = new HashMap<>();
        map_data.put("id",id);
        map_data.put("confirmor",userInfo.getUser_name());
        map_data.put("confirmor_code",userInfo.getUser_code());
        map_data.put("confirm_time",now_time);
        map_data.put("planning_process",MessageType.TWO);//校准计划进度
        calibrationManageDAO.ReviewCalibrationResult(map_data);
        //校准完成后需要将下一次校准日期同步至台账该设备中
        Map<String,Object> ledgerMap = equipmentLedgerDAO.FindEquipmentLedgerById(mapData.get("id2").toString());
        JSONObject ledgerJson = JSONObject.fromObject(ledgerMap);
        ledgerJson.put("end_calibration_date",mapData.get("complete_time"));
        ledgerJson.put("next_calibration_date",mapData.get("next_date"));
        ledgerJson.put("update_user_name",userInfo.getUser_name());
        ledgerJson.put("update_user_code",userInfo.getUser_code());
        ledgerJson.put("update_time",now_time);
        equipmentLedgerDAO.UpdateEquipmentLedgerInfo(ledgerJson);
        /*
        //添加流水号
        String now_year = ToolUtil.GetNowDateYearString();
        int total = serialNumberDAO.FindCurrentYearSerialNumber(now_year);
        Map<String,Object> serial_number_map = new HashMap<>();
        serial_number_map.put("approval_form_id",id);
        serial_number_map.put("user_code",userInfo.getUser_code());
        serial_number_map.put("serial_number",now_year+"-"+ToolUtil.GetStringNumberForValue(total+1, 3));
        serial_number_map.put("create_time",now_time);
        serial_number_map.put("table_status",MessageType.FIVE);//审批表状态，0采购申请表，1报废审批表，2维修申请表，3外出申请表,4校准计划表，5校准核查跟踪记录表
        serialNumberDAO.AddApprovalSerialNumber(serial_number_map);
         */
        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",mapData.get("id2"));
        operationMap.put("raw_data",MessageType.EMPTYSTR);
        operationMap.put("operation_record","确认了校准计划id为："+id+"的校准结果");
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);
        return ReturnResult.success();

    }

}
