package com.tianke.equipmentledger.controller;

import com.tianke.equipmentledger.WordExport.WordOprateUtil;
import com.tianke.equipmentledger.context.*;
import com.tianke.equipmentledger.dao.*;
import com.tianke.equipmentledger.entity.DataTable;
import com.tianke.equipmentledger.entity.UserLoginInfo;
import net.sf.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/DonwLoad")
public class DonwLoadResultController {
    @Resource
    private AcquisitionManageDAO acquisitionManageDAO;
    @Resource
    private SerialNumberDAO serialNumberDAO;
    @Resource
    private EquipmentLedgerDAO equipmentLedgerDAO;
    @Resource
    private MaintainApprovalDAO maintainApprovalDAO;
    @Resource
    private ScrapApprovalDAO scrapApprovalDAO;
    @Resource
    private OutOfficeManageDAO outOfficeManageDAO;
    @Resource
    private CalibrationManageDAO calibrationManageDAO;
    @Resource
    private VerificationManageDAO verificationManageDAO;
    @Resource
    private UserInfoDAO userInfoDAO;
    @Resource
    private VerifyApprovalDAO verifyApprovalDAO;

    @Value("${sign.dir.address}")
    private String sign_dir_address;
    @Value("${upload.file.path}")
    private String upload_file_path;

    /**
     * 下载设备采购审批表
     * @param request
     * @param response
     */
    @GetMapping("/PurchaseApprovalForm")
    public void DonwLoadEquipmentPurchaseApprovalForm(HttpServletRequest request, HttpServletResponse response) {
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            try {
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter outThis = response.getWriter();
                outThis.write("<script>alert('对不起！请登陆！');history.back(-1);</script>");
                outThis.close();
                return;
            } catch (IOException e) {
            }
        }
        ApplicationHome h = new ApplicationHome(getClass());
        File jarF = h.getSource();
        String path = jarF.getParentFile().toString()+File.separator+"DonwLoad";
        String fileName = "Equipment_Approval_Form"+System.currentTimeMillis()+".doc";
        String fileNameAndPath = path+File.separator+fileName;
        String id = request.getParameter("id");
        Map<String,Object> mapData = acquisitionManageDAO.FindPurchaseRequisitionInfoById(id);
        int application_status = Integer.parseInt(mapData.get("application_status").toString());
        if(MessageType.FOUR != application_status){
            try {
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter outThis = response.getWriter();
                outThis.write("<script>alert('对不起！该申请暂未通过审批无法下载！');history.back(-1);</script>");
                outThis.close();
                return;
            } catch (IOException e) {
            }
        }
        //获取用户电子签名
        List<Map<String,String>> userList = userInfoDAO.FindUserSignatureInfo();
        Map<String,String> signMap = new HashMap<>();
        for(Map<String,String> map : userList){
            signMap.put(map.get("user_code"),sign_dir_address+map.get("user_signature"));
        }
        mapData.put("apply_department_leader",ToolUtil.getBase64(signMap.get(mapData.get("apply_department_leader_code"))));
        mapData.put("deputy_general_manager",ToolUtil.getBase64(signMap.get(mapData.get("deputy_general_manager_code"))));
        mapData.put("general_manager",ToolUtil.getBase64(signMap.get(mapData.get("general_manager_code"))));
        if(!MessageType.XIEGANG.equals(mapData.get("chairman_code"))){
            mapData.put("chairman",ToolUtil.getBase64(signMap.get(mapData.get("chairman_code"))));
        }

        /*
        //获取该下载采购审批的流水号
        Map<String,Object> map = new HashMap<>();
        map.put("approval_form_id",mapData.get("id"));
        map.put("table_status", MessageType.ZERO);
        String approval_form_id = serialNumberDAO.FindApprovalSerialNumber(map);
        mapData.put("approval_form_id",approval_form_id);
         */
        mapData.put("approval_form_id",MessageType.EMPTYSTR);

        String template_name = "purchase_approval_form01.ftl";
        if(MessageType.XIEGANG.equals(mapData.get("chairman_code"))){
            //设备金额不超过30万，不需要董事长签字
            template_name = "purchase_approval_form02.ftl";
        }

        //判断是否存在技术指标附件，存在需要同步下载
        String technical_indicators_address = MessageType.EMPTYSTR;
        if(null != mapData.get("technical_indicators_address") && !MessageType.EMPTYSTR.equals(mapData.get("technical_indicators_address"))){
            technical_indicators_address = mapData.get("technical_indicators_address").toString();
        }
        if(!MessageType.EMPTYSTR.equals(technical_indicators_address)){
            //打包成压缩文件,把技术指标概述文件复制一份到该文件夹内
            path = path+File.separator+"Equipment_Approval_Form_"+id;
            if(path.isEmpty()){

            }
            //首先将该附件复制一份到该下载目录中
            String inPath = upload_file_path+File.separator+"technical_indicators"+File.separator+id+".pdf";
            String outPath = path+File.separator+id+".pdf";
            File outFile = new File(outPath);
            if(!outFile.exists()){
                outFile.mkdir();
            }
            try {
                if(FileIoUtil.CopyFileToDir(inPath,outPath)){
                    if(WordOprateUtil.createWord(mapData,template_name,path,fileName)){
                        String zipName = "Equipment_Approval_Form.zip";
                        if(FileIoUtil.fileToZip(path, path, zipName)){
                            DownLoadFiles(response, zipName, path+File.separator+zipName, path,1);
                        }else{
                            try {
                                response.setContentType("text/html; charset=UTF-8");
                                PrintWriter outThis = response.getWriter();
                                outThis.write("<script>alert('对不起！没有找到相关数据！');history.back(-1);</script>");
                                outThis.close();
                                return;
                            } catch (IOException e) {
                            }
                        }

                    }else{
                        try {
                            response.setContentType("text/html; charset=UTF-8");
                            PrintWriter outThis = response.getWriter();
                            outThis.write("<script>alert('对不起！没有找到相关数据！');history.back(-1);</script>");
                            outThis.close();
                            return;
                        } catch (IOException e) {
                        }
                    }
                }else{
                    try {
                        response.setContentType("text/html; charset=UTF-8");
                        PrintWriter outThis = response.getWriter();
                        outThis.write("<script>alert('对不起！没有找到相关附件信息！');history.back(-1);</script>");
                        outThis.close();
                        return;
                    } catch (IOException e) {
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }

        }else{
            if(WordOprateUtil.createWord(mapData,template_name,path,fileName)){
                //下载
                DownLoadFilesFile(response,fileName,fileNameAndPath);
                try{
                    FileIoUtil.DeleteFile(fileNameAndPath);
                }catch (Exception e){
                    System.out.println(e.getMessage());
                }

            }else{
                try {
                    response.setContentType("text/html; charset=UTF-8");
                    PrintWriter outThis = response.getWriter();
                    outThis.write("<script>alert('对不起！没有找到相关数据！');history.back(-1);</script>");
                    outThis.close();
                    return;
                } catch (IOException e) {
                }
            }
        }


    }

    /**
     * 下载台账管理页面数据
     * @param request
     * @param response
     */
    @GetMapping("/EquipmentLedgerData")
    public void DonwLoadEquipmentLedgerData(HttpServletRequest request,HttpServletResponse response){
        //台账模块各部门查看各部门的数据，综合管理部可查看整个实验部的数据，固定资产管理员可查看所有数据
        String token = request.getParameter("token");
        String device_status = request.getParameter("device_status");
        String fixed_assets = request.getParameter("fixed_assets");
        JSONArray jsonarray = JSONArray.fromObject(request.getParameter("aoData"));
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            try {
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter outThis = response.getWriter();
                outThis.write("<script>alert('对不起！请登陆！');history.back(-1);</script>");
                outThis.close();
                return;
            } catch (IOException e) {
            }
        }

        DataTable dataTable = ToolUtil.GetDataTableValue(jsonarray);
        int ruleNumber = dataTable.getiSortCol();
        String key_name ="id";
        switch (ruleNumber) {
            case 3: key_name="signing_date";break;
            case 4: key_name="contract_amount";break;
            case 8: key_name="create_time";break;
        }
        dataTable.setSortTable(key_name);
        //获取用户信息
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        String department = userMap.get("department").toString();
        String dept_code = MessageType.EMPTYSTR;
        String asset_status = MessageType.STRONE;
        int fixed_asset_admin = Integer.parseInt(userMap.get("fixed_asset_admin").toString());
        if(MessageType.ONE == fixed_asset_admin){
            asset_status = MessageType.EMPTYSTR;
        }
        if("综合管理部".equals(department)){
            dept_code = "02";
        }
        Map<String,Object> map_data = new HashMap<>();
        map_data.put("dataTable",dataTable);
        map_data.put("device_status",device_status);
        map_data.put("fixed_assets",fixed_assets);
        map_data.put("use_department",department);
        map_data.put("dept_code",dept_code);
        map_data.put("asset_status",asset_status);

        List<Map<String,Object>> listMap = equipmentLedgerDAO.FindDonwLoadEquipmentLedgerData(map_data);
        if(listMap.size() > 0){
            ApplicationHome h = new ApplicationHome(getClass());
            File jarF = h.getSource();
            String path = jarF.getParentFile().toString()+File.separator+"DonwLoad";
            String fileName = "Equipment_Ledger_Data"+System.currentTimeMillis()+".xls";
            String fileNameAndPath = path+File.separator+fileName;
            if("06".equals(userMap.get("dept_code")) || "0201".equals(userMap.get("dept_code")) || "0202".equals(userMap.get("dept_code"))){
                //添加序号
                int id = MessageType.ONE;
                for(Map<String,Object> map : listMap){
                    map.put("id",id);
                    id++;
                }
                fileName = "Equipment_Ledger_Data"+System.currentTimeMillis()+".doc";
                fileNameAndPath = path+File.separator+fileName;
                Map<String,Object> mapData = new HashMap<>();
                mapData.put("list",listMap);
                if(WordOprateUtil.createWord(mapData,"equipment_ledger_info.ftl",path,fileName)){
                    //下载
                    DownLoadFilesFile(response,fileName,fileNameAndPath);
                    try{
                        FileIoUtil.DeleteFile(fileNameAndPath);
                    }catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                }else{
                    try {
                        response.setContentType("text/html; charset=UTF-8");
                        PrintWriter outThis = response.getWriter();
                        outThis.write("<script>alert('对不起！没有找到相关数据！');history.back(-1);</script>");
                        outThis.close();
                        return;
                    } catch (IOException e) {
                    }
                }

            }else{
                //其他部门及固定资产管理员下载
                if(FileIoUtil.writeEquipmentLedgerDataTwo(fileNameAndPath,listMap)){
                    DownLoadFilesFile(response,fileName,fileNameAndPath);
                    try{
                        FileIoUtil.DeleteFile(fileNameAndPath);
                    }catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                }else{
                    try {
                        response.setContentType("text/html; charset=UTF-8");
                        PrintWriter outThis = response.getWriter();
                        outThis.write("<script>alert('文件下载失败，请重试');history.back(-1);</script>");
                        outThis.close();
                        return;
                    } catch (IOException e) {
                    }
                }
            }


        }else{
            try {
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter outThis = response.getWriter();
                outThis.write("<script>alert('对不起！没有找到相关数据！');history.back(-1);</script>");
                outThis.close();
                return;
            } catch (IOException e) {
            }
        }
    }

    /**
     * 下载设备停用/报废审批申请表
     * @param request
     * @param response
     */
    @GetMapping("/ScrapApprovalForm")
    public void DonwLoadScrapApprovalForm(HttpServletRequest request,HttpServletResponse response){
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            try {
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter outThis = response.getWriter();
                outThis.write("<script>alert('对不起！请登陆！');history.back(-1);</script>");
                outThis.close();
                return;
            } catch (IOException e) {
            }
        }
        ApplicationHome h = new ApplicationHome(getClass());
        File jarF = h.getSource();
        String path = jarF.getParentFile().toString()+File.separator+"DonwLoad";
        String fileName = "Scrap_Approval_Form"+System.currentTimeMillis()+".doc";
        String fileNameAndPath = path+File.separator+fileName;
        String id = request.getParameter("id");
        Map<String,Object> mapData = scrapApprovalDAO.FindScrapApprovalInfoById(id);
        //首先判断该申请是否已完成审批
        int application_steps = Integer.parseInt(mapData.get("approval_steps").toString());
        if(MessageType.FOUR != application_steps){
            try {
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter outThis = response.getWriter();
                outThis.write("<script>alert('对不起！该申请未通过审批，暂不能下载该申请表！');history.back(-1);</script>");
                outThis.close();
                return;
            } catch (IOException e) {
            }
        }
        /*
        //获取该下载设备维修审批的流水号
        Map<String,Object> map = new HashMap<>();
        map.put("approval_form_id",mapData.get("id"));
        map.put("table_status", MessageType.ONE);
        String approval_form_id = serialNumberDAO.FindApprovalSerialNumber(map);
        mapData.put("approval_form_id",approval_form_id);
         */
        mapData.put("approval_form_id",MessageType.EMPTYSTR);
        //依据用户获取其电子签名
        List<Map<String,String>> userList = userInfoDAO.FindUserSignatureInfo();
        Map<String,String> signMap = new HashMap<>();
        for(Map<String,String> map : userList){
            signMap.put(map.get("user_code"),sign_dir_address+map.get("user_signature"));
        }
        mapData.put("use_department_leader",ToolUtil.getBase64(signMap.get(mapData.get("use_department_leader_code"))));
        mapData.put("deputy_general_manager",ToolUtil.getBase64(signMap.get(mapData.get("deputy_general_manager_code"))));
        mapData.put("general_manager",ToolUtil.getBase64(signMap.get(mapData.get("general_manager_code"))));
        mapData.put("financial_affairs",ToolUtil.getBase64(signMap.get(mapData.get("financial_affairs_code"))));

        if(WordOprateUtil.createWord(mapData,"scrap_approval_form.ftl",path,fileName)){
            //下载
            DownLoadFilesFile(response,fileName,fileNameAndPath);
            try{
                FileIoUtil.DeleteFile(fileNameAndPath);
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }else{
            try {
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter outThis = response.getWriter();
                outThis.write("<script>alert('对不起！没有找到相关数据！');history.back(-1);</script>");
                outThis.close();
                return;
            } catch (IOException e) {
            }
        }
    }

    /**
     * 下载设备维修审批申请表
     * @param request
     * @param response
     */
    @GetMapping("/MaintainApprovalForm")
    public void DonwLoadMaintainApprovalForm(HttpServletRequest request,HttpServletResponse response){
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            try {
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter outThis = response.getWriter();
                outThis.write("<script>alert('对不起！请登陆！');history.back(-1);</script>");
                outThis.close();
                return;
            } catch (IOException e) {
            }
        }
        ApplicationHome h = new ApplicationHome(getClass());
        File jarF = h.getSource();
        String path = jarF.getParentFile().toString()+File.separator+"DonwLoad";
        String fileName = "Maintain_Approval_Form"+System.currentTimeMillis()+".doc";
        String fileNameAndPath = path+File.separator+fileName;
        String id = request.getParameter("id");
        Map<String,Object> mapData = maintainApprovalDAO.FindMaintainApprovalInfoById(id);
        //首先判断该申请是否已完成审批
        int application_steps = Integer.parseInt(mapData.get("application_steps").toString());
        if(MessageType.FOUR != application_steps){
            try {
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter outThis = response.getWriter();
                outThis.write("<script>alert('对不起！该申请未通过审批，暂不能下载该申请表！');history.back(-1);</script>");
                outThis.close();
                return;
            } catch (IOException e) {
            }
        }
        //获取该下载设备维修审批的流水号
        /*
        Map<String,Object> map = new HashMap<>();
        map.put("approval_form_id",mapData.get("id"));
        map.put("table_status", MessageType.TWO);
        String approval_form_id = serialNumberDAO.FindApprovalSerialNumber(map);
         */
        mapData.put("approval_form_id",MessageType.EMPTYSTR);
        //依据用户获取其电子签名
        List<Map<String,String>> userList = userInfoDAO.FindUserSignatureInfo();
        Map<String,String> signMap = new HashMap<>();
        for(Map<String,String> map : userList){
            signMap.put(map.get("user_code"),sign_dir_address+map.get("user_signature"));
        }
        mapData.put("create_user_name",ToolUtil.getBase64(signMap.get(mapData.get("create_user_code"))));
        mapData.put("general_management_leader",ToolUtil.getBase64(signMap.get(mapData.get("general_management_leader_code"))));
        mapData.put("deputy_general_manager",ToolUtil.getBase64(signMap.get(mapData.get("deputy_general_manager_code"))));
        mapData.put("general_manager",ToolUtil.getBase64(signMap.get(mapData.get("general_manager_code"))));
        //处理一下申请时间
        mapData.put("create_time",mapData.get("create_time").toString().substring(0,mapData.get("create_time").toString().indexOf(" ")));
        if(WordOprateUtil.createWord(mapData,"maintain_approval_form.ftl",path,fileName)){
            //下载
            DownLoadFilesFile(response,fileName,fileNameAndPath);
            try{
                FileIoUtil.DeleteFile(fileNameAndPath);
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }else{
            try {
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter outThis = response.getWriter();
                outThis.write("<script>alert('对不起！没有找到相关数据！');history.back(-1);</script>");
                outThis.close();
                return;
            } catch (IOException e) {
            }
        }
    }

    /**
     * 下载外出审批表信息
     */
    @GetMapping("/GoOutApprovalForm")
    public void DonwLoadGoOutApprovalForm(HttpServletRequest request,HttpServletResponse response){
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            try {
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter outThis = response.getWriter();
                outThis.write("<script>alert('对不起！请登陆！');history.back(-1);</script>");
                outThis.close();
                return;
            } catch (IOException e) {
            }
        }
        ApplicationHome h = new ApplicationHome(getClass());
        File jarF = h.getSource();
        String path = jarF.getParentFile().toString()+File.separator+"DonwLoad";
        String fileName = "Go_Out_Approval_Form"+System.currentTimeMillis()+".doc";
        String fileNameAndPath = path+File.separator+fileName;
        String id = request.getParameter("id");
        Map<String,Object> mapData = outOfficeManageDAO.FindGooutApprovalInfoById(id);
        //判断该申请表是否完成
        int device_status = Integer.parseInt(mapData.get("device_status").toString());
        if(MessageType.TWO != device_status){
            try {
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter outThis = response.getWriter();
                outThis.write("<script>alert('对不起！该申请未完成，暂不能下载该申请表！');history.back(-1);</script>");
                outThis.close();
                return;
            } catch (IOException e) {
            }
        }
        //添加序号
        mapData.put("serial_id",MessageType.ONE);
        //依据用户获取其电子签名
        List<Map<String,String>> userList = userInfoDAO.FindUserSignatureInfo();
        Map<String,String> signMap = new HashMap<>();
        for(Map<String,String> map : userList){
            signMap.put(map.get("user_code"),sign_dir_address+map.get("user_signature"));
        }
        mapData.put("use_department_leader",ToolUtil.getBase64(signMap.get(mapData.get("use_department_leader_code"))));
        mapData.put("general_management_leader",ToolUtil.getBase64(signMap.get(mapData.get("general_management_leader_code"))));
        mapData.put("technical_director",ToolUtil.getBase64(signMap.get(mapData.get("technical_director_code"))));
        mapData.put("laboratory_director",ToolUtil.getBase64(signMap.get(mapData.get("laboratory_director_code"))));
        if(WordOprateUtil.createWord(mapData,"go_out_approval_form.ftl",path,fileName)){
            //下载
            DownLoadFilesFile(response,fileName,fileNameAndPath);
            try{
                FileIoUtil.DeleteFile(fileNameAndPath);
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }else{
            try {
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter outThis = response.getWriter();
                outThis.write("<script>alert('对不起！没有找到相关数据！');history.back(-1);</script>");
                outThis.close();
                return;
            } catch (IOException e) {
            }
        }
    }

    /**
     * 下载校准计划数据或下载校准/核查跟踪记录
     * @param request
     * @param response
     */
    @GetMapping("/CalibrationApprovalForm")
    public void DonwLoadCalibrationApprovalForm(HttpServletRequest request,HttpServletResponse response){
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            try {
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter outThis = response.getWriter();
                outThis.write("<script>alert('对不起！请登陆！');history.back(-1);</script>");
                outThis.close();
                return;
            } catch (IOException e) {
            }
        }
        String data_type = request.getParameter("data_type");//下载数据类型，0为校准，1为跟踪核查
        JSONArray jsonarray = JSONArray.fromObject(request.getParameter("aoData"));
        ApplicationHome h = new ApplicationHome(getClass());
        File jarF = h.getSource();
        String path = jarF.getParentFile().toString()+File.separator+"DonwLoad";
        String fileName = "Calibration_Approval_Form"+System.currentTimeMillis()+".doc";
        if(MessageType.STRONE.equals(data_type)){
            fileName = "Check_Track_Record"+System.currentTimeMillis()+".doc";
        }
        String fileNameAndPath = path+File.separator+fileName;

        DataTable dataTable = ToolUtil.GetDataTableValue(jsonarray);
        int ruleNumber = dataTable.getiSortCol();
        String key_name ="id";
        switch (ruleNumber) {
            case 1: key_name="id";break;
        }
        dataTable.setSortTable(key_name);
        List<Map<String,Object>> listMap = calibrationManageDAO.FindDonwLoadCalibrationPlanData(dataTable);
        //添加序号
        int serial_id = MessageType.ONE;
        for(Map<String,Object> map : listMap){
            map.put("serial_id",serial_id);
            serial_id++;
        }
        Map<String,Object> mapData = new HashMap<>();
        mapData.put("listMap",listMap);
        String templateName = "calibration_approval_form.ftl";
        if(MessageType.STRONE.equals(data_type)){
            templateName = "check_track_record.ftl";
        }
        if(WordOprateUtil.createWord(mapData,templateName,path,fileName)){
            //下载
            DownLoadFilesFile(response,fileName,fileNameAndPath);
            try{
                FileIoUtil.DeleteFile(fileNameAndPath);
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }else{
            try {
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter outThis = response.getWriter();
                outThis.write("<script>alert('对不起！没有找到相关数据！');history.back(-1);</script>");
                outThis.close();
                return;
            } catch (IOException e) {
            }
        }

    }

    /**
     * 下载期间核查数据
     * @param request
     * @param response
     */
    @GetMapping("/VerificationApprovalForm")
    public void DonwLoadVerificationApprovalForm(HttpServletRequest request,HttpServletResponse response){
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            try {
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter outThis = response.getWriter();
                outThis.write("<script>alert('对不起！请登陆！');history.back(-1);</script>");
                outThis.close();
                return;
            } catch (IOException e) {
            }
        }
        ApplicationHome h = new ApplicationHome(getClass());
        File jarF = h.getSource();
        String path = jarF.getParentFile().toString()+File.separator+"DonwLoad";
        String fileName = "Verification_Approval_Form"+System.currentTimeMillis()+".doc";
        String fileNameAndPath = path+File.separator+fileName;
        String finished_status = request.getParameter("finished_status");//计划进行状态,0查看未完成的，1查看已完成的
        JSONArray jsonarray = JSONArray.fromObject(request.getParameter("aoData"));
        DataTable dataTable = ToolUtil.GetDataTableValue(jsonarray);
        int ruleNumber = dataTable.getiSortCol();
        String key_name ="id";
        switch (ruleNumber) {
            case 1: key_name="id";break;
        }
        dataTable.setSortTable(key_name);
        Map<String,Object> map_data = new HashMap<>();
        map_data.put("finished_status",finished_status);
        map_data.put("dataTable",dataTable);
        List<Map<String,Object>> listMap = verificationManageDAO.FindDonwLoadVerificationData(map_data);
        //添加序号
        int serial_id = MessageType.ONE;
        for(Map<String,Object> map : listMap){
            map.put("serial_id",serial_id);
            serial_id++;
        }
        Map<String,Object> mapData = new HashMap<>();
        mapData.put("listMap",listMap);
        if(WordOprateUtil.createWord(mapData,"verification_approval_form.ftl",path,fileName)){
            //下载
            DownLoadFilesFile(response,fileName,fileNameAndPath);
            try{
                FileIoUtil.DeleteFile(fileNameAndPath);
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }else{
            try {
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter outThis = response.getWriter();
                outThis.write("<script>alert('对不起！没有找到相关数据！');history.back(-1);</script>");
                outThis.close();
                return;
            } catch (IOException e) {
            }
        }
    }


    /**
     * 下载设备验收审批表
     * @param request
     * @param response
     */
    @GetMapping("/EquipmentAcceptanceForm")
    public void DonwLoadEquipmentAcceptanceForm(HttpServletRequest request,HttpServletResponse response){
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            try {
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter outThis = response.getWriter();
                outThis.write("<script>alert('对不起！请登陆！');history.back(-1);</script>");
                outThis.close();
                return;
            } catch (IOException e) {
            }
        }
        ApplicationHome h = new ApplicationHome(getClass());
        File jarF = h.getSource();
        String path = jarF.getParentFile().toString()+File.separator+"DonwLoad";
        String fileName = "Acceptance_Approval_Form"+System.currentTimeMillis()+".doc";
        String fileNameAndPath = path+File.separator+fileName;
        String id = request.getParameter("id");
        Map<String,Object> mapData = verifyApprovalDAO.FindFindVerifyApprovalById(id);
        //首先判断该申请是否已完成审批
        int application_status = Integer.parseInt(mapData.get("application_status").toString());
        if(MessageType.FIVE != application_status){
            try {
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter outThis = response.getWriter();
                outThis.write("<script>alert('对不起！该申请未通过审批，暂不能下载该申请表！');history.back(-1);</script>");
                outThis.close();
                return;
            } catch (IOException e) {
            }
        }
        //获取该下载设备维修审批的流水号
        /*
        Map<String,Object> map = new HashMap<>();
        map.put("approval_form_id",mapData.get("id"));
        map.put("table_status", MessageType.TWO);
        String approval_form_id = serialNumberDAO.FindApprovalSerialNumber(map);
         */
        mapData.put("approval_form_id",MessageType.EMPTYSTR);
        //依据用户获取其电子签名
        List<Map<String,String>> userList = userInfoDAO.FindUserSignatureInfo();
        Map<String,String> signMap = new HashMap<>();
        for(Map<String,String> map : userList){
            signMap.put(map.get("user_code"),sign_dir_address+map.get("user_signature"));
        }
        mapData.put("use_department_leader",ToolUtil.getBase64(signMap.get(mapData.get("use_department_leader_code"))));
        mapData.put("device_admin",ToolUtil.getBase64(signMap.get(mapData.get("device_admin_code"))));
        mapData.put("deputy_general_manager",ToolUtil.getBase64(signMap.get(mapData.get("deputy_general_manager_code"))));
        mapData.put("general_manager",ToolUtil.getBase64(signMap.get(mapData.get("general_manager_code"))));
        String asset_admin = mapData.get("asset_admin").toString();//资产管理员
        //判断资产管理员是否为/，是/则该设备金额不大于2000故不需要资产管理员签字
        String template_name = "acceptance_approval_form_01.ftl";
        if(MessageType.XIEGANG.equals(asset_admin)){
            //设备金额不大于2000，资产管理员不需要签字
            template_name = "acceptance_approval_form_02.ftl";
        }else{
            mapData.put("asset_admin",ToolUtil.getBase64(signMap.get(mapData.get("asset_admin_code"))));
        }
        if(WordOprateUtil.createWord(mapData,template_name,path,fileName)){
            //下载
            DownLoadFilesFile(response,fileName,fileNameAndPath);
            try{
                FileIoUtil.DeleteFile(fileNameAndPath);
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
        }else{
            try {
                response.setContentType("text/html; charset=UTF-8");
                PrintWriter outThis = response.getWriter();
                outThis.write("<script>alert('对不起！没有找到相关数据！');history.back(-1);</script>");
                outThis.close();
                return;
            } catch (IOException e) {
            }
        }
    }


    private void DownLoadFilesFile(HttpServletResponse response, String fileName, String fileNameAndPath) {
        //准备下载
        //1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
        response.setContentType("multipart/form-data");
        //2.设置文件头：最后一个参数是设置下载文件名
        //response.setContentType("application/OCTET-STREAM;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment;fileName="+fileName);
        ServletOutputStream out;
//		        //通过文件路径获得File对象
        File file = new File(fileNameAndPath);
        try {
            FileInputStream inputStream = new FileInputStream(file);
            //3.通过response获取ServletOutputStream对象(out)
            out = response.getOutputStream();
            int b = 0;
            byte[] buffer = new byte[1024];
            while ((b =inputStream.read(buffer)) != -1){
                //4.写到输出流(out)中
                out.write(buffer,0,b);
            }
            out.flush();
            out.close();
            inputStream.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * 下载
     * @author autumnal leaves
     * @version 1.0
     * @date 2018年6月21日
     * @param response
     * @param fileName
     * @param fileNameAndPath
     * path:路径
     * @功能描述：
     */
    private void DownLoadFiles(HttpServletResponse response, String fileName, String fileNameAndPath,String path,int deleNumber) {
        //准备下载
        //1.设置文件ContentType类型，这样设置，会自动判断下载文件类型
        response.setContentType("multipart/form-data");
        //2.设置文件头：最后一个参数是设置下载文件名
        //response.setContentType("application/OCTET-STREAM;charset=UTF-8");
        try {
            response.setHeader("Content-Disposition", "attachment;fileName="+ URLEncoder.encode(fileName, "UTF-8"));
        } catch (Exception e) {
            response.setHeader("Content-Disposition", "attachment;fileName="+fileName);
        }
        ServletOutputStream out;
//		        //通过文件路径获得File对象
        File file = new File(fileNameAndPath);
        try {
            FileInputStream inputStream = new FileInputStream(file);
            //3.通过response获取ServletOutputStream对象(out)
            out = response.getOutputStream();
            int b = 0;
            byte[] buffer = new byte[1024];
            while ((b =inputStream.read(buffer)) != -1){
                //4.写到输出流(out)中
                out.write(buffer,0,b);
            }
            out.flush();
            out.close();
            inputStream.close();
            if(deleNumber == 1){
                FileIoUtil.DeleteFile(fileNameAndPath);
            }else{
                File fileDir = new File(path);
                FileIoUtil.removeDir(fileDir);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //外部导入设备台账数据
    @GetMapping("/SaveLedgerData")
    public ReturnResult SaveLedgerData(HttpServletRequest request){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo.getUser_code()){
            return ReturnResult.NotExist();
        }
        File file111 = new File("C:\\Users\\Administrator\\Desktop\\yzjyy\\old_ledger_data.xlsx");
        ArrayList<ArrayList<Object>> new_list = ToolUtil.readHighThroughputExcel(file111, 0);
        List<Map<String, Object>> listMap = new ArrayList<>();
        if(new_list.size()>0){
            for(int i=0;i<new_list.size();i++){
                Map<String, Object> map = new HashMap<>();
                map.put("associate_id", 0);
                map.put("device_id", new_list.get(i).get(2).toString());
                map.put("device_name", new_list.get(i).get(3).toString());
                map.put("trademark", new_list.get(i).get(4).toString());
                map.put("device_model", new_list.get(i).get(5).toString());
                map.put("instrument_serial_number", new_list.get(i).get(6).toString());
                map.put("classification", new_list.get(i).get(7).toString());
                map.put("purchase_date", MessageType.EMPTYSTR);
                map.put("maintenance_due_date", MessageType.EMPTYSTR);
                map.put("device_situation",new_list.get(i).get(8).toString());
                map.put("storage_location",new_list.get(i).get(9).toString());
                map.put("use_department",new_list.get(i).get(10).toString());
                map.put("main_purpose",new_list.get(i).get(11).toString());
                map.put("verify_requirements",new_list.get(i).get(12).toString());
                map.put("inspection_cycle",MessageType.EMPTYSTR);
                int device_status = 0;
                if("停用".equals(new_list.get(i).get(8).toString())){
                    device_status = 1;
                }
                if("报废".equals(new_list.get(i).get(8).toString())){
                    device_status = 2;
                }
                if("脱离实验室".equals(new_list.get(i).get(8).toString())){
                    device_status = 3;
                }
                map.put("device_status",device_status);
                map.put("user_name","赵红霞");
                map.put("user_code","TK02004");
                map.put("create_time","2022-05-17");
                listMap.add(map);
            }
        }
        if(listMap.size() > 0){
            equipmentLedgerDAO.ImportEquipmentLedgerInfo(listMap);
            result.setObject("导入成功");
        }else{
            result.setObject("没有数据");
        }

        return result;
    }



}
