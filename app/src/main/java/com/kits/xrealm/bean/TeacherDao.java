package com.kits.xrealm.bean;

import io.realm.RealmFieldType;
import io.realm.internal.Property;

/**
 * 目标 包名: io.realm
 * 类型 xxDao(XX 是注解的类名)
 *
 */
public class TeacherDao implements ITableDao{
    public static String tableName = "teacher";
    public static RealmColumn id = new RealmColumn(" ", "id", RealmFieldType.INTEGER, Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
    public static RealmColumn name = new RealmColumn(" ", "name", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, Property.REQUIRED);
    public static RealmColumn job = new RealmColumn(" ", "job", RealmFieldType.STRING, !Property.PRIMARY_KEY, !Property.INDEXED, !Property.REQUIRED);
    static {
        name.alias = "123";
    }
    public static RealmColumn[] columns = {
            id,name,job
    };
    public static RealmColumn[] columns1 = {

    };

    @Override
    public RealmColumn getColumnByName(String name) {
        for (RealmColumn column : columns){
            if (column.alias.equals(name) || column.internalName.equals(name)) {
                return column;
            }
        }
        return null;
    }

    @Override
    public String getTableName() {
        return tableName;
    }
}
