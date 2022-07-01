package com.tianke.equipmentledger.service;

import com.tianke.equipmentledger.context.ReturnResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface OutOfficeManageService {

    /**
     * 申请设备外出
     * @param request
     * @param response
     */
    public ReturnResult ApplyEquipmentGoingOut(HttpServletRequest request, HttpServletResponse response);

    /**
     * 获取各审批人员需要审批的外出申请数据
     * @param request
     * @param response
     */
    public ReturnResult FindApplyEquipmentGoingOutData(HttpServletRequest request,HttpServletResponse response);

    /**
     * 审批设备外出申请
     * @param request
     * @param response
     */
    public ReturnResult ReviewGoOutApproval(HttpServletRequest request,HttpServletResponse response);

    /**
     * 设备外出申请审核不通过
     * @param request
     * @param response
     * @return
     */
    public ReturnResult ReviewGoOutApprovalFailed(HttpServletRequest request,HttpServletResponse response);

    /**
     * 获取外出模块外出申请信息
     * @param request
     * @param response
     */
    public ReturnResult FindApplyGoingOutData(HttpServletRequest request,HttpServletResponse response);

    /**
     * 综合管理室填写设备回归后的验证接收数据
     * @param request
     * @param response
     */
    public ReturnResult FillEquipmentReturnInfo(HttpServletRequest request,HttpServletResponse response);

    /**
     * 修改设备外出申请信息（打回）
     * @param request
     * @param response
     * @return
     */
    public ReturnResult UpdateOutboundApplicationInfo(HttpServletRequest request,HttpServletResponse response);

}
