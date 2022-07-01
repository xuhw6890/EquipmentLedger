package com.tianke.equipmentledger.service;

import com.tianke.equipmentledger.context.ReturnResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface CalibrationManageService {

    /**
     * 创建设备校准计划
     * @param request
     * @param response
     */
    public ReturnResult CreateCalibrationPlan(HttpServletRequest request, HttpServletResponse response);

    /**
     * 展示校准计划数据
     * @param request
     * @param response
     */
    public ReturnResult FindCreateCalibrationPlanData(HttpServletRequest request,HttpServletResponse response);

    /**
     * 校准完成填写校准结果
     * @param request
     * @param response
     */
    public ReturnResult FillCalibrationResult(HttpServletRequest request,HttpServletResponse response);

    /**
     * 设备使用人复核校准结果
     * @param request
     * @param response
     */
    public ReturnResult ReviewCalibrationResult(HttpServletRequest request,HttpServletResponse response);
}
