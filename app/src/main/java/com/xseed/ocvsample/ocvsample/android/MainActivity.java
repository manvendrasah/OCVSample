package com.xseed.ocvsample.ocvsample.android;

import android.Manifest;
import android.Manifest.permission;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.xseed.ocvsample.ocvsample.R;
import com.xseed.ocvsample.ocvsample.camera.AutoFitSurfaceView;
import com.xseed.ocvsample.ocvsample.camera.CameraView;
import com.xseed.ocvsample.ocvsample.listener.AbstractFrameListener;
import com.xseed.ocvsample.ocvsample.listener.OMRSheetListener;
import com.xseed.ocvsample.ocvsample.pojo.Output;
import com.xseed.ocvsample.ocvsample.utility.Constants;
import com.xseed.ocvsample.ocvsample.utility.ErrorType;
import com.xseed.ocvsample.ocvsample.utility.Logger;
import com.xseed.ocvsample.ocvsample.utility.Utility;

import org.opencv.core.Mat;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OMRSheetListener {
    private CameraView camPreview;
    private FrameLayout flScan;
    private boolean isSurfaceSet = false;
    public static final int PREVIEW_SIZE_WIDTH = 480;
    public static final int PREVIEW_SIZE_HEIGHT = 640;
    private LinearLayout llContainer;
    private HorizontalScrollView hsvFrame;
    private LayoutInflater inflater;
    private Menu menu;
    private ProgressDialog progressDialog;
    private PopupWindow scanFailPopup;
    private long sT = 0;
    private static final int CAMERA_RESULT_CODE = 76;
    private static final int STORAGE_RESULT_CODE = 177;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        View footer;
        FrameLayout flTile;
        footer = findViewById(R.id.fl_omr_scan_footer);
        flScan = (FrameLayout) findViewById(R.id.fl_scan);
        flTile = (FrameLayout) findViewById(R.id.fl_scan_tile);
        hsvFrame = (HorizontalScrollView) findViewById(R.id.hsv_frame);
        llContainer = (LinearLayout) findViewById(R.id.ll_frame);
        inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) flTile.getLayoutParams();
        int deviceWidth = Utility.getDeviceWidthInPixel(this);
        int ht = (deviceWidth * PREVIEW_SIZE_HEIGHT) / PREVIEW_SIZE_WIDTH;
        params.height = ht;
        flTile.setLayoutParams(params);
        onPictureSize(Utility.getDeviceWidthInPixel(this));
        footer.setOnClickListener(this);
        flTile.setOnClickListener(this);
        llContainer.setOnClickListener(this);
        findViewById(R.id.iv_crosshair).setOnClickListener(this);
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermission(permission.WRITE_EXTERNAL_STORAGE, STORAGE_RESULT_CODE);
        }
    }

    private void checkPermissionAndSetSurface() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            if (isSurfaceSet) {
                camPreview.onResume();
            } else
                initSurfaceView();
        } else {
            requestPermission(Manifest.permission.CAMERA, CAMERA_RESULT_CODE);
            isSurfaceSet = false;
        }
    }

    private void initSurfaceView() {
        flScan.removeAllViews();
        AutoFitSurfaceView mSurfaceView = new AutoFitSurfaceView(this);
        camPreview = new CameraView(this, mSurfaceView);
        flScan.addView(mSurfaceView, new WindowManager.LayoutParams(PREVIEW_SIZE_WIDTH, PREVIEW_SIZE_HEIGHT));
        isSurfaceSet = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkStoragePermission();
        checkPermissionAndSetSurface();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.ll_frame) {
            restoreScanState();
        } else {
            capturePicture();
        }
    }

    private void restoreScanState() {
        Logger.logOCV("restoreScanState()");
        camPreview.recycleFrames();
        Utility.deleteImageDirectory();
        Utility.deleteLogFile();
        hsvFrame.setVisibility(View.GONE);
        llContainer.removeAllViews();
        showOverflowMenu(false);
    }

    private void capturePicture() {
        Logger.logOMR("AISS# > Capture click");
        camPreview.takePicture();
        showProgress();
        sT = System.currentTimeMillis();
    }

    private void refreshProcessState(boolean doVibrate) {
        camPreview.startPreview();
        camPreview.shutdownFrameListener();
        if (doVibrate) {
            Vibrator v = (Vibrator) MainActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(300);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camPreview != null) {
            refreshProcessState(false);
            camPreview.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (camPreview != null)
            camPreview.onDestroy();
    }

    @Override
    public void onOMRSheetGradingComplete(Bitmap originalBitmap, Mat originalMat, Output finalOutput) {
        long dT = System.currentTimeMillis();
        logScanSucess((dT - sT));
        camPreview.startPreview();
        showOverflowMenu(true);
        hsvFrame.setVisibility(View.VISIBLE);
        hideProgress();
    }

    private void logScanSucess(long l) {
        Map<String, Object> map = new HashMap<>();
        map.put("Scan Time", ((float) l) / 1000);
        Logger.logEventToFA("Scan Success", map);
    }

    @Override
    public void onOMRSheetGradingFailed(final int errorType) {
        if (scanFailPopup != null && scanFailPopup.isShowing())
            scanFailPopup.dismiss();
        scanFailPopup = Utility.showScanFailPopup(MainActivity.this, findViewById(R.id.root),
                errorType, scanFailPopupDismissClickListener, scanFailPopupSendErrorClickListener);
        long dT = System.currentTimeMillis();
        showOverflowMenu(true);
        logScanFailure((dT - sT), errorType);
        hsvFrame.setVisibility(View.VISIBLE);
//        showErrorSnackBar(errorType);
//        Toast.makeText(MainActivity.this,
//                "Sheet Detection FAILED! " + ErrorType.getErrorString(errorType), Toast.LENGTH_LONG).show();
        Logger.logOCV("Sheet Detection fail > " + ErrorType.getErrorString(errorType));
        camPreview.startPreview();
        hideProgress();
    }

    public void showErrorSnackBar(final int errorType) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Snackbar.make(hsvFrame, ErrorType.getErrorMessage(errorType), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void logScanFailure(long l, int errorType) {
        Map<String, Object> map = new HashMap<>();
        map.put("Scan Time", ((float) l) / 1000);
        map.put("Error Type", errorType);
        Logger.logEventToFA("Scan Failure", map);
    }

    @Override
    public void onOMRSheetBitmap(final Bitmap bitmap, final String name) {
        Utility.storeImage(bitmap, name);
        if (name.equals(AbstractFrameListener.TAG_BLOBS_DETECTED)) {
            View view = inflater.inflate(R.layout.layout_frame, null);
            ImageView ivFrame = (ImageView) view.findViewById(R.id.ivFrame);
            TextView tvName = (TextView) view.findViewById(R.id.tv_frame_name);
            ivFrame.setImageBitmap(bitmap);
            tvName.setText(name);
            llContainer.addView(view, 0);
        }
    }

    public void showProgress() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.getWindow().setGravity(Gravity.CENTER);
            progressDialog.setMessage("Detecting OMR...");
            progressDialog.setIndeterminate(true);
        }

        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    public void hideProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null && progressDialog.isShowing() && !isFinishing()) {
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void requestPermission(@NonNull String permission, int resultCode) {

        ActivityCompat.requestPermissions(this,
                new String[]{permission},
                resultCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_RESULT_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermissionAndSetSurface();
            } else {
                finish();
            }
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Utility.sendErrorFiles(this);
        return super.onOptionsItemSelected(item);
    }

    public void showOverflowMenu(boolean showMenu) {
        if (menu == null)
            return;
        menu.setGroupVisible(R.id.main_menu_group, showMenu);
    }

    public void onPictureSize(int width) {
        int side = /*pictureSize.height /*/ width / Constants.VIEWFINDER_MULTIPLIER;
        Logger.logOCV("ViewFinder side = " + side);
        ImageView iv;
        FrameLayout.LayoutParams params;

        iv = (ImageView) findViewById(R.id.iv_scan_tile_tl);
        params = (FrameLayout.LayoutParams) iv.getLayoutParams();
        params.width = side;
        params.height = side;
        iv.setLayoutParams(params);

        iv = (ImageView) findViewById(R.id.iv_scan_tile_tr);
        params = (FrameLayout.LayoutParams) iv.getLayoutParams();
        params.width = side;
        params.height = side;
        iv.setLayoutParams(params);

        iv = (ImageView) findViewById(R.id.iv_scan_tile_bl);
        params = (FrameLayout.LayoutParams) iv.getLayoutParams();
        params.width = side;
        params.height = side;
        iv.setLayoutParams(params);

        iv = (ImageView) findViewById(R.id.iv_scan_tile_br);
        params = (FrameLayout.LayoutParams) iv.getLayoutParams();
        params.width = side;
        params.height = side;
        iv.setLayoutParams(params);
    }

    private OnClickListener scanFailPopupDismissClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (scanFailPopup != null && scanFailPopup.isShowing()) {
                scanFailPopup.dismiss();
                restoreScanState();
            }
        }
    };

    private OnClickListener scanFailPopupSendErrorClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Utility.sendErrorFiles(MainActivity.this);
        }
    };

    @Override
    public void onBackPressed() {
        if (scanFailPopup != null && scanFailPopup.isShowing()) {
            scanFailPopup.dismiss();
            restoreScanState();
        } else
            super.onBackPressed();
    }
}