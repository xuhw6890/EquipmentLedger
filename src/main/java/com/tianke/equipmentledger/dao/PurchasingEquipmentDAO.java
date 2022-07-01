package com.tianke.equipmentledger.dao;

import com.tianke.equipmentledger.entity.DataTable;
import net.sf.json.JSONObject;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface PurchasingEquipmentDAO {

    /**
     * 根据id查询采购信息
     * @param id2
     * @return
     */
    @Select("SELECT * FROM tbl_purchasing_equipment_info WHERE id = #{id2} AND use_status = 0;")
    public Map<String,Object> FindPurchasingEquipmentInfoByIdTwo(String id2);

    /**
     * 根据id查询采购信息
     * @param id
     * @return
     */
    @Select("SELECT * FROM tbl_purchasing_equipment_info WHERE id = #{id} AND use_status = 0;")
    public Map<String,Object> FindPurchasingEquipmentInfoById(String id);

    /**
     * 添加采购订单数据
     * @param map
     */
    @Insert("INSERT INTO tbl_purchasing_equipment_info(id2,device_name,trademark,device_model,supplier,order_quantity," +
            "apply_department,dept_code,create_time,main_purpose) " +
            "VALUES(#{id2},#{device_name},#{trademark},#{device_model},#{supplier},#{order_quantity}," +
            "#{apply_department},#{dept_code},#{create_time},#{main_purpose});")
    public void AddPurchasingEquipmentInfo(Map<String,Object> map);

    /**
     * 修改补充采购订单数据
     * @param jsonObject
     */
    @Update("UPDATE tbl_purchasing_equipment_info SET unit_price = #{unit_price},total_price = #{total_price}," +
            "maintenance_period = #{maintenance_period},user_code = #{user_code}," +
            "user_name = #{user_name},update_time = #{update_time}," +
            "info_supplement_status = #{info_supplement_status} " +
            "WHERE id2 = #{id};")
    public void UpdatePurchasingEquipmentInfo(JSONObject jsonObject);

    /**
     * 修改采购订单中的合同生成状态
     * @param map
     */
    @Update("UPDATE tbl_purchasing_equipment_info SET purchase_status = #{purchase_status},update_time = #{update_time} WHERE id = #{id};")
    public void UpdatePurchasingEquipmentPurchaseStatus(Map<String,Object> map);

    /**
     * 查询采购订单
     * @param map
     * @return
     */
    @Select("<script>"
            + "SELECT * FROM tbl_purchasing_equipment_info WHERE use_status = 0 "
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
            + "device_model LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) )"
            + "</if>"
            + "<if test='dataTable.sortTable != null and dataTable.sortTable != &quot;&quot;'>"
            + " ORDER BY ${dataTable.sortTable} ${dataTable.sortRule} "
            + "</if>"
            + "LIMIT #{dataTable.start},#{dataTable.end};"
            + "</script>")
    public List<Map<String,Object>> FindAllPurchaseEquipmentOrder(Map<String,Object> map);

    @Select("<script>"
            + "SELECT COUNT(id) AS total FROM tbl_purchasing_equipment_info WHERE use_status = 0 "
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
            + "device_model LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) )"
            + "</if>"
            + ";"
            + "</script>")
    public int FindAllPurchaseEquipmentOrderTotal(Map<String,Object> map);

    /**
     * 修改采购信息的发票状态
     * @param id
     */
    @Update("UPDATE tbl_purchasing_equipment_info SET invoice_status = 1 WHERE id = #{id};")
    public void UpdateInvoiceStatusByid(String id);

    /**
     * 修改设备验收状态
     * @param id
     */
    @Update("UPDATE tbl_purchasing_equipment_info SET acceptance_status = 1 WHERE id = #{id};")
    public void UpdateAcceptanceStatusById(String id);

    /**
     * 修改设备验收数量
     * @param map
     */
    @Update("UPDATE tbl_purchasing_equipment_info SET acceptance_number = #{acceptance_number} WHERE id = #{id};")
    public void UpdateAcceptanceNumberById(Map<String,Object> map);

}
