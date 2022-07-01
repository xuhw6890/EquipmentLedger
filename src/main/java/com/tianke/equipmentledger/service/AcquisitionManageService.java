package com.tianke.equipmentledger.service;

import com.sun.scenario.effect.impl.sw.java.JSWBrightpassPeer;
import com.tianke.equipmentledger.context.ReturnResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface AcquisitionManageService {
    /**
     * 提交采购申请
     * @param request
     * @param response
     */
    public ReturnResult SubmitPurchaseRequisition(HttpServletRequest request, HttpServletResponse response);

    /**
     * 获取需要审批的采购信息
     * @param request
     * @param response
     */
    public ReturnResult FindPurchaseApprovalData(HttpServletRequest request, HttpServletResponse response);

    /**
     * 采购申请审批
     * @param request
     * @param response
     */
    public ReturnResult ActionPurchaseApproval(HttpServletRequest request,HttpServletResponse response);

    /**
     * 采购申请审批失败
     * @param request
     * @param response
     * @return
     */
    public ReturnResult ActionPurchaseApprovalFailed(HttpServletRequest request,HttpServletResponse response);

    /**
     * 获取申请采购信息
     * @param request
     * @param response
     */
    public ReturnResult FindApproveCompletedPurchaseOrders(HttpServletRequest request,HttpServletResponse response);

    /**
     * 展示采购管理中采购订单模块数据
     * @param request
     * @param response
     */
    public ReturnResult FindPurchaseEquipmentOrderData(HttpServletRequest request,HttpServletResponse response);

    /**
     * 采购人员，补充采购设备信息
     * @param request
     * @param response
     */
    public ReturnResult FillPurchasedEquipmentInfo(HttpServletRequest request,HttpServletResponse response);

    /**
     * 创建采购合同
     * @param request
     * @param response
     */
    public ReturnResult GeneratePurchaseContract(HttpServletRequest request, HttpServletResponse response);

    /**
     * 查看合同管理模块数据
     * @param request
     * @param response
     */
    public ReturnResult FindPurchaseContractData(HttpServletRequest request,HttpServletResponse response);

    /**
     * 采购页面添加设备信息到台账是需要做验证（判断台账中是否已存在该设备信息）
     * @param request
     * @param response
     * @return
     */
    public ReturnResult VerifyLedgerData(HttpServletRequest request,HttpServletResponse response);

    /**
     * 采购页面选择采购信息生成发票记录保存到经费管理系统中
     * @param request
     * @param response
     * @return
     */
    public ReturnResult AddInvoiceRecord(HttpServletRequest request,HttpServletResponse response);

    /**
     * 提交设备验收申请
     * @param request
     * @param response
     * @return
     */
    public ReturnResult SubmitAcceptanceApplication(HttpServletRequest request,HttpServletResponse response);

    /**
     * 展示设备验收申请数据
     * @param request
     * @param response
     * @return
     */
    public ReturnResult FindVerifyApprovalData(HttpServletRequest request,HttpServletResponse response);

    /**
     * 获取需要审批的验收申请信息
     * @param request
     * @param response
     * @return
     */
    public ReturnResult FindAcceptanceApplicationData(HttpServletRequest request,HttpServletResponse response);

    /**
     * 审批设备验收申请信息
     * @param request
     * @param response
     * @return
     */
    public ReturnResult ActionAcceptanceApproval(HttpServletRequest request,HttpServletResponse response);

    /**
     * 验收申请审核失败
     * @param request
     * @param response
     * @return
     */
    public ReturnResult ActionAcceptanceApprovalFailed(HttpServletRequest request,HttpServletResponse response);

    /**
     * 修改采购申请信息（打回）
     * @param request
     * @param response
     * @return
     */
    public ReturnResult UpdatePurchaseRequisitionInfo(HttpServletRequest request,HttpServletResponse response);

    /**
     * 修改设备验收申请信息（打回）
     * @param request
     * @param response
     * @return
     */
    public ReturnResult UpdateAcceptanceApplicationInfo(HttpServletRequest request,HttpServletResponse response);
}
