package com.kits.xrealm.migration

import io.realm.*
import io.realm.annotations.*
import java.util.*
import kotlin.random.Random

import io.realm.ex.InitDefVal
import java.lang.reflect.Modifier
import java.lang.reflect.Field
import kotlin.reflect.KClass

class DbMigration : RealmMigration {
    override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
        println("数据库升级 oldVersion $oldVersion; newVersion $newVersion")
//        if(oldVersion < 2){
//
//            realm.schema.get("Teacher")?.let {
//               // println("PrimaryKey == ${it.primaryKey}")
//                    it.addField("id",String::class.java)
//                        .addPrimaryKey("id")
//
//            }
//        }
        migration(realm)
        realm.schema.get("Teacher")?.let {
            println("PrimaryKey == ${it.primaryKey}")
        }
        realm.schema.get("Student")?.let {
            println("PrimaryKey == ${it.primaryKey}")
        }
        realm.schema.get("School")?.let {
            println("PrimaryKey == ${it.primaryKey}")
        }
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
            // 存储列 和列的类型
            val columnNameMap = mutableMapOf<String,Class<*>>()
            // 存储列和列的初始值
            val columnDefValMap = mutableMapOf<String,KClass<out InitDefVal<Any>>>()

            for(field in fields){
                // 判断是否是列属性
                if(!isColumnField(field)){
                    continue
                }
                saveColumnType(field,columnNameMap)
                saveColumnDefVal(field,columnDefValMap)
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
                    for ((name,type ) in columnNameMap){
                        tableDao?.getColumnByName(name)?.let { column->
                            migrationAdd(realmObjectSchema,type,column,columnDefValMap)
                        }
                    }
                }else{
                    // 通过表名查到列信息
                    val tableDao =  RealmMasterDao.tableDaoMap[table.name]
                    // 判断列是否存在
                    for((name,type) in columnNameMap){
                        if(!realmObjectSchema.hasField(name)){
                            tableDao?.getColumnByName(name)?.let { column->
                                migrationAdd(realmObjectSchema,type,column,columnDefValMap)
                            }
                        }else{
                            val column = tableDao?.getColumnByName(name);
                            if(column != null){
                                // 列的属性变更迁徙
                                migrationChange(realmObjectSchema,column)
                            }

                        }
                    }
                }
            }

        }
    }

    /**
     * 获取列和列的初始值
     * @param field 成员变量
     * @param defValMap  存储数据的map
     */
    private fun saveColumnDefVal(field: Field,defValMap:MutableMap<String, KClass<out InitDefVal<Any>>>){
        val annotation = field.getAnnotation(RealmExField::class.java) ?: return
        defValMap[getColumnName(field)] = annotation.defValue
    }

    /**
     *  获取列的名称 和 列的数据类型
     *  @param field 成员变量
     *  @param columnNameMap 存储数据的map
     *
     */
    private fun saveColumnType(field: Field,columnNameMap:MutableMap<String,Class<*>>){
        columnNameMap[getColumnName(field)] = field.type
    }

    /**
     * 获取列的名称
     * @param field 成员变量
     * @return 返回列的名称
     */
    private fun getColumnName(field: Field):String{
        val fieldAnnotation = field.getAnnotation(RealmField::class.java)
        var columnName:String? = null

        if(fieldAnnotation != null){
            if(fieldAnnotation.value.isNotEmpty()){
                columnName = fieldAnnotation.value
            }

            if(fieldAnnotation.name.isNotEmpty()){
                columnName = fieldAnnotation.name
            }
        }
        return columnName ?: field.name
    }

    /**
     * 判断成员属性是否是为数据库的列
     * @param field 成员变量
     * @return true 成员变量是数据库表的列
     */
    private fun isColumnField(field: Field):Boolean{
        if(Modifier.isStatic(field.modifiers)){
            return false
        }
        if(Modifier.isTransient(field.modifiers)){
            return false
        }
        if(field.getAnnotation(Ignore::class.java) != null){
            return false
        }
        return true
    }

    /**
     * 数据库统一迁徙,对不存在的列进行新增
     * @param realm 数据库操作R
     * @param column 列信息
     */
    private fun migrationAdd(realm: RealmObjectSchema, clazz:Class<*>,column:RealmColumn,
                             defValMap:MutableMap<String, KClass<out InitDefVal<Any>>>){
        var isPrimaryKey = false
        val attributes = mutableListOf<FieldAttribute>()
        if(column.PRIMARY_KEY()){
            removePrimaryKey(realm)
            isPrimaryKey = true
        }
        if(column.INDEXED()){
            attributes.add(FieldAttribute.INDEXED)
        }
        if(column.REQUIRED()){
            attributes.add(FieldAttribute.REQUIRED)
        }
        println("添加列 ${column.columnName()} $attributes")
        val defValClazz = defValMap[column.name()]
        if( defValClazz != null){
            if (isPrimaryKey){
                realm.addField(column.name(),clazz, *attributes.toTypedArray())
                    .transform { it.set(column.name(), defValClazz.java.newInstance().defVal()) }
                    .addPrimaryKey(column.name())
            }else{
                realm.addField(column.name(),clazz, *attributes.toTypedArray())
                    .transform { it.set(column.name(),defValClazz.java.newInstance().defVal()) }
            }

        }else{
            if (isPrimaryKey){
                attributes.add(FieldAttribute.PRIMARY_KEY)
            }
            realm.addField(column.name(),clazz, *attributes.toTypedArray())
        }

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
                    object :ChangeWrap(it,realm,column){
                        override fun removeChange(
                            attr: FieldAttribute,
                            realm: RealmObjectSchema,
                            column: RealmColumn
                        ) {
                            super.removeChange(attr, realm, column)
                            realm.setRequired(column.name(),false)
                        }

                        override fun addChange(
                            attr: FieldAttribute,
                            realm: RealmObjectSchema,
                            column: RealmColumn
                        ) {
                            super.addChange(attr, realm, column)
                            realm.setRequired(column.name(),true)
                        }
                    }.apply(realm.isRequired(column.name()),column.REQUIRED())
                }

                FieldAttribute.INDEXED->{
                    object :ChangeWrap(it,realm,column){
                        override fun removeChange(
                            attr: FieldAttribute,
                            realm: RealmObjectSchema,
                            column: RealmColumn
                        ) {
                            super.removeChange(attr, realm, column)
                            realm.removeIndex(column.name())
                        }

                        override fun addChange(
                            attr: FieldAttribute,
                            realm: RealmObjectSchema,
                            column: RealmColumn
                        ) {
                            super.addChange(attr, realm, column)
                            realm.addIndex(column.name())
                        }

                    }.apply(realm.hasIndex(column.name()),column.INDEXED()||column.PRIMARY_KEY())
                }

                FieldAttribute.PRIMARY_KEY->{
                    object :ChangeWrap(it,realm,column){
                        override fun removeChange(
                            attr: FieldAttribute,
                            realm: RealmObjectSchema,
                            column: RealmColumn
                        ) {
                            super.removeChange(attr, realm, column)
                            realm.removePrimaryKey()
                        }
                        override fun addChange(
                            attr: FieldAttribute,
                            realm: RealmObjectSchema,
                            column: RealmColumn
                        ) {
                            super.addChange(attr, realm, column)
                            realm.addPrimaryKey(column.name())
                        }

                    }.apply(realm.isPrimaryKey(column.name()),column.PRIMARY_KEY())
                }

            }
        }
    }

    /**
     * 移除主键操作，当表存在主键移除主键
     */
    private fun removePrimaryKey(realm: RealmObjectSchema){
        try{
            realm.primaryKey?.let {
                realm.removePrimaryKey()
            }
        }catch (e:Exception){
            println("无主键异常")
        }
    }


    internal abstract class ChangeWrap(private val attr:FieldAttribute,
        private  val realm: RealmObjectSchema, private val column:RealmColumn){
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
                0,3-> noChange(attr, realm, column)
                1->addChange(attr, realm, column)
                2->removeChange(attr, realm, column)
            }
        }

        private fun transform(attr:FieldAttribute):String{
            return when(attr){
                FieldAttribute.REQUIRED->"FieldAttribute.REQUIRED"
                FieldAttribute.PRIMARY_KEY->"FieldAttribute.PRIMARY_KEY"
                FieldAttribute.INDEXED->"FieldAttribute.INDEXED"
            }
        }
        /**
         * 属性无变化
         */
         open fun noChange(attr:FieldAttribute,realm: RealmObjectSchema, column:RealmColumn){
            println("${transform(attr)} ${column.columnName()} noChange")
         }

        /**
         * 移除属性
         */
        open fun removeChange(attr:FieldAttribute,realm: RealmObjectSchema, column:RealmColumn){
            println("${transform(attr)} ${column.columnName()} removeChange")
        }

        /**
         * 新增属性
         */
        open fun addChange(attr:FieldAttribute,realm: RealmObjectSchema, column:RealmColumn){
            println("${transform(attr)} ${column.columnName()} addChange")
            if (attr == FieldAttribute.PRIMARY_KEY){
                try{
                    realm.primaryKey?.let {
                        realm.removePrimaryKey()
                    }
                }catch (e:Exception){
                    println("无主键异常")
                }

            }
        }
    }
}