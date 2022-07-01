package com.tianke.equipmentledger.service.impl;

import com.tianke.equipmentledger.context.MessageType;
import com.tianke.equipmentledger.context.ReturnResult;
import com.tianke.equipmentledger.context.SendMessagesUtil;
import com.tianke.equipmentledger.context.ToolUtil;
import com.tianke.equipmentledger.dao.*;
import com.tianke.equipmentledger.entity.DataTable;
import com.tianke.equipmentledger.entity.UserLoginInfo;
import com.tianke.equipmentledger.service.AcquisitionManageService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("acquisitionManageService")
public class AcquisitionManageServiceImpl implements AcquisitionManageService {
    @Resource
    private AcquisitionManageDAO acquisitionManageDAO;
    @Resource
    private SerialNumberDAO serialNumberDAO;
    @Resource
    private PurchasingEquipmentDAO purchasingEquipmentDAO;
    @Resource
    private ContractPurchaseDAO contractPurchaseDAO;
    @Resource
    private OperationRecordDAO operationRecordDAO;
    @Resource
    private UserInfoDAO userInfoDAO;
    @Resource
    private EquipmentLedgerDAO equipmentLedgerDAO;
    @Resource
    private InvoiceRecordDAO invoiceRecordDAO;
    @Resource
    private VerifyApprovalDAO verifyApprovalDAO;

    @Override
    public ReturnResult SubmitPurchaseRequisition(HttpServletRequest request, HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        JSONObject jsonObject = JSONObject.fromObject(request.getParameter("json"));
        //判断提交采购申请信息的用户是否是部门负责人，不是则没有权限提交
        String token = request.getParameter("token");
        UserLoginInfo userinfo = ToolUtil.getUserTokenInfo(token);
        if(null == userinfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userinfo.getUser_code());
        int job_title_status = Integer.parseInt(userMap.get("job_title_status").toString());//职位 0员工，1部门负责人
        if(MessageType.ZERO == job_title_status){
            //不是部门负责人
            result = ReturnResult.error();
            result.setMessage("操作失败，您没有该权限，请联系部门负责人操作");
            return result;
        }
        //判断申请部门与该申请人部门是否为同一个
        String apply_department = jsonObject.get("apply_department").toString();
        String department = userMap.get("department").toString();
        if(!apply_department.equals(department)){
            result = ReturnResult.error();
            result.setMessage("操作失败，申请部门与您所在部门不相符，请检查");
            return result;
        }
        if("综合管理部".equals(apply_department) || "微生物检测室".equals(apply_department) || "分子检测室".equals(apply_department)){
            jsonObject.put("dept_code","02");
        }
        jsonObject.put("apply_department_leader",userinfo.getUser_name());
        jsonObject.put("apply_department_leader_code",userinfo.getUser_code());
        jsonObject.put("apply_department_leader_time",ToolUtil.GetNowDateYMDString());
        jsonObject.put("create_time",ToolUtil.GetNowDateString());
        jsonObject.put("back_reason",MessageType.EMPTYSTR);
        acquisitionManageDAO.AddPurchaseRequisitionInfo(jsonObject);
        //添加用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",MessageType.EMPTYSTR);
        operationMap.put("operation_record","提交了设备："+jsonObject.get("device_name")+"的采购申请");
        operationMap.put("user_code",userinfo.getUser_code());
        operationMap.put("user_name",userinfo.getUser_name());
        operationMap.put("create_time",ToolUtil.GetNowDateString());
        operationRecordDAO.SaveUserOperationRecord(operationMap);
        return result;
    }

    @Override
    public ReturnResult FindPurchaseApprovalData(HttpServletRequest request, HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
            //ResponseUtils.renderJson(response,JSONObject.fromObject(result).toString());
        }
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        String approval_authority = userMap.get("approval_authority").toString();
        //int job_title_status = userInfo.getJob_title_status();
        List<Integer> application_status_list = new ArrayList<>();
        //int application_status = 0;
        if(!MessageType.EMPTYSTR.equals(approval_authority)){
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
        }
        if(application_status_list.size() == 0){
            application_status_list.add(MessageType.ZERO);
        }

        List<Map<String,Object>> listMap = acquisitionManageDAO.FindPurchaseRequisitionInfoByApplicationStatus(application_status_list);
        result.setObject(listMap);
        return result;
    }

    @Override
    public ReturnResult ActionPurchaseApproval(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        String id = request.getParameter("id");
        String token = request.getParameter("token");
        UserLoginInfo userinfo = ToolUtil.getUserTokenInfo(token);
        if(null == userinfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
            //ResponseUtils.renderJson(response,JSONObject.fromObject(result).toString());
        }
        int job_title_status = userinfo.getJob_title_status();
        //判断该职位是否符合当前审批操作
        Map<String,Object> purchaseMap = acquisitionManageDAO.FindPurchaseRequisitionInfoById(id);
        int application_status = Integer.parseInt(purchaseMap.get("application_status").toString());
        BigDecimal budget_amount = new BigDecimal(purchaseMap.get("budget_amount").toString());
        //判断申请状态不为4,申请状态为4则代表已被审批通过
        if(MessageType.FOUR == application_status){
            result = ReturnResult.error();
            result.setMessage("操作失败，该采购申请已通过审批，请勿重复操作");
            return result;
        }
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userinfo.getUser_code());
        String approval_authority = userMap.get("approval_authority").toString();
        int new_application_status = application_status+1;
        //首先判断该用户当前是否有权限对该采购信息进行审批(职位状态:6副总经理，7总经理，8董事长)
        if(!MessageType.EMPTYSTR.equals(approval_authority)){
            boolean bool = false;
            if(application_status == 1 && !approval_authority.contains(MessageType.STRSIX)){
                bool = true;
            }
            if(application_status == 2 && !approval_authority.contains(MessageType.STRSEVEN)){
                bool = true;
            }
            if(application_status == 3 && !approval_authority.contains(MessageType.STREIGTH)){
                bool = true;
            }
            if(bool){
                result = ReturnResult.error();
                result.setMessage("操作失败，该采购申请当前不属于该审批步骤，请检查");
                return result;
            }
        }else{
            result = ReturnResult.error();
            result.setMessage("操作失败，您没有权限审批该信息");
            return result;
        }
        //判断金额不大于30万，则不需要董事长审批
        if(budget_amount.compareTo(new BigDecimal(300000)) < 0 && application_status == 2){
            new_application_status = MessageType.FOUR;
        }
        //添加审批人信息并修改审批状态码
        String now_time = ToolUtil.GetNowDateString();
        String now_date = ToolUtil.GetNowDateYMDString();
        Map<String,Object> mapData = purchaseMap;
        mapData.put("id",id);
        if(application_status == 1){
            mapData.put("deputy_general_manager",userinfo.getUser_name());
            mapData.put("deputy_general_manager_code",userinfo.getUser_code());
            mapData.put("deputy_general_manager_time",now_date);
        }else{
            mapData.put("deputy_general_manager",purchaseMap.get("deputy_general_manager"));
            mapData.put("deputy_general_manager_code",purchaseMap.get("deputy_general_manager_code"));
            mapData.put("deputy_general_manager_time",purchaseMap.get("deputy_general_manager_time"));
        }
        if(application_status == 2){
            mapData.put("general_manager",userinfo.getUser_name());
            mapData.put("general_manager_code",userinfo.getUser_code());
            mapData.put("general_manager_time",now_date);
            //判断设备金额是否大于30万，不大于则不需要董事长审批
            if(new_application_status == MessageType.FOUR){
                mapData.put("chairman",MessageType.XIEGANG);
                mapData.put("chairman_code",MessageType.XIEGANG);
                mapData.put("chairman_time",MessageType.XIEGANG);
            }
        }else{
            mapData.put("general_manager",purchaseMap.get("general_manager"));
            mapData.put("general_manager_code",purchaseMap.get("general_manager_code"));
            mapData.put("general_manager_time",purchaseMap.get("general_manager_time"));
        }
        if(application_status == 3){
            mapData.put("chairman",userinfo.getUser_name());
            mapData.put("chairman_code",userinfo.getUser_code());
            mapData.put("chairman_time",now_date);
        }else{
            mapData.put("chairman",purchaseMap.get("chairman"));
            mapData.put("chairman_code",purchaseMap.get("chairman_code"));
            mapData.put("chairman_time",purchaseMap.get("chairman_time"));
        }
        mapData.put("application_status",new_application_status);
        mapData.put("update_time",now_time);
        acquisitionManageDAO.UpdatePurchaseApprovalInfo(mapData);
        //如果审批完成则需要生成该采购审核对应的流水号,及对应的采购信息
        if(MessageType.FOUR == new_application_status){
            //生成采购信息
            Map<String,Object> map_data = new HashMap<>();
            map_data.put("id2",id);
            map_data.put("device_name",purchaseMap.get("device_name"));
            map_data.put("trademark",purchaseMap.get("trademark"));
            map_data.put("device_model",purchaseMap.get("device_model"));
            map_data.put("supplier",purchaseMap.get("supplier"));
            map_data.put("order_quantity",purchaseMap.get("order_quantity"));
            map_data.put("apply_department",purchaseMap.get("apply_department"));
            map_data.put("dept_code",purchaseMap.get("dept_code"));
            map_data.put("create_time",now_time);
            map_data.put("main_purpose",purchaseMap.get("main_purpose"));
            purchasingEquipmentDAO.AddPurchasingEquipmentInfo(map_data);
        }
        //添加用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",MessageType.EMPTYSTR);
        operationMap.put("operation_record","审核通过了id为："+id+"的采购申请");
        operationMap.put("user_code",userinfo.getUser_code());
        operationMap.put("user_name",userinfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);
        //审核通过后需要通知固定资产管理员及财务，系统发送短信提醒
        //首先获取资产管理员及财务的手机号
        if(MessageType.FOUR == new_application_status){
            List<String> user_phone_list = userInfoDAO.FindAssetManagerMobilePhone(MessageType.STRONE);
            if(user_phone_list.size() > 0){
                SendMessagesUtil.SendActionSMS(user_phone_list,purchaseMap.get("apply_department")+"的采购申请已审批通过");
            }
        }

        return result;

    }

    @Override
    public ReturnResult ActionPurchaseApprovalFailed(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        String id = request.getParameter("id");
        String token = request.getParameter("token");
        String back_reason = request.getParameter("back_reason");//打回理由
        String now_time = ToolUtil.GetNowDateString();
        UserLoginInfo userinfo = ToolUtil.getUserTokenInfo(token);
        if(null == userinfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        //判断该职位是否符合当前审批操作
        Map<String,Object> purchaseMap = acquisitionManageDAO.FindPurchaseRequisitionInfoById(id);
        int application_status = Integer.parseInt(purchaseMap.get("application_status").toString());
        //判断申请状态不为4,申请状态为4则代表已被审批通过
        if(MessageType.FOUR == application_status){
            result = ReturnResult.error();
            result.setMessage("操作失败，该采购申请已通过审批，请勿重复操作");
            return result;
        }
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userinfo.getUser_code());
        String approval_authority = userMap.get("approval_authority").toString();
        //首先判断该用户当前是否有权限对该采购信息进行审批(职位状态:6副总经理，7总经理，8董事长)
        if(!MessageType.EMPTYSTR.equals(approval_authority)){
            boolean bool = false;
            if(application_status == 1 && !approval_authority.contains(MessageType.STRSIX)){
                bool = true;
            }
            if(application_status == 2 && !approval_authority.contains(MessageType.STRSEVEN)){
                bool = true;
            }
            if(application_status == 3 && !approval_authority.contains(MessageType.STREIGTH)){
                bool = true;
            }
            if(bool){
                result = ReturnResult.error();
                result.setMessage("操作失败，该采购申请当前不属于该审批步骤，请检查");
                return result;
            }
        }else{
            result = ReturnResult.error();
            result.setMessage("操作失败，您没有权限审批该信息");
            return result;
        }
        //将打回信息进行保存，并同步调整审批步骤状态码
        Map<String,Object> map_data = new HashMap<>();
        map_data.put("id",id);
        map_data.put("application_status",MessageType.ONE);
        map_data.put("back_status",MessageType.ONE);
        map_data.put("back_reason",back_reason);
        map_data.put("back_user_name",userinfo.getUser_name());
        map_data.put("back_user_code",userinfo.getUser_code());
        acquisitionManageDAO.RestoringApprovalInfo(map_data);

        //添加用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",JSONObject.fromObject(purchaseMap).toString());
        operationMap.put("operation_record","打回了id为："+id+"的采购申请，打回理由为："+back_reason);
        operationMap.put("user_code",userinfo.getUser_code());
        operationMap.put("user_name",userinfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);

        return result;
    }

    @Override
    public ReturnResult FindApproveCompletedPurchaseOrders(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        JSONArray jsonarray = JSONArray.fromObject(request.getParameter("aoData"));
        DataTable dataTable = ToolUtil.GetDataTableValue(jsonarray);
        //获取当前所有采购申请记录,只能查看自己部门所申请的数据
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        String department = userMap.get("department").toString();
        String dept_status = MessageType.EMPTYSTR;
        //判断当前查看信息的是否为综合管理的用户，是则可以查看实验部所有的申请（综合管理、微生物检测室、分子检测室）
        if("综合管理部".equals(department)){
            dept_status = "1";
        }
        Map<String,Object> map_data = new HashMap<>();
        map_data.put("dataTable",dataTable);
        map_data.put("apply_department",department);
        map_data.put("dept_status",dept_status);
        List<Map<String,Object>> listData = acquisitionManageDAO.FindAllApprovedPurchaseRequisitions(map_data);
        int total = acquisitionManageDAO.FindAllApprovedPurchaseRequisitionsTotal(map_data);
        //展示数据需要处理一下
        for(Map<String,Object> map : listData){
            int back_status = Integer.parseInt(map.get("back_status").toString());
            if(MessageType.ONE == back_status){
                map.put("project_progress","审批失败，请按照打回原因调整");
                continue;
            }
            int application_status = Integer.parseInt(map.get("application_status").toString());
            //获取当前审批进度
            if(MessageType.ONE == application_status){
                map.put("project_progress","副总经理待审批");
            }
            if(MessageType.TWO == application_status){
                map.put("project_progress","总经理待审批");
            }
            if(MessageType.THREE == application_status){
                map.put("project_progress","董事长待审批");
            }
            if(MessageType.FOUR == application_status){
                map.put("project_progress","审批完成");
            }
        }
        JSONObject getObj = new JSONObject();
        getObj.put("sEcho", dataTable.getsEcho());//
        getObj.put("iTotalRecords", total);//实际的行数
        getObj.put("iTotalDisplayRecords", total);//显示的行数,这个要和上面写的一样
        getObj.put("aaData", listData);//要以JSON格式返回
        result.setObject(getObj.toString());
        return result;

    }

    @Override
    public ReturnResult FindPurchaseEquipmentOrderData(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        //首先获取用户的所在部门，依据所在部门查看该部门的采购信息
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        String department = userMap.get("department").toString();
        JSONArray jsonarray = JSONArray.fromObject(request.getParameter("aoData"));
        DataTable dataTable = ToolUtil.GetDataTableValue(jsonarray);
        //判断当前用户是否是综合管理部员工，是则能看到所有实验部申请（综合管理、微生物检测室、分子检测室）
        String dept_status = MessageType.EMPTYSTR;
        if("综合管理部".equals(department)){
            dept_status = "1";
        }
        Map<String,Object> map_data = new HashMap<>();
        map_data.put("dataTable",dataTable);
        map_data.put("apply_department",department);
        map_data.put("dept_status",dept_status);
        List<Map<String,Object>> listMap = purchasingEquipmentDAO.FindAllPurchaseEquipmentOrder(map_data);
        int total = purchasingEquipmentDAO.FindAllPurchaseEquipmentOrderTotal(map_data);
        JSONObject getObj = new JSONObject();
        getObj.put("sEcho", dataTable.getsEcho());//
        getObj.put("iTotalRecords", total);//实际的行数
        getObj.put("iTotalDisplayRecords", total);//显示的行数,这个要和上面写的一样
        getObj.put("aaData", listMap);//要以JSON格式返回
        result.setObject(getObj.toString());
        return result;
    }

    @Override
    public ReturnResult FillPurchasedEquipmentInfo(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        JSONObject jsonObject = JSONObject.fromObject(request.getParameter("json"));
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
            //ResponseUtils.renderJson(response,JSONObject.fromObject(result).toString());
        }
        //首先判断是否拥有采购权限
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        int procurement_staff = Integer.parseInt(userMap.get("procurement_staff").toString());
        if(MessageType.ONE != procurement_staff){
            result = ReturnResult.error();
            result.setMessage("操作失败，您没有该权限");
            return result;
        }
        //判断该采购申请是否已生成采购合同
        String id = jsonObject.get("id").toString();
        Map<String,Object> mapData = purchasingEquipmentDAO.FindPurchasingEquipmentInfoByIdTwo(id);
        if(null != mapData && MessageType.ONE == Integer.parseInt(mapData.get("purchase_status").toString())){
            result = ReturnResult.error();
            result.setMessage("操作失败，该采购信息已生成采购合同，不可再进行修改");
            return result;
            //ResponseUtils.renderJson(response,JSONObject.fromObject(result).toString());
        }
        String now_time = ToolUtil.GetNowDateString();
        jsonObject.put("info_supplement_status",MessageType.ONE);
        jsonObject.put("user_code",userInfo.getUser_code());
        jsonObject.put("user_name",userInfo.getUser_name());
        jsonObject.put("update_time",now_time);
        purchasingEquipmentDAO.UpdatePurchasingEquipmentInfo(jsonObject);
        //需要同步修改采购申请中的信息补充状态
        Map<String,Object> map_data = new HashMap<>();
        map_data.put("id",id);
        map_data.put("info_supplement_status",MessageType.ONE);
        acquisitionManageDAO.SaveInformationSupplementStatus(map_data);
        //添加用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",JSONObject.fromObject(mapData).toString());
        operationMap.put("operation_record","补充完善了id为："+id+"的设备采购信息");
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);
        return result;
        //ResponseUtils.renderJson(response,JSONObject.fromObject(result).toString());

    }

    @Override
    public synchronized ReturnResult GeneratePurchaseContract(HttpServletRequest request, HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        JSONObject jsonObject = JSONObject.fromObject(request.getParameter("json"));
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        //判断用户是否拥有采购权限
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        int procurement_staff = Integer.parseInt(userMap.get("procurement_staff").toString());
        if(MessageType.ONE != procurement_staff){
            result = ReturnResult.error();
            result.setMessage("操作失败，您没有该权限");
            return result;
        }

        //判断该采购申请是否已生成采购合同且是否补充了采购信息
        Map<String,Object> mapData = purchasingEquipmentDAO.FindPurchasingEquipmentInfoById(jsonObject.getString("id"));
        if(null == mapData){
            result = ReturnResult.error();
            result.setMessage("操作失败，该采购信息不存在，请检查");
            return result;
        }
        if(null != mapData && MessageType.ONE == Integer.parseInt(mapData.get("purchase_status").toString())){
            result = ReturnResult.error();
            result.setMessage("操作失败，该采购信息已生成采购合同，不可重复生成");
            return result;
        }
        if(null != mapData && MessageType.ZERO == Integer.parseInt(mapData.get("info_supplement_status").toString())){
            result = ReturnResult.error();
            result.setMessage("操作失败，该采购信息未补充完善，请检查");
            return result;
        }
        //添加该采购信息的采购合同
        String now_time = ToolUtil.GetNowDateString();
        /*
        int year = Integer.parseInt(ToolUtil.GetNowDateYearString());
        Map<String,Object> map_data = new HashMap<>();
        map_data.put("start_time",year+"-01-01");
        map_data.put("end_time",(year+1)+"01-01");
        int total = contractPurchaseDAO.FindContractPurchaseNumber(map_data);
        String contract_code = "TK"+year+"-"+ToolUtil.GetStringNumberForValue(total+1,3);
         */
        String contract_code = jsonObject.getString("contract_code");
        //判断用户输入的采购合同编号是否已经存在
        Map<String,Object> map_data = contractPurchaseDAO.FindContractPurchaseByContractCode(contract_code);
        if(null != map_data){
            result = ReturnResult.error();
            result.setMessage("操作失败，该合同编号已存在，请检查");
            return result;
        }
        jsonObject.put("manager",userInfo.getUser_name());
        jsonObject.put("manager_code",userInfo.getUser_code());
        jsonObject.put("create_time",now_time);
        jsonObject.put("attachment_address",MessageType.EMPTYSTR);
        contractPurchaseDAO.AddContractPurchaseInfo(jsonObject);
        //生成合同后需要修改采购订单中的合同生成状态
        Map<String,Object> map = new HashMap<>();
        map.put("id",jsonObject.get("id"));
        map.put("purchase_status",MessageType.ONE);
        map.put("update_time",now_time);
        purchasingEquipmentDAO.UpdatePurchasingEquipmentPurchaseStatus(map);
        //修改采购审批表中的采购状态
        map.put("id",mapData.get("id2"));
        acquisitionManageDAO.UpdatePurchaseStatus(map);
        //添加用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",MessageType.EMPTYSTR);
        operationMap.put("operation_record","创建了id为："+jsonObject.get("id")+"的采购合同");
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);
        return result;
        //ResponseUtils.renderJson(response,JSONObject.fromObject(result).toString());

    }

    @Override
    public ReturnResult FindPurchaseContractData(HttpServletRequest request,HttpServletResponse response){
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
            case 3: key_name="signing_date";break;
            case 4: key_name="contract_amount";break;
            case 8: key_name="create_time";break;
        }
        dataTable.setSortTable(key_name);
        //首先获取用户的所在部门，依据所在部门查看该部门的采购信息
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        String department = userMap.get("department").toString();
        //判断当前用户是否是综合管理部员工，是则能看到所有实验部申请（综合管理、微生物检测室、分子检测室）
        String dept_status = MessageType.EMPTYSTR;
        if("综合管理部".equals(department)){
            dept_status = "1";
        }
        Map<String,Object> map_data = new HashMap<>();
        map_data.put("dataTable",dataTable);
        map_data.put("apply_department",department);
        map_data.put("dept_status",dept_status);
        List<Map<String,Object>> listMap = contractPurchaseDAO.FindAllContractPurchaseData(map_data);
        int total = contractPurchaseDAO.FindAllContractPurchaseDataTotal(map_data);
        JSONObject getObj = new JSONObject();
        getObj.put("sEcho", dataTable.getsEcho());//
        getObj.put("iTotalRecords", total);//实际的行数
        getObj.put("iTotalDisplayRecords", total);//显示的行数,这个要和上面写的一样
        getObj.put("aaData", listMap);//要以JSON格式返回
        result.setObject(getObj.toString());
        return result;

    }

    public ReturnResult VerifyLedgerData(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        String id = request.getParameter("id");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        //还需要验证一下该用户是否是设备管理员
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        if(MessageType.ONE != Integer.parseInt(userMap.get("device_admin").toString())){
            result = ReturnResult.error();
            result.setMessage("操作失败，您没有该权限，请联系设备管理员进行添加");
            return result;
        }
        //首先判断一下该设备信息是否已存在
        Map<String,Object> map = equipmentLedgerDAO.FindEquipmentLedgerByAssociateId(id);
        if(null != map){
            result = ReturnResult.error();
            result.setMessage("操作失败，已存在该设备信息");
            return result;
        }
        return result;
    }

    @Override
    public ReturnResult AddInvoiceRecord(HttpServletRequest request,HttpServletResponse response){
        //由前端验证，判断是否生成合同及发票，对应参数purchase_status为1且invoice_status为0才可添加
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        String id = request.getParameter("id");
        JSONObject jsonObject = JSONObject.fromObject(request.getParameter("json"));
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        String now_time = ToolUtil.GetNowDateString();
        Map<String, Object> mapData = new HashMap<>();
        mapData.put("item_detail", jsonObject.toString());
        mapData.put("associated_projects", MessageType.EMPTYSTR);
        mapData.put("associated_projects_two", MessageType.EMPTYSTR);
        mapData.put("associated_projects_name", MessageType.EMPTYSTR);
        mapData.put("use_table", MessageType.EMPTYSTR);
        mapData.put("association_id", MessageType.EMPTYSTR);
        mapData.put("create_time", now_time);
        mapData.put("operator", userInfo.getUser_name());
        mapData.put("user_account", MessageType.EMPTYSTR);
        mapData.put("source_status",MessageType.TWO);
        mapData.put("update_time", MessageType.EMPTYSTR);
        invoiceRecordDAO.AddInvoiceRecordInfo(mapData);
        //添加成功后需要修改该采购信息的发票状态
        purchasingEquipmentDAO.UpdateInvoiceStatusByid(id);
        //添加用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",MessageType.EMPTYSTR);
        operationMap.put("operation_record","添加了id为："+id+"的采购信息的发票信息");
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);

        return result;
    }

    @Override
    public ReturnResult SubmitAcceptanceApplication(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        JSONObject jsonObject = JSONObject.fromObject(request.getParameter("json"));
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        String id = jsonObject.get("id").toString();
        String instrument_serial_number = jsonObject.get("instrument_serial_number").toString().trim();
        //获取该采购数量
        Map<String,Object> mapData = purchasingEquipmentDAO.FindPurchasingEquipmentInfoById(id);
        int order_quantity = Integer.parseInt(mapData.get("order_quantity").toString());
        //首先判断该设备是否已经存在正在验证的审批
        List<Map<String,Object>> verifyList = verifyApprovalDAO.FindVerifyApprovalById2(id);
        if(order_quantity == verifyList.size()){
            result = ReturnResult.error();
            result.setMessage("操作失败，该采购设备已全部进行验证审批，请检查");
            return result;
        }
        //验证用户输入的设备序列号是否存在一致的情况
        if(MessageType.EMPTYSTR.equals(instrument_serial_number)){
            result = ReturnResult.error();
            result.setMessage("操作失败，该设备序列号不得为空，请检查");
            return result;
        }
        for(Map<String,Object> map : verifyList){
            if(instrument_serial_number.equals(map.get("instrument_serial_number").toString())){
                result = ReturnResult.error();
                result.setMessage("操作失败，该设备序列号已存在，请检查");
                return result;
            }
        }

        //判断申请人是否是该部门负责人，是则直接代入审批人
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        int job_title_status = Integer.parseInt(userMap.get("job_title_status").toString());
        String use_department_leader = MessageType.EMPTYSTR;
        String use_department_leader_code = MessageType.EMPTYSTR;
        String use_department_leader_time = MessageType.EMPTYSTR;
        int application_status = MessageType.ZERO;
        String applicant = userInfo.getUser_name();
        String applicant_code = userInfo.getUser_code();
        String now_time = ToolUtil.GetNowDateString();
        if(MessageType.ONE == job_title_status){
            use_department_leader = userInfo.getUser_name();
            use_department_leader_code = userInfo.getUser_code();
            use_department_leader_time = ToolUtil.GetNowDateYMDString();
            application_status = MessageType.ONE;
        }
        jsonObject.put("use_department_leader",use_department_leader);
        jsonObject.put("use_department_leader_code",use_department_leader_code);
        jsonObject.put("use_department_leader_time",use_department_leader_time);
        jsonObject.put("application_status",application_status);
        jsonObject.put("applicant",applicant);
        jsonObject.put("applicant_code",applicant_code);
        jsonObject.put("create_time",now_time);
        jsonObject.put("back_reason",MessageType.EMPTYSTR);
        jsonObject.put("unit_price",new BigDecimal(mapData.get("unit_price").toString()));
        jsonObject.put("maintenance_period",mapData.get("maintenance_period"));
        verifyApprovalDAO.SaveVerifyApprovalInfo(jsonObject);
        //保存其验收数量
        int acceptance_number = Integer.parseInt(mapData.get("acceptance_number").toString());
        Map<String,Object> map_data = new HashMap<>();
        map_data.put("acceptance_number",acceptance_number+1);
        map_data.put("id",id);
        purchasingEquipmentDAO.UpdateAcceptanceNumberById(map_data);
        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",MessageType.EMPTYSTR);
        operationMap.put("operation_record","添加了id为："+jsonObject.get("id")+"的采购信息的设备验收申请");
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);
        return result;
    }

    @Override
    public ReturnResult FindVerifyApprovalData(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
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
        //首先获取用户的所在部门，依据所在部门查看该部门的采购信息
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        String department = userMap.get("department").toString();
        //判断当前用户是否是综合管理部员工，是则能看到所有实验部申请（综合管理、微生物检测室、分子检测室）
        String dept_status = MessageType.EMPTYSTR;
        if("综合管理部".equals(department)){
            dept_status = "1";
        }
        Map<String,Object> map_data = new HashMap<>();
        map_data.put("dataTable",dataTable);
        map_data.put("use_department",department);
        map_data.put("dept_status",dept_status);
        List<Map<String,Object>> listMap = verifyApprovalDAO.FindAllVerifyApprovalData(map_data);
        int total = verifyApprovalDAO.FindAllVerifyApprovalDataTotal(map_data);
        //整理当前审核状态
        for(Map<String,Object> map : listMap){
            int back_status = Integer.parseInt(map.get("back_status").toString());
            if(MessageType.ONE == back_status){
                map.put("review_progress","审批失败，请按照打回原因调整");
                continue;
            }
            int application_status = Integer.parseInt(map.get("application_status").toString());
            if(MessageType.ZERO == application_status){
                map.put("review_progress","部门负责人待审批");
            }
            if(MessageType.ONE == application_status){
                map.put("review_progress","设备管理员待审批");
            }
            if(MessageType.TWO == application_status){
                map.put("review_progress","资产管理员待审批");
            }
            if(MessageType.THREE == application_status){
                map.put("review_progress","副总经理待审批");
            }
            if(MessageType.FOUR == application_status){
                map.put("review_progress","总经理待审批");
            }
            if(MessageType.FIVE == application_status){
                map.put("review_progress","审批通过");
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
    public ReturnResult FindAcceptanceApplicationData(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        //获取用户权限
        String department = userMap.get("department").toString();
        String dept_code = userMap.get("dept_code").toString();
        //综合管理员可以查看实验室整个部门的申请数据
        if("综合管理部".equals(department)){
            dept_code = "02";
        }
        int device_admin = Integer.parseInt(userMap.get("device_admin").toString());
        int fixed_asset_admin = Integer.parseInt(userMap.get("fixed_asset_admin").toString());
        String approval_authority = userMap.get("approval_authority").toString();
        String department_head = MessageType.EMPTYSTR;//部门负责人
        String device_admin_status = MessageType.EMPTYSTR;//设备管理员状态
        List<Integer> application_status_list = new ArrayList<>();//审批权限
        if(!MessageType.EMPTYSTR.equals(approval_authority)){
            //判断用户审批权限，依据审批权限不同展示当前该用户需要审批的数据
            if(approval_authority.contains(MessageType.STRSIX)){
                //副总
                application_status_list.add(MessageType.THREE);
            }
            if(approval_authority.contains(MessageType.STRSEVEN)){
                //总经理
                application_status_list.add(MessageType.FOUR);
            }
            if(approval_authority.contains(MessageType.STRONE)){
                //部门负责人
                department_head = MessageType.STRONE;
            }
        }
        //判断是否是资产管理员
        if(MessageType.ONE == fixed_asset_admin){
            application_status_list.add(MessageType.TWO);
        }
        //判断是否是设备管理员
        if(MessageType.ONE == device_admin){
            device_admin_status = MessageType.STRONE;
        }
        //该用户没有审批权限则给0，防止系统报错
        if(application_status_list.size() == 0){
            application_status_list.add(MessageType.ZERO);
        }
        Map<String,Object> mapData = new HashMap<>();
        mapData.put("application_status",application_status_list);
        mapData.put("dept_code",dept_code);
        mapData.put("use_department",department);
        mapData.put("department_head",department_head);
        mapData.put("device_admin_status",device_admin_status);
        List<Map<String,Object>> listMap = verifyApprovalDAO.FindAcceptanceApplicationData(mapData);
        result.setObject(listMap);
        return result;
    }

    @Override
    public ReturnResult ActionAcceptanceApproval(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.error();
        String token = request.getParameter("token");
        String id = request.getParameter("id");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        //根据id获取设备验收信息
        Map<String,Object> verifyMap = verifyApprovalDAO.FindFindVerifyApprovalById(id);
        String use_department = verifyMap.get("use_department").toString();
        String dept_code = verifyMap.get("dept_code").toString();
        int application_status = Integer.parseInt(verifyMap.get("application_status").toString());
        int new_application_status = application_status+1;
        //获取用户权限
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        String department = userMap.get("department").toString();
        int device_admin = Integer.parseInt(userMap.get("device_admin").toString());
        int fixed_asset_admin = Integer.parseInt(userMap.get("fixed_asset_admin").toString());
        String approval_authority = userMap.get("approval_authority").toString();
        Map<String,Object> map_data = verifyMap;
        String now_date = ToolUtil.GetNowDateYMDString();
        String now_time = ToolUtil.GetNowDateString();
        Boolean bool = false;
        //判断当前审批状态
        if(MessageType.ZERO == application_status){
            //部门负责人
            if(!use_department.equals(department)){
                bool = true;
            }
            if(!approval_authority.contains(MessageType.STRONE)){
                bool = true;
            }
            map_data.put("use_department_leader",userInfo.getUser_name());
            map_data.put("use_department_leader_code",userInfo.getUser_code());
            map_data.put("use_department_leader_time",now_date);
        }
        if(MessageType.ONE == application_status){
            //设备管理员，综合管理部可以查看整个实验部的申请数据
            if("综合管理部".equals(department)){
                if(!"02".equals(dept_code)){
                    bool = true;
                }
            }else{
                if(!use_department.equals(department)){
                    bool = true;
                }
            }
            if(MessageType.ZERO == device_admin){
                bool = true;
            }
            map_data.put("device_admin",userInfo.getUser_name());
            map_data.put("device_admin_code",userInfo.getUser_code());
            map_data.put("device_admin_time",now_date);
        }
        if(MessageType.TWO == application_status){
            if(MessageType.ONE != fixed_asset_admin){
                bool = true;
            }
            map_data.put("asset_admin",userInfo.getUser_name());
            map_data.put("asset_admin_code",userInfo.getUser_code());
            map_data.put("asset_admin_time",now_date);
        }
        if(MessageType.THREE == application_status){
            if(!approval_authority.contains(MessageType.STRSIX)){
                bool = true;
            }
            map_data.put("deputy_general_manager",userInfo.getUser_name());
            map_data.put("deputy_general_manager_code",userInfo.getUser_code());
            map_data.put("deputy_general_manager_time",now_date);
        }
        if(MessageType.FOUR == application_status){
            if(!approval_authority.contains(MessageType.STRSEVEN)){
                bool = true;
            }
            map_data.put("general_manager",userInfo.getUser_name());
            map_data.put("general_manager_code",userInfo.getUser_code());
            map_data.put("general_manager_time",now_date);
        }
        if(MessageType.FIVE == application_status){
            result.setMessage("操作失败，该验收申请已通过审批，请勿重复操作");
            return result;
        }
        if(bool){
            result.setMessage("操作失败，您没有该权限");
            return result;
        }
        //判断当前审批是否处于设备管理员审批，判断该设备金额是否超过2000
        if(MessageType.ONE == application_status){
            BigDecimal total_price = new BigDecimal(verifyMap.get("total_price").toString());
            if(total_price.compareTo(new BigDecimal(2000)) < 0){
                //金额不大于2000
                new_application_status = new_application_status+1;
                map_data.put("asset_admin",MessageType.XIEGANG);
                map_data.put("asset_admin_code",MessageType.XIEGANG);
                map_data.put("asset_admin_time",MessageType.XIEGANG);
            }
        }
        map_data.put("application_status",new_application_status);
        verifyApprovalDAO.ApprovalAcceptanceApplication(map_data);
        //判断是否审批完成，完成后才可将设备信息添加到台账中
        if(MessageType.FIVE == new_application_status){
            purchasingEquipmentDAO.UpdateAcceptanceStatusById(verifyMap.get("id2").toString());
        }
        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",JSONObject.fromObject(verifyMap).toString());
        operationMap.put("operation_record","审批了id为："+id+"的设备验收申请");
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);
        return ReturnResult.success();
    }

    @Override
    public ReturnResult ActionAcceptanceApprovalFailed(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.error();
        String token = request.getParameter("token");
        String id = request.getParameter("id");
        String back_reason = request.getParameter("back_reason");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        //根据id获取设备验收信息
        Map<String,Object> verifyMap = verifyApprovalDAO.FindFindVerifyApprovalById(id);
        String use_department = verifyMap.get("use_department").toString();
        String dept_code = verifyMap.get("dept_code").toString();
        int application_status = Integer.parseInt(verifyMap.get("application_status").toString());
        //获取用户权限
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        String department = userMap.get("department").toString();
        int device_admin = Integer.parseInt(userMap.get("device_admin").toString());
        int fixed_asset_admin = Integer.parseInt(userMap.get("fixed_asset_admin").toString());
        String approval_authority = userMap.get("approval_authority").toString();
        String now_time = ToolUtil.GetNowDateString();
        Boolean bool = false;
        //判断当前审批状态
        if(MessageType.ZERO == application_status){
            //部门负责人
            if(!use_department.equals(department)){
                bool = true;
            }
            if(!approval_authority.contains(MessageType.STRONE)){
                bool = true;
            }
        }
        if(MessageType.ONE == application_status){
            //设备管理员，综合管理部可以查看整个实验部的申请数据
            if("综合管理部".equals(department)){
                if(!"02".equals(dept_code)){
                    bool = true;
                }
            }else{
                if(!use_department.equals(department)){
                    bool = true;
                }
            }
            if(MessageType.ZERO == device_admin){
                bool = true;
            }
        }
        if(MessageType.TWO == application_status){
            if(MessageType.ONE != fixed_asset_admin){
                bool = true;
            }
        }
        if(MessageType.THREE == application_status){
            if(!approval_authority.contains(MessageType.STRSIX)){
                bool = true;
            }
        }
        if(MessageType.FOUR == application_status){
            if(!approval_authority.contains(MessageType.STRSEVEN)){
                bool = true;
            }
        }
        if(MessageType.FIVE == application_status){
            result.setMessage("操作失败，该验收申请已通过审批，请勿重复操作");
            return result;
        }
        if(bool){
            result.setMessage("操作失败，您没有该权限");
            return result;
        }
        //还原设备验收申请审批状态码
        Map<String,Object> map_data = new HashMap<>();
        map_data.put("id",id);
        map_data.put("application_status",MessageType.ZERO);
        map_data.put("back_status",MessageType.ONE);
        map_data.put("back_reason",back_reason);
        map_data.put("back_user_name",userInfo.getUser_name());
        map_data.put("back_user_code",userInfo.getUser_code());
        verifyApprovalDAO.RestoreAcceptanceApplicationInfo(map_data);

        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",JSONObject.fromObject(verifyMap).toString());
        operationMap.put("operation_record","打回了id为："+id+"的设备验收申请，打回原因为："+back_reason);
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);

        return ReturnResult.success();
    }

    @Override
    public synchronized ReturnResult UpdatePurchaseRequisitionInfo(HttpServletRequest request,HttpServletResponse response){
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
        Map<String,Object> purchaseMap = acquisitionManageDAO.FindPurchaseRequisitionInfoById(id);
        //判断一下该操作人与原申请人是否为同一人
        if(!userInfo.getUser_code().equals(purchaseMap.get("apply_department_leader_code").toString())){
            result = ReturnResult.error();
            result.setMessage("操作失败，您不是该申请原申请人，请检查");
            return result;
        }
        //需要判断一下是否是打回的
        int back_status = Integer.parseInt(purchaseMap.get("back_status").toString());
        if(MessageType.ONE != back_status){
            result = ReturnResult.error();
            result.setMessage("操作失败，该申请未被打回，不能进行修改");
            return result;
        }
        //保存修改后的申请信息
        jsonObject.put("update_time",now_time);
        jsonObject.put("back_status",MessageType.ZERO);
        acquisitionManageDAO.UpdatePurchaseRequisitionInfo(jsonObject);

        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",JSONObject.fromObject(purchaseMap).toString());
        operationMap.put("operation_record","修改了id为："+id+"的设备采购申请信息");
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);

        return result;
    }

    @Override
    public ReturnResult UpdateAcceptanceApplicationInfo(HttpServletRequest request,HttpServletResponse response){
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
        Map<String,Object> verifyMap = verifyApprovalDAO.FindFindVerifyApprovalById(id);
        //判断一下该操作人与原申请人是否为同一人
        if(!userInfo.getUser_code().equals(verifyMap.get("applicant_code").toString())){
            result = ReturnResult.error();
            result.setMessage("操作失败，您不是该申请原申请人，请检查");
            return result;
        }
        //需要判断一下是否是打回的
        int back_status = Integer.parseInt(verifyMap.get("back_status").toString());
        if(MessageType.ONE != back_status){
            result = ReturnResult.error();
            result.setMessage("操作失败，该申请未被打回，不能进行修改");
            return result;
        }
        //保存修改后的申请信息
        jsonObject.put("back_status",MessageType.ZERO);
        verifyApprovalDAO.UpdateAcceptanceApplicationInfo(jsonObject);

        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",JSONObject.fromObject(verifyMap).toString());
        operationMap.put("operation_record","修改了id为："+id+"的设备验收申请信息");
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);

        return result;
    }


}
