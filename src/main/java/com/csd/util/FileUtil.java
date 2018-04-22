package com.csd.util;

import java.io.File;
import java.io.IOException;

/**
 * 文件操作
 * Created by sdc on 2018/4/20.
 */
public class FileUtil {
    private static String CURRENT_PATH ="";

    static{
        CURRENT_PATH = getProjectPath();
    }

    /**
     * 创建文件
     * @param filePath 文件路径+文件名称
     * @return
     * @throws IOException
     */
    public File createFile(String filePath) throws IOException {
        File file = new File(filePath);
        if(!file.exists()){
            file.createNewFile();
        }
        return file;
    }
    public File getXmlName(String xmlName) throws IOException {
        String xml = getProjectPath() + "/" + xmlName;
        return createFile(xml);
    }
    /**
     * 获取当前jar包所在路径
     * @return
     */
    public static String getProjectPath() {
        java.net.URL url = Run.class.getProtectionDomain().getCodeSource()
                .getLocation();
        String filePath = null;
        try {
            filePath = java.net.URLDecoder.decode(url.getPath(), "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (filePath.endsWith(".jar"))
            filePath = filePath.substring(0, filePath.lastIndexOf("/") + 1);
        java.io.File file = new java.io.File(filePath);
        filePath = file.getAbsolutePath();
        return filePath;
    }
}