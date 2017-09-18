package com.xseed.ocvsample.ocvsample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Handler;
import android.view.Surface;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

/**
 * Created by Manvendra Sah on 16/06/17.
 */

public class FrameListener implements Camera.PictureCallback {

    private Context mContext = null;
    private OMRSheetListener mSheetListener;
    private Mat baseMat, blobMat, finalMat, circleMat;
    private Bitmap baseBitmap, blobBitmap, cvBitmap, threshBitmap, circleBitmap;
    private FrameModel frame;
    private long dT;
    Handler handler = new Handler();
    private int instCount = 0, inInstCount = 0;
    ArrayList<Circle> circles = new ArrayList<Circle>();
    private CircleDS circleData;
    private CircleHelper circleHelper;
    private DotHelper dotHelper;
    private DotDS dotData;
    private BlobHelper blobHelper;
    private CircleRatios cRatios;

    public FrameListener(Context context) {
        mContext = context;
    }

    public void startProcessingFrame() {
        if (mContext instanceof OMRSheetListener)
            mSheetListener = (OMRSheetListener) mContext;
    }

    @Override
    public void onPictureTaken(final byte[] data, Camera camera) {
        instCount = 0;
        inInstCount = 0;
        dT = System.currentTimeMillis();
        circleHelper = new CircleHelper();
        //  lineHelper = new LineHelper();
        dotHelper = new DotHelper();
        Camera.Parameters parameters = camera.getParameters();
        FrameModel fModel = new FrameModel(data);
        fModel.setPreviewWidth(parameters.getPreviewSize().width);
        fModel.setPreviewHeight(parameters.getPreviewSize().height);
        fModel.setRotation(getRotation());
        validateFrame(fModel);
    }

    private int getRotation() {
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

    private void validateFrame(FrameModel frame) {
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

                    if (frame != null) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Config.ARGB_8888;
                        options.inMutable = true;
                        baseBitmap = BitmapFactory.decodeByteArray(frame.getData(), 0, frame.getData().length, options);
                        Logger.logOCV(" getBitmapFromByte =  " + (System.currentTimeMillis() - dT));

                        baseMat = new Mat();
                        Utils.bitmapToMat(baseBitmap, baseMat);
                        Logger.logOCV(" getMatFromBitmap =  " + (System.currentTimeMillis() - dT));
                        if (frame.getRotation() == 270) {
                            Core.flip(baseMat, baseMat, 0);
                        } else if (frame.getRotation() == 180) {
                            Core.flip(baseMat, baseMat, -1);
                        } else if (frame.getRotation() == 90) {
                            Core.flip(baseMat.t(), baseMat, 1);
                        }
                        finalMat = baseMat.clone();
                        Logger.logOCV(" getRotatedMat =  " + (System.currentTimeMillis() - dT));
                        prepareMatForBlobDetection();
                        findCircles();
                        findBoundaryDots();
                    }
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

    private void prepareMatForBlobDetection() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                blobMat = finalMat.clone();
                Imgproc.cvtColor(blobMat, blobMat, Imgproc.COLOR_RGB2GRAY);
                Imgproc.adaptiveThreshold(blobMat, blobMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
                        Imgproc.THRESH_BINARY, 69, 10);
                threshBitmap = Bitmap.createBitmap(blobMat.cols(), blobMat.rows(), Config.ARGB_8888);
                Utils.matToBitmap(blobMat, threshBitmap);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mSheetListener != null)
                            mSheetListener.onOMRSheetBitmap(threshBitmap, "AdaptThresh");
                    }
                });
            }
        }.start();
    }

    private void findBoundaryDots() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                ++inInstCount;
                dotData = dotHelper.findDots(baseMat);
                Logger.logOCV("boundaryDots = " + dotData.toString() + ", time : " + (System.currentTimeMillis() - dT));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onDotsDetection(dotData);
                    }
                });
            }
        }.start();
    }

    private void findCircles() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                ++inInstCount;
                circles = circleHelper.findCircles(baseMat);
                circleMat = finalMat.clone();
                circleHelper.drawCirclesOnMat(circles, circleMat);
                circleBitmap = Bitmap.createBitmap(circleMat.cols(), circleMat.rows(), Config.ARGB_8888);
                Utils.matToBitmap(circleMat, circleBitmap);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mSheetListener != null)
                            mSheetListener.onOMRSheetBitmap(circleBitmap, "InitialCircles");
                    }
                });
                Logger.logOCV("circles = " + circles.size() + "   time =  " + (System.currentTimeMillis() - dT));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onCircleDetection(circles);
                    }
                });
            }
        }.start();
    }

    public void onCircleDetection(ArrayList<Circle> list) {
        this.circles = list;
        instCount++;
        checkInstCount();
    }

    public void onDotsDetection(DotDS data) {
        instCount++;
        checkInstCount();
    }

    private void checkInstCount() {
        if (instCount == inInstCount) {
            if (!dotData.isValid()) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mSheetListener != null)
                            mSheetListener.onOMRSheetGradingFailed(ErrorType.TYPE1);
                    }
                });
                return;
            }
            cRatios = new CircleRatios(dotData, baseMat);
            Logger.logOCV(cRatios.toString());
            if (!cRatios.areValidLineRatios()) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mSheetListener != null)
                            mSheetListener.onOMRSheetGradingFailed(ErrorType.TYPE2);
                    }
                });
                return;
            }
            int numCircles = circles.size();
            if (numCircles < SheetConstants.MIN_DETECTED_CIRCLES) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mSheetListener != null)
                            mSheetListener.onOMRSheetGradingFailed(ErrorType.TYPE3);
                    }
                });
                return;
            }
            getCircleData();
        }
    }

    private void getCircleData() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                circleHelper.createDataSource(circles, baseMat, dotData, cRatios);
                if (circleHelper.isError()) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mSheetListener != null) {
                                mSheetListener.onOMRSheetGradingFailed(circleHelper.getError());
                            }
                        }
                    });
                    return;
                }
                circleData = circleHelper.getCircleData();
                drawElementsOnMat();
                circleHelper.transformAnswerCircleMap();
                Logger.logOCV("blob detection START time = " + (System.currentTimeMillis() - dT));

                blobHelper = new BlobHelper(dotData, circleData, finalMat, threshBitmap, cRatios);
                blobHelper.findBlobsInCircles();
                Logger.logOCV("blob detection END time = " + (System.currentTimeMillis() - dT));
                blobHelper.drawBlobsOnMat();
                Logger.logOCV("blob draw on mat time = " + (System.currentTimeMillis() - dT));

                blobBitmap = Bitmap.createBitmap(finalMat.cols(), finalMat.rows(), Config.ARGB_8888);
                Utils.matToBitmap(finalMat, blobBitmap);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mSheetListener != null) {
                            mSheetListener.onOMRSheetBitmap(blobBitmap, "BlobDetect");
                            mSheetListener.onOMRSheetGradingComplete();
                        }
                    }
                });
                Logger.logOCV("circle&Line time = " + (System.currentTimeMillis() - dT));
            }
        }.start();
    }

    private void drawElementsOnMat() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                dotHelper.drawLinesOnMat(baseMat);
                circleHelper.drawCirclesOnMat();
                cvBitmap = Bitmap.createBitmap(baseMat.cols(), baseMat.rows(), Config.ARGB_8888);
                Utils.matToBitmap(baseMat, cvBitmap);
                dotHelper.drawDotsOnBitmap(cvBitmap);
                if (mSheetListener != null)
                    mSheetListener.onOMRSheetBitmap(cvBitmap, "DotNCircle");
            }
        }.start();
    }

    public void shutDown() {
        mSheetListener = null;
    }

    public void recycleFrames() {
        baseMat = null;
        blobMat = null;
        finalMat = null;
        circleMat = null;
        recycleBitmap(baseBitmap);
        recycleBitmap(blobBitmap);
        recycleBitmap(cvBitmap);
        recycleBitmap(threshBitmap);
        recycleBitmap(circleBitmap);
    }

    public void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled())
            bitmap.recycle();
    }
}
