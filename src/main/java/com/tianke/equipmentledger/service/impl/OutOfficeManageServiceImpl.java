package com.tianke.equipmentledger.service.impl;

import com.tianke.equipmentledger.context.MessageType;
import com.tianke.equipmentledger.context.ResponseUtils;
import com.tianke.equipmentledger.context.ReturnResult;
import com.tianke.equipmentledger.context.ToolUtil;
import com.tianke.equipmentledger.dao.OperationRecordDAO;
import com.tianke.equipmentledger.dao.OutOfficeManageDAO;
import com.tianke.equipmentledger.dao.SerialNumberDAO;
import com.tianke.equipmentledger.dao.UserInfoDAO;
import com.tianke.equipmentledger.entity.DataTable;
import com.tianke.equipmentledger.entity.UserLoginInfo;
import com.tianke.equipmentledger.service.OutOfficeManageService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import org.apache.ibatis.annotations.One;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("outOfficeManageService")
public class OutOfficeManageServiceImpl implements OutOfficeManageService {
    @Resource
    private OutOfficeManageDAO outOfficeManageDAO;
    @Resource
    private SerialNumberDAO serialNumberDAO;
    @Resource
    private UserInfoDAO userInfoDAO;
    @Resource
    private OperationRecordDAO operationRecordDAO;

    @Override
    public ReturnResult ApplyEquipmentGoingOut(HttpServletRequest request, HttpServletResponse response){
        ReturnResult result = ReturnResult.error();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        JSONObject jsonObject = JSONObject.fromObject(request.getParameter("json"));
        //首先查看该设备当前是否存在已申请的记录
        Map<String,Object> maintainMap = outOfficeManageDAO.FindGooutApprovalInfoById2(jsonObject.get("id").toString());
        if(null != maintainMap){
            result.setMessage("操作失败，该设备已存在外出申请，请勿重复申请");
            return result;
        }
        //判断故障现象及故障原因是否填写，不能为空
        if( MessageType.EMPTYSTR.equals(jsonObject.get("delivery_company")) ||
                MessageType.EMPTYSTR.equals(jsonObject.get("company_contacts")) ||
                MessageType.EMPTYSTR.equals(jsonObject.get("company_phone"))){
            result.setMessage("操作失败，设备外出单位/联系人/电话等信息不能为空");
            return result;
        }
        //由该设备的使用部门负责人申请外出
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        String department = userMap.get("department").toString();
        int job_title_status = Integer.parseInt(userMap.get("job_title_status").toString());
        String use_department = jsonObject.get("use_department").toString();
        if(!department.equals(use_department) || MessageType.ONE != job_title_status){
            result.setMessage("操作失败，您不是该设备使用人员，没有该操作权限");
            return result;
        }
        //保存申请信息
        String now_time = ToolUtil.GetNowDateString();
        jsonObject.put("use_department_leader",userInfo.getUser_name());
        jsonObject.put("use_department_leader_code",userInfo.getUser_code());
        jsonObject.put("use_department_leader_time",ToolUtil.GetNowDateYMDString());
        jsonObject.put("approval_steps",MessageType.ONE);
        jsonObject.put("create_user_name",userInfo.getUser_name());
        jsonObject.put("create_user_code",userInfo.getUser_code());
        jsonObject.put("create_time",now_time);
        jsonObject.put("back_reason",MessageType.EMPTYSTR);
        outOfficeManageDAO.AddGooutApprovalInfo(jsonObject);
        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",jsonObject.get("id"));
        operationMap.put("raw_data",MessageType.EMPTYSTR);
        operationMap.put("operation_record","创建了设备编号为："+jsonObject.get("device_id")+"的外出申请记录");
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);
        return ReturnResult.success();

    }

    @Override
    public ReturnResult FindApplyEquipmentGoingOutData(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        List<Map<String,Object>> listMap = new ArrayList<>();
        //首先判断该用户职位，查看是否有权限进行审批
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        String approval_authority = userMap.get("approval_authority").toString();
        //该审批操作需要三个部门进行审批即：综合管理部2，技术负责人3，实验室主任4
        if(!approval_authority.contains(MessageType.STRTWO) && !approval_authority.contains(MessageType.STRTHREE) &&
        !approval_authority.contains(MessageType.STRFOUR)){
            result.setObject(listMap);
            return result;
        }
        List<Integer> approval_steps_list = new ArrayList<>();
        if(!MessageType.EMPTYSTR.equals(approval_authority)){
            if(approval_authority.contains(MessageType.STRTWO)){
                approval_steps_list.add(MessageType.ONE);//综合管理
            }
            if(approval_authority.contains(MessageType.STRTHREE)){
                approval_steps_list.add(MessageType.TWO);//技术负责人
            }
            if(approval_authority.contains(MessageType.STRFOUR)){
                approval_steps_list.add(MessageType.THREE);//实验室主任
            }
        }
        if(approval_steps_list.size() == 0){
            result.setObject(listMap);
            return result;
        }

        listMap = outOfficeManageDAO.FindGooutApprovalInfoByApprovalSteps(approval_steps_list);
        result.setObject(listMap);
        return result;
    }

    @Override
    public ReturnResult ReviewGoOutApproval(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.error();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        String id = request.getParameter("id");
        Map<String,Object> goOutMap = outOfficeManageDAO.FindGooutApprovalInfoById(id);
        Map<String,Object> mapData = goOutMap;
        //判断该用户是否有该审批权限
        String now_time = ToolUtil.GetNowDateString();
        String now_date = ToolUtil.GetNowDateYMDString();
        boolean bool = false;
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        String approval_authority = userMap.get("approval_authority").toString();
        //int job_title_status = userInfo.getJob_title_status();
        String use_department = goOutMap.get("use_department").toString();
        int approval_steps = Integer.parseInt(goOutMap.get("approval_steps").toString());
        int new_approval_steps = approval_steps+1;
        //首先判断用户是否有审批权限即：综合管理部负责人2，技术负责人3，实验室主任4
        if(!approval_authority.contains(MessageType.STRTWO) && !approval_authority.contains(MessageType.STRTHREE) &&
                !approval_authority.contains(MessageType.STRFOUR)){
            result.setMessage("操作失败，您没有该审批权限");
            return result;
        }
        //判断该申请处于正在审批状态
        if(MessageType.ZERO != Integer.parseInt(goOutMap.get("device_status").toString())){
            result.setMessage("操作失败，该申请已通过审批，请勿重复操作");
            ResponseUtils.renderJson(response, JSONObject.fromObject(result).toString());
        }
        /*
        //部门负责人
        if((MessageType.ZERO == approval_steps && approval_authority.contains(MessageType.STRONE) && MessageType.WEISHENGWUJIANCE.equals(use_department)) ||
                (MessageType.ZERO == approval_steps && approval_authority.contains(MessageType.STRSIX) && MessageType.FENZIJIANCE.equals(use_department))){
            mapData.put("approval_steps",new_approval_steps);
            mapData.put("device_status",MessageType.ZERO);
            mapData.put("use_department_leader",userInfo.getUser_name());
            mapData.put("use_department_leader_code",userInfo.getUser_code());
            mapData.put("use_department_leader_time",now_date);
            bool = true;
        }
         */
        //综合管理
        if(MessageType.ONE == approval_steps && approval_authority.contains(MessageType.STRTWO)){
            mapData.put("approval_steps",new_approval_steps);
            mapData.put("device_status",MessageType.ZERO);
            mapData.put("general_management_leader",userInfo.getUser_name());
            mapData.put("general_management_leader_code",userInfo.getUser_code());
            mapData.put("general_management_leader_time",now_date);
            bool = true;
        }
        //技术负责人
        if(MessageType.TWO == approval_steps && approval_authority.contains(MessageType.STRTHREE)){
            mapData.put("approval_steps",new_approval_steps);
            mapData.put("device_status",MessageType.ZERO);
            mapData.put("technical_director",userInfo.getUser_name());
            mapData.put("technical_director_code",userInfo.getUser_code());
            mapData.put("technical_director_time",now_date);
            bool = true;
        }
        //实验室主任
        if(MessageType.THREE == approval_steps && approval_authority.contains(MessageType.STRFOUR)){
            mapData.put("approval_steps",new_approval_steps);
            mapData.put("device_status",MessageType.ONE);
            mapData.put("laboratory_director",userInfo.getUser_name());
            mapData.put("laboratory_director_code",userInfo.getUser_code());
            mapData.put("laboratory_director_time",now_date);
            bool = true;
        }
        if(!bool){
            result.setMessage("操作失败，该申请当前不属于该审批步骤，请检查");
            return result;
        }
        //将审批结果进行保存
        outOfficeManageDAO.SaveGoOutApprovalInfo(mapData);
        /*
        //项目审批完成后需要生成对应的流水号
        if(MessageType.FOUR == new_approval_steps){
            String now_year = ToolUtil.GetNowDateYearString();
            int total = serialNumberDAO.FindCurrentYearSerialNumber(now_year);
            Map<String,Object> serial_number_map = new HashMap<>();
            serial_number_map.put("approval_form_id",id);
            serial_number_map.put("user_code",userInfo.getUser_code());
            serial_number_map.put("serial_number",now_year+"-"+ToolUtil.GetStringNumberForValue(total+1, 3));
            serial_number_map.put("create_time",now_time);
            serial_number_map.put("table_status",MessageType.THREE);//审批表状态，0采购申请表，1报废审批表，2维修申请表，3外出申请表
            serialNumberDAO.AddApprovalSerialNumber(serial_number_map);
        }
        */
        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",MessageType.EMPTYSTR);
        operationMap.put("operation_record","审核了id为："+id+"的外出申请记录");
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);
        return ReturnResult.success();

    }

    @Override
    public ReturnResult ReviewGoOutApprovalFailed(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.error();
        String token = request.getParameter("token");
        String id = request.getParameter("id");
        String back_reason = request.getParameter("back_reason");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        Map<String,Object> goOutMap = outOfficeManageDAO.FindGooutApprovalInfoById(id);
        //判断该用户是否有该审批权限
        String now_time = ToolUtil.GetNowDateString();
        boolean bool = false;
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        String approval_authority = userMap.get("approval_authority").toString();
        String use_department = goOutMap.get("use_department").toString();
        int approval_steps = Integer.parseInt(goOutMap.get("approval_steps").toString());
        //首先判断用户是否有审批权限即：综合管理部负责人2，技术负责人3，实验室主任4
        if(!approval_authority.contains(MessageType.STRTWO) && !approval_authority.contains(MessageType.STRTHREE) &&
                !approval_authority.contains(MessageType.STRFOUR) ){
            result.setMessage("操作失败，您没有该审批权限");
            return result;
        }
        //判断该申请处于正在审批状态
        if(MessageType.ZERO != Integer.parseInt(goOutMap.get("device_status").toString())){
            result.setMessage("操作失败，该申请已通过审批，请勿重复操作");
            ResponseUtils.renderJson(response, JSONObject.fromObject(result).toString());
        }
        //综合管理
        if(MessageType.ONE == approval_steps && approval_authority.contains(MessageType.STRTWO)){
            bool = true;
        }
        //技术负责人
        if(MessageType.TWO == approval_steps && approval_authority.contains(MessageType.STRTHREE)){
            bool = true;
        }
        //实验室主任
        if(MessageType.THREE == approval_steps && approval_authority.contains(MessageType.STRFOUR)){
            bool = true;
        }
        if(!bool){
            result.setMessage("操作失败，该申请当前不属于该审批步骤，请检查");
            return result;
        }
        Map<String,Object> map_data = new HashMap<>();
        map_data.put("id",id);
        map_data.put("approval_steps", MessageType.ONE);
        map_data.put("back_status",MessageType.ONE);
        map_data.put("back_reason",back_reason);
        map_data.put("back_user_name",userInfo.getUser_name());
        map_data.put("back_user_code",userInfo.getUser_code());
        outOfficeManageDAO.RestoringOutgoApplicationInfo(map_data);
        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data", JSONObject.fromObject(goOutMap).toString());
        operationMap.put("operation_record","打回了id为："+id+"的设备外出申请，打回原因为："+back_reason);
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);

        return ReturnResult.success();
    }

    @Override
    public ReturnResult FindApplyGoingOutData(HttpServletRequest request,HttpServletResponse response){
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
        List<Map<String,Object>> listMap = outOfficeManageDAO.FindAllApprovalGoOutApplication(dataTable);
        int total = outOfficeManageDAO.FindAllApprovalGoOutApplicationTotal(dataTable);
        //展示数据需要处理一下
        for(Map<String,Object> map : listMap){
            int go_out_reason = Integer.parseInt(map.get("go_out_reason").toString());
            int approval_steps = Integer.parseInt(map.get("approval_steps").toString());
            int back_status = Integer.parseInt(map.get("back_status").toString());
            if(MessageType.ONE == go_out_reason){
                map.put("go_out_reason","校准");
            }
            if(MessageType.TWO == go_out_reason){
                map.put("go_out_reason","维修");
            }
            if(MessageType.THREE == go_out_reason){
                map.put("go_out_reason","借用");
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
                map.put("project_progress","综合管理待审批");
            }
            if(MessageType.TWO == approval_steps){
                map.put("project_progress","技术负责人待审批");
            }
            if(MessageType.THREE == approval_steps){
                map.put("project_progress","实验室主任待审批");
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
    public ReturnResult FillEquipmentReturnInfo(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.error();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        //首先判断该用户是否是综合管理部员工及设备管理员
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        String department = userMap.get("department").toString();
        int device_admin = Integer.parseInt(userMap.get("device_admin").toString());
        if(!"综合管理部".equals(department) || MessageType.ONE != device_admin){
            result.setMessage("操作失败，您没有该权限");
            return result;
        }
        JSONObject jsonObject = JSONObject.fromObject(request.getParameter("json"));
        String id = jsonObject.get("id").toString();
        Map<String,Object> goOutMap = outOfficeManageDAO.FindGooutApprovalInfoById(id);
        //判断该申请的审批步骤及设备状态
        int approval_steps = Integer.parseInt(goOutMap.get("approval_steps").toString());
        int device_status = Integer.parseInt(goOutMap.get("device_status").toString());
        if(MessageType.FOUR == approval_steps && MessageType.ONE == device_status){
            //填写的信息不能为空，交由前端进行控制
            jsonObject.put("device_status",MessageType.TWO);
            outOfficeManageDAO.SaveBackCheckInfo(jsonObject);
        }else{
            //不处于该操作步骤
            result.setMessage("操作失败，改申请暂不属于该操作步骤");
            return result;
        }
        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",MessageType.EMPTYSTR);
        operationMap.put("operation_record","填写了id为："+id+"的外出设备回归确认信息");
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",ToolUtil.GetNowDateString());
        operationRecordDAO.SaveUserOperationRecord(operationMap);
        return ReturnResult.success();
    }

    @Override
    public ReturnResult UpdateOutboundApplicationInfo(HttpServletRequest request,HttpServletResponse response){
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
        Map<String,Object> goOutMap = outOfficeManageDAO.FindGooutApprovalInfoById(id);
        //判断一下该操作人与原申请人是否为同一人
        if(!userInfo.getUser_code().equals(goOutMap.get("use_department_leader_code").toString())){
            result = ReturnResult.error();
            result.setMessage("操作失败，您不是该申请原申请人，请检查");
            return result;
        }
        //需要判断一下是否是打回的
        int back_status = Integer.parseInt(goOutMap.get("back_status").toString());
        if(MessageType.ONE != back_status){
            result = ReturnResult.error();
            result.setMessage("操作失败，该申请未被打回，不能进行修改");
            return result;
        }
        //保存修改后的申请信息
        jsonObject.put("back_status",MessageType.ZERO);
        outOfficeManageDAO.UpdateOutboundApplicationInfo(jsonObject);
        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",JSONObject.fromObject(goOutMap).toString());
        operationMap.put("operation_record","修改了id为："+id+"的设备外出申请信息");
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",ToolUtil.GetNowDateString());
        operationRecordDAO.SaveUserOperationRecord(operationMap);

        return result;

    }

}
