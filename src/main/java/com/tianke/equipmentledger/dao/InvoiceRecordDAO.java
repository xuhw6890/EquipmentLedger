package com.tianke.equipmentledger.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface InvoiceRecordDAO {

    /**
     * 添加发票信息
     * @param map
     */
    @DS(value = "a02")
    @Insert("INSERT INTO tbl_funding_account(item_detail,associated_projects,associated_projects_two,associated_projects_name," +
            "use_table,association_id,create_time,operator,user_account,update_time,source_status) " +
            " VALUES(#{item_detail},#{associated_projects},#{associated_projects_two},#{associated_projects_name}," +
            "#{use_table},#{association_id},#{create_time},#{operator},#{user_account},#{update_time},#{source_status});")
    public void AddInvoiceRecordInfo(Map<String,Object> map);
}
