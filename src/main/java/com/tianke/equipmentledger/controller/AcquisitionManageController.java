package com.tianke.equipmentledger.controller;

import com.tianke.equipmentledger.context.ReturnResult;
import com.tianke.equipmentledger.service.AcquisitionManageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/Acquisition")
public class AcquisitionManageController {
    @Resource
    private AcquisitionManageService acquisitionManageService;

    @GetMapping("/SubmitPurchaseRequisition")
    public ReturnResult SubmitPurchaseRequisition(HttpServletRequest request, HttpServletResponse response){
        return acquisitionManageService.SubmitPurchaseRequisition(request,response);
    }

    @GetMapping("/FindPurchaseApprovalData")
    public ReturnResult FindPurchaseApprovalData(HttpServletRequest request,HttpServletResponse response){
        return acquisitionManageService.FindPurchaseApprovalData(request,response);
    }

    @GetMapping("/ActionPurchaseApproval")
    public ReturnResult ActionPurchaseApproval(HttpServletRequest request,HttpServletResponse response){
        return acquisitionManageService.ActionPurchaseApproval(request,response);
    }

    @GetMapping("/ActionPurchaseApprovalFailed")
    public ReturnResult ActionPurchaseApprovalFailed(HttpServletRequest request,HttpServletResponse response){
        return acquisitionManageService.ActionPurchaseApprovalFailed(request,response);
    }

    @GetMapping("/FindApprovedPurchaseOrderData")
    public ReturnResult FindApproveCompletedPurchaseOrders(HttpServletRequest request,HttpServletResponse response){
        return acquisitionManageService.FindApproveCompletedPurchaseOrders(request,response);
    }

    @GetMapping("/FindPurchaseEquipmentOrderData")
    public ReturnResult FindPurchaseEquipmentOrderData(HttpServletRequest request,HttpServletResponse response){
        return acquisitionManageService.FindPurchaseEquipmentOrderData(request,response);
    }

    @GetMapping("/FillPurchasedEquipmentInfo")
    public ReturnResult FillPurchasedEquipmentInfo(HttpServletRequest request,HttpServletResponse response){
        return acquisitionManageService.FillPurchasedEquipmentInfo(request,response);
    }

    @GetMapping("/CreatePurchaseContract")
    public ReturnResult GeneratePurchaseContract(HttpServletRequest request, HttpServletResponse response){
        return acquisitionManageService.GeneratePurchaseContract(request,response);
    }

    @GetMapping("/FindPurchaseContractData")
    public ReturnResult FindPurchaseContractData(HttpServletRequest request, HttpServletResponse response){
        return acquisitionManageService.FindPurchaseContractData(request,response);
    }

    @GetMapping("/VerifyLedgerData")
    public ReturnResult VerifyLedgerData(HttpServletRequest request,HttpServletResponse response){
        return acquisitionManageService.VerifyLedgerData(request,response);
    }

    @GetMapping("/AddInvoiceRecord")
    public ReturnResult AddInvoiceRecord(HttpServletRequest request,HttpServletResponse response){
        return acquisitionManageService.AddInvoiceRecord(request,response);
    }

    @GetMapping("/SubmitAcceptanceApplication")
    public ReturnResult SubmitAcceptanceApplication(HttpServletRequest request,HttpServletResponse response){
        return acquisitionManageService.SubmitAcceptanceApplication(request,response);
    }

    @GetMapping("/FindVerifyApprovalData")
    public ReturnResult FindVerifyApprovalData(HttpServletRequest request,HttpServletResponse response){
        return acquisitionManageService.FindVerifyApprovalData(request,response);
    }

    @GetMapping("/FindAcceptanceApplicationData")
    public ReturnResult FindAcceptanceApplicationData(HttpServletRequest request,HttpServletResponse response){
        return acquisitionManageService.FindAcceptanceApplicationData(request,response);
    }

    @GetMapping("/ActionAcceptanceApproval")
    public ReturnResult ActionAcceptanceApproval(HttpServletRequest request,HttpServletResponse response){
        return acquisitionManageService.ActionAcceptanceApproval(request,response);
    }

    @GetMapping("/ActionAcceptanceApprovalFailed")
    public ReturnResult ActionAcceptanceApprovalFailed(HttpServletRequest request,HttpServletResponse response){
        return acquisitionManageService.ActionAcceptanceApprovalFailed(request,response);
    }

    @GetMapping("/UpdatePurchaseRequisitionInfo")
    public ReturnResult UpdatePurchaseRequisitionInfo(HttpServletRequest request,HttpServletResponse response){
        return acquisitionManageService.UpdatePurchaseRequisitionInfo(request,response);
    }

    @GetMapping("/UpdateAcceptanceApplicationInfo")
    public ReturnResult UpdateAcceptanceApplicationInfo(HttpServletRequest request,HttpServletResponse response){
        return acquisitionManageService.UpdateAcceptanceApplicationInfo(request,response);
    }

}
