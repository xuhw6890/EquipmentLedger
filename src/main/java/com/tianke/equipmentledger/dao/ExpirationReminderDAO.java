package com.tianke.equipmentledger.dao;

import net.sf.json.JSONObject;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

@Mapper
public interface ExpirationReminderDAO {
    /**
     * 查询到期期限信息
     * @return
     */
    @Select("SELECT * FROM tbl_alarm_reminder;")
    public Map<String,Object> FindAlarmReminderData();

    /**
     * 修改报警提醒期限
     * @param jsonObject
     */
    @Update("UPDATE tbl_alarm_reminder SET calibration_reminder_date = #{calibration_reminder_date}," +
            "maintenance_reminder_date = #{maintenance_reminder_date}," +
            "verification_reminder_date = #{verification_reminder_date}," +
            "user_name = #{user_name}," +
            "update_time = #{update_time} " +
            "WHERE id = #{id};")
    public void UpdateAlarmReminderData(JSONObject jsonObject);
}
