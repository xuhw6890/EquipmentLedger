package com.tianke.equipmentledger.service.impl;

import com.tianke.equipmentledger.context.*;
import com.tianke.equipmentledger.dao.*;
import com.tianke.equipmentledger.entity.DataTable;
import com.tianke.equipmentledger.entity.UserLoginInfo;
import com.tianke.equipmentledger.service.DeviceApprovalService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("deviceApprovalService")
public class DeviceApprovalServiceImpl implements DeviceApprovalService {
    @Resource
    private ScrapApprovalDAO scrapApprovalDAO;
    @Resource
    private SerialNumberDAO serialNumberDAO;
    @Resource
    private EquipmentLedgerDAO equipmentLedgerDAO;
    @Resource
    private UserInfoDAO userInfoDAO;
    @Resource
    private OperationRecordDAO operationRecordDAO;
    @Resource
    private AcquisitionManageDAO acquisitionManageDAO;
    @Resource
    private MaintainApprovalDAO maintainApprovalDAO;
    @Resource
    private OutOfficeManageDAO outOfficeManageDAO;
    @Resource
    private VerifyApprovalDAO verifyApprovalDAO;

    @Override
    public synchronized ReturnResult ApplyDeviceDeactivation(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        JSONObject jsonObject = JSONObject.fromObject(request.getParameter("json"));
        String use_department = jsonObject.get("use_department").toString();
        //设备申请停用或报废由该设备使用部门负责人发起
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        String department = userMap.get("department").toString();
        int job_title_status = Integer.parseInt(userMap.get("job_title_status").toString());
        if(!use_department.equals(department) || MessageType.ONE != job_title_status){
            result = ReturnResult.error();
            result.setMessage("操作失败，您没有该权限，请联系该设备使用部门负责人申请");
            return result;
        }

        //判断该设备是否已被申请过报废，防止重复操作
        String id2 = jsonObject.get("id").toString();
        Map<String,Object> scrapMap = scrapApprovalDAO.FindScrapApprovalInfoById2(id2);
        if(null != scrapMap){
            result = ReturnResult.error();
            result.setMessage("操作失败，该设备已申请过报废/停用，请勿重复操作");
            return result;
        }
        //判断报废原因是否填写，不能为空或空格
        String reason_deactivation = jsonObject.get("reason_deactivation").toString().trim();
        if(MessageType.EMPTYSTR.equals(reason_deactivation)){
            result = ReturnResult.error();
            result.setMessage("操作失败，报废/停用原因不能为空");
            return result;
        }
        String now_time = ToolUtil.GetNowDateString();
        jsonObject.put("use_department_leader",userInfo.getUser_name());
        jsonObject.put("use_department_leader_code",userInfo.getUser_code());
        jsonObject.put("use_department_leader_time",ToolUtil.GetNowDateYMDString());
        jsonObject.put("create_user_name",userInfo.getUser_name());
        jsonObject.put("create_user_code",userInfo.getUser_code());
        jsonObject.put("create_time",now_time);
        jsonObject.put("back_reason",MessageType.EMPTYSTR);
        scrapApprovalDAO.AddScrapApprovalInfo(jsonObject);
        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",jsonObject.get("id"));
        operationMap.put("raw_data",MessageType.EMPTYSTR);
        operationMap.put("operation_record","创建了设备编号为："+jsonObject.get("device_id")+"的停用/报废申请");
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);
        return result;

    }

    @Override
    public ReturnResult FindScrapApprovalData(HttpServletRequest request, HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        String approval_authority = userMap.get("approval_authority").toString();
        List<Map<String,Object>> listMap = new ArrayList<>();
        //首先判断用户是否有审批权限即：6副总经理，7总经理，5财务
        if(!approval_authority.contains(MessageType.STRFIVE) && !approval_authority.contains(MessageType.STRSIX) &&
                !approval_authority.contains(MessageType.STRSEVEN)){
            result.setObject(listMap);
            return result;
        }

        List<Integer> approval_steps_list = new ArrayList<>();
        if(!MessageType.EMPTYSTR.equals(approval_authority)){
            if(approval_authority.contains(MessageType.STRSIX)){
                approval_steps_list.add(MessageType.ONE);//副总经理
            }
            if(approval_authority.contains(MessageType.STRSEVEN)){
                approval_steps_list.add(MessageType.TWO);//总经理
            }
            if(approval_authority.contains(MessageType.STRFIVE)){
                approval_steps_list.add(MessageType.THREE);//财务
            }
        }
        if(approval_steps_list.size() == 0){
            result.setObject(listMap);
            return result;
        }
        listMap = scrapApprovalDAO.FindScrapApprovalInfoByUserRights(approval_steps_list);
        result.setObject(listMap);
        return result;

    }

    @Transactional(rollbackFor = Exception.class)
    public synchronized ReturnResult ReviewScrapApplications(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.error();
        String token = request.getParameter("token");
        String id = request.getParameter("id");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        Map<String,Object> scrapMap = scrapApprovalDAO.FindScrapApprovalInfoById(id);
        if(null == scrapMap){
            result.setMessage("操作失败，没有找到数据");
            return result;
        }
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        String approval_authority = userMap.get("approval_authority").toString();
        Map<String,Object> mapData = scrapMap;
        String now_time = ToolUtil.GetNowDateString();
        String now_date = ToolUtil.GetNowDateYMDString();
        int approval_steps = Integer.parseInt(scrapMap.get("approval_steps").toString());
        int new_approval_steps = approval_steps+1;
        String use_department = scrapMap.get("use_department").toString();
        Boolean bool = false;
        //判断用户是否能审核该记录
        if(!approval_authority.contains(MessageType.STRSIX) && !approval_authority.contains(MessageType.STRSEVEN) &&
                !approval_authority.contains(MessageType.STRFIVE)){
            result.setMessage("操作失败，您没有该审批权限");
            return result;
        }
        //副总经理
        if(MessageType.ONE == approval_steps && approval_authority.contains(MessageType.STRSIX)){
            mapData.put("deputy_general_manager",userInfo.getUser_name());
            mapData.put("deputy_general_manager_code",userInfo.getUser_code());
            mapData.put("deputy_general_manager_time",now_date);
            mapData.put("approval_steps",new_approval_steps);
            bool = true;
        }
        //总经理
        if(MessageType.TWO == approval_steps && approval_authority.contains(MessageType.STRSEVEN)){
            mapData.put("general_manager",userInfo.getUser_name());
            mapData.put("general_manager_code",userInfo.getDept_code());
            mapData.put("general_manager_time",now_date);
            mapData.put("approval_steps",new_approval_steps);
            bool = true;
        }
        //财务
        if(MessageType.THREE == approval_steps && approval_authority.contains(MessageType.STRFIVE) ){
            mapData.put("financial_affairs",userInfo.getUser_name());
            mapData.put("financial_affairs_code",userInfo.getDept_code());
            mapData.put("financial_affairs_time",now_date);
            mapData.put("approval_steps",new_approval_steps);
            bool = true;
        }
        if(!bool){
            result.setMessage("操作失败，该申请当前不属于该审批步骤，请检查");
            return result;
        }
        //保存审批信息
        scrapApprovalDAO.ReviewScrapApprovalInfo(mapData);

        if(MessageType.FOUR == new_approval_steps){
            //将设备台账中该设备的使用状态调整为停用/报废
            equipmentLedgerDAO.UpdateDeviceStatus(scrapMap.get("id2").toString(),scrapMap.get("device_status").toString());
            //修改申请表中的启用状态
            scrapApprovalDAO.UpdateEquipmentEnabledStatus(id,MessageType.STRZERO);
            //发送短信给资产管理员，提醒该设备已停用/报废
            List<String> user_phone_list = userInfoDAO.FindAssetManagerMobilePhone(MessageType.EMPTYSTR);
            if(user_phone_list.size() > 0){
                String back_str = "固定资产编号为："+scrapMap.get("fixed_asset_number")+"的停用/报废申请已通过审批";
                if(MessageType.EMPTYSTR.equals(scrapMap.get("fixed_asset_number"))){
                    back_str = "设备编号为："+scrapMap.get("device_id")+"的停用/报废申请已通过审批";
                }
                SendMessagesUtil.SendActionSMS(user_phone_list,back_str);
            }
        }
        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",MessageType.EMPTYSTR);
        operationMap.put("operation_record","审核了id为："+id+"的停用/报废申请");
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);
        return ReturnResult.success();

    }

    @Override
    public ReturnResult ReviewScrapApplicationsFailed(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.error();
        String token = request.getParameter("token");
        String id = request.getParameter("id");
        String back_reason = request.getParameter("back_reason");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        Map<String,Object> scrapMap = scrapApprovalDAO.FindScrapApprovalInfoById(id);
        if(null == scrapMap){
            result.setMessage("操作失败，没有找到数据");
            return result;
        }
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        String approval_authority = userMap.get("approval_authority").toString();
        String now_time = ToolUtil.GetNowDateString();
        int approval_steps = Integer.parseInt(scrapMap.get("approval_steps").toString());
        Boolean bool = false;
        //判断用户是否能审核该记录
        if(!approval_authority.contains(MessageType.STRSIX) && !approval_authority.contains(MessageType.STRSEVEN) &&
                !approval_authority.contains(MessageType.STRFIVE)){
            result.setMessage("操作失败，您没有该审批权限");
            return result;
        }
        //副总经理
        if(MessageType.ONE == approval_steps && approval_authority.contains(MessageType.STRSIX)){
            bool = true;
        }
        //总经理
        if(MessageType.TWO == approval_steps && approval_authority.contains(MessageType.STRSEVEN)){
            bool = true;
        }
        //财务
        if(MessageType.THREE == approval_steps && approval_authority.contains(MessageType.STRFIVE) ){
            bool = true;
        }
        if(!bool){
            result.setMessage("操作失败，该申请当前不属于该审批步骤，请检查");
            return result;
        }
        //还原设备报废申请审批状态码
        Map<String,Object> map_data = new HashMap<>();
        map_data.put("id",id);
        map_data.put("approval_steps",MessageType.ONE);
        map_data.put("back_status",MessageType.ONE);
        map_data.put("back_reason",back_reason);
        map_data.put("back_user_name",userInfo.getUser_name());
        map_data.put("back_user_code",userInfo.getUser_code());
        scrapApprovalDAO.RestoreDeactivationRequestInfo(map_data);

        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",JSONObject.fromObject(scrapMap).toString());
        operationMap.put("operation_record","打回了id为："+id+"的停用/报废申请，打回原因为："+back_reason);
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);
        return ReturnResult.success();
    }

    @Override
    public ReturnResult FindChangeApplicationData(HttpServletRequest request,HttpServletResponse response){
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
        //每个部门只查看自己部门的数据
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        Map<String,Object> map_data = new HashMap<>();
        String department = userMap.get("department").toString();
        String dept_code = MessageType.EMPTYSTR;
        if("综合管理部".equals(department)){
            dept_code = "02";
        }
        map_data.put("dataTable",dataTable);
        map_data.put("use_department",department);
        map_data.put("dept_code",dept_code);
        List<Map<String,Object>> listMap = scrapApprovalDAO.FindAllChangeApplicationData(map_data);
        int total = scrapApprovalDAO.FindAllChangeApplicationDataTotal(map_data);
        //展示数据需要处理一下
        for(Map<String,Object> map : listMap){
            int device_status = Integer.parseInt(map.get("device_status").toString());
            int approval_steps = Integer.parseInt(map.get("approval_steps").toString());
            int back_status = Integer.parseInt(map.get("back_status").toString());
            if(MessageType.ONE == device_status){
                map.put("device_status","停用");
            }
            if(MessageType.TWO == device_status){
                map.put("device_status","报废");
            }
            if(MessageType.ONE == back_status){
                map.put("project_progress","审批失败，请按照打回原因调整");
                continue;
            }
            //获取当前审批进度
            if(MessageType.ZERO == approval_steps){
                map.put("project_progress","部门负责人待审批");
            }
            if(MessageType.ONE == approval_steps){
                map.put("project_progress","副总经理待审批");
            }
            if(MessageType.TWO == approval_steps){
                map.put("project_progress","总经理待审批");
            }
            if(MessageType.THREE == approval_steps){
                map.put("project_progress","财务待审批");
            }
            if(MessageType.FOUR == approval_steps){
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
    public ReturnResult FindApprovalInfoReminder(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        //判断该用户是否有审批权限
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        String approval_authority = userMap.get("approval_authority").toString();
        Map<String,Object> mapData = new HashMap<>();
        if(!MessageType.EMPTYSTR.equals(approval_authority)){
            //有审批权限，依据用户的审批权限查找当前是否存在需要审批的信息
            //采购申请
            List<Integer> application_status_list = new ArrayList<>();
            //判断用户审批权限，依据审批权限不同展示当前该用户需要审批的数据
            if(approval_authority.contains(MessageType.STRSIX)){
                //副总
                application_status_list.add(MessageType.ONE);
            }
            if(approval_authority.contains(MessageType.STRSEVEN)){
                //总经理
                application_status_list.add(MessageType.TWO);
            }
            if (approval_authority.contains(MessageType.STREIGTH)) {
                //董事长
                application_status_list.add(MessageType.THREE);
            }
            List<Map<String,Object>> listMap = acquisitionManageDAO.FindPurchaseRequisitionInfoByApplicationStatus(application_status_list);
            mapData.put("purchase",listMap.size());

            //维保申请
            List<Map<String,Object>> maintainList = new ArrayList<>();
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
                maintainList = maintainApprovalDAO.FindRepairRequestData(application_steps_list);
            }
            mapData.put("maintain",maintainList.size());

            //外出申请
            List<Map<String,Object>> GoOutList = new ArrayList<>();
            List<Integer> approval_steps_list = new ArrayList<>();
            if(approval_authority.contains(MessageType.STRTWO)){
                approval_steps_list.add(MessageType.ONE);//综合管理
            }
            if(approval_authority.contains(MessageType.STRTHREE)){
                approval_steps_list.add(MessageType.TWO);//技术负责人
            }
            if(approval_authority.contains(MessageType.STRFOUR)){
                approval_steps_list.add(MessageType.THREE);//实验室主任
            }
            if(approval_steps_list.size() > 0){
                GoOutList = outOfficeManageDAO.FindGooutApprovalInfoByApprovalSteps(approval_steps_list);
            }
            mapData.put("goOut",GoOutList.size());

            //变更申请
            List<Integer> approval_steps_two_list = new ArrayList<>();
            if(!MessageType.EMPTYSTR.equals(approval_authority)){
                if(approval_authority.contains(MessageType.STRSIX)){
                    approval_steps_two_list.add(MessageType.ONE);//副总经理
                }
                if(approval_authority.contains(MessageType.STRSEVEN)){
                    approval_steps_two_list.add(MessageType.TWO);//总经理
                }
                if(approval_authority.contains(MessageType.STRFIVE)){
                    approval_steps_two_list.add(MessageType.THREE);//总经理
                }
            }
            List<Map<String,Object>> changeList = new ArrayList<>();
            if(approval_steps_two_list.size() > 0){
                changeList = scrapApprovalDAO.FindScrapApprovalInfoByUserRights(approval_steps_two_list);
            }
            mapData.put("change",changeList.size());

        }

        //验收申请，存在设备管理员审批，该审批权限为单独的，没有归类到整的审批权限中
        //获取用户权限
        String department = userMap.get("department").toString();
        String dept_code = userMap.get("dept_code").toString();
        //综合管理员可以查看实验室整个部门的申请数据
        if("综合管理部".equals(department)){
            dept_code = "02";
        }
        int device_admin = Integer.parseInt(userMap.get("device_admin").toString());
        int fixed_asset_admin = Integer.parseInt(userMap.get("fixed_asset_admin").toString());
        String department_head = MessageType.EMPTYSTR;//部门负责人
        String device_admin_status = MessageType.EMPTYSTR;//设备管理员状态
        List<Integer> application_status_two_list = new ArrayList<>();//审批权限
        if(!MessageType.EMPTYSTR.equals(approval_authority)){
            //判断用户审批权限，依据审批权限不同展示当前该用户需要审批的数据
            if(approval_authority.contains(MessageType.STRSIX)){
                //副总
                application_status_two_list.add(MessageType.THREE);
            }
            if(approval_authority.contains(MessageType.STRSEVEN)){
                //总经理
                application_status_two_list.add(MessageType.FOUR);
            }
            if(approval_authority.contains(MessageType.STRONE)){
                //部门负责人
                department_head = MessageType.STRONE;
            }
        }
        //判断是否是资产管理员
        if(MessageType.ONE == fixed_asset_admin){
            application_status_two_list.add(MessageType.TWO);
        }
        //判断是否是设备管理员
        if(MessageType.ONE == device_admin){
            device_admin_status = MessageType.STRONE;
        }
        //该用户没有审批权限则给0，防止系统报错
        if(application_status_two_list.size() == 0){
            application_status_two_list.add(MessageType.ZERO);
        }
        Map<String,Object> map_data = new HashMap<>();
        map_data.put("application_status",application_status_two_list);
        map_data.put("dept_code",dept_code);
        map_data.put("use_department",department);
        map_data.put("department_head",department_head);
        map_data.put("device_admin_status",device_admin_status);
        List<Map<String,Object>> verifyList = verifyApprovalDAO.FindAcceptanceApplicationData(map_data);
        mapData.put("verify",verifyList.size());

        result.setObject(mapData);
        return result;
    }

    @Override
    public ReturnResult ActionDeviceEnabled(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.error();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        String id = request.getParameter("id");
        Map<String,Object> ledgerMap = equipmentLedgerDAO.FindEquipmentLedgerById(id);
        //判断该设备是否是停用的状态
        int device_status = Integer.parseInt(ledgerMap.get("device_status").toString());
        if(MessageType.ONE != device_status){
            result.setMessage("操作失败，该设备状态当前不为停用状态，请检查");
            return result;
        }
        //判断用户是否是该设备使用部门部门负责人
        String use_department = ledgerMap.get("use_department").toString();
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        String department = userMap.get("department").toString();
        int job_title_status = Integer.parseInt(userMap.get("job_title_status").toString());
        if(!department.equals(use_department) || MessageType.ONE != job_title_status){
            result.setMessage("操作失败，您没有该权限");
            return result;
        }
        //判断该设备的设备编号与当前正常使用的设备中是否存在冲突
        String device_id = ledgerMap.get("device_id").toString();
        if(MessageType.EMPTYSTR.equals(device_id)){
            result.setMessage("操作失败，设备编号不得为空");
            return result;
        }
        Map<String,Object> dev_id_map = equipmentLedgerDAO.FindEquipmentLedgerByDeviceId(device_id);
        if(null != dev_id_map){
            result.setMessage("操作失败，该设备编号已被使用，请检查");
            return result;
        }
        //将修改设备使用状态
        equipmentLedgerDAO.UpdateDeviceStatusById(id);
        //修改申请表中的启用状态
        scrapApprovalDAO.UpdateEquipmentEnabledStatus(id,MessageType.STRONE);
        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",JSONObject.fromObject(ledgerMap).toString());
        operationMap.put("operation_record","启用了id为："+id+"的设备，该设备状态原为停用");
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",ToolUtil.GetNowDateString());
        operationRecordDAO.SaveUserOperationRecord(operationMap);

        return ReturnResult.success();
    }

    @Override
    public ReturnResult UpdateScrapApplicationInfo(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        JSONObject jsonObject = JSONObject.fromObject(request.getParameter("json"));
        String id = jsonObject.get("id").toString();
        String now_time = ToolUtil.GetNowDateString();
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        Map<String,Object> scrapMap = scrapApprovalDAO.FindScrapApprovalInfoById(id);
        //判断一下该操作人与原申请人是否为同一人
        if(!userInfo.getUser_code().equals(scrapMap.get("use_department_leader_code").toString())){
            result = ReturnResult.error();
            result.setMessage("操作失败，您不是该申请原申请人，请检查");
            return result;
        }
        //需要判断一下是否是打回的
        int back_status = Integer.parseInt(scrapMap.get("back_status").toString());
        if(MessageType.ONE != back_status){
            result = ReturnResult.error();
            result.setMessage("操作失败，该申请未被打回，不能进行修改");
            return result;
        }
        //保存修改后的申请信息
        jsonObject.put("back_status",MessageType.ZERO);
        scrapApprovalDAO.UpdateScrapApplicationInfo(jsonObject);
        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",JSONObject.fromObject(scrapMap).toString());
        operationMap.put("operation_record","修改了id为："+id+"的设备停用/报废申请信息");
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);

        return result;

    }

}
