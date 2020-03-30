package com.example.wyk.scanner.model;

import android.content.Context;
import android.graphics.Bitmap;

import com.example.wyk.scanner.presenter.OnImgProcFinishedListener;

import org.opencv.core.Mat;


public interface ImageModelApi {
    public void saveImgToGallery(Context context, Bitmap bmp, OnImgProcFinishedListener onImgProcFinishedListener);

    public void preProcessImg(Context context, Mat src, Mat dst, double rotatedAngle, OnImgProcFinishedListener onImgProcFinishedListener);

    public void correctImg(Context context, Mat src, Mat dst, OnImgProcFinishedListener onImgProcFinishedListener);
}
