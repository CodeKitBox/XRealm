package com.kits.xrealm.migration

import io.realm.*
import io.realm.annotations.*
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

                        val fieldAnnotation = field.getAnnotation(RealmField::class.java)
                        var fileName:String? = null

                        if(fieldAnnotation != null){
                            if(fieldAnnotation.value.isNotEmpty()){
                                fileName = fieldAnnotation.value
                            }

                            if(fieldAnnotation.name.isNotEmpty()){
                                fileName = fieldAnnotation.name
                            }
                        }

                        if(fileName!= null){
                            fieldNameMap[fileName] = field.type
                        }else{
                            fieldNameMap[field.name] = field.type
                        }
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
                            val column = tableDao?.getColumnByName(name);
                            if(column != null){
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
        realm.addField(column.name(),clazz, *attributes.toTypedArray())
    }


    /**
     * 数据库统一迁徙,对已存在的列进行修改属性进行修改
     * @param realm 数据库操作
     * @param column 列信息
     */
    private fun migrationChange(realm: RealmObjectSchema,column:RealmColumn){
        println("判断列 ${column.name()} 属性变更")
        FieldAttribute.values().forEach {

            when(it){
                FieldAttribute.REQUIRED->{
                    println("属性 FieldAttribute.REQUIRED")
                    object :ChangeWrap(){
                        override fun removeChange() {
                            super.removeChange()
                            realm.setRequired(column.name(),false)
                        }

                        override fun addChange() {
                            super.addChange()
                            realm.setRequired(column.name(),true)
                        }
                    }.apply(realm.isRequired(column.name()),column.REQUIRED())
                }

                FieldAttribute.INDEXED->{
                    println("属性 FieldAttribute.INDEXED")
                    object :ChangeWrap(){
                        override fun removeChange() {
                            super.removeChange()
                            realm.removeIndex(column.name())
                        }

                        override fun addChange() {
                            super.addChange()
                            realm.addIndex(column.name())
                        }

                    }.apply(realm.hasIndex(column.name()),column.INDEXED()||column.PRIMARY_KEY())
                }

                FieldAttribute.PRIMARY_KEY->{
                    println("属性 FieldAttribute.PRIMARY_KEY")
                    object :ChangeWrap(){
                        override fun removeChange() {
                            super.removeChange()
                            realm.removePrimaryKey()
                        }
                        override fun addChange() {
                            super.addChange()
                            realm.addPrimaryKey(column.name())
                        }

                    }.apply(realm.isPrimaryKey(column.name()),column.PRIMARY_KEY())
                }

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
            println("升级前 属性 $pre ; 升级后 $cur")
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
         open fun noChange(){
            println("noChange")
         }

        /**
         * 移除属性
         */
        open fun removeChange(){
            println("removeChange")
        }

        /**
         * 新增属性
         */
        open fun addChange(){
            println("addChange")
        }
    }
}