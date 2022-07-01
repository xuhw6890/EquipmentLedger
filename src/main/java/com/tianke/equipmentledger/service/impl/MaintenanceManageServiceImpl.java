package com.tianke.equipmentledger.service.impl;

import com.tianke.equipmentledger.context.MessageType;
import com.tianke.equipmentledger.context.ResponseUtils;
import com.tianke.equipmentledger.context.ReturnResult;
import com.tianke.equipmentledger.context.ToolUtil;
import com.tianke.equipmentledger.dao.*;
import com.tianke.equipmentledger.entity.DataTable;
import com.tianke.equipmentledger.entity.UserLoginInfo;
import com.tianke.equipmentledger.service.MaintenanceManageService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("MmintenanceManageService")
public class MaintenanceManageServiceImpl implements MaintenanceManageService {
    @Resource
    private MaintainApprovalDAO maintainApprovalDAO;
    @Resource
    private UserInfoDAO userInfoDAO;
    @Resource
    private EquipmentLedgerDAO equipmentLedgerDAO;
    @Resource
    private MaintenanceInsuranceDAO maintenanceInsuranceDAO;
    @Resource
    private OperationRecordDAO operationRecordDAO;

    @Override
    public ReturnResult ApplyEquipmentRepair(HttpServletRequest request, HttpServletResponse response){
        ReturnResult result = ReturnResult.error();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        JSONObject jsonObject = JSONObject.fromObject(request.getParameter("json"));
        //首先查看该设备当前是否存在已申请的记录
        Map<String,Object> maintainMap = maintainApprovalDAO.FindMaintainApprovalInfoById2(jsonObject.get("id").toString());
        if(null != maintainMap){
            result.setMessage("操作失败，该设备已存在维修申请，请勿重复申请");
            return result;
        }
        //判断故障现象及故障原因是否填写，不能为空
        if( MessageType.EMPTYSTR.equals(jsonObject.get("fault_description")) ||
                 MessageType.EMPTYSTR.equals(jsonObject.get("fault_reason"))){
            result.setMessage("操作失败，故障现象及故障原因不能为空");
            return result;
        }
        //依据设备使用部门获取当前该部门负责人
        //依据设备使用部门，判断申请人是否是该部门负责人
        boolean bool = false;
        String use_department = jsonObject.get("use_department").toString();
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        String department = userMap.get("department").toString();
        int job_title_status = Integer.parseInt(userMap.get("job_title_status").toString());
        if(!use_department.equals(department) || MessageType.ONE != job_title_status){
            bool = true;
        }
        if(bool){
            result.setMessage("操作失败，您不是该设备使用部门负责人，没有该操作权限");
            return result;
        }
        //保存设备维修申请基本信息
        String now_time = ToolUtil.GetNowDateString();
        jsonObject.put("department_head",userInfo.getUser_name());
        jsonObject.put("create_user_name",userInfo.getUser_name());
        jsonObject.put("create_user_code",userInfo.getUser_code());
        jsonObject.put("create_time",now_time);
        jsonObject.put("back_reason",MessageType.EMPTYSTR);
        maintainApprovalDAO.AddMaintainApprovalInfo(jsonObject);
        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",jsonObject.get("id"));
        operationMap.put("raw_data",MessageType.EMPTYSTR);
        operationMap.put("operation_record","提交了设备编号为："+jsonObject.get("device_id")+"的维修申请");
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);
        return ReturnResult.success();
    }

    @Override
    public ReturnResult FindSubimtRepairRequestData(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        List<Map<String,Object>> listMap = new ArrayList<>();
        //判断用户是否是设备管理员，每个部门都设有设备管理员，故只能查看自己部门的维修申请
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        int device_admin = Integer.parseInt(userMap.get("device_admin").toString());
        String department = userMap.get("department").toString();
        String dept_code = MessageType.EMPTYSTR;
        if(MessageType.ZERO == device_admin){
            result.setObject(listMap);
            return result;
        }
        //判断该部门是否是综合管理部，综合管理部可查看整个实验部的数据
        if("综合管理部".equals(department)){
            dept_code = "02";
        }
        Map<String,Object> map_data = new HashMap<>();
        map_data.put("use_department",department);
        map_data.put("dept_code",dept_code);
        listMap = maintainApprovalDAO.FindSubimtRepairRequestData(map_data);
        result.setObject(listMap);
        return result;
    }

    @Override
    public synchronized ReturnResult FillEquipmentRepairInfo(HttpServletRequest request,HttpServletResponse response){
        //前端需要注意，判断一下维修方式、维修项目、维修费用、预计完成时间不能为空
        ReturnResult result = ReturnResult.error();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        //判断用户是否是设备管理员
        Map<String,Object> info = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        int device_admin = Integer.parseInt(info.get("device_admin").toString());
        if(MessageType.ONE != device_admin){
            result.setMessage("操作失败，您没有该权限，请联系设备管理员进行操作");
            return result;
        }
        JSONObject jsonObject = JSONObject.fromObject(request.getParameter("json"));
        //判断该申请是否处于该步骤
        Map<String,Object> maintainMap = maintainApprovalDAO.FindMaintainApprovalInfoById(jsonObject.get("id").toString());
        int application_steps = Integer.parseInt(maintainMap.get("application_steps").toString());
        if(application_steps > MessageType.TWO){//进入审批阶段即不能再修改相关信息
            result.setMessage("操作失败，该申请当前不属于该操作步骤，请检查");
            return result;
        }
        //填写维修情况
        String now_time = ToolUtil.GetNowDateString();
        jsonObject.put("application_steps",MessageType.TWO);
        jsonObject.put("general_management_leader",userInfo.getUser_name());
        jsonObject.put("general_management_leader_code",userInfo.getUser_code());
        jsonObject.put("general_management_leader_time",now_time);
        maintainApprovalDAO.UpdateMaintainApprovalInfo(jsonObject);
        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",MessageType.EMPTYSTR);
        operationMap.put("operation_record","补充了id为："+jsonObject.get("id")+"的维修申请中维修情况说明信息");
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);
        return ReturnResult.success();
    }

    @Override
    public ReturnResult FindRepairRequestData(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            return ReturnResult.NotExist();
        }
        //获取设备维修申请数据，什么审批权限就看那些需要审批的信息
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        String approval_authority = userMap.get("approval_authority").toString();
        List<Map<String,Object>> listMap = new ArrayList<>();
        if(!MessageType.EMPTYSTR.equals(approval_authority)){
            List<Integer> application_steps_list = new ArrayList<>();
            if(approval_authority.contains(MessageType.STRSIX)){
                //副总经理
                application_steps_list.add(MessageType.TWO);
            }
            if(approval_authority.contains(MessageType.STRSEVEN)){
                //总经理
                application_steps_list.add(MessageType.THREE);
            }
            if(application_steps_list.size() > 0){
                listMap = maintainApprovalDAO.FindRepairRequestData(application_steps_list);
            }

        }
        result.setObject(listMap);
        return result;
    }

    @Transactional(rollbackFor = Exception.class)
    public synchronized ReturnResult ReviewRepairApproval(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.error();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            return ReturnResult.NotExist();
        }
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        String approval_authority = userMap.get("approval_authority").toString();
        String id = request.getParameter("id");
        String now_time = ToolUtil.GetNowDateString();
        //首先判断用户是否有资格审批
        if(!approval_authority.contains(MessageType.STRSIX) && !approval_authority.contains(MessageType.STRSEVEN)){
            result.setMessage("操作失败，您没有该审批权限");
            return result;
        }
        Map<String,Object> maintainMap = maintainApprovalDAO.FindMaintainApprovalInfoById(id);
        Map<String,Object> mapData = maintainMap;
        Boolean bool = false;
        int application_steps = Integer.parseInt(maintainMap.get("application_steps").toString());
        int new_application_steps = application_steps+1;
        if(MessageType.FOUR == application_steps){
            result.setMessage("操作失败，该申请已审批通过，请勿重复操作");
            return result;
        }
        if(MessageType.TWO == application_steps && approval_authority.contains(MessageType.STRSIX) ){//副总经理审批
            mapData.put("application_steps",new_application_steps);
            mapData.put("deputy_general_manager",userInfo.getUser_name());
            mapData.put("deputy_general_manager_code",userInfo.getUser_code());
            mapData.put("deputy_general_manager_time",now_time);
            bool = true;
        }
        if(MessageType.THREE == application_steps && approval_authority.contains(MessageType.STRSEVEN)){//总经理审批
            mapData.put("application_steps",new_application_steps);
            mapData.put("general_manager",userInfo.getUser_name());
            mapData.put("general_manager_code",userInfo.getUser_code());
            mapData.put("general_manager_time",now_time);
            bool = true;
        }
        if(!bool){
            result.setMessage("操作失败，该申请当前不属于该审批步骤，请检查");
            return result;
        }
        //将审批数据进行保存
        maintainApprovalDAO.SaveApprovalInfo(mapData);
        /*
        //项目审批完成后需要生成对应的流水号
        if(MessageType.FIVE == new_application_steps){
            String now_year = ToolUtil.GetNowDateYearString();
            int total = serialNumberDAO.FindCurrentYearSerialNumber(now_year);
            Map<String,Object> serial_number_map = new HashMap<>();
            serial_number_map.put("approval_form_id",id);
            serial_number_map.put("user_code",userInfo.getUser_code());
            serial_number_map.put("serial_number",now_year+"-"+ToolUtil.GetStringNumberForValue(total+1, 3));
            serial_number_map.put("create_time",now_time);
            serial_number_map.put("table_status",MessageType.TWO);//审批表状态，0采购申请表，1报废审批表，2维修申请表
            serialNumberDAO.AddApprovalSerialNumber(serial_number_map);
        }
         */
        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",MessageType.EMPTYSTR);
        operationMap.put("operation_record","审核通过了id为："+id+"的维修申请");
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);
        return ReturnResult.success();
    }

    @Override
    public ReturnResult ReviewRepairApprovalFailed(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.error();
        String token = request.getParameter("token");
        String id = request.getParameter("id");
        String back_reason = request.getParameter("back_reason");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            return ReturnResult.NotExist();
        }
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        String approval_authority = userMap.get("approval_authority").toString();
        String now_time = ToolUtil.GetNowDateString();
        //首先判断用户是否有资格审批
        if(!approval_authority.contains(MessageType.STRSIX) && !approval_authority.contains(MessageType.STRSEVEN)){
            result.setMessage("操作失败，您没有该审批权限");
            return result;
        }
        Map<String,Object> maintainMap = maintainApprovalDAO.FindMaintainApprovalInfoById(id);
        Boolean bool = false;
        int application_steps = Integer.parseInt(maintainMap.get("application_steps").toString());
        if(MessageType.FOUR == application_steps){
            result.setMessage("操作失败，该申请已审批通过，请勿重复操作");
            return result;
        }
        if(MessageType.TWO == application_steps && approval_authority.contains(MessageType.STRSIX) ){//副总经理审批
            bool = true;
        }
        if(MessageType.THREE == application_steps && approval_authority.contains(MessageType.STRSEVEN)){//总经理审批
            bool = true;
        }
        if(!bool){
            result.setMessage("操作失败，该申请当前不属于该审批步骤，请检查");
            return result;
        }
        //还原维修申请审批状态码
        Map<String,Object> map_data = new HashMap<>();
        map_data.put("id",id);
        map_data.put("application_steps", MessageType.ONE);
        map_data.put("back_status",MessageType.ONE);
        map_data.put("back_reason",back_reason);
        map_data.put("back_user_name",userInfo.getUser_name());
        map_data.put("back_user_code",userInfo.getUser_code());
        maintainApprovalDAO.RestoreRepairRequestInfo(map_data);
        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",JSONObject.fromObject(maintainMap).toString());
        operationMap.put("operation_record","打回了id为："+id+"的维修申请，打回原因为："+back_reason);
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);

        return ReturnResult.success();
    }


    @Override
    public ReturnResult FindEquipmentMaintenanceData(HttpServletRequest request,HttpServletResponse response){
        //依据返回信息中application_steps该字段进行展示当前维修申请的情况，1-3项目正在审批中，4审批通过
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            return ReturnResult.NotExist();
        }
        JSONArray jsonarray = JSONArray.fromObject(request.getParameter("aoData"));
        DataTable dataTable = ToolUtil.GetDataTableValue(jsonarray);
        /*
        int ruleNumber = dataTable.getiSortCol();
        String key_name ="id";
        switch (ruleNumber) {
            case 3: key_name="signing_date";break;
            case 4: key_name="contract_amount";break;
            case 8: key_name="create_time";break;
        }
        dataTable.setSortTable(key_name);
         */
        List<Map<String,Object>> listMap = maintainApprovalDAO.FindEquipmentMaintenanceData(dataTable);
        int total = maintainApprovalDAO.FindEquipmentMaintenanceDataTotal(dataTable);
        //添加设备当前审批状态
        for(Map<String,Object> map : listMap){
            int application_steps = Integer.parseInt(map.get("application_steps").toString());
            int back_status = Integer.parseInt(map.get("back_status").toString());
            if(MessageType.ONE == back_status){
                map.put("project_progress","审批失败，请按照打回原因调整");
                continue;
            }
            if(MessageType.ONE == application_steps){
                map.put("project_progress","设备管理员待审批");
            }
            if(MessageType.TWO == application_steps){
                map.put("project_progress","副总经理待审批");
            }
            if(MessageType.THREE == application_steps){
                map.put("project_progress","总经理待审批");
            }
            if(MessageType.FOUR == application_steps){
                map.put("project_progress","审批完成");
            }
        }
        JSONObject getObj = new JSONObject();
        getObj.put("sEcho", dataTable.getsEcho());//
        getObj.put("iTotalRecords", total);//实际的行数
        getObj.put("iTotalDisplayRecords", total);//显示的行数,这个要和上面写的一样
        getObj.put("aaData", listMap);//要以JSON格式返回
        result.setObject(getObj.toString());
        return result;

    }

    @Override
    public ReturnResult AddMaintenanceInsuranceInfo(HttpServletRequest request,HttpServletResponse response){
        //用户输入的日期需要前端验证即结束时间不得大于开始时间
        ReturnResult result = ReturnResult.error();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            return ReturnResult.NotExist();
        }
        //维保记录只能是设备管理员才能添加，判断操作用户是否为设备管理员
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        if(MessageType.ONE != Integer.parseInt(userMap.get("device_admin").toString())){
            result.setMessage("操作失败，您没有该权限，请联系设备管理员操作");
            return result;
        }
        JSONObject jsonObject = JSONObject.fromObject(request.getParameter("json"));
        String id = jsonObject.get("id").toString();
        String maintenance_period = jsonObject.get("maintenance_period").toString();
        String starting_date = jsonObject.get("starting_date").toString();
        String end_date = jsonObject.get("end_date").toString();
        String now_time = ToolUtil.GetNowDateString();
        Map<String,Object> ledgerMap = equipmentLedgerDAO.FindEquipmentLedgerById(id);
        //获取该设备的维保到期时间，新增的维保开始时间不得小于当前该设备维保结束时间
        if(ToolUtil.compare_date(ledgerMap.get("maintenance_due_date").toString(),starting_date)){
            result.setMessage("操作失败，维保日期错误");
            return result;
        }
        Map<String,Object> mapData = new HashMap<>();
        mapData.put("id2",id);
        mapData.put("device_id",ledgerMap.get("device_id"));
        mapData.put("device_name",ledgerMap.get("device_name"));
        mapData.put("device_model",ledgerMap.get("device_model"));
        mapData.put("maintenance_period",maintenance_period);
        mapData.put("starting_date",starting_date);
        mapData.put("end_date",end_date);
        mapData.put("user_name",userInfo.getUser_name());
        mapData.put("user_code",userInfo.getUser_code());
        mapData.put("create_time",now_time);
        maintenanceInsuranceDAO.SaveMaintenanceInsuranceInfo(mapData);
        //添加成功后需要将设备台账中的维保日期进行修改
        JSONObject ledgerJson = JSONObject.fromObject(ledgerMap);
        ledgerJson.put("maintenance_due_date",end_date);
        ledgerJson.put("update_user_name",userInfo.getUser_name());
        ledgerJson.put("update_user_code",userInfo.getUser_code());
        ledgerJson.put("update_time",now_time);
        equipmentLedgerDAO.UpdateEquipmentLedgerInfo(ledgerJson);
        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",id);
        operationMap.put("raw_data",MessageType.EMPTYSTR);
        operationMap.put("operation_record","添加了设备编号为："+ledgerMap.get("device_id")+"的维保记录");
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);
        return ReturnResult.success();
    }

    @Override
    public ReturnResult FindMaintenanceInsuranceData(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            return ReturnResult.NotExist();
        }
        JSONArray jsonarray = JSONArray.fromObject(request.getParameter("aoData"));
        DataTable dataTable = ToolUtil.GetDataTableValue(jsonarray);
        int ruleNumber = dataTable.getiSortCol();
        String key_name ="id";
        switch (ruleNumber) {
            case 4: key_name="starting_date";break;
            case 5: key_name="end_date";break;
        }
        dataTable.setSortTable(key_name);
        List<Map<String,Object>> listMap = maintenanceInsuranceDAO.FindAllMaintenanceInsurance(dataTable);
        int total = maintenanceInsuranceDAO.FindAllMaintenanceInsuranceTotal(dataTable);
        JSONObject getObj = new JSONObject();
        getObj.put("sEcho", dataTable.getsEcho());//
        getObj.put("iTotalRecords", total);//实际的行数
        getObj.put("iTotalDisplayRecords", total);//显示的行数,这个要和上面写的一样
        getObj.put("aaData", listMap);//要以JSON格式返回
        result.setObject(getObj.toString());
        return result;
    }

    @Override
    public ReturnResult UpdateMaintenanceRequestInfo(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        JSONObject jsonObject = JSONObject.fromObject(request.getParameter("json"));
        String now_time = ToolUtil.GetNowDateString();
        String id = jsonObject.get("id").toString();
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        Map<String,Object> maintainMap = maintainApprovalDAO.FindMaintainApprovalInfoById(id);
        /*
        //判断一下该操作人与原申请人是否为同一人
        if(!userInfo.getUser_code().equals(maintainMap.get("general_management_leader_code").toString())){
            result = ReturnResult.error();
            result.setMessage("操作失败，您不是该申请原审批设备管理员，请检查");
            return result;
        }
        */
        //需要判断一下是否是打回的
        int back_status = Integer.parseInt(maintainMap.get("back_status").toString());
        if(MessageType.ONE != back_status){
            result = ReturnResult.error();
            result.setMessage("操作失败，该申请未被打回，不能进行修改");
            return result;
        }
        //保存修改后的申请信息
        jsonObject.put("back_status",MessageType.ZERO);
        jsonObject.put("application_steps",MessageType.TWO);
        jsonObject.put("general_management_leader",userInfo.getUser_name());
        jsonObject.put("general_management_leader_code",userInfo.getUser_code());
        jsonObject.put("general_management_leader_time",now_time);
        maintainApprovalDAO.UpdateMaintenanceRequestInfo(jsonObject);

        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",id);
        operationMap.put("raw_data",JSONObject.fromObject(maintainMap).toString());
        operationMap.put("operation_record","修改了id为："+id+"的维修申请信息");
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);

        return result;
    }

}
