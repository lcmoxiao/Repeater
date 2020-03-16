package com.example.repeater.elf_home

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.IOException


/*
数据库的名字是"elf_db"
存储的结构为一个MOV一个table
每个MOV的行数据结构(int id,int type,int x1,int y1,int x2,int y2,blob img)
 */

class ElfSqliteManager(context: Context?) :
    SQLiteOpenHelper(context, "elf_db", null, 1) {
    lateinit var downXs: IntArray
    lateinit var downYs: IntArray
    lateinit var xs: IntArray
    lateinit var ys: IntArray
    lateinit var types: IntArray
    lateinit var bitmaps: Array<Bitmap?>

    //获取的是这个MOV的所有的信息数据
    fun getDataById(MovId: Int) {
        val db = this.writableDatabase
        val cursor = db.query(
            "MOV$MovId",
            arrayOf("type", "x1", "y1", "x2", "y2", "img"),
            null,
            null,
            null,
            null,
            null
        )
        val size = cursor.count
        if (size == 0) return
        xs = IntArray(size)
        ys = IntArray(size)
        downXs = IntArray(size)
        downYs = IntArray(size)
        types = IntArray(size)
        bitmaps = arrayOfNulls(size)
        for (id in 0 until size) {
            types[id] = cursor.getInt(0)
            downXs[id] = cursor.getInt(1)
            downYs[id] = cursor.getInt(2)
            if (types[id] == 2) { //滑动手势
                xs[id] = cursor.getInt(3)
                ys[id] = cursor.getInt(4)
            } else {
                xs[id] = -1
                ys[id] = -1
            }
            val imgData = cursor.getBlob(5)
            bitmaps[id] =
                BitmapFactory.decodeByteArray(imgData, 0, imgData.size)
            cursor.moveToNext()
        }
        db.close()
        cursor.close()
    }

    //获取所有表的数量即获取MOV的数量
    fun getSize():Int{
        val cursor = readableDatabase.rawQuery("select count(*) from sqlite_master where type='table'", null)
        var n = 0
        if (cursor.moveToNext()) {
            val count = cursor.getInt(0) - 1
            if (count > 0) {
                n = count
            } else {
                onCreate(readableDatabase)
            }
        }
        cursor.close()
        readableDatabase.close()
        return n
    }

    //获取MOV的动作数
    fun getMovSize(MovNub: Int): Int {
        val db = this.readableDatabase
        val cursor = db.query(
            "MOV$MovNub",
            arrayOf("type", "x1", "y1", "x2", "y2", "img"),
            null,
            null,
            null,
            null,
            null
        )
        val size = cursor.count
        cursor.close()
        db.close()
        return size
    }

    fun createTable(MovNub: Int) {
        val db = this.writableDatabase
        if (isNotLivingTable(MovNub, db)) {
            val sql =
                "create table MOV$MovNub(type integer,x1 integer ,y1 integer,x2 integer,y2 integer,img Blob)"
            db.execSQL(sql)
        }
        db.close()
    }

    fun deleteTable(MovNub: Int) {
        val db = this.writableDatabase
        if (!isNotLivingTable(MovNub, db)) {
            val sql = "drop table MOV$MovNub"
            db.execSQL(sql)
        }
        db.close()
    }

    fun DeleteAll(MovNub: Int) {
        val db = this.writableDatabase
        if (!isNotLivingTable(MovNub, db)) {
            val sql = "delete from MOV$MovNub"
            db.execSQL(sql)
        }
        db.close()
    }

    fun add(MovNub: Int, type: Int, x1: Int, y1: Int, img: Bitmap?) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("type", type)
        values.put("x1", x1)
        values.put("y1", y1)
        values.put("x2", -1)
        values.put("y2", -1)
        values.put("img", bitmapToBytes(img))
        db.insert("MOV$MovNub", null, values)
        db.close()
    }

    fun add(MovNub: Int, type: Int,x1: Int, y1: Int, x2: Int, y2: Int, img: Bitmap?) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put("type", type)
        values.put("x1", x1)
        values.put("y1", y1)
        values.put("x2", x2)
        values.put("y2", y2)
        values.put("img", bitmapToBytes(img))
        db.insert("MOV$MovNub", null, values)
        db.close()
    }

    private fun isNotLivingTable(
        MovNub: Int,
        db: SQLiteDatabase
    ): Boolean {
        var b = false
        val sql =
            "select count(*) as c from sqlite_master where type ='table' and name ='MOV$MovNub';"
        val cursor = db.rawQuery(sql, null)
        if (cursor.moveToNext()) {
            val count = cursor.getInt(0)
            if (count > 0) {
                b = true
                Log.i("info", "MOV" + MovNub + "数据表已经存在")
            } else {
                onCreate(db)
            }
        }
        cursor.close()
        return !b
    }

    //图片转为二进制数据
    private fun bitmapToBytes(bitmap: Bitmap?): ByteArray {
        if (bitmap == null) return ByteArray(0)
        //将图片转化为位图
        val size = bitmap.width * bitmap.height * 4
        //创建一个字节数组输出流,流的大小为size
        val baos = ByteArrayOutputStream(size)
        try {
            //设置位图的压缩格式，质量为100%，并放入字节数组输出流中
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            //将字节数组输出流转化为字节数组byte[]
            return baos.toByteArray()
        } catch (ignored: Exception) {
        } finally {
            try {
                bitmap.recycle()
                baos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return ByteArray(0)
    }

    override fun onCreate(db: SQLiteDatabase) {}
    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
    }
}