package com.kits.xrealm.bean

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmExField
import io.realm.ex.InitDefVal
import java.util.*

open class School : RealmObject(){
    @RealmExField(defValue = SchoolIdDefVal::class)
    @PrimaryKey
    var schoolId:String? = null
    var name:String = ""
}

class SchoolIdDefVal : InitDefVal<String> {
    override fun defVal(): String {
        val ret = UUID.randomUUID().mostSignificantBits.toString()
        println("def val == $ret")
        return ret
    }

}