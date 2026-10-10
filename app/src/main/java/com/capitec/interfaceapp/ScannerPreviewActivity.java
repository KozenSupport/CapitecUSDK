package com.capitec.interfaceapp;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.Gravity;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.capitec.interfaceapp.impl.ScanManager;
import com.kozen.financial.constant.ConstantScanner;
import com.kozen.financial.engine.FinancialEngine;
import com.kozen.financial.scanner.ICamScanInitStatusListener;
import com.kozen.financial.scanner.IConnectionStatusListener;
import com.kozen.financial.scanner.IScannerManager;
import com.kozen.financial.scanner.IScannerResultCallback;
import com.kozen.financial.scanner.PreviewTextureView;

public final class ScannerPreviewActivity extends Activity {
    private static ScannerPreviewActivity activeActivity;

    private IScannerManager scannerManager;
    private PreviewTextureView preview;
    private TextView statusText;
    private SurfaceTexture surfaceTexture;
    private boolean connected;
    private boolean scanStarted;
    private boolean completed;

    public static void stopActiveScan() {
        ScannerPreviewActivity activity = activeActivity;
        if (activity != null) {
            activity.runOnUiThread(() -> {
                activity.stopScanner();
                activity.finish();
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activeActivity = this;
        ScanManager.setScannerPreviewStopAction(ScannerPreviewActivity::stopActiveScan);
        buildView();
        startCameraScanner();
    }

    @Override
    protected void onDestroy() {
        stopScanner();
        ScanManager.clearPreviewCallback();
        if (activeActivity == this) {
            activeActivity = null;
        }
        super.onDestroy();
    }

    private void buildView() {
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        preview = new PreviewTextureView(this);
        preview.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                surfaceTexture = surface;
                tryStartSurfaceScan();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) { }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                surfaceTexture = null;
                scanStarted = false;
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) { }
        });
        root.addView(preview, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        statusText = new TextView(this);
        statusText.setTextColor(Color.WHITE);
        statusText.setTextSize(16f);
        statusText.setGravity(Gravity.CENTER);
        statusText.setBackgroundColor(0x99000000);
        statusText.setText("Starting scanner...");
        FrameLayout.LayoutParams statusParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
        );
        root.addView(statusText, statusParams);

        setContentView(root);
    }

    private void startCameraScanner() {
        try {
            FinancialEngine.INSTANCE.init(getApplicationContext(), (code, message) ->
                    runOnUiThread(() -> setStatus("Financial init: " + code + " " + message)));
            scannerManager = FinancialEngine.INSTANCE.getScannerManager();
            scannerManager.open(cameraTypeFromIntent(), new IConnectionStatusListener() {
                @Override
                public void onConnected() {
                    connected = true;
                    runOnUiThread(() -> {
                        setStatus("Scanner connected");
                        tryStartSurfaceScan();
                    });
                }

                @Override
                public void onError(int code, String message) {
                    if (completed) {
                        return;
                    }
                    String value = "Scanner error " + code + ": " + message;
                    ScanManager.dispatchPreviewResult(value);
                    runOnUiThread(() -> setStatus(value));
                }

                @Override
                public void onDisconnected() {
                    if (completed) {
                        return;
                    }
                    ScanManager.dispatchPreviewResult("Scanner disconnected");
                    runOnUiThread(() -> setStatus("Scanner disconnected"));
                }
            });
            scannerManager.registerResultCallback(new IScannerResultCallback() {
                @Override
                public void onResult(String barcode, String format) {
                    String value = format + ":" + barcode;
                    runOnUiThread(() -> completeWithResult(value));
                }
            });
        } catch (Throwable throwable) {
            String value = "Scanner failure: " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
            ScanManager.dispatchPreviewResult(value);
            setStatus(value);
        }
    }

    private ConstantScanner.ScannerCameraType cameraTypeFromIntent() {
        String raw = getIntent().getStringExtra(ScanManager.EXTRA_CAMERA_TYPE);
        if ("FRONT".equals(raw)) {
            return ConstantScanner.ScannerCameraType.CAMERA_FRONT;
        }
        return ConstantScanner.ScannerCameraType.CAMERA_REAR;
    }

    private void tryStartSurfaceScan() {
        if (!connected || scannerManager == null || surfaceTexture == null || scanStarted) {
            return;
        }
        scanStarted = true;
        try {
            int result = scannerManager.startScan(surfaceTexture, new ICamScanInitStatusListener() {
                @Override
                public void updatePreviewSize(int width, int height) {
                    if (height > 0) {
                        runOnUiThread(() -> preview.setAspectRatio(width / (double) height));
                    }
                }

                @Override
                public void onInitSuccess() {
                    runOnUiThread(() -> setStatus("Camera scanner ready"));
                    scannerManager.startDecoding();
                }

                @Override
                public void onInitFailed(int code) {
                    if (completed) {
                        return;
                    }
                    String value = "Camera scanner init failed: " + code;
                    ScanManager.dispatchPreviewResult(value);
                    runOnUiThread(() -> setStatus(value));
                }
            });
            setStatus("Starting camera scan: " + result);
        } catch (Throwable throwable) {
            scanStarted = false;
            String value = "Camera scan failure: " + throwable.getClass().getSimpleName() + ": " + throwable.getMessage();
            ScanManager.dispatchPreviewResult(value);
            setStatus(value);
        }
    }

    private void completeWithResult(String value) {
        if (completed) {
            return;
        }
        completed = true;
        ScanManager.dispatchPreviewResult(value);
        setStatus("Result: " + value);
        stopScanner();
        finish();
    }

    private void stopScanner() {
        if (scannerManager == null) {
            return;
        }
        try {
            scannerManager.stopDecoding();
        } catch (Throwable ignored) {
        }
        try {
            scannerManager.stopScan();
        } catch (Throwable ignored) {
        }
        try {
            scannerManager.close();
        } catch (Throwable ignored) {
        }
        scannerManager = null;
        scanStarted = false;
        connected = false;
    }

    private void setStatus(String value) {
        if (statusText != null) {
            statusText.setText(value);
        }
    }
}
