package com.tianke.equipmentledger.dao;

import com.tianke.equipmentledger.entity.DataTable;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface MaintenanceInsuranceDAO {

    /**
     * 添加维保信息
     * @param map
     */
    @Insert("INSERT INTO tbl_maintenance_insurance(id2,device_id,device_name,device_model,maintenance_period," +
            "starting_date,end_date,user_name,user_code,create_time)" +
            " VALUES(#{id2},#{device_id},#{device_name},#{device_model},#{maintenance_period}," +
            "#{starting_date},#{end_date},#{user_name},#{user_code},#{create_time});")
    public void SaveMaintenanceInsuranceInfo(Map<String,Object> map);

    /**
     * 查询维保记录数据
     * @param dataTable
     * @return
     */
    @Select("<script>"
            + "SELECT * FROM tbl_maintenance_insurance WHERE use_status = 0 "
            + "<if test='searchValue != null and searchValue != &quot;&quot;'>"
            + "AND ( device_id LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "device_name LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "device_model LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) )"
            + "</if>"
            + "<if test='sortTable != null and sortTable != &quot;&quot;'>"
            + " ORDER BY ${sortTable} ${sortRule} "
            + "</if>"
            + "LIMIT #{start},#{end};"
            + "</script>")
    public List<Map<String,Object>> FindAllMaintenanceInsurance(DataTable dataTable);

    @Select("<script>"
            + "SELECT COUNT(id) AS total FROM tbl_maintenance_insurance WHERE use_status = 0 "
            + "<if test='searchValue != null and searchValue != &quot;&quot;'>"
            + "AND ( device_id LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "device_name LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) || "
            + "device_model LIKE CONCAT ('%',CONCAT(#{searchValue},'%')) )"
            + "</if>"
            + ";"
            + "</script>")
    public int FindAllMaintenanceInsuranceTotal(DataTable dataTable);

}
