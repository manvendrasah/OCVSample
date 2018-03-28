package com.xseed.ocvsample.ocvsample.helper;

import android.text.TextUtils;

import com.xseed.ocvsample.ocvsample.comparator.AnswerCircleColumnComparator;
import com.xseed.ocvsample.ocvsample.comparator.AnswerCircleRowComparator;
import com.xseed.ocvsample.ocvsample.comparator.IdGradeCircleColumnComparator;
import com.xseed.ocvsample.ocvsample.datasource.CircleDS;
import com.xseed.ocvsample.ocvsample.datasource.CircleRatios;
import com.xseed.ocvsample.ocvsample.datasource.ConfigDS;
import com.xseed.ocvsample.ocvsample.datasource.DotDS;
import com.xseed.ocvsample.ocvsample.pojo.Circle;
import com.xseed.ocvsample.ocvsample.pojo.Line;
import com.xseed.ocvsample.ocvsample.pojo.OCVCircleConfig;
import com.xseed.ocvsample.ocvsample.utility.ErrorType;
import com.xseed.ocvsample.ocvsample.utility.Logger;
import com.xseed.ocvsample.ocvsample.utility.SheetConstants;
import com.xseed.ocvsample.ocvsample.utility.Utility;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by Manvendra Sah on 27/03/18.
 */

public abstract class AbstractCircleHelper {

    protected double avgAnswerRadius = 0;
    protected double avgIdGradeRadius = 0;
    protected ArrayList<Circle> circles;
    protected CircleDS circleData;
    protected DotDS dotData;
    protected int rows, cols;
    protected CircleRatios cRatios;
    protected int errorType = ErrorType.TYPE0;

    public CircleDS getCircleData() {
        return circleData;
    }

    public void createDataSource(ArrayList<Circle> circles, int rows, int cols,
                                 DotDS dotData, CircleRatios cRatios) {
        Logger.logOCV("baseMat size > " + cols + "," + rows);
        this.circles = circles;
        this.cRatios = cRatios;
        this.rows = rows;
        this.cols = cols;
        this.dotData = dotData;

        avgAnswerRadius = getAverageRadius(circles);
        cRatios.setAvgAnswerRadius(avgAnswerRadius);

        getFilteredListByRadius();
        ConfigDS.getInstance().setCirclesDetected(circles.size());
        createInitialCircleDataSource();
        filterDuplicateCircles(circleData.answerCircleMap);

        boolean canExtrapolateOuterAnswers = extrapolateOuterAnswerUndetectedCircles();
        if (!canExtrapolateOuterAnswers) return;
        boolean canExtrapolateMiddleAnswers = extrapolateMiddleAnswerUndetectedCircles();
        if (!canExtrapolateMiddleAnswers) return;
        boolean canFilterExtraAnswers = filterOutExtraAnswerCircles();
        if (!canFilterExtraAnswers) return;

        boolean canCreateIdGradeMap = createIdGradeCircleMap();
        if (!canCreateIdGradeMap) return;
        filterDuplicateCircles(circleData.idCircleMap);
        filterDuplicateCircles(circleData.gradeCircleMap);

        extrapolateIdGradeUndetectedCircles();
    }

    protected abstract boolean extrapolateOuterAnswerUndetectedCircles();

    protected abstract boolean extrapolateMiddleAnswerUndetectedCircles();

    protected abstract boolean extrapolateIdGradeUndetectedCircles();

    /* remove all circles with radius outside min and max threshhold values*/
    private void getFilteredListByRadius() {
        double maxRad = cRatios.getFilterMaxRadius();
        double minRad = cRatios.getFilterMinRadius();
        int deletedCircles = 0;
        ListIterator<Circle> iter = circles.listIterator();
        double tempMax = avgAnswerRadius;
        while (iter.hasNext()) {
            double rad = iter.next().radius;
            //  Logger.logOCV("iter > rad = " + rad);
            if (rad > maxRad || rad < minRad) {
                deletedCircles++;
                iter.remove();
            } else if (rad > tempMax) {
                //    Logger.logOCV("maxCircleRad = " + rad);
                tempMax = rad;
            }
        }
        //  avgRadius = tempMax;
        Logger.logOCV("normalised answer avgRad = " + avgAnswerRadius + " , filtered Circles by Radius = " + deletedCircles);
    }

    private void createInitialCircleDataSource() {
        circleData = new CircleDS();
        int numCircles = circles.size();
        Logger.logOCV("creteInitialDS > noOfCircles : " + numCircles);
        ArrayList<Circle> tempList = new ArrayList<>();
        tempList.addAll(circles);
        /* sort circles by descending values of y*/
        Collections.sort(tempList, new AnswerCircleColumnComparator());
        /* as a row can have max of SheetConstants.NUM_ROWS_ANSWERS answer circles,
          - > take twice this number (as sorting by descending Y may not always give the bottom most circles) for safety
          - > get perpendicular distance of these circles from nearest bottom line
          - > answer row map can be created thus row by row in a bottom-up manner
          */
        for (int i = 0; i < SheetConstants.NUM_ROWS_ANSWERS; ++i) {
            Line bottom = getBottomLine(circleData.answerCircleMap, i);
            int toIndex = tempList.size() > 2 * SheetConstants.NUM_ANSWERS_IN_ROW ? 2 * SheetConstants.NUM_ANSWERS_IN_ROW : tempList.size();
            if (toIndex > 0) {
                List<Circle> sample = tempList.subList(0, toIndex);
                ArrayList<Circle> row = getRowByPerpendicularFromNearestBottomLine(bottom, sample);
                circleData.answerCircleMap.put(i, row);
                Logger.logOCV("creteInitialDS > added row : " + i);
                tempList.removeAll(row);
            }
        }
        // all circles not in answer rows are presumed as id/grade circles, but need filtering
        circleData.idGradeCircleList = tempList;
        Logger.logOCV(circleData.toString());
    }

    private Line getBottomLine(TreeMap<Integer, ArrayList<Circle>> map, int i) {
       /* for lowest answer row, nearest bottom line is enclosed by bottom left and right dots*/
        if (i == 0)
            return dotData.getBottomLine();
        else {
            /* else get nearest available bottom line*/
            List<Circle> row = map.get(i - 1);
            int len = row.size();
            if (len < 2)
                return getBottomLine(map, i - 1);
            else {
                Point p1 = row.get(0).center;
                Point p2 = row.get(len - 1).center;
                return new Line(p1.x, p1.y, p2.x, p2.y);
            }
        }
    }

    private ArrayList<Circle> getRowByPerpendicularFromNearestBottomLine(Line line, List<Circle> sample) {
        /* create map with different rows from circles list sample by measuring perp distance from bottom line.
        * The row at 0 index with the required row
        * */
        ArrayList<Circle> finalRow;
        TreeMap<Integer, ArrayList<Circle>> map = new TreeMap<>();
        ListIterator<Circle> iter = sample.listIterator();
        double verticalThreshhold = cRatios.getVerticalThreshholdBetweenCirclesForSorting();

        while (iter.hasNext()) {
            Circle circle = iter.next();
            int perpendicular = (int) Utility.circleToLineDistance(line, circle);
            //circle.radius = avgAnswerRadius;
            Set<Integer> set = map.keySet();
            boolean isSet = false;
            for (Integer key : set) {
                if (Math.abs(key - perpendicular) <= verticalThreshhold) {
                    map.get(key).add(circle);
                    isSet = true;
                    break;
                }
            }
            if (!isSet) {
                ArrayList<Circle> listCircles = new ArrayList<>();
                listCircles.add(circle);
                map.put(perpendicular, listCircles);
            }
        }
        finalRow = map.get(map.firstKey());
        Collections.sort(finalRow, new AnswerCircleRowComparator());
        return finalRow;
    }

    private boolean createIdGradeCircleMap() {
        /*create circle map by segregating circles on basis of perpendicular distance from left line*/
        ArrayList<Circle> list = circleData.idGradeCircleList;
        avgIdGradeRadius = getAverageRadius(list);
        cRatios.avgIdGradeRadius = avgIdGradeRadius;
        Logger.logOCV(cRatios.toString());
        Line leftLine = dotData.getLeftLine();
        TreeMap<Integer, ArrayList<Circle>> map = new TreeMap<>();
        double horizontalThreshhold = cRatios.getHorizontalThreshholdBetweenIds();
        ListIterator<Circle> iter = list.listIterator();

        while (iter.hasNext()) {
            Circle circle = iter.next();
            int perpendicular = (int) Utility.circleToLineDistance(leftLine, circle);
            if (perpendicular > cols / 2)
                iter.remove();
            else {
                circle.leftDist = perpendicular;
                Set<Integer> set = map.keySet();
                boolean isSet = false;
                for (Integer key : set) {
                    if (Math.abs(key - perpendicular) <= horizontalThreshhold) {
                        map.get(key).add(circle);
                        isSet = true;
                        break;
                    }
                }
                if (!isSet) {
                    ArrayList<Circle> listCircles = new ArrayList<>();
                    listCircles.add(circle);
                    map.put(perpendicular, listCircles);
                }
            }
        }
        int index = 0;
        for (Map.Entry<Integer, ArrayList<Circle>> entry : map.entrySet()) {
            ArrayList<Circle> tempCol = entry.getValue();
            list.removeAll(tempCol);
            Collections.sort(tempCol, new IdGradeCircleColumnComparator());
            circleData.idGradeCircleMap.put(index, tempCol);
            // Logger.logOCV("idGradeCols > " + index + " > " + tempCol.toString());
            index++;
        }
        Logger.logOCV("allCirclesExhausted > " + list.isEmpty());
        Logger.logOCV("Id & Grade Circles > \n" + circleData.getIdGradeCircleString());
        createGradeCircleDS();
        createIdCircleDS();

        int realSize = 0;
        for (ArrayList<Circle> tempList : circleData.gradeCircleMap.values()) {
            if (!tempList.isEmpty()) realSize++;
        }
        for (ArrayList<Circle> tempList : circleData.idCircleMap.values()) {
            if (!tempList.isEmpty()) realSize++;
        }
        /* even if one column from id or grade isnt detected - > throw error*/
        if (realSize < 4) {
            errorType = ErrorType.TYPE8;
            return false;
        }
        return true;
    }

    private void createGradeCircleDS() {
        /*column with perpendicular distance from answer row col4 line, is below threshhold, is the grade column*/
        TreeSet<Integer> keySet = new TreeSet<>();
        Set<Integer> tempSet = circleData.idGradeCircleMap.keySet();
        keySet.addAll(tempSet);
        int size = keySet.size();
        double threshHoldToAns4Line = cRatios.getIdGradePerpThreshholdToAnsLine();
        Logger.logOCV("createGradeDS > threshAnsLine = " + threshHoldToAns4Line);
        int gradeIndex = -1;
        Line ansLineGrade = getAnswerLineCorrespondingToIdGrade(4);
        Logger.logOCV("Ans 4 Line = " + ansLineGrade.toString());
        while (size > 0) {
            int key = keySet.pollLast();
            size--;
            Circle c0 = circleData.idGradeCircleMap.get(key).get(0);
            double perp = Utility.circleToLineDistance(ansLineGrade, c0);
            Logger.logOCV("circle = " + c0.toString() + ", perp = " + perp);
            if (perp < threshHoldToAns4Line) {
                gradeIndex = key;
                break;
            }
        }
        if (gradeIndex != -1) {
            ArrayList<Circle> list = circleData.idGradeCircleMap.get(gradeIndex);
            circleData.gradeCircleMap.put(0, list);
            Logger.logOCV("Grade Circles Dectected > \n" + circleData.getGradeCircleString());
            // remove all indices >= gradeIndex
            ArrayList<Integer> tempList = new ArrayList<>();
            tempList.addAll(tempSet);
            for (Integer key : tempList) {
                if (key >= gradeIndex)
                    circleData.idGradeCircleMap.remove(key);
            }
        } else {
            circleData.gradeCircleMap.put(0, new ArrayList<Circle>());
            Logger.logOCV("Grade Circles Not Detected ");
        }
    }

    protected Line getAnswerLineCorrespondingToIdGrade(int i) {
        Circle c0 = circleData.answerCircleMap.get(0).get(i);
        Circle cn = circleData.answerCircleMap.get(SheetConstants.NUM_ROWS_ANSWERS - 1).get(i);
        Line line = new Line(c0.center.x, c0.center.y, cn.center.x, cn.center.y);
        return line;
    }

    private void createIdCircleDS() {
        /* last element of every column has its distance measured from the answer column row 0-2
        *  - > this way extra circles are filtered and columns are not extrapolated from them
        *  - > also id columns are segregated correctly*/
        double threshHoldToAns4Line = cRatios.getIdGradePerpThreshholdToAnsLine();
        Logger.logOCV("createIdDS > threshAnsLine = " + threshHoldToAns4Line);
        TreeSet<Integer> keySet = new TreeSet<>();
        Set<Integer> tempSet = circleData.idGradeCircleMap.keySet();
        keySet.addAll(tempSet);
        Line ansLine0 = getAnswerLineCorrespondingToIdGrade(0);
        Line ansLine1 = getAnswerLineCorrespondingToIdGrade(1);
        Line ansLine2 = getAnswerLineCorrespondingToIdGrade(2);
        Logger.logOCV("Ans Line > 0 = " + ansLine0.toString() + ", 1 = " + ansLine1.toString() + ", 2 = " + ansLine2.toString());
        Set<Integer> setIndices = new HashSet<>();
        while (keySet.size() > 0) {
            int key = keySet.pollFirst();
            ArrayList<Circle> list = circleData.idGradeCircleMap.get(key);
            Circle cn = list.get(list.size() - 1);
            String tempStr = "";
            tempStr += "Circle N = " + cn.toString();
            double perp;
            if (!setIndices.contains(0)) {
                perp = Utility.circleToLineDistance(ansLine0, cn);
                tempStr += " > perp0 = " + perp;
                if (perp < threshHoldToAns4Line) {
                    circleData.idCircleMap.put(0, list);
                    setIndices.add(0);
                    tempStr += "; set 0";
                }
            } else if (!setIndices.contains(1)) {
                perp = Utility.circleToLineDistance(ansLine1, cn);
                tempStr += " > perp1 = " + perp;
                if (perp < threshHoldToAns4Line) {
                    circleData.idCircleMap.put(1, list);
                    setIndices.add(1);
                    tempStr += "; set 1";
                }
            } else if (!setIndices.contains(2)) {
                perp = Utility.circleToLineDistance(ansLine2, cn);
                tempStr += " > perp1 = " + perp;
                if (perp < threshHoldToAns4Line) {
                    circleData.idCircleMap.put(2, list);
                    setIndices.add(2);
                    tempStr += "; set 2";
                }
            }
            Logger.logOCV(tempStr);
        }
        String tempStr = "";
        if (!setIndices.contains(0)) {
            circleData.idCircleMap.put(0, new ArrayList<Circle>());
            tempStr += " 0 ";
        } else if (!setIndices.contains(1)) {
            circleData.idCircleMap.put(1, new ArrayList<Circle>());
            tempStr += "  1 ";
        } else if (!setIndices.contains(2)) {
            circleData.idCircleMap.put(2, new ArrayList<Circle>());
            tempStr += "  2 ";
        }
        if (!TextUtils.isEmpty(tempStr))
            Logger.logOCV("Empty Ids > " + tempStr);
        Logger.logOCV("IdCircles > \n" + circleData.getIdCircleString());
    }

    private void filterDuplicateCircles(TreeMap<Integer, ArrayList<Circle>> map) {
        // filter circles detected due to opencv detecting inner and outer rings
        // in circles
        for (ArrayList<Circle> list : map.values()) {
            filterDuplicateCirclesFromList(list);
        }
    }

    private void filterDuplicateCirclesFromList(ArrayList<Circle> list) {
        int count = 0;
        int len = list.size();
        for (int i = 0; i < len - 1; ++i) {
            Circle c1 = list.get(i);
            Circle c2 = list.get(i + 1);
            double dist = Utility.getDistanceBetweenPoints(c1.center, c2.center);
            if (dist < avgAnswerRadius) {
                list.remove(c2);
                len -= 1;
                count++;
            }
        }
        Logger.logOCV("Filtered duplicates = " + count);
    }

    private boolean filterOutExtraAnswerCircles() {
        /* max distance between consecutive circles ina a row*/
        double ccd = cRatios.getAnswerCirclesCenterDistanceThreshhold();
        LinkedList<Integer> errorIndices = new LinkedList<>(); // contains row indices with wrong no of answer circles
        LinkedList<Integer> safeIndices = new LinkedList<>(); // containes row indices with right no of answer circles
        int k = 0;
        for (ArrayList<Circle> list : circleData.answerCircleMap.values()) {
            int indexCounter = 0;
            int len = list.size();
            if (len == SheetConstants.NUM_TOTAL_DETECTED_CIRCLES_IN_ROW) {
                /* if row has correct no. of extrapolated and detected circles
                 - > remove every 5th consecutive circle, if at a;;
                 - >  mark indice as safe
                 - > else mark as error */
                safeIndices.add(k);
                for (int i = 0; i < len - 1; ++i) {
                    Circle ci = list.get(i);
                    Circle ci1 = list.get(i + 1);
                    double dist = Utility.getDistanceBetweenPoints(ci, ci1);
                    if (dist < ccd) {
                        ++indexCounter;
                        if (indexCounter == 4) {
                            list.remove(ci1);
                            len--;
                            indexCounter = 0;
                        }
                    }
                }
            } else /*if (len > SheetConstants.NUM_TOTAL_DETECTED_CIRCLES_IN_ROW)*/ {
                errorIndices.add(k);
            }
            ++k;
        }

        /* if only one/zero error indice has correct no. of answer circles
        * - >  throw error as filtering and extrapolation not possible with the sample set*/
        if (safeIndices.size() < 2) {
            errorType = ErrorType.TYPE7;
            return false;
        }
        Collections.sort(safeIndices);
        Collections.sort(errorIndices);
        Logger.logOCV("Answer Final Filter >  error in indices > " + errorIndices.toString());

        /* extrapolate whole rows with error indices
        * - > by finding intersection of answer row with corresponding column of safe indices */
        int ind0 = safeIndices.get(0);
        int indN = safeIndices.get(safeIndices.size() - 1);
        while (!errorIndices.isEmpty()) {
            int ind = errorIndices.poll();
            ArrayList<Circle> list = circleData.answerCircleMap.get(ind);
            Circle cAns1 = list.get(0);
            Circle cAns2 = list.get(list.size() - 1);
            Line ansLine = new Line(cAns1.center.x, cAns1.center.y, cAns2.center.x, cAns2.center.y);
            ArrayList<Circle> tempList = new ArrayList<>();
            ArrayList<Circle> list0 = circleData.answerCircleMap.get(ind0);
            ArrayList<Circle> listN = circleData.answerCircleMap.get(indN);
            for (int i = 0; i < SheetConstants.NUM_ANSWERS_IN_ROW; ++i) {
                Circle cVert1 = list0.get(i);
                Circle cVert2 = listN.get(i);
                Line vertLine = new Line(cVert1.center.x, cVert1.center.y, cVert2.center.x, cVert2.center.y);
                Circle newCircle = Utility.getCircleAtIntersection(ansLine, vertLine, avgAnswerRadius);
                tempList.add(0, newCircle);
            }
            Collections.sort(tempList, new AnswerCircleRowComparator());
            circleData.answerCircleMap.put(ind, tempList);
        }

        // in 16th row , remove last circles
        ArrayList<Circle> list0 = circleData.answerCircleMap.get(0);
        int len = list0.size();
        List<Circle> tempList = list0.subList(0, len < SheetConstants.NUM_ANSWERS_IN_ROW0 ? len : SheetConstants.NUM_ANSWERS_IN_ROW0);
        ArrayList<Circle> tempList1 = new ArrayList<>();
        tempList1.addAll(tempList);
        circleData.answerCircleMap.put(0, tempList1);
        Logger.logOCV("Filtered final answers = \n" + circleData.toString());
        return true;
    }

    public void transformAnswerCircleMap() {
        /* transform answer map by question no.*/
        int size = circleData.answerCircleMap.size();
        Set<Integer> set = circleData.answerCircleMap.keySet();
        //   Logger.logOCV("Answer map : \n" + circleData.toString());
        for (Integer i : set) {
            int superInd = size - i;
            ArrayList<Circle> list = circleData.answerCircleMap.get(i);
            int len = list.size();
            //   Logger.logOCV("PROCESS ROW > " + i + ", len = " + len);
            int s = 0;
            int t = 4;
            int indCount = 0;
            while (s < len) {
                ArrayList<Circle> subList = new ArrayList<>();
                subList.addAll(list.subList(s, t));
                int subInd = superInd + indCount * size;
                //   Logger.logOCV("superInd, subInd = " + superInd + "," + subInd + " > s, t  = " + s + "," + t + " > " + subList.toString());
                circleData.transwerCircleMap.put(subInd, subList);
                s += 4;
                t += 4;
                if (t > len)
                    t = len;
                indCount++;
            }
        }
        Logger.logOCV("Transformed Answer map : \n" + circleData.getTranswerCircleString());
    }

    public void drawCirclesOnMat(Mat mat) {
        circleData.printAnswerCirclesOnMat(mat);
        circleData.printIdCirclesOnMat(mat);
        circleData.printGradeCirclesOnMat(mat);
    }

    protected double getAverageRadius(ArrayList<Circle> circleList) {
        int numCircles = circleList.size();
        double sum = 0;
        for (Circle circle : circleList) {
            sum += circle.radius;
        }
        double avgRad = sum / (double) numCircles;
        Logger.logOCV("avgRad = " + avgRad + ", sum =  " + sum);
        return avgRad;
    }

    protected double getAverageCcdOfTopAnswerRow(ArrayList<Circle> circleList) {
        double sum = 0;
        for (int i = 0; i < SheetConstants.NUM_ANSWERS_IN_COLUMN_IN_ROW - 1; ++i) {
            Circle c1 = circleList.get(i);
            Circle c2 = circleList.get(i + 1);
            double dist = Utility.getDistanceBetweenPoints(c1, c2);
            sum += dist;
        }
        return sum / (SheetConstants.NUM_ANSWERS_IN_COLUMN_IN_ROW - 1);
    }

    public void drawCirclesOnMat(ArrayList<Circle> circles, Mat circleMat) {
        for (Circle circle : circles) {
            Imgproc.circle(circleMat, circle.center, (int) Math.round(circle.radius),
                    new Scalar(0, 40, 200), 2);
        }
    }

    public boolean isError() {
        return errorType > ErrorType.TYPE0;
    }

    public int getError() {
        return errorType;
    }

    protected OCVCircleConfig getOCVConfig(int cols, int rows) {
        OCVCircleConfig config = ConfigDS.getInstance().getConfig();
        config.rectWidth = cols;
        config.rectHeight = rows;
        return config;
    }
}