package com.xseed.ocvsample.ocvsample.scanbase.utility;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Manvendra Sah on 21/08/17.
 */

public class Dummy {

  /*  double maxArea = -1;
    int maxAreaIdx = -1;
                    Log.d("size", Integer.toString(contours.size()));
    MatOfPoint temp_contour = contours.get(0); //the largest is at the index 0 for starting point
    MatOfPoint2f approxCurve = new MatOfPoint2f();
    MatOfPoint largest_contour = contours.get(0);
    //largest_contour.ge
    List<MatOfPoint> largest_contours = new ArrayList<MatOfPoint>();
    //Imgproc.drawContours(imgSource,contours, -1, new Scalar(0, 255, 0), 1);

                    for (int idx = 0; idx < contours.size(); idx++) {
        temp_contour = contours.get(idx);
        double contourarea = Imgproc.contourArea(temp_contour);
        //compare this contour to the previous largest contour found
        if (contourarea > maxArea) {
            //check if this contour is a square
            MatOfPoint2f new_mat = new MatOfPoint2f(temp_contour.toArray());
            int contourSize = (int) temp_contour.total();
            MatOfPoint2f approxCurve_temp = new MatOfPoint2f();
            Imgproc.approxPolyDP(new_mat, approxCurve_temp, contourSize * 0.05, true);
            if (approxCurve_temp.total() == 4) {
                maxArea = contourarea;
                maxAreaIdx = idx;
                approxCurve = approxCurve_temp;
                largest_contour = temp_contour;
            }
        }
    }

    Imgproc.cvtColor(processedMatData, processedMatData, Imgproc.COLOR_BayerBG2RGB);

    double[] temp_double;
    temp_double = approxCurve.get(0, 0);
    Point p1 = new Point(temp_double[0], temp_double[1]);
    //Core.circle(imgSource,p1,55,new Scalar(0,0,255));
    //Imgproc.warpAffine(sourceImage, dummy, rotImage,sourceImage.size());
    temp_double = approxCurve.get(1, 0);
    Point p2 = new Point(temp_double[0], temp_double[1]);
    // Core.circle(imgSource,p2,150,new Scalar(255,255,255));
    temp_double = approxCurve.get(2, 0);
    Point p3 = new Point(temp_double[0], temp_double[1]);
    //Core.circle(imgSource,p3,200,new Scalar(255,0,0));
    temp_double = approxCurve.get(3, 0);
    Point p4 = new Point(temp_double[0], temp_double[1]);
    // Core.circle(imgSource,p4,100,new Scalar(0,0,255));
    List<Point> source = new ArrayList<Point>();
                    source.add(p1);
                    source.add(p2);
                    source.add(p3);
                    source.add(p4);
    Mat startM = Converters.vector_Point2f_to_Mat(source);
    Mat result = warp(orgMat, startM);
    saveBitmapToDisk(result, "contour");*/


    public Mat warp(Mat inputMat, Mat startM) {
        int resultWidth = 480;
        int resultHeight = 640;

        Mat outputMat = new Mat(resultWidth, resultHeight, CvType.CV_8UC4);

        Point ocvPOut1 = new Point(0, 0);
        Point ocvPOut2 = new Point(0, resultHeight);
        Point ocvPOut3 = new Point(resultWidth, resultHeight);
        Point ocvPOut4 = new Point(resultWidth, 0);
        List<Point> dest = new ArrayList<Point>();
        dest.add(ocvPOut1);
        dest.add(ocvPOut2);
        dest.add(ocvPOut3);
        dest.add(ocvPOut4);
        Mat endM = Converters.vector_Point2f_to_Mat(dest);

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(startM, endM);

        Imgproc.warpPerspective(inputMat,
                outputMat,
                perspectiveTransform,
                new Size(resultWidth, resultHeight),
                Imgproc.INTER_CUBIC);

        return outputMat;
    }

   /*
     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (MyCameraView) findViewById(R.id.show_camera_activity_java_surface_view);
        mOpenCvCameraView.setMaxFrameSize(480, 640);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }*/

   /* @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(width, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        // Rotate mRgba 90 degrees
        Imgproc.cvtColor(inputFrame.gray(), mRgba,
                Imgproc.COLOR_GRAY2RGBA, 4);
        Core.transpose(mRgba, mRgbaT);
        Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0, 0, 0);
        Core.flip(mRgbaF, mRgba, 1);
        return mRgba;
    }
*/


//                    int width = bitmap.getWidth();
//                    int height = bitmap.getHeight();
//                    mRgba = new Mat(height, width, CvType.CV_8UC4);
//                    mRgbaF = new Mat(height, width, CvType.CV_8UC4);
//                    mRgbaT = new Mat(width, width, CvType.CV_8UC4);
//                    mRgba = processedMatData;
//                    Core.transpose(mRgba, mRgbaT);
//                    Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0, 0, 0);
//                    Core.flip(mRgbaF, mRgba, 1);

//doImageProcessing();
//  doImageProcessing();
                   /* Bitmap baseBitmap = null;

                    baseMat = processedMatData.clone();
                    Imgproc.cvtColor(processedMatData, processedMatData, Imgproc.COLOR_RGB2GRAY);
                    Imgproc.threshold(processedMatData, processedMatData, 100, 240, Imgproc.THRESH_BINARY);
                    Utils.matToBitmap(processedMatData, baseBitmap);
                    computeResult(baseBitmap, FrameConstants.DARKNESSFILTER, FrameConstants.SIZEFACTOR);*/
}


