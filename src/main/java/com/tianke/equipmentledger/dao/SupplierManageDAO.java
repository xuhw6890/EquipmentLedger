package com.tianke.equipmentledger.dao;

import net.sf.json.JSONObject;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface SupplierManageDAO {

    /**
     * 依据公司名查询对应信息
     * @param company_name
     * @return
     */
    @Select("SELECT * FROM tbl_supplier_company WHERE company_name = #{company_name} AND use_status = 0;")
    public Map<String,Object> FindSupplierCompanyInfoByName(String company_name);

    /**
     * 添加供应商信息
     * @param jsonObject
     */
    @Insert("INSERT INTO tbl_supplier_company(company_name,user_phone,contact_person,product_service,other_ways_contact," +
            "qualification_documents_path,validity_period,remark,create_time)" +
            " VALUES(#{company_name},#{user_phone},#{contact_person},#{product_service},#{other_ways_contact}," +
            "#{qualification_documents_path},#{validity_period},#{remark},#{create_time});")
    public void SaveSupplierCompanyInfo(JSONObject jsonObject);

    /**
     * 获取当前所有可用的供应商数据
     * @return
     */
    @Select("<script>"
            + "SELECT * FROM tbl_supplier_company WHERE use_status = 0 "
            + "<if test='search_info != null and search_info != &quot;&quot;'>"
            + "AND company_name LIKE CONCAT ('%',CONCAT(#{search_info},'%')) "
            + "</if>"
            + ";"
            + "</script>")
    public List<Map<String,Object>> FindSupplierCompanyData(@Param("search_info") String search_info);

    /**
     * 删除供应商
     * @param id
     */
    @Update("UPDATE tbl_supplier_company SET use_status = 1 WHERE id = #{id}")
    public void DeleteSupplierCompanyData(String id);

    /**
     * 根据公司名称查询数量
     * @param company_name
     * @return
     */
    @Select("SELECT COUNT(id) AS total FROM tbl_supplier_company WHERE company_name = #{company_name} AND use_status = 0;")
    public int FindCompanyNameTotal(String company_name);

    /**
     * 更新供应商信息
     * @param jsonObject
     */
    @Update("UPDATE tbl_supplier_company SET company_name = #{company_name}," +
            "user_phone = #{user_phone}," +
            "contact_person = #{contact_person}," +
            "product_service = #{product_service}," +
            "other_ways_contact = #{other_ways_contact}," +
            "validity_period = #{validity_period}," +
            "remark = #{remark} " +
            "WHERE id = #{id};")
    public void UpdateSupplierCompanyData(JSONObject jsonObject);

    /**
     * 依据id查询对应信息
     * @param id
     * @return
     */
    @Select("SELECT * FROM tbl_supplier_company WHERE id = #{id} AND use_status = 0;")
    public Map<String,Object> FindSupplierCompanyInfoById(String id);

    /**
     * 保存供应商资质文件存放路径
     * @param map
     */
    @Update("UPDATE tbl_supplier_company SET qualification_documents_path = #{qualification_documents_path} WHERE id = #{id};")
    public void SaveQualificationDocumentsPath(Map<String,Object> map);

    /**
     * 获取当前所有可用的供应商名称
     * @return
     */
    @Select("SELECT company_name FROM tbl_supplier_company WHERE use_status = 0;")
    public List<String> FindSupplierNameAll();
}
