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
            // 获取table bean的 所有属性
            val fields = table.declaredFields
            // 列信息
            val columnList = mutableListOf<Column>()
            fields.forEach {
                field ->
                    println("name == ${field.name}")
                    if(!Modifier.isStatic(field.modifiers)){
                        println("非静态属性 name == ${field.name}")
                        field.declaredAnnotations.forEach {
                            annotation ->
                            println("annotation == $annotation")
                        }
                        if(!field.isAnnotationPresent(Ignore::class.java)){
                            // 列名
                            val column = Column(field.name, field.type as Class<Any>,
                                    field.isAnnotationPresent(PrimaryKey::class.java),
                                    field.isAnnotationPresent(Index::class.java),
                                    field.isAnnotationPresent(Required::class.java))
                            columnList.add(column)
                        }
                    }
            }
            println("columnList size == ${columnList.size}")
            // 判断表是否存在
            var realmObjectSchema :RealmObjectSchema ? = realm.schema.get(table.simpleName)
            if(realmObjectSchema == null){
                realmObjectSchema = realm.schema.create(table.simpleName)
                columnList.forEach {
                    column ->
                        migrationAdd(realmObjectSchema,column)
                }
            }else{
                columnList.forEach {
                    column ->
                    println("column 信息  == ${column}")
                        if (!realmObjectSchema.hasField(column.name)){
                            migrationAdd(realmObjectSchema, column)
                        }else{
                           // migrationChange(realmObjectSchema, column)
                        }
                }
            }
        }
    }

    /**
     * 数据库统一迁徙,对不存在的列进行新增
     * @param realm 数据库操作
     * @param column 列信息
     */
    private fun migrationAdd(realm: RealmObjectSchema, column:Column){
        val attributes = column.fieldAttributes().toTypedArray()
        realm.addField(column.name,column.type, *attributes)

    }


    /**
     * 数据库统一迁徙,对已存在的列进行修改，逻辑处理有问题，预留
     * @param realm 数据库操作
     * @param column 列信息
     */
    private fun migrationChange(realm: RealmObjectSchema,column:Column){

        FieldAttribute.values().forEach {
            println("name = ${column.name} $it")
            when(it){
                FieldAttribute.REQUIRED->
                    object :ChangeWrap(){
                        override fun removeChange() {
                            // 非空注解，无法运行时获取，因此只支持添加,不支持删除
                            super.noChange()
                        }

                        override fun addChange() {
                            super.addChange()
                            realm.setRequired(column.name,true)
                        }
                    }.apply(realm.isRequired(column.name),column.required || column.primaryKey)
                FieldAttribute.INDEXED->
                    object :ChangeWrap(){
                        // 原列是否为主键
                        val dbPrimaryKey = realm.isPrimaryKey(column.name)
                        // 升级后为主键
                        val curPrimaryKey = column.primaryKey
                        // 是否有 索引 注解
                        val indexAnnotation = column.indexed
                        override fun removeChange() {
                            if(!dbPrimaryKey && !curPrimaryKey){
                                super.removeChange()
                                realm.removeIndex(column.name)
                            }else{
                                noChange()
                            }
                        }

                        override fun addChange() {
                            if(indexAnnotation){
                                super.addChange()
                                realm.addIndex(column.name)
                            }else{
                                noChange()
                            }
                        }

                    }.apply(realm.hasIndex(column.name),column.indexed)
                FieldAttribute.PRIMARY_KEY->
                    object :ChangeWrap(){
                        override fun removeChange() {
                            super.removeChange()
                            realm.removePrimaryKey()
                        }
                        override fun addChange() {
                            super.addChange()
                            realm.addPrimaryKey(column.name)
                        }

                    }.apply(realm.isPrimaryKey(column.name),column.primaryKey)
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
        open fun noChange(){
            println("无变化")
        }

        /**
         * 移除属性
         */
        open  fun removeChange(){
            println("移除属性")
        }

        /**
         * 新增属性
         */
        open  fun addChange(){
            println("新增属性")
        }
    }
}