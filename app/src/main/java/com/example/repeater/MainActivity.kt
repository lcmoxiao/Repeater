package com.example.repeater

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.repeater.elf_home.ElfHomeActivity
import com.example.repeater.elf_home.ElfSqliteManager
import com.example.repeater.img_recog.RecogImgActivity
import com.example.repeater.img_recog.UseOpencv
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    companion object{
        lateinit var sqliteManager: ElfSqliteManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initClick()
        initPermission()
        UseOpencv.staticLoadCVLibraries()
        sqliteManager =
            ElfSqliteManager(baseContext)
    }

    private fun initClick() {
        main_btn1!!.setOnClickListener {
            startActivity(
                Intent(this, RecogImgActivity::class.java)
            )
        }

        main_btn2!!.setOnClickListener {
            sqliteManager.createTable(1)
            sqliteManager.add(1, 1, 1, 1, null)
            sqliteManager.add(1, 2, 1, 1, 1, 1, null)
            sqliteManager.clearAll(1)
        }



        main_btn3!!.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    ElfHomeActivity::class.java
                )
            )
        }
    }

    // 初始化权限
    private fun initPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(applicationContext, "麻烦手动开启一下悬浮窗权限 ", Toast.LENGTH_SHORT).show()
            finish()
        }
        //读写权限检查
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                67
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == 67) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    applicationContext,
                    "授权成功",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    applicationContext,
                    "不给权限拉倒",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sqliteManager.close()
        Log.i("info", "Activity销毁")
    }
}