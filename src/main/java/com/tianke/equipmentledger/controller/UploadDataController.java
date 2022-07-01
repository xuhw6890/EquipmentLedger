package com.tianke.equipmentledger.controller;

import com.sun.org.apache.bcel.internal.generic.RET;
import com.tianke.equipmentledger.context.MessageType;
import com.tianke.equipmentledger.context.ResponseUtils;
import com.tianke.equipmentledger.context.ReturnResult;
import com.tianke.equipmentledger.context.ToolUtil;
import com.tianke.equipmentledger.dao.*;
import com.tianke.equipmentledger.entity.UserLoginInfo;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/Upload")
public class UploadDataController {
    @Resource
    private ContractPurchaseDAO contractPurchaseDAO;
    @Resource
    private MaintainApprovalDAO maintainApprovalDAO;
    @Resource
    private UserInfoDAO userInfoDAO;
    @Resource
    private OperationRecordDAO operationRecordDAO;
    @Resource
    private SupplierManageDAO supplierManageDAO;
    @Resource
    private AcquisitionManageDAO acquisitionManageDAO;
    @Resource
    private EquipmentLedgerDAO equipmentLedgerDAO;

    @Value("${upload.file.path}")
    private String upload_file_path;
    @Value("${file.access.path}")
    private String file_access_path;

    /**
     * 合同管理模块，上传电子版合同附件
     * @param file PDF文件
     * @param request
     * @param response
     */
    @PostMapping ("/SubmitContractPurchaseFile")
    public ReturnResult SubmitContractPurchaseFile(@RequestParam("files") MultipartFile file, HttpServletRequest request, HttpServletResponse response){
        ReturnResult result = ReturnResult.error();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo){
            return ReturnResult.NotExist();
        }
        //判断是否拥有采购权限
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        int procurement_staff = Integer.parseInt(userMap.get("procurement_staff").toString());
        if(MessageType.ONE != procurement_staff){
            result.setMessage("操作失败，您没有该权限");
            return result;
        }
        String id = request.getParameter("id");
        Map<String,Object> mapData = contractPurchaseDAO.FindContractPurchaseInfoById(id);
        String contract_code = mapData.get("contract_code").toString();
        //首先需要将文件上传到服务器中
        String originalFilename = contract_code+".pdf";//样本图片名称设定为：合同编号
        String attachment_address = MessageType.EMPTYSTR;
        if(!file.isEmpty()) {
            //File targetImg = new File("C:\\Users\\Administrator\\Desktop\\yzjyy\\pdf\\hetong");
            File targetImg = new File(upload_file_path+"/contract");
            // 判断文件夹是否存在
            if(!targetImg.exists()) {
                targetImg.mkdirs();    //级联创建文件夹
            }
            try {
                // 开始保存图片
                FileOutputStream outputStream = new FileOutputStream(upload_file_path+"/contract" +File.separator+originalFilename);
                outputStream.write(file.getBytes());
                outputStream.flush();
                outputStream.close();
                //图片上传成功需要将该图片地址保存到数据库中
                attachment_address = file_access_path+"/contract"+File.separator+originalFilename;
                //attachment_address = "C:\\Users\\Administrator\\Desktop\\yzjyy\\pdf\\hetong"+File.separator+originalFilename;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //将上传附件的路径进行保存
        if(!MessageType.EMPTYSTR.equals(attachment_address)){
            Map<String,Object> map_data = new HashMap<>();
            map_data.put("id",id);
            map_data.put("attachment_address",attachment_address);
            contractPurchaseDAO.UpdateContractAttachmentAddress(map_data);
            //添加用户操作记录
            Map<String,Object> operationMap = new HashMap<>();
            operationMap.put("id2",MessageType.EMPTYSTR);
            operationMap.put("raw_data",MessageType.EMPTYSTR);
            operationMap.put("operation_record","提交了采购合同编号为："+contract_code+"的电子版采购合同附件");
            operationMap.put("user_code",userInfo.getUser_code());
            operationMap.put("user_name",userInfo.getUser_name());
            operationMap.put("create_time",ToolUtil.GetNowDateString());
            operationRecordDAO.SaveUserOperationRecord(operationMap);
            return ReturnResult.success();
        }
        return result;
    }

    /**
     * 上传设备维修报价单
     * @param file PDF文件
     * @param request
     * @param response
     */
    @PostMapping ("/SubmitRepairQuotationFile")
    public ReturnResult SubmitRepairQuotationFile(@RequestParam("files") MultipartFile file, HttpServletRequest request, HttpServletResponse response){
        ReturnResult result = ReturnResult.error();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo){
            return ReturnResult.NotExist();
        }
        //判断一下是否为设备管理员
        Map<String,Object> info = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        int device_admin = Integer.parseInt(info.get("device_admin").toString());
        if(MessageType.ONE != device_admin){
            result.setMessage("操作失败，您没有该权限，请联系设备管理员进行操作");
            return result;
        }
        String id = request.getParameter("id");
        Map<String,Object> maintainMap = maintainApprovalDAO.FindMaintainApprovalInfoById(id);
        //判断一下该审批未完成，已完成审批则不能再上传或更改维修报价单
        int application_steps = Integer.parseInt(maintainMap.get("application_steps").toString());
        if(MessageType.FOUR == application_steps){
            result.setMessage("操作失败，该申请已完成审批，不能再更改其数据");
            return result;
        }
        String device_id = maintainMap.get("device_id").toString();
        //首先需要将文件上传到服务器中
        String originalFilename = device_id+"_"+id+".pdf";//样本图片名称设定为：设备编号+id
        String repair_quotation_address = MessageType.EMPTYSTR;
        if(!file.isEmpty()) {
            File targetImg = new File(upload_file_path+"/quotation");
            //File targetImg = new File("/public/Users/hongy/chloroplasti/apache-tomcat-8.0.39/webapps/RepairQuotationProject/");
            // 判断文件夹是否存在
            if(!targetImg.exists()) {
                targetImg.mkdirs();    //级联创建文件夹
            }
            try {
                // 开始保存图片
                FileOutputStream outputStream = new FileOutputStream(upload_file_path+"/quotation" +File.separator+originalFilename);
                outputStream.write(file.getBytes());
                outputStream.flush();
                outputStream.close();
                //图片上传成功需要将该图片地址保存到数据库中
                repair_quotation_address = file_access_path+"/quotation"+File.separator+originalFilename;
                //repair_quotation_address = "C:\\Users\\Administrator\\Desktop\\yzjyy\\pdf\\baojiadan"+File.separator+originalFilename;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //将上传附件的路径进行保存
        if(!MessageType.EMPTYSTR.equals(repair_quotation_address)){
            Map<String,Object> map_data = new HashMap<>();
            map_data.put("id",id);
            map_data.put("repair_quotation_address",repair_quotation_address);
            map_data.put("back_status",MessageType.ZERO);
            maintainApprovalDAO.UpdateRepairQuotationAddress(map_data);
            //添加用户操作记录
            Map<String,Object> operationMap = new HashMap<>();
            operationMap.put("id2",MessageType.EMPTYSTR);
            operationMap.put("raw_data",MessageType.EMPTYSTR);
            operationMap.put("operation_record","提交了维修申请id为："+id+"的维修报价单");
            operationMap.put("user_code",userInfo.getUser_code());
            operationMap.put("user_name",userInfo.getUser_name());
            operationMap.put("create_time",ToolUtil.GetNowDateString());
            operationRecordDAO.SaveUserOperationRecord(operationMap);
            return ReturnResult.success();
        }
        return result;


    }


    /**
     * 上传供应商资质文件
     * @param file PDF文件
     * @param request
     * @param response
     */
    @PostMapping("/SubmitSupplierQualificationFile")
    public ReturnResult SubmitSupplierQualificationDocuments(@RequestParam("files") MultipartFile file, HttpServletRequest request, HttpServletResponse response){
        ReturnResult result = ReturnResult.error();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo){
            return ReturnResult.NotExist();
        }
        //判断一下是否为设备管理员
        Map<String,Object> info = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        int device_admin = Integer.parseInt(info.get("device_admin").toString());
        if(MessageType.ONE != device_admin){
            result.setMessage("操作失败，您没有该权限，请联系设备管理员进行操作");
            return result;
        }
        String id = request.getParameter("id");
        String company_name = request.getParameter("company_name");
        //首先需要将文件上传到服务器中
        String originalFilename = id+".pdf";//图片名称设定为：id
        String qualification_documents_address = MessageType.EMPTYSTR;
        if(!file.isEmpty()) {
            File targetImg = new File(upload_file_path+"/qualification_documents");
            //File targetImg = new File("/public/Users/hongy/chloroplasti/apache-tomcat-8.0.39/webapps/RepairQuotationProject/");
            // 判断文件夹是否存在
            if(!targetImg.exists()) {
                targetImg.mkdirs();    //级联创建文件夹
            }
            try {
                // 开始保存图片
                FileOutputStream outputStream = new FileOutputStream(upload_file_path+"/qualification_documents" +File.separator+originalFilename);
                outputStream.write(file.getBytes());
                outputStream.flush();
                outputStream.close();
                //图片上传成功需要将该图片地址保存到数据库中
                qualification_documents_address = file_access_path+"/qualification_documents"+File.separator+originalFilename;
                //qualification_documents_address = "C:\\Users\\Administrator\\Desktop\\yzjyy\\pdf\\zizhiwenjian"+File.separator+originalFilename;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //将上传附件的路径进行保存
        if(!MessageType.EMPTYSTR.equals(qualification_documents_address)){
            Map<String,Object> map_data = new HashMap<>();
            map_data.put("id",id);
            map_data.put("qualification_documents_path",qualification_documents_address);
            supplierManageDAO.SaveQualificationDocumentsPath(map_data);
            //添加用户操作记录
            Map<String,Object> operationMap = new HashMap<>();
            operationMap.put("id2",MessageType.EMPTYSTR);
            operationMap.put("raw_data",MessageType.EMPTYSTR);
            operationMap.put("operation_record","提交了供应商："+company_name+"的资质文件");
            operationMap.put("user_code",userInfo.getUser_code());
            operationMap.put("user_name",userInfo.getUser_name());
            operationMap.put("create_time",ToolUtil.GetNowDateString());
            operationRecordDAO.SaveUserOperationRecord(operationMap);
            return ReturnResult.success();
        }
        return result;

    }


    /**
     * 上传采购设备技术指标概述文件
     * @param file PDF文件
     * @param request
     * @param response
     */
    @PostMapping("/SubmitEquipmentTechnicalIndicators")
    public ReturnResult SubmitEquipmentTechnicalIndicators(@RequestParam("files") MultipartFile file, HttpServletRequest request, HttpServletResponse response){
        ReturnResult result = ReturnResult.error();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo){
            return ReturnResult.NotExist();
        }
        String id = request.getParameter("id");
        //需要判断该申请是否还未进行审批
        Map<String,Object> purchaseMap = acquisitionManageDAO.FindPurchaseRequisitionInfoById(id);
        int application_status = Integer.parseInt(purchaseMap.get("application_status").toString());
        if(MessageType.ONE != application_status){
            result.setMessage("操作失败，该申请已开始审批流程，请勿再修改原始申请信息");
            return result;
        }
        //首先需要将文件上传到服务器中
        String originalFilename = id+".pdf";//图片名称设定为：id
        String technical_indicators_address = MessageType.EMPTYSTR;
        if(!file.isEmpty()) {
            File targetImg = new File(upload_file_path+"/technical_indicators");
            //File targetImg = new File("/public/Users/hongy/chloroplasti/apache-tomcat-8.0.39/webapps/RepairQuotationProject/");
            // 判断文件夹是否存在
            if(!targetImg.exists()) {
                targetImg.mkdirs();    //级联创建文件夹
            }
            try {
                // 开始保存图片
                FileOutputStream outputStream = new FileOutputStream(upload_file_path+"/technical_indicators" +File.separator+originalFilename);
                outputStream.write(file.getBytes());
                outputStream.flush();
                outputStream.close();
                //图片上传成功需要将该图片地址保存到数据库中
                technical_indicators_address = file_access_path+"/technical_indicators"+File.separator+originalFilename;
                //qualification_documents_address = "C:\\Users\\Administrator\\Desktop\\yzjyy\\pdf\\zizhiwenjian"+File.separator+originalFilename;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //将上传附件的路径进行保存
        if(!MessageType.EMPTYSTR.equals(technical_indicators_address)){
            Map<String,Object> map_data = new HashMap<>();
            map_data.put("id",id);
            map_data.put("technical_indicators_address",technical_indicators_address);
            map_data.put("back_status",MessageType.ZERO);
            map_data.put("update_time",ToolUtil.GetNowDateString());
            acquisitionManageDAO.SaveTechnicalIndicatorsAddress(map_data);

            //添加用户操作记录
            Map<String,Object> operationMap = new HashMap<>();
            operationMap.put("id2",MessageType.EMPTYSTR);
            operationMap.put("raw_data",JSONObject.fromObject(purchaseMap).toString());
            operationMap.put("operation_record","上传了id为："+id+"的设备技术指标概述文件");
            operationMap.put("user_code",userInfo.getUser_code());
            operationMap.put("user_name",userInfo.getUser_name());
            operationMap.put("create_time",ToolUtil.GetNowDateString());
            operationRecordDAO.SaveUserOperationRecord(operationMap);
            return ReturnResult.success();
        }
        return result;

    }

    /**
     * 本地导入设备台账历史数据
     */
    @GetMapping("/ImportHistoricalLedgerData")
    public ReturnResult ImportHistoricalLedgerData(HttpServletRequest request,HttpServletResponse response){
        ReturnResult result = ReturnResult.success();
        File file = new File("");
        ArrayList<ArrayList<Object>> new_list = ToolUtil.readHighThroughputExcel(file, 0);
        List<Map<String, Object>> newsList = new ArrayList<Map<String, Object>>();
        //获取部门对应部门编号
        Map<String,Object> dept_name_map = new HashMap<>();
        dept_name_map.put("总经办","01");
        dept_name_map.put("综合管理部","02");
        dept_name_map.put("微生物检测室","02");
        dept_name_map.put("分子检测室","02");
        dept_name_map.put("信息部","03");
        dept_name_map.put("运营部","04");
        dept_name_map.put("营销部","05");
        dept_name_map.put("咨询部","07");
        dept_name_map.put("物管部","08");
        dept_name_map.put("财务部","09");
        if(new_list.size() > 0){
            for(int i=1;i<new_list.size();i++){
                Map<String, Object> mapData = new HashMap<>();
                mapData.put("associate_id", MessageType.ZERO);
                mapData.put("device_id", new_list.get(i).get(1));
                mapData.put("device_name", new_list.get(i).get(2));
                mapData.put("trademark", new_list.get(i).get(3));
                mapData.put("device_model", new_list.get(i).get(4));
                mapData.put("instrument_serial_number", new_list.get(i).get(5));
                mapData.put("classification", new_list.get(i).get(6));
                mapData.put("main_purpose", new_list.get(i).get(7));
                mapData.put("use_department", new_list.get(i).get(8));
                //根据使用部门获取使用部门编号
                mapData.put("dept_code", dept_name_map.get(new_list.get(i).get(8)));
                mapData.put("purchase_date", new_list.get(i).get(9));
                mapData.put("storage_location", new_list.get(i).get(10));
                mapData.put("verify_requirements", new_list.get(i).get(11));
                mapData.put("maintenance_due_date",new_list.get(i).get(12));
                mapData.put("inspection_cycle",new_list.get(i).get(13));
                mapData.put("device_situation",new_list.get(i).get(14));
                mapData.put("fixed_asset_number",new_list.get(i).get(15));
                mapData.put("result_impact",new_list.get(i).get(16));
                mapData.put("total_price",new_list.get(i).get(17));
                mapData.put("remark",new_list.get(i).get(18));
                int device_status = MessageType.ZERO;
                if("停用".equals(new_list.get(i).get(14))){
                    device_status = MessageType.ONE;
                }
                if("报废".equals(new_list.get(i).get(14))){
                    device_status = MessageType.TWO;
                }
                if("脱离实验室".equals(new_list.get(i).get(14))){
                    device_status = MessageType.THREE;
                }
                mapData.put("device_status",device_status);
                mapData.put("user_name","");
                mapData.put("user_code","");
                mapData.put("create_time","");
                newsList.add(mapData);
            }
            equipmentLedgerDAO.ImportEquipmentLedgerInfo(newsList);

        }
        return result;

    }

}
