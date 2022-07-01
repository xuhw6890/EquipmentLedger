package com.tianke.equipmentledger.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface SerialNumberDAO {

    /**
     * 添加采购审批流水号
     * @param map
     */
    @Insert("INSERT INTO tbl_approval_serial_number(approval_form_id,user_code,serial_number,create_time,table_status) " +
            "VALUES(#{approval_form_id},#{user_code},#{serial_number},#{create_time},#{table_status});")
    public void AddApprovalSerialNumber(Map<String,Object> map);

    @Select("SELECT COUNT(id) AS total FROM tbl_approval_serial_number WHERE create_time LIKE CONCAT(#{create_time},'%');")
    public int FindCurrentYearSerialNumber(String create_time);

    /**
     * 获取用户下载时该申请表的流水号
     * @param map
     * @return
     */
    @Select("SELECT serial_number FROM tbl_approval_serial_number WHERE approval_form_id = #{approval_form_id} AND table_status = #{table_status};")
    public String FindApprovalSerialNumber(Map<String,Object> map);
}
