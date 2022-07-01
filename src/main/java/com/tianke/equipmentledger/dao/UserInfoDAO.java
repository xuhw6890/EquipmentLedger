package com.tianke.equipmentledger.dao;

import com.baomidou.dynamic.datasource.annotation.DS;
import net.sf.json.JSONObject;
import org.apache.ibatis.annotations.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserInfoDAO {

    @Select("SELECT * FROM tbl_user_info WHERE user_code = #{user_code} AND user_status = 0;")
    public Map<String,Object> FindUserAppKeyByUserCode(String user_code);

    @Insert("INSERT INTO tbl_user_info(user_code,user_name,dept_code,department,user_phone,user_signature,appkey,appsecret,create_time)\n" +
            " VALUES(#{user_code},#{user_name},#{dept_code},#{department},#{user_phone},#{user_signature},#{appkey},#{appsecret},#{create_time});")
    public void AddLegderUserInfo(Map<String,Object> map);

    @Select("SELECT * FROM tbl_user_info WHERE appkey = #{appkey} AND user_status = 0;")
    public Map<String,Object> FindUserInfo(String appkey);

    /**
     * 依据职位查询用户信息
     * @param job_title_status
     * @return
     */
    @Select("SELECT * FROM tbl_user_info WHERE job_title_status = #{job_title_status} AND user_status = 0;")
    public Map<String,Object> FindUserJobTitleInfo(int job_title_status);

    /**
     * 获取除管理员外所有人员的信息
     * @return
     */
    @Select("SELECT * FROM tbl_user_info WHERE user_rights = 0 AND user_status = 0 ORDER BY create_time ASC;")
    public List<Map<String,Object>> FindAllUserInfo();

    /**
     * 修改用户权限信息
     * @param jsonObject
     */
    @Update("UPDATE tbl_user_info SET " +
            "dept_code = #{dept_code}," +
            "department = #{department}," +
            "job_title_status = #{job_title_status}," +
            "device_admin = #{device_admin}," +
            "fixed_asset_admin = #{fixed_asset_admin}," +
            "procurement_staff = #{procurement_staff}," +
            "ledger_status = #{ledger_status}," +
            "request_status = #{request_status}," +
            "purchasing_status = #{purchasing_status}," +
            "calibration_status = #{calibration_status}," +
            "verification_status = #{verification_status}," +
            "maintain_status = #{maintain_status}," +
            "scrap_status = #{scrap_status}," +
            "go_out_status = #{go_out_status}," +
            "other_status = #{other_status}," +
            "operation_record_status = #{operation_record_status}, " +
            "approval_authority = #{approval_authority}, " +
            "department = #{department} " +
            "WHERE id = #{id};")
    public void UpdateUserRightsInfo(JSONObject jsonObject);

    /**
     * 用户停用
     * @param id
     */
    @Update("UPDATE tbl_user_info SET user_status = 1 WHERE id = #{id};")
    public void DeleteUserInfo(String id);

    /**
     * 依据id查询用户信息
     * @param id
     * @return
     */
    @Select("SELECT * FROM tbl_user_info WHERE id = #{id} AND user_status = 0;")
    public Map<String,Object> FindUserInfoById(String id);

    /**
     * 获取公司职位信息
     * @return
     */
    @Select("<script>"
            + "SELECT * FROM tbl_job_title "
            + "<if test='approval_type != null and approval_type != &quot;&quot;'>"
            + " WHERE approval_status = 1 "
            + "</if>"
            + " ORDER BY id ASC;"
            + "</script>")
    public List<Map<String,Object>> FindCompanyJobInfo(@Param("approval_type") String approval_type);

    /**
     * 获取部门架构
     * @return
     */
    @Select("SELECT * FROM tbl_departmental_structure WHERE use_status = 0;")
    public List<Map<String,Object>> FindDepartmentalStructure();

    /**
     * 获取审批架构
     * @return
     */
    @Select("SELECT * FROM tbl_approval_framework;")
    public List<Map<String,Object>> FindApprovalFramework();

    /**
     * 获取固定资产管理员及财务的手机号
     * @return
     */
    @Select("<script>"
            + "SELECT user_phone FROM tbl_user_info WHERE user_status = 0 "
            + "<choose>"
            + "<when test='approval_authority != null and approval_authority != &quot;&quot;'>"
            + "AND (fixed_asset_admin = 1 OR approval_authority LIKE '%5%' ) "
            + "</when>"
            + "<otherwise>"
            + "AND fixed_asset_admin = 1 "
            + "</otherwise>"
            + "</choose>"
            + ";"
            + "</script>")
    public List<String> FindAssetManagerMobilePhone(@Param("approval_authority") String approval_authority);

    /**
     * 获取用户电子签名信息
     * @param
     * @return
     */
    @Select("SELECT user_code,user_signature FROM tbl_user_info WHERE user_status = 0;")
    public List<Map<String,String>> FindUserSignatureInfo();

}
