package com.tianke.equipmentledger.dao;

import net.sf.json.JSONObject;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface VerifyApprovalDAO {

    /**
     * 根据采购信息关联id查询相关设备验证信息
     * @param id2
     * @return
     */
    @Select("SELECT * FROM tbl_equipment_acceptance WHERE id2 = #{id2} AND use_status = 0;")
    public List<Map<String,Object>> FindVerifyApprovalById2(String id2);

    /**
     * 保存设备验证申请信息
     * @param jsonObject
     */
    @Insert("INSERT INTO tbl_equipment_acceptance(id2,device_name,trademark,device_model,instrument_serial_number," +
            "software_version,arrival_date,use_department,dept_code,storage_location,verification_project1," +
            "verification_project2,verification_project3,verification_project4,verify_situation,verify_results,total_price," +
            "use_department_leader,use_department_leader_code,use_department_leader_time,applicant,applicant_code," +
            "application_status,create_time,back_reason,unit_price,maintenance_period)" +
            " VALUES(#{id},#{device_name},#{trademark},#{device_model},#{instrument_serial_number}," +
            "#{software_version},#{arrival_date},#{use_department},#{dept_code},#{storage_location}," +
            "#{verification_project1},#{verification_project2},#{verification_project3},#{verification_project4}," +
            "#{verify_situation},#{verify_results},#{total_price},#{use_department_leader},#{use_department_leader_code}," +
            "#{use_department_leader_time},#{applicant},#{applicant_code},#{application_status},#{create_time}," +
            "#{back_reason},#{unit_price},#{maintenance_period});")
    public void SaveVerifyApprovalInfo(JSONObject jsonObject);

    /**
     * 查询合同管理模块数据
     * @param map
     * @return
     */
    @Select("<script>"
            + "SELECT * FROM tbl_equipment_acceptance WHERE use_status = 0 "
            + "<choose>"
            + "<when test='dept_status != null and dept_status != &quot;&quot;'>"
            + "AND dept_code = '02' "
            + "</when>"
            + "<otherwise>"
            + "AND use_department = #{use_department} "
            + "</otherwise>"
            + "</choose>"
            + "<if test='dataTable.searchValue != null and dataTable.searchValue != &quot;&quot;'>"
            + "AND ( device_name LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "trademark LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "device_model LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "instrument_serial_number LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) )"
            + "</if>"
            + "<if test='dataTable.sortTable != null and dataTable.sortTable != &quot;&quot;'>"
            + " ORDER BY ${dataTable.sortTable} ${dataTable.sortRule} "
            + "</if>"
            + "LIMIT #{dataTable.start},#{dataTable.end};"
            + "</script>")
    public List<Map<String,Object>> FindAllVerifyApprovalData(Map<String,Object> map);

    @Select("<script>"
            + "SELECT COUNT(id) AS total FROM tbl_equipment_acceptance WHERE use_status = 0 "
            + "<choose>"
            + "<when test='dept_status != null and dept_status != &quot;&quot;'>"
            + "AND dept_code = '02' "
            + "</when>"
            + "<otherwise>"
            + "AND use_department = #{use_department} "
            + "</otherwise>"
            + "</choose>"
            + "<if test='dataTable.searchValue != null and dataTable.searchValue != &quot;&quot;'>"
            + "AND ( device_name LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "trademark LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "device_model LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "instrument_serial_number LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) )"
            + "</if>"
            + ";"
            + "</script>")
    public int FindAllVerifyApprovalDataTotal(Map<String,Object> map);

    /**
     * 获取设备验收审批数据
     * @param map
     * @return
     */
    @Select("<script>"
            + "SELECT * FROM tbl_equipment_acceptance WHERE application_status != 5 AND use_status = 0 AND back_status = 0 AND (" +
            " application_status IN "
            + "<foreach collection='application_status' index='index' item='application_status' open='(' separator=',' close=')'> "
            + " #{application_status} "
            + "</foreach>"
            + "<if test='department_head != null and department_head != &quot;&quot;'>"
            + " OR (use_department = #{use_department} AND application_status = 0)"
            + "</if>"
            + "<if test='device_admin_status != null and device_admin_status != &quot;&quot;'>"
            + " OR (dept_code = #{dept_code} AND application_status = 1)"
            + "</if>"
            + " );"
            + "</script>")
    public List<Map<String,Object>> FindAcceptanceApplicationData(Map<String,Object> map);

    /**
     * 根据id获取设备验收申请信息
     * @param id
     * @return
     */
    @Select("SELECT * FROM tbl_equipment_acceptance WHERE id = #{id} AND use_status = 0;")
    public Map<String,Object> FindFindVerifyApprovalById(String id);

    /**
     * 审批设备验收申请信息
     * @param map
     */
    @Update("UPDATE tbl_equipment_acceptance SET " +
            "use_department_leader = #{use_department_leader}," +
            "use_department_leader_code = #{use_department_leader_code}," +
            "use_department_leader_time = #{use_department_leader_time}," +
            "device_admin = #{device_admin}," +
            "device_admin_code = #{device_admin_code}," +
            "device_admin_time = #{device_admin_time}," +
            "asset_admin = #{asset_admin}," +
            "asset_admin_code = #{asset_admin_code}," +
            "asset_admin_time = #{asset_admin_time}," +
            "deputy_general_manager = #{deputy_general_manager}," +
            "deputy_general_manager_code = #{deputy_general_manager_code}," +
            "deputy_general_manager_time = #{deputy_general_manager_time}," +
            "general_manager = #{general_manager}," +
            "general_manager_code = #{general_manager_code}," +
            "general_manager_time = #{general_manager_time}," +
            "application_status = #{application_status} " +
            "WHERE id = #{id};")
    public void ApprovalAcceptanceApplication(Map<String,Object> map);

    /**
     * 还原设备验收申请审批状态码
     * @param map
     */
    @Update("UPDATE tbl_equipment_acceptance SET application_status = #{application_status}," +
            "use_department_leader = ''," +
            "use_department_leader_code = ''," +
            "use_department_leader_time = ''," +
            "device_admin = ''," +
            "device_admin_code = ''," +
            "device_admin_time = ''," +
            "asset_admin = ''," +
            "asset_admin_code = ''," +
            "asset_admin_time = ''," +
            "deputy_general_manager = ''," +
            "deputy_general_manager_code = ''," +
            "deputy_general_manager_time = ''," +
            "general_manager = ''," +
            "general_manager_code = ''," +
            "general_manager_time = ''," +
            "back_status = #{back_status}," +
            "back_reason = #{back_reason}," +
            "back_user_name = #{back_user_name}," +
            "back_user_code = #{back_user_code} " +
            "WHERE id = #{id};")
    public void RestoreAcceptanceApplicationInfo(Map<String,Object> map);

    /**
     * 修改设备验收申请信息
     * @param jsonObject
     */
    @Update("UPDATE tbl_equipment_acceptance SET device_name = #{device_name}," +
            "trademark = #{trademark}," +
            "device_model = #{device_model}," +
            "instrument_serial_number = #{instrument_serial_number}," +
            "software_version = #{software_version}," +
            "arrival_date = #{arrival_date}," +
            "use_department = #{use_department}," +
            "dept_code = #{dept_code}," +
            "storage_location = #{storage_location}," +
            "verification_project1 = #{verification_project1}," +
            "verification_project2 = #{verification_project2}," +
            "verification_project3 = #{verification_project3}," +
            "verification_project4 = #{verification_project4}," +
            "verify_situation = #{verify_situation}," +
            "verify_results = #{verify_results}," +
            "total_price = #{total_price}," +
            "back_status = #{back_status} " +
            "WHERE id = #{id};")
    public void UpdateAcceptanceApplicationInfo(JSONObject jsonObject);

    /**
     * 修改台账状态码
     * @param id
     */
    @Update("UPDATE tbl_equipment_acceptance SET ledger_status = 1 WHERE id = #{id};")
    public void UpdateLedgerStatus(String id);

}
