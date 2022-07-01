package com.tianke.equipmentledger.controller;

import com.tianke.equipmentledger.context.ReturnResult;
import com.tianke.equipmentledger.service.CalibrationManageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/Calibration")
public class CalibrationManageController {
    @Resource
    private CalibrationManageService calibrationManageService;

    @GetMapping("/CreateCalibrationPlan")
    public ReturnResult CreateCalibrationPlan(HttpServletRequest request, HttpServletResponse response){
        return calibrationManageService.CreateCalibrationPlan(request,response);
    }

    @GetMapping("/FindCreateCalibrationPlanData")
    public ReturnResult FindCreateCalibrationPlanData(HttpServletRequest request,HttpServletResponse response){
        return calibrationManageService.FindCreateCalibrationPlanData(request,response);
    }

    @GetMapping("/FillCalibrationResult")
    public ReturnResult FillCalibrationResult(HttpServletRequest request,HttpServletResponse response){
        return calibrationManageService.FillCalibrationResult(request,response);
    }

    @GetMapping("/ReviewCalibrationResult")
    public ReturnResult ReviewCalibrationResult(HttpServletRequest request,HttpServletResponse response){
        return calibrationManageService.ReviewCalibrationResult(request,response);
    }

}
