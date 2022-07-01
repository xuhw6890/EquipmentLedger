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
public interface MaintainApprovalDAO {

    /**
     * 依据设备关联id查询当前设备是否存在正在审批的维修申请
     * @param id2
     * @return
     */
    @Select("SELECT * FROM tbl_equipment_maintain_approval WHERE id2 = #{id2} AND application_steps < 4;")
    public Map<String,Object> FindMaintainApprovalInfoById2(String id2);

    /**
     * 用户申请设备维修
     * @param jsonObject
     */
    @Insert("INSERT INTO tbl_equipment_maintain_approval(id2,device_id,device_name,trademark,device_model," +
            "instrument_serial_number,classification,use_department,dept_code,storage_location,fault_description,fault_reason," +
            "create_user_name,create_user_code,create_time,department_head,back_reason)" +
            " VALUES(#{id},#{device_id},#{device_name},#{trademark},#{device_model},#{instrument_serial_number},#{classification}," +
            "#{use_department},#{dept_code},#{storage_location},#{fault_description},#{fault_reason},#{create_user_name}," +
            "#{create_user_code},#{create_time},#{department_head},#{back_reason});")
    public void AddMaintainApprovalInfo(JSONObject jsonObject);

    /**
     * 根据id获取设备维修申请信息
     * @param id
     * @return
     */
    @Select("SELECT * FROM tbl_equipment_maintain_approval WHERE id = #{id};")
    public Map<String,Object> FindMaintainApprovalInfoById(String id);

    /**
     * 设备管理员查询用户提交的维修申请
     * @return
     */
    @Select("<script>"
            + "SELECT * FROM tbl_equipment_maintain_approval WHERE application_steps = 1 "
            + "<choose>"
            + "<when test='dept_status != null and dept_status != &quot;&quot;'>"
            + "AND dept_code = '02' "
            + "</when>"
            + "<otherwise>"
            + "AND use_department = #{use_department} "
            + "</otherwise>"
            + "</choose>"
            + " ORDER BY create_time ASC;"
            + "</script>")
    public List<Map<String,Object>> FindSubimtRepairRequestData(Map<String,Object> map);

    /**
     * 设备管理员填写设备维修情况
     * @param jsonObject
     */
    @Update("UPDATE tbl_equipment_maintain_approval SET maintain_method = #{maintain_method}," +
            "maintain_project = #{maintain_project},repair_fees = #{repair_fees}," +
            "estimated_finish_time = #{estimated_finish_time},general_management_leader = #{general_management_leader}," +
            "general_management_leader_code = #{general_management_leader_code},general_management_leader_time = #{general_management_leader_time}," +
            "application_steps = #{application_steps} WHERE id = #{id};")
    public void UpdateMaintainApprovalInfo(JSONObject jsonObject);

    /**
     * 查询设备维修申请数据
     * @return
     */
    @Select("<script>"
            + "SELECT * FROM tbl_equipment_maintain_approval WHERE use_status = 0 AND back_status = 0 AND application_steps IN "
            + "<foreach collection='list' index='index' item='application_steps' open='(' separator=',' close=')'> "
            + " #{application_steps} "
            + "</foreach>"
            + " ORDER BY id ASC;"
            + "</script>")
    public List<Map<String,Object>> FindRepairRequestData(List<Integer> application_steps);

    /**
     * 查询设备维修情况数据
     * @param dataTable
     * @return
     */
    @Select("<script>"
            + "SELECT * FROM tbl_equipment_maintain_approval WHERE use_status = 0 "
            + "<if test='searchValue != null and searchValue != &quot;&quot;'>"
            + "AND ( device_id LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "device_name LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "trademark LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "device_model LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "classification LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "instrument_serial_number LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) )"
            + "</if>"
            + "<if test='sortTable != null and sortTable != &quot;&quot;'>"
            + " ORDER BY ${sortTable} ${sortRule} "
            + "</if>"
            + "LIMIT #{start},#{end};"
            + "</script>")
    public List<Map<String,Object>> FindEquipmentMaintenanceData(DataTable dataTable);

    @Select("<script>"
            + "SELECT COUNT(id) AS total FROM tbl_equipment_maintain_approval WHERE use_status = 0 "
            + "<if test='searchValue != null and searchValue != &quot;&quot;'>"
            + "AND ( device_id LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "device_name LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "trademark LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "device_model LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "classification LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "instrument_serial_number LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) )"
            + "</if>"
            + ";"
            + "</script>")
    public int FindEquipmentMaintenanceDataTotal(DataTable dataTable);

    /**
     * 依据审批步骤查询对应需要审批的维修申请信息
     * @param application_steps
     * @return
     */
    @Select("SELECT * FROM tbl_equipment_maintain_approval WHERE application_steps IN"
            + "<foreach collection='approval_steps' index='index' item='approval_steps' open='(' separator=',' close=')'>"
            + " #{approval_steps} "
            + "</foreach>"
            + ";")
    public List<Map<String,Object>> FindMaintainApprovalInfoBySteps(List<Integer> application_steps);

    /**
     * 修改设备维修报价单存放地址
     * @param map
     */
    @Update("UPDATE tbl_equipment_maintain_approval SET " +
            "repair_quotation_address = #{repair_quotation_address}," +
            "back_status = #{back_status} WHERE id = #{id};")
    public void UpdateRepairQuotationAddress(Map<String,Object> map);

    /**
     * 保存设备维修审批信息
     * @param map
     */
    @Update("UPDATE tbl_equipment_maintain_approval SET " +
            "deputy_general_manager = #{deputy_general_manager}," +
            "deputy_general_manager_code = #{deputy_general_manager_code}," +
            "deputy_general_manager_time = #{deputy_general_manager_time}," +
            "general_manager = #{general_manager}," +
            "general_manager_code = #{general_manager_code}," +
            "general_manager_time = #{general_manager_time}," +
            "application_steps = #{application_steps} WHERE id = #{id};")
    public void SaveApprovalInfo(Map<String,Object> map);

    /**
     * 还原维修申请审批状态码
     * @param map
     */
    @Update("UPDATE tbl_equipment_maintain_approval SET application_steps = #{application_steps}," +
            "general_management_leader = ''," +
            "general_management_leader_code = ''," +
            "general_management_leader_time = ''," +
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
    public void RestoreRepairRequestInfo(Map<String,Object> map);

    /**
     * 修改设备维修申请信息
     * @param jsonObject
     */
    @Update("UPDATE tbl_equipment_maintain_approval SET maintain_method = #{maintain_method}," +
            "maintain_project = #{maintain_project}," +
            "repair_fees = #{repair_fees}," +
            "estimated_finish_time = #{estimated_finish_time}," +
            "back_status = #{back_status}," +
            "application_steps = #{application_steps}," +
            "general_management_leader = #{general_management_leader}," +
            "general_management_leader_code = #{general_management_leader_code}," +
            "general_management_leader_time = #{general_management_leader_time} " +
            "WHERE id = #{id};")
    public void UpdateMaintenanceRequestInfo(JSONObject jsonObject);


}
