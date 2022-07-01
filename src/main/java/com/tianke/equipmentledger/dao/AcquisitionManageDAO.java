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
public interface AcquisitionManageDAO {

    /**
     * 添加采购申请信息
     * @param jsonObject
     */
    @Insert("INSERT INTO tbl_equipment_request_form(device_name,trademark,device_model,supplier,order_quantity," +
            "budget_amount,delivery_date,apply_department,purchase_reason,main_purpose,technical_indicators," +
            "apply_department_leader,apply_department_leader_code,apply_department_leader_time,create_time,dept_code,back_reason)" +
            " VALUES(#{device_name},#{trademark},#{device_model},#{supplier},#{order_quantity},#{budget_amount}," +
            "#{delivery_date},#{apply_department},#{purchase_reason},#{main_purpose},#{technical_indicators}," +
            "#{apply_department_leader},#{apply_department_leader_code},#{apply_department_leader_time}," +
            "#{create_time},#{dept_code},#{back_reason});")
    public void AddPurchaseRequisitionInfo(JSONObject jsonObject);

    /**
     * 根据id查询采购申请信息
     * @param id
     * @return
     */
    @Select("SELECT * FROM tbl_equipment_request_form WHERE id = #{id} AND use_status = 0;")
    public Map<String,Object> FindPurchaseRequisitionInfoById(String id);

    /**
     * 修改采购申请的审核状态
     * @param map
     */
    @Update("UPDATE tbl_equipment_request_form SET " +
            "deputy_general_manager = #{deputy_general_manager}," +
            "deputy_general_manager_code = #{deputy_general_manager_code}," +
            "deputy_general_manager_time = #{deputy_general_manager_time}," +
            "general_manager = #{general_manager}," +
            "general_manager_code = #{general_manager_code}," +
            "general_manager_time = #{general_manager_time}," +
            "chairman = #{chairman}," +
            "chairman_code = #{chairman_code}," +
            "chairman_time = #{chairman_time}," +
            "application_status = #{application_status}," +
            "update_time = #{update_time} " +
            "WHERE id = #{id};")
    public void UpdatePurchaseApprovalInfo(Map<String,Object> map);

    /**
     * 查询该审批人员当前可审批的所有采购信息
     * @param
     * @return
     */
    @Select("<script>"
            + "SELECT * FROM tbl_equipment_request_form WHERE application_status IN "
            + "<foreach collection='list' index='index' item='application_status' open='(' separator=',' close=')'> "
            + " #{application_status} "
            + "</foreach>"
            + " AND use_status = 0 AND back_status = 0;"
            + "</script>")
    public List<Map<String,Object>> FindPurchaseRequisitionInfoByApplicationStatus(List<Integer> application_status);

    /**
     * 查询当年审批完成的采购申请数量
     * @return
     */
    @Select("SELECT COUNT(id) AS total FROM tbl_equipment_request_form WHERE create_time LIKE CONCAT(#{create_time},'%') application_status = 5 AND use_status = 0;")
    public int FindApprovalsCompletedNumber(String create_time);

    /**
     * 查询已完成审批的采购申请信息
     * @param map
     * @return
     */
    @Select("<script>"
            + "SELECT * FROM tbl_equipment_request_form WHERE use_status = 0 "
            + "<choose>"
            + "<when test='dept_status != null and dept_status != &quot;&quot;'>"
            + "AND dept_code = '02' "
            + "</when>"
            + "<otherwise>"
            + "AND apply_department = #{apply_department} "
            + "</otherwise>"
            + "</choose>"
            + "<if test='dataTable.searchValue != null and dataTable.searchValue != &quot;&quot;'>"
            + "AND ( device_name LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "trademark LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "supplier LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "apply_department LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) )"
            + "</if>"
            + "<if test='dataTable.sortTable != null and dataTable.sortTable != &quot;&quot;'>"
            + " ORDER BY ${dataTable.sortTable} ${dataTable.sortRule} "
            + "</if>"
            + "LIMIT #{dataTable.start},#{dataTable.end};"
            + "</script>")
    public List<Map<String,Object>> FindAllApprovedPurchaseRequisitions(Map<String,Object> map);

    @Select("<script>"
            + "SELECT COUNT(id) AS total FROM tbl_equipment_request_form WHERE use_status = 0 "
            + "<choose>"
            + "<when test='dept_status != null and dept_status != &quot;&quot;'>"
            + "AND dept_code = '02' "
            + "</when>"
            + "<otherwise>"
            + "AND apply_department = #{apply_department} "
            + "</otherwise>"
            + "</choose>"
            + "<if test='dataTable.searchValue != null and dataTable.searchValue != &quot;&quot;'>"
            + "AND ( device_name LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "trademark LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "supplier LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "apply_department LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) )"
            + "</if>"
            + ";"
            + "</script>")
    public int FindAllApprovedPurchaseRequisitionsTotal(Map<String,Object> map);

    /**
     * 修改采购合同状态
     * @param map
     */
    @Update("UPDATE tbl_equipment_request_form SET purchase_status = #{purchase_status},update_time = #{update_time} WHERE id = #{id};")
    public void UpdatePurchaseStatus(Map<String,Object> map);

    /**
     * 用户上传采购申请设备技术指标概述文件，保存该文件存放路径
     * @param map
     */
    @Update("UPDATE tbl_equipment_request_form SET " +
            "technical_indicators_address = #{technical_indicators_address}," +
            "back_status = #{back_status},update_time = #{update_time} " +
            "WHERE id = #{id};")
    public void SaveTechnicalIndicatorsAddress(Map<String,Object> map);

    /**
     * 修改采购信息补充状态
     * @param map
     */
    @Update("UPDATE tbl_equipment_request_form SET info_supplement_status = #{info_supplement_status} WHERE id = #{id};")
    public void SaveInformationSupplementStatus(Map<String,Object> map);

    /**
     * 采购申请不通过，还原采购申请审批状态信息
     * @param map
     */
    @Update("UPDATE tbl_equipment_request_form SET application_status = #{application_status}," +
            "deputy_general_manager = ''," +
            "deputy_general_manager_code = ''," +
            "deputy_general_manager_time = ''," +
            "general_manager = ''," +
            "general_manager_code = ''," +
            "general_manager_time = ''," +
            "chairman = ''," +
            "chairman_code = ''," +
            "chairman_time = ''," +
            "back_status = #{back_status}," +
            "back_reason = #{back_reason}," +
            "back_user_name = #{back_user_name}," +
            "back_user_code = #{back_user_code} " +
            "WHERE id = #{id};")
    public void RestoringApprovalInfo(Map<String,Object> map);

    /**
     * 修改采购申请信息
     * @param jsonObject
     */
    @Update("UPDATE tbl_equipment_request_form SET device_name = #{device_name}," +
            "trademark = #{trademark}," +
            "device_model = #{device_model}," +
            "supplier = #{supplier}," +
            "order_quantity = #{order_quantity}," +
            "budget_amount = #{budget_amount}," +
            "delivery_date = #{delivery_date}," +
            "apply_department = #{apply_department}," +
            "dept_code = #{dept_code}," +
            "purchase_reason = #{purchase_reason}," +
            "main_purpose = #{main_purpose}," +
            "technical_indicators = #{technical_indicators}," +
            "back_status = #{back_status}," +
            "update_time = #{update_time} " +
            "WHERE id = #{id};")
    public void UpdatePurchaseRequisitionInfo(JSONObject jsonObject);


}
