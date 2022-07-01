package com.tianke.equipmentledger.service;

import com.tianke.equipmentledger.context.ReturnResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface DeviceApprovalService {

    /**
     * 申请设备停用
     * @param request
     * @param response
     */
    public ReturnResult ApplyDeviceDeactivation(HttpServletRequest request, HttpServletResponse response);

    /**
     * 查询该用户需要审批的设备报废申请信息
     * @param request
     * @param response
     */
    public ReturnResult FindScrapApprovalData(HttpServletRequest request, HttpServletResponse response);

    /**
     * 审核设备报废信息
     * @param request
     * @param response
     */
    public ReturnResult ReviewScrapApplications(HttpServletRequest request,HttpServletResponse response);

    /**
     * 设备报废申请审核不通过
     * @param request
     * @param response
     * @return
     */
    public ReturnResult ReviewScrapApplicationsFailed(HttpServletRequest request,HttpServletResponse response);

    /**
     * 展示变更模块数据
     * @param request
     * @param response
     * @return
     */
    public ReturnResult FindChangeApplicationData(HttpServletRequest request,HttpServletResponse response);

    /**
     * 获取审批权限信息提醒
     * @param request
     * @param response
     * @return
     */
    public ReturnResult FindApprovalInfoReminder(HttpServletRequest request,HttpServletResponse response);

    /**
     * 重新启用停用的设备
     * @param request
     * @param response
     * @return
     */
    public ReturnResult ActionDeviceEnabled(HttpServletRequest request,HttpServletResponse response);

    /**
     * 修改设备停用/报废申请信息（打回）
     * @param request
     * @param response
     * @return
     */
    public ReturnResult UpdateScrapApplicationInfo(HttpServletRequest request,HttpServletResponse response);

}
