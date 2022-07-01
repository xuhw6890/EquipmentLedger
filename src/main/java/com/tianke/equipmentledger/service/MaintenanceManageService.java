package com.tianke.equipmentledger.service;

import com.tianke.equipmentledger.context.ReturnResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface MaintenanceManageService {

    /**
     * 用户申请设备维修
     * @param request
     * @param response
     */
    public ReturnResult ApplyEquipmentRepair(HttpServletRequest request, HttpServletResponse response);

    /**
     * 设备管理员查看用户提交的设备维修申请
     * @param request
     * @param response
     */
    public ReturnResult FindSubimtRepairRequestData(HttpServletRequest request,HttpServletResponse response);

    /**
     * 设备管理员填写设备维修情况
     * @param request
     * @param response
     */
    public ReturnResult FillEquipmentRepairInfo(HttpServletRequest request,HttpServletResponse response);

    /**
     * 查询设备维修申请数据
     * @param request
     * @param response
     */
    public ReturnResult FindRepairRequestData(HttpServletRequest request,HttpServletResponse response);

    /**
     * 审批设备维修申请
     * @param request
     * @param response
     */
    public ReturnResult ReviewRepairApproval(HttpServletRequest request,HttpServletResponse response);

    /**
     * 设备维修申请审批不通过
     * @param request
     * @param response
     * @return
     */
    public ReturnResult ReviewRepairApprovalFailed(HttpServletRequest request,HttpServletResponse response);

    /**
     * 查询设备维修情况数据
     * @param request
     * @param response
     */
    public ReturnResult FindEquipmentMaintenanceData(HttpServletRequest request,HttpServletResponse response);

    /**
     * 添加维保记录
     * @param request
     * @param response
     */
    public ReturnResult AddMaintenanceInsuranceInfo(HttpServletRequest request,HttpServletResponse response);

    /**
     * 查询维保记录数据
     * @param request
     * @param response
     */
    public ReturnResult FindMaintenanceInsuranceData(HttpServletRequest request,HttpServletResponse response);

    /**
     * 修改维修申请信息（打回）
     * @param request
     * @param response
     * @return
     */
    public ReturnResult UpdateMaintenanceRequestInfo(HttpServletRequest request,HttpServletResponse response);
}
