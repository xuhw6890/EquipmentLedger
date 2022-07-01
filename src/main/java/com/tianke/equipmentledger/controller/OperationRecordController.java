package com.tianke.equipmentledger.controller;

import com.tianke.equipmentledger.context.MessageType;
import com.tianke.equipmentledger.context.ResponseUtils;
import com.tianke.equipmentledger.context.ReturnResult;
import com.tianke.equipmentledger.context.ToolUtil;
import com.tianke.equipmentledger.dao.OperationRecordDAO;
import com.tianke.equipmentledger.dao.UserInfoDAO;
import com.tianke.equipmentledger.entity.DataTable;
import com.tianke.equipmentledger.entity.UserLoginInfo;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/log")
public class OperationRecordController {
    @Resource
    private UserInfoDAO userInfoDAO;
    @Resource
    private OperationRecordDAO operationRecordDAO;

    @GetMapping("/FindOperationRecordData")
    public ReturnResult FindUserOperationRecordData(HttpServletRequest request){
        ReturnResult result = ReturnResult.success();
        String token = request.getParameter("token");
        UserLoginInfo userInfo = ToolUtil.getUserTokenInfo(token);
        if(null == userInfo){
            result = ReturnResult.NotExist();
            return result;
        }
        //管理员能看到所有人的，用户只能看到自己的
        Map<String,Object> userMap = userInfoDAO.FindUserAppKeyByUserCode(userInfo.getUser_code());
        String user_code = userInfo.getUser_code();
        if(MessageType.ONE == Integer.parseInt(userMap.get("user_rights").toString())){
            user_code = MessageType.EMPTYSTR;
        }
        JSONArray jsonarray = JSONArray.fromObject(request.getParameter("aoData"));
        DataTable dataTable = ToolUtil.GetDataTableValue(jsonarray);
        int ruleNumber = dataTable.getiSortCol();
        String key_name ="id";
        switch (ruleNumber) {
            case 1: key_name="id";break;
        }
        dataTable.setSortTable(key_name);
        Map<String,Object> mapData = new HashMap<>();
        mapData.put("dataTable",dataTable);
        mapData.put("user_code",user_code);
        List<Map<String,Object>> listMap = operationRecordDAO.FindUserOperationRecord(mapData);
        int total = operationRecordDAO.FindUserOperationRecordTotal(mapData);
        JSONObject getObj = new JSONObject();
        getObj.put("sEcho", dataTable.getsEcho());//
        getObj.put("iTotalRecords", total);//实际的行数
        getObj.put("iTotalDisplayRecords", total);//显示的行数,这个要和上面写的一样
        getObj.put("aaData", listMap);//要以JSON格式返回
        result.setObject(getObj.toString());

        return result;
    }
}
