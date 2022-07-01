package com.tianke.equipmentledger.dao;

import com.tianke.equipmentledger.entity.DataTable;
import net.sf.json.JSONObject;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface EquipmentLedgerDAO {
    /**
     * 查询台账数据
     * @param map
     * @return
     */
    @Select("<script>"
            + "SELECT * FROM tbl_equipment_ledger_info WHERE use_status = 0 AND device_status = #{device_status} "
            + "<if test='asset_status != null and asset_status != &quot;&quot;'>"
            + "<choose>"
            + "<when test='dept_status != null and dept_status != &quot;&quot;'>"
            + "AND dept_code = '02' "
            + "</when>"
            + "<otherwise>"
            + "AND use_department = #{use_department} "
            + "</otherwise>"
            + "</choose>"
            + "</if>"
            + "<if test='fixed_assets != null and fixed_assets != &quot;&quot;'>"
            + "AND ( fixed_asset_number != '' OR fixed_assets_status = 1 ) "
            + "</if>"
            + "<if test='dataTable.searchValue != null and dataTable.searchValue != &quot;&quot;'>"
            + "AND ( device_id LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "device_name LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "trademark LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "device_model LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "classification LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "use_department LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "storage_location LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "instrument_serial_number LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) )"
            + "</if>"
            + "<if test='dataTable.sortTable != null and dataTable.sortTable != &quot;&quot;'>"
            + " ORDER BY ${dataTable.sortTable} ${dataTable.sortRule} "
            + "</if>"
            + "LIMIT #{dataTable.start},#{dataTable.end};"
            + "</script>")
    public List<Map<String,Object>> FindAllEquipmentLedger(Map<String,Object> map);

    @Select("<script>"
            + "SELECT COUNT(id) AS total FROM tbl_equipment_ledger_info WHERE use_status = 0 AND device_status = #{device_status} "
            + "<if test='asset_status != null and asset_status != &quot;&quot;'>"
            + "<choose>"
            + "<when test='dept_status != null and dept_status != &quot;&quot;'>"
            + "AND dept_code = '02' "
            + "</when>"
            + "<otherwise>"
            + "AND use_department = #{use_department} "
            + "</otherwise>"
            + "</choose>"
            + "</if>"
            + "<if test='fixed_assets != null and fixed_assets != &quot;&quot;'>"
            + "AND ( fixed_asset_number != '' OR fixed_assets_status = 1 ) "
            + "</if>"
            + "<if test='dataTable.searchValue != null and dataTable.searchValue != &quot;&quot;'>"
            + "AND ( device_id LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "device_name LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "trademark LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "device_model LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "classification LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "use_department LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "storage_location LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "instrument_serial_number LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) )"
            + "</if>"
            + ";"
            + "</script>")
    public int FindAllEquipmentLedgerTotal(Map<String,Object> map);

    /**
     * 根据设备编号查询台账信息
     * @param device_id
     * @return
     */
    @Select("SELECT * FROM tbl_equipment_ledger_info WHERE device_id = #{device_id} AND use_status = 0 AND device_status = 0;")
    public Map<String,Object> FindEquipmentLedgerByDeviceId(String device_id);

    /**
     * 新增设备台账数据
     * @param jsonObject
     */
    @Insert("INSERT INTO tbl_equipment_ledger_info(associate_id,device_id,device_name,trademark,device_model,instrument_serial_number," +
            "classification,main_purpose,use_department,dept_code,total_price,purchase_date,storage_location,verify_requirements," +
            "maintenance_due_date,inspection_cycle,device_situation,fixed_assets_status,user_name,user_code,create_time,result_impact,remark) " +
            " VALUES(#{associate_id},#{device_id},#{device_name},#{trademark},#{device_model},#{instrument_serial_number},#{classification}," +
            "#{main_purpose},#{use_department},#{dept_code},#{total_price},#{purchase_date},#{storage_location},#{verify_requirements}," +
            "#{maintenance_due_date},#{inspection_cycle},#{device_situation},#{fixed_assets_status},#{user_name},#{user_code},#{create_time}," +
            "#{result_impact},#{remark});")
    public void AddEquipmentLedgerInfo(JSONObject jsonObject);

    /**
     * 修改台账数据
     * @param jsonObject
     */
    @Update("UPDATE tbl_equipment_ledger_info SET device_name = #{device_name},trademark = #{trademark}," +
            "device_model = #{device_model},instrument_serial_number = #{instrument_serial_number}," +
            "classification = #{classification},main_purpose = #{main_purpose}," +
            "use_department = #{use_department},purchase_date = #{purchase_date}," +
            "storage_location = #{storage_location},verify_requirements = #{verify_requirements}," +
            "maintenance_due_date = #{maintenance_due_date},inspection_cycle = #{inspection_cycle}," +
            "end_calibration_date = #{end_calibration_date},next_calibration_date = #{next_calibration_date}," +
            "end_verification_date = #{end_verification_date},next_verification_date = #{next_verification_date}," +
            "device_situation = #{device_situation},remark = #{remark}," +
            "result_impact = #{result_impact},update_user_name = #{update_user_name}," +
            "update_user_code = #{update_user_code},update_time = #{update_time}" +
            " WHERE id = #{id};")
    public void UpdateEquipmentLedgerInfo(JSONObject jsonObject);

    /**
     * 查询下载的台账数据
     * @param map
     * @return
     */
    @Select("<script>"
            + "SELECT * FROM tbl_equipment_ledger_info WHERE use_status = 0 AND device_status = #{device_status} "
            + "<if test='asset_status != null and asset_status != &quot;&quot;'>"
            + "<choose>"
            + "<when test='dept_status != null and dept_status != &quot;&quot;'>"
            + "AND dept_code = '02' "
            + "</when>"
            + "<otherwise>"
            + "AND use_department = #{use_department} "
            + "</otherwise>"
            + "</choose>"
            + "</if>"
            + "<if test='fixed_assets != null and fixed_assets != &quot;&quot;'>"
            + "AND ( fixed_asset_number != '' OR fixed_assets_status = 1 ) "
            + "</if>"
            + "<if test='dataTable.searchValue != null and dataTable.searchValue != &quot;&quot;'>"
            + "AND ( device_id LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "device_name LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "trademark LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "device_model LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "classification LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "use_department LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "storage_location LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "instrument_serial_number LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) )"
            + "</if>"
            + "<if test='dataTable.sortTable != null and dataTable.sortTable != &quot;&quot;'>"
            + " ORDER BY ${dataTable.sortTable} ${dataTable.sortRule} "
            + "</if>"
            + ";"
            + "</script>")
    public List<Map<String,Object>> FindDonwLoadEquipmentLedgerData(Map<String,Object> map);

    /**
     * 根据id查询台账信息
     * @param id
     * @return
     */
    @Select("SELECT * FROM tbl_equipment_ledger_info WHERE id = #{id} AND use_status = 0;")
    public Map<String,Object> FindEquipmentLedgerById(String id);

    /**
     * 设备停用
     * @param map
     */
    @Update("UPDATE tbl_equipment_ledger_info SET device_status = #{device_status},update_user_name = #{update_user_name}," +
            "update_user_code = #{update_user_code},update_time = #{update_time} WHERE id = #{id};")
    public void ActionDeviceDeactivation(Map<String,Object> map);

    /**
     * 将设备使用状态调整为停用或报废
     * @param id
     */
    @Update("UPDATE tbl_equipment_ledger_info SET device_status = #{device_status} WHERE id = #{id};")
    public void UpdateDeviceStatus(@Param("id") String id,@Param("device_status") String device_status);

    /**
     * 依据采购id获取对应设备信息
     * @param associate_id
     * @return
     */
    @Select("SELECT * FROM tbl_equipment_ledger_info WHERE associate_id = #{associate_id} AND use_status = 0;")
    public Map<String,Object> FindEquipmentLedgerByAssociateId(String associate_id);

    /**
     * 获取所有可用的台账信息
     * @return
     */
    @Select("SELECT * FROM tbl_equipment_ledger_info WHERE use_status = 0 AND device_status = 0;")
    public List<Map<String,Object>> FindAllEquipmentLedgerData();

    /**
     * 修改固定资产编号
     * @param jsonObject
     */
    @Update("UPDATE tbl_equipment_ledger_info SET fixed_asset_number = #{fixed_asset_number}," +
            "update_user_name = #{update_user_name},update_user_code = #{update_user_code}," +
            "update_time = #{update_time} WHERE id = #{id};")
    public void UpdateLedgerFixedAssetNumber(JSONObject jsonObject);

    /**
     * 根据固定资产编号查询项目台账数据
     * @param fixed_asset_number
     * @return
     */
    @Select("SELECT * FROM tbl_equipment_ledger_info WHERE fixed_asset_number = #{fixed_asset_number} AND use_status = 0 AND device_status = 0;")
    public Map<String,Object> FindLedgerInfoByFixedAssetNumber(String fixed_asset_number);

    /**
     * 导入设备台账数据
     * @param list
     */
    @Insert(
            "<script>" +
            "INSERT INTO tbl_equipment_ledger_info(associate_id,device_id,device_name,trademark,device_model," +
            "instrument_serial_number,classification,result_impact,main_purpose,use_department,dept_code,total_price," +
            "purchase_date,storage_location,verify_requirements,maintenance_due_date,inspection_cycle,device_situation," +
            "fixed_asset_number,device_status,user_name,user_code,create_time,remark) " +
            " VALUES "+
            "<foreach collection='list' item='item' index='index' separator=','>"+
            " (#{item.associate_id},#{item.device_id},#{item.device_name},#{item.trademark},#{item.device_model}," +
            "#{item.instrument_serial_number},#{item.classification},#{item.result_impact},#{item.main_purpose}," +
            "#{item.use_department},#{item.dept_code},#{item.total_price},#{item.purchase_date},#{item.storage_location}," +
            "#{item.verify_requirements},#{item.maintenance_due_date},#{item.inspection_cycle},#{item.device_situation}," +
            "#{item.fixed_asset_number},#{item.device_status},#{item.user_name},#{item.user_code},#{item.create_time},#{item.remark})" +
            "</foreach>" +
            "</script>"
    )
    public void ImportEquipmentLedgerInfo(List<Map<String,Object>> list);

    /**
     * 修改设备使用情况
     * @param id
     */
    @Update("UPDATE tbl_equipment_ledger_info SET device_status = 0 WHERE id = #{id};")
    public void UpdateDeviceStatusById(String id);

    /**
     * 修改下次期间核查时间
     * @param next_verification_date
     * @param id
     */
    @Update("UPDATE tbl_equipment_ledger_info SET next_verification_date = #{next_verification_date} WHERE id = '';")
    public void UpdateNextVerificationDateById(@Param("next_verification_date") String next_verification_date,@Param("id") String id);

}
