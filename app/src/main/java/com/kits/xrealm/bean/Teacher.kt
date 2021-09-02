package com.kits.xrealm.bean

import io.realm.RealmObject
import io.realm.annotations.*
import io.realm.ex.InitDefVal

import java.util.*

open class Teacher : RealmObject(){
//    @PrimaryKey
//    var pKey:String=UUID.randomUUID().mostSignificantBits.toString()
    @PrimaryKey
    var id:Long? = UUID.randomUUID().mostSignificantBits
    var name:String = ""
    @Index
    private var test5:String? = null
    // 这是版本3
    var test1:String ="test1"
    var test2:String = "test2"
    @RealmExField(defValue = PKeyDefVal::class)
    var test3:String?= null
    @Required
    @Index
    var test4:String = "test4"

    companion object{
        var testA = 200
        fun add(){

        }
    }


}

class PKeyDefVal : InitDefVal<String> {
    override fun defVal(): String {
        val ret = UUID.randomUUID().mostSignificantBits.toString()
        println("def val == $ret")
        return ret
    }

}