package com.tianke.equipmentledger.controller;

import com.tianke.equipmentledger.context.ReturnResult;
import com.tianke.equipmentledger.service.VerificationManageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/Verification")
public class VerificationManageController {
    @Resource
    private VerificationManageService verificationManageService;

    @GetMapping("/SaveDeviceVerificationInfo")
    public ReturnResult AddDeviceVerificationInfo(HttpServletRequest request, HttpServletResponse response){
        return verificationManageService.AddDeviceVerificationInfo(request,response);
    }

    @GetMapping("/FindDeviceVerificationData")
    public ReturnResult FindDeviceVerificationData(HttpServletRequest request,HttpServletResponse response){
        return verificationManageService.FindDeviceVerificationData(request,response);
    }

    @GetMapping("/ActionConfirmVerificationPlan")
    public ReturnResult ConfirmVerificationPlan(HttpServletRequest request,HttpServletResponse response){
        return verificationManageService.ConfirmVerificationPlan(request,response);
    }

}
