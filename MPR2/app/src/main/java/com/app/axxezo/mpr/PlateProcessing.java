package com.app.axxezo.mpr;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.Math.min;

/**
 * Created by tstaig on 9/9/17.
 */

public class PlateProcessing {
    public double getDist(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x-p2.x, 2)+Math.pow(p1.y-p2.y, 2));
    }

    public int getPlateTopLeftIndex(Point[] box) {
        int top1 = 0;
        int top2 = 0;
        int i1 = 0;
        int i2 = 0;
        for (int i = 0; i < box.length; i++){
            Point p = box[i];
            if (p.y >= top1) {
                top1 = (int)p.y;
                i1 = i;
            }
        }
        for (int i = 0; i < box.length; i++) {
            Point p = box[i];
            if (i == i1)
                continue;
            if (p.y >= top2) {
                top2 = (int)p.y;
                i2 = i;
            }
        }
        if (box[i1].x <= box[i2].x)
            return i1;
        else
            return i2;
    }

    public MatOfPoint2f getBox(Rect brect, MatOfPoint2f apprect, int lptype) {
        Point[] apprectarr = apprect.toArray();
        double d1 = getDist(apprectarr[0], apprectarr[1]);
        double d2 = getDist(apprectarr[1], apprectarr[2]);
        double d3 = getDist(apprectarr[2], apprectarr[3]);
        double d4 = getDist(apprectarr[3], apprectarr[0]);
        List<Double> dists = new ArrayList<Double>();
        dists.add(d1);
        dists.add(d2);
        dists.add(d3);
        dists.add(d4);
        Collections.sort(dists);
        double dx;
        double dy = dists.get(1);
        if (lptype == 0)
            dx = dy/13.0*36.0;
        else
            dx = dy/12.0*14.5;
        //0: Right top, 1: Left top, 2: Left bottom, 3: Right bottom
        int pos;
        //p = [0, 0, 0, 0]
        Point[] p = new Point[4];
        pos = 0;
        int s = 0;
        if (dy == d1)
            pos = 0;
        else if (dy == d2)
            pos = 1;
        else if (dy == d3)
            pos = 2;
        else if (dy == d4)
            pos = 3;
        if (apprectarr[(pos+1)%4].y > apprectarr[pos].y)
            s = 1;
        else
            s = -1;
        p[pos] = apprectarr[pos];
        p[(pos+1)%4] = new Point(p[pos].x, p[pos].y + s*dy);
        p[(pos+2)%4] = new Point(p[(pos+1)%4].x - s*dx, p[(pos+1)%4].y);
        p[(pos+3)%4] = new Point(p[(pos+2)%4].x, p[(pos+2)%4].y - s*dy);
        double mx = Math.min(Math.min(p[pos].x, p[(pos+1)%4].x), Math.min(p[(pos+2)%4].x, p[(pos+3)%4].x));
        double my = Math.min(Math.min(p[pos].y, p[(pos+1)%4].y), Math.min(p[(pos+2)%4].y, p[(pos+3)%4].y));
        if (mx < 0)
            for(int pi = 0; pi < 4; pi++)
                p[pi].x += -mx + 20;
        if (my < 0)
            for(int pi = 0; pi < 4; pi++)
                p[pi].y += -my + 20;
        MatOfPoint2f wbox = new MatOfPoint2f(p);
        return wbox;
    }

    public List<RotatedRect> findRectangle(Mat img, Mat orig, double img_i, int lptype) {
        double vmin, vmax;
        Core.MinMaxLocResult res = Core.minMaxLoc(img);
        vmin = res.minVal;
        vmax = res.maxVal;
        MatOfDouble hvmean = new MatOfDouble();
        MatOfDouble hvstd = new MatOfDouble();
        double vmean, vstd;
        Core.meanStdDev(img, hvmean, hvstd);
        vmean = hvmean.get(0, 0)[0];
        vstd = hvstd.get(0, 0)[0];
        //Mat thresh_img;
        //Imgproc.threshold(img, thresh_img, img_i, 255, Imgproc.THRESH_BINARY)
        Mat thresh_img = img;
        Mat npaHierarchy = new Mat();
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat threshcpy_img = new Mat();
        thresh_img.copyTo(threshcpy_img);
        Imgproc.findContours(thresh_img, contours, npaHierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        List<RotatedRect> rects = new ArrayList<RotatedRect>();
        for (int i = 0; i < contours.size(); i++) {
            MatOfPoint c = contours.get(i);
            MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());
            double epsilon = 0.01*Imgproc.arcLength(c2f, true);
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(c2f, approx, epsilon, true);
            RotatedRect rect = Imgproc.minAreaRect(c2f);
            //Mat box;
            //Imgproc.boxPoints(rect, box);
            Point[] box = new Point[4];
            rect.points(box);
            //box = np.int0(box)
            int j1 = getPlateTopLeftIndex(box);
            int j2 = (j1 + 1)%4;
            int j3 = (j2 + 1)%4;
            double d1 = getDist(box[j1], box[j2]);
            double d2 = getDist(box[j2], box[j3]);
            if (d1 == 0 || d2 == 0)
                continue;
            double carea = Imgproc.contourArea(c);
            double area = Imgproc.contourArea(new MatOfPoint(box));
            if (lptype == 0 && (d1/d2 > 0.5 || d1/d2 < 0.25 || area < 300 || carea/area < 0.5))
                continue;
            if (lptype == 1 && (d1/d2 > 1.0 || d1/d2 < 0.40 || area < 300 || carea/area < 0.5))
                continue;
            rects.add(rect);
            break;
        }
        return rects;
    }

    public int[] getThresh(Mat img, double width, double height, int cxl, int cxr, int cyb, int cyt) {
        int tmin = 100;
        int tmax = 255;
        int ptmin = 0;
        Mat thresh_img = new Mat();
        Imgproc.threshold(img, thresh_img, tmin, tmin, Imgproc.THRESH_BINARY);
        double vmin, vmax;
        Core.MinMaxLocResult mmlres = Core.minMaxLoc(thresh_img.submat(cyb, cyt, cxl, cxr));
        vmin = mmlres.minVal;
        vmax = mmlres.maxVal;
        MatOfDouble hvmean = new MatOfDouble();
        MatOfDouble hvstd = new MatOfDouble();
        double vmean, vstd;
        Core.meanStdDev(thresh_img.submat(cyb, cyt, cxl, cxr), hvmean, hvstd);
        vmean = hvmean.get(0, 0)[0];
        vstd = hvstd.get(0, 0)[0];
        int step = 20;
        double eps = 2;
        int target = 180;
        int direction = 0;
        if (vmean > target + eps)
            direction = 1;
        else if (vmean <target - eps)
            direction = -1;
        while ((vmean < target - eps) || (vmean > target + eps)) {
            tmin += step * direction;
            if (tmin == ptmin)
                break;
            Imgproc.threshold(img, thresh_img, tmin, 255, Imgproc.THRESH_BINARY);
            mmlres = Core.minMaxLoc(thresh_img.submat(cyb, cyt, cxl, cxr));
            vmin = mmlres.minVal;
            vmax = mmlres.maxVal;
            Core.meanStdDev(thresh_img.submat(cyb, cyt, cxl, cxr), hvmean, hvstd);
            vmean = hvmean.get(0, 0)[0];
            vstd = hvstd.get(0, 0)[0];
            if (vmean > target + eps) {
                if (direction == -1)
                    step = (int)(step / 2);
                direction = 1;
            } else if (vmean < target - eps) {
                if (direction == 1)
                    step = (int)(step / 2);
                direction = -1;
            }
            ptmin = tmin;
        }
        tmax = 255;
        int [] thr = new int[2];
        thr[0] = tmin;
        thr[1] = tmax;
        return thr;
    }

    public double getThresh2(Mat img, Mat orig, int lptype) {
        double vmin, vmax;
        Core.MinMaxLocResult mmlres = Core.minMaxLoc(img);
        vmin = mmlres.minVal;
        vmax = mmlres.maxVal;
        MatOfDouble hvmean = new MatOfDouble();
        MatOfDouble hvstd = new MatOfDouble();
        double vmean, vstd;
        Core.meanStdDev(img, hvmean, hvstd);
        vmean = hvmean.get(0, 0)[0];
        vstd = hvstd.get(0, 0)[0];
        double thr = vmean;
        int step = 5;
        double res = -1;
        //Test values!
        thr = 0;
        vmax = 255;
        vstd = 0;
        while (thr < vmax - vstd) {
            Mat thresh_img = new Mat();
            Imgproc.threshold(img, thresh_img, thr, 255, Imgproc.THRESH_BINARY);
            Mat npaHierarchy = new Mat();
            List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
            Mat threshcpy_img = new Mat();
            thresh_img.copyTo(threshcpy_img);
            Imgproc.findContours(threshcpy_img, contours, npaHierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
            List<RotatedRect> rects0 = new ArrayList<RotatedRect>();
            List<MatOfPoint2f> rects1 = new ArrayList<MatOfPoint2f>();
            List<MatOfPoint2f> rects2 = new ArrayList<MatOfPoint2f>();
            for (int i = 0; i < contours.size(); i++) {
                MatOfPoint c = contours.get(i);
                MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());
                MatOfPoint2f approx = new MatOfPoint2f();
                double epsilon = 0.01 * Imgproc.arcLength(c2f, true);
                Imgproc.approxPolyDP(c2f, approx, epsilon, true);
                RotatedRect rect = Imgproc.minAreaRect(c2f);
                //Mat box;
                //Imgproc.boxPoints(rect, box);
                Point[] box = new Point[4];
                rect.points(box);
                int j1 = getPlateTopLeftIndex(box);
                int j2 = (j1 + 1) % 4;
                int j3 = (j2 + 1) % 4;
                double d1 = getDist(box[j1], box[j2]);
                double d2 = getDist(box[j2], box[j3]);
                if (d1 == 0 || d2 == 0)
                    continue;
                double carea = Imgproc.contourArea(c);
                double area = Imgproc.contourArea(new MatOfPoint(box));
                if (d1 / d2 > 0.5 || d1 / d2 < 0.25 || area < 300 || carea / area < 0.5)
                    continue;
                rects0.add(rect);
                rects1.add(approx);
                rects2.add(c2f);
            }
            if (rects0.size() == 1) {
                MatOfInt chull = new MatOfInt();
                Imgproc.convexHull(new MatOfPoint(rects1.get(0).toArray()), chull);
                double epsilon = 0.02 * Imgproc.arcLength(rects2.get(0), true);
                int[] chullindex = chull.toArray();
                Point[] rectarr = rects1.get(0).toArray();
                Point[] pcv = new Point[chullindex.length];
                for (int j = 0; j < chullindex.length; j++) {
                    pcv[j] = rectarr[chullindex[j]];
                }
                MatOfPoint2f apprect = new MatOfPoint2f();
                Imgproc.approxPolyDP(new MatOfPoint2f(pcv), apprect, epsilon, true);
                if (apprect.size().height != 4) {
                    thr += step;
                    continue;
                }
                Mat copy = new Mat();
                orig.copyTo(copy);
                Rect wrect = Imgproc.boundingRect(new MatOfPoint(apprect.toArray()));
                MatOfPoint2f wbox = getBox(wrect, apprect, lptype);
                Mat pm;
                pm = Imgproc.getPerspectiveTransform(apprect, wbox);
                Mat warped = new Mat();
                Imgproc.warpPerspective(copy, warped, pm, new Size((int) (copy.size().height * 1.4), (int) (copy.size().width * 1.4)));
                if (apprect.size().height == 4)
                    res = thr;
            }
            thr += step;
        }
        return res;
    }

    public Mat checkLicensePlate(Mat img, RotatedRect rect, String out, String ocr, int lptype) {
        if (rect.angle < -45) {
            rect.angle = 90 + rect.angle;
            double tmp = rect.size.width;
            rect.size.width = rect.size.height;
            rect.size.height = tmp;
        }
        Mat black = new Mat(img.size(), img.type());
        black.setTo(new Scalar(255, 255, 255));
        Point[] box = new Point[4];
        rect.points(box);
        Mat newm = new Mat(img.size(), img.type());
        newm.setTo(new Scalar(255, 255, 255));
        Mat mask = new Mat(img.size(), CvType.CV_8U);
        mask.setTo(new Scalar(0));
        Imgproc.fillConvexPoly(mask, new MatOfPoint(box), new Scalar(255, 255, 255));
        Core.bitwise_and(img, black, newm, mask);
        Mat gray_img = new Mat();
        Imgproc.cvtColor(newm, gray_img, Imgproc.COLOR_BGRA2GRAY);
        Mat rm = Imgproc.getRotationMatrix2D(rect.center, rect.angle, 1.0);
        Imgproc.warpAffine(gray_img, gray_img, rm, gray_img.size(), Imgproc.INTER_LINEAR, Core.BORDER_CONSTANT, new Scalar(255));
        Mat el = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Mat top = new Mat();
        Imgproc.morphologyEx(gray_img, top, Imgproc.MORPH_TOPHAT, el);
        black = new Mat();
        Imgproc.morphologyEx(gray_img, black, Imgproc.MORPH_BLACKHAT, el);
        Mat thresh_img = new Mat();
        Core.add(gray_img, top, thresh_img);
        Core.subtract(thresh_img, black, thresh_img);
        Mat blur_img = new Mat();
        Imgproc.GaussianBlur(thresh_img, blur_img, new Size(5, 5), 0);
        int cxl, cxr, cyb, cyt;
        cxl = cxr = cyb = cyt = 0;
        if (lptype == 0) {
            cxl = Math.min(Math.max(0, (int)(Math.round(rect.center.x - rect.size.width / 2 + rect.size.width * 0.01))), thresh_img.width());
            cxr = Math.min(Math.max(0, (int)(Math.round(rect.center.x + rect.size.width / 2 - rect.size.width * 0.01))), thresh_img.width());
            cyb = Math.min(Math.max(0, (int)(Math.round(rect.center.y - rect.size.height / 2 + rect.size.height * 0.05))), thresh_img.height());
            cyt = Math.min(Math.max(0, (int)(Math.round(rect.center.y + rect.size.height / 2 - rect.size.height * 0.20))), thresh_img.height());
        } else if (lptype == 1) {
            cxl = Math.min(Math.max(0, (int)(Math.round(rect.center.x - rect.size.width / 2 + rect.size.width * 0.01))), thresh_img.width());
            cxr = Math.min(Math.max(0, (int)(Math.round(rect.center.x + rect.size.width / 2 - rect.size.width * 0.01))), thresh_img.width());
            cyb = Math.min(Math.max(0, (int)(Math.round(rect.center.y - rect.size.height / 2 + rect.size.height * 0.30))), thresh_img.height());
            cyt = Math.min(Math.max(0, (int)(Math.round(rect.center.y + rect.size.height / 2 - rect.size.height * 0.10))), thresh_img.height());
        }
        int[] thr = getThresh(blur_img, rect.size.width, rect.size.height, cxl, cxr, cyb, cyt);

        Imgproc.threshold(blur_img, thresh_img, thr[0], thr[1], Imgproc.THRESH_BINARY);
        //thresh_img.adjustROI(cyt, cyb, cxl, cxr);
        Mat pre_img = thresh_img.submat(cyb, cyt, cxl, cxr);

        return pre_img;
    }

    public Mat getLicensePlate(Mat img, String out, String ocr, int lptype) {
        Mat gray_img = new Mat();
        Imgproc.cvtColor(img, gray_img, Imgproc.COLOR_BGRA2GRAY);
        double vmin, vmax;
        Core.MinMaxLocResult res = Core.minMaxLoc(gray_img);
        vmin = res.minVal;
        vmax = res.maxVal;
        MatOfDouble hvmean = new MatOfDouble();
        MatOfDouble hvstd = new MatOfDouble();
        double vmean, vstd;
        Core.meanStdDev(gray_img, hvmean, hvstd);
        vmean = hvmean.get(0, 0)[0];
        vstd = hvstd.get(0, 0)[0];
        Mat el = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Mat top = new Mat();
        Imgproc.morphologyEx(gray_img, top, Imgproc.MORPH_TOPHAT, el);
        Mat black = new Mat();
        Imgproc.morphologyEx(gray_img, black, Imgproc.MORPH_BLACKHAT, el);
        Mat thresh_img = new Mat();
        Core.add(gray_img, top, thresh_img);
        Core.subtract(thresh_img, black, thresh_img);
        Mat blur_img = new Mat();
        Imgproc.GaussianBlur(thresh_img, blur_img, new Size(5, 5), 0);
        //double thr = getThresh2(blur_img, img, lptype);
        Imgproc.threshold(blur_img, thresh_img, min(vmean + vstd, vmax - vmin - 60), 255, Imgproc.THRESH_BINARY);
        Mat copy = new Mat();
        img.copyTo(copy);
        Mat pre_img = thresh_img;
        List<RotatedRect> rects = findRectangle(pre_img, copy, vmean, lptype);
        Mat bmp = null;
        for (int i = 0; i < rects.size(); i++)
            bmp = checkLicensePlate(img, rects.get(i), out, ocr, lptype);
        return bmp;
    }

    public void processLicensePlate(String img_l, String out, String ocr, int lptype) {
        Mat img = Imgcodecs.imread(img_l);
        getLicensePlate(img, out, ocr, lptype);
    }
}
