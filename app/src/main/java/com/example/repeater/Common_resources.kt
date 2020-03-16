package com.example.repeater

import android.os.Environment
import android.util.Log
import java.io.File
import java.io.IOException

object Common_resources {
    //系统目录
    private val rootPath =
        Environment.getExternalStorageDirectory().path + "/"

    //文件目录
    var dataPath = rootPath + "1test/"

    //MOV目录
    var MOVPath = dataPath + "MOV"

    // 刷新目录
    fun initMovFile(MOVnub: Int) {
        try {
            delFile(MOVPath + MOVnub)
            val f1 = File(dataPath)
            if (!f1.exists()) {
                if (!f1.mkdirs()) throw Exception("你创建不了文件夹")
            }
            val f2 = File(MOVPath + MOVnub)
            if (!f2.exists()) {
                if (!f2.mkdirs()) throw Exception("你创建不了文件夹")
            }
            val f3 = File("$MOVPath$MOVnub/images")
            if (!f3.exists()) {
                if (!f3.mkdirs()) throw Exception("你创建不了文件夹")
            }
            val f4 = File("$MOVPath$MOVnub/gesture.txt")
            if (!f4.exists()) {
                if (!f4.createNewFile()) throw Exception("你创建不了文件")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun delFile(file: File) {
        if (!file.exists()) {
            return
        }
        if (file.isDirectory) {
            val files = file.listFiles()
            for (f in files) {
                delFile(f)
            }
        }
        file.delete()
    }

    fun delFile(path: String?): Boolean {
        val file = File(path)
        if (!file.exists()) {
            return false
        }
        if (file.isDirectory) {
            val files = file.listFiles()
            for (f in files) {
                delFile(f)
            }
        }
        return file.delete()
    }

    fun delFile(MOVnub: Int) {
        delFile(MOVPath + MOVnub)
    }

    private fun exec(cmd: String) {
        try {
            val startTime = System.currentTimeMillis()
            val SuProcess = Runtime.getRuntime().exec("su")
            SuProcess.outputStream.write(
                """$cmd
    """.toByteArray()
            )
            SuProcess.outputStream.write("exit\n".toByteArray())
            SuProcess.outputStream.flush()
            SuProcess.waitFor()
            val endTime = System.currentTimeMillis()
            Log.e("xx", "模拟点击花费时间： " + (endTime - startTime) + "ms")
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun simulateClick(x: Int, y: Int) {
        exec("input tap $x $y")
    }

    fun simulateSwipe(downX: Int, downY: Int, x: Int, y: Int) {
        exec("input swipe $downX $downY $x $y 1000")
    }
}