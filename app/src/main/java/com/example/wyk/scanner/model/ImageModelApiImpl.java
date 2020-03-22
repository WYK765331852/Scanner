package com.example.wyk.scanner.model;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

import com.example.wyk.scanner.presenter.OnImgProcFinishedListener;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.example.wyk.scanner.view.CameraActivity.TAG_TEST;

public class ImageModelApiImpl implements ImageModelApi {
    @Override
    public void saveImgToGallery(final Context context, final Bitmap bmp, final OnImgProcFinishedListener onImgProcFinishedListener) {
        // 首先保存图片
        // 参数以final修饰，意为在方法中不可以修改其值
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                File appDir = new File(Environment.getExternalStorageDirectory(), "Scanner");
                if (!appDir.exists()) {
                    appDir.mkdir();
                }
                String fileName = System.currentTimeMillis() + ".jpg";
                File file = new File(appDir, fileName);
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // 其次把文件插入到系统图库
                try {
                    MediaStore.Images.Media.insertImage(context.getContentResolver(),
                            file.getAbsolutePath(), fileName, null);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                Uri uri = Uri.fromFile(file);
                // 最后通知图库更新
//        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + Environment.getExternalStorageDirectory()+"Scanner")));
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE), uri.toString());
                if (bmp != null && uri != null) {
                    onImgProcFinishedListener.onSaveImgSuccess();
                }
            }
        }, 1500);

    }

    //    预处理代码写这里
    @Override
    public void preProcessImg(Context context, Mat src, Mat dst, OnImgProcFinishedListener onImgProcFinishedListener) {
//        rows: Mat矩阵的行数。
//        cols: Mat矩阵的列数。
//        depth: 用来度量每一个像素中每一个通道的精度，但它本身与图像的通道数无关！
        Log.d(TAG_TEST, "运行到model-preprocessing");
        Mat kernel = new Mat(3, 3, CvType.CV_32F, new Scalar(-1));
        kernel.put(1, 1, 8.9);
//        新线程中运行，以面UI阻塞
        new Thread() {
            @Override
            public void run() {
                super.run();
//                锐化
                Imgproc.filter2D(src, dst, src.depth(), kernel);
//                灰度化
                Imgproc.cvtColor(dst, dst, Imgproc.COLOR_RGBA2GRAY);
//                高斯滤波
                Imgproc.GaussianBlur(dst, dst, new Size(3, 3), 0);
//                二值化
                /*
                thresh: 阈值
                maxval：dst图像中最大值
                THRESH_OTSU和THRESH_TRIANGLE是作为优化算法配合
                THRESH_BINARY、THRESH_BINARY_INV、THRESH_TRUNC、THRESH_TOZERO以及THRESH_TOZERO_INV来使用的。
                当使用了THRESH_OTSU和THRESH_TRIANGLE两个标志时，输入图像必须为单通道。
                */
//                Imgproc.threshold(dst, dst, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);
                Imgproc.threshold(dst, dst, 100, 200, Imgproc.THRESH_BINARY);

//                闭运算
                Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
                Imgproc.morphologyEx(dst, dst, Imgproc.MORPH_CLOSE, element);
//                腐蚀
                Imgproc.erode(dst, dst, element);
//                边缘检测
                Imgproc.Canny(dst, dst, 30, 120, 3);

//                查找轮廓
                List<MatOfPoint> contours = new ArrayList<>();
                Mat hierarchy = new Mat();
                Imgproc.findContours(dst, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
//                加粗增强所有找到的轮廓，先不画出来了
//                Imgproc.drawContours(dst, contours, -1, new Scalar(255), 3);

//                再次查找轮廓
                contours.clear();
                hierarchy = new Mat();
                Imgproc.findContours(dst, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

//                获取最大轮廓并画出
                double maxArea = -1;
                double maxAreaIdx = -1;
                MatOfPoint tempContour = contours.get(0);
                MatOfPoint2f approxCurve = new MatOfPoint2f();
                Mat largestContour = contours.get(0);
                List<MatOfPoint> largestContours = new ArrayList<>();
                for (int idx = 0; idx < contours.size(); idx++) {
                    tempContour = contours.get(idx);
                    double contourArea = Imgproc.contourArea(tempContour);
                    if (contourArea > maxArea) {
                        MatOfPoint2f newMat = new MatOfPoint2f(tempContour.toArray());
                        int contourSize = (int) tempContour.total();
                        Imgproc.approxPolyDP(newMat, approxCurve, contourSize * 0.05, true);
                        if (approxCurve.total() == 4) {
                            maxArea = contourArea;
                            maxAreaIdx = idx;
                            largestContours.add(tempContour);
                            largestContour = tempContour;
                        }
                    }
                }
                MatOfPoint mPoint = largestContours.get(largestContours.size() - 1);
                contours.clear();
                contours.add(mPoint);
                //填充为黑色
                dst.setTo(new Scalar(0));
                Imgproc.drawContours(dst, contours, -1, new Scalar(255, 255, 255), 3);

                Imgproc.cvtColor(dst, dst, Imgproc.COLOR_GRAY2RGBA);
                Bitmap dstBmp = Bitmap.createBitmap(dst.width(), dst.height(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(dst, dstBmp);

                if (dst != null && dstBmp != null) {
//                    传入bitmap，通知view绘制
                    onImgProcFinishedListener.onPreProcessSuccess((Activity)context, dstBmp);
                } else {
                    onImgProcFinishedListener.onPreProcessImgError();
                }
            }
        }.start();
    }

    //    矫正代码写这里
    @Override
    public void correctImg(Context context, Mat src, Mat dst, OnImgProcFinishedListener onImgProcFinishedListener) {

        if (dst == null) {
            onImgProcFinishedListener.onCorrectionImgError();
        } else {
            onImgProcFinishedListener.onCorrectionSuccess();
        }
    }

}
