package com.tianke.equipmentledger.service;

import com.tianke.equipmentledger.context.ReturnResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface EquipmentLedgerService {

    /**
     * 查询台账页面数据
     * @param request
     * @param response
     */
    public ReturnResult FindEquipmentLedgerData(HttpServletRequest request, HttpServletResponse response);

    /**
     * 添加台账数据，从采购管理模块添加
     * @param request
     * @param response
     */
    public ReturnResult AddEquipmentLedgerInfo(HttpServletRequest request,HttpServletResponse response);

    /**
     * 修改台账数据
     * @param request
     * @param response
     */
    public ReturnResult UpdateEquipmentLedgerInfo(HttpServletRequest request,HttpServletResponse response);

    /**
     * 删除台账数据
     * @param request
     * @param response
     */
    public ReturnResult DeleteEquipmentLedgerInfo(HttpServletRequest request,HttpServletResponse response);


}
