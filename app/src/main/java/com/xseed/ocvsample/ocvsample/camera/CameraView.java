package com.xseed.ocvsample.ocvsample.camera;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Build;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import com.xseed.ocvsample.ocvsample.listener.FrameListener;
import com.xseed.ocvsample.ocvsample.utility.Logger;
import com.xseed.ocvsample.ocvsample.android.MainActivity;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Manvendra Sah on 15/06/17.
 */

public class CameraView implements TextureView.SurfaceTextureListener {
    private Camera mCamera = null;
    private int imageFormat;
    private final Context mContext;
    private final SharedPreferences pref;
    private SurfaceTexture mPreviewSurfaceTexture;
    private final AutoFitSurfaceView mSurfaceView;

    private final FrameListener frameListener;

    public CameraView(Context context, AutoFitSurfaceView mSurfaceView) {
        mContext = context;
        pref = mContext.getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        frameListener = new FrameListener(context);
        mSurfaceView.setSurfaceTextureListener(this);
        this.mSurfaceView = mSurfaceView;
    }

    public void onResume() {
        Logger.logOMR("AISS > cam >onResume()");
        initCamera();
    }

    public void onPause() {
        Logger.logOMR("AISS > cam >onPause()");
        releaseCamera();
    }

    private void releaseCamera() {
        // check if Camera instance exists
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void startPreview() {
        try {
            if (mCamera != null)
                mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
            releaseCamera();
        }
    }

    public void takePicture() {
        frameListener.startProcessingFrame();
        try {
            mCamera.takePicture(null, null, frameListener);
        } catch (Exception e) {
            Logger.logOMR("AISS > cam >Picture capture error");
        }
    }

    public void shutdownFrameListener() {
        frameListener.shutDown();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
        Logger.logOMR("AISS > cam >onSurfaceTextureAvailable()");
        mPreviewSurfaceTexture = texture;
        initCamera();

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
        Logger.logOMR("AISS > cam >surfaceChanged()");
        mPreviewSurfaceTexture = texture;
        initCamera();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
        Logger.logOMR("AISS > cam >surfaceDestroyed()");
        mPreviewSurfaceTexture = null;
        if (frameListener != null)
            frameListener.shutDown();
        releaseCamera();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture texture) {
    }

    private void initCamera() {
        if (mCamera == null && mPreviewSurfaceTexture != null) {
            try {
                // open the mCamera
                mCamera = Camera.open();

            } catch (RuntimeException e) {
                // check for exceptions
                e.printStackTrace();
                showErrorAndFinish();
                return;
            }
            Camera.Parameters param;
            param = mCamera.getParameters();
            imageFormat = param.getPreviewFormat();

            if (pref.getBoolean("cameraset", false)) {
                param.setPreviewSize(pref.getInt("preWidth", 640), pref.getInt("preHeight", 480));
                param.setPictureSize(pref.getInt("picWidth", 1280), pref.getInt("picHeight", 960));
                Logger.logOMR("picture > width = " + param.getPictureSize().width + ", height = " + param.getPictureSize().height);
                Logger.logOMR("preview > width = " + param.getPreviewSize().width + ", height = " + param.getPreviewSize().height);
            } else {
                Camera.Size previewsize = getBestPreviewSize(param);
                Camera.Size picturesize = getBestPictureSize(param);
                param.setPreviewSize(previewsize.width, previewsize.height);
                param.setPictureSize(picturesize.width, picturesize.height);
                Logger.logOMR("AISS > cam >pictureSize > " + picturesize.width + "," + picturesize.height);
                Logger.logOMR("AISS > cam >previewSize > " + previewsize.width + "," + previewsize.height);

                float sizefactor = (float) ((picturesize.width) / (640.0));
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("preWidth", previewsize.width);
                editor.putInt("preHeight", previewsize.height);
                editor.putInt("picWidth", picturesize.width);
                editor.putInt("picHeight", picturesize.height);
                editor.putFloat("sizeFactor", sizefactor);
                editor.putBoolean("cameraset", true);
                editor.commit();
            }

            // ((MainActivity)mContext).onPictureSize(param.getPictureSize());
            // modify parameter
            setFocusMode(param);
            setSceneMode(param);
            setFlashMode(param);

            mSurfaceView.setAspectRatio(480, 640);
            Camera.CameraInfo info =
                    new Camera.CameraInfo();
            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
            int rotation = ((MainActivity) mContext).getWindowManager().getDefaultDisplay()
                    .getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break;
                case Surface.ROTATION_90:
                    degrees = 90;
                    break;
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;
                default:
                    degrees = 0;
                    break;
            }

            int result;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360;  // compensate the mirror
            } else {  // back-facing
                result = (info.orientation - degrees + 360) % 360;
            }
            Logger.logOMR("display orientaion" + result);
            //            mTextureView.setRotation();
            mCamera.setDisplayOrientation(result);
            //            param.setRotation(result);
            mCamera.setParameters(param);
        }
        try {
            // The Surface has been created, now tell the mCamera where to draw
            // the preview.
            if (mPreviewSurfaceTexture != null) {
                mCamera.setPreviewTexture(mPreviewSurfaceTexture);
                mCamera.startPreview();
            }
        } catch (Exception e) {
            // check for exceptions
            e.printStackTrace();
            releaseCamera();
            showErrorAndFinish();
            return;
        }
    }

    private void setFocusMode(Camera.Parameters param) {
        List<String> focusModes = param.getSupportedFocusModes();
        Logger.logOMR("focusModes=" + focusModes);
        if (focusModes != null && !focusModes.isEmpty()) {

            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                param.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            } else if (focusModes.contains(Camera.Parameters.FOCUS_MODE_MACRO)) {
                param.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);

            }

        }
    }

    private void setFlashMode(Camera.Parameters param) {
        List<String> flash = param.getSupportedFlashModes();
        Logger.logOMR("flash=" + flash);
        if (flash != null && !flash.isEmpty() && flash.contains(Camera.Parameters.FLASH_MODE_OFF)) {
            param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            Logger.logOMR("flash tourch");
        }
    }

    private Camera.Size getBestPreviewSize(Camera.Parameters param) {
        List psizes = param.getSupportedPreviewSizes();
        Camera.Size nearAspect = null;
        Camera.Size nearArea = null;
        int previewDesiredarea = 480 * 640;
        for (int ii = 0; ii < psizes.size(); ii++) {
            Camera.Size temp = (Camera.Size) psizes.get(ii);
            Logger.logOMR("AISS > cam >Preview > " + ii + " > " + temp.width + "," + temp.height);
            int temparea = temp.width * temp.height;
            if (temp.width == 640 && temp.height == 480)
                return temp;

            if ((double) temp.width / 640.0 == (double) temp.height / 480.0) {
                if (nearAspect == null)
                    nearAspect = temp;
                else if (temparea < previewDesiredarea * 2 + 1 && temparea > previewDesiredarea)
                    nearAspect = temp;
            }
            if (temparea < previewDesiredarea * 2 + 1 && temparea > previewDesiredarea)
                nearArea = temp;
        }

        if (nearAspect == null) {
            if (nearArea == null)
                return (Camera.Size) psizes.get(0);
            else
                return nearArea;
        } else
            return nearAspect;
    }

    private Camera.Size getBestPictureSize(Camera.Parameters param) {
        List<Size> psizes = param.getSupportedPictureSizes();
        sortList(psizes);
        Camera.Size nearAspect = null;
        Camera.Size nearArea = null;
        int previewDesiredarea = 960 * 1280;
        for (int ii = 0; ii < psizes.size(); ii++) {
            Camera.Size temp = psizes.get(ii);
//            Logger.getinstance().error(this, "width" + psizes.get(ii).width + " height" + psizes.get(ii).height);
            Logger.logOMR("AISS > cam >Picture > " + ii + " > " + temp.width + "," + temp.height);
            int temparea = temp.width * temp.height;
            if (temp.width == 1280 && temp.height == 960)
                return temp;

            if ((double) temp.width / 640.0 == (double) temp.height / 480.0) {
                if (nearAspect == null && temp.width > 640) {
                    nearAspect = temp;
                    break;
                } else if (temparea < previewDesiredarea * 2 && temparea > previewDesiredarea / 2 - 1)
                    nearAspect = temp;
            }
            if (temparea < previewDesiredarea * 2 + 1 && temparea > previewDesiredarea)
                nearArea = temp;
        }

        if (nearAspect == null) {
            if (nearArea == null)
                return psizes.get(0);
            else
                return nearArea;
        } else
            return nearAspect;
    }

    void sortList(List<Size> list) {
        Collections.sort(list, new Comparator<Size>() {
            @Override
            public int compare(Camera.Size size, Camera.Size t1) {
                return size.width - t1.width;

            }
        });
    }

    public void setSceneMode(Camera.Parameters param) {
        List<String> scenceModes = param.getSupportedSceneModes();
        if (scenceModes != null && !scenceModes.isEmpty()) {
            if (scenceModes.contains(Camera.Parameters.SCENE_MODE_BARCODE))
                param.setSceneMode(Camera.Parameters.SCENE_MODE_BARCODE);
            else if (scenceModes.contains(Camera.Parameters.SCENE_MODE_AUTO))
                param.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && scenceModes.contains(Camera.Parameters.SCENE_MODE_HDR))
                param.setSceneMode(Camera.Parameters.SCENE_MODE_HDR);
        }
    }

    public void onDestroy() {
        releaseCamera();
        if (frameListener != null)
            frameListener.shutDown();
    }

    private void showErrorAndFinish() {
        Toast.makeText(mContext.getApplicationContext(), "Camera Error", Toast.LENGTH_LONG).show();
        if (mContext instanceof MainActivity)
            ((MainActivity) mContext).finish();
    }

    public void recycleFrames() {
        frameListener.recycleFrames();
    }
}