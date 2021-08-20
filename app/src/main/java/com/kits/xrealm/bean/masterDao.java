package com.kits.xrealm.bean;

import java.util.HashMap;

import java.util.Map;


public class masterDao {
    public static Map<String,ITableDao> tableDaoMap = new HashMap<>();
    static {
        tableDaoMap.put(com.kits.xrealm.bean.Teacher.class.getName(),new TeacherDao());
    }


}
