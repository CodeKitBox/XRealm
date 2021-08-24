package com.kits.xrealm.bean;

import io.realm.RealmFieldType;

/**
 * 生成一个固定的列信息的类
 */
public class RealmColumn {
    /**
     * 列的别名,通过注解 RealmField 来实现,默认为null
     * 如果不为空,则别名是列在数据库存在的名称
     */
    public String alias = "";
    /**
     * 列的名称,属性名称
     */
    public String internalName;

    /**
     * 列的在数据库中类型
     */
    public RealmFieldType realmFieldType;
    /**
     * 列是否为主键
     */
    public boolean PRIMARY_KEY = false;
    /**
     * 列是否必须有值
     */
    public boolean REQUIRED = false;
    /**
     * 列是否是索引
     */
    public boolean INDEXED = false;

    public RealmColumn(String alias, String internalName, RealmFieldType realmFieldType, boolean PRIMARY_KEY, boolean REQUIRED, boolean INDEXED) {
        this.alias = alias;
        this.internalName = internalName;
        this.realmFieldType = realmFieldType;
        this.PRIMARY_KEY = PRIMARY_KEY;
        this.REQUIRED = REQUIRED;
        this.INDEXED = INDEXED;
    }

    public RealmColumn(String alias, String internalName, RealmFieldType realmFieldType,  boolean REQUIRED) {
        this.alias = alias;
        this.internalName = internalName;
        this.realmFieldType = realmFieldType;
        this.PRIMARY_KEY = false;
        this.REQUIRED = REQUIRED;
        this.INDEXED = false;
    }

    public String getAlias() {
        return alias;
    }
}
