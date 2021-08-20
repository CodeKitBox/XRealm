package com.kits.realm.processor

import com.squareup.javawriter.JavaWriter
import io.realm.annotations.Ignore
import java.io.BufferedWriter
import java.io.IOException
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier

/**
 * 生成 ITableDao 接口
 */
class RealmTableDaoInterfaceGenerator(private val env: ProcessingEnvironment) {

    @Throws(IOException::class)
    fun generate() {
        val qualifiedGeneratedInterfaceName = String.format(Locale.US, "%s.%s", Constants.REALM_PACKAGE_NAME, Constants.DEFAULT_TABLE_INTERFACE_NAME)
        println("生成ITableDao的完整名 $qualifiedGeneratedInterfaceName")
        val sourceFile = env.filer.createSourceFile(qualifiedGeneratedInterfaceName)
        val writer = JavaWriter(BufferedWriter(sourceFile.openWriter()!!))
        writer.apply {
            indent = Constants.INDENT
            emitPackage(Constants.REALM_PACKAGE_NAME)
            emitEmptyLine()
            beginType(qualifiedGeneratedInterfaceName, "interface", EnumSet.of(Modifier.PUBLIC))

            beginMethod(String::class.java.name, "getTableName", EnumSet.of(Modifier.PUBLIC))
            endMethod()

            beginMethod("RealmColumn", "getColumnByName", EnumSet.of(Modifier.PUBLIC),"String","name")
            endMethod()

            endType()
            close()
        }
    }
}