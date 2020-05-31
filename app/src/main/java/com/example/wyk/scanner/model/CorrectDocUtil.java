package com.example.wyk.scanner.model;

import android.util.Log;

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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.example.wyk.scanner.view.CameraActivity.TAG_TEST;

public class CorrectDocUtil {
    //角度转换
    public double transDegree(double theta) {
        double res = theta / Math.PI * 180;
        return res;
    }

    //预处理
    public Mat preProcessDoc(Mat src) {
        //src：RGBA
        Mat dst = new Mat();
        //转化成灰度图
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_RGBA2GRAY);
        //高斯滤波
        Imgproc.GaussianBlur(dst, dst, new Size(5, 5), 0);

        //Sobel算子，求x方向的梯度，为了使得文字的竖线更加清晰
        Imgproc.Sobel(dst, dst, CvType.CV_8U, 1, 0, 3);
        //Scharr算子比sobel算子更为精确
//        Imgproc.Scharr(dst, dst, CvType.CV_8U, 1, 0, 3);

        Imgproc.threshold(dst, dst, 0, 255, Imgproc.THRESH_OTSU + Imgproc.THRESH_BINARY);
//
        //腐蚀和膨胀操作的核函数
        Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(30, 9));
        Mat element2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(24, 4));

        //膨胀一次，让轮廓突出
        Imgproc.dilate(dst, dst, element2);
        //腐蚀一次，去掉细节，如表格线，这里是去掉竖直的线
        Imgproc.erode(dst, dst, element1);
        //再次膨胀，让轮廓明显
//        * @param anchor position of the anchor within the element; default value (-1, -1) means that the
//        * anchor is at the element center.
//        * @param iterations number of times dilation is applied.
        Imgproc.dilate(dst, dst, element2, new Point(-1, -1), 2);

        return dst;
    }

    //文字区域查找
    public List<RotatedRect> findTextRegion(Mat src) {
        List<RotatedRect> rectList = new ArrayList<>();
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        //最后一个参数point：所有的轮廓信息相对于原始图像对应点的偏移量，相当于在每一个检测出的轮廓点上加 上该偏移量
        Imgproc.findContours(src, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));

        for (int i = 0; i < contours.size(); i++) {
            //计算当前轮廓的面积
            double area = Imgproc.contourArea(contours.get(i));
//            Log.d(TAG_TEST, "Doc contourArea" + i + ": " + area);
            //面积小于1000的全部筛选掉
            if (area < 1000) {
                continue;
            }
            //轮廓近似，作用较小
            MatOfPoint2f matOfPoint2f = new MatOfPoint2f(contours.get(i).toArray());
            double peri = Imgproc.arcLength(matOfPoint2f, true);
            double epsilon = 0.001 * peri;
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            Imgproc.approxPolyDP(matOfPoint2f, approxCurve, epsilon, true);

            //找到最小矩形，该矩形可能有方向
            RotatedRect rect = Imgproc.minAreaRect(matOfPoint2f);

            //计算高和宽
            int width = rect.boundingRect().width;
            int height = rect.boundingRect().height;

            //筛选那些太细的矩形，留下扁的
            if (height > width * 1.2) {
                continue;
            }

            rectList.add(rect);
        }
        Log.d(TAG_TEST, "Doc RotatedRect Size: " + rectList.size());
        return rectList;
    }

    public Mat detect(Mat src) {
        //1.形态学变换的预处理，得到可以查找矩形的轮廓
        Mat dilation = preProcessDoc(src);

        //2.查找和筛选文字区域
        List<RotatedRect> rects = findTextRegion(dilation);

        //3.用绿线画出这些找到的轮廓
        for (RotatedRect rect : rects) {
            Point[] points = new Point[4];
            rect.points(points);
            for (int i = 0; i <= 3; i++) {
                Imgproc.line(src, points[i], points[(i + 1) % 4], new Scalar(0, 255, 0), 2);
            }
        }
        return src;
    }

    //    求文字区域矩形框的四个顶点
    public Point[] getDocSrcPoints(List<RotatedRect> rotatedRects) {
        Point[] srcRectPoint = new Point[4];
        for (int i = 0; i < 4; i++) {
            srcRectPoint[i] = new Point();
        }
        List<Point> topRights = new ArrayList<>();
        List<Point> topLefts = new ArrayList<>();
        List<Point> bottomRights = new ArrayList<>();
        List<Point> bottomLefts = new ArrayList<>();
        int left, right, top, bottom = 0;


        for (int i = 0; i < rotatedRects.size(); i++) {
            Point[] srcPoint = new Point[4];
            rotatedRects.get(i).points(srcPoint);
            Point[] dstPoint = getRectPoints(srcPoint);

            topRights.add(dstPoint[1]);
            topLefts.add(dstPoint[0]);
            bottomRights.add(dstPoint[2]);
            bottomLefts.add(dstPoint[3]);
        }

        Log.d(TAG_TEST, "Doc topLeft size: " + topLefts.size());
        //        求左边界
        //        从低到高排序
        topLefts.sort((point1, point2) -> (int) (point1.x - point2.x));
        bottomLefts.sort((point, t1) -> (int) (point.x - t1.x));
        left = (int) (topLefts.get(0).x < bottomLefts.get(0).x ? topLefts.get(0).x : bottomLefts.get(0).x);
        srcRectPoint[0].x = left;
        srcRectPoint[3].x = left;

        //        求上边界
        //        从低到高排序
        topLefts.sort((point, t1) -> (int) (point.y - t1.y));
        topRights.sort((point, t1) -> (int) (point.y - t1.y));
        top = (int) (topLefts.get(0).y < topRights.get(0).y ? topLefts.get(0).y : topRights.get(0).y);
        srcRectPoint[0].y = top;
        srcRectPoint[1].y = top;

        //        求右边界
        //        从高到低排序
        topRights.sort((point, t1) -> (int) (t1.x - point.x));
        bottomRights.sort((point, t1) -> (int) (t1.x - point.x));
        right = (int) (topRights.get(0).x > bottomRights.get(0).x ? topRights.get(0).x : bottomRights.get(0).x);
        srcRectPoint[1].x = right;
        srcRectPoint[2].x = right;

        //        求下边界
        //        从高到低排序
        bottomLefts.sort((point, t1) -> (int) (t1.y - point.y));
        bottomRights.sort((point, t1) -> (int) (t1.y - point.y));
        bottom = (int) (bottomLefts.get(0).y > bottomRights.get(0).y ? bottomLefts.get(0).y : bottomRights.get(0).y);
        srcRectPoint[2].y = bottom;
        srcRectPoint[3].y = bottom;

        Log.d(TAG_TEST, "Doc cutArea point0: " + srcRectPoint[0] + " point1: " + srcRectPoint[1]
                + " point2: " + srcRectPoint[2] + " point3: " + srcRectPoint[3]);
        return srcRectPoint;
    }

    //    裁剪文字区域
    public Mat cutDocRectArea(Mat src) {

        //1.形态学变换的预处理，得到可以查找矩形的轮廓
        Mat dilation = preProcessDoc(src);

        //2.查找和筛选文字区域
        List<RotatedRect> rects = findTextRegion(dilation);

        //3.获取文字区域外边框四个顶点
        Point[] docRectPoint = getDocSrcPoints(rects);

        Log.d(TAG_TEST, "Doc docRectPoint size: " + docRectPoint.length);

        int width = (int) Math.abs(docRectPoint[1].x - docRectPoint[0].x);
        int height = (int) Math.abs(docRectPoint[2].y - docRectPoint[1].y);

        //4.裁剪
        Mat tempMat = new Mat(src, new Rect((int) docRectPoint[0].x, (int) docRectPoint[0].y, width, height));

        Log.d(TAG_TEST, "Doc outputMat size: " + tempMat.size());
        return tempMat;
    }

    //    整理四个顶点坐标的顺序
    public Point[] getRectPoints(Point[] origPoints) {
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
}
