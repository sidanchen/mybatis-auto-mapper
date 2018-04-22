package com.csd.util;


import java.io.IOException;

/**
 * 程序的入口
 * Created by sdc on 2018/4/20.
 */
public class Run {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        new XmlUtil().createMapper("com.csd.pojo.Person");
    }
}
