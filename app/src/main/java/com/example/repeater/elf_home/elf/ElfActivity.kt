package com.example.repeater.elf_home.elf

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.repeater.R
import kotlinx.android.synthetic.main.activity_core.*


class ElfActivity : AppCompatActivity(R.layout.activity_core) {

    // 是否打开操作悬浮窗
    var isrFloating = false
    var issFloating = false
    private val nowMovId: Int = this.intent.extras!!.get("MOV_ID") as Int
    lateinit var recordServiceControl: RecordService.MyBinder
    private lateinit var recordServiceConnection: RecordServiceConnection
    lateinit var simulateServiceControl: SimulateService.MyBinder
    lateinit var simulateServiceConnection: SimulateServiceConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initClick()
        startScreenRecord()
        screenBaseInfo
    }

    /**
     * 获取屏幕基本信息
     */
    private val screenBaseInfo: Unit
        get() {
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            mScreenWidth = metrics.widthPixels
            mScreenHeight = metrics.heightPixels
            mScreenDensity = metrics.densityDpi
        }

    private var mScreenWidth = 0
    private var mScreenHeight = 0
    private var mScreenDensity = 0
    private fun initClick() {
        core_btn1!!.setOnClickListener {
            val intent = Intent(this, RecordService::class.java)
            recordServiceConnection = RecordServiceConnection()
            if (isrFloating) {
                recordServiceControl.killService()
            } else {
                bindService(intent, recordServiceConnection, BIND_AUTO_CREATE)
            }
        }
        core_btn2!!.setOnClickListener {
            simulateServiceConnection = SimulateServiceConnection()
            if (issFloating) {
                simulateServiceControl.killService()
            } else {
                bindService(intent, simulateServiceConnection, BIND_AUTO_CREATE)
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000) {
            if (resultCode == RESULT_OK) {
                //获得录屏权限，启动Service进行录制
                val intent = Intent(this, ShotService::class.java)
                intent.putExtra("resultCode", resultCode)
                intent.putExtra("resultData", data)
                intent.putExtra("mScreenWidth", mScreenWidth)
                intent.putExtra("mScreenHeight", mScreenHeight)
                intent.putExtra("mScreenDensity", mScreenDensity)
                startService(intent)
                //Toast.makeText(this, "成功开启服务", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "服务开启失败,无法截屏，退出", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    //start screen record
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun startScreenRecord() {
        //Manages the retrieval of certain types of MediaProjection tokens.
        val mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        //Returns an Intent that must passed to startActivityForResult() in order to start screen capture.
        val permissionIntent = mediaProjectionManager.createScreenCaptureIntent()
        startActivityForResult(permissionIntent, 1000)
    }

    //stop screen record.
    private fun stopScreenRecord() {
        val service = Intent(this, ShotService::class.java)
        stopService(service)
    }

    override fun onDestroy() {
        stopScreenRecord()
        super.onDestroy()
    }


    inner class RecordServiceConnection : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            isrFloating = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            isrFloating = true
            recordServiceControl = service as RecordService.MyBinder
            Log.i("xx", "bind RecordService   $name")
            recordServiceControl.setNowMovId(nowMovId)
        }
    }

    inner class SimulateServiceConnection : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            issFloating = false
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            issFloating = true
            simulateServiceControl = service as SimulateService.MyBinder
            Log.i("xx", "bind SimulateService   $name")
            simulateServiceControl.setNowMovId(nowMovId)
        }
    }


}