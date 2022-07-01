package com.tianke.equipmentledger.controller;

import com.tianke.equipmentledger.context.ReturnResult;
import com.tianke.equipmentledger.service.EquipmentLedgerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/Ledger")
public class EquipmentLedgerController {
    @Resource
    private EquipmentLedgerService equipmentLedgerService;

    @GetMapping("/FindEquipmentLedgerData")
    public ReturnResult FindEquipmentLedgerData(HttpServletRequest request, HttpServletResponse response){
        return equipmentLedgerService.FindEquipmentLedgerData(request,response);
    }

    @GetMapping("/AddEquipmentLedgerInfo")
    public ReturnResult AddEquipmentLedgerInfo(HttpServletRequest request,HttpServletResponse response){
        return equipmentLedgerService.AddEquipmentLedgerInfo(request,response);
    }

    @GetMapping("/UpdateEquipmentLedgerInfo")
    public ReturnResult UpdateEquipmentLedgerInfo(HttpServletRequest request,HttpServletResponse response){
        return equipmentLedgerService.UpdateEquipmentLedgerInfo(request,response);
    }
    //设备停用/报废后会自动消失在台账中，故不需要人工删除（待定）
    @GetMapping("/DeleteEquipmentLedgerInfo")
    public ReturnResult DeleteEquipmentLedgerInfo(HttpServletRequest request,HttpServletResponse response){
        return equipmentLedgerService.DeleteEquipmentLedgerInfo(request,response);
    }


}
