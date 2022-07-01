package com.tianke.equipmentledger.service;

import com.tianke.equipmentledger.context.ReturnResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface VerificationManageService {

    /**
     * 添加设备期间核查计划信息
     * @param request
     * @param response
     */
    public ReturnResult AddDeviceVerificationInfo(HttpServletRequest request, HttpServletResponse response);

    /**
     * 获取期间核查管理页面设备核查计划信息
     * @param request
     * @param response
     */
    public ReturnResult FindDeviceVerificationData(HttpServletRequest request,HttpServletResponse response);

    /**
     * 确认核查计划
     * @param request
     * @param response
     */
    public ReturnResult ConfirmVerificationPlan(HttpServletRequest request,HttpServletResponse response);
}
