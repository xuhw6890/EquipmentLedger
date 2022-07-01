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
public interface VerificationManageDAO {

    /**
     * 保存期间核查计划信息
     * @param jsonObject
     */
    @Insert("INSERT INTO tbl_equipment_verification(id2,device_id,device_name,device_model,instrument_serial_number,method," +
            "frequency,planning_time,remark,create_time)" +
            " VALUES(#{id},#{device_id},#{device_name},#{device_model},#{instrument_serial_number},#{method}," +
            "#{frequency},#{planning_time},#{remark},#{create_time});")
    public void SaveDeviceVerificationInfo(JSONObject jsonObject);

    /**
     * 查询设备校准计划数据
     * @param
     * @return
     */
    @Select("<script>"
            + "SELECT * FROM tbl_equipment_verification WHERE finished_status = #{finished_status} AND use_status = 0 "
            + "<if test='dataTable.searchValue != null and dataTable.searchValue != &quot;&quot;'>"
            + "AND ( device_id LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "device_name LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "device_model LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "instrument_serial_number LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "method LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "executor LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) )"
            + "</if>"
            + "<if test='dataTable.sortTable != null and dataTable.sortTable != &quot;&quot;'>"
            + " ORDER BY ${dataTable.sortTable} ${dataTable.sortRule} "
            + "</if>"
            + "LIMIT #{dataTable.start},#{dataTable.end};"
            + "</script>")
    public List<Map<String,Object>> FindAllDeviceVerificationData(Map<String,Object> map);

    @Select("<script>"
            + "SELECT COUNT(id) AS total FROM tbl_equipment_verification WHERE finished_status = #{finished_status} AND use_status = 0 "
            + "<if test='dataTable.searchValue != null and dataTable.searchValue != &quot;&quot;'>"
            + "AND ( device_id LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "device_name LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "device_model LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "instrument_serial_number LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "method LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "executor LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) )"
            + "</if>"
            + ";"
            + "</script>")
    public int FindAllDeviceVerificationDataTotal(Map<String,Object> map);

    /**
     * 根据台账关联id查询正在进行的核查计划
     * @param id2
     * @return
     */
    @Select("SELECT * FROM tbl_equipment_verification WHERE id2 = #{id2} AND use_status = 0 AND finished_status = 0;")
    public Map<String,Object> FindOngoingPlanInfoById2(String id2);

    /**
     * 依据id查询期间核查计划
     * @param id
     * @return
     */
    @Select("SELECT * FROM tbl_equipment_verification WHERE id = #{id};")
    public Map<String,Object> FindDeviceVerificationInfoById(String id);

    /**
     * 保存确认期间核查计划信息
     * @param map
     */
    @Update("UPDATE tbl_equipment_verification SET executor = #{executor},remark = #{remark}," +
            "finished_status = #{finished_status} WHERE id = #{id};")
    public void SaveConfirmVerificationPlanInfo(Map<String,Object> map);

    /**
     * 获取正在进行的期间核查信息
     * @return
     */
    @Select("SELECT * FROM tbl_equipment_verification WHERE finished_status = 0 AND use_status = 0;")
    public List<Map<String,Object>> FindUnfinishedPeriodVerificationInfo();

    /**
     * 查询需要下载的设备校准计划数据
     * @param
     * @return
     */
    @Select("<script>"
            + "SELECT * FROM tbl_equipment_verification WHERE finished_status = #{finished_status} AND use_status = 0 "
            + "<if test='dataTable.searchValue != null and dataTable.searchValue != &quot;&quot;'>"
            + "AND ( device_id LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "device_name LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "device_model LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "instrument_serial_number LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "method LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "executor LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) )"
            + "</if>"
            + "<if test='dataTable.sortTable != null and dataTable.sortTable != &quot;&quot;'>"
            + " ORDER BY ${dataTable.sortTable} ${dataTable.sortRule} "
            + "</if>"
            + ";"
            + "</script>")
    public List<Map<String,Object>> FindDonwLoadVerificationData(Map<String,Object> map);

}
