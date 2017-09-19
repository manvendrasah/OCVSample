package com.xseed.ocvsample.ocvsample.utility;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import com.xseed.ocvsample.ocvsample.android.MyApplication;
import com.xseed.ocvsample.ocvsample.pojo.Circle;
import com.xseed.ocvsample.ocvsample.pojo.Line;

import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Created by Manvendra Sah on 21/08/17.
 */

public class Utility {

    public static int getDeviceWidthInPixel(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int dpToPixel(int dimenId, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        float dimen = context.getResources().getDimension(dimenId);
        return (int) (dimen * density);
    }

    public static void storeImage(Bitmap image, String name) {
        File pictureFile = getOutputMediaFile(name);
        if (pictureFile == null) {
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(String name) {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + MyApplication.getInstance().getPackageName()
                /*+ "/frames"*/);
        if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
            return null;
        }
        File mediaFile;
        String mImageName = name + ".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    public static void deleteImageDirectory() {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + MyApplication.getInstance().getPackageName()
               /* + "/frames"*/);
        deleteImageDirectory(mediaStorageDir);
    }

    private static boolean deleteImageDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteImageDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return path.delete();
    }

    public static void writeToLogFile(String log) {
        File mStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + MyApplication.getInstance().getPackageName()
                /*+ "/log"*/);
        if (!mStorageDir.exists() && !mStorageDir.mkdirs()) {
            return;
        }
        String mFileName = "logFile.txt";
        File txtFile = new File(mStorageDir, mFileName);
        if (!txtFile.exists()) {
            try {
                txtFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        if (txtFile != null) {
            FileOutputStream fOut = null;
            OutputStreamWriter osw = null;
            try {
                fOut = new FileOutputStream(txtFile, true);
                osw = new
                        OutputStreamWriter(fOut);
                osw.write(log + "\n\n");
                osw.flush();
                osw.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void deleteLogFile() {
        File mStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + MyApplication.getInstance().getPackageName()
               /* + "/log"*/);
        if (!mStorageDir.exists() && !mStorageDir.mkdirs()) {
            return;
        }
        String mFileName = "logFile.txt";
        File txtFile = new File(mStorageDir, mFileName);
        if (txtFile.exists()) {
            txtFile.delete();
        }
    }

    public static void sendErrorFiles(Activity context) {
        Intent ei = new Intent(Intent.ACTION_SEND_MULTIPLE);
        ei.setType("plain/text");
        ArrayList<String> emails = new ArrayList<String>();
        emails.add("manvendra.sah@xseededucation.com");
        emails.add("deepak.saxena@xseededucation.com");
        emails.add("sandeep.kamjula@xseededucation.com");
        emails.add("raj.chourasia@xseededucation.com");
        emails.add("renjith.ponnappan@xseededucation.com");
        ei.putExtra(Intent.EXTRA_EMAIL, new String[]{"manvendra.sah@xseededucation.com", "deepak.saxena@xseededucation.com",
                "sandeep.kamjula@xseededucation.com", "raj.chourasia@xseededucation.com", "renjith.ponnappan@xseededucation.com"});
        ei.putExtra(Intent.EXTRA_SUBJECT, "OCV Error : ");
        ei.setType("message/rfc822");
        ArrayList<String> fileList = new ArrayList<String>();
        fileList.add(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + MyApplication.getInstance().getPackageName() + "/DotNCircle.jpg");
        fileList.add(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + MyApplication.getInstance().getPackageName() + "/BlobDetect.jpg");
        fileList.add(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + MyApplication.getInstance().getPackageName() + "/InitialCircles.jpg");
        fileList.add(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + MyApplication.getInstance().getPackageName() + "/AdaptThresh.jpg");
        fileList.add(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + MyApplication.getInstance().getPackageName() + "/logFile.txt");

        ArrayList<Uri> uris = new ArrayList<Uri>();
        //convert from paths to Android friendly Parcelable Uri's

        for (int i = 0; i < fileList.size(); i++) {
            File fileIn = new File(fileList.get(i));
            Uri u = Uri.fromFile(fileIn);
            uris.add(u);
        }

        ei.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        context.startActivityForResult(Intent.createChooser(ei, "Sending OCV files..."), 12345);
    }

    public static Scalar getRGBScalar(int i) {
        if (i % 3 == 0)
            return new Scalar(255, 0, 0);
        else if (i % 3 == 1)
            return new Scalar(0, 255, 0);
        else
            return new Scalar(0, 0, 255);
    }


    public static double getLineLength(Line topLine) {
        double dis = getDistanceBetweenPoints(topLine.p1, topLine.p2);
        return dis;
    }

    public static double getDistanceBetweenPoints(Circle c1, Circle c2) {
        double dis = getDistanceBetweenPoints(c1.center, c2.center);
        return dis;
    }

    public static double getDistanceBetweenPoints(Point p1, Point p2) {
        double x1 = p1.x;
        double y1 = p1.y;
        double x2 = p2.x;
        double y2 = p2.y;
        double dis = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        return dis;
    }

    public static double circleToLineDistance(Line line, Circle circle) {
        return pointToLineDistance(line.p1, line.p2, circle.center);
    }

    public static double pointToLineDistance(Point l1, Point l2, Point c) {
        double x1 = l1.x;
        double y1 = l1.y;
        double x2 = l2.x;
        double y2 = l2.y;
        double x3 = c.x;
        double y3 = c.y;
        double k = ((y2 - y1) * (x3 - x1) - (x2 - x1) * (y3 - y1)) / ((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
        double x4 = x3 - k * (y2 - y1);
        double y4 = y3 + k * (x2 - x1);
        double dis = Math.sqrt((x4 - x3) * (x4 - x3) + (y4 - y3) * (y4 - y3));
        return dis;
    }

    public static Point getPerpendicularPoint(Point l1, Point l2, Point c) {
        double x1 = l1.x;
        double y1 = l1.y;
        double x2 = l2.x;
        double y2 = l2.y;
        double x3 = c.x;
        double y3 = c.y;
        double k = ((y2 - y1) * (x3 - x1) - (x2 - x1) * (y3 - y1)) / ((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
        double x4 = x3 - k * (y2 - y1);
        double y4 = y3 + k * (x2 - x1);
        return new Point(x4, y4);
    }

    public static Circle getCircleAtHalfDistance(Circle c1, Circle c2, double avgRadius) {
        double x = (c1.center.x + c2.center.x) / 2;
        double y = (c1.center.y + c2.center.y) / 2;
        Circle newCircle = new Circle(x, y, avgRadius);
        newCircle.isExtrapolated = true;
        return newCircle;
    }

    public static Circle getCircleAtOneThirdDistance(Circle c1, Circle c2, double avgRadius) {
        double x = c1.center.x * 2 / 3 + c2.center.x / 3;
        double y = c1.center.y * 2 / 3 + c2.center.y / 3;
        Circle newCircle = new Circle(x, y, avgRadius);
        newCircle.isExtrapolated = true;
        return newCircle;
    }

    public static Circle getCircleAtTwoThirdDistance(Circle c1, Circle c2, double avgRadius) {
        double x = c1.center.x / 3 + c2.center.x * 2 / 3;
        double y = c1.center.y / 3 + c2.center.y * 2 / 3;
        Circle newCircle = new Circle(x, y, avgRadius);
        newCircle.isExtrapolated = true;
        return newCircle;
    }

    public static Circle getCircleBetweenCircles(Circle c1, Circle c2, double avgRadius, int pos, int totalParts) {
        double x = c1.center.x * (totalParts - pos) / totalParts + c2.center.x * pos / totalParts;
        double y = c1.center.y * (totalParts - pos) / totalParts + c2.center.y * pos / totalParts;
        Circle newCircle = new Circle(x, y, avgRadius);
        newCircle.isExtrapolated = true;
        return newCircle;
    }

    public static Circle getCircleToTop(Circle circle1, Circle circle2, double avgRadius) {
        double x = 2 * circle1.center.x - circle2.center.x;
        double y = 2 * circle1.center.y - circle2.center.y;
        Circle newCircle = new Circle(x, y, avgRadius);
        newCircle.isExtrapolated = true;
        return newCircle;
    }

    public static Circle getCircleToLeft(Circle circle1, Circle circle2, double avgRadius) {
        double x = 2 * circle1.center.x - circle2.center.x;
        double y = 2 * circle1.center.y - circle2.center.y;
        Circle newCircle = new Circle(x, y, avgRadius);
        newCircle.isExtrapolated = true;
        return newCircle;
    }

    public static Circle getCircleToRight(Circle circle1, Circle circle2, double avgRadius) {
        double x = 2 * circle1.center.x - circle2.center.x;
        double y = 2 * circle1.center.y - circle2.center.y;
        Circle newCircle = new Circle(x, y, avgRadius);
        newCircle.isExtrapolated = true;
        return newCircle;
    }

    public static Circle getCircleToBottom(Circle circle1, Circle circle2, double avgRadius) {
        double x = 2 * circle1.center.x - circle2.center.x;
        double y = 2 * circle1.center.y - circle2.center.y;
        Circle newCircle = new Circle(x, y, avgRadius);
        newCircle.isExtrapolated = true;
        return newCircle;
    }

    public static Line getRowLine(Circle leftMostCircle, Circle rightMostCircle, double avgRadius) {
        Circle newLeftMost = getCircleToLeft(leftMostCircle, rightMostCircle, avgRadius);
        Circle newLeftMost2 = getCircleToLeft(newLeftMost, rightMostCircle, avgRadius);
        Line rowLine = new Line(newLeftMost2.center.x, newLeftMost2.center.y, rightMostCircle.center.x, rightMostCircle.center.y);
        return rowLine;
    }

    public static Point findIntersection(Line exLeftLine, Line rowLine) {
        Point A = exLeftLine.p1;
        Point B = exLeftLine.p2;
        Point C = rowLine.p1;
        Point D = rowLine.p2;

        // Line AB represented as a1x + b1y = c1
        double a1 = B.y - A.y;
        double b1 = A.x - B.x;
        double c1 = a1 * (A.x) + b1 * (A.y);

        // Line CD represented as a2x + b2y = c2
        double a2 = D.y - C.y;
        double b2 = C.x - D.x;
        double c2 = a2 * (C.x) + b2 * (C.y);

        double determinant = a1 * b2 - a2 * b1;
        double x = (b2 * c1 - b1 * c2) / determinant;
        double y = (a1 * c2 - a2 * c1) / determinant;
        return new Point(x, y);
    }

    public static Circle getBottomCircleParallelToLine(Circle circle0, double avgRadius, Line leftLine, double avgCcd) {
        Point p1 = leftLine.p1;
        Point p2 = leftLine.p2;
        double slope = (p2.x - p1.x) / (p2.y - p1.y);
        double radian = Math.atan(slope);
        double sinVal = Math.sin(radian);
        double cosVal = Math.cos(radian);
        double cX = avgCcd * sinVal + circle0.center.x;
        double cY = avgCcd * cosVal + circle0.center.y;
        Circle newCircle = new Circle(cX, cY, avgRadius);
        newCircle.isExtrapolated = true;
        return newCircle;
    }

    public static Circle getCircleAtIntersection(Line l1, Line l2, double avgRadius) {
        Point p = findIntersection(l1, l2);
        Circle newCircle = new Circle(p.x, p.y, avgRadius);
        newCircle.isExtrapolated = true;
        return newCircle;
    }
}
