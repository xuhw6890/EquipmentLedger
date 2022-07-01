package com.tianke.equipmentledger.context;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.*;


import java.io.*;
import java.lang.Boolean;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileIoUtil {

    public static WritableCellFormat getDataFont() {
        // 定义字体
        WritableFont font = new WritableFont(WritableFont.TAHOMA, 10);
        WritableCellFormat format = new WritableCellFormat(font);
        try {
            // 黑色字体
            font.setColour(Colour.BLACK);
            // 左右居中
            format.setAlignment(Alignment.CENTRE);
            // 上下居中
            format.setVerticalAlignment(VerticalAlignment.CENTRE);
            // 黑色边框
            format.setBorder(Border.ALL, BorderLineStyle.THIN,Colour.BLACK);
            // 黄色背景
            //format.setBackground(Colour.AQUA);

        } catch (WriteException e) {
            e.printStackTrace();
        }
        return format;
    }


    public static WritableCellFormat getHeader() {
        // 定义字体
        WritableFont font = new WritableFont(WritableFont.TIMES, 12,WritableFont.BOLD);
        WritableCellFormat format = new WritableCellFormat(font);
        try {
            // 黑色字体
            font.setColour(Colour.BLACK);
            // 左右居中
            format.setAlignment(Alignment.CENTRE);
            // 上下居中
            format.setVerticalAlignment(VerticalAlignment.CENTRE);
            // 黑色边框
            format.setBorder(Border.ALL, BorderLineStyle.THIN);
            // 背景颜色
            format.setBackground(Colour.BLACK);

        } catch (WriteException e) {
            e.printStackTrace();
        }
        return format;
    }


    public static Boolean writeEquipmentLedgerData(String FileName, List<Map<String,Object>> list) {
        String title = "序号,设备编号,设备名称,品牌,规格型号,仪器序列号,设备分类,主要用途,使用部门,购置日期,存放位置,维护要求,维保到期时间,检验周期,设备状态,固定资产编号";
        String[] titles = title.split(",");

        //创建一个文件夹和流
        Boolean bool = false;
        File file = new File(FileName);
        WritableWorkbook wwb = null;
        OutputStream os = null;
        List<String[]> listStr = new ArrayList<String[]>();
        int a = 1;
        for(int i=0;i<list.size();i++){
            String[] DataArray=new String[titles.length];
            DataArray[0]="";DataArray[0]=Integer.toString(a++);
            DataArray[1]="";if(list.get(i).get("device_id") != null){DataArray[1]=list.get(i).get("device_id").toString();};
            DataArray[2]="";if(list.get(i).get("device_name") != null) {DataArray[2]=list.get(i).get("device_name").toString();};
            DataArray[3]="";if(list.get(i).get("trademark") != null) {DataArray[3]=list.get(i).get("trademark").toString();};
            DataArray[4]="";if(list.get(i).get("device_model") != null) {DataArray[4]=list.get(i).get("device_model").toString();};
            DataArray[5]="";if(list.get(i).get("instrument_serial_number") != null) {DataArray[5]=list.get(i).get("instrument_serial_number").toString();};
            DataArray[6]="";if(list.get(i).get("classification") != null) {DataArray[6]=list.get(i).get("classification").toString();};
            DataArray[7]="";if(list.get(i).get("main_purpose") != null) {DataArray[7]=list.get(i).get("main_purpose").toString();};
            DataArray[8]="";if(list.get(i).get("use_department") != null) {DataArray[8]=list.get(i).get("use_department").toString();};
            DataArray[9]="";if(list.get(i).get("purchase_date") != null) {DataArray[9]=list.get(i).get("purchase_date").toString();};
            DataArray[10]="";if(list.get(i).get("storage_location") != null) {DataArray[10]=list.get(i).get("storage_location").toString();};
            DataArray[11]="";if(list.get(i).get("verify_requirements") != null) {DataArray[11]=list.get(i).get("verify_requirements").toString();};
            DataArray[12]="";if(list.get(i).get("maintenance_due_date") != null) {DataArray[12]=list.get(i).get("maintenance_due_date").toString();};
            DataArray[13]="";if(list.get(i).get("inspection_cycle") != null) {DataArray[13]=list.get(i).get("inspection_cycle").toString();};
            DataArray[14]="";if(list.get(i).get("device_situation") != null) {DataArray[14]=list.get(i).get("device_situation").toString();};
            DataArray[15]="";if(list.get(i).get("fixed_asset_number") != null) {DataArray[15]=list.get(i).get("fixed_asset_number").toString();};
            listStr.add(DataArray);
        }
        try {
            if(!file.exists()){
                file.createNewFile();
                bool = true;
            }

            if(bool){
                // 创建Excel工作薄
                // 新建立一个xls文件
                os = new FileOutputStream(FileName);
                wwb = Workbook.createWorkbook(os);
                Label label;
                WritableSheet sheets = wwb.createSheet("sheet1", 1);
                for (int i = 0; i < titles.length; i++) {
                    //添加字段名
                    // Label(x,y,z) 代表单元格的第x+1列，第y+1行, 内容z
                    // 在Label对象的子对象中指明单元格的位置和内容
                    //label = new Label(i, 0, title[i]);
                    label = new Label(i, 0, titles[i], getHeader());
                    //设置列宽
                    sheets.setColumnView(i, 30);
                    //sheets.setColumnView(4, 100);
                    // 将定义好的单元格添加到工作表中
                    sheets.addCell(label);
                }

                for(int i=0;i<listStr.size();i++){
                    //向特定单元格写入数据
                    for(int j=0;j<listStr.get(i).length;j++){
                        //sheets.setColumnView(j, 30);
                        label = new Label(j, 1+i, listStr.get(i)[j], getDataFont());
                        sheets.addCell(label);
                    }
                }
                // 写入数据
                wwb.write();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            // 关闭文件
            try {
                if(wwb != null){
                    wwb.close();
                }
                if(os != null){
                    os.close();
                }
            } catch (WriteException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;

    }

    //固定资产管理员下载数据
    public static Boolean writeEquipmentLedgerDataTwo(String FileName, List<Map<String,Object>> list) {
        String title = "序号,固定资产编号,设备编号,设备名称,品牌,规格型号,存放位置,使用部门,购置日期,原值,备注";
        String[] titles = title.split(",");

        //创建一个文件夹和流
        Boolean bool = false;
        File file = new File(FileName);
        WritableWorkbook wwb = null;
        OutputStream os = null;
        List<String[]> listStr = new ArrayList<String[]>();
        int a = 1;
        for(int i=0;i<list.size();i++){
            String[] DataArray=new String[titles.length];
            DataArray[0]="";DataArray[0]=Integer.toString(a++);
            DataArray[1]="";if(list.get(i).get("fixed_asset_number") != null){DataArray[1]=list.get(i).get("fixed_asset_number").toString();};
            DataArray[2]="";if(list.get(i).get("device_id") != null){DataArray[2]=list.get(i).get("device_id").toString();};
            DataArray[3]="";if(list.get(i).get("device_name") != null) {DataArray[3]=list.get(i).get("device_name").toString();};
            DataArray[4]="";if(list.get(i).get("trademark") != null) {DataArray[4]=list.get(i).get("trademark").toString();};
            DataArray[5]="";if(list.get(i).get("device_model") != null) {DataArray[5]=list.get(i).get("device_model").toString();};
            DataArray[6]="";if(list.get(i).get("storage_location") != null) {DataArray[6]=list.get(i).get("storage_location").toString();};
            DataArray[7]="";if(list.get(i).get("use_department") != null) {DataArray[7]=list.get(i).get("use_department").toString();};
            DataArray[8]="";if(list.get(i).get("purchase_date") != null) {DataArray[8]=list.get(i).get("purchase_date").toString();};
            DataArray[9]="";if(list.get(i).get("total_price") != null) {DataArray[9]=list.get(i).get("total_price").toString();};
            DataArray[10]="";if(list.get(i).get("remark") != null) {DataArray[10]=list.get(i).get("remark").toString();};
            listStr.add(DataArray);
        }
        try {
            if(!file.exists()){
                file.createNewFile();
                bool = true;
            }

            if(bool){
                // 创建Excel工作薄
                // 新建立一个xls文件
                os = new FileOutputStream(FileName);
                wwb = Workbook.createWorkbook(os);
                Label label;
                WritableSheet sheets = wwb.createSheet("sheet1", 1);
                for (int i = 0; i < titles.length; i++) {
                    //添加字段名
                    // Label(x,y,z) 代表单元格的第x+1列，第y+1行, 内容z
                    // 在Label对象的子对象中指明单元格的位置和内容
                    //label = new Label(i, 0, title[i]);
                    label = new Label(i, 0, titles[i], getHeader());
                    //设置列宽
                    sheets.setColumnView(i, 30);
                    //sheets.setColumnView(4, 100);
                    // 将定义好的单元格添加到工作表中
                    sheets.addCell(label);
                }

                for(int i=0;i<listStr.size();i++){
                    //向特定单元格写入数据
                    for(int j=0;j<listStr.get(i).length;j++){
                        //sheets.setColumnView(j, 30);
                        label = new Label(j, 1+i, listStr.get(i)[j], getDataFont());
                        sheets.addCell(label);
                    }
                }
                // 写入数据
                wwb.write();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            // 关闭文件
            try {
                if(wwb != null){
                    wwb.close();
                }
                if(os != null){
                    os.close();
                }
            } catch (WriteException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;

    }

    //删除文件
    public static boolean DeleteFile(String Path){
        Boolean bool = false;
        File file = new File(Path);
        try {
            if(file.exists()){
                file.delete();
                bool = true;
            }else{
                //System.out.println("文件不存在："+Path);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());

        }
        return bool;
    }

    /**
     * 复制文件到指定地点
     * @param oldpath
     * @param newpath
     * @return
     * @throws IOException
     */
    public static Boolean CopyFileToDir(String oldpath, String newpath) throws IOException {
        Boolean bool = false;
        File oldpaths = new File(oldpath);
        File newpaths = new File(newpath);
        if (!newpaths.exists()) {
            Files.copy(oldpaths.toPath(), newpaths.toPath());
            bool = true;
        } else {
            newpaths.delete();
            Files.copy(oldpaths.toPath(), newpaths.toPath());
            bool = true;
        }
        return bool;
    }

    /**
     * 将目录文件打包成压缩包
     * @param sourceFilePath
     * @param zipFilePath
     * @param fileName
     * @return
     */
    public static boolean fileToZip(String sourceFilePath,String zipFilePath,String fileName){
        boolean flag = false;
        File sourceFile = new File(sourceFilePath);
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        if(sourceFile.exists() == false){
            System.out.println("待压缩的文件目录："+sourceFilePath+"不存在.");
        }else{
            try {
                File zipFile = new File(zipFilePath + "/" + fileName);
                if(zipFile.exists()){
                    System.out.println(zipFilePath + "目录下存在名字为:" + fileName +"打包文件.");
                }else{
                    File[] sourceFiles = sourceFile.listFiles();
                    if(null == sourceFiles || sourceFiles.length<1){
                        System.out.println("待压缩的文件目录：" + sourceFilePath + "里面不存在文件，无需压缩.");
                    }else{
                        fos = new FileOutputStream(zipFile);
                        zos = new ZipOutputStream(new BufferedOutputStream(fos));
                        byte[] bufs = new byte[1024*10];
                        for(int i=0;i<sourceFiles.length;i++){
                            //创建ZIP实体，并添加进压缩包
                            ZipEntry zipEntry = new ZipEntry(sourceFiles[i].getName());
                            zos.putNextEntry(zipEntry);
                            //读取待压缩的文件并写进压缩包里
                            fis = new FileInputStream(sourceFiles[i]);
                            bis = new BufferedInputStream(fis, 1024*10);
                            int read = 0;
                            while((read=bis.read(bufs, 0, 1024*10)) != -1){
                                zos.write(bufs,0,read);
                            }
                        }
                        flag = true;
                    }
                }
            } catch (FileNotFoundException e) {
                System.out.println(e.getMessage());
                throw new RuntimeException(e);
            } catch (IOException e) {
                System.out.println(e.getMessage());
                throw new RuntimeException(e);
            } finally{
                //关闭流
                try {
                    if(null != bis) bis.close();
                    if(null != zos) zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }
        return flag;
    }

    /**
     * 删除目录及以下所有文件
     * @param file
     */
    public static void removeDir(File file){
        if(!file.exists()){//目录不存在
            return;
        }else{
            if(file.isFile()){//地址指向一个文件时，就删除这个文件
                file.delete();
            }else{//当地址指向一个文件夹或目录
                File[] fileArray = file.listFiles();
                for (int i = 0; i < fileArray.length; i++) {
                    //递归删除目录下的文件
                    removeDir(fileArray[i]);
                }
                //删除目录
                file.delete();
            }
        }
    }


}
