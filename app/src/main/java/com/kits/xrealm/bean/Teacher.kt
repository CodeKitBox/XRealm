package com.kits.xrealm.bean

import io.realm.RealmObject
import io.realm.annotations.*
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
    var test1:String? = null
    @Index
    var test2:String = "myTest2"
    @Index
    @RealmField(name="TeacherStu")
    var stTest1:String = "stTest1"
    // 属性不能是val
    // val testStr:String = "100"
    companion object{
        var testA = 200
        fun add(){

        }
    }
}