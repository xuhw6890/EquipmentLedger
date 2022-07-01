package com.tianke.equipmentledger.context;

import java.io.Serializable;

public class ReturnResult implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final Integer AJAX_STATUS_CODE_SUCCESS = 0;
    public static final Integer AJAX_STATUS_CODE_WARN = 1;
    public static final Integer AJAX_STATUS_CODE_ERROR = 2;
    public static final Integer AJAX_STATUS_CODE_FAIL = 3;
    public static final Integer AJAX_STATUS_CODE_NotExist = 4;
    public static final String[] COLOR = {"#DC143C","#DB7093","#FF69B4","#FF1493","#C71585","#DA70D6","#D8BFD8","#DDA0DD","#EE82EE","#FF00FF","#FF00FF","#8B008B","#800080","#BA55D3","#9400D3","#9932CC","#4B0082","#8A2BE2","#9370DB","#7B68EE","#6A5ACD","#483D8B","#FFB6C1","#FFC0CB","#0000FF","#0000CD","#191970","#00008B","#000080","#4169E1","#6495ED","#B0C4DE","#778899","#708090","#1E90FF","#4682B4","#87CEFA","#87CEEB","#00BFFF","#ADD8E6","#B0E0E6","#5F9EA0","#AFEEEE","#00FFFF","#00FFFF","#00CED1","#2F4F4F","#008B8B","#008080","#48D1CC","#20B2AA","#40E0D0","#7FFFAA","#00FA9A","#00FF7F","#3CB371","#2E8B57","#90EE90","#98FB98","#8FBC8F","#32CD32","#00FF00","#228B22","#008000","#006400","#7FFF00","#7CFC00","#ADFF2F","#556B2F","#F5F5DC","#FAFAD2","#FFFFF0","#FFFFE0","#FFFF00","#808000","#BDB76B","#FFFACD","#EEE8AA","#F0E68C","#FFD700","#FFF8DC","#DAA520","#FFFAF0","#FDF5E6","#F5DEB3","#FFE4B5","#FFA500","#FFEFD5","#FFEBCD","#FFDEAD","#FAEBD7","#D2B48C","#DEB887","#FFE4C4","#FF8C00","#FAF0E6","#CD853F","#FFDAB9","#F4A460","#D2691E","#8B4513","#FFF5EE","#A0522D","#FFA07A","#FF7F50","#FF4500","#E9967A","#FF6347","#FFE4E1","#F08080","#BC8F8F","#CD5C5C","#FF0000","#A52A2A","#B22222","#8B0000"};

    private Integer statusCode;
    private String message;
    private Object object;
    private Integer otherStatus;
    public ReturnResult(){
        super();
    }
    public static ReturnResult success(){
        ReturnResult ajaxResult = new ReturnResult();
        ajaxResult.setStatusCode(AJAX_STATUS_CODE_SUCCESS);
        ajaxResult.setMessage("操作成功！");
        return ajaxResult;
    }
    public static ReturnResult error(){
        ReturnResult ajaxResult = new ReturnResult();
        ajaxResult.setStatusCode(AJAX_STATUS_CODE_ERROR);
        ajaxResult.setMessage("操作失败！");
        return ajaxResult;
    }
    public static ReturnResult warn(){
        ReturnResult ajaxResult = new ReturnResult();
        ajaxResult.setStatusCode(AJAX_STATUS_CODE_WARN);
        ajaxResult.setMessage("操作异常！请联系维护人员检查！");
        return ajaxResult;
    }
    public static ReturnResult Fail(){
        ReturnResult ajaxResult = new ReturnResult();
        ajaxResult.setStatusCode(AJAX_STATUS_CODE_FAIL);
        ajaxResult.setMessage("对不起！没有找到对应的文件夹！");
        return ajaxResult;
    }
    public static ReturnResult Late(){
        ReturnResult ajaxResult = new ReturnResult();
        ajaxResult.setStatusCode(AJAX_STATUS_CODE_ERROR);
        ajaxResult.setMessage("对不起！您来晚了！请返回重试！");
        return ajaxResult;
    }

    public static ReturnResult NotExist(){
        ReturnResult ajaxResult = new ReturnResult();
        ajaxResult.setStatusCode(AJAX_STATUS_CODE_NotExist);
        ajaxResult.setMessage("对不起！请重新登陆");
        return ajaxResult;
    }

    public ReturnResult(Integer statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
    public Integer getStatusCode() {
        return statusCode;
    }
    public Object getObject() {
        return object;
    }
    public void setObject(Object object) {
        this.object = object;
    }
    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }


    public String getMessage() {
        return message;
    }


    public Integer getOtherStatus() {
        return otherStatus;
    }
    public void setOtherStatus(Integer otherStatus) {
        this.otherStatus = otherStatus;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ReturnResult{" +
                "statusCode=" + statusCode +
                ", message='" + message + '\'' +
                ", object=" + object +
                ", otherStatus=" + otherStatus +
                '}';
    }
}
