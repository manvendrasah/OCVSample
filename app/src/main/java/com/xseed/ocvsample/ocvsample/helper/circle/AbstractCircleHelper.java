package com.xseed.ocvsample.ocvsample.helper.circle;

import android.text.TextUtils;

import com.xseed.ocvsample.ocvsample.comparator.AnswerCircleColumnComparator;
import com.xseed.ocvsample.ocvsample.comparator.AnswerCircleRowComparator;
import com.xseed.ocvsample.ocvsample.comparator.IdGradeCircleColumnComparator;
import com.xseed.ocvsample.ocvsample.datasource.CircleDS;
import com.xseed.ocvsample.ocvsample.datasource.CircleRatios;
import com.xseed.ocvsample.ocvsample.datasource.ConfigDS;
import com.xseed.ocvsample.ocvsample.datasource.PrimaryDotDS;
import com.xseed.ocvsample.ocvsample.datasource.SecondaryDotDS;
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
    protected PrimaryDotDS primaryDotDS;
    protected SecondaryDotDS secondaryDotDS;

    protected int rows, cols;
    protected CircleRatios cRatios;
    protected int wrongRowIndex = -1;
    protected int errorType = ErrorType.TYPE0;

    public CircleDS getCircleData() {
        return circleData;
    }

    public void createDataSource(ArrayList<Circle> circles, int rows, int cols,
                                 PrimaryDotDS primaryDotDS, SecondaryDotDS secondaryDotDS, CircleRatios cRatios) {
        Logger.logOCV("baseMat size > " + cols + "," + rows);
        this.circles = circles;
        this.cRatios = cRatios;
        this.rows = rows;
        this.cols = cols;
        this.primaryDotDS = primaryDotDS;
        this.secondaryDotDS = secondaryDotDS;

        avgAnswerRadius = getAverageRadius(circles);
        cRatios.setAvgAnswerRadius(avgAnswerRadius);

        getFilteredListByRadius();
        ConfigDS.getInstance().setCirclesDetected(circles.size());

        boolean canCreateInitialDatasource = createInitialCircleDataSource();
        if (!canCreateInitialDatasource) return;

        filterDuplicateCircles(circleData.answerCircleMap);

        boolean canExtrapolateAnswerUndetectedCircles = extrapolatedAnswerUndetectedCircles();
        if (!canExtrapolateAnswerUndetectedCircles)
            return;
        Logger.logOCV("Extrapolated answers = \n" + circleData.toString());
        resolveErrorCasesInAnswerCircles();
        filterAnswerRowZero();

        boolean canCreateIdGradeMap = createIdGradeCircleMap();
        if (!canCreateIdGradeMap) return;

        filterDuplicateCircles(circleData.idCircleMap);
        filterDuplicateCircles(circleData.gradeCircleMap);

        filterExtraTopCircles(circleData.idCircleMap);
        filterExtraTopCircles(circleData.gradeCircleMap);

        extrapolateIdGradeUndetectedCircles();
    }

    private boolean extrapolatedAnswerUndetectedCircles() {
        if (getActualMapSize(circleData.answerCircleMap) < SheetConstants.NUM_ROWS_ANSWERS - 1) {
//            more than 1 row is missing
            errorType = ErrorType.TYPE11;
            return false;
        }
        boolean canExtrapolateOuterAnswers = extrapolateOuterAnswerUndetectedCircles();
        if (!canExtrapolateOuterAnswers) return false;
        boolean canExtrapolateMiddleAnswers = extrapolateMiddleAnswerUndetectedCircles();
        if (!canExtrapolateMiddleAnswers) return false;
        boolean canFilterExtraAnswers = filterOutExtraAnswerCircles();
        if (!canFilterExtraAnswers) return false;
        return true;
    }

    protected abstract boolean extrapolateOuterAnswerUndetectedCircles();

    protected abstract boolean extrapolateMiddleAnswerUndetectedCircles();

    protected abstract boolean extrapolateIdGradeUndetectedCircles();

    protected abstract void resolveErrorCasesInAnswerCircles();

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

    boolean createInitialCircleDataSource() {
        circleData = new CircleDS();
        int numCircles = circles.size();
        Logger.logOCV("creteInitialDS > noOfCircles : " + numCircles);
        ArrayList<Circle> tempList = new ArrayList<>();
        tempList.addAll(circles);
        /* sort circles by descending values of y*/
        Collections.sort(tempList, new AnswerCircleColumnComparator());
        Line midLine = secondaryDotDS.getMidLine();
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
                // check if this is the topmost line
                Circle c0 = row.get(0);
                double dist = Utility.circleToLineDistance(midLine, c0);
                if (dist < 1.5 * avgAnswerRadius) {
                    // topmost answer line is obtained -> stop loop, if not in last iteration
                    i = SheetConstants.NUM_ROWS_ANSWERS;
                }
            }
        }
        int mapSize = circleData.answerCircleMap.size();
        // clear row where only one circle is detected
        // Will be used in resolveErrorCasesInAnswerCircles()
        for (int i = 0; i < mapSize; ++i) {
            if (circleData.answerCircleMap.get(i) != null && circleData.answerCircleMap.get(i).size() < 2) {
                circleData.answerCircleMap.put(i, new ArrayList<Circle>());
            }
        }
        filterWrongAnswerRows();
        // return with error if more than two rows are absent
        // case where one row is absent will be handled in later process
        int actualMapSize = getActualMapSize(circleData.answerCircleMap);
        Logger.logOCV("actualMapSize = " + actualMapSize);
        if (actualMapSize != SheetConstants.NUM_ROWS_ANSWERS) {
            if (actualMapSize > SheetConstants.NUM_ROWS_ANSWERS) {
                errorType = ErrorType.TYPE12;
                return false;
            } else if (actualMapSize == SheetConstants.NUM_ROWS_ANSWERS - 1) {
                errorType = ErrorType.TYPE10;
            } else if (actualMapSize < SheetConstants.NUM_ROWS_ANSWERS - 1) {
                errorType = ErrorType.TYPE11;
                return false;
            }
        }
        // all circles not in answer rows are presumed as id/grade circles, but need filtering
        circleData.idGradeCircleList = tempList;
        Logger.logOCV("INITIAL >" + circleData.toString());
        return true;
    }

    private void filterWrongAnswerRows() {
        Logger.logOCV("filterWrongAnswerRows");
        int size = circleData.answerCircleMap.size();
        Line midLine = secondaryDotDS.getMidLine();
        // check for topmost answer row
        int index = size - 1;
        ArrayList<Circle> list = circleData.answerCircleMap.get(index);
        if (!list.isEmpty()) {
            Circle c0 = list.get(0);
            double perpDist = Utility.circleToLineDistance(midLine, c0);
            if (perpDist < 1.5d * avgAnswerRadius) {
                // this is the topmost answer line correctly detected
                // simple return
                return;
            } else {
                double topDist = Utility.circleToLineDistance(primaryDotDS.getTopLine(), c0);
                double bottomDist = Utility.circleToLineDistance(primaryDotDS.getBottomLine(), c0);
                if (bottomDist > topDist) {
                    // i.e. circles have been detected in idGrade circle area
                    circleData.idGradeCircleList.addAll(list);
                    circleData.answerCircleMap.put(index, new ArrayList<Circle>());
                    Logger.logOCV("Cleared > row > " + index);
                }
            }
        } else {
            // check for second topmost answer row
            index--;
            list = circleData.answerCircleMap.get(index);
            if (!list.isEmpty()) {
                Circle c0 = list.get(0);
                double perpDist = Utility.circleToLineDistance(midLine, c0);
                if (perpDist < 1.5d * avgAnswerRadius) {
                    // this is the topmost answer line correctly detected
                    // simple return
                    return;
                } else {
                    double topDist = Utility.circleToLineDistance(primaryDotDS.getTopLine(), c0);
                    double bottomDist = Utility.circleToLineDistance(primaryDotDS.getBottomLine(), c0);
                    if (bottomDist > topDist) {
                        // i.e. circles have been detected in idGrade circle area
                        circleData.idGradeCircleList.addAll(list);
                        circleData.answerCircleMap.put(index, new ArrayList<Circle>());
                        Logger.logOCV("Cleared > row > " + index);
                    }
                }
            }
        }
    }

    protected int getActualMapSize(TreeMap<Integer, ArrayList<Circle>> circleMap) {
        int mapSize = 0;
        for (ArrayList<Circle> list : circleMap.values()) {
            if (!list.isEmpty())
                mapSize++;
        }
        return mapSize;
    }

    private Line getBottomLine(TreeMap<Integer, ArrayList<Circle>> map, int i) {
       /* for lowest answer row, nearest bottom line is enclosed by bottom left and right dots*/
        if (i == 0)
            return primaryDotDS.getBottomLine();
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
        Line leftLine = primaryDotDS.getLeftLine();
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
        /*if (realSize < 4) {
            errorType = ErrorType.TYPE8;
            return false;
        }*/
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
        if (i == 0)
            return secondaryDotDS.getLeftLine();
        Circle c0 = circleData.answerCircleMap.get(0).get(i);
        Circle cn = circleData.answerCircleMap.get(circleData.answerCircleMap.size() - 1).get(i);
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
            }
            if (!setIndices.contains(1)) {
                perp = Utility.circleToLineDistance(ansLine1, cn);
                tempStr += " > perp1 = " + perp;
                if (perp < threshHoldToAns4Line) {
                    circleData.idCircleMap.put(1, list);
                    setIndices.add(1);
                    tempStr += "; set 1";
                }
            }
            if (!setIndices.contains(2)) {
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

    private void filterExtraTopCircles(TreeMap<Integer, ArrayList<Circle>> map) {
        Double topDist = Utility.getDistanceBetweenDots(primaryDotDS.topLeft, secondaryDotDS.llTop);
        Logger.logOCV("filterExtraTopCircles > top points > " + primaryDotDS.topLeft.toString() + " > " + secondaryDotDS.llTop.toString() + " > dist = " + topDist);
        for (ArrayList<Circle> list : map.values()) {
            filterExtraTopCirclesFromList(list, topDist);
        }
    }

    private void filterExtraTopCirclesFromList(List<Circle> list, Double topDist) {
        int size = list.size() > 2 ? 2 : list.size();
        for (int i = 0; i < size; ++i) {
            Circle circle = list.get(i);
            double dist = Utility.circleToLineDistance(primaryDotDS.getTopLine(), circle);
            boolean isAboveTop = dist < (topDist - avgIdGradeRadius);
            Logger.logOCV("filterExtraTopCirclesFromList > " + circle.toString() + " > dist = " + dist + ", isAbove = " + isAboveTop);
            if (isAboveTop) {
                list.remove(circle);
                i--;
                size--;
            }
        }
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
            Logger.logOCV("Correcting error index > " + ind);
            ArrayList<Circle> list = circleData.answerCircleMap.get(ind);
            if (!list.isEmpty()) {
                Logger.logOCV("Empty list");
                Circle cAns1 = list.get(0);
                Circle cAns2 = list.get(list.size() - 1);
                Line ansLine = new Line(cAns1.center.x, cAns1.center.y, cAns2.center.x, cAns2.center.y);
                ArrayList<Circle> tempList = new ArrayList<>();
                ArrayList<Circle> list0 = circleData.answerCircleMap.get(ind0);
                ArrayList<Circle> listN = circleData.answerCircleMap.get(indN);
                for (int i = 0; i < SheetConstants.NUM_ANSWERS_IN_ROW; ++i) {
                    Logger.logOCV("Row > " + i);
                    Circle cVert1 = list0.get(i);
                    Circle cVert2 = listN.get(i);
                    Line vertLine = new Line(cVert1.center.x, cVert1.center.y, cVert2.center.x, cVert2.center.y);
                    Circle newCircle = Utility.getCircleAtIntersection(ansLine, vertLine, avgAnswerRadius);
                    Logger.logOCV("Row > " + i + " | lineH = " + ansLine.toString() +
                            " | lineV = " + vertLine.toString() + " | newC = " + newCircle.toString());
                    tempList.add(0, newCircle);
                }
                Collections.sort(tempList, new AnswerCircleRowComparator());
                circleData.answerCircleMap.put(ind, tempList);
            }
        }
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

    protected int findMissingAnswerRowIndex() {
        Logger.logOCV("findMissingAnswerRowIndex > errorType = " + errorType);
        int missingIndex = -1;
        int mapSize = circleData.answerCircleMap.size();

        // first check if top row is missing
        Line topLine = secondaryDotDS.getMidLine();
        double perp = Utility.circleToLineDistance(topLine, circleData.answerCircleMap.get(mapSize - 1).get(0));
        if (perp > avgAnswerRadius) {
            missingIndex = SheetConstants.NUM_ROWS_ANSWERS - 1;
//             recalibrateAnswerMap(missingIndex);
            return missingIndex;
        }
        // second check in bottom row is missing
        Line bottomLine = secondaryDotDS.getBottomLine();
        perp = Utility.circleToLineDistance(bottomLine, circleData.answerCircleMap.get(0).get(0));
        if (perp > avgAnswerRadius) {
            missingIndex = 0;
            recalibrateAnswerMap(missingIndex);
            return missingIndex;
        }
        double maxDist = Double.MIN_VALUE;
        for (int i = 0; i < mapSize - 1; ++i) {
            Circle c1 = circleData.answerCircleMap.get(i).get(0);
            Circle c2 = circleData.answerCircleMap.get(i + 1).get(0);
            double dist = Utility.getDistanceBetweenPoints(c1, c2);
            if (dist > maxDist) {
                missingIndex = i + 1;
                maxDist = dist;
            }
        }
        recalibrateAnswerMap(missingIndex);
        return missingIndex;
    }

    private void recalibrateAnswerMap(int missingIndex) {
        Logger.logOCV("recalibrateAnswerMap > missingIndex = " + missingIndex);
        TreeMap<Integer, ArrayList<Circle>> tempMap = new TreeMap<>();
        TreeMap<Integer, ArrayList<Circle>> oldMap = circleData.answerCircleMap;
        for (int i = 0; i < missingIndex; ++i)
            tempMap.put(i, oldMap.get(i));
        tempMap.put(missingIndex, new ArrayList<Circle>());
        for (int i = missingIndex + 1; i < SheetConstants.NUM_ROWS_ANSWERS; ++i)
            tempMap.put(i, oldMap.get(i - 1));
        circleData.answerCircleMap = tempMap;
    }

    protected ArrayList<Circle> getRowByIntersection(Line rowLine, ArrayList<Circle> list1, ArrayList<Circle> list2) {
        ArrayList<Circle> newRow = new ArrayList<>();
        for (int i = 0; i < SheetConstants.NUM_ANSWERS_IN_ROW; ++i) {
            Point center1 = list1.get(i).center;
            Point center2 = list2.get(i).center;
            Line colLine = new Line(center1.x, center1.y, center2.x, center2.y);
            Circle newCircle = Utility.getCircleAtIntersection(rowLine, colLine, avgAnswerRadius);
            newRow.add(i, newCircle);
        }
        return newRow;
    }

    protected ArrayList<Circle> getRowBetweenRows(int parts, int totalParts, ArrayList<Circle> list1, ArrayList<Circle> list2) {
        ArrayList<Circle> newRow = new ArrayList<>();
        for (int i = 0; i < SheetConstants.NUM_ANSWERS_IN_ROW; ++i) {
            Circle c1 = list1.get(i);
            Circle c2 = list2.get(i);
            Circle newCircle = Utility.getCircleBetweenCircles(c1, c2, avgAnswerRadius, parts, totalParts);
            newRow.add(i, newCircle);
        }
        return newRow;
    }

    private void filterAnswerRowZero() {
        // in 16th row , remove last circles
        ArrayList<Circle> list0 = circleData.answerCircleMap.get(0);
        int len = list0.size();
        List<Circle> tempList = list0.subList(0, len < SheetConstants.NUM_ANSWERS_IN_ROW0 ? len : SheetConstants.NUM_ANSWERS_IN_ROW0);
        ArrayList<Circle> tempList1 = new ArrayList<>();
        tempList1.addAll(tempList);
        circleData.answerCircleMap.put(0, tempList1);
        Logger.logOCV("Filtered final answers = \n" + circleData.toString());
    }

    public void drawCirclesOnMat(ArrayList<Circle> circles, Mat circleMat) {
        for (Circle circle : circles) {
            Imgproc.circle(circleMat, circle.center, (int) Math.round(circle.radius),
                    new Scalar(0, 40, 200), 2);
        }
    }

    public boolean isError() {
        return errorType > ErrorType.TYPE0 && errorType != ErrorType.TYPE4 && errorType != ErrorType.TYPE10;
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
