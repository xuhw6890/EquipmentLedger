package com.tianke.equipmentledger.dao;

import com.tianke.equipmentledger.entity.DataTable;
import net.sf.json.JSONObject;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ScrapApprovalDAO {

    /**
     * 根据设备关联id查询报废申请信息
     * @param id2
     * @return
     */
    @Select("SELECT * FROM tbl_equipment_scrap_approval WHERE id2 = #{id2} AND approval_status = 0 AND enabled_status = 0;")
    public Map<String,Object> FindScrapApprovalInfoById2(String id2);

    /**
     * 添加设备报废/停用申请信息
     * @param jsonObject
     */
    @Insert("INSERT INTO tbl_equipment_scrap_approval(id2,device_id,device_name,trademark,device_model,instrument_serial_number," +
            "classification,fixed_asset_number,original_value,use_department,dept_code,purchase_date,main_purpose," +
            "reason_deactivation,use_department_leader,use_department_leader_code,use_department_leader_time,device_status," +
            "create_user_name,create_user_code,create_time,back_reason)" +
            " VALUES(#{id},#{device_id},#{device_name},#{trademark},#{device_model},#{instrument_serial_number}," +
            "#{classification},#{fixed_asset_number},#{original_value},#{use_department},#{dept_code},#{purchase_date}," +
            "#{main_purpose},#{reason_deactivation},#{use_department_leader},#{use_department_leader_code}," +
            "#{use_department_leader_time},#{device_status},#{create_user_name},#{create_user_code},#{create_time},#{back_reason});")
    public void AddScrapApprovalInfo(JSONObject jsonObject);

    /**
     * 根据用户职位展示该用户需要审批的设备信息
     * @param approval_steps 审批权限
     * @return
     */
    @Select("<script>"
            + "SELECT * FROM tbl_equipment_scrap_approval WHERE approval_steps IN "
            + "<foreach collection='list' index='index' item='approval_steps' open='(' separator=',' close=')'>"
            + " #{approval_steps} "
            + "</foreach>"
            + "AND approval_status = 0 AND back_status = 0 ;"
            + "</script>")
    public List<Map<String,Object>> FindScrapApprovalInfoByUserRights(List<Integer> approval_steps);

    /**
     * 根据id查询报废申请信息
     * @param id
     * @return
     */
    @Select("SELECT * FROM tbl_equipment_scrap_approval WHERE id = #{id} AND approval_status = 0;")
    public Map<String,Object> FindScrapApprovalInfoById(String id);

    /**
     * 用户审批
     * @param map
     */
    @Update("UPDATE tbl_equipment_scrap_approval SET " +
            "deputy_general_manager = #{deputy_general_manager}," +
            "deputy_general_manager_code = #{deputy_general_manager_code}," +
            "deputy_general_manager_time = #{deputy_general_manager_time}," +
            "general_manager = #{general_manager}," +
            "general_manager_code = #{general_manager_code}," +
            "general_manager_time = #{general_manager_time}," +
            "financial_affairs = #{financial_affairs}," +
            "financial_affairs_code = #{financial_affairs_code}," +
            "financial_affairs_time =  #{financial_affairs_time}," +
            "approval_steps = #{approval_steps} WHERE id = #{id};")
    public void ReviewScrapApprovalInfo(Map<String,Object> map);

    /**
     * 查询设备变更申请数据
     * @param map
     * @return
     */
    @Select("<script>"
            + "SELECT * FROM tbl_equipment_scrap_approval WHERE approval_status = 0 "
            + "<choose>"
            + "<when test='dept_status != null and dept_status != &quot;&quot;'>"
            + "AND dept_code = '02' "
            + "</when>"
            + "<otherwise>"
            + "AND use_department = #{use_department} "
            + "</otherwise>"
            + "</choose>"
            + "<if test='dataTable.searchValue != null and dataTable.searchValue != &quot;&quot;'>"
            + "AND ( device_id LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "device_name LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "trademark LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "device_model LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "classification LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "instrument_serial_number LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "use_department LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) )"
            + "</if>"
            + "<if test='dataTable.sortTable != null and dataTable.sortTable != &quot;&quot;'>"
            + " ORDER BY ${dataTable.sortTable} ${dataTable.sortRule} "
            + "</if>"
            + "LIMIT #{dataTable.start},#{dataTable.end};"
            + "</script>")
    public List<Map<String,Object>> FindAllChangeApplicationData(Map<String,Object> map);

    @Select("<script>"
            + "SELECT COUNT(id) AS total FROM tbl_equipment_scrap_approval WHERE approval_status = 0 "
            + "<choose>"
            + "<when test='dept_status != null and dept_status != &quot;&quot;'>"
            + "AND dept_code = '02' "
            + "</when>"
            + "<otherwise>"
            + "AND use_department = #{use_department} "
            + "</otherwise>"
            + "</choose>"
            + "<if test='dataTable.searchValue != null and dataTable.searchValue != &quot;&quot;'>"
            + "AND ( device_id LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "device_name LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "trademark LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "device_model LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "classification LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "instrument_serial_number LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "use_department LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) )"
            + "</if>"
            + ";"
            + "</script>")
    public int FindAllChangeApplicationDataTotal(Map<String,Object> map);

    /**
     * 还原设备停用/报废申请审批状态码
     * @param map
     */
    @Update("UPDATE tbl_equipment_scrap_approval SET approval_steps = #{approval_steps}," +
            "deputy_general_manager = ''," +
            "deputy_general_manager_code = ''," +
            "deputy_general_manager_time = ''," +
            "general_manager = ''," +
            "general_manager_code = ''," +
            "general_manager_time = ''," +
            "financial_affairs = ''," +
            "financial_affairs_code = ''," +
            "financial_affairs_time = ''," +
            "back_status = #{back_status}," +
            "back_reason = #{back_reason}," +
            "back_user_name = #{back_user_name}," +
            "back_user_code = #{back_user_code} " +
            "WHERE id = #{id};")
    public void RestoreDeactivationRequestInfo(Map<String,Object> map);

    /**
     * 修改设备停用/报废申请信息
     * @param jsonObject
     */
    @Update("UPDATE tbl_equipment_scrap_approval SET reason_deactivation = #{reason_deactivation}," +
            "back_status = #{back_status},device_status = #{device_status} WHERE id = #{id};")
    public void UpdateScrapApplicationInfo(JSONObject jsonObject);

    /**
     * 修改停用申请信息中设备启用状态
     * @param id2
     */
    @Update("UPDATE tbl_equipment_scrap_approval SET enabled_status = #{enabled_status} WHERE id2 = #{id2};")
    public void UpdateEquipmentEnabledStatus(@Param("id2") String id2,@Param("enabled_status") String enabled_status);
}
