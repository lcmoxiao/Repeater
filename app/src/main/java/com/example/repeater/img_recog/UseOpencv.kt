package com.example.repeater.img_recog

import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.OpenCVLoader
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import kotlin.math.abs

object UseOpencv {
    //OpenCV库静态加载并初始化
    fun staticLoadCVLibraries() {
        val load = OpenCVLoader.initDebug()
        if (load) {
            Log.i("CV", "Open CV Libraries loaded...")
        }
    }

    //像返回true，不像false。
    //核心思想是，大过平均灰度的点和小于平均灰度的点进行划分，判断有多少点不一样。
    fun hashCompare(Bp1: Bitmap?, Bp2: Bitmap?): Boolean {
        val src1 = Mat()
        val dst1 = Mat()
        val src2 = Mat()
        val dst2 = Mat()
        Utils.bitmapToMat(Bp1, src1)
        Utils.bitmapToMat(Bp2, src2)
        Imgproc.cvtColor(src1, dst1, Imgproc.COLOR_BGR2GRAY)
        Imgproc.cvtColor(src2, dst2, Imgproc.COLOR_BGR2GRAY)
        Imgproc.resize(dst1, dst1, Size(8.0, 8.0), 0.0, 0.0, Imgproc.INTER_CUBIC)
        Imgproc.resize(dst2, dst2, Size(8.0, 8.0), 0.0, 0.0, Imgproc.INTER_CUBIC)
        val data1 =
            Array(64) { DoubleArray(1) }
        val data2 =
            Array(64) { DoubleArray(1) }
        var iAvg1 = 0
        var iAvg2 = 0
        val arr1 = DoubleArray(64)
        val arr2 = DoubleArray(64)
        for (i in 0..7) {
            val tmp = i * 8
            for (j in 0..7) {
                val tmp1 = tmp + j
                data1[tmp1] = dst1[i, j]
                data2[tmp1] = dst2[i, j]
                arr1[tmp1] = data1[tmp1][0]
                arr2[tmp1] = data2[tmp1][0]
                iAvg1 += arr1[tmp1].toInt()
                iAvg2 += arr2[tmp1].toInt()
            }
        }
        iAvg1 /= 64
        iAvg2 /= 64
        //比对每个像素灰度值和平均灰度值大小
        for (i in 0..63) {
            arr1[i] = (if (arr1[i] >= iAvg1) 1 else 0).toDouble()
            arr2[i] = (if (arr2[i] >= iAvg2) 1 else 0).toDouble()
        }
        //计算差异值
        var iDiffNum = 0
        for (i in 0..63) if (arr1[i] != arr2[i]) ++iDiffNum
        //输出什么看个人喜好
        Log.e("xx", "有那么多处不同$iDiffNum")
        Log.e(
            "xx",
            "相似度" + (64 - iDiffNum) / (64)
        )
        return iDiffNum <= 5
    }

    //像返回true，不像false。
    //核心思想是，灰度差异小于一定数值，就Ok。
    fun newCompare(Bp1: Bitmap?, Bp2: Bitmap?): Boolean {
        val precision = 32
        val src1 = Mat()
        val dst1 = Mat()
        val src2 = Mat()
        val dst2 = Mat()
        //读取位图到MAT
        Utils.bitmapToMat(Bp1, src1)
        Utils.bitmapToMat(Bp2, src2)
        //四通变三通，三通变一通
        Imgproc.cvtColor(src1, dst1, Imgproc.COLOR_BGR2GRAY)
        Imgproc.cvtColor(src2, dst2, Imgproc.COLOR_BGR2GRAY)
        //缩成8*8
        Imgproc.resize(
            dst1,
            dst1,
            Size(precision.toDouble(), precision.toDouble()),
            0.0,
            0.0,
            Imgproc.INTER_CUBIC
        )
        Imgproc.resize(
            dst2,
            dst2,
            Size(precision.toDouble(), precision.toDouble()),
            0.0,
            0.0,
            Imgproc.INTER_CUBIC
        )

        //计算差异值
        var iDiffNum = 0.0
        //get灰度给data，计算每个像素的灰度差异。
        for (i in 0 until precision) {
            for (j in 0 until precision) {
                if (abs(
                        dst1[i, j][0] - dst2[i, j][0]
                    ) > 10
                ) iDiffNum++
            }
        }
        Log.e("xx", "有那么多处不同$iDiffNum")
        Log.e(
            "xx",
            "相似度" + (precision * precision - iDiffNum) / (precision * precision)
        )
        //输出什么看个人喜好
        return iDiffNum <= 100
    }

    //像返回true，不想false。
    //核心思想是，点不一样就记为不同。
    fun diffCompare(Bp1: Bitmap?, Bp2: Bitmap?): Boolean {
        val precision = 32
        val src1 = Mat()
        val dst1 = Mat()
        val src2 = Mat()
        val dst2 = Mat()
        //读取位图到MAT
        Utils.bitmapToMat(Bp1, src1)
        Utils.bitmapToMat(Bp2, src2)
        //四通变三通，三通变一通
        Imgproc.cvtColor(src1, dst1, Imgproc.COLOR_BGR2GRAY)
        Imgproc.cvtColor(src2, dst2, Imgproc.COLOR_BGR2GRAY)
        //缩成8*8
        Imgproc.resize(
            dst1,
            dst1,
            Size(precision.toDouble(), precision.toDouble()),
            0.0,
            0.0,
            Imgproc.INTER_CUBIC
        )
        Imgproc.resize(
            dst2,
            dst2,
            Size(precision.toDouble(), precision.toDouble()),
            0.0,
            0.0,
            Imgproc.INTER_CUBIC
        )
        val data1 = Array(precision * precision) { DoubleArray(1) }
        val data2 = Array(precision * precision) { DoubleArray(1) }
        //计算差异值
        var iDiffNum = 0.0
        //get灰度给data，查找不重合的灰度
        for (i in 0 until precision) {
            val tmp = i * precision
            for (j in 0 until precision - 1) {
                val tmp1 = tmp + j
                var b1: Boolean
                var b2: Boolean
                data1[tmp1] = dst1[i, j]
                data1[tmp1 + 1] = dst1[i, j + 1]
                b1 = data1[tmp1][0] > data1[tmp1 + 1][0]
                data2[tmp1] = dst2[i, j]
                data2[tmp1 + 1] = dst2[i, j + 1]
                b2 = data2[tmp1][0] > data2[tmp1 + 1][0]
                if (b1 != b2) iDiffNum++
            }
        }
        Log.e("xx", "有那么多处不同$iDiffNum")
        Log.e(
            "xx",
            "相似度" + (precision * precision - iDiffNum) / (precision * precision)
        )
        //输出什么看个人喜好
        return iDiffNum <= 100
    }
}