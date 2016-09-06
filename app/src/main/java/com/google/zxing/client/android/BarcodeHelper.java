package com.google.zxing.client.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.client.android.camera.CameraManager;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import eu.vranckaert.poc.barcode.MainActivity;
import eu.vranckaert.poc.barcode.R;

/**
 * Created by dirkvranckaert on 05/09/16.
 */

public class BarcodeHelper implements ICameraScanner, SurfaceHolder.Callback {
    private String TAG = MainActivity.class.getSimpleName();

    private static BarcodeHelper INSTANCE;

    private Context context;
    private ViewfinderView viewfinder;
    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private SurfaceHolder surfaceHolder;
    private boolean hasSurface;
    private DecodeListener decodeListener;

    private BarcodeHelper() {
    }

    public static BarcodeHelper getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new BarcodeHelper();
        }
        INSTANCE.context = context;
        return INSTANCE;
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
    }

    public void setViewFinder(ViewfinderView viewfinder) {
        this.viewfinder = viewfinder;
    }

    public void setFurfaceHolder(SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;
    }

    public void setDecodeListener(DecodeListener decodeListener) {
        this.decodeListener = decodeListener;
    }

    public void onResume() {
        cameraManager = new CameraManager(context);
        viewfinder.setCameraManager(cameraManager);

        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
        }
    }

    public void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        cameraManager.closeDriver();
        //historyManager = null; // Keep for onActivityResult
        if (!hasSurface) {
            surfaceHolder.removeCallback(this);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    public Handler getHandler() {
        return handler;
    }

    @Override
    public ViewfinderView getViewfinderView() {
        return viewfinder;
    }

    @Override
    public void handleDecode(Result obj, Bitmap barcode, float scaleFactor) {
        Log.d(TAG, "handleDecode");
        if (decodeListener != null) {
            decodeListener.handleDecode(obj, barcode, scaleFactor);
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (handler == null) {
                Set<BarcodeFormat> decodeFormats = EnumSet.noneOf(BarcodeFormat.class);
                decodeFormats.addAll(DecodeFormatManager.PRODUCT_FORMATS); // 1D PRODUCT
                decodeFormats.addAll(DecodeFormatManager.INDUSTRIAL_FORMATS); // 1D INDUSTRIAL
                decodeFormats.addAll(DecodeFormatManager.QR_CODE_FORMATS); // QR
                decodeFormats.addAll(DecodeFormatManager.DATA_MATRIX_FORMATS); // MATRIX
                decodeFormats.addAll(DecodeFormatManager.AZTEC_FORMATS); // AZTEC
                decodeFormats.addAll(DecodeFormatManager.PDF417_FORMATS); // PDF417

                Map<DecodeHintType, Object> hints = new HashMap<>();
                hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);

                handler = new CaptureActivityHandler(this, decodeFormats, hints, null, cameraManager);
            }
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
        }
    }

    public interface DecodeListener {
        void handleDecode(Result obj, Bitmap barcode, float scaleFactor);
    }
}
