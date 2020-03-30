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
import android.widget.Toast;

import com.example.wyk.scanner.presenter.OnImgProcFinishedListener;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
//                File appDir = new File(Environment.getExternalStorageDirectory(), "Scanner");
                File appDir = context.getExternalFilesDir("Scanner");
                if (!appDir.exists()) {
                    appDir.mkdir();
                    Log.d(TAG_TEST, "Scanner创建！");
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
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(file);
                mediaScanIntent.setData(uri);
                context.sendBroadcast(mediaScanIntent);
                // 最后通知图库更新
//                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + Environment.getExternalStorageDirectory() + "Scanner")));
//                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE), uri.toString());
                if (bmp != null && uri != null) {
                    onImgProcFinishedListener.onSaveImgSuccess();
                }
            }
        }, 1500);

    }

    //    预处理代码写这里
    @Override
    public void preProcessImg(Context context, Mat src, Mat dst, double rotatedAngle, OnImgProcFinishedListener onImgProcFinishedListener) {
//        rows: Mat矩阵的行数。
//        cols: Mat矩阵的列数。
//        depth: 用来度量每一个像素中每一个通道的精度，但它本身与图像的通道数无关！
        final Bitmap[] dstBmp = new Bitmap[1];
        final Mat[] warpedMat = new Mat[1];
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
                Imgproc.GaussianBlur(dst, dst, new Size(5, 5), 0);
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
//                Imgproc.morphologyEx(dst, dst, Imgproc.MORPH_CLOSE, element);
//                腐蚀
                Imgproc.erode(dst, dst, element);
//                膨胀
                Imgproc.dilate(dst, dst, element);
//                边缘检测
                Imgproc.Canny(dst, dst, 75, 200, 3);
//                查找轮廓
                List<MatOfPoint> contours = new ArrayList<>();
                Mat hierarchy = new Mat();
                Imgproc.findContours(dst, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
//                加粗增强所有找到的轮廓，先不画出来了
//                Imgproc.drawContours(dst, contours, -1, new Scalar(255), 3);

//                再次查找轮廓
//                contours.clear();
//                hierarchy = new Mat();
//                Imgproc.findContours(dst, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);


                //从高到低排序
                contours.sort(new Comparator<MatOfPoint>() {
                    @Override
                    public int compare(MatOfPoint m1, MatOfPoint m2) {
                        return (int) (Imgproc.contourArea(m2) - Imgproc.contourArea(m1));
                    }
                });
//                Collections.reverse(contours);
                List<Integer> indexList = new ArrayList<>();
                PreProcessUtil preProcessUtil = new PreProcessUtil();
                preProcessUtil.getMaxIndex(indexList, contours);

                if (indexList != null){
                    MatOfPoint2f matOfPoint2fMax = new MatOfPoint2f(contours.get(indexList.get(0)).toArray());
                    double peri = Imgproc.arcLength(matOfPoint2fMax, true);
                    MatOfPoint2f approxCurveMax = new MatOfPoint2f();
                    Imgproc.approxPolyDP(matOfPoint2fMax, approxCurveMax, 0.02 * peri, true);
                    MatOfPoint matOfPointMax = new MatOfPoint();
                    approxCurveMax.convertTo(matOfPointMax, CvType.CV_32S);
                    Point[] points = matOfPointMax.toArray();
                    Log.d(TAG_TEST, "points length: " + points.length);
                    for (int i = 0; i < points.length; i++) {
                        Log.d(TAG_TEST, "point " + i + ": (" + points[i] + ")");
                    }

                    Point[] srcPoints = preProcessUtil.getSrcPoints(points);
                    if (srcPoints != null){
                        warpedMat[0] = preProcessUtil.getWarpedPerspective(src, srcPoints);
                        dstBmp[0] = Bitmap.createBitmap(warpedMat[0].width(), warpedMat[0].height(), Bitmap.Config.ARGB_8888);
                        Utils.matToBitmap(warpedMat[0], dstBmp[0]);
                    }
                }else {
                    String errorDetect = "未检测到最大矩形框";
                    onImgProcFinishedListener.onPreProcessImgError(errorDetect);
                }

                if (warpedMat[0] != null && dstBmp[0] != null) {
//                    传入bitmap，通知view绘制
                    onImgProcFinishedListener.onPreProcessSuccess((Activity) context, dstBmp[0]);
                } else {
                    String errorBmp = "未获取到处理后的图像";
                    onImgProcFinishedListener.onPreProcessImgError(errorBmp);
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
