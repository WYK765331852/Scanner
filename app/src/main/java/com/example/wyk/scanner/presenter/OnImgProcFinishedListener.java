package com.example.wyk.scanner.presenter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

public interface OnImgProcFinishedListener {
    void onSaveImgError();

    void onSaveImgSuccess();

    void onPreProcessImgError(String error);

    void onCorrectionImgError();

    void onThresholdImgError();

    void onPreProcessSuccess(Activity context, Bitmap bmp);

    void onCorrectionSuccess(Activity context, Bitmap bmp);

    void onThresholdSuccess(Activity context, Bitmap bmp);

}
