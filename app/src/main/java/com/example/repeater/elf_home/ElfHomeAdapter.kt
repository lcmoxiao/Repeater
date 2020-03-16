package com.example.repeater.elf_home

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.repeater.Common_resources
import com.example.repeater.R
import com.example.repeater.RootShell.RootShell
import com.example.repeater.elf_home.elf.ElfActivity

/*
这里的Adapter，
展示的只是数据库的MOV_ID,MOV_NUM
有着增删的功能，
可以点击进入进行录制和模拟操作
 */
class ElfHomeAdapter internal constructor(
    private val elfDataManager: ElfDataManager,
    private val mContext: Context
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {

    companion object{
        const val IS_BOTTOM = 1
        const val IS_NOT_BOTTOM = 0
    }

    //自定义Holder
    internal inner class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var length: TextView = itemView.findViewById(R.id.length)
        var desc: TextView = itemView.findViewById(R.id.desc)
        var enterBtn: Button = itemView.findViewById(R.id.enter)
    }

    //自定义Holder
    internal inner class ButtonHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var addBtn: Button = itemView.findViewById(R.id.newelf)
        var killBtn: Button = itemView.findViewById(R.id.killelf)
    }

    //自定义Holder
    internal inner class NullHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            0 -> {
                val view: View =
                    LayoutInflater.from(parent.context).inflate(R.layout.item_alf, parent, false)
                return ItemHolder(view)
            }
            1 -> {
                val view: View =
                    LayoutInflater.from(parent.context).inflate(R.layout.item_elfmag, parent, false)
                return ButtonHolder(view)
            }
        }
        return NullHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_elfmag, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            IS_NOT_BOTTOM -> {
                val itemHolder =
                    holder as ItemHolder
                itemHolder.length.text = elfDataManager.mList[position].length
                itemHolder.desc.text = elfDataManager.mList[position].desc
                itemHolder.enterBtn.setOnClickListener {
                    if (!RootShell.isRootAvailable()) Toast.makeText(
                        mContext, "没ROOT你用不了,放弃吧", Toast.LENGTH_SHORT
                    ).show() else {
                        val intent = Intent(mContext, ElfActivity::class.java)
                        intent.putExtra("MOV_ID",position + 1)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                        mContext.startActivity(intent)
                    }
                }
            }
            IS_BOTTOM -> {
                val buttonHolder =
                    holder as ButtonHolder
                buttonHolder.addBtn.setOnClickListener {
                    val movSize = elfDataManager.mList.size + 1
                    Common_resources.initMovFile(movSize)
                    elfDataManager.mList.add(ElfData(0, movSize))
                    notifyDataSetChanged()
                }
                if (position != 0) {
                    buttonHolder.killBtn.setOnClickListener {
                        val movSize = elfDataManager.mList.size
                        Common_resources.delFile(movSize)
                        elfDataManager.mList.removeAt(movSize - 1)
                        notifyDataSetChanged()
                    }
                    buttonHolder.killBtn.visibility = View.VISIBLE
                } else {
                    buttonHolder.killBtn.visibility = View.INVISIBLE
                }
            }
        }
    }


    override fun getItemCount(): Int {
        return elfDataManager.mList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == elfDataManager.mList.size) IS_BOTTOM else IS_NOT_BOTTOM
    }

}