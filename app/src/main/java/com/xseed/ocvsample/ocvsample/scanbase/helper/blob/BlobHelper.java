package com.xseed.ocvsample.ocvsample.scanbase.helper.blob;

import android.graphics.Bitmap;

import com.xseed.ocvsample.ocvsample.scanbase.datasource.BlobDS;
import com.xseed.ocvsample.ocvsample.scanbase.datasource.CircleDS;
import com.xseed.ocvsample.ocvsample.scanbase.datasource.CircleRatios;
import com.xseed.ocvsample.ocvsample.scanbase.datasource.PrimaryDotDS;
import com.xseed.ocvsample.ocvsample.scanbase.pojo.Blob;
import com.xseed.ocvsample.ocvsample.scanbase.pojo.Circle;
import com.xseed.ocvsample.ocvsample.scanbase.utility.Logger;
import com.xseed.ocvsample.ocvsample.scanbase.utility.SheetConstants;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Manvendra Sah on 15/09/17.
 */

public class BlobHelper extends AbstractBlobHelper {

    public BlobHelper(PrimaryDotDS dotData, CircleDS circleData, Bitmap bitmap, CircleRatios cRatios) {
        this.dotData = dotData;
        this.circleData = circleData;
        this.cRatios = cRatios;
        this.bitmap = bitmap;
        blobData = new BlobDS();
    }

    public BlobDS getBlobData() {
        return blobData;
    }

    @Override
    protected void findAnswersBlobs(int startIndex) {
        int endIndex = circleData.transwerCircleMap.size();
        findAnswersBlobs(startIndex, endIndex);
    }

    @Override
    protected void findAnswersBlobs(int startIndex, int endIndex) {
        Logger.logOCV("PENCIL DETECTION IN ANSWERS -----------------" + startIndex + "," + endIndex);

        for (int i = startIndex; i <= endIndex; ++i) {
            Logger.logOCV("ANSWER > index = " + i);
            Blob blob = getDarkestCircleInList(circleData.transwerCircleMap.get(i), i, SheetConstants.TYPE_ANSWER);
            if (blob != null)
                blobData.setAnswerBlob(i, blob);
        }
        /*Set<Integer> set = circleData.transwerCircleMap.keySet();
        for (Integer i : set) {
            Blob blob = getDarkestCircleInList(circleData.transwerCircleMap.get(i), i, SheetConstants.TYPE_ANSWER);
            if (blob != null)
                blobData.setAnswerBlob(blob);
        }*/
    }

    @Override
    protected void findIdBlobs() {
        Logger.logOCV("PENCIL DETECTION IN IDS -----------------");
        int ind = 0;
        for (ArrayList<Circle> list : circleData.idCircleMap.values()) {
            Logger.logOCV("ID > index = " + ind);
            Blob blob = getDarkestCircleInList(list, ind, SheetConstants.TYPE_ID);
            if (blob != null)
                blobData.setIdBlob(blob);
            ++ind;
        }
    }

    @Override
    protected void findGradeBlobs() {
        Logger.logOCV("PENCIL DETECTION IN GRADE -----------------");
        ArrayList<Circle> list = circleData.gradeCircleMap.get(0);
        Logger.logOCV("GRADE > index = " + 0);
        Blob blob = getDarkestCircleInList(list, 0, SheetConstants.TYPE_GRADE);
        if (blob != null)
            blobData.setGradeBlob(blob);
    }


    public synchronized void drawBlobsOnMat(Mat mat) {
        for (Blob blob : blobData.gradeBlobs) {
            Imgproc.circle(mat, blob.circle.center, (int) Math.ceil(cRatios.avgIdGradeRadius), new Scalar(10, 255, 10), 2);
            //Imgproc.rectangle(mat, getTopLeftPointOfCircle(blob), getBottomRightPointOfCircle(blob), new Scalar(10, 255, 10), 2);
            Imgproc.putText(mat, String.valueOf(blob.index), getPointToRightForText(blob), Core.FONT_HERSHEY_COMPLEX_SMALL, 1, new Scalar(54, 31, 200), 2);
        }
        for (Map.Entry<Integer, Blob> entry : blobData.idBlobs.entrySet()) {
            Blob blob = entry.getValue();
            Imgproc.circle(mat, blob.circle.center, (int) Math.ceil(cRatios.avgIdGradeRadius), new Scalar(10, 255, 10), 2);
            //Imgproc.rectangle(mat, getTopLeftPointOfCircle(blob), getBottomRightPointOfCircle(blob), new Scalar(10, 255, 10), 2);
            Imgproc.putText(mat, String.valueOf(blob.index), getPointToRightForText(blob), Core.FONT_HERSHEY_COMPLEX_SMALL, 1, new Scalar(54, 31, 200), 2);
        }
        for (Map.Entry<Integer, Blob> entry : blobData.answerBlobs.entrySet()) {
            Blob blob = entry.getValue();
            Imgproc.circle(mat, blob.circle.center, (int) Math.ceil(cRatios.avgAnswerRadius), new Scalar(10, 255, 10), 2);
            //Imgproc.rectangle(mat, getTopLeftPointOfCircle(blob), getBottomRightPointOfCircle(blob), new Scalar(10, 255, 10), 2);
            Imgproc.putText(mat, blob.superIndex + getAlphaForAnswerSubIndex(blob.index), getPointToBottomForText(blob), Core.FONT_HERSHEY_COMPLEX_SMALL, 1.10, new Scalar(54, 31, 200), 2);
        }
    }
}
