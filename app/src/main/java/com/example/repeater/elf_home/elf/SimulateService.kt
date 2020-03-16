package com.example.repeater.elf_home.elf

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import com.example.repeater.Common_resources
import com.example.repeater.MainActivity.Companion.sqliteManager
import com.example.repeater.R
import com.example.repeater.elf_home.elf.ShotService.Companion.startCapture
import com.example.repeater.img_recog.UseOpencv
import java.io.*


class SimulateService : Service() {
    //绑定的图片
    private var floatImg: ImageView? = null
    //实例化的WindowManager.
    private var windowManager: WindowManager? = null
    private var nowMovNub:Int = 0

    //悬浮窗的参数
    private var floatParams: WindowManager.LayoutParams? = null

    var isSimulating = false
    //读取的动作位置
    var imagesNub = 0
    var isforced = false


    override fun onBind(intent: Intent): IBinder? {
        Log.i("info","sium binded")
        return MyBinder()
    }

    inner class MyBinder : Binder() {
        fun setNowMovId(movNub: Int){
            nowMovNub = movNub
        }
        fun killService()
        {
            killService()
            stopSelf()
        }
    }

    override fun onCreate() {
        super.onCreate()
        initFloatParams()
        try {
            sqliteManager.getDataById(nowMovNub)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        createToucher()
    }

    inner class SimulateThread : Thread() {
        override fun run() {
            super.run()
            //开始模拟
            var motivationNub = 0
            Log.e("xx", "t start with$motivationNub")
            while (motivationNub < imagesNub && isSimulating) {
                sleep(500)
                try {
                    if (UseOpencv.newCompare(startCapture(), sqliteManager.bitmaps[motivationNub])) {
                        Log.e("xx", "找到了图片$motivationNub")
                        if (sqliteManager.types[motivationNub] == 0) {
                            Common_resources.simulateClick(sqliteManager.xs[motivationNub], sqliteManager.ys[motivationNub])
                            Log.e(
                                "xx",
                                "将要点击" + "x:" + sqliteManager.xs[motivationNub] + "y:" + sqliteManager.ys[motivationNub]
                            )
                        } else if (sqliteManager.types[motivationNub] == 1) {
                            Common_resources.simulateSwipe(
                                sqliteManager.downXs[motivationNub],
                                sqliteManager. downYs[motivationNub],
                                sqliteManager. xs[motivationNub],
                                sqliteManager.   ys[motivationNub]
                            )
                            Log.e("xx", "将要滑动")
                        }
                        motivationNub++
                    } else Log.e("xx", "长得不一样和$motivationNub")
                } catch (ignored: Exception) {
                }
            }
            if (!isforced) {
                isSimulating = false
                Log.e("xx", "t end with" + 1)
                Log.e("xx", "t end with" + 2)
                Log.e("xx", "t end with" + 3)
                Looper.prepare()
                Handler(Looper.getMainLooper()).post {
                    updateViewToWait()
                    Log.e("xx", "t end with")
                }
                Log.e("xx", "t end with4")
                Toast.makeText(applicationContext, "正常终止模拟", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updateViewToWait() {
        floatImg!!.setImageResource(R.color.colorSimulateWait)
        windowManager!!.updateViewLayout(floatImg, floatParams)
    }

    private fun updateViewToExecute() {
        floatImg!!.setImageResource(R.color.colorExecuting)
        windowManager!!.updateViewLayout(floatImg, floatParams)
    }

    private fun initFloatWindow() {
        windowManager =
            application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        floatImg = ImageView(this)
        floatImg!!.setImageResource(R.color.colorSimulateWait)
        windowManager!!.addView(floatImg, floatParams)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun createToucher() {
        initFloatWindow()
        floatImg!!.setOnTouchListener(OnTouchListener { _, event ->
            val action = event.action
            if (action == MotionEvent.ACTION_UP) {
                if (!isSimulating) {
                    isSimulating = true
                    isforced = false
                    SimulateThread()
                        .start()
                    updateViewToExecute()
                    Toast.makeText(baseContext, "开始模拟", Toast.LENGTH_SHORT).show()
                } else {
                    isforced = true
                    isSimulating = false
                    updateViewToWait()
                    Toast.makeText(baseContext, "强制终止模拟", Toast.LENGTH_SHORT).show()
                    stopSelf()
                }
                return@OnTouchListener true
            }
            false
        })
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