package com.example.repeater.elf_home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.repeater.R
import androidx.recyclerview.widget.RecyclerView


class ElfHomeActivity : AppCompatActivity(R.layout.activity_depository) {
    //主页中的RecycleView
    private var mRecycleView: RecyclerView? = null
    private var dataManager: ElfDataManager? = null
    private var mAdapter: ElfHomeAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initRecycleView()
    }


    //初始化界面
    private fun initRecycleView() {
        dataManager = ElfDataManager()
        mRecycleView!!.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        //设置适配器adapter
        mAdapter = ElfHomeAdapter(dataManager!!, this)
        mRecycleView!!.adapter = mAdapter
    }

    override fun onResume() {
        super.onResume()
        dataManager?.update()
        mAdapter?.notifyDataSetChanged()
    }


}