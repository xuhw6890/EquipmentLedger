package com.tianke.equipmentledger.dao;

import com.tianke.equipmentledger.entity.DataTable;
import net.sf.json.JSONObject;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface CalibrationManageDAO {

    /**
     * 根据设备关联ID查询是否有正在执行的校准计划
     * @param id2 设备关联id
     * @return
     */
    @Select("SELECT * FROM tbl_equipment_calibration WHERE id2 = #{id2} AND use_status = 0 AND planning_process < 2;")
    public Map<String,Object> FindOngoingCalibrationProgramById2(String id2);

    /**
     * 保存设备校准计划信息
     * @param jsonObject
     */
    @Insert("INSERT INTO tbl_equipment_calibration(id2,device_id,device_name,trademark,device_model,instrument_serial_number," +
            "use_department,storage_location,parameters,method,company_name,calibration_time,planned_return_time,remarks," +
            "create_user_name,create_user_code,create_time)" +
            " VALUES(#{id},#{device_id},#{device_name},#{trademark},#{device_model},#{instrument_serial_number}," +
            "#{use_department},#{storage_location},#{parameters},#{method},#{company_name},#{calibration_time}," +
            "#{planned_return_time},#{remarks},#{create_user_name},#{create_user_code},#{create_time});")
    public void SaveCalibrationProgramInfo(JSONObject jsonObject);

    /**
     * 查询设备校准计划数据
     * @param dataTable
     * @return
     */
    @Select("<script>"
            + "SELECT * FROM tbl_equipment_calibration WHERE use_status = 0 "
            + "<if test='searchValue != null and searchValue != &quot;&quot;'>"
            + "AND ( device_id LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "device_name LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "trademark LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "device_model LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "classification LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "instrument_serial_number LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "company_name LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) )"
            + "</if>"
            + "<if test='sortTable != null and sortTable != &quot;&quot;'>"
            + " ORDER BY ${sortTable} ${sortRule} "
            + "</if>"
            + "LIMIT #{start},#{end};"
            + "</script>")
    public List<Map<String,Object>> FindAllCalibrationPlanData(DataTable dataTable);

    @Select("<script>"
            + "SELECT COUNT(id) AS total FROM tbl_equipment_calibration WHERE use_status = 0 "
            + "<if test='searchValue != null and searchValue != &quot;&quot;'>"
            + "AND ( device_id LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "device_name LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "trademark LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "device_model LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "classification LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "instrument_serial_number LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "company_name LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) )"
            + "</if>"
            + ";"
            + "</script>")
    public int FindAllCalibrationPlanDataTotal(DataTable dataTable);

    /**
     * 依据校准计划id查询该校准计划信息
     * @param id
     * @return
     */
    @Select("SELECT * FROM tbl_equipment_calibration WHERE id = #{id} AND use_status = 0;")
    public Map<String,Object> FindOngoingCalibrationProgramById(String id);

    /**
     * 保存校准计划确认结果信息
     * @param jsonObject
     */
    @Update("UPDATE tbl_equipment_calibration SET complete_time = #{complete_time}," +
            "next_date = #{next_date},report_number = #{report_number}," +
            "correction_factor = #{correction_factor}," +
            "register_person = #{register_person}," +
            "register_person_code = #{register_person_code}," +
            "check_in_time = #{check_in_time}," +
            "planning_process = #{planning_process} " +
            "WHERE id = #{id};")
    public void SaveCalibrationConfirmationResult(JSONObject jsonObject);

    /**
     * 复核校准结果
     * @param map
     */
    @Update("UPDATE tbl_equipment_calibration SET result_confirmation = 1 ," +
            "confirmor = #{confirmor}," +
            "confirmor_code = #{confirmor_code}," +
            "confirm_time = #{confirm_time}," +
            "planning_process = #{planning_process} " +
            "WHERE id = #{id};")
    public void ReviewCalibrationResult(Map<String,Object> map);

    /**
     * 根据设备关联id获取刚刚创建校准计划信息
     * @param id2
     * @return
     */
    @Select("SELECT * FROM tbl_equipment_calibration WHERE id2 = #{id2} AND planning_process = 0;")
    public Map<String,Object> FindCalibrationPlanDataById2(String id2);

    /**
     * 获取已完成的校准计划
     * @return
     */
    @Select("SELECT * FROM tbl_equipment_calibration WHERE planning_process = 2 AND use_status = 0 GROUP BY id2 ORDER BY create_time DESC;")
    public List<Map<String,Object>> FindCompletedCalibrationPlan();

    /**
     * 获取当前正在进行的校准计划
     * @return
     */
    @Select("SELECT * FROM tbl_equipment_calibration WHERE planning_process < 2 AND use_status = 0 ORDER BY create_time DESC;")
    public List<Map<String,Object>> FindUnfinishedCalibrationPlan();

    /**
     * 查询下载的设备校准计划数据
     * @param dataTable
     * @return
     */
    @Select("<script>"
            + "SELECT * FROM tbl_equipment_calibration WHERE use_status = 0 "
            + "<if test='searchValue != null and searchValue != &quot;&quot;'>"
            + "AND ( device_id LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "device_name LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "trademark LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "device_model LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "classification LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "instrument_serial_number LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "company_name LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) )"
            + "</if>"
            + "<if test='sortTable != null and sortTable != &quot;&quot;'>"
            + " ORDER BY ${sortTable} ${sortRule} "
            + "</if>"
            + ";"
            + "</script>")
    public List<Map<String,Object>> FindDonwLoadCalibrationPlanData(DataTable dataTable);

}
