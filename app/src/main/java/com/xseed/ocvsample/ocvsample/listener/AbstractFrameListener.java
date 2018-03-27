package com.xseed.ocvsample.ocvsample.listener;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Handler;
import android.view.Surface;

import com.xseed.ocvsample.ocvsample.android.MainActivity;
import com.xseed.ocvsample.ocvsample.pojo.FrameModel;
import com.xseed.ocvsample.ocvsample.utility.Logger;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

/**
 * Created by Manvendra Sah on 27/03/18.
 */

public abstract class AbstractFrameListener implements Camera.PictureCallback {

    protected Context mContext = null;
    protected OMRSheetListener mSheetListener;
    protected FrameModel frame;
    protected long dT;
    protected Handler handler = new Handler();

    public static final String TAG_ADAPTIVE_THRESHHOLD = "AdaptThresh";
    public static final String TAG_INITIAL_CIRCLES = "InitialCircles";
    public static final String TAG_BLOBS_DETECTED = "BlobDetect";
    public static final String TAG_ELEMENTS = "Elements";

    public void startProcessingFrame() {
        if (mContext instanceof OMRSheetListener)
            mSheetListener = (OMRSheetListener) mContext;
    }

    @Override
    public void onPictureTaken(final byte[] data, Camera camera) {
        dT = System.currentTimeMillis();
        refreshStateVariables();
        Camera.Parameters parameters = camera.getParameters();
        FrameModel fModel = new FrameModel(data);
        fModel.setPreviewWidth(parameters.getPreviewSize().width);
        fModel.setPreviewHeight(parameters.getPreviewSize().height);
        fModel.setRotation(getRotation());
        validateFrame(fModel);
    }

    protected abstract void refreshStateVariables();

    protected void validateFrame(FrameModel frame) {
        this.frame = frame;
        initialzeOpenCv(mContext);
    }

    private void initialzeOpenCv(Context context) {
        if (OpenCVLoader.initDebug()) {
            Logger.logOCV("OpenCv Sucess > " + (System.currentTimeMillis() - dT));
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Logger.logOCV("OpenCv Fail");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0, context, baseLoaderCallback);
        }
    }

    private BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(mContext) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);
            switch (status) {
                case BaseLoaderCallback.SUCCESS:
                    Logger.logOCV("onManagerConnected() > = " + (System.currentTimeMillis() - dT));
                    onOpencvLibraryLoaded();
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }

        @Override
        public void onPackageInstall(int operation, InstallCallbackInterface callback) {
            super.onPackageInstall(operation, callback);
        }
    };

    protected abstract void onOpencvLibraryLoaded();

    protected void postError(final int errorType) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mSheetListener != null) {
                    mSheetListener.onOMRSheetGradingFailed(errorType);
                }
            }
        });
    }

    protected void postBitmap(final Bitmap bitmap, final String tag) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mSheetListener != null) {
                    mSheetListener.onOMRSheetBitmap(bitmap, tag);
                }
            }
        });
    }

    protected void postSuccess() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mSheetListener != null) {
                    mSheetListener.onOMRSheetGradingComplete();
                }
            }
        });
    }

    protected int getRotation() {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
        int degrees = 0;
        int rotation = ((MainActivity) mContext).getWindowManager().getDefaultDisplay()
                .getRotation();
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
        return result;
    }

    public void shutDown() {
        mSheetListener = null;
    }

    public abstract void recycleFrames();
}
