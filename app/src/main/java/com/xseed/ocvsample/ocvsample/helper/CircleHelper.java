package com.xseed.ocvsample.ocvsample.helper;

import android.text.TextUtils;

import com.xseed.ocvsample.ocvsample.comparator.AnswerCircleColumnComparator;
import com.xseed.ocvsample.ocvsample.comparator.AnswerCircleRowComparator;
import com.xseed.ocvsample.ocvsample.comparator.IdGradeCircleColumnComparator;
import com.xseed.ocvsample.ocvsample.datasource.CircleDS;
import com.xseed.ocvsample.ocvsample.datasource.CircleRatios;
import com.xseed.ocvsample.ocvsample.datasource.DotDS;
import com.xseed.ocvsample.ocvsample.pojo.Circle;
import com.xseed.ocvsample.ocvsample.pojo.Line;
import com.xseed.ocvsample.ocvsample.pojo.OCVCircleConfig;
import com.xseed.ocvsample.ocvsample.utility.ErrorType;
import com.xseed.ocvsample.ocvsample.utility.Logger;
import com.xseed.ocvsample.ocvsample.utility.SheetConstants;
import com.xseed.ocvsample.ocvsample.utility.Utility;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
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
 * Created by Manvendra Sah on 31/08/17.
 */

public class CircleHelper {

    private double avgAnswerRadius = 0;
    private double avgIdGradeRadius = 0;
    private ArrayList<Circle> circles;
    private CircleDS circleData;
    private DotDS dotData;
    private int rows, cols;
    private CircleRatios cRatios;
    private int errorType = ErrorType.TYPE0;

    public CircleDS getCircleData() {
        return circleData;
    }

    public void createDataSource(ArrayList<Circle> circles, int rows, int cols,
                                 DotDS dotData, CircleRatios cRatios) {
        this.circles = circles;
        this.cRatios = cRatios;
        this.rows = rows;
        this.cols = cols;
        Logger.logOCV("baseMat size > " + cols + "," + rows);
        this.dotData = dotData;
        avgAnswerRadius = getAverageRadius(circles);
        cRatios.setAvgAnswerRadius(avgAnswerRadius);
        getFilteredListByRadius();
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

    public ArrayList<Circle> findCircles(Mat circleMat) {
        OCVCircleConfig config = new OCVCircleConfig();
        config.dp = 1.4d;
        config.minDist = 25;
        config.minRadius = 7;
        config.maxRadius = 20;
        config.param1 = 12;
        config.param2 = 35;//38;
        config.topLeftX = 0;
        config.topLeftY = 0;
        config.rectWidth = circleMat.cols();
        config.rectHeight = circleMat.rows();

        Rect extractedRect = new Rect(config.topLeftX, config.topLeftY, config.rectWidth, config.rectHeight);
        Mat mat = new Mat(circleMat, extractedRect);
       /* create a Mat object to store the circles detected */
        Mat circles = new Mat(config.rectWidth,
                config.rectHeight, CvType.CV_8UC1);
       /* find the circle in the image */
        Imgproc.HoughCircles(mat, circles,
                Imgproc.CV_HOUGH_GRADIENT, config.dp, config.minDist, config.param1,
                config.param2, config.minRadius, config.maxRadius);
       /* get the number of circles detected */
        int numberOfCircles = (circles.rows() == 0) ? 0 : circles.cols();
        Logger.logOCV("numIdCircles = " + numberOfCircles);
        final ArrayList<Circle> circleList = new ArrayList<Circle>();
       /* draw the circles found on the image */
        for (int i = 0; i < numberOfCircles; i++) {
           /* get the circle details, circleCoordinates[0, 1, 2] = (x,y,r)
             * (x,y) are the coordinates of the circle's center */
            double[] circleCoordinates = circles.get(0, i);
            int x = (int) circleCoordinates[0], y = (int) circleCoordinates[1];
            Point center = new Point(x, y);
            double radius = circleCoordinates[2];
            circleList.add(new Circle(x, y, radius));
            // Imgproc.circle(baseMat, center, radius, new Scalar(0, 255, 0), 4);
            //   Imgproc.putText(baseMat, String.valueOf(i), center, Core.FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 255, 45), 4);
            //   Logger.logOCV(" circle " + i + " :  " + ((int) (center.x - radius)) + " , " + ((int) (center.y - radius)));
        }
        return circleList;
    }

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

    private boolean extrapolateOuterAnswerUndetectedCircles() {
        /* Extrapolate outermost left/right circles in each row by measuring distance of left/most detected circles
        * from left/right lines
        *  - distance is normalised by top/bottom or left/right line ratios
        * */
        double ratioVertLine = cRatios.getRightToLeftLineRatio();
        double ratioHorzLine = cRatios.getBottomToTopLineRatio();
        double changeFactorHorz = Math.abs(ratioHorzLine - 1);
        double height = rows * 0.7d;
        double heightDiff = rows * 0.3d;
        double normHorzMultiplier = 0.95d;
        Logger.logOCV("ratio Vert = " + ratioVertLine + ", horz = " + ratioHorzLine);

        Line leftLine = dotData.getLeftLine();
        double threshLeft0 = cRatios.getAnswerCirclesLeftPerpendicularThreshhold(0);
        double threshLeft1 = cRatios.getAnswerCirclesLeftPerpendicularThreshhold(1);
        double threshLeftVertNorm0 = (threshLeft0 * normHorzMultiplier) / ratioVertLine;
        double threshLeftVertNorm1 = (threshLeft1 * normHorzMultiplier) / ratioVertLine;
        Logger.logOCV("threshL Vert > " + threshLeft0 + ", " + threshLeft1);
        /* contains indices of rows with leftmost circles detected*/
        ArrayList<Integer> leftMost = new ArrayList<>();

        for (int k = 0; k < SheetConstants.NUM_ROWS_ANSWERS; ++k) {
            ArrayList<Circle> row = circleData.answerCircleMap.get(k);
            if (row.size() < 2) {
                errorType = ErrorType.TYPE4;
                return false;
            }
            Circle circle1 = row.get(0);
            double perpL = Utility.circleToLineDistance(leftLine, circle1);
            double centerY = circle1.center.y;
            double changeFactorY = (centerY - heightDiff) * changeFactorHorz / height;
            double ratioHorzNorm = (ratioHorzLine > 1 ? (1 + changeFactorY) : (1 - changeFactorY));
            double threshLeftHorzNorm0 = threshLeft0 * ratioHorzNorm * normHorzMultiplier;
            double threshLeftHorzNorm1 = threshLeft1 * ratioHorzNorm * normHorzMultiplier;
            Logger.logOCV("perpL > " + k + " > " + perpL + ", threshNorm0 > vert = " + threshLeftVertNorm0 + ", horz = " + threshLeftHorzNorm0);
            if (perpL < Math.min(threshLeftVertNorm0, threshLeftHorzNorm0))
                leftMost.add(k);
            /*else if (perpL > threshLeft0 && perpL < threshLeft1 && row.size() > 1) {
                Circle circle2 = row.get(1);
                Circle newCircle = Utility.getCircleToLeft(circle1, circle2, avgRadius);
                circleData.answerCircleMap.get(k).add(0, newCircle);
                Logger.logOCV("extrapolateOuter Left > " + k + " > added at pos 0");
                leftMost.add(k);
            }*/
        }
        Collections.sort(leftMost);
        Logger.logOCV("leftMost > " + leftMost.toString());

        Line rightLine = dotData.getRightLine();
        double threshRight0 = cRatios.getAnswerCirclesRightPerpendicularThreshhold(0);
        double threshRight1 = cRatios.getAnswerCirclesRightPerpendicularThreshhold(1);
        double threshRightVertNorm0 = threshRight0 * ratioVertLine * normHorzMultiplier;
        double threshRightVertNorm1 = threshRight1 * ratioVertLine * normHorzMultiplier;
        Logger.logOCV("threshR Vert > " + threshRight0 + ", " + threshRight1);
        /* contains indices of rows with rightmost circles detected*/
        ArrayList<Integer> rightMost = new ArrayList<>();

        for (int k = 0; k < SheetConstants.NUM_ROWS_ANSWERS; ++k) {
            ArrayList<Circle> row = circleData.answerCircleMap.get(k);
            int len = row.size();
            Circle circle1 = row.get(len - 1);
            double perpR = Utility.circleToLineDistance(rightLine, circle1);
            double centerY = circle1.center.y;
            double changeFactorY = (centerY - heightDiff) * changeFactorHorz / height;
            double ratioHorzNorm = (ratioHorzLine > 1 ? (1 + changeFactorY) : (1 - changeFactorY));
            double threshRightHorzNorm0 = threshRight0 * ratioHorzNorm * normHorzMultiplier;
            double threshRightHorzNorm1 = threshRight1 * ratioHorzNorm * normHorzMultiplier;
            Logger.logOCV("perpL > " + k + " > " + perpR + ", threshNorm0 > vert = " + threshRightVertNorm0 + ", horz = " + threshRightHorzNorm0);
            if (perpR < Math.min(threshRightVertNorm0, threshRightHorzNorm0))
                rightMost.add(k);
           /* else if (perpR > threshRight0 && perpR < threshRight1 && len > 1) {
                Circle circle2 = row.get(len - 2);
                Circle newCircle = Utility.getCircleToRight(circle1, circle2, avgRadius);
                circleData.answerCircleMap.get(k).add(len, newCircle);
                Logger.logOCV("extrapolateOuter Right> " + k + " > added at pos " + len);
                rightMost.add(k);
            }*/
        }
        Collections.sort(rightMost);
        Logger.logOCV("rightMost > " + rightMost.toString());

        /*if no two rows have left/rightmost detected circles, extrapolation cant be done and error is thrown*/
        int sizeLeft = leftMost.size();
        int sizeRight = rightMost.size();
        if (sizeLeft < 2 || sizeRight < 2) {
            errorType = ErrorType.TYPE5;
            return false;
        }

        // extrapolate leftmost & rightmost line
        boolean canExtraPolateLeft = leftMost.size() > 1;
        Line exLeftLine = null;
        if (canExtraPolateLeft) {
            Circle leftTop = circleData.answerCircleMap.get(leftMost.get(0)).get(0);
            Circle leftBottom = circleData.answerCircleMap.get(leftMost.get(leftMost.size() - 1)).get(0);
            exLeftLine = new Line(leftTop.center.x, leftTop.center.y, leftBottom.center.x, leftBottom.center.y);
            Logger.logOCV("exLeftLine > " + exLeftLine.toString());
        }

        boolean canExtraPolateRight = rightMost.size() > 1;
        Line exRightLine = null;
        if (canExtraPolateRight) {
            ArrayList<Circle> temp = circleData.answerCircleMap.get(rightMost.get(0));
            Circle rightTop = temp.get(temp.size() - 1);
            temp = circleData.answerCircleMap.get(rightMost.get(rightMost.size() - 1));
            Circle rightBottom = temp.get(temp.size() - 1);
            exRightLine = new Line(rightTop.center.x, rightTop.center.y, rightBottom.center.x, rightBottom.center.y);
            Logger.logOCV("exRightLine > " + exRightLine.toString());
        }

        /* Extrapolate left/right-most circles in other rows (where opencv couldnt detect outermost circles)
        * - done by finding intersection of left/rightmost answer column lines with individual
        *  answer rows
        * */
        for (int k = 0; k < SheetConstants.NUM_ROWS_ANSWERS; ++k) {
            ArrayList<Circle> row = circleData.answerCircleMap.get(k);
            int size = row.size();
            Circle leftMostCircle = row.get(0);
            Circle rightMostCircle = row.get(size - 1);
            Line rowLine = Utility.getRowLine(leftMostCircle, rightMostCircle, avgAnswerRadius);
            // Logger.logOCV("rowLine > " + rowLine.toString());
            if (canExtraPolateLeft && !leftMost.contains(k)) {
                Point point = Utility.findIntersection(exLeftLine, rowLine);
                // Logger.logOCV("intersection point left > " + point.toString());
                Circle newCircle = new Circle(point.x, point.y, avgAnswerRadius);
                newCircle.isExtrapolated = true;
                row.add(0, newCircle);//add leftmost circle at index 0
                Logger.logOCV("extrapolateOuter Left > " + k + " > added by intersection at pos 0 at center = " + point.toString());
            }
            if (canExtraPolateRight && !rightMost.contains(k)) {
                Point point = Utility.findIntersection(exRightLine, rowLine);
                // Logger.logOCV("intersection point right > " + point.toString());
                Circle newCircle = new Circle(point.x, point.y, avgAnswerRadius);
                newCircle.isExtrapolated = true;
                row.add(row.size(), newCircle);// add rightmost circle at last index
                Logger.logOCV("extrapolateOuter Right > " + k + " > added by intersection at pos " + size + " at center = " + point.toString());
            }
        }
        return true;
    }

    private boolean extrapolateMiddleAnswerUndetectedCircles() {

        LinkedList<Integer> linkList = new LinkedList<>();
        for (int k = 0; k < SheetConstants.NUM_ROWS_ANSWERS; ++k)
            linkList.add(k, k);

        /* contains indices of rows where minCcd couldnt be found*/
        Set<Integer> noAvgSet = new TreeSet<>();
        /* max circle center distance between two consecutive circles*/
        double ccd = cRatios.getAnswerCirclesCenterDistanceThreshhold();
        double minCcd = 0;
        double ratioLineLen = cRatios.getRightToLeftLineRatio();
        double width = 0.8 * cols;
        double changeFactor = Math.abs(ratioLineLen - 1);
        Logger.logOCV("extrapolateMiddle > ccd = " + ccd + ", ratioLineLen = " + ratioLineLen);

        /* -  get minccd of every row by normalising avgCcd obtained
        *  - if row has no consecutive circles, use minCcd of previous row, if available, else add back to list */
        while (!linkList.isEmpty()) {
            int k = linkList.poll();
            Logger.logOCV("extrapolateMiddle > ------------ PROCESS ROW > " + k + " --------------");

            ArrayList<Circle> circles = circleData.answerCircleMap.get(k);
            double len = circles.size();
            String tempDist = "";
            double avgCount = 0;
            // double tempAvgCcd = 0;
            double tempMinCcd = 0;

            for (int i = 0; i < len - 1; ++i) {
                Circle c1 = circles.get(i);
                Circle c2 = circles.get(i + 1);
                double centerDist = Utility.getDistanceBetweenPoints(c1, c2);
                double centerX = (c1.center.x + c2.center.x) / 2;
                if (centerDist < ccd) {
                    double dRatio = centerX * changeFactor / width;
                    double ratioNorm = ratioLineLen > 1 ? (1 + dRatio) : (1 - dRatio);
                    tempMinCcd += centerDist / ratioNorm;
                    tempDist += i + " : " + centerDist + ", ";
                    // tempAvgCcd += centerDist;
                    avgCount++;
                }
            }
            if (avgCount > 0) {
                //  avgCcd = tempAvgCcd / avgCount;
                minCcd = tempMinCcd / avgCount;
                minCcd = minCcd * 0.95d;
                Logger.logOCV("extrapolateMiddle > distString = " + tempDist + ", minCcd = " + minCcd);
            }

            // if (avgCcd > 0) {
            if (minCcd > 0) {
                for (int i = 0; i < len - 1; ++i) {
                    Circle c1 = circles.get(i);
                    Circle c2 = circles.get(i + 1);
                    double centerDist = Utility.getDistanceBetweenPoints(c1, c2);
                    double centerX = (c1.center.x + c2.center.x) / 2;
                    double dRatio = centerX * changeFactor / width;
                    double ratioNorm = ratioLineLen > 1 ? (1 + dRatio) : (1 - dRatio);
                    double ccdNorm = minCcd * ratioNorm;
                    // double avgCcdNorm = getNormalisedCenterDistance(c1, c2, avgCcd);
                    // double numPaths = centerDist / avgCcdNorm;
                    // double numPathsNorm = Math.floor(numPaths + 0.3d);
                    double numPaths = centerDist / ccdNorm;
                    double numPathsNorm = Math.floor(numPaths + 0.4d);
                    Logger.logOCV("extrapolateMiddle > row = " + k + " > i = " + i + ", dist = " + (int) centerDist + ", ccdNorm = " + ccdNorm +
                            ", numPaths=" + numPaths + ", numPathsNorm = " + numPathsNorm /*+", dRatio = " + dRatio*/);
                    for (int j = 1; j < numPathsNorm; ++j) {
                        Circle newCircle = Utility.getCircleBetweenCircles(c1, c2, avgAnswerRadius, j, (int) numPathsNorm);
                        circles.add(i + 1, newCircle);
                        len += 1;
                        ++i;
                    }
                }
                if (noAvgSet.contains(k))
                    noAvgSet.remove(k);
            } else {
                noAvgSet.add(k);
                int size = noAvgSet.size();
                /* if none of the rows had consecutive circles
                - > minCcd couldnt be calculated - > throw error*/
                if (size >= SheetConstants.NUM_ROWS_ANSWERS) {
                    errorType = ErrorType.TYPE6;
                    return false;
                }
                Logger.logOCV("extrapolateMiddle > addToSet > " + k + ", setSize = " + noAvgSet.size());
                // if first index in linklist is in noAvgSet - > remove
                for (Integer i : noAvgSet) {
                    if (linkList.peek() == i)
                        linkList.poll();
                }
                int firstInd = 1;
                /* add all indices in noAvgSet to linklist from 1st index, so that if 0th index has minCcd calculated
                * - >  these rows can use the same minCcd, as they themselves don't have consecutive circles*/
                for (Integer i : noAvgSet) {
                    Logger.logOCV("extrapolateMiddle > addToLinkList > " + i + " at index " + firstInd);
                    linkList.add(firstInd, i);
                    firstInd++;
                }
            }
        }
        return true;
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

    private boolean extrapolateIdGradeUndetectedCircles() {
        /*get avgCcD between two circles*/
        Logger.logOCV("extrapolateIdGradeUndetectedCircles >  avgCcd calculation -------------");
        int len = circleData.idCircleMap.size();

        double height = 0.8 * rows;
        double ratioLineLen = cRatios.getBottomToTopLineRatio();

        double avgCcd = 0;
        double avgCount = 0;
        double threshC0 = cRatios.getVerticalThreshholdBetweenIdCircles(0);
        double threshC1 = cRatios.getVerticalThreshholdBetweenIdCircles(1);
        double threshC2 = cRatios.getVerticalThreshholdBetweenIdCircles(2);
        Logger.logOCV("extrapolateIdGradeMid > thresh 0,1,2 = " + threshC0 + ", " + threshC1 + ", " + threshC2 + ", ratioLineLen = " + ratioLineLen);

        for (int i = 0; i < len; ++i) {
            ArrayList<Circle> list = circleData.idCircleMap.get(i);
            int size = list.size();
            for (int j = 0; j < size - 1; ++j) {
                Circle circle = list.get(j);
                Circle circle1 = list.get(j + 1);
                double centerY = (circle.center.y + circle1.center.y) / 2;
                double changeFactor = Math.abs(1 - ratioLineLen) * centerY / height;
                double ratioY = (ratioLineLen > 1 ? (1 + changeFactor) : (1 - changeFactor));
                double dist = Utility.getDistanceBetweenPoints(circle, circle1);
                double threshC0Norm = threshC0 / ratioY;
                double threshC1Norm = threshC1 / ratioY;
                double threshC2Norm = threshC2 / ratioY;
                int inc = 0;
                if (dist < threshC0Norm) {
                    avgCcd += dist;
                    inc = 1;
                } else if (dist > threshC0Norm && dist < threshC1Norm) {
                    avgCcd += dist;
                    inc = 2;
                } /*else if (dist > threshC1Norm && dist < threshC2Norm) {
                    avgCcd += dist;
                    inc = 3;
                }*/
                avgCount += inc;
                Logger.logOCV("col : " + i + ", row : " + j + ", avgCCd  : +" + inc +
                        "  : " + dist + ", threshNorm 0, 1,2 = " + threshC0Norm + ", " + threshC1Norm + ", " + threshC2Norm + ", ratioY = " + ratioY);
            }
        }

        ArrayList<Circle> list2 = circleData.gradeCircleMap.get(0);
       /* if (list2 == null) {
            mSheetListener.onOMRSheetGradingFailed("No grade circles");
            return;
        }*/
        int size = list2 == null ? 0 : list2.size();
        for (int j = 0; j < size - 1; ++j) {
            Circle circle = list2.get(j);
            Circle circle1 = list2.get(j + 1);
            double centerY = (circle.center.y + circle1.center.y) / 2;
            double changeFactor = Math.abs(1 - ratioLineLen) * centerY / height;
            double ratioY = (ratioLineLen > 1 ? (1 + changeFactor) : (1 - changeFactor));
            double dist = Utility.getDistanceBetweenPoints(circle, circle1);
            double threshC0Norm = threshC0 / ratioY;
            double threshC1Norm = threshC1 / ratioY;
            double threshC2Norm = threshC2 / ratioY;
            int inc = 0;
            if (dist < threshC0Norm) {
                avgCcd += dist;
                inc = 1;
            } else if (dist > threshC0Norm && dist < threshC1Norm) {
                avgCcd += dist;
                inc = 2;
            } /*else if (dist > threshC1Norm && dist < threshC2Norm) {
                avgCcd += dist;
                inc = 3;
            }*/
            avgCount += inc;
            Logger.logOCV("col : " + 3 + ", row : " + j + ", avgCCd  : +" + inc + "  : " + dist +
                    ", threshNorm 0, 1,2 = " + threshC0Norm + ", " + threshC1Norm + ", " + threshC2Norm + ", ratioY = " + ratioY);
        }

        if (avgCount == 0) {
           /* mSheetListener.onOMRSheetGradingFailed("No consecutive id circles");
            return;*/
           /*if id/grade columns dont have consecutive circles, avgCCd from top answer row is taken and normalised*/
            avgCcd = getAverageCcdOfTopAnswerRow(circleData.answerCircleMap.get(SheetConstants.NUM_ROWS_ANSWERS - 1));
            avgCcd = avgCcd * avgIdGradeRadius / avgAnswerRadius;
            Logger.logOCV("No Consecutive id/grade circle , avgCcd calculated from Ans row Top 1st Column ");
        } else {
            avgCcd = avgCcd / avgCount;
        }
        Logger.logOCV("extrapolateIdGradeMid > avgCCd > " + avgCcd + "------------------");

        extrapolateInnerIdGradeCircles(avgCcd, circleData.idCircleMap);
        extrapolateOuterIdGradeCircles(avgCcd, circleData.idCircleMap);
        extrapolateInnerIdGradeCircles(avgCcd, circleData.gradeCircleMap);
        extrapolateOuterIdGradeCircles(avgCcd, circleData.gradeCircleMap);
        return true;
    }

    private void extrapolateInnerIdGradeCircles(double avgCcd, TreeMap<Integer, ArrayList<Circle>> circleMap) {
        int len = circleMap.size();
        for (int i = 0; i < len; ++i) {
            ArrayList<Circle> list = circleMap.get(i);
            int size = list.size();
            for (int j = 0; j < size - 1; ++j) {
                Circle circle = list.get(j);
                Circle circle1 = list.get(j + 1);
                double dist = Utility.getDistanceBetweenPoints(circle, circle1);
                double numPaths = dist / avgCcd;
                double numPaths2 = Math.floor(numPaths + 0.4d);
                Logger.logOCV("extrapolateInnerIdGrade > dist > col > " + i + " > " + j + " = " + dist + ", numPaths = " + numPaths + " -> " + numPaths2);
                for (int k = 1; k < numPaths2; ++k) {
                    Circle newCircle = Utility.getCircleBetweenCircles(circle, circle1, avgIdGradeRadius, k, (int) numPaths2);
                    Logger.logOCV("extrapolateInnerIdGrade >  added in col " + i + " at pos " + (j + 1));
                    list.add(j + 1, newCircle);
                    size += 1;
                    ++j;
                }
            }
        }
    }

    private void extrapolateOuterIdGradeCircles(double avgCcd, TreeMap<Integer, ArrayList<Circle>> circleMap) {
        /* if column has only 1 circle
        * - > extrapolate bottom circle parallel to answer column 2 line
        * - > if not - > extrapolate top circles by thresholding using avgCCd
        *  - > extrapolate bottom circles*/
        Line topLine = dotData.getTopLine();
        int len = circleMap.size();
      /*  double height = 0.9 * rows;
        double ratioLineLen = cRatios.getBottomToTopLineRatio();
        double ratioVertLineLen = cRatios.getRightToLeftLineRatio();*/

       /* double threshTop = cRatios.getTopPerpThreshholdForIdCircles();
        Logger.logOCV("extrapolateOuterIdGradeCircles > threshTop =  " + threshTop);*/
        Logger.logOCV(cRatios.toString());

        for (int i = 0; i < len; ++i) {
            ArrayList<Circle> list = circleMap.get(i);
            int size = list.size();
            // extrapolate top circles
            while (size < SheetConstants.NUM_IDS_IN_COLUMN && size > 0) {
                Circle circle0 = list.get(0);
                Circle circle1 = null;
                if (size < 2) {
                    Logger.logOCV("perp > col " + i + " > added bottom circle by parallel line extrapolation");
                    circle1 = Utility.getBottomCircleParallelToLine(circle0, avgIdGradeRadius, getAnswerLineCorrespondingToIdGrade(2), avgCcd);
                    list.add(1, circle1);
                } else
                    circle1 = list.get(1);

                Circle newCircle = Utility.getCircleToTop(circle0, circle1, avgIdGradeRadius);
                double perp = Utility.circleToLineDistance(topLine, newCircle);
                double ratioCcdPerp = perp / avgCcd;
                Logger.logOCV("perp > col " + i + " > " + perp + ", ratioCcdPerp = " + ratioCcdPerp);
              /*  Logger.logOCV("perp > col " + i + " > " + perp + ", ratioCcdPerp = " + ratioCcdPerp + ", threshNorm = " + threshTopNorm);
                      double centerY = (circle0.center.y + newCircle.center.y) / 2;
                double changeFactor = Math.abs(1 - ratioLineLen) * (ratioLineLen > 1 ? (height - centerY) : centerY) / height;
                double ratioY = (ratioLineLen > 1 ? (1 + changeFactor) : (1 - changeFactor));
                double threshTopNorm = threshTop / ratioY;
                if (ratioVertLineLen > 1)
                    threshTopNorm /= ratioVertLineLen;
                if ((perp > threshTopNorm && (perp - threshTopNorm) > 0.8d * avgIdGradeRadius) ||
                        (perp < threshTopNorm && (threshTopNorm - perp) < 0.8d * avgIdGradeRadius))
                if (perp < (threshTopNorm - 3d)) {*/
                if (ratioCcdPerp < SheetConstants.MULTIPLIER_CCD_THRESH_TOP0) {
                    Logger.logOCV("DISC > col " + i + " > " + newCircle.center.x + "," + newCircle.center.y);
                    break;
                } else {
                    list.add(0, newCircle);
                    Logger.logOCV("ADDD > col " + i + " > " + newCircle.center.x + "," + newCircle.center.y + " > from >"
                            + circle0.center.x + "," + circle0.center.y + "  and  " + circle1.center.x + "," + circle1.center.y);
                }
                size = list.size();
            }
            // extrapolate bottom circles
            size = list.size();
            int numCirclesLeft = SheetConstants.NUM_IDS_IN_COLUMN - size;
            if (size > 0) {
                while (numCirclesLeft > 0) {
                    Circle circle0 = list.get(size - 1);
                    Circle circle1 = list.get(size - 2);
                    Circle newCircle = Utility.getCircleToBottom(circle0, circle1, avgIdGradeRadius);
                    list.add(size, newCircle);
                    size++;
                    numCirclesLeft = SheetConstants.NUM_IDS_IN_COLUMN - size;
                }
            }
        }
    }

    public void drawCirclesOnMat(Mat mat) {
        circleData.printAnswerCirclesOnMat(mat);
        circleData.printIdCirclesOnMat(mat);
        circleData.printGradeCirclesOnMat(mat);
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

    private Line getAnswerLineCorrespondingToIdGrade(int i) {
        Circle c0 = circleData.answerCircleMap.get(0).get(i);
        Circle cn = circleData.answerCircleMap.get(SheetConstants.NUM_ROWS_ANSWERS - 1).get(i);
        Line line = new Line(c0.center.x, c0.center.y, cn.center.x, cn.center.y);
        return line;
    }

    private double getAverageRadius(ArrayList<Circle> circleList) {
        int numCircles = circleList.size();
        double sum = 0;
        for (Circle circle : circleList) {
            sum += circle.radius;
        }
        double avgRad = sum / (double) numCircles;
        Logger.logOCV("avgRad = " + avgRad + ", sum =  " + sum);
        return avgRad;
    }

    private double getAverageCcdOfTopAnswerRow(ArrayList<Circle> circleList) {
        double sum = 0;
        for (int i = 0; i < SheetConstants.NUM_ANSWERS_IN_COLUMN_IN_ROW - 1; ++i) {
            Circle c1 = circleList.get(i);
            Circle c2 = circleList.get(i + 1);
            double dist = Utility.getDistanceBetweenPoints(c1, c2);
            sum += dist;
        }
        return sum / (SheetConstants.NUM_ANSWERS_IN_COLUMN_IN_ROW - 1);
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

    public boolean isError() {
        return errorType > ErrorType.TYPE0;
    }

    public int getError() {
        return errorType;
    }

    public void drawCirclesOnMat(ArrayList<Circle> circles, Mat circleMat) {
        for (Circle circle : circles) {
            Imgproc.circle(circleMat, circle.center, (int) Math.round(circle.radius),
                    new Scalar(0, 40, 200), 2);
        }
    }
}