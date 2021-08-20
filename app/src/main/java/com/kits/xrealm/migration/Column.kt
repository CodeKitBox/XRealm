package com.kits.xrealm.migration

import io.realm.FieldAttribute
import io.realm.RealmFieldType
import io.realm.internal.Property
import java.util.*

/**
 * @param name 列名称
 * @param type 列类型
 * @param primaryKey 是否为主键
 * @param indexed 是否为索引
 * @param required 是否为必须有值
 */
class Column(val name: String, val type: Class<Any>,
                  val primaryKey: Boolean, val indexed: Boolean, val required: Boolean) {
    /**
     * 获取列属性
     */
    fun fieldAttributes():MutableList<FieldAttribute>{
        val attributes = mutableListOf<FieldAttribute>()
        if(primaryKey){
            attributes.add(FieldAttribute.PRIMARY_KEY)
        }
        if(indexed){
            attributes.add(FieldAttribute.INDEXED)
        }
        if(required){
            attributes.add(FieldAttribute.REQUIRED)
        }
        return attributes
    }
}