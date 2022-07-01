package com.tianke.equipmentledger.controller;

import com.tianke.equipmentledger.context.ReturnResult;
import com.tianke.equipmentledger.service.OutOfficeManageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/GoOut")
public class OutOfficeManageController {
    @Resource
    private OutOfficeManageService outOfficeManageService;

    @GetMapping("/ApplyEquipmentGoingOut")
    public ReturnResult ApplyEquipmentGoingOut(HttpServletRequest request, HttpServletResponse response){
        return outOfficeManageService.ApplyEquipmentGoingOut(request,response);
    }

    @GetMapping("/FindApplyEquipmentGoingOutData")
    public ReturnResult FindApplyEquipmentGoingOutData(HttpServletRequest request,HttpServletResponse response){
        return outOfficeManageService.FindApplyEquipmentGoingOutData(request,response);
    }

    @GetMapping("/ReviewGoOutApproval")
    public ReturnResult ReviewGoOutApproval(HttpServletRequest request,HttpServletResponse response){
        return outOfficeManageService.ReviewGoOutApproval(request,response);
    }

    @GetMapping("/ReviewGoOutApprovalFailed")
    public ReturnResult ReviewGoOutApprovalFailed(HttpServletRequest request,HttpServletResponse response){
        return outOfficeManageService.ReviewGoOutApprovalFailed(request,response);
    }

    @GetMapping("/FindApplyGoingOutData")
    public ReturnResult FindApplyGoingOutData(HttpServletRequest request,HttpServletResponse response){
        return outOfficeManageService.FindApplyGoingOutData(request,response);
    }

    @GetMapping("/FillEquipmentReturnInfo")
    public ReturnResult FillEquipmentReturnInfo(HttpServletRequest request,HttpServletResponse response){
        return outOfficeManageService.FillEquipmentReturnInfo(request,response);
    }

    @GetMapping("/UpdateOutboundApplicationInfo")
    public ReturnResult UpdateOutboundApplicationInfo(HttpServletRequest request,HttpServletResponse response){
        return outOfficeManageService.UpdateOutboundApplicationInfo(request,response);
    }

}
