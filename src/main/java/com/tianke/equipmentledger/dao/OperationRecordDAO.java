package com.tianke.equipmentledger.dao;

import com.tianke.equipmentledger.entity.DataTable;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface OperationRecordDAO {


    @Insert("INSERT INTO tbl_operation_record(id2,raw_data,operation_record,user_code,user_name,create_time)" +
            " VALUES(#{id2},#{raw_data},#{operation_record},#{user_code},#{user_name},#{create_time});")
    public void SaveUserOperationRecord(Map<String,Object> map);

    /**
     * 查询用户操作记录数据
     * @param
     * @return
     */
    @Select("<script>"
            + "SELECT * FROM tbl_operation_record WHERE use_status = 0 "
            + "<if test='user_code != null and user_code != &quot;&quot;'>"
            + "AND user_code = #{user_code} "
            + "</if>"
            + "<if test='dataTable.searchValue != null and dataTable.searchValue != &quot;&quot;'>"
            + "AND ( operation_record LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "user_name LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) )"
            + "</if>"
            + "<if test='dataTable.sortTable != null and dataTable.sortTable != &quot;&quot;'>"
            + " ORDER BY ${dataTable.sortTable} ${dataTable.sortRule} "
            + "</if>"
            + "LIMIT #{dataTable.start},#{dataTable.end};"
            + "</script>")
    public List<Map<String,Object>> FindUserOperationRecord(Map<String,Object> map);

    @Select("<script>"
            + "SELECT COUNT(id) AS total FROM tbl_operation_record WHERE use_status = 0 "
            + "<if test='user_code != null and user_code != &quot;&quot;'>"
            + "AND user_code = #{user_code} "
            + "</if>"
            + "<if test='dataTable.searchValue != null and dataTable.searchValue != &quot;&quot;'>"
            + "AND ( operation_record LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) || "
            + "user_name LIKE CONCAT ('%',CONCAT(#{dataTable.searchValue},'%')) )"
            + "</if>"
            + ";"
            + "</script>")
    public int FindUserOperationRecordTotal(Map<String,Object> map);

}
