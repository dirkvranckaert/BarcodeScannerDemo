package com.google.zxing.client.android;

import android.graphics.Bitmap;
import android.os.Handler;

import com.google.zxing.Result;
import com.google.zxing.client.android.camera.CameraManager;

/**
 * Created by dirkvranckaert on 05/09/16.
 */

public interface ICameraScanner {
    CameraManager getCameraManager();
    Handler getHandler();
    ViewfinderView getViewfinderView();
    void handleDecode(Result obj, Bitmap barcode, float scaleFactor);
}
