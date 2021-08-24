package com.kits.xrealm

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.kits.xrealm.bean.Teacher
//import com.kits.xrealm.bean.build.TeacherDao
import io.realm.FieldAttribute
import io.realm.Realm
import io.realm.annotations.PrimaryKey


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnAdd).setOnClickListener {
            addTeacher()
        }

        findViewById<Button>(R.id.btnQuery).setOnClickListener {
            queryTeacher()

        }

        findViewById<Button>(R.id.btnTest).setOnClickListener {
            enumTest()
            bitTest()
            val realm = Realm.getDefaultInstance()
            val studentSchema = realm.schema.get("Student");
            val ret1 = studentSchema?.hasField("name")
            println("ret1 = $ret1")
            val list = realm.sharedRealm.configuration.realmObjectClasses
            list.forEach {
                clazz ->
                println("simpleName == ${clazz.simpleName}")
                val table = realm.schema.get(clazz.simpleName)
                println("table == $table")
                // 获取 类的所有属性
                val fields = clazz.declaredFields
                fields.forEach {
                    println("属性名  ${it.name}")
                    println("属性的类型 ${it.type}")
                    val ret = it.isAnnotationPresent(PrimaryKey::class.java)
                    println("是否有注解  $ret")
                    it.declaredAnnotations.forEach {
                        annotation ->
                            println("属性上的注解  $annotation")
                    }
                }
            }

        }

        findViewById<Button>(R.id.btnDb).setOnClickListener {
            val realm = Realm.getDefaultInstance()
            realm.schema.get("Teacher")?.let {
                val col = "data"
                println("存在 ${it.hasField(col)}")
                if(it.hasField(col)){
                    println("主键 ${it.isPrimaryKey(col)}")
                    println("索引 ${it.hasIndex(col)}")
                    println("必要 ${it.isRequired(col)}")
                }
            }

        }
    }

    private fun enumTest(){
        FieldAttribute.values().forEach {
            println("it == $it")
        }
    }

    private fun bitTest(){
        val a = 1
        val b = 2
        val c = a.or(b)

        println("c == $c")

    }

    private fun addTeacher(){
        for (i in 0..10){
            val teacher = Teacher()
            teacher.name = "name $i"
            teacher.job = "job $i"

            val realm = Realm.getDefaultInstance()
            realm.beginTransaction()
            realm.insert(teacher)
            realm.commitTransaction()
        }
    }

    private fun queryTeacher(){
        val list = Realm.getDefaultInstance().where(Teacher::class.java).findAll()
        list.forEach {
            println("item $it")
        }
    }

    fun nullTest(){
        val x: String? = "11"
        if( x is String){
            println("aaa")
        }else{
            println("bbbb")
        }
    }
}