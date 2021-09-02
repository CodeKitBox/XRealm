package com.kits.realm.processor

import com.squareup.javawriter.JavaWriter
import java.io.BufferedWriter
import java.io.IOException
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier

/**
 * 生成类 RealmColumn
 */
class RealmColumnGenerator(private val env: ProcessingEnvironment) {
    companion object{
        // 存储成员变量 key : 成员变量名称  value 是成员变量类型
        val propertyMap = mapOf<String,String>(
            "alias" to String::class.java.name,
            "name" to String::class.java.name,
            "realmFieldType" to "RealmFieldType",
            "PRIMARY_KEY" to Boolean::class.java.name,
            "INDEXED" to Boolean::class.java.name,
            "REQUIRED" to Boolean::class.java.name

        )

    }

    @Throws(IOException::class)
    fun generate() {

        val qualifiedGeneratedClassName = String.format(Locale.US, "%s.%s", Constants.REALM_PACKAGE_NAME, Constants.DEFAULT_COLUMN_CLASS_NAME)
        val sourceFile = env.filer.createSourceFile(qualifiedGeneratedClassName)
        val writer = JavaWriter(BufferedWriter(sourceFile.openWriter()))


        writer.apply {
            indent = Constants.INDENT
            emitPackage(Constants.REALM_PACKAGE_NAME)
            emitEmptyLine()

            // 生成构造函数
            beginType(
                qualifiedGeneratedClassName, // full qualified name of the item to generate
                "class",               // the type of the item
                setOf(Modifier.PUBLIC),    // modifiers to apply
                null)           // class to extend
            emitEmptyLine()
            // 生成私有成员变量
            for ((key,value) in propertyMap){
                emitField(value, key,EnumSet.of(Modifier.PRIVATE))
            }
            // 单独生成关联的成员变量
            emitField("String", "linkedClassName",EnumSet.of(Modifier.PRIVATE),"null")
            // 转换为构造函数参数
            val constructorParams = mutableListOf<String>()
            for ((key,value) in propertyMap){
                constructorParams.add(value)
                constructorParams.add(key)
            }
            beginConstructor(EnumSet.of(Modifier.PUBLIC),constructorParams,null)

            for ((key,value) in propertyMap){
                emitStatement("this.$key = $key")
            }
            endConstructor()
            emitConstructorParam4(this)
            emitConstructorLinked(this)
            emitEmptyLine()
            // 生成getter 方法
            emitGetterMethod(this)
            emitGetterRealNameMethod(this)

            emitEmptyLine()

            endType()
            close()
        }

    }

    /**
     * 生成四个参数的构造函数
     *
    public RealmColumn(String alias, String internalName, RealmFieldType realmFieldType,  boolean REQUIRED) {
    this.alias = alias;
    this.name = name;
    this.realmFieldType = realmFieldType;
    this.PRIMARY_KEY = false;
    this.REQUIRED = REQUIRED;
    this.INDEXED = false;
    }
     */
    private fun emitConstructorParam4(writer: JavaWriter){
        writer.apply {
            emitEmptyLine()
            val paramsArray = arrayOf("String","alias",
                "String", "name",
                "RealmFieldType", "realmFieldType",
                "boolean","REQUIRED")
            beginConstructor(EnumSet.of(Modifier.PUBLIC),*paramsArray)
            emitStatement("this.alias = alias")
            emitStatement("this.name = name")
            emitStatement("this.realmFieldType = realmFieldType")
            emitStatement("this.PRIMARY_KEY = false")
            emitStatement("this.INDEXED = false")
            emitStatement("this.REQUIRED = REQUIRED")
            endConstructor()
            emitEmptyLine()
        }
    }

    /**
     * 生成关联其他数据库表的构造函数
     */
    private fun emitConstructorLinked(writer: JavaWriter){
        writer.apply {
            emitEmptyLine()
            val paramsArray = arrayOf("String","alias",
                "String", "name",
                "RealmFieldType", "realmFieldType",
                "String","linkedClassName")
            beginConstructor(EnumSet.of(Modifier.PUBLIC),*paramsArray)
            emitStatement("this.alias = alias")
            emitStatement("this.name = name")
            emitStatement("this.realmFieldType = realmFieldType")
            emitStatement("this.PRIMARY_KEY = false")
            emitStatement("this.REQUIRED = false")
            emitStatement("this.INDEXED = false")
            emitStatement("this.linkedClassName = linkedClassName")
            endConstructor()
            emitEmptyLine()
        }
    }



    private fun emitGetterMethod(writer: JavaWriter) {
        writer.apply {
            for ((key,value) in propertyMap){
                beginMethod(value, "$key", EnumSet.of(Modifier.PUBLIC))
                emitStatement("return $key")
                endMethod()
                emitEmptyLine()
            }
        }
    }

    /**
     * 生成 RealName 方法
     */
    private fun emitGetterRealNameMethod(writer: JavaWriter){
        writer.apply {
            emitEmptyLine()
            beginMethod("String", "columnName", EnumSet.of(Modifier.PUBLIC))
            beginControlFlow("if(alias != null && alias.length() != 0)")
            emitStatement("return alias")
            endControlFlow()
            emitStatement("return name")
            endMethod()
            emitEmptyLine()
        }
    }

}