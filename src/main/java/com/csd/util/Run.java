package com.csd.util;


import java.io.IOException;
import java.util.List;

/**
 * 程序的入口
 * Created by sdc on 2018/4/20.
 */
public class Run {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        List<String> classNames =  ClassNameUtil.getClassName("com.csd.pojo");
        for(String s:classNames) {
            new XmlUtil().createMapper(s);
        }
    }
}
