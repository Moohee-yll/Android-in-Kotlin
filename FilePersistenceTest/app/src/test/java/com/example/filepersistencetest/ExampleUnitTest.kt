package com.example.filepersistencetest

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
    fun Tester(){
        val split = "00101011100"
        val b = Integer.parseInt(split.substring(0,4),2)
        println(b)
    }
    @Test
    fun printByte(){
        val s = "01010100"
        val byteArray = s.toByteArray()
        for (i in byteArray){
            println(Integer.toHexString(i.toInt()))
        }
    }


    @Test
    fun test(){
        val s = "bjkbjb啊啊啊".toByteArray()
        println(s.decodeToString())
    }
}