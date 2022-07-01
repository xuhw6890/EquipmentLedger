package com.tianke.equipmentledger.service.impl;

import com.tianke.equipmentledger.context.MessageType;
import com.tianke.equipmentledger.context.ResponseUtils;
import com.tianke.equipmentledger.context.ReturnResult;
import com.tianke.equipmentledger.context.ToolUtil;
import com.tianke.equipmentledger.dao.*;
import com.tianke.equipmentledger.entity.DataTable;
import com.tianke.equipmentledger.entity.UserLoginInfo;
import com.tianke.equipmentledger.service.EquipmentLedgerService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("equipmentLedgerService")
public class EquipmentLedgerServiceImpl implements EquipmentLedgerService {
    @Resource
    private EquipmentLedgerDAO equipmentLedgerDAO;
    @Resource
    private UserInfoDAO userInfoDAO;
    @Resource
    private ScrapApprovalDAO scrapApprovalDAO;
    @Resource
    private PurchasingEquipmentDAO purchasingEquipmentDAO;
    @Resource
    private OperationRecordDAO operationRecordDAO;
    @Resource
    private VerifyApprovalDAO verifyApprovalDAO;

    @Override
    public ReturnResult FindEquipmentLedgerData(HttpServletRequest request, HttpServletResponse response){
        //台账模块各部门查看各部门的数据，综合管理部可查看整个实验部的数据，固定资产管理员可查看所有数据
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        String device_status = request.getParameter("device_status");
        String fixed_assets = request.getParameter("fixed_assets");
        JSONArray jsonarray = JSONArray.fromObject(request.getParameter("aoData"));
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }

        DataTable dataTable = ToolUtil.GetDataTableValue(jsonarray);
        int ruleNumber = dataTable.getiSortCol();
        String key_name ="id";
        switch (ruleNumber) {
            case 3: key_name="signing_date";break;
            case 4: key_name="contract_amount";break;
            case 8: key_name="create_time";break;
        }
        dataTable.setSortTable(key_name);
        //获取用户信息
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        String department = userMap.get("department").toString();
        String dept_code = MessageType.EMPTYSTR;
        String asset_status = MessageType.STRONE;
        int fixed_asset_admin = Integer.parseInt(userMap.get("fixed_asset_admin").toString());
        if(MessageType.ONE == fixed_asset_admin){
            asset_status = MessageType.EMPTYSTR;
        }
        if("综合管理部".equals(department)){
            dept_code = "02";
        }
        Map<String,Object> map_data = new HashMap<>();
        map_data.put("dataTable",dataTable);
        map_data.put("device_status",device_status);
        map_data.put("fixed_assets",fixed_assets);
        map_data.put("use_department",department);
        map_data.put("dept_code",dept_code);
        map_data.put("asset_status",asset_status);
        List<Map<String,Object>> listMap = equipmentLedgerDAO.FindAllEquipmentLedger(map_data);
        int total = equipmentLedgerDAO.FindAllEquipmentLedgerTotal(map_data);
        //整理固定资产状态，未填写固定资产编号的设备需要高亮显示
        for(Map<String,Object> map: listMap){
            String fixed_asset_number = map.get("fixed_asset_number").toString();
            int fixed_assets_status = Integer.parseInt(map.get("fixed_assets_status").toString());
            if(MessageType.EMPTYSTR.equals(fixed_asset_number) && MessageType.ONE == fixed_assets_status){
                map.put("fixed_assets",MessageType.ONE);
            }else{
                map.put("fixed_assets",MessageType.ZERO);
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
    public ReturnResult AddEquipmentLedgerInfo(HttpServletRequest request,HttpServletResponse response){
        //从设备验收模块添加到台账页面(需要填写其余信息)，前端判断该验收状态application_status 为5则可以添加该设备信息
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        JSONObject jsonObject = JSONObject.fromObject(request.getParameter("json"));
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
        Map<String,Object> map = equipmentLedgerDAO.FindEquipmentLedgerByAssociateId(jsonObject.get("associate_id").toString());
        if(null != map){
            result = ReturnResult.error();
            result.setMessage("操作失败，已存在该设备信息");
            return result;
        }
        //判断使用部门是否为（综合管理部、微生物检测室、分子检测室）是则需要将部门编号调整成02以便综合管理部进行管理
        String use_department = jsonObject.get("use_department").toString();
        if(MessageType.ZONGHEGUANLI.equals(use_department) || MessageType.WEISHENGWUJIANCE.equals(use_department)
                || MessageType.FENZIJIANCE.equals(use_department)){
            jsonObject.put("dept_code","02");
        }
        //将数据进行保存
        //根据设备编号查询该设备编号是否存在
        String device_id = jsonObject.get("device_id").toString();
        Map<String,Object> mapData = equipmentLedgerDAO.FindEquipmentLedgerByDeviceId(device_id);
        if(null != mapData){
            result = ReturnResult.error();
            result.setMessage("操作失败，该设备编号已存在，请检查");
            return result;
        }
        //判断该设备是否属于固定资产，即总额大于2000元
        int fixed_assets_status = MessageType.ZERO;
        BigDecimal total_price = new BigDecimal(jsonObject.get("total_price").toString());
        if(total_price.compareTo(new BigDecimal(2000)) >= 0){
            fixed_assets_status = MessageType.ONE;
        }
        jsonObject.put("fixed_assets_status",fixed_assets_status);
        String now_time = ToolUtil.GetNowDateString();
        jsonObject.put("user_name",userInfo.getUser_name());
        jsonObject.put("user_code",userInfo.getUser_code());
        jsonObject.put("create_time",now_time);
        equipmentLedgerDAO.AddEquipmentLedgerInfo(jsonObject);
        //需要保存其台账状态码
        verifyApprovalDAO.UpdateLedgerStatus(jsonObject.get("associate_id").toString());
        //添加用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",MessageType.EMPTYSTR);
        operationMap.put("operation_record","台账管理页面添加了一条设备台账信息，设备编号为："+device_id);
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);
        return result;

    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnResult UpdateEquipmentLedgerInfo(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.error();
        String token = request.getParameter("token");
        String modify_fields = request.getParameter("modify_fields");//修改字段，0为常规数据，1为固定资产编号
        JSONObject jsonObject = JSONObject.fromObject(request.getParameter("json"));
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            result = ReturnResult.NotExist();
            return result;
        }
        Map<String,Object> mapData = equipmentLedgerDAO.FindEquipmentLedgerById(jsonObject.get("id").toString());
        //还需要验证一下该用户是否是设备管理员
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        if(MessageType.STRZERO.equals(modify_fields)){
            if(MessageType.ONE != Integer.parseInt(userMap.get("device_admin").toString())){
                result.setMessage("操作失败，您没有该权限，请联系设备管理员进行修改");
                return result;
            }
            //修改常规数据，判断是否把固定资产编号也传递过来了
            if(!jsonObject.get("fixed_asset_number").toString().equals(mapData.get("fixed_asset_number").toString())){
                result.setMessage("操作失败，您存在违规操作，请重新执行");
                return result;
            }
            //判断使用部门是否是（综合管理部、微生物检测室、分子检测室），是则需要将部门编号调整成02，方便综合管理部进行管理
            String use_department = jsonObject.get("use_department").toString();
            if(MessageType.ZONGHEGUANLI.equals(use_department) || MessageType.WEISHENGWUJIANCE.equals(use_department)
                    ||MessageType.FENZIJIANCE.equals(use_department)){
                jsonObject.put("dept_code","02");
            }
        }else{
            if(MessageType.ONE != Integer.parseInt(userMap.get("fixed_asset_admin").toString())){
                result.setMessage("操作失败，您没有该权限，请联系固定资产管理员进行修改");
                return result;
            }
            //判断该设备价值是超过2000元（价值2000及以上才为固定资产）
            String associate_id = jsonObject.get("associate_id").toString();
            //关联id为0的代表是外部导入的台账历史数据，默认可以修改固定资产编号
            if(!MessageType.STRZERO.equals(associate_id)){
                /*
                Map<String,Object> map = purchasingEquipmentDAO.FindPurchasingEquipmentInfoById(associate_id);
                BigDecimal total_price = new BigDecimal(map.get("total_price").toString());
                if(total_price.compareTo(new BigDecimal(2000)) < 0){
                    result.setMessage("操作失败，该设备价值不超过2000元，不需要填写固定资产编号");
                    return result;
                }
                 */
                if(MessageType.ZERO == Integer.parseInt(jsonObject.get("fixed_assets_status").toString())){
                    result.setMessage("操作失败，该设备价值不超过2000元，不需要填写固定资产编号");
                    return result;
                }
            }
            //判断该固定资产编号是否存在
            Map<String,Object> ledgerInfo = equipmentLedgerDAO.FindLedgerInfoByFixedAssetNumber(jsonObject.get("fixed_asset_number").toString());
            if(null != ledgerInfo){
                result.setMessage("操作失败，该固定资产编号已被使用，请检查");
                return result;
            }

        }

        String now_time = ToolUtil.GetNowDateString();
        jsonObject.put("update_user_name",userInfo.getUser_name());
        jsonObject.put("update_user_code",userInfo.getUser_code());
        jsonObject.put("update_time",now_time);
        if(MessageType.STRZERO.equals(modify_fields)){
            equipmentLedgerDAO.UpdateEquipmentLedgerInfo(jsonObject);
        }else{
            equipmentLedgerDAO.UpdateLedgerFixedAssetNumber(jsonObject);
        }

        //添加用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",JSONObject.fromObject(mapData).toString());
        if(MessageType.STRZERO.equals(modify_fields)){
            operationMap.put("operation_record","修改了id为："+jsonObject.get("id")+"的台账信息");
        }else{
            operationMap.put("operation_record","修改了台账信息中id为："+jsonObject.get("id")+"的固定资产编号");
        }
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);
        return ReturnResult.success();

    }

    @Override
    public synchronized ReturnResult DeleteEquipmentLedgerInfo(HttpServletRequest request,HttpServletResponse response){
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
            result.setMessage("操作失败，您没有该权限，请联系设备管理员进行删除");
            return result;
        }
        //需要判断该设备是否是真的报废了，即该设备是否通过停用/报废审批信息
        Map<String,Object> scrapMap = scrapApprovalDAO.FindScrapApprovalInfoById2(id);
        if(null == scrapMap){
            result = ReturnResult.error();
            result.setMessage("操作失败，该设备未报废/停用，不能删除");
            return result;
        }
        Map<String,Object> ledgerMap = equipmentLedgerDAO.FindEquipmentLedgerById(id);
        int device_status = Integer.parseInt(ledgerMap.get("device_status").toString());
        if(MessageType.ONE != device_status){
            result = ReturnResult.error();
            result.setMessage("操作失败，该设备暂未通过报废/停用审批，请联系相关审批人员");
            return result;
        }
        //将设备在台账中进行删除
        String now_time = ToolUtil.GetNowDateString();
        Map<String,Object> mapData = new HashMap<>();
        mapData.put("id",id);
        mapData.put("device_status",MessageType.ONE);
        mapData.put("update_user_name",userInfo.getUser_name());
        mapData.put("update_user_code",userInfo.getUser_code());
        mapData.put("update_time",now_time);
        equipmentLedgerDAO.ActionDeviceDeactivation(mapData);
        return result;


    }




}
