package com.example.wyk.scanner.presenter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

import com.example.wyk.scanner.model.ImageModelApi;
import com.example.wyk.scanner.model.ImageModelApiImpl;
import com.example.wyk.scanner.view.ViewApi;

import org.opencv.core.Mat;

public class ImgProcPresenterApiImpl implements ImgProcPresenterApi, OnImgProcFinishedListener {
    private ViewApi viewApi;
    private ImageModelApi imageModelApi;

    public ImgProcPresenterApiImpl(ViewApi viewApi) {
        this.viewApi = viewApi;
        imageModelApi = new ImageModelApiImpl();
    }

    @Override
    public void preProcessImg(Context context, Mat src, double rotatedAngle) {
        if (viewApi != null) {
            viewApi.showProgressBar();
        }
        imageModelApi.preProcessImg(context, src, rotatedAngle, this);
    }

    @Override
    public void correctionDocImg(Context context, Mat src) {
        if (viewApi != null) {
            viewApi.showProgressBar();
        }
        imageModelApi.correctDocImg(context, src, this);
    }

    @Override
    public void thresholdImg(Context context, Mat src) {
        if (viewApi != null) {
            viewApi.showProgressBar();
        }
        imageModelApi.thresholdImg(context, src, this);
    }

    @Override
    public void saveImg(Context context, Bitmap src) {
        if (viewApi != null) {
            viewApi.showProgressBar();
        }
        imageModelApi.saveImgToGallery(context, src, this);
    }

    @Override
    public void onDestroy() {
        viewApi = null;
    }

    @Override
    public void onSaveImgError() {
        if (viewApi != null) {
            viewApi.setSaveImgError();
            viewApi.hideProgressBar();
        }
    }

    @Override
    public void onPreProcessImgError(String error) {
        if (viewApi != null) {
            viewApi.setPreProcessError(error);
            viewApi.hideProgressBar();
        }
    }

    @Override
    public void onCorrectionImgError() {
        if (viewApi != null) {
            viewApi.setCorrectionError();
            viewApi.hideProgressBar();
        }
    }

    @Override
    public void onThresholdImgError() {
        if (viewApi != null) {
            viewApi.setThresholdError();
            viewApi.hideProgressBar();
        }
    }

    @Override
    public void onSaveImgSuccess() {
        if (viewApi != null) {
            viewApi.hideProgressBar();
            viewApi.setSaveImgSuccess();
        }
    }

    @Override
    public void onPreProcessSuccess(Activity context, Bitmap bmp) {
        if (viewApi != null) {
            context.runOnUiThread(() -> {
                viewApi.setPreProcessSuccess(bmp);
                viewApi.hideProgressBar();
            });
        }
    }

    @Override
    public void onCorrectionSuccess(Activity context, Bitmap bmp) {
        if (viewApi != null) {
            context.runOnUiThread(() -> {
                viewApi.hideProgressBar();
                viewApi.setCorrectionSuccess(bmp);
            });
        }
    }

    @Override
    public void onThresholdSuccess(Activity context, Bitmap bmp) {
        if (viewApi != null) {
            context.runOnUiThread(() -> {
                viewApi.hideProgressBar();
                viewApi.setThresholdSuccess(bmp);
            });
        }
    }
}
