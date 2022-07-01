package com.tianke.equipmentledger.context;


import com.tianke.equipmentledger.entity.DataTable;
import com.tianke.equipmentledger.entity.UserLoginInfo;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class ToolUtil {
    /*
     * 盐值
     */
    private static String salt="dsgfdsgsdgsdgsdg++=======32424^$$%#$&%#$&$U*%$&*&&";

    private static DecimalFormat df = new DecimalFormat("0");
    // 默认单元格格式化日期字符串
    private static SimpleDateFormat sdf = new SimpleDateFormat(  "yyyy-MM-dd");
    // 格式化数字
    private static DecimalFormat nf = new DecimalFormat("0");// #.##  保留两位小数

    private static NumberFormat Nmf = NumberFormat.getInstance();


    /**
     * 通过加研加密后的密码
     * @author autumnal leaves
     * @version 1.0
     * @date 2022年3月15日
     * @param pwd
     * @return
     * @功能描述：
     */
    public static String crypt(String pwd){
        return DigestUtils.md5Hex(pwd+salt);
    }

    /**
     * 获取当前时间
     * @return
     */
    public static String GetNowDateString(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(new Date());
    }

    public static String GetNowDateYMDString(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(new Date());
    }

    public static String GetNowDateYearString(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy");
        return formatter.format(new Date());
    }

    /**
     * 生成token
     * @param user_account
     * @return
     */
    public static String GenerateToken(String user_account){
        String uuid = UUID.randomUUID().toString();
        uuid = uuid.replace("-", "");
        return DigestUtils.md5Hex(user_account+uuid).toUpperCase();
    }

    /**
     * 存入session值
     * @author autumnal leaves
     * @version 1.0
     * @date 2022年3月15日
     * @功能描述：
     */
    public static void setUserToken(String token, UserLoginInfo userLoginInfo){
        if(LoginSessionCheck.tokenSession.get(token) != null){
            //如果当前账号已经登录了，需要将之前seesion废除
            LoginSessionCheck.removeToken(token);
        }
        //废除之后重新添加
        LoginSessionCheck.AddToken(token,userLoginInfo);
    }

    /**
     * 获取用户信息
     * @param token
     * @return
     */
    public static UserLoginInfo getUserTokenInfo(String token){
        UserLoginInfo userLoginInfo = new UserLoginInfo();
        if(LoginSessionCheck.tokenSession.get(token) != null){
            userLoginInfo = LoginSessionCheck.tokenSession.get(token);
        }
        return userLoginInfo;
    }

    /**
     * 将数字转换为想要的字符串
     * @author autumnal leaves
     * @version 1.0
     * @date 2022年3月17日
     * @param number 数字
     * @param total 想得到几位数的字符串
     * @return
     * @功能描述：
     */
    public static String GetStringNumberForValue(int number,int total){
        String TempStr = String.format("%0"+total+"d",number);
        return TempStr;

    }

    /**
     * 获取dataTable 传到后台的参数
     *
     */
    public static DataTable GetDataTableValue(JSONArray jsonarray){
        String sSortDir_0="asc";
        String searchValue="";
        String sEcho = null;
        int iSortCol_0 = 0;
        int iDisplayStart = 0; // 起始索引
        int iDisplayLength = 0; // 每页显示的行数
        for (int i = 0; i < jsonarray.size(); i++) {
            JSONObject obj = (JSONObject) jsonarray.get(i);
            if(obj.get("name").equals("sEcho"))
                sEcho = obj.get("value").toString();
            if(obj.get("name").equals("iDisplayStart"))
                iDisplayStart = obj.getInt("value");
            if (obj.get("name").equals("iDisplayLength"))
                iDisplayLength = obj.getInt("value");
            if(obj.get("name").equals("search_value"))
                searchValue = obj.get("value").toString();
            if(obj.get("name").equals("sSortDir_0"))
                sSortDir_0 = obj.get("value").toString();
            if(obj.get("name").equals("iSortCol_0"))
                iSortCol_0 = obj.getInt("value");
        }
        DataTable dataTable = new DataTable();
        dataTable.setStart(iDisplayStart);
        dataTable.setEnd(iDisplayLength);
        dataTable.setsEcho(sEcho);
        dataTable.setiSortCol(iSortCol_0);
        dataTable.setSearchValue(searchValue.trim());
        dataTable.setSortRule(sSortDir_0);
        return dataTable;

    }

    /**
     * 获取之前多少天的日期
     * @param specifiedDay
     * @return
     */
    public static String getSpecifiedDayBefore(String specifiedDay,int day_number){
        Calendar c = Calendar.getInstance();
        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd").parse(specifiedDay);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        c.setTime(date);
        int day = c.get(Calendar.DATE);
        c.set(Calendar.DATE, day - day_number);
        String dayBefore = new SimpleDateFormat("yyyy-MM-dd").format(c.getTime());
        return dayBefore;
    }

    /**
     * 比较两个日期大小
     * @param DATE1
     * @param DATE2
     * @return
     */
    public static boolean compare_date(String DATE1, String DATE2) {
        boolean bool = false;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date1 = df.parse(DATE1);
            Date date2 = df.parse(DATE2);
            if (date1.getTime() >= date2.getTime()) {
                bool = true;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return bool;
    }

    public static String[] NumericalSorting(String[] numberStrArr){
        String temp = "";
        for(int i = 0;i<numberStrArr.length-1;i++){
            for(int j = 0;j<numberStrArr.length-1-i;j++){
                if(Integer.parseInt(numberStrArr[j+1])<Integer.parseInt(numberStrArr[j])){
                    temp = numberStrArr[j];
                    numberStrArr[j] = numberStrArr[j+1];
                    numberStrArr[j+1] = temp;
                }
            }
        }
        return numberStrArr;
    }

    public static ArrayList<ArrayList<Object>> readHighThroughputExcel(File file, int number){
        //System.out.println("file:"+file);
        if(file == null){
            return null;
        }
        if(file.getName().endsWith("xlsx")){
            //处理ecxel2007
            System.out.println("2007");
            return readHighThroughputExcel2007(file,number);
        }else{
            //处理ecxel2003
            System.out.println("2003");
            return readHighThroughputExcel2003(file,number);
        }
    }

    @SuppressWarnings("deprecation")
    private static ArrayList<ArrayList<Object>> readHighThroughputExcel2007(File file,int number) {
        try{
            ArrayList<ArrayList<Object>> rowList = new ArrayList<ArrayList<Object>>();
            ArrayList<Object> colList;
            XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(file));
            //属水平
            XSSFSheet sheet = wb.getSheetAt(number);
            //种水平中文名
            //XSSFSheet sheet = wb.getSheetAt(1);
            XSSFRow row;
            XSSFCell cell;
            Object value;
            for(int i = sheet.getFirstRowNum() , rowCount = 0; rowCount < sheet.getPhysicalNumberOfRows() ; i++ ){
                row = sheet.getRow(i);
                colList = new ArrayList<Object>();
                if(row == null){
                    //当读取行为空时
                    if(i != sheet.getPhysicalNumberOfRows()){//判断是否是最后一行
                        rowList.add(colList);
                    }
                    continue;
                }else{
                    rowCount++;
                }
                // for( int j = row.getFirstCellNum() ; j <= row.getLastCellNum() ;j++){
                for( int j = row.getFirstCellNum() ; j < 17 ;j++){
                    cell = row.getCell(j);
/*		                  if(null != cell){
	                	  System.out.println(cell.toString());
	                  }*/
                    if(cell == null || cell.getCellType() == HSSFCell.CELL_TYPE_BLANK){
                        //当该单元格为空
//	                        if(j != row.getLastCellNum()){//判断是否是该行中最后一个单元格
                        if(j != 9){//判断是否是该行中最后一个单元格
                            colList.add("");
                        }
                        continue;
                        //break;
                    }
                    switch(cell.getCellType()){
                        case XSSFCell.CELL_TYPE_STRING:
                            //System.out.println(i + "行" + j + " 列 is String type");
                            value = cell.getStringCellValue();
                            //System.out.println(i + "行" + j + " 列 is String type:"+value);
                            break;
                        case XSSFCell.CELL_TYPE_NUMERIC:
                            if ("@".equals(cell.getCellStyle().getDataFormatString())) {
                                value = df.format(cell.getNumericCellValue());
                            } else if ("General".equals(cell.getCellStyle()
                                    .getDataFormatString())) {
                                value = nf.format(cell.getNumericCellValue());
                            } else {
                                value = sdf.format(HSSFDateUtil.getJavaDate(cell
                                        .getNumericCellValue()));
                            }
                            //System.out.println(i + "行" + j+ " 列 is Number type ; DateFormt:"+ value.toString());
                            break;
                        case XSSFCell.CELL_TYPE_BOOLEAN:
                            //System.out.println(i + "行" + j + " 列 is Boolean type");
                            value = Boolean.valueOf(cell.getBooleanCellValue());
                            break;
                        case XSSFCell.CELL_TYPE_BLANK:
                            //System.out.println(i + "行" + j + " 列 is Blank type");
                            value = "";
                            break;
                        default:
                            //System.out.println(i + "行" + j + " 列 is default type");
                            value = cell.toString();

                    }// end switch
                    colList.add(value);
                }//end for j
                rowList.add(colList);
            }//end for i

            return rowList;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }


    /*
     * @return 将返回结果存储在ArrayList内，存储结构与二位数组类似
     * lists.get(0).get(0)表示过去Excel中0行0列单元格
     */
    public static ArrayList<ArrayList<Object>> readHighThroughputExcel2003(File file,int number){
        try{
            ArrayList<ArrayList<Object>> rowList = new ArrayList<ArrayList<Object>>();
            ArrayList<Object> colList;
            HSSFWorkbook wb = new HSSFWorkbook(new FileInputStream(file));
            HSSFSheet sheet = wb.getSheetAt(number);
            HSSFRow row;
            HSSFCell cell;
            Object value;
            for(int i = sheet.getFirstRowNum() , rowCount = 0; rowCount < sheet.getPhysicalNumberOfRows() ; i++ ){
                row = sheet.getRow(i);
                colList = new ArrayList<Object>();
                if(row == null){
                    //当读取行为空时
                    if(i != sheet.getPhysicalNumberOfRows()){//判断是否是最后一行
                        rowList.add(colList);
                    }
                    continue;
                }else{
                    rowCount++;
                }
                for( int j = row.getFirstCellNum() ; j < 17 ;j++){
                    cell = row.getCell(j);
                    if(cell == null || cell.getCellType() == HSSFCell.CELL_TYPE_BLANK){
                        //当该单元格为空
//                        if(j != row.getLastCellNum()){//判断是否是该行中最后一个单元格
                        if(j != 9){//判断是否是该行中最后一个单元格
                            colList.add("");
                        }
                        continue;
                    }
                    switch(cell.getCellType()){
                        case XSSFCell.CELL_TYPE_STRING:
                            //System.out.println(i + "行" + j + " 列 is String type");
                            value = cell.getStringCellValue();
                            // System.out.println(i + "行" + j + " 列 is StringValue:"+value);
                            break;
                        case XSSFCell.CELL_TYPE_NUMERIC:
                            if ("@".equals(cell.getCellStyle().getDataFormatString())) {
                                value = df.format(cell.getNumericCellValue());
                            } else if ("General".equals(cell.getCellStyle()
                                    .getDataFormatString())) {
                                value = Nmf.format(cell.getNumericCellValue());

                            } else {
                                value = sdf.format(HSSFDateUtil.getJavaDate(cell
                                        .getNumericCellValue()));
                            }
                            // System.out.println(i + "行" + j
                            //        + " 列 is Number type ; DateFormt:"
                            //       + value.toString());
                            break;
                        case XSSFCell.CELL_TYPE_BOOLEAN:
                            //System.out.println(i + "行" + j + " 列 is Boolean type");
                            value = Boolean.valueOf(cell.getBooleanCellValue());
                            break;
                        case XSSFCell.CELL_TYPE_BLANK:
                            // System.out.println(i + "行" + j + " 列 is Blank type");
                            value = "";
                            break;
                        case HSSFCell.CELL_TYPE_FORMULA:
                            value = cell.getCellFormula();
                        default:
                            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
                            value = evaluator.evaluate(cell).getNumberValue();
                            //value = String.valueOf(cell.getRichStringCellValue());
                            value = nf.format(value);
                            // System.out.println(i + "行" + j + " 列 is default type"+"value:"+value);

                    }// end switch
                    colList.add(value);
                }//end for j
                rowList.add(colList);
            }//end for i

            return rowList;
        }catch(Exception e){
            e.getStackTrace();
            return null;
        }
    }

    /**
     * 根据图片路径将图片转成base64编码
     * @param imageUrl
     * @return
     */
    public static String getBase64(String imageUrl) {
        InputStream in = null;
        final ByteArrayOutputStream data = new ByteArrayOutputStream();
        //读取图片字节数组
        try {
            URL url = new URL(imageUrl);
            final byte[] by = new byte[1024];
            // 创建链接获取图片
            final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            in = conn.getInputStream();
            int len = -1;
            while ((len = in.read(by)) != -1) {
                data.write(by, 0, len);
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //对字节数组Base64编码
        BASE64Encoder encoder = new BASE64Encoder();
        //返回Base64编码过的字节数组字符串
        String encode = encoder.encode(data.toByteArray());
        /*encode = encode.replaceAll("[\\s*\t\n\r]", "");*/
        return encode;
    }


}
