package com.example.repeater.elf_home.elf

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import com.example.repeater.MainActivity.Companion.sqliteManager
import com.example.repeater.R
import com.example.repeater.RootShell.RootShell
import com.example.repeater.RootShell.execution.Command
import com.example.repeater.elf_home.elf.ShotService.Companion.startCapture
import java.util.*


class RecordService : Service() {
    //绑定的图片
    private var floatImg: ImageView? = null
    private var nowMovNub: Int = 0

    //实例化的WindowManager.
    private var windowManager: WindowManager? = null
    private var motivationNub = 0

    //预留截屏
    private lateinit var screenShot: Bitmap

    // 是否在录制
    var isRecording = false

    //悬浮窗的参数
    private var floatParams: WindowManager.LayoutParams? = null
    override fun onCreate() {
        super.onCreate()
        try {
            initFloatParams()
            initWindow()
            createToucher()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.i("info", "rec binded")
        return MyBinder()
    }

    inner class MyBinder : Binder() {
        fun setNowMovId(movNub: Int) {
            nowMovNub = movNub
        }

        fun killService() {
            killService()
            stopSelf()
        }
    }

    private fun initWindow() {
        windowManager = application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        floatImg = ImageView(this)
        floatImg!!.setImageResource(R.color.colorRecordingWait)
        windowManager!!.addView(floatImg, floatParams)
    }

    private fun updateViewToExecute() {
        floatImg!!.setImageResource(R.color.colorExecuting)
        windowManager!!.updateViewLayout(floatImg, floatParams)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createToucher() {
        floatImg!!.setOnTouchListener { _, event ->
            val action = event.action
            if (action == MotionEvent.ACTION_UP) {
                if (!isRecording) {
                    //点击后开始录制
                    updateViewToExecute()
                    isRecording = true
                    GetEventThread().start()
                    Toast.makeText(baseContext, "准备就绪", Toast.LENGTH_SHORT).show()
                } else {
                    isRecording = false
                    stopSelf()
                    Toast.makeText(baseContext, "录制结束", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }
    }

    inner class GetEventThread : Thread() {
        private val getLines = ArrayList<String>()
        private val clickPos = intArrayOf(-1, -1, -1, -1)
        private var canShot = true
        override fun run() {
            try {
                val command: Command = object : Command(0, false, "getevent /dev/input/event3") {
                    override fun commandOutput(id: Int, line: String) {
                        if (isRecording) {
                            getLines.add(line)
                            if (getLines.size >= 10) {
                                clickPos[0] = getLines[3].split(" ").toTypedArray()[2].toInt(16)
                                clickPos[1] = getLines[4].split(" ").toTypedArray()[2].toInt(16)
                                if (canShot) {
                                    canShot = false
                                    screenShot = startCapture()
                                }
                                if (getLines[6].split(" ").toTypedArray()[2] == "ffffffff") {
                                    Log.e("xxx", "录制了一次点击")
                                    sqliteManager.add(
                                        sqliteManager.getMovSize(nowMovNub),
                                        1,
                                        clickPos[0],
                                        clickPos[1],
                                        -1,
                                        -1,
                                        screenShot
                                    )
                                    motivationNub++
                                    getLines.clear()
                                    canShot = true
                                } else if (getLines[getLines.size - 4].split(" ")
                                        .toTypedArray()[2] == "ffffffff"
                                ) {
                                    clickPos[2] =
                                        getLines[getLines.size - 7].split(" ").toTypedArray()[2]
                                            .toInt(16)
                                    clickPos[3] =
                                        getLines[getLines.size - 6].split(" ").toTypedArray()[2]
                                            .toInt(16)
                                    Log.e("xxx", "录制了一次滑动")
                                    sqliteManager.add(
                                        sqliteManager.getMovSize(nowMovNub),
                                        1,
                                        clickPos[0],
                                        clickPos[1],
                                        clickPos[2],
                                        clickPos[3],
                                        screenShot
                                    )
                                    motivationNub++
                                    getLines.clear()
                                    canShot = true
                                }
                                super.commandOutput(id, line)
                            }
                        }
                    }
                }
                //开始getevent
                RootShell.getShell(true).add(command)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun initFloatParams() {
        //赋值WindowManager&LayoutParam.
        floatParams = WindowManager.LayoutParams()
        //设置type.系统提示型窗口，一般都在应用程序窗口之上.
        floatParams!!.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        //设置效果为背景透明.
        floatParams!!.format = PixelFormat.RGBA_8888
        //设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
        floatParams!!.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        //设置窗口初始停靠位置.
        floatParams!!.gravity = Gravity.TOP or Gravity.START
        floatParams!!.x = 980
        floatParams!!.y = 1024
        //设置悬浮窗口长宽数据.
        floatParams!!.width = 100 // 设置悬浮窗口长宽数据
        floatParams!!.height = 100
    }

    override fun onDestroy() {
        floatImg!!.isEnabled = false
        windowManager!!.removeView(floatImg)
        super.onDestroy()
    }

}