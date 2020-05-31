package com.example.wyk.scanner.model;

import android.content.Context;
import android.graphics.Bitmap;

import com.example.wyk.scanner.presenter.OnImgProcFinishedListener;

import org.opencv.core.Mat;


public interface ImageModelApi {
    void saveImgToGallery(Context context, Bitmap bmp, OnImgProcFinishedListener onImgProcFinishedListener);

    void preProcessImg(Context context, Mat src, double rotatedAngle, OnImgProcFinishedListener onImgProcFinishedListener);

    void correctDocImg(Context context, Mat src, OnImgProcFinishedListener onImgProcFinishedListener);

    void thresholdImg(Context context, Mat src, OnImgProcFinishedListener onImgProcFinishedListener);
}
