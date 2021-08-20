package com.kits.realm.processor

import com.squareup.javawriter.JavaWriter
import java.io.BufferedWriter
import java.io.IOException
import java.lang.StringBuilder
import java.util.*
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement

class RealmDaoGenerator(private val processingEnvironment: ProcessingEnvironment,
                        private val typeMirrors: TypeMirrors,
                        private val metadata: ClassMetaData,
                        private val classCollection: ClassCollection)  {
    companion object {


        private val IMPORTS: List<String>

        init {
            val l = Arrays.asList(
                "io.realm.RealmFieldType",
                "io.realm.internal.Property",
                "io.realm.ITableDao",
                "io.realm.RealmColumn")
            IMPORTS = Collections.unmodifiableList(l)
        }

    }
    // 类的简要名称
    private val simpleJavaClassName: SimpleClassName = metadata.simpleJavaClassName
    // 类的完整名称
    private val qualifiedJavaClassName: QualifiedClassName = metadata.qualifiedClassName
    // 生成的包名
    private val generatedPackageName = metadata.packageName+".build"
    // 生成的完整类名
    private val generatedClassName: QualifiedClassName = QualifiedClassName(String.format(Locale.US, "%s.%s%s", generatedPackageName, simpleJavaClassName,"Dao"))
    // 数据库内部名称 -- 表名
    private val internalClassName: String = metadata.internalClassName
    @Throws(IOException::class)
    fun generate() {
        println("生成Dao文件 ")
        println("简要名称 $simpleJavaClassName")
        println("完整名称 $qualifiedJavaClassName")
        println("生成的包名 $generatedPackageName")
        println("生成的完整类名 $generatedClassName")
        val sourceFile = processingEnvironment.filer.createSourceFile(generatedClassName.toString())
        val imports = ArrayList(IMPORTS)
        val writer = JavaWriter(BufferedWriter(sourceFile.openWriter()))
        writer.apply {
            indent = Constants.INDENT // Set source code indent
            emitPackage(generatedPackageName)
            emitEmptyLine()
            emitImports(imports)
            emitEmptyLine()
            // 生成类
            beginType(generatedClassName.toString(), "class", setOf(Modifier.PUBLIC), null, "ITableDao")
            // 生成成员变量,表名
            emitTableNameField(this)
            // 生成成员变量
            emitField("String", "NO_ALIAS", EnumSet.of(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL), "\"\"")
            // 生成列信息的成员变量
            emitCreateSchemaInfoField(this)
            // 生成数组存储列信息
            emitColumnArrayField(this)

            // 生成返回表名的方法
            emitGetTableNameMethod(this)
            // 生成根据列明获取列信息的方法
            emitGetColumnByName(this)
            emitEmptyLine()
            // End the class definition
            endType()
            close()
        }
    }
    private fun emitGetColumnByName(writer: JavaWriter){
        writer.apply {
            emitAnnotation("Override")
            beginMethod("RealmColumn", "getColumnByName", EnumSet.of(Modifier.PUBLIC),
                "String","name")
            beginControlFlow("for (RealmColumn column : columns)")
                beginControlFlow("if (name.equals(column.alias()) || name.equals(column.name()))")
                    emitStatement("return column")
                endControlFlow()
            endControlFlow()
            emitStatement("return null")
            endMethod()
        }
    }

    /**
     * 生成返回表名的类
     */
    @Throws(IOException::class)
    private fun emitGetTableNameMethod(writer: JavaWriter) {
        writer.apply {
            emitAnnotation("Override")
            beginMethod("String", "getTableName", EnumSet.of(Modifier.PUBLIC))
            emitStatement("return \"%s\"", internalClassName)
            endMethod()
            emitEmptyLine()
        }
    }

    /**
     * 生成列信息数组，存储列信息
     */
    private fun emitColumnArrayField(writer: JavaWriter) {

        writer.apply {
            emitEmptyLine()
            val elementBuilder = StringBuilder()
            for (field in metadata.fields){
                val internalFieldName = field.internalFieldName
                elementBuilder.append(" $internalFieldName ,")
            }
            val initExpression = String.format("{%s}",elementBuilder.toString())
            emitField("RealmColumn[]", "columns", EnumSet.of(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL),initExpression)
            emitEmptyLine()
        }
    }

    /**
     * 成员变量，表名
     */
    private fun emitTableNameField(writer: JavaWriter) {

        writer.apply {
            emitEmptyLine()
            val tableName = "\"$internalClassName\""
            emitField("String", "tableName", EnumSet.of(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL), tableName)
            emitEmptyLine()
        }
    }

    /**
     * 生成列信息的成员变量
     * 这个方法要详细测试
     */
    @Throws(IOException::class)
    private fun emitCreateSchemaInfoField(writer: JavaWriter) {
        writer.apply {
            // Guess capacity for Arrays used by OsObjectSchemaInfo.
            // Used to prevent array resizing at runtime
            val persistedFields = metadata.fields.size
            val computedFields = metadata.backlinkFields.size
            val embeddedClass = if (metadata.embedded) "true" else "false"
            val publicClassName = if (simpleJavaClassName.name != internalClassName) "\"${simpleJavaClassName.name}\"" else "NO_ALIAS"

            // For each field generate corresponding table index constant
            for (field in metadata.fields) {
                val internalFieldName = field.internalFieldName
                val publicFieldName = if (field.javaName == internalFieldName) "NO_ALIAS" else "\"${field.javaName}\""

                when (val fieldType = getRealmTypeChecked(field)) {
                    Constants.RealmFieldType.NOTYPE -> {
                        // Perhaps this should fail quickly?
                    }
                    Constants.RealmFieldType.OBJECT -> {
                        val fieldTypeQualifiedName = Utils.getFieldTypeQualifiedName(field)
                        val internalClassName = Utils.getReferencedTypeInternalClassNameStatement(fieldTypeQualifiedName, classCollection)
                        //emitField("RealmColumn", internalClassName, EnumSet.of(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL))
                        //emitStatement("builder.addPersistedLinkProperty(%s, \"%s\", RealmFieldType.OBJECT, %s)", publicFieldName, internalFieldName, internalClassName)
                    }
                    Constants.RealmFieldType.LIST -> {
                        val genericTypeQualifiedName = Utils.getGenericTypeQualifiedName(field)
                        val internalClassName = Utils.getReferencedTypeInternalClassNameStatement(genericTypeQualifiedName, classCollection)
                        //emitStatement("builder.addPersistedLinkProperty(%s, \"%s\", RealmFieldType.LIST, %s)", publicFieldName, internalFieldName, internalClassName)
                    }
                    Constants.RealmFieldType.INTEGER_LIST,
                    Constants.RealmFieldType.BOOLEAN_LIST,
                    Constants.RealmFieldType.STRING_LIST,
                    Constants.RealmFieldType.BINARY_LIST,
                    Constants.RealmFieldType.DATE_LIST,
                    Constants.RealmFieldType.FLOAT_LIST,
                    Constants.RealmFieldType.DECIMAL128_LIST,
                    Constants.RealmFieldType.OBJECT_ID_LIST,
                    Constants.RealmFieldType.UUID_LIST,
                    Constants.RealmFieldType.MIXED_LIST,
                    Constants.RealmFieldType.DOUBLE_LIST -> {
                        val requiredFlag = if (metadata.isElementNullable(field)) "!Property.REQUIRED" else "Property.REQUIRED"
                        //emitStatement("builder.addPersistedValueListProperty(%s, \"%s\", %s, %s)", publicFieldName, internalFieldName, fieldType.realmType, requiredFlag)
                    }
                    Constants.RealmFieldType.BACKLINK -> {
                        throw IllegalArgumentException("LinkingObject field should not be added to metadata")
                    }
                    Constants.RealmFieldType.INTEGER,
                    Constants.RealmFieldType.FLOAT,
                    Constants.RealmFieldType.DOUBLE,
                    Constants.RealmFieldType.BOOLEAN,
                    Constants.RealmFieldType.STRING,
                    Constants.RealmFieldType.DATE,
                    Constants.RealmFieldType.BINARY,
                    Constants.RealmFieldType.DECIMAL128,
                    Constants.RealmFieldType.OBJECT_ID,
                    Constants.RealmFieldType.UUID,
                    Constants.RealmFieldType.MIXED,
                    Constants.RealmFieldType.REALM_INTEGER -> {
                        val nullableFlag = (if (metadata.isNullable(field)) "!" else "") + "Property.REQUIRED"
                        val indexedFlag = (if (metadata.isIndexed(field)) "" else "!") + "Property.INDEXED"
                        val primaryKeyFlag = (if (metadata.isPrimaryKey(field)) "" else "!") + "Property.PRIMARY_KEY"
                        val initExpression = String.format("new RealmColumn(%s, \"%s\", %s, %s, %s, %s)",
                            publicFieldName, internalFieldName, fieldType.realmType, primaryKeyFlag, indexedFlag, nullableFlag)
                        emitField("RealmColumn", internalFieldName, EnumSet.of(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL), initExpression)
                        // emitStatement("builder.addPersistedProperty(%s, \"%s\", %s, %s, %s, %s)", publicFieldName, internalFieldName, fieldType.realmType, primaryKeyFlag, indexedFlag, nullableFlag)
                    }
                    Constants.RealmFieldType.STRING_TO_BOOLEAN_MAP,
                    Constants.RealmFieldType.STRING_TO_STRING_MAP,
                    Constants.RealmFieldType.STRING_TO_INTEGER_MAP,
                    Constants.RealmFieldType.STRING_TO_FLOAT_MAP,
                    Constants.RealmFieldType.STRING_TO_DOUBLE_MAP,
                    Constants.RealmFieldType.STRING_TO_BINARY_MAP,
                    Constants.RealmFieldType.STRING_TO_DATE_MAP,
                    Constants.RealmFieldType.STRING_TO_DECIMAL128_MAP,
                    Constants.RealmFieldType.STRING_TO_OBJECT_ID_MAP,
                    Constants.RealmFieldType.STRING_TO_UUID_MAP,
                    Constants.RealmFieldType.STRING_TO_MIXED_MAP -> {
                        val valueNullable = metadata.isDictionaryValueNullable(field)
                        val requiredFlag = if (valueNullable) "!Property.REQUIRED" else "Property.REQUIRED"
                        //emitStatement("builder.addPersistedMapProperty(%s, \"%s\", %s, %s)", publicFieldName, internalFieldName, fieldType.realmType, requiredFlag)
                    }
                    Constants.RealmFieldType.STRING_TO_LINK_MAP -> {
                        val genericTypeQualifiedName = Utils.getGenericTypeQualifiedName(field)
                        val internalClassName = Utils.getReferencedTypeInternalClassNameStatement(genericTypeQualifiedName, classCollection)
                        //emitStatement("builder.addPersistedLinkProperty(%s, \"%s\", RealmFieldType.STRING_TO_LINK_MAP, %s)", publicFieldName, internalFieldName, internalClassName)
                    }
                    Constants.RealmFieldType.BOOLEAN_SET,
                    Constants.RealmFieldType.STRING_SET,
                    Constants.RealmFieldType.INTEGER_SET,
                    Constants.RealmFieldType.FLOAT_SET,
                    Constants.RealmFieldType.DOUBLE_SET,
                    Constants.RealmFieldType.BINARY_SET,
                    Constants.RealmFieldType.DATE_SET,
                    Constants.RealmFieldType.DECIMAL128_SET,
                    Constants.RealmFieldType.OBJECT_ID_SET,
                    Constants.RealmFieldType.UUID_SET,
                    Constants.RealmFieldType.MIXED_SET -> {
                        val valueNullable = metadata.isSetValueNullable(field)
                        val requiredFlag = if (valueNullable) "!Property.REQUIRED" else "Property.REQUIRED"
                        //emitStatement("builder.addPersistedSetProperty(%s, \"%s\", %s, %s)", publicFieldName, internalFieldName, fieldType.realmType, requiredFlag)
                    }
                    Constants.RealmFieldType.LINK_SET -> {
                        val genericTypeQualifiedName = Utils.getGenericTypeQualifiedName(field)
                        val internalClassName = Utils.getReferencedTypeInternalClassNameStatement(genericTypeQualifiedName, classCollection)
                       // emitStatement("builder.addPersistedLinkProperty(${publicFieldName}, \"${internalFieldName}\", RealmFieldType.LINK_SET, ${internalClassName})")
                    }
                }
            }
            for (backlink in metadata.backlinkFields) {
                // Backlinks can only be created between classes in the current round of annotation processing
                // as the forward link cannot be created unless you know the type already.
                val sourceClass = classCollection.getClassFromQualifiedName(backlink.sourceClass!!)
                val targetField = backlink.targetField // Only in the model, so no internal name exists
                val internalSourceField = sourceClass.getInternalFieldName(backlink.sourceField!!)
                //emitStatement("""builder.addComputedLinkProperty("%s", "%s", "%s")""", targetField, sourceClass.internalClassName, internalSourceField)
            }

            emitEmptyLine()
        }
    }

    private fun getRealmTypeChecked(field: VariableElement): Constants.RealmFieldType {
        val type = getRealmType(field)
        if (type === Constants.RealmFieldType.NOTYPE) {
            throw IllegalStateException("Unsupported type " + field.asType().toString())
        }
        return type
    }
    private fun getRealmType(field: VariableElement): Constants.RealmFieldType {
        val fieldTypeCanonicalName: String = field.asType().toString()
        val type: Constants.RealmFieldType? = Constants.JAVA_TO_REALM_TYPES[fieldTypeCanonicalName]
        if (type != null) {
            return type
        }
        if (Utils.isMutableRealmInteger(field)) {
            return Constants.RealmFieldType.REALM_INTEGER
        }
        if (Utils.isRealmAny(field)){
            return Constants.RealmFieldType.MIXED
        }
        if (Utils.isRealmModel(field)) {
            return Constants.RealmFieldType.OBJECT
        }
        if (Utils.isRealmModelList(field)) {
            return Constants.RealmFieldType.LIST
        }
        if (Utils.isRealmValueList(field) || Utils.isRealmAnyList(field)) {
            return Utils.getValueListFieldType(field)
        }
        if (Utils.isRealmModelDictionary(field)) {
            return Constants.RealmFieldType.STRING_TO_LINK_MAP
        }
        if (Utils.isRealmDictionary(field)) {
            return Utils.getValueDictionaryFieldType(field)
        }
        if (Utils.isRealmModelSet(field)) {
            return Constants.RealmFieldType.LINK_SET
        }
        if (Utils.isRealmSet(field)) {
            return Utils.getValueSetFieldType(field)
        }
        return Constants.RealmFieldType.NOTYPE
    }
}