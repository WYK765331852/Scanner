package com.example.wyk.scanner.model;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static com.example.wyk.scanner.view.CameraActivity.TAG_TEST;

public class PreProcessUtil {

    public PreProcessUtil() {

    }

    //    旋转bitmap
    public Bitmap rotateImg(int angle, Bitmap bmp) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap resizeBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        if (resizeBmp != bmp && bmp != null && !bmp.isRecycled()) {
            bmp.recycle();
            bmp = null;
        }
        return resizeBmp;
    }

    //    缩放bitmap
    public Bitmap scaleImg(Bitmap bmp, int newWidth) {
        if (bmp == null) {
            return null;
        }
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        float scale = ((float) newWidth) / width;
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        Bitmap newBmp = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true);
        if (bmp != null && !bmp.isRecycled()) {
            //销毁原图
            bmp.recycle();
            bmp = null;
        }
        return newBmp;
    }

    //    找到最大轮廓对应的index
    public int findMaxContour(List<MatOfPoint> contours) {
        int index = 0;
        double tempArea;
        double area = Imgproc.boundingRect(contours.get(0)).area();
        for (int i = 0; i < contours.size(); i++) {
            tempArea = Imgproc.boundingRect(contours.get(i)).area();
            if (tempArea > area) {
                area = tempArea;
                index = i;
            }
        }
        return index;
    }

    //    由rect获取旋转因子，旋转mat
    public Mat rotateMat(Mat dst, RotatedRect rect, double rotatedAngle) {
        //获取中心
        Point center = rect.center;
        double angle = rect.angle + rotatedAngle;
        Log.d(TAG_TEST, "rect angle: " + angle);
        Log.d(TAG_TEST, "rect center: (" + center.x + ", " + center.y + ")");

        Mat correctionMat = new Mat(dst.size(), dst.type());
        dst.copyTo(correctionMat);

        //获取旋转因子
        Mat matrix = Imgproc.getRotationMatrix2D(center, angle, 0.8);
        Imgproc.warpAffine(correctionMat, correctionMat, matrix, correctionMat.size(), 1, 0);

        return correctionMat;
    }


    //切割旋转后的图像
    public Mat cutRectArea(Mat correctionMat, Mat nativeCorrectMat, RotatedRect maxRect) {
        Point[] maxRectPoint = new Point[4];
        maxRect.points(maxRectPoint);

        int startLeft = (int) Math.abs(maxRectPoint[0].x < maxRectPoint[3].x ? maxRectPoint[0].x : maxRectPoint[3].x);
        startLeft = (int) Math.abs(startLeft < maxRectPoint[1].x ? startLeft : maxRectPoint[1].x);
        int startUp = (int) Math.min(Math.abs(maxRectPoint[1].y), Math.abs(maxRectPoint[2].y));
        startUp = (int) Math.min(startUp, (int) Math.abs(maxRectPoint[3].y));

        Log.d(TAG_TEST, "cutRectArea start point: (" + startLeft + ", " + startUp + ")");

        int distance12X = (int) Math.abs(maxRectPoint[1].x - maxRectPoint[2].x);
        int distance10X = (int) Math.abs(maxRectPoint[1].x - maxRectPoint[0].x);
        int distance12Y = (int) Math.abs(maxRectPoint[1].y - maxRectPoint[2].y);
        int distance10Y = (int) Math.abs(maxRectPoint[1].y - maxRectPoint[0].y);

        int width = Math.max(distance10X, distance12X);
        int height = Math.max(distance10Y, distance12Y);

        Mat tempMat = new Mat(nativeCorrectMat, new Rect(startLeft, startUp, width, height));
        Mat outputMat = new Mat();
        tempMat.copyTo(outputMat);

        return outputMat;
    }

    //获取最大contours对应的index：需要传入已从高到低排好顺序的contours
    public List<Integer> getMaxIndex(List<Integer> indexList, List<MatOfPoint> contours) {
        int indexCnt = 0;
        int size = contours.size();
        if (size >= 4) {
            for (int i = 0; i < 4; i++) {
                MatOfPoint2f matOfPoint2f = new MatOfPoint2f(contours.get(i).toArray());
                Log.d(TAG_TEST, "index: " + i + " contours area: " + Imgproc.contourArea(contours.get(i)));
                double peri = Imgproc.arcLength(matOfPoint2f, true);
                MatOfPoint2f approxCurve = new MatOfPoint2f();
                Imgproc.approxPolyDP(matOfPoint2f, approxCurve, 0.02 * peri, true);
                Log.d(TAG_TEST, "approxCurve total: " + approxCurve.total());
                if (approxCurve.total() == 4) {
                    indexCnt = i;
                    indexList.add(i);
                    Log.d(TAG_TEST, "运行到此处");
                }
            }
        } else {
            for (int i = 0; i < size; i++) {
                MatOfPoint2f matOfPoint2f = new MatOfPoint2f(contours.get(i).toArray());
                Log.d(TAG_TEST, "index: " + i + " contours area: " + Imgproc.contourArea(contours.get(i)));
                double peri = Imgproc.arcLength(matOfPoint2f, true);
                MatOfPoint2f approxCurve = new MatOfPoint2f();
                Imgproc.approxPolyDP(matOfPoint2f, approxCurve, 0.02 * peri, true);
                Log.d(TAG_TEST, "approxCurve total: " + approxCurve.total());
                if (approxCurve.total() == 4) {
                    indexCnt = i;
                    indexList.add(i);
                    Log.d(TAG_TEST, "运行到此处");
                }
            }
        }
        Log.d(TAG_TEST, "方法一 最大contours index：" + indexList.get(0) + "\n对应contours area: " + Imgproc.contourArea(contours.get(indexList.get(0))));

//                判断一下是否为空，万一照片质量不好，未检测出来文档
        if (indexList == null) {
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
                        indexList.clear();
                        indexList.add(idx);
                    }
                }
            }
        }
        return indexList;
    }

    //由approxPolyDP得到矩形框——→得到矩形四个顶点坐标——→整理顺序得到srcPoints
    public Point[] getSrcPoints(Point[] origPoints) {
        Point[] srcPoints = new Point[4];
        double[] sumPoints = new double[4];
        for (int i = 0; i < 4; i++) {
            sumPoints[i] = Math.abs(origPoints[i].x) + Math.abs(origPoints[i].y);
        }
//                获取最大sum和最小sum对应的index
        int sumMinIndex = 0;
        int sumMaxIndex = 0;
        double sumMin = sumPoints[0];
        double sumMax = 0;
        for (int i = 0; i < 4; i++) {
            if (sumPoints[i] > sumMax) {
                sumMax = sumPoints[i];
                sumMaxIndex = i;
            }
            if (sumPoints[i] < sumMin) {
                sumMin = sumPoints[i];
                sumMinIndex = i;
            }
        }
//        top-left：x+y的值最小；bottom-right：x+y的值最大
        srcPoints[0] = origPoints[sumMinIndex];
        srcPoints[2] = origPoints[sumMaxIndex];

        double[] diffPoints = new double[4];
        for (int i = 0; i < 4; i++) {
            diffPoints[i] = origPoints[i].x - origPoints[i].y;
        }

        int diffMinIndex = 0;
        int diffMaxIndex = 0;
        double diffMin = diffPoints[0];
        double diffMax = 0;
        for (int i = 0; i < 4; i++) {
            if (diffPoints[i] > diffMax) {
                diffMax = diffPoints[i];
                diffMaxIndex = i;
            }
            if (diffPoints[i] < diffMin) {
                diffMin = diffPoints[i];
                diffMinIndex = i;
            }
        }
//        求差分x-y。top-right：具有最大的差分；bottom-left具有最小的差分
        srcPoints[1] = origPoints[diffMaxIndex];
        srcPoints[3] = origPoints[diffMinIndex];

        return srcPoints;
    }

    //    透视算法
    public Mat getWarpedPerspective(Mat src, Point[] srcPoints) {
//        求出dstMat的长和宽
        double dstWidthA = Math.sqrt(Math.abs(srcPoints[0].x - srcPoints[1].x) * Math.abs(srcPoints[0].x - srcPoints[1].x) +
                Math.abs(srcPoints[0].y - srcPoints[1].y) * Math.abs(srcPoints[0].y - srcPoints[1].y));
        double dstWidthB = Math.sqrt(Math.abs(srcPoints[2].x - srcPoints[3].x) * Math.abs(srcPoints[2].x - srcPoints[3].x) +
                Math.abs(srcPoints[2].y - srcPoints[3].y) * Math.abs(srcPoints[2].y - srcPoints[3].y));
        double dstWidth = Math.max(dstWidthA, dstWidthB);

        double dstHeightA = Math.sqrt(Math.abs(srcPoints[0].x - srcPoints[3].x) * Math.abs(srcPoints[0].x - srcPoints[3].x) +
                Math.abs(srcPoints[0].y - srcPoints[3].y) * Math.abs(srcPoints[0].y - srcPoints[3].y));
        double dstHeightB = Math.sqrt(Math.abs(srcPoints[1].x - srcPoints[2].x) * Math.abs(srcPoints[1].x - srcPoints[2].x) +
                Math.abs(srcPoints[1].y - srcPoints[2].y) * Math.abs(srcPoints[1].y - srcPoints[2].y));
        double dstHeight = Math.max(dstHeightA, dstHeightB);

        Point[] dstPoints = new Point[4];
        for (int i = 0; i < 4; i++) {
            dstPoints[i] = new Point();
        }
        dstPoints[0].x = 0;
        dstPoints[0].y = 0;
        dstPoints[1].x = 0 + dstWidth;
        dstPoints[1].y = 0;
        dstPoints[2].x = 0 + dstWidth;
        dstPoints[2].y = 0 + dstHeight;
        dstPoints[3].x = 0;
        dstPoints[3].y = 0 + dstHeight;

        MatOfPoint2f srcPoint2f = new MatOfPoint2f(srcPoints);
        MatOfPoint2f dstPoint2f = new MatOfPoint2f(dstPoints);

        Mat transformMat = Imgproc.getPerspectiveTransform(dstPoint2f, srcPoint2f);
        Mat warpedMat = new Mat();
        Imgproc.warpPerspective(src, warpedMat, transformMat, new Size(dstWidth, dstHeight), Imgproc.INTER_LINEAR | Imgproc.WARP_INVERSE_MAP);

        return warpedMat;
    }

}
