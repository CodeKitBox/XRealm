package com.kits.xrealm.migration

import io.realm.*
import io.realm.annotations.Ignore
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import io.realm.annotations.Required
import java.lang.reflect.Modifier

class DbMigration : RealmMigration {
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        println("数据库升级 oldVersion $oldVersion; newVersion $newVersion")
        migration(realm)
    }

    /**
     * 数据库统一迁徙
     * @param realm 数据库操作
     */
    private fun migration(realm: DynamicRealm){
        val realmTableList = realm.sharedRealm.configuration.realmObjectClasses
        realmTableList.forEach {
            table->
            // 获取所有的属性
            val fields = table.declaredFields
            val fieldNameMap = mutableMapOf<String,Class<*>>()
            fields.forEach {
                field ->
                if(!Modifier.isStatic(field.modifiers) && !Modifier.isTransient(field.modifiers)
                    && field.getAnnotation(Ignore::class.java) == null){
                    fieldNameMap[field.name] = field.type
                }
            }
            val tableName = RealmMasterDao.tableDaoMap[table.name]?.tableName
            if (tableName != null){
                println("tableName == $tableName")
                // 判断表是否存在
                var realmObjectSchema :RealmObjectSchema ? = realm.schema.get(tableName)
                if(realmObjectSchema == null){
                    // 生成表
                    realmObjectSchema = realm.schema.create(tableName)
                    // 通过表名查到列信息
                    val tableDao =  RealmMasterDao.tableDaoMap[table.name]
                    // 获取列信息
                    for ((name,type ) in fieldNameMap){
                        tableDao?.getColumnByName(name)?.let { column->
                            migrationAdd(realmObjectSchema,type,column)
                        }
                    }
                }else{
                    // 通过表名查到列信息
                    val tableDao =  RealmMasterDao.tableDaoMap[table.name]
                    // 判断列是否存在
                    for((name,type) in fieldNameMap){
                        if(!realmObjectSchema.hasField(name)){
                            tableDao?.getColumnByName(name)?.let { column->
                                migrationAdd(realmObjectSchema,type,column)
                            }
                        }else{
                            tableDao?.getColumnByName(name)?.let { column->
                                migrationChange(realmObjectSchema,column)
                            }
                        }
                    }
                }
            }

        }
    }

    /**
     * 数据库统一迁徙,对不存在的列进行新增
     * @param realm 数据库操作R
     * @param column 列信息
     */
    private fun migrationAdd(realm: RealmObjectSchema, clazz:Class<*>,column:RealmColumn){
        val attributes = mutableListOf<FieldAttribute>()
        if(column.PRIMARY_KEY()){
            attributes.add(FieldAttribute.PRIMARY_KEY)
        }
        if(column.INDEXED()){
            attributes.add(FieldAttribute.INDEXED)
        }
        if(column.REQUIRED()){
            attributes.add(FieldAttribute.REQUIRED)
        }
        realm.addField(column.columnName(),clazz, *attributes.toTypedArray())
    }


    /**
     * 数据库统一迁徙,对已存在的列进行修改属性进行修改
     * @param realm 数据库操作
     * @param column 列信息
     */
    private fun migrationChange(realm: RealmObjectSchema,column:RealmColumn){

        FieldAttribute.values().forEach {
            when(it){
                FieldAttribute.REQUIRED->
                    object :ChangeWrap(){
                        override fun removeChange() {
                            realm.setRequired(column.columnName(),false)
                        }

                        override fun addChange() {
                            realm.setRequired(column.columnName(),true)
                        }
                    }.apply(realm.isRequired(column.columnName()),column.REQUIRED())
                FieldAttribute.INDEXED->
                    object :ChangeWrap(){
                        override fun removeChange() {
                            realm.removeIndex(column.columnName())
                        }

                        override fun addChange() {
                            realm.addIndex(column.columnName())
                        }

                    }.apply(realm.hasIndex(column.columnName()),column.INDEXED())
                FieldAttribute.PRIMARY_KEY->
                    object :ChangeWrap(){
                        override fun removeChange() {
                            realm.removePrimaryKey()
                        }
                        override fun addChange() {
                            realm.addPrimaryKey(column.columnName())
                        }

                    }.apply(realm.isPrimaryKey(column.columnName()),column.PRIMARY_KEY())
            }
        }
    }


    internal abstract class ChangeWrap{
        // 存在属性
        private val exist = 0b1
        // 不存在属性
        private val none = 0b0

        /**
         * @param pre 升级前的列属性
         * @param cur 升级后的列属性
         */
        fun apply(pre:Boolean, cur:Boolean){
            val high = (if(pre) exist else none).shl(1)
            val low = if(cur) exist else none
            when(high.or(low)){
                0,3-> noChange()
                1->addChange()
                2->removeChange()
            }
        }

        /**
         * 属性无变化
         */
         fun noChange(){

         }

        /**
         * 移除属性
         */
        abstract  fun removeChange()

        /**
         * 新增属性
         */
        abstract fun addChange()
    }
}