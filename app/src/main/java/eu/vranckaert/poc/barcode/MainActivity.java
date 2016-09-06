package eu.vranckaert.poc.barcode;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.client.android.BarcodeHelper;
import com.google.zxing.client.android.ViewfinderView;

public class MainActivity extends AppCompatActivity implements BarcodeHelper.DecodeListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BarcodeHelper.getInstance(this).setDecodeListener(this);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ViewfinderView viewFinder = (ViewfinderView) findViewById(R.id.viewfinder);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
        BarcodeHelper.getInstance(this).onResume(surfaceView, viewFinder);
    }

    @Override
    protected void onPause() {
        BarcodeHelper.getInstance(this).onPause();
        super.onPause();
    }

    @Override
    public void handleDecode(final Result rawResult, Bitmap barcode, float scaleFactor) {

        Toast.makeText(this, "Hooray!", Toast.LENGTH_SHORT).show();
//        drawResultPoints(rawResult, barcode, scaleFactor);

        findViewById(R.id.viewfinder).postDelayed(new Runnable() {
            @Override
            public void run() {
                String barCode = rawResult.getText();
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Barcode Found")
                        .setMessage("Barcode: " + barCode + "\nFormat: " + rawResult.getBarcodeFormat().name())
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                BarcodeHelper.getInstance(MainActivity.this).restartPreviewAfterDelay(200);
                            }
                        })
                        .show();
            }
        }, 500);
    }

//    private void drawResultPoints(Result rawResult, Bitmap barcode, float scaleFactor) {
//        ResultPoint[] points = rawResult.getResultPoints();
//        if (points != null && points.length > 0) {
//            Canvas canvas = new Canvas(barcode);
//            Paint paint = new Paint();
//            paint.setColor(getResources().getColor(R.color.zxing_result_points));
//            if (points.length == 2) {
//                paint.setStrokeWidth(4.0f);
//                drawLine(canvas, paint, points[0], points[1], scaleFactor);
//            } else if (points.length == 4 &&
//                    (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A ||
//                            rawResult.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
//                // Hacky special case -- draw two lines, for the barcode and metadata
//                drawLine(canvas, paint, points[0], points[1], scaleFactor);
//                drawLine(canvas, paint, points[2], points[3], scaleFactor);
//            } else {
//                paint.setStrokeWidth(10.0f);
//                for (ResultPoint point : points) {
//                    if (point != null) {
//                        canvas.drawPoint(scaleFactor * point.getX(), scaleFactor * point.getY(), paint);
//                    }
//                }
//            }
//        }
//    }
//
//    private static void drawLine(Canvas canvas, Paint paint, ResultPoint a, ResultPoint b, float scaleFactor) {
//        if (a != null && b != null) {
//            canvas.drawLine(scaleFactor * a.getX(),
//                    scaleFactor * a.getY(),
//                    scaleFactor * b.getX(),
//                    scaleFactor * b.getY(),
//                    paint);
//        }
//    }
}
