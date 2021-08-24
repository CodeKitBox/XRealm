# Realm 简要
## Realm 支持的数据类型

Realm 支持的数据类型，主要收到注解解析的影响，如果注解解析失败，则编译失败，不能正常的运行。通过类RealmProxyClassGenerator的方法 emitPersistedFieldAccessors。具体源码如下

```kotlin
    @Throws(IOException::class)
    private fun emitPersistedFieldAccessors(writer: JavaWriter) {
        for (field in metadata.fields) {
            println("emitPersistedFieldAccessors  fieldName = ${field.simpleName.toString()}")
            val fieldName = field.simpleName.toString()
            val fieldTypeCanonicalName = field.asType().toString()
            when {
                Constants.JAVA_TO_REALM_TYPES.containsKey(fieldTypeCanonicalName) -> emitPrimitiveType(writer, field, fieldName, fieldTypeCanonicalName)
                // 支持得数据类型 MutableRealmInteger
                // 示例 MutableRealmInteger test3 = MutableRealmInteger.valueOf(0);
                Utils.isMutableRealmInteger(field) -> emitMutableRealmInteger(writer, field, fieldName, fieldTypeCanonicalName)
                // 支持数据类 RealmAny
                Utils.isRealmAny(field) -> emitRealmAny(writer, field, fieldName, fieldTypeCanonicalName)
                // 支持数据类 RealmModel 这个是接口，暂时不知道怎么使用
                Utils.isRealmModel(field) -> emitRealmModel(writer, field, fieldName, fieldTypeCanonicalName)
                // 支持 RealmList
                Utils.isRealmList(field) -> {
                    val elementTypeMirror = TypeMirrors.getRealmListElementTypeMirror(field)
                    emitRealmList(writer, field, fieldName, fieldTypeCanonicalName, elementTypeMirror)
                }
                Utils.isRealmDictionary(field) -> {
                    val valueTypeMirror = TypeMirrors.getRealmDictionaryElementTypeMirror(field)
                    emitRealmDictionary(writer, field, fieldName, fieldTypeCanonicalName, requireNotNull(valueTypeMirror))
                }
                Utils.isRealmSet(field) -> {
                    val valueTypeMirror = TypeMirrors.getRealmSetElementTypeMirror(field)
                    emitRealmSet(writer, field, fieldName, fieldTypeCanonicalName, requireNotNull(valueTypeMirror))
                }
                else -> throw UnsupportedOperationException(String.format(Locale.US, "Field \"%s\" of type \"%s\" is not supported.", fieldName, fieldTypeCanonicalName))
            }
            writer.emitEmptyLine()
        }
    }
```

支持得数据类型

```kotlin
    val JAVA_TO_REALM_TYPES = mapOf("byte" to RealmFieldType.INTEGER,
            "short" to RealmFieldType.INTEGER,
            "int" to RealmFieldType.INTEGER,
            "long" to RealmFieldType.INTEGER,
            "float" to RealmFieldType.FLOAT,
            "double" to RealmFieldType.DOUBLE,
            "boolean" to RealmFieldType.BOOLEAN,
            "java.lang.Byte" to RealmFieldType.INTEGER,
            "java.lang.Short" to RealmFieldType.INTEGER,
            "java.lang.Integer" to RealmFieldType.INTEGER,
            "java.lang.Long" to RealmFieldType.INTEGER,
            "java.lang.Float" to RealmFieldType.FLOAT,
            "java.lang.Double" to RealmFieldType.DOUBLE,
            "java.lang.Boolean" to RealmFieldType.BOOLEAN,
            "java.lang.String" to RealmFieldType.STRING,
            "java.util.Date" to RealmFieldType.DATE,
            "byte[]" to RealmFieldType.BINARY,
            "org.bson.types.Decimal128" to RealmFieldType.DECIMAL128,
            "org.bson.types.ObjectId" to RealmFieldType.OBJECT_ID,
            "java.util.UUID" to RealmFieldType.UUID
    )
```

