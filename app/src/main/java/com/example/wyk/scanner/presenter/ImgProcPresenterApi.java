package com.example.wyk.scanner.presenter;

import android.content.Context;
import android.graphics.Bitmap;

import org.opencv.core.Mat;

public interface ImgProcPresenterApi {
    void preProcessImg(Context context, Mat src, double rotatedAngle);

    void correctionDocImg(Context context, Mat src);

    void thresholdImg(Context context, Mat src);

    void saveImg(Context context, Bitmap src);

    void onDestroy();

}
