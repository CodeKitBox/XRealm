package com.kits.xrealm

import org.junit.Test

import org.junit.Assert.*
import kotlin.reflect.typeOf

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
    @Test
    fun bitTest(){
        val a = 0b01
        val b = 0b10
        val c = a.or(b)
        println("a == $a")
        println("b == $b")
        println("c == $c")
    }
    @Test
    fun nullTest(){
        val x: String = "11"
        if( x is String?){
            println("aaa")
        }else{
            println("bbbb")
        }
    }
}