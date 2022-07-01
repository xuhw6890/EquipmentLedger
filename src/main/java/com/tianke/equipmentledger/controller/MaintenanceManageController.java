package com.tianke.equipmentledger.controller;

import com.tianke.equipmentledger.context.ReturnResult;
import com.tianke.equipmentledger.service.MaintenanceManageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/Maintain")
public class MaintenanceManageController {
    @Resource
    private MaintenanceManageService maintenanceManageService;

    @GetMapping("/ApplyEquipmentRepair")
    public ReturnResult ApplyEquipmentRepair(HttpServletRequest request, HttpServletResponse response){
        return maintenanceManageService.ApplyEquipmentRepair(request,response);
    }

    @GetMapping("/FindSubimtRepairRequestData")
    public ReturnResult FindSubimtRepairRequestData(HttpServletRequest request,HttpServletResponse response){
        return maintenanceManageService.FindSubimtRepairRequestData(request,response);
    }

    @GetMapping("/FillEquipmentRepairInfo")
    public ReturnResult FillEquipmentRepairInfo(HttpServletRequest request,HttpServletResponse response){
        return maintenanceManageService.FillEquipmentRepairInfo(request,response);
    }

    @GetMapping("/FindRepairRequestData")
    public ReturnResult FindRepairRequestData(HttpServletRequest request,HttpServletResponse response){
        return maintenanceManageService.FindRepairRequestData(request,response);
    }

    @GetMapping("/ReviewRepairApproval")
    public ReturnResult ReviewRepairApproval(HttpServletRequest request,HttpServletResponse response){
        return maintenanceManageService.ReviewRepairApproval(request,response);
    }

    @GetMapping("/ReviewRepairApprovalFailed")
    public ReturnResult ReviewRepairApprovalFailed(HttpServletRequest request,HttpServletResponse response){
        return maintenanceManageService.ReviewRepairApprovalFailed(request,response);
    }

    @GetMapping("/FindEquipmentMaintenanceData")
    public ReturnResult FindEquipmentMaintenanceData(HttpServletRequest request,HttpServletResponse response){
        return maintenanceManageService.FindEquipmentMaintenanceData(request,response);
    }

    @GetMapping("/SaveMaintenanceInsuranceInfo")
    public ReturnResult AddMaintenanceInsuranceInfo(HttpServletRequest request,HttpServletResponse response){
        return maintenanceManageService.AddMaintenanceInsuranceInfo(request,response);
    }

    @GetMapping("/FindMaintenanceInsuranceData")
    public ReturnResult FindMaintenanceInsuranceData(HttpServletRequest request,HttpServletResponse response){
        return maintenanceManageService.FindMaintenanceInsuranceData(request,response);
    }

    @GetMapping("/UpdateMaintenanceRequestInfo")
    public ReturnResult UpdateMaintenanceRequestInfo(HttpServletRequest request,HttpServletResponse response){
        return maintenanceManageService.UpdateMaintenanceRequestInfo(request,response);
    }

}
