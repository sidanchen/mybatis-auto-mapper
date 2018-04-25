package com.csd.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 该工具类用于获取全类名
 * Created by sdc on 2018/4/20.
 */
public class ClassNameUtil {

    public static String allClassName = "";
    public static String className = "";
    public static Field[] fields;
    public static String tableName = "";
    public static String instanceName;

    public static void init(String allClassName1) throws ClassNotFoundException {
        //设置全类名
        allClassName = allClassName1;
        //截取类名
        className =  allClassName.substring(allClassName.lastIndexOf(".") + 1, allClassName.length());
        //设置属性
        fields = Class.forName(allClassName).getDeclaredFields();
        tableName = propertyToColumns(className);
        instanceName = classNameConvert(className);
    }


    /**
     * 把包名解析成路径
     * @param packageName
     * @return
     */
    public static List<String> getClassName(String packageName) {

        List<String> classNameList = new ArrayList<>();
        String filePath = ClassLoader.getSystemResource("").getPath()
                + packageName.replace(".", "//");
        return getClassName(filePath, classNameList);
    }


    /**
     * 根据传入的path搜索该路径下面的所有类
     * @param path 路径
     * @param classNameList 用于储存全类名的集合
     * @return
     */
    public static List<String> getClassName(String path,
                                            List<String> classNameList) {
        List<String> myClassName = new ArrayList<String>();
        File file = new File(path);
        File[] childFile = file.listFiles();
        for (File f : childFile) {
            if (f.isDirectory()) {
                myClassName.addAll(getClassName(f.getPath(), myClassName));
            } else {
                String childFilePath = f.getPath();
                childFilePath = childFilePath.substring(
                        childFilePath.indexOf("\\classes") + 9,
                        childFilePath.lastIndexOf("."));
                childFilePath = childFilePath.replace("\\", ".");
                myClassName.add(childFilePath);
            }
        }
        return myClassName;
    }

    /**
     * 将类名转换为普通实体命名
     * @param className
     * @return
     */
    public static String classNameConvert(String className){
        StringBuffer sb = new StringBuffer();
        char[] cs = className.toCharArray();
        if(cs.length > 0){
            cs[0] += 32;
        }
        for(char c:cs){
            sb.append(c);
        }
        return sb.toString();
    }


    /**
     * 将实体属性转换为数据库的列名(该方法也支持全pojo名转换为表名)
     * @return
     */
    public static String propertyToColumns(String propertyName){
        if(propertyName == null || propertyName.equals("")){
            return "";
        }
        //按照大写字母添加空格作为分割符
        String repStr = propertyName.replaceAll("[A-Z]", " $0");//正则替换注意“ $0”前面有个空格
        String[] spliStr = repStr.split(" ");
        for(int i = 0;i < spliStr.length;i++){
            spliStr[i] = spliStr[i].toLowerCase();
        }
        StringBuffer stringBuffer = new StringBuffer();
        for(String s:spliStr){
            stringBuffer.append(s + " ");
        }
        repStr = stringBuffer.toString().trim();
        return repStr.replaceAll(" ","_");
    }


}
