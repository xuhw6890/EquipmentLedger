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
public interface OutOfficeManageDAO {

    /**
     * 依据设备台账关联id查询为审批完成的申请
     * @param id2
     * @return
     */
    @Select("SELECT * FROM tbl_equipment_go_out_approval WHERE id2 = #{id2} AND device_status != 2 AND use_status = 0;")
    public Map<String,Object> FindGooutApprovalInfoById2(String id2);

    /**
     * 添加外出申请信息
     * @param jsonObject
     */
    @Insert("INSERT INTO tbl_equipment_go_out_approval(id2,device_id,device_name,trademark,device_model,instrument_serial_number," +
            "go_out_reason,time_limit,use_department,delivery_company,company_contacts,company_phone,use_department_leader," +
            "use_department_leader_code,use_department_leader_time,approval_steps,create_user_name,create_user_code,create_time,back_reason)" +
            " VALUES(#{id},#{device_id},#{device_name},#{trademark},#{device_model},#{instrument_serial_number}," +
            "#{go_out_reason},#{time_limit},#{use_department},#{delivery_company},#{company_contacts},#{company_phone}," +
            "#{use_department_leader},#{use_department_leader_code},#{use_department_leader_time},#{approval_steps}," +
            "#{create_user_name},#{create_user_code},#{create_time},#{back_reason});")
    public void AddGooutApprovalInfo(JSONObject jsonObject);

    /**
     * 依据审核人员不同展示处于不同申请状态的申请信息
     * @param
     * @return
     */
    @Select("<script>"
            + "SELECT * FROM tbl_equipment_go_out_approval WHERE approval_steps IN "
            + "<foreach collection='list' index='index' item='approval_steps' open='(' separator=',' close=')'>"
            + " #{approval_steps} "
            + "</foreach>"
            + "AND device_status = 0 AND use_status = 0 AND back_status = 0  ORDER BY create_time DESC ;"
            + "</script>")
    public List<Map<String,Object>> FindGooutApprovalInfoByApprovalSteps(List<Integer> approval_steps);

    /**
     * 根据id获取设备外出申请表信息
     * @param id
     * @return
     */
    @Select("SELECT * FROM tbl_equipment_go_out_approval WHERE id = #{id};")
    public Map<String,Object> FindGooutApprovalInfoById(String id);

    /**
     * 保存审批信息
     * @param map
     */
    @Update("UPDATE tbl_equipment_go_out_approval SET use_department_leader = #{use_department_leader}," +
            "use_department_leader_code = #{use_department_leader_code}," +
            "use_department_leader_time = #{use_department_leader_time}," +
            "general_management_leader = #{general_management_leader}," +
            "general_management_leader_code = #{general_management_leader_code}," +
            "general_management_leader_time = #{general_management_leader_time}," +
            "technical_director = #{technical_director}," +
            "technical_director_code = #{technical_director_code}," +
            "technical_director_time = #{technical_director_time}," +
            "laboratory_director = #{laboratory_director}," +
            "laboratory_director_code = #{laboratory_director_code}," +
            "laboratory_director_time = #{laboratory_director_time}," +
            "approval_steps = #{approval_steps}," +
            "device_status = #{device_status} WHERE id = #{id};")
    public void SaveGoOutApprovalInfo(Map<String,Object> map);

    /**
     * 查询已审核通过的设备外出申请数据
     * @param dataTable
     * @return
     */
    @Select("<script>"
            + "SELECT * FROM tbl_equipment_go_out_approval WHERE use_status = 0 "
            + "<if test='searchValue != null and searchValue != &quot;&quot;'>"
            + "AND ( device_id LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "device_name LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "trademark LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "device_model LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "classification LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "instrument_serial_number LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "delivery_company LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) )"
            + "</if>"
            + "<if test='sortTable != null and sortTable != &quot;&quot;'>"
            + " ORDER BY ${sortTable} ${sortRule} "
            + "</if>"
            + "LIMIT #{start},#{end};"
            + "</script>")
    public List<Map<String,Object>> FindAllApprovalGoOutApplication(DataTable dataTable);

    @Select("<script>"
            + "SELECT COUNT(id) AS total FROM tbl_equipment_ledger_info WHERE use_status = 0 "
            + "<if test='searchValue != null and searchValue != &quot;&quot;'>"
            + "AND ( device_id LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "device_name LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "trademark LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "device_model LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "classification LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "instrument_serial_number LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "delivery_company LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) )"
            + "</if>"
            + ";"
            + "</script>")
    public int FindAllApprovalGoOutApplicationTotal(DataTable dataTable);

    /**
     * 保存设备返回核查信息
     * @param jsonObject
     */
    @Update("UPDATE tbl_equipment_go_out_approval SET time_return = #{time_return}," +
            "device_integrity_check = #{device_integrity_check}," +
            "device_recipient = #{device_recipient}," +
            "receive_time = #{receive_time}," +
            "device_status = #{device_status} " +
            "WHERE id = #{id};")
    public void SaveBackCheckInfo(JSONObject jsonObject);

    /**
     * 还原设备外出申请审批状态码
     * @param map
     */
    @Update("UPDATE tbl_equipment_go_out_approval SET approval_steps = #{approval_steps}," +
            "general_management_leader = ''," +
            "general_management_leader_code = ''," +
            "general_management_leader_time = ''," +
            "technical_director = ''," +
            "technical_director_code = ''," +
            "technical_director_time = ''," +
            "laboratory_director = ''," +
            "laboratory_director_code = ''," +
            "laboratory_director_time = ''," +
            "back_status = #{back_status}," +
            "back_reason = #{back_reason}," +
            "back_user_name = #{back_user_name}," +
            "back_user_code = #{back_user_code} " +
            "WHERE id = #{id};")
    public void RestoringOutgoApplicationInfo(Map<String,Object> map);

    /**
     * 修改设备外出申请信息
     * @param jsonObject
     */
    @Update("UPDATE tbl_equipment_go_out_approval SET go_out_reason = #{go_out_reason}," +
            "time_limit = #{time_limit}," +
            "delivery_company = #{delivery_company}," +
            "company_contacts = #{company_contacts}," +
            "company_phone = #{company_phone}," +
            "back_status = #{back_status} " +
            "WHERE id = #{id};")
    public void UpdateOutboundApplicationInfo(JSONObject jsonObject);
}
