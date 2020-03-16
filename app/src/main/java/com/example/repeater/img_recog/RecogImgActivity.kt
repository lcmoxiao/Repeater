package com.example.repeater.img_recog

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.repeater.R
import kotlinx.android.synthetic.main.activity_recog.*
import java.io.InputStream

class RecogImgActivity : AppCompatActivity(R.layout.activity_recog) {

    private var img1: Bitmap? = null
    private var img2: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        imgRecognition()
    }

    private fun imgRecognition() {
        UseOpencv.staticLoadCVLibraries()
        recog_btn1!!.setOnClickListener { selectImage(1) }
        recog_btn2!!.setOnClickListener { selectImage(2) }
        process_btn!!.setOnClickListener {
            UseOpencv.hashCompare(img1, img2)
            UseOpencv.newCompare(img1, img2)
            UseOpencv.diffCompare(img1, img2)
        }
    }

    private fun selectImage(requestCode: Int) {
        startActivityForResult(
            Intent.createChooser(
                Intent().setType("image/*")
                    .setAction(Intent.ACTION_GET_CONTENT), "选择图像..."
            ), requestCode
        )
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == 1 || requestCode == 2) && resultCode == RESULT_OK && data != null && data.data != null) {
            val uri = data.data
            try {
                val input: InputStream? = uri?.let { contentResolver.openInputStream(it) }
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapFactory.decodeStream(input, null, options)
                options.inSampleSize = 2
                options.inJustDecodeBounds = false
                options.inPreferredConfig = Bitmap.Config.ARGB_8888
                if (requestCode == 2) {
                    img2 = BitmapFactory.decodeStream(
                        uri?.let { contentResolver.openInputStream(it) }, null, options
                    )
                    imageView2!!.setImageBitmap(img2)
                } else {
                    img1 = BitmapFactory.decodeStream(
                        uri?.let { contentResolver.openInputStream(it) }, null, options
                    )
                    imageView1!!.setImageBitmap(img1)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}