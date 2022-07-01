package com.tianke.equipmentledger.controller;

import com.tianke.equipmentledger.context.MessageType;
import com.tianke.equipmentledger.context.ReturnResult;
import com.tianke.equipmentledger.context.ToolUtil;
import com.tianke.equipmentledger.dao.*;
import com.tianke.equipmentledger.entity.UserLoginInfo;
import net.sf.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/Reminder")
public class ExpirationReminderController {
    @Resource
    private ExpirationReminderDAO expirationReminderDAO;
    @Resource
    private VerificationManageDAO verificationManageDAO;
    @Resource
    private CalibrationManageDAO calibrationManageDAO;
    @Resource
    private EquipmentLedgerDAO equipmentLedgerDAO;
    @Resource
    private OperationRecordDAO operationRecordDAO;

    /**
     * 查看报警期限数据
     * @param request
     * @return
     */
    @GetMapping("/FindReminderPeriodData")
    public ReturnResult FindReminderPeriodData(HttpServletRequest request){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo){
            return ReturnResult.NotExist();
        }
        Map<String,Object> reminderMap = expirationReminderDAO.FindAlarmReminderData();
        result.setObject(reminderMap);
        return result;
    }

    /**
     * 修改报警提醒期限
     * @param request
     * @return
     */
    @GetMapping("/UpdateReminderPeriodData")
    public ReturnResult UpdateReminderPeriodData(HttpServletRequest request){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo){
            return ReturnResult.NotExist();
        }
        //修改报警期限是否需要设置权限人员进行修改
        Map<String,Object> reminderMap = expirationReminderDAO.FindAlarmReminderData();
        String now_time = ToolUtil.GetNowDateString();
        JSONObject jsonObject = JSONObject.fromObject(request.getParameter("json"));
        jsonObject.put("user_name",userInfo.getUser_name());
        jsonObject.put("update_time",now_time);
        expirationReminderDAO.UpdateAlarmReminderData(jsonObject);
        //保存用户操作记录
        Map<String,Object> operationMap = new HashMap<>();
        operationMap.put("id2",MessageType.EMPTYSTR);
        operationMap.put("raw_data",JSONObject.fromObject(reminderMap).toString());
        operationMap.put("operation_record","修改了报警提醒期限");
        operationMap.put("user_code",userInfo.getUser_code());
        operationMap.put("user_name",userInfo.getUser_name());
        operationMap.put("create_time",now_time);
        operationRecordDAO.SaveUserOperationRecord(operationMap);

        return result;
    }

    /**
     * 获取到了报警提醒期限的设备
     * @param request
     * @return
     */
    @GetMapping("/FindExpirationReminder")
    public ReturnResult FindExpirationReminder(HttpServletRequest request){
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo){
            return ReturnResult.NotExist();
        }
        String now_time = ToolUtil.GetNowDateYMDString();
        ReturnResult result = ReturnResult.success();
        Map<String,Object> backMap = new HashMap<>();
        //首先拿到各提醒期限
        Map<String,Object> reminderMap = expirationReminderDAO.FindAlarmReminderData();
        int calibration_reminder_date = Integer.parseInt(reminderMap.get("calibration_reminder_date").toString());//校准提醒
        int maintenance_reminder_date = Integer.parseInt(reminderMap.get("maintenance_reminder_date").toString());//维保提醒
        int verification_reminder_date = Integer.parseInt(reminderMap.get("verification_reminder_date").toString());//期间核查提醒
        List<Map<String,Object>> ledgerList = equipmentLedgerDAO.FindAllEquipmentLedgerData();
        //校准提醒
        if(calibration_reminder_date > MessageType.ZERO){
            //需要拿到各设备的校准到期时间
            List<String> listMap = new ArrayList<>();
            /*
            List<Map<String,Object>> closedCalibrationList = calibrationManageDAO.FindCompletedCalibrationPlan();//已完成的计划
            List<Map<String,Object>> unfinishedCalibrationList = calibrationManageDAO.FindUnfinishedCalibrationPlan();//正在进行的校准计划
            Map<String,Object> idMap = new HashMap<>();
            for(Map<String,Object> map : unfinishedCalibrationList){
                idMap.put(map.get("id2").toString(),map.get("id2"));
            }
            for(Map<String,Object> map : closedCalibrationList){
                if(null == idMap.get(map.get("id2"))){
                    //没有正在进行的校准计划，即判断该计划是否到了提醒期限
                    String next_date = map.get("next_date").toString();
                    String reminder_date = ToolUtil.getSpecifiedDayBefore(next_date,verification_reminder_date);
                    if(ToolUtil.compare_date(reminder_date,now_time)){
                        //当前时间小于等于提醒期限
                        listMap.add(map);
                    }
                }
            }*/

            for(Map<String,Object> map : ledgerList){
                //没有正在执行的校准计划
                if(MessageType.EMPTYSTR.equals(map.get("next_calibration_date"))){
                    continue;
                }
                String reminder_date = ToolUtil.getSpecifiedDayBefore(map.get("next_calibration_date").toString(),maintenance_reminder_date);
                if(ToolUtil.compare_date(now_time,reminder_date)){
                    //当前时间小于等于提醒期限
                    listMap.add(map.get("device_id").toString());
                }

            }
            backMap.put("calibration",listMap);
        }
        //维保提醒
        if(maintenance_reminder_date > MessageType.ZERO){
            List<String> listMap = new ArrayList<>();
            for(Map<String,Object> map : ledgerList){
                if(MessageType.EMPTYSTR.equals(map.get("maintenance_due_date"))){
                    continue;
                }
                String reminder_date = ToolUtil.getSpecifiedDayBefore(map.get("maintenance_due_date").toString(),maintenance_reminder_date);
                if(ToolUtil.compare_date(now_time,reminder_date)){
                    //当前时间小于等于提醒期限
                    listMap.add(map.get("device_id").toString());
                }
            }
            backMap.put("maintenance",listMap);

        }
        //期间核查提醒
        if(verification_reminder_date > MessageType.ZERO){
            List<String> listMap = new ArrayList<>();
            /*
            List<Map<String,Object>> verificationList = verificationManageDAO.FindUnfinishedPeriodVerificationInfo();
            for(Map<String,Object> map : verificationList){
                String reminder_date = ToolUtil.getSpecifiedDayBefore(map.get("planning_time").toString(),verification_reminder_date);
                if(ToolUtil.compare_date(reminder_date,now_time)){
                    //当前时间小于等于提醒期限
                    listMap.add(map);
                }
            }
             */
            for(Map<String,Object> map : ledgerList){
                //没有正在执行的期间核查计划
                if(MessageType.EMPTYSTR.equals(map.get("next_verification_date"))){
                    continue;
                }
                String reminder_date = ToolUtil.getSpecifiedDayBefore(map.get("next_verification_date").toString(),maintenance_reminder_date);
                if(ToolUtil.compare_date(now_time,reminder_date)){
                    //当前时间小于等于提醒期限
                    listMap.add(map.get("device_id").toString());
                }

            }
            backMap.put("verification",listMap);
        }

        result.setObject(backMap);
        return result;
    }

}
