package com.xseed.ocvsample.ocvsample.helper.circle;

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
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by Manvendra Sah on 31/08/17.
 */

public class CircleHelper extends AbstractCircleHelper {

    public ArrayList<Circle> findCircles(Mat circleMat) {
        OCVCircleConfig config = getOCVConfig(circleMat.cols(), circleMat.rows());
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

    @Override
    protected boolean extrapolateOuterAnswerUndetectedCircles() {
        Line leftLine = secondaryDotDS.getLeftLine();
        Line rightLine = secondaryDotDS.getRightLine();
        for (int k = 0; k < SheetConstants.NUM_ROWS_ANSWERS; ++k) {
            ArrayList<Circle> row = circleData.answerCircleMap.get(k);
            int rowSize = row.size();
            if (rowSize < 2) {
                errorType = ErrorType.TYPE4;
                return false;
            }
            Circle circle0 = row.get(0);
            Circle circleN = row.get(rowSize - 1);
            Line rowLine = new Line(circle0.center.x, circle0.center.y, circleN.center.x, circleN.center.y);
            double dist0 = Utility.circleToLineDistance(leftLine, circle0);
            if (dist0 > 2 * cRatios.avgAnswerRadius) {
                // if distance of leftmost circle is greater than atleast twice of circle radius
                // then its not the actual leftmost circles
                // get leftmost circle by intersection of rowLine and identity left line
                Circle circle = Utility.getCircleAtIntersection(leftLine, rowLine, avgAnswerRadius);
                row.add(0, circle);
                Logger.logOCV("extrapolateOuterAnswerUndetectedCircles > Row = " + k + " added LEFTMOST");
                rowSize++;
            }

            double distN = Utility.circleToLineDistance(rightLine, circleN);
            if (distN > 2 * cRatios.avgAnswerRadius) {
                // if distance of rightmost circle is greater than atleast twice of circle radius
                // then its not the actual rightmost circles
                // get rightmost circle by intersection of rowLine and identity right line
                Circle circle = Utility.getCircleAtIntersection(rightLine, rowLine, avgAnswerRadius);
                row.add(rowSize, circle);
                Logger.logOCV("extrapolateOuterAnswerUndetectedCircles > Row = " + k + " added RIGHTMOST");
                rowSize++;
            }
        }
        return true;
    }

    @Override
    protected boolean extrapolateMiddleAnswerUndetectedCircles() {

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

    @Override
    protected boolean extrapolateIdGradeUndetectedCircles() {
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

        extrapolateTopIdGradeCircle(avgCcd, circleData.idCircleMap, false);
        extrapolateTopIdGradeCircle(avgCcd, circleData.gradeCircleMap, true);
        extrapolateInnerIdGradeCircles(avgCcd, circleData.idCircleMap);
        extrapolateOuterIdGradeCircles(avgCcd, circleData.idCircleMap);
        extrapolateInnerIdGradeCircles(avgCcd, circleData.gradeCircleMap);
        extrapolateOuterIdGradeCircles(avgCcd, circleData.gradeCircleMap);
        return true;
    }

    private void extrapolateTopIdGradeCircle(double avgCcd, TreeMap<Integer, ArrayList<Circle>> circleMap, boolean isGradeMap) {
        int len = circleMap.size();
        Line topLine = secondaryDotDS.getTopLine();
        for (int i = 0; i < len; ++i) {
            ArrayList<Circle> list = circleMap.get(i);
            Circle c0 = list.get(0);
            double perp = Utility.circleToLineDistance(topLine, c0);
            if (perp > 1.5 * avgIdGradeRadius) {
                // no top circle is there
                Line ansLine = isGradeMap ? getAnswerLineCorrespondingToIdGrade(4) : getAnswerLineCorrespondingToIdGrade(i);
                Circle circle = Utility.getCircleAtIntersection(topLine, ansLine, avgIdGradeRadius);
                list.add(0, circle);
            }
        }
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
//        Line topLine = secondaryDotDS.getTopLine();
        int len = circleMap.size();
        Logger.logOCV(cRatios.toString());

        for (int i = 0; i < len; ++i) {
            ArrayList<Circle> list = circleMap.get(i);
            /*  int size = list.size();
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
                if (perp < avgIdGradeRadius) {
                    Logger.logOCV("DISC > col " + i + " > " + newCircle.center.x + "," + newCircle.center.y);
                    break;
                } else {
                    list.add(0, newCircle);
                    Logger.logOCV("ADDD > col " + i + " > " + newCircle.center.x + "," + newCircle.center.y + " > from >"
                            + circle0.center.x + "," + circle0.center.y + "  and  " + circle1.center.x + "," + circle1.center.y);
                }
                size = list.size();
            }*/
            // extrapolate bottom circles
            int size = list.size();
            int numCirclesLeft = SheetConstants.NUM_IDS_IN_COLUMN - size;
            if (size > 1) {
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
}



 /*   @Override
    protected boolean extrapolateOuterAnswerUndetectedCircles() {
        *//* Extrapolate outermost left/right circles in each row by measuring distance of left/most detected circles
        * from left/right lines
        *  - distance is normalised by top/bottom or left/right line ratios
        * *//*
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
        *//* contains indices of rows with leftmost circles detected*//*
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
            *//*else if (perpL > threshLeft0 && perpL < threshLeft1 && row.size() > 1) {
                Circle circle2 = row.get(1);
                Circle newCircle = Utility.getCircleToLeft(circle1, circle2, avgRadius);
                circleData.answerCircleMap.get(k).add(0, newCircle);
                Logger.logOCV("extrapolateOuter Left > " + k + " > added at pos 0");
                leftMost.add(k);
            }*//*
        }
        Collections.sort(leftMost);
        Logger.logOCV("leftMost > " + leftMost.toString());

        Line rightLine = dotData.getRightLine();
        double threshRight0 = cRatios.getAnswerCirclesRightPerpendicularThreshhold(0);
        double threshRight1 = cRatios.getAnswerCirclesRightPerpendicularThreshhold(1);
        double threshRightVertNorm0 = threshRight0 * ratioVertLine * normHorzMultiplier;
        double threshRightVertNorm1 = threshRight1 * ratioVertLine * normHorzMultiplier;
        Logger.logOCV("threshR Vert > " + threshRight0 + ", " + threshRight1);
        *//* contains indices of rows with rightmost circles detected*//*
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
           *//* else if (perpR > threshRight0 && perpR < threshRight1 && len > 1) {
                Circle circle2 = row.get(len - 2);
                Circle newCircle = Utility.getCircleToRight(circle1, circle2, avgRadius);
                circleData.answerCircleMap.get(k).add(len, newCircle);
                Logger.logOCV("extrapolateOuter Right> " + k + " > added at pos " + len);
                rightMost.add(k);
            }*//*
        }
        Collections.sort(rightMost);
        Logger.logOCV("rightMost > " + rightMost.toString());

        *//*if no two rows have left/rightmost detected circles, extrapolation cant be done and error is thrown*//*
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

        *//* Extrapolate left/right-most circles in other rows (where opencv couldnt detect outermost circles)
        * - done by finding intersection of left/rightmost answer column lines with individual
        *  answer rows
        * *//*
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
*/