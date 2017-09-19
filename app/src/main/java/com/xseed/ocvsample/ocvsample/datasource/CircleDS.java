package com.xseed.ocvsample.ocvsample.datasource;

import com.xseed.ocvsample.ocvsample.utility.SheetConstants;
import com.xseed.ocvsample.ocvsample.utility.Utility;
import com.xseed.ocvsample.ocvsample.pojo.Circle;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Manvendra Sah on 31/08/17.
 */

public class CircleDS {

    public ArrayList<Circle> idGradeCircleList = new ArrayList<Circle>();
    public TreeMap<Integer, ArrayList<Circle>> idGradeCircleMap = new TreeMap<>();

    public TreeMap<Integer, ArrayList<Circle>> answerCircleMap = new TreeMap<>();
    public TreeMap<Integer, ArrayList<Circle>> transwerCircleMap = new TreeMap<>(); // Transformed answer map
    public TreeMap<Integer, ArrayList<Circle>> idCircleMap = new TreeMap<>();
    public TreeMap<Integer, ArrayList<Circle>> gradeCircleMap = new TreeMap<>();

    public ArrayList<Circle> getAnswerRow(int index) {
        if (index >= SheetConstants.NUM_ROWS_ANSWERS)
            return null;
        return answerCircleMap.get(index);
    }

    @Override
    public String toString() {
        String temp = "\n";
        int ind = 0, total = 0;
        for (Map.Entry<Integer, ArrayList<Circle>> entry : answerCircleMap.entrySet()) {
            temp += "" + entry.getKey() + " > ";
            temp += entry.getValue().size() + " > ";
            temp += entry.getValue().toString() + "\n";
        }
        return temp;
    }

    public String getGradeCircleString() {
        String temp = "\n";
        int ind = 0, total = 0;
        for (Map.Entry<Integer, ArrayList<Circle>> entry : gradeCircleMap.entrySet()) {
            temp += "" + entry.getKey() + " > ";
            temp += entry.getValue().size() + " > ";
            temp += entry.getValue().toString() + "\n";
        }
        return temp;
    }

    public String getIdCircleString() {
        String temp = "\n";
        int ind = 0, total = 0;
        for (Map.Entry<Integer, ArrayList<Circle>> entry : idCircleMap.entrySet()) {
            temp += "" + entry.getKey() + " > ";
            temp += entry.getValue().size() + " > ";
            temp += entry.getValue().toString() + "\n";
        }
        return temp;
    }

    public String getTranswerCircleString() {
        String temp = "\n";
        int ind = 0, total = 0;
        for (Map.Entry<Integer, ArrayList<Circle>> entry : transwerCircleMap.entrySet()) {
            temp += "" + entry.getKey() + " > ";
            temp += entry.getValue().size() + " > ";
            temp += entry.getValue().toString() + "\n";
        }
        return temp;
    }

    public String getIdGradeCircleString() {
        String temp = "\n";
        int ind = 0, total = 0;
        for (Map.Entry<Integer, ArrayList<Circle>> entry : idGradeCircleMap.entrySet()) {
            temp += "" + entry.getKey() + " > ";
            temp += entry.getValue().size() + " > ";
            temp += entry.getValue().toString() + "\n";
        }
        return temp;
    }

    public void printAnswerCirclesOnMat(Mat baseMat) {
        int len = answerCircleMap.size();
        for (int i = 0; i < len; ++i)
            printAnswerCircleOnMat(baseMat, i, Utility.getRGBScalar(i));
    }

    public void printAnswerCircleOnMat(Mat baseMat, int index, Scalar scalar) {
        int ik = 0;
        if (index < SheetConstants.NUM_ROWS_ANSWERS) {
            ArrayList<Circle> list = answerCircleMap.get(index);
            for (Circle circle : list) {
                //  Imgproc.putText(baseMat, String.valueOf(ik), circle.center, Core.FONT_HERSHEY_COMPLEX_SMALL, 1, new Scalar(54, 31, 200), 2);
                Imgproc.circle(baseMat, circle.center, /*(int) Math.ceil(circle.radius)*/(int) Math.round(circle.radius),
                        circle.isExtrapolated ? new Scalar(240, 240, 30) : scalar, 3);
                //  Imgproc.rectangle(baseMat, getTopLeftPointOfCircle(circle), getBottomRightPointOfCircle(circle), new Scalar(10, 255, 10), 2);
                ik++;
            }
        }
    }

    public void printIdCirclesOnMat(Mat baseMat) {
        int len = idCircleMap.size();
        for (int i = 0; i < len; ++i) {
            ArrayList<Circle> list = idCircleMap.get(i);
            for (Circle circle : list) {
                Imgproc.circle(baseMat, circle.center, (int) Math.round(circle.radius),
                        circle.isExtrapolated ? new Scalar(240, 240, 30) : Utility.getRGBScalar(i), 3);
            }
        }
    }

    public void printGradeCirclesOnMat(Mat baseMat) {
        int len = gradeCircleMap.size();
        for (int i = 0; i < len; ++i) {
            ArrayList<Circle> list = gradeCircleMap.get(i);
            for (Circle circle : list) {
                Imgproc.circle(baseMat, circle.center, (int) Math.round(circle.radius),
                        circle.isExtrapolated ? new Scalar(240, 240, 30) : Utility.getRGBScalar(i), 3);
            }
        }
    }

    private Point getTopLeftPointOfCircle(Circle circle) {
        Point p = circle.center;
        double rad = circle.radius / 1.5;
        Point point = new Point();
        point.x = (int) (p.x - rad);
        point.y = (int) (p.y - rad);
        return point;
    }

    private Point getBottomRightPointOfCircle(Circle circle) {
        Point p = circle.center;
        double rad = circle.radius / 1.5;
        Point point = new Point();
        point.x = (int) (p.x + rad);
        point.y = (int) (p.y + rad);
        return point;
    }
}
