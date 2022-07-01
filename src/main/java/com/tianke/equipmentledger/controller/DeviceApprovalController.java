package com.tianke.equipmentledger.controller;

import com.tianke.equipmentledger.context.ReturnResult;
import com.tianke.equipmentledger.service.DeviceApprovalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/Approval")
public class DeviceApprovalController {
    @Resource
    private DeviceApprovalService deviceApprovalService;

    @GetMapping("/ApplyDeviceDeactivation")
    public ReturnResult ApplyDeviceDeactivation(HttpServletRequest request, HttpServletResponse response){
        return deviceApprovalService.ApplyDeviceDeactivation(request,response);
    }

    @GetMapping("/FindScrapApprovalData")
    public ReturnResult FindScrapApprovalData(HttpServletRequest request, HttpServletResponse response){
        return deviceApprovalService.FindScrapApprovalData(request,response);
    }

    @GetMapping("/ReviewScrapApplications")
    public ReturnResult ReviewScrapApplications(HttpServletRequest request,HttpServletResponse response){
        return deviceApprovalService.ReviewScrapApplications(request,response);
    }

    @GetMapping("/ReviewScrapApplicationsFailed")
    public ReturnResult ReviewScrapApplicationsFailed(HttpServletRequest request,HttpServletResponse response){
        return deviceApprovalService.ReviewScrapApplicationsFailed(request,response);
    }

    @GetMapping("/FindChangeApplicationData")
    public ReturnResult FindChangeApplicationData(HttpServletRequest request,HttpServletResponse response){
        return deviceApprovalService.FindChangeApplicationData(request,response);
    }

    @GetMapping("/FindApprovalInfoReminder")
    public ReturnResult FindApprovalInfoReminder(HttpServletRequest request,HttpServletResponse response){
        return deviceApprovalService.FindApprovalInfoReminder(request,response);
    }

    @GetMapping("/ActionDeviceEnabled")
    public ReturnResult ActionDeviceEnabled(HttpServletRequest request,HttpServletResponse response){
        return deviceApprovalService.ActionDeviceEnabled(request,response);
    }

    @GetMapping("/UpdateScrapApplicationInfo")
    public ReturnResult UpdateScrapApplicationInfo(HttpServletRequest request,HttpServletResponse response){
        return deviceApprovalService.UpdateScrapApplicationInfo(request,response);
    }


}
