package com.example.wyk.scanner.presenter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;

public interface OnImgProcFinishedListener {
    void onSaveImgError();

    void onSaveImgSuccess();

    void onPreProcessImgError(String error);

    void onCorrectionImgError();

    void onPreProcessSuccess(Activity context, Bitmap bmp);

    void onCorrectionSuccess();
}
