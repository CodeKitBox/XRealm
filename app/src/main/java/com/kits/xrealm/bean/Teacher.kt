package com.kits.xrealm.bean

import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

@RealmClass(value = "MyGoodTeacher",name = "MyTeacher")
open class Teacher : RealmObject(){
    @PrimaryKey
    var id:Long? = UUID.randomUUID().mostSignificantBits
    var name:String = ""
    var job:String? = null
    @Index
    var data:Int = 100
    var simple:String? = null
    // 属性不能是val
    // val testStr:String = "100"
    companion object{
        var testA = 200
        fun add(){

        }
    }
}