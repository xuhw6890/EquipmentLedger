package com.tianke.equipmentledger.WordExport;

import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.*;
import java.util.Map;

public class WordOprateUtil {
    public  static boolean  createWord(Map<String,Object> dataMap, String templateName, String filePath, String fileName){
        boolean flag =false;
        try{
            //创建配置实例
            @SuppressWarnings("deprecation")
            Configuration configuration = new Configuration();

            //设置编码
            configuration.setDefaultEncoding("UTF-8");
            configuration.setClassForTemplateLoading(WordOprateUtil.class, "/com/tianke/equipmentledger/WordExport");
            //获取模板
            Template template = configuration.getTemplate(templateName);
            //输出文件
            File outFile = new File(filePath+File.separator+fileName);
            //如果输出目标文件夹不存在，则创建
            if (!outFile.getParentFile().exists()){
                outFile.getParentFile().mkdirs();
            }
            //将模板和数据模型合并生成文件
            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile),"UTF-8"));
            //生成文件
            template.process(dataMap, out);

            //关闭流
            out.flush();
            out.close();
            flag=true;
        }catch(Exception e){
            // e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return flag;

    }

}
