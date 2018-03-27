package com.xseed.ocvsample.ocvsample.listener;

import android.content.Context;

import com.xseed.ocvsample.ocvsample.datasource.CircleDS;
import com.xseed.ocvsample.ocvsample.datasource.CircleRatios;
import com.xseed.ocvsample.ocvsample.datasource.DotDS;
import com.xseed.ocvsample.ocvsample.datasource.MatDS;
import com.xseed.ocvsample.ocvsample.helper.BlobHelper;
import com.xseed.ocvsample.ocvsample.helper.CircleHelper;
import com.xseed.ocvsample.ocvsample.helper.DotHelper;
import com.xseed.ocvsample.ocvsample.pojo.Circle;
import com.xseed.ocvsample.ocvsample.utility.Constants;
import com.xseed.ocvsample.ocvsample.utility.ErrorType;
import com.xseed.ocvsample.ocvsample.utility.Logger;
import com.xseed.ocvsample.ocvsample.utility.SheetConstants;

import java.util.ArrayList;

/**
 * Created by Manvendra Sah on 16/06/17.
 */

public class FrameListener extends AbstractFrameListener {

    ArrayList<Circle> circles = new ArrayList<Circle>();
    protected int operations;
    protected int circleThreadCount;

    private CircleDS circleData; // datasource for circles detected
    private DotDS dotData;  // datasource for boundary dots
    private MatDS matDS;  // datasource for all mat and bitmap variables

    private CircleHelper circleHelper;
    private DotHelper dotHelper;
    private BlobHelper blobHelper;

    private CircleRatios cRatios;

    public FrameListener(Context context) {
        mContext = context;
    }

    @Override
    protected void refreshStateVariables() {
        circles = new ArrayList<>();
        operations = 0;
        circleThreadCount = 0;
        matDS = new MatDS();
        circleHelper = new CircleHelper();
        dotHelper = new DotHelper();
    }

    @Override
    protected void onOpencvLibraryLoaded() {
        if (frame != null) {
            matDS.createBaseBitmap(frame);
            Logger.logOCV("time > getBitmapFromByte =  " + (System.currentTimeMillis() - dT));
            matDS.createBaseMat(frame);
            Logger.logOCV("time > getBaseRotatedMat =  " + (System.currentTimeMillis() - dT));
            findCircles();
            prepareMatForBlobDetection();
        }
    }

    /* create grayscale mat with adaptive thresh-hold applied to remove shadows
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
                        if (Constants.IS_DEBUG)
                            postBitmap(matDS.getBitmapWithAdaptiveThreshToDraw(), AbstractFrameListener.TAG_ADAPTIVE_THRESHHOLD);
                    }
                });
                Logger.logOCV("time > Mat created for blob detection > " + (System.currentTimeMillis() - dT));
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
                Logger.logOCV("boundaryDots = " + dotData.toString());
                Logger.logOCV("time > boundary dots detected : " + (System.currentTimeMillis() - dT));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        onDotsDetection();
                    }
                });
            }
        }.start();
    }

    /*    divide image into two halves and do circle detection parallelly
        then combine circles detected*/
    private void findCircles() {
        operations++;
        new Thread(new Runnable() {
            @Override
            public void run() {
                circleThreadCount++;
                ArrayList<Circle> topHalfCircles = circleHelper.findCircles(matDS.getTopHalfMat());
                Logger.logOCV("top Half circles = " + circles.size() + "\n");
                Logger.logOCV("time > top circles detected : " + (System.currentTimeMillis() - dT));
                onHalfCircles(topHalfCircles);
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                circleThreadCount++;
                ArrayList<Circle> bottomHalfCircles = circleHelper.findCircles(matDS.getBottomHalfMat());
                Logger.logOCV("bottom Half circles = " + circles.size());
                Logger.logOCV("time > bottom circles detected : " + (System.currentTimeMillis() - dT));
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
        if (circleThreadCount <= 0)
            onBothHalvesCircles();
    }

    private synchronized void onBothHalvesCircles() {
        if (Constants.IS_DEBUG) {
            circleHelper.drawCirclesOnMat(circles, matDS.getCircleMatToDraw());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    postBitmap(matDS.getCircleBitmapToDraw(), TAG_INITIAL_CIRCLES);
                }
            });
        }
        Logger.logOCV("total circles = " + circles.size());
        Logger.logOCV("time > total circles detected : " + (System.currentTimeMillis() - dT));
        handler.post(new Runnable() {
            @Override
            public void run() {
                onCircleDetection();
            }
        });
    }

    public void onCircleDetection() {
        operations--;
        checkOperationCount();
    }

    public void onDotsDetection() {
        operations--;
        checkOperationCount();
    }

    private void checkOperationCount() {
        if (operations <= 0) {
            if (!dotData.isValid()) {
                postError(ErrorType.TYPE1);
                return;
            }
            cRatios = new CircleRatios(dotData);
            Logger.logOCV(cRatios.toString());
            if (!cRatios.areValidLineRatios()) {
                postError(ErrorType.TYPE2);
                return;
            }
            int numCircles = circles.size();
            if (numCircles < SheetConstants.MIN_DETECTED_CIRCLES) {
                postError(ErrorType.TYPE3);
                return;
            }
            getCircleData();
        }
    }

    /*
        create circle datasource and start detecting marked answers/grades
    */
    private void getCircleData() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                circleHelper.createDataSource(circles, matDS.getBaseMat().rows(), matDS.getBaseMat().cols(), dotData, cRatios);
                if (circleHelper.isError()) {
                    postError(circleHelper.getError());
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
                        postBitmap(matDS.getAnswerBitmapToDraw(), TAG_BLOBS_DETECTED);
                        postSuccess();
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
                    postBitmap(matDS.getElementBitmap(), TAG_ELEMENTS);
                }
            }.start();
        }
    }

    @Override
    public void recycleFrames() {
        matDS.release();
    }
}
