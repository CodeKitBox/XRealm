package com.kits.realm.processor

import com.squareup.javawriter.JavaWriter
import javax.lang.model.element.Modifier
import java.io.BufferedWriter
import java.io.IOException
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.tools.JavaFileObject


/**
 * RealmMasterDao 文件生成
 */
class RealmMasterDaoGenerator (private val processingEnvironment: ProcessingEnvironment,
                               private val className: SimpleClassName,
                               classesToValidate: Set<ClassMetaData>){


    /**
     * key 值是 数据库表的实列的类名
     * value 值是  数据库表的实列的类名 对应的Dao文件
     */
    private val qualifiedDaoMap = mutableMapOf<QualifiedClassName, QualifiedClassName>()
    init {
        for (metadata in classesToValidate) {
            qualifiedDaoMap[metadata.qualifiedClassName] = metadata.qualifiedDaoClassName
        }

    }

    @Throws(IOException::class)
    fun generate() {
        val qualifiedGeneratedClassName: String = String.format(Locale.US, "%s.%s", Constants.REALM_PACKAGE_NAME, "RealmMasterDao")
        val sourceFile: JavaFileObject = processingEnvironment.filer.createSourceFile(qualifiedGeneratedClassName)
        val imports = ArrayList(Arrays.asList(
            "java.io.IOException",
            "java.util.Collections",
            "java.util.HashSet",
            "java.util.Map",
            "java.util.HashMap"))

        val writer = JavaWriter(BufferedWriter(sourceFile.openWriter()))
        writer.apply {
            indent = "    "
            emitPackage(Constants.REALM_PACKAGE_NAME)
            emitEmptyLine()
            emitImports(imports)
            emitEmptyLine()

            beginType(qualifiedGeneratedClassName,
                "class", EnumSet.of(Modifier.PUBLIC))
            emitEmptyLine()
            emitFields(this)

            endType()
            close()
        }
    }

    @Throws(IOException::class)
    private fun emitFields(writer: JavaWriter) {
        writer.apply {
            emitField("Map<String,ITableDao>", "tableDaoMap", EnumSet.of(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL))
            beginInitializer(true)
            emitStatement("Map<String,ITableDao> daoMap = new HashMap<>()")
            for ((key,value ) in qualifiedDaoMap){
                emitStatement("daoMap.put(%s.class.getName(),new %s())",key,value)
            }
            emitStatement("tableDaoMap = Collections.unmodifiableMap(daoMap)")
            endInitializer()
            emitEmptyLine()
        }
    }
}