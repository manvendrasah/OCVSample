package com.xseed.ocvsample.ocvsample.listener;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.view.Surface;

import com.xseed.ocvsample.ocvsample.android.MainActivity;
import com.xseed.ocvsample.ocvsample.datasource.CircleDS;
import com.xseed.ocvsample.ocvsample.datasource.CircleRatios;
import com.xseed.ocvsample.ocvsample.datasource.DotDS;
import com.xseed.ocvsample.ocvsample.datasource.MatDS;
import com.xseed.ocvsample.ocvsample.helper.BlobHelper;
import com.xseed.ocvsample.ocvsample.helper.CircleHelper;
import com.xseed.ocvsample.ocvsample.helper.DotHelper;
import com.xseed.ocvsample.ocvsample.pojo.Circle;
import com.xseed.ocvsample.ocvsample.pojo.FrameModel;
import com.xseed.ocvsample.ocvsample.utility.Constants;
import com.xseed.ocvsample.ocvsample.utility.ErrorType;
import com.xseed.ocvsample.ocvsample.utility.Logger;
import com.xseed.ocvsample.ocvsample.utility.SheetConstants;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.util.ArrayList;

/**
 * Created by Manvendra Sah on 16/06/17.
 */

public class FrameListener implements Camera.PictureCallback {

    private Context mContext = null;
    private OMRSheetListener mSheetListener;

    private FrameModel frame;
    private long dT;
    Handler handler = new Handler();
    ArrayList<Circle> circles = new ArrayList<Circle>();
    protected int operations;
    protected int circleThreadCount;
    private CircleDS circleData;
    private CircleHelper circleHelper;
    private DotHelper dotHelper;
    private DotDS dotData;
    private BlobHelper blobHelper;
    private CircleRatios cRatios;
    private MatDS matDS;


    public FrameListener(Context context) {
        mContext = context;
    }

    public void startProcessingFrame() {
        if (mContext instanceof OMRSheetListener)
            mSheetListener = (OMRSheetListener) mContext;
    }

    @Override
    public void onPictureTaken(final byte[] data, Camera camera) {
        circles = new ArrayList<>();
        operations = 0;
        circleThreadCount = 0;
        dT = System.currentTimeMillis();
        matDS = new MatDS();
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
                        matDS.createBaseBitmap(frame);
                        Logger.logOCV(" getBitmapFromByte =  " + (System.currentTimeMillis() - dT));
                        matDS.createBaseMat(frame);
                        Logger.logOCV(" getRotatedMat =  " + (System.currentTimeMillis() - dT));
                        findCircles();
                        prepareMatForBlobDetection();
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

    /* create grayscale mat with adaptive threshhold applied to remove shadows
    * - > get bitmap for blob detection after all circles and dots are found*/
    private void prepareMatForBlobDetection() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                matDS.createMatWithAdaptiveThreshhold();
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        findBoundaryDots();
                        if (mSheetListener != null && Constants.IS_DEBUG)
                            mSheetListener.onOMRSheetBitmap(matDS.getBitmapWithAdaptiveThreshToDraw(), "AdaptThresh");
                    }
                });
                Logger.logOCV("Mat created for blob detection > " + "time : " + (System.currentTimeMillis() - dT));
            }
        }.start();
    }

    private void findBoundaryDots() {
        operations++;
        new Thread() {
            @Override
            public void run() {
                super.run();
                dotData = dotHelper.findDots(matDS.getBitmapForDotDetection());
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
        operations++;
        new Thread(new Runnable() {
            @Override
            public void run() {
                circleThreadCount++;
                ArrayList<Circle> topHalfCircles = circleHelper.findCircles(matDS.getTopHalfMat());
                Logger.logOCV("top Half circles = " + circles.size() + "\n");
                onHalfCircles(topHalfCircles);
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                circleThreadCount++;
                ArrayList<Circle> bottomHalfCircles = circleHelper.findCircles(matDS.getBottomHalfMat());
                Logger.logOCV("bottom Half circles = " + circles.size());
                normalizeHalfCircles(bottomHalfCircles, (int) (matDS.getBaseMat().rows() * MatDS.PART_MULTIPLIER1));
                onHalfCircles(bottomHalfCircles);
            }
        }).start();
    }

    private void normalizeHalfCircles(ArrayList<Circle> circles, double topOffset) {
        for (Circle circle : circles) {
            circle.center.y += topOffset;
        }
    }

    private synchronized void onHalfCircles(ArrayList<Circle> circles) {
        circleThreadCount--;
        this.circles.addAll(circles);
        Logger.logOCV("total circles = " + circles.size());
        if (circleThreadCount <= 0)
            onBothHalvesCircles();
    }

    private synchronized void onBothHalvesCircles() {
        Logger.logOCV("onBothHalvesCircles = " + circles.size());
        if (Constants.IS_DEBUG) {
            circleHelper.drawCirclesOnMat(circles, matDS.getCircleMatToDraw());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (mSheetListener != null)
                        mSheetListener.onOMRSheetBitmap(matDS.getCircleBitmapToDraw(), "InitialCircles");
                }
            });
        }
        Logger.logOCV("circles = " + circles.size() + "   time =  " + (System.currentTimeMillis() - dT));
        handler.post(new Runnable() {
            @Override
            public void run() {
                onCircleDetection(circles);
            }
        });
    }

    public void onCircleDetection(ArrayList<Circle> list) {
        this.circles = list;
        operations--;
        checkOperationCount();
    }

    public void onDotsDetection(DotDS data) {
        operations--;
        checkOperationCount();
    }

    private void checkOperationCount() {
        if (operations <= 0) {
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
            cRatios = new CircleRatios(dotData);
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
                circleHelper.createDataSource(circles, matDS.getBaseMat().rows(), matDS.getBaseMat().cols(), dotData, cRatios);
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

                blobHelper = new BlobHelper(dotData, circleData, matDS.getBitmapForBlobDetection(), cRatios);
                blobHelper.findBlobsInCircles();
                Logger.logOCV("blob detection END time = " + (System.currentTimeMillis() - dT));
                blobHelper.drawBlobsOnMat(matDS.getAnswerMatToDraw());
                Logger.logOCV("blob draw on mat time = " + (System.currentTimeMillis() - dT));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mSheetListener != null) {
                            mSheetListener.onOMRSheetBitmap(matDS.getAnswerBitmapToDraw(), "BlobDetect");
                            mSheetListener.onOMRSheetGradingComplete();
                        }
                    }
                });
                Logger.logOCV("circle&Line time = " + (System.currentTimeMillis() - dT));
            }
        }.start();
    }

    private void drawElementsOnMat() {
        if (Constants.IS_DEBUG) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    dotHelper.drawLinesOnMat(matDS.getElementMat());
                    circleHelper.drawCirclesOnMat(matDS.getElementMat());
                    dotHelper.drawDotsOnBitmap(matDS.getElementBitmap());
                    if (mSheetListener != null)
                        mSheetListener.onOMRSheetBitmap(matDS.getElementBitmap(), "DotNCircle");
                }
            }.start();
        }
    }

    public void shutDown() {
        mSheetListener = null;
    }

    public void recycleFrames() {
        matDS.release();
    }
}
