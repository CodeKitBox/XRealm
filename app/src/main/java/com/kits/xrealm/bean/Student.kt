package com.kits.xrealm.bean

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class Student : RealmObject(){
    @PrimaryKey
    var id:Long? = UUID.randomUUID().mostSignificantBits
    var name:String = ""
}