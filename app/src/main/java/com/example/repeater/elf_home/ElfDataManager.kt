package com.example.repeater.elf_home

import com.example.repeater.MainActivity.Companion.sqliteManager
import java.util.*

/*
这里仅仅只是列表行而已，MOV_ID，MOV_NUM
 */
class ElfDataManager {
    var mList: ArrayList<ElfData> = ArrayList<ElfData>()

    //更新列表行
    fun update() {
        val list: ArrayList<ElfData> = ArrayList<ElfData>()
        val dataSize = sqliteManager.getSize()
        for (i in 1..dataSize)
            list.add(ElfData(sqliteManager.getMovSize(i), i))
        mList = list
    }

    init {
        update()
    }
}