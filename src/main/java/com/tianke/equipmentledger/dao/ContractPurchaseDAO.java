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
public interface ContractPurchaseDAO {
    /**
     * 查询该时间段内有多少合同数
     * @param map
     * @return
     */
    @Select("SELECT COUNT(id) AS total FROM tbl_contract_purchase WHERE create_time <![CDATA[ >= ]]> #{start_time} AND create_time <![CDATA[ < ]]> #{end_time} AND use_status = 0;")
    public int FindContractPurchaseNumber(Map<String,Object> map);

    /**
     * 生成采购合同信息
     * @param jsonObject
     */
    @Insert("INSERT INTO tbl_contract_purchase(id2,contract_code,contract_name,signing_date,contract_amount,settlement_method," +
            "manager,manager_code,attachment_address,create_time,remark,apply_department,dept_code)" +
            "VALUES(#{id},#{contract_code},#{contract_name},#{signing_date},#{contract_amount},#{settlement_method}," +
            "#{manager},#{manager_code},#{attachment_address},#{create_time},#{remark},#{apply_department},#{dept_code});")
    public void AddContractPurchaseInfo(JSONObject jsonObject);

    /**
     * 根据id查询该采购合同的基本信息
     * @param id
     * @return
     */
    @Select("SELECT * FROM tbl_contract_purchase WHERE id = #{id} AND use_status = 0;")
    public Map<String,Object> FindContractPurchaseInfoById(String id);

    /**
     * 根据id修改合同附件路径
     * @param map
     */
    @Update("UPDATE tbl_contract_purchase SET attachment_address = #{attachment_address} WHERE id = #{id};")
    public void UpdateContractAttachmentAddress(Map<String,Object> map);

    /**
     * 查询合同管理模块数据
     * @param map
     * @return
     */
    @Select("<script>"
            + "SELECT * FROM tbl_contract_purchase WHERE use_status = 0 "
            + "<choose>"
            + "<when test='dept_status != null and dept_status != &quot;&quot;'>"
            + "AND dept_code = '02' "
            + "</when>"
            + "<otherwise>"
            + "AND apply_department = #{apply_department} "
            + "</otherwise>"
            + "</choose>"
            + "<if test='dataTable.searchValue != null and dataTable.searchValue != &quot;&quot;'>"
            + "AND ( contract_code LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "contract_name LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "manager LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) )"
            + "</if>"
            + "<if test='dataTable.sortTable != null and dataTable.sortTable != &quot;&quot;'>"
            + " ORDER BY ${dataTable.sortTable} ${dataTable.sortRule} "
            + "</if>"
            + "LIMIT #{dataTable.start},#{dataTable.end};"
            + "</script>")
    public List<Map<String,Object>> FindAllContractPurchaseData(Map<String,Object> map);

    @Select("<script>"
            + "SELECT COUNT(id) AS total FROM tbl_contract_purchase WHERE use_status = 0 "
            + "<choose>"
            + "<when test='dept_status != null and dept_status != &quot;&quot;'>"
            + "AND dept_code = '02' "
            + "</when>"
            + "<otherwise>"
            + "AND apply_department = #{apply_department} "
            + "</otherwise>"
            + "</choose>"
            + "<if test='dataTable.searchValue != null and dataTable.searchValue != &quot;&quot;'>"
            + "AND ( contract_code LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "contract_name LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "manager LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) )"
            + "</if>"
            + ";"
            + "</script>")
    public int FindAllContractPurchaseDataTotal(Map<String,Object> map);

    /**
     * 根据合同编号查询采购合同信息
     * @param contract_code
     * @return
     */
    @Select("SELECT * FROM tbl_contract_purchase WHERE contract_code = #{contract_code} AND use_status = 0;")
    public Map<String,Object> FindContractPurchaseByContractCode(String contract_code);
}
