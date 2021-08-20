package com.kits.xrealm.bean;

public interface ITableDao {
    /**
     * 通过 名称查找列属性
     * @param name
     * @return 查找到返回列信息,否则返回null
     */
    RealmColumn getColumnByName(String name);

    /**
     * 获取表名
     * @return
     */
    String getTableName();
}
