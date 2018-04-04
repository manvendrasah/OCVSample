package com.xseed.ocvsample.ocvsample.datasource;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;

import com.xseed.ocvsample.ocvsample.pojo.FrameModel;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

/**
 * Created by Manvendra Sah on 26/03/18.
 */

public class MatDS {

    private Mat baseMat, // mat of original image > never changes
            threshMat, // mat of original image with adaptive thresh-holding
            circleMat,// mat of original image for drawing initial circles
            elementMat,// mat of original image for drawing elements
            answerMat; // mat to draw final detected answers/grades.ids

    private Mat topHalfMat, // mats of top and bottom halves of original image with Gaussian Blurs
            bottomHalfMat;

    private Bitmap baseBitmap,// orignal bitmap
            threshBitmap, // bitmap of original with adaptive thresholding > never changes
            dotDetectionBitmap, // bitmap () used for detecting dots >  never changes
            blobDetectionBitmap, // bitmap () for detecting answer/grades/ids
            circleInitialBitmap, // bitmap of original image to draw initial circles
            answersDetectedBitmap,// bitmap of answerMat with detected answers marked
            elementBitmap; // bitmap of original image for drawing elements

    public static final double PART_MULTIPLIER1 = 0.5;
    public static final double PART_MULTIPLIER2 = 0.5;

    public void createBaseBitmap(FrameModel frame) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Config.ARGB_8888;
        options.inMutable = true;
        baseBitmap = BitmapFactory.decodeByteArray(frame.getData(), 0, frame.getData().length, options);
    }

    public void createBaseMat(FrameModel frame) {
        baseMat = new Mat();
        Utils.bitmapToMat(baseBitmap, baseMat);
        /*rotate mat instead of bitmap to save memory and processing time*/
        if (frame.getRotation() == 270) {
            Core.flip(baseMat, baseMat, 0);
        } else if (frame.getRotation() == 180) {
            Core.flip(baseMat, baseMat, -1);
        } else if (frame.getRotation() == 90) {
            Core.flip(baseMat.t(), baseMat, 1);
        }
        // final answers will be drawn onto answer mat
        answerMat = baseMat.clone();
        elementMat = baseMat.clone();
    }

    public Mat getBaseMat() {
        return baseMat;
    }

    public Mat getElementMat() {
        return elementMat;
    }

    public Bitmap getElementBitmap() {
        if (elementBitmap == null) {
            elementBitmap = Bitmap.createBitmap(elementMat.cols(), elementMat.rows(), Config.ARGB_8888);
            Utils.matToBitmap(elementMat, elementBitmap);
        }
        return elementBitmap;
    }

    public Mat getAnswerMatToDraw() {
        return answerMat;
    }

    public Bitmap getAnswerBitmapToDraw() {
        if (answersDetectedBitmap == null) {
            answersDetectedBitmap = Bitmap.createBitmap(answerMat.cols(), answerMat.rows(), Config.ARGB_8888);
            Utils.matToBitmap(answerMat, answersDetectedBitmap);
        }
        return answersDetectedBitmap;
    }

    public void createMatWithAdaptiveThreshhold() {
        // threshMat has adaptive threshhold and is used for detecting answers/grades
        threshMat = baseMat.clone();
        Imgproc.cvtColor(threshMat, threshMat, Imgproc.COLOR_RGB2GRAY);
        Imgproc.adaptiveThreshold(threshMat, threshMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C,
                Imgproc.THRESH_BINARY, 69, 10);
    }

    public Bitmap getBitmapWithAdaptiveThreshToDraw() {
        // return threshholded bitmap for debug
        if (threshBitmap == null) {
            threshBitmap = Bitmap.createBitmap(threshMat.cols(), threshMat.rows(), Config.ARGB_8888);
            Utils.matToBitmap(threshMat, threshBitmap);
        }
        return threshBitmap;
    }

    public Bitmap getBitmapForDotDetection() {
        // returns threshholded bitmap for dot detection
        if (dotDetectionBitmap == null) {
            dotDetectionBitmap = Bitmap.createBitmap(threshMat.cols(), threshMat.rows(), Config.ARGB_8888);
            Utils.matToBitmap(threshMat, dotDetectionBitmap);
        }
        return dotDetectionBitmap;
    }

    public Bitmap getBitmapForBlobDetection() {
        // returns threshholded bitmap for detecting answer blobs
        if (blobDetectionBitmap == null) {
            blobDetectionBitmap = Bitmap.createBitmap(threshMat.cols(), threshMat.rows(), Config.ARGB_8888);
            Utils.matToBitmap(threshMat, blobDetectionBitmap);
        }
        return blobDetectionBitmap;
    }

    public Mat getTopHalfMat() {
        if (topHalfMat == null) {
            final int rows = getBaseMat().rows();
            final int cols = getBaseMat().cols();
            Rect topExtractedRect = new Rect(
                    0, 0,
                    cols, (int) (rows * PART_MULTIPLIER1)
            );
            Mat tempMat = new Mat(getBaseMat(), topExtractedRect);
            topHalfMat = new Mat();
            Imgproc.cvtColor(tempMat, topHalfMat, Imgproc.COLOR_RGB2GRAY);
            Imgproc.GaussianBlur(topHalfMat, topHalfMat, new org.opencv.core.Size(3, 3), 4);
            Imgproc.GaussianBlur(topHalfMat, topHalfMat, new org.opencv.core.Size(3, 3), 12);
        }
        return topHalfMat;
    }

    public Mat getBottomHalfMat() {
        if (bottomHalfMat == null) {
            final int rows = getBaseMat().rows();
            final int cols = getBaseMat().cols();
            Rect bottomExtractedRect = new Rect(
                    0, (int) (rows * PART_MULTIPLIER1),
                    cols, (int) (rows * PART_MULTIPLIER2)
            );
            Mat tempMat = new Mat(getBaseMat(), bottomExtractedRect);
            bottomHalfMat = new Mat();
            Imgproc.cvtColor(tempMat, bottomHalfMat, Imgproc.COLOR_RGB2GRAY);
            Imgproc.GaussianBlur(bottomHalfMat, bottomHalfMat, new org.opencv.core.Size(3, 3), 4);
            Imgproc.GaussianBlur(bottomHalfMat, bottomHalfMat, new org.opencv.core.Size(3, 3), 12);
        }
        return bottomHalfMat;
    }

    public Mat getCircleMatToDraw() {
        if (circleMat == null)
            circleMat = baseMat.clone();
        return circleMat;
    }

    public Bitmap getCircleBitmapToDraw() {
        if (circleInitialBitmap == null) {
            circleInitialBitmap = Bitmap.createBitmap(circleMat.cols(), circleMat.rows(), Config.ARGB_8888);
            Utils.matToBitmap(circleMat, circleInitialBitmap);
        }
        return circleInitialBitmap;
    }

    public void release() {
        baseMat = null;
        threshMat = null;
        elementMat = null;
        circleMat = null;
        answerMat = null;
        topHalfMat = null;
        bottomHalfMat = null;
        recycleBitmap(baseBitmap);
        recycleBitmap(threshBitmap);
        recycleBitmap(dotDetectionBitmap);
        recycleBitmap(blobDetectionBitmap);
        recycleBitmap(circleInitialBitmap);
        recycleBitmap(answersDetectedBitmap);
        recycleBitmap(elementBitmap);
    }

    public void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled())
            bitmap.recycle();
        bitmap = null;
    }
}
