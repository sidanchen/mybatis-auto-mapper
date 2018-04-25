package com.csd.util;


import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.lang.reflect.Field;

/**
 * xml工具类 该工具类用于生成xml文件
 * Created by sdc on 2018/4/20.c
 */
public class XmlUtil {

    private static Document document;
    private Element root;


    /**
     * 初始化document
     */
    public static void initDocument() {
        document = DocumentHelper.createDocument();
    }


    /**
     * 将创建的document写入到文件中
     *
     * @param file
     * @throws IOException
     */
    public static void write(File file) throws IOException {
        System.out.println(file);
        XMLWriter xmlWriter = new XMLWriter(new FileOutputStream(file, false));
        xmlWriter.write(document);
        xmlWriter.close();
    }


    /**
     * 创建mapper文件
     *
     * @param allClassName 全类名
     */
    public void createMapper(String allClassName) throws IOException, ClassNotFoundException {
        initDocument();
        ClassNameUtil.init(allClassName);
        root = document.addElement("mapper");
        //设置namespace
        root.addAttribute("namespace", allClassName + "Dao");

        //生成ResultMap
        resultMap();
        //生成insert
        insert();
        //生成delete
        delete();
        //生成update
        update();
        //生成findByPage
        findByPage();
        //生成count
        count();
        String mapperName = ClassNameUtil.className + "Mapper.xml";
        write(new FileUtil().getXmlName(mapperName));
    }

    private void resultMap() {
        Element resultMap = root.addElement("resultMap");
        //设置resultMap的id
        resultMap.addAttribute("id", "BaseRM");
        //设置resultMap的type
        resultMap.addAttribute("type", ClassNameUtil.allClassName);
        int i = 0;
        //设置列名与属性名映射
        for (Field field : ClassNameUtil.fields) {
            String fieldsName = field.getName();
            String columnName = ClassNameUtil.propertyToColumns(fieldsName);
            if (i == 0) {
                //设置id
                Element id = resultMap.addElement("id");
                //设置column
                id.addAttribute("column", columnName);
                //设置property
                id.addAttribute("property", fieldsName);
                i++;
                continue;
            }
            //添加result节点
            Element result = resultMap.addElement("result");
            //设置column
            result.addAttribute("column", columnName);
            //设置property
            result.addAttribute("property", fieldsName);
        }
    }

    private void insert() {
        //创建insert节点
        Element insert = root.addElement("insert");

        //添加id
        insert.addAttribute("id", "insert");
        //添加参数类别
        insert.addAttribute("parameterType", ClassNameUtil.allClassName);

        String sql = "";
        sql = "insert into " + ClassNameUtil.tableName + "(";
        //拼接列名
        for (int i = 0; i < ClassNameUtil.fields.length; i++) {
            if (i == ClassNameUtil.fields.length - 1) {
                sql += ClassNameUtil.propertyToColumns(ClassNameUtil.fields[i].getName());
                continue;
            }
            sql += ClassNameUtil.propertyToColumns(ClassNameUtil.fields[i].getName()) + ",";
        }

        sql += ") values(";
        //拼接属性名
        for (int i = 0; i < ClassNameUtil.fields.length; i++) {
            if (i == ClassNameUtil.fields.length - 1) {
                sql += "#{" + ClassNameUtil.fields[i].getName() + "}";
                continue;
            }
            sql += "#{" + ClassNameUtil.fields[i].getName() + "}" + ",";
        }
        sql += ");";
        insert.setText(sql);
    }


    private void delete() {
        Element delete = root.addElement("delete");
        delete.addAttribute("id", "delete");
        String sql = "delete from " + ClassNameUtil.tableName +" where "+ ClassNameUtil.propertyToColumns(ClassNameUtil.fields[0].getName()) + "=#{id}";
        delete.setText(sql);
    }

    private void update() {
        Element update = root.addElement("update");
        //设置id
        update.addAttribute("id","update");
        //设置parameter
        update.addAttribute("parameterType",ClassNameUtil.allClassName);

        String sql = "update " + ClassNameUtil.tableName;

        update.setText(sql);

        Element ife = null;
        String type = "";
        String columnName = "";

        int i = 0;
        //创建set节点
        Element set = update.addElement("set");

        for(Field f:ClassNameUtil.fields){
            columnName = ClassNameUtil.propertyToColumns(f.getName());
            if(i == 0){
                i++;
                continue;
            }

            ife = set.addElement("if");
            type = f.getGenericType().toString();
            ifIsType(type,f,columnName,ife);
        }

        //设置where条件
        Element where = update.addElement("where");
        Element wif = where.addElement("if");
        Field f_0 = ClassNameUtil.fields[0];
        columnName = ClassNameUtil.propertyToColumns(f_0.getName());
        ifIsType(f_0.getGenericType().toString(),f_0,columnName,wif);
    }

    /**
     * 设置更新节点中的if节点的属性值和文本值
     * @param ife
     * @param condition
     * @param ifText
     */
    public void setUpdateIf(Element ife,String condition ,String ifText){
        ife.addAttribute("test",condition);
        ife.setText(ifText);
    }

    /**
     * 通用设置if节点的方法
     * @param type
     * @param f
     * @param columnName
     * @param ife
     */
    public void ifIsType(String type,Field f,String columnName,Element ife){
        String conditioin = "";
        String ifText = "";
        if("class java.lang.String".equals(type) || "class java.util.Date".equals(type)){
            conditioin = f.getName() + "!= null and " + f.getName() + "! = ''";
            ifText = columnName + "=#{" + f.getName() + "},";
            setUpdateIf(ife,conditioin,ifText);
        }else if("class java.lang.Integer".equals(type) || "class java.lang.Double".equals(type) || "int".equals(type) || "double".equals(type) || "float".equals(type)){
            conditioin = f.getName() + "!= 0";
            ifText = columnName + "=#{" + f.getName() + "},";
            setUpdateIf(ife,conditioin,ifText);
        }
    }

    /**
     * 设置findbypage方法where条件中的if条件
     * @param type 字段类型
     * @param f 字段
     * @param columnName 列名
     * @param ife if节点
     */
    public void findByPageIfIsType(String type,Field f,String columnName,Element ife){
        String conditioin = "";
        String ifText = "";
        if("class java.lang.String".equals(type) ){
            conditioin = ClassNameUtil.instanceName + "." + f.getName() + "!= null";
            ifText = "and " + columnName + " like CONCAT(CONCAT('%',#{"+ClassNameUtil.instanceName+"." + f.getName() + "}),'%')";
            setUpdateIf(ife,conditioin,ifText);
        }else if("class java.lang.Integer".equals(type) || "class java.lang.Double".equals(type) || "int".equals(type) || "double".equals(type) || "float".equals(type)){
            conditioin = ClassNameUtil.instanceName + "." + f.getName() + "!= 0";
            ifText = "and " + columnName + " =#{"+ClassNameUtil.instanceName+"." + f.getName() + "}";
            setUpdateIf(ife,conditioin,ifText);
        }else if("class java.util.Date".equals(type)){
            conditioin = ClassNameUtil.instanceName + "." + f.getName() + "!= null";
            ifText = "and " + columnName + " =#{"+ClassNameUtil.instanceName+"." + f.getName() + "}";
            setUpdateIf(ife,conditioin,ifText);
        }
    }



    private void findAll() {

    }

    private void findByProperty() {

    }


    private void findByPage() {
        Element select = root.addElement("select");
        //设置id
        select.addAttribute("id","findByPage");
        //设置resultMap
        select.addAttribute("resultMap","BaseRM");
        String sql = "select * from " + ClassNameUtil.tableName;
        select.setText(sql);
        Element where = select.addElement("where");

        String columnName = "";
        String condition = "";
        String ifText = "";
        Element ife = null;
        String type = "";
        //设置where条件
        for(Field f:ClassNameUtil.fields){
            ife = where.addElement("if");
            columnName = ClassNameUtil.propertyToColumns(f.getName());
            type = f.getGenericType().toString();
            findByPageIfIsType(type,f,columnName,ife);
        }

        //设置 order
        Element oif = select.addElement("if");
        condition = "shortName != null and shortName != '' and shortOrder != null and shortOrder != ''";
        ifText = "order by ${shortName} ${shortOrder}";
        setUpdateIf(oif,condition,ifText);

        //设置limit
        select.addText("limit #{startRow},#{showNumber}");
    }

    public void count() {
        Element select = root.addElement("select");
        select.addAttribute("id","count");
        select.addAttribute("resultType","int");


        String sql = "select count(*) from " + ClassNameUtil.tableName;
        select.setText(sql);
        String type = "";
        String columnName = "";
        Element ife = null;
        for(Field f:ClassNameUtil.fields){
            ife = select.addElement("if");
            type = f.getGenericType().toString();
            columnName = ClassNameUtil.propertyToColumns(f.getName());
            findByPageIfIsType(type,f,columnName,ife);
        }
    }


    public static void main(String[] args) throws ClassNotFoundException {
        Class c = Class.forName("com.csd.pojo.Person");
        Field[] f =  c.getDeclaredFields();
        System.out.println(f[0].getGenericType().toString());
    }
}
