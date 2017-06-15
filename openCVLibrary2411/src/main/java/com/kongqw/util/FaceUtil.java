package com.kongqw.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.Log;

import org.bytedeco.javacpp.opencv_core.CvHistogram;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.bytedeco.javacpp.helper.opencv_imgproc.cvCalcHist;
import static org.bytedeco.javacpp.opencv_core.CV_HIST_ARRAY;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgproc.CV_COMP_CORREL;
import static org.bytedeco.javacpp.opencv_imgproc.CV_COMP_INTERSECT;
import static org.bytedeco.javacpp.opencv_imgproc.cvCompareHist;
import static org.bytedeco.javacpp.opencv_imgproc.cvNormalizeHist;
import static org.opencv.highgui.Highgui.CV_LOAD_IMAGE_GRAYSCALE;

/**
 * Created by kqw on 2016/9/9.
 * FaceUtil
 */
public final class FaceUtil {

    private static final String TAG = "FaceUtil";

    private FaceUtil() {
    }

    /**
     * 特征保存
     *
     * @param context  Context
     * @param image    Mat
     * @param rect     人脸信息
     * @param fileName 文件名字
     * @return 保存是否成功
     */
    public static boolean saveImage(Context context, Mat image, Rect rect, String fileName) {
        Mat mat = grayChange(image, rect);
        return Highgui.imwrite(getFilePath(context, fileName), mat);
    }

    /**
     * 将检测的人脸置灰且变成固定大小
     *
     * @param image
     * @param rect
     */
    public static Mat grayChange(Mat image, Rect rect) {
        // 原图置灰
        Mat grayMat = new Mat();
        Imgproc.cvtColor(image, grayMat, Imgproc.COLOR_BGR2GRAY);
        // 把检测到的人脸重新定义大小后保存成文件
        Mat sub = grayMat.submat(rect);
        Mat mat = new Mat();
        Size size = new Size(100, 100);
        Imgproc.resize(sub, mat, size);
        return mat;
    }

    /**
     * 将图片置灰
     *
     * @param bitmap
     * @return
     */
    public static Bitmap grey(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Bitmap faceIconGreyBitmap = Bitmap
                .createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(faceIconGreyBitmap);
        Paint paint = new Paint();
        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0);
        ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(
                colorMatrix);
        paint.setColorFilter(colorMatrixFilter);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return faceIconGreyBitmap;
    }

    /**
     * 得到固定大小的bitmap
     *
     * @param bitmap
     * @return
     */
    public static Bitmap getSizeBmp(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 设置想要的大小
        int newWidth = 100;
        int newHeight = 100;
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap mbitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return mbitmap;
    }

    /*************** 第一种算法***************************/
   /* *
     *  提取图片特征
     * @return
     */
    public static Mat extractORB(Mat test_mat) {
        Mat desc = new Mat();
        FeatureDetector fd = FeatureDetector.create(FeatureDetector.ORB);
        MatOfKeyPoint mkp = new MatOfKeyPoint();
        //Log.d(TAG, "extractSIFT: +++++++++----------");
        fd.detect(test_mat, mkp);//报错
        //Log.d(TAG, "extractSIFT: +++++++++");
        //Log.d(TAG, "extractORB: 图像特征点个数"+mkp.size());
        DescriptorExtractor de = DescriptorExtractor.create(DescriptorExtractor.ORB);
        de.compute(test_mat, mkp, desc);//提取特征
        //Log.d(TAG, "extractORB: 特征描述矩阵大小"+desc.size());
        //Log.d(TAG, "extractSIFT: "+desc.cols());
        //Log.d(TAG, "extractSIFT: "+desc.rows());
        return desc;
    }

    /**
     * 匹配特征
     */
    public static double match(Mat face1, Mat face2) {
        double max = 0;
        DescriptorMatcher descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        MatOfDMatch matches = new MatOfDMatch();
        descriptorMatcher.match(face1, face2, matches);
      /*  double max_dist = 0;
        double min_dist = 100;
        DMatch [] dma = matches.toArray();
        for (int i = 0;i<dma.length;i++){
            double dist = dma[i].distance;
            if( dist < min_dist ) min_dist = dist;
            if( dist > max_dist ) max_dist = dist;
        }
        Log.d(TAG, "match: max="+max_dist);
        Log.d(TAG, "match: min="+min_dist);
        for(int i = 0;i<dma.length;i++){
            if(dma[i].distance<0.9*max_dist){
                max++;
            }
        }*/
        //Log.d(TAG, "match: 个数"+matches.size());
        DMatch[] dma = matches.toArray();
        for (int i = 0; i < dma.length; i++) {
            double list = dma[i].distance;
            if (list < 100) {
                max++;
            }
            //Log.d(TAG, "match: 距离="+list);
        }
        //Log.d(TAG, "match: max="+max);
        //Log.d(TAG, "match: 相似度"+max/dma.length*100);
        return max / dma.length * 100;
    }

    /**
     * 将bitmap保存至固定路径下
     *
     * @param bitmap
     */
    public static void saveImage(Context context, Bitmap bitmap, String fileName) {
        String path = getFilePath(context, fileName);
        File file = new File(path);
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
        try {
            fOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除特征
     *
     * @param context  Context
     * @param fileName 特征文件
     * @return 是否删除成功
     */
    public static boolean deleteImage(Context context, String fileName) {
        // 文件名不能为空
        if (TextUtils.isEmpty(fileName)) {
            return false;
        }
        // 文件路径不能为空
        String path = getFilePath(context, fileName);
        if (path != null) {
            File file = new File(path);
            return file.exists() && file.delete();
        } else {
            return false;
        }
    }

    /**
     * 提取特征
     *
     * @param context  Context
     * @param fileName 文件名
     * @return 特征图片
     */
    public static Bitmap getImage(Context context, String fileName) {
        String filePath = getFilePath(context, fileName);
        if (TextUtils.isEmpty(filePath)) {
            return null;
        } else {
            return BitmapFactory.decodeFile(filePath);
        }
    }

    /**
     * 特征对比，直方图的比较
     *
     * @param context   Context
     * @param fileName1 人脸特征
     * @param fileName2 人脸特征
     * @return 相似度
     */
    public static double compare(Context context, String fileName1, String fileName2) {
        try {
            String pathFile1 = getFilePath(context, fileName1);
            String pathFile2 = getFilePath(context, fileName2);
            IplImage image1 = cvLoadImage(pathFile1, CV_LOAD_IMAGE_GRAYSCALE);
            IplImage image2 = cvLoadImage(pathFile2, CV_LOAD_IMAGE_GRAYSCALE);
            if (null == image1 || null == image2) {
                return -1;
            }
            int l_bins = 256;
            int hist_size[] = {l_bins};
            float v_ranges[] = {0, 255};
            float ranges[][] = {v_ranges};

            IplImage imageArr1[] = {image1};
            IplImage imageArr2[] = {image2};
            CvHistogram Histogram1 = CvHistogram.create(1, hist_size, CV_HIST_ARRAY, ranges, 1);
            CvHistogram Histogram2 = CvHistogram.create(1, hist_size, CV_HIST_ARRAY, ranges, 1);
            cvCalcHist(imageArr1, Histogram1, 0, null);
            cvCalcHist(imageArr2, Histogram2, 0, null);
            cvNormalizeHist(Histogram1, 100.0);
            cvNormalizeHist(Histogram2, 100.0);
            // 参考：http://blog.csdn.net/nicebooks/article/details/8175002
            double c1 = cvCompareHist(Histogram1, Histogram2, CV_COMP_CORREL) * 100;
            double c2 = cvCompareHist(Histogram1, Histogram2, CV_COMP_INTERSECT);
//            Log.i(TAG, "compare: ----------------------------");
//            Log.i(TAG, "compare: c1 = " + c1);
//            Log.i(TAG, "compare: c2 = " + c2);
//            Log.i(TAG, "compare: 平均值 = " + ((c1 + c2) / 2));
//            Log.i(TAG, "compare: ----------------------------");
            return (c1 + c2) / 2;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }


    /**
     * 比较两个矩阵的相似度
     *
     * @param srcMat
     * @param desMat
     */
    public static double comPareHist(Mat srcMat, Mat desMat) {

        srcMat.convertTo(srcMat, CvType.CV_32F);
        desMat.convertTo(desMat, CvType.CV_32F);
        double target = Imgproc.compareHist(srcMat, desMat, Imgproc.CV_COMP_CORREL);
        return target * 100;

    }

    /**
     * 获取人脸特征路径
     *
     * @param fileName 人脸特征的图片的名字
     * @return 路径
     */
    private static String getFilePath(Context context, String fileName) {
        if (TextUtils.isEmpty(fileName)) {
        }
        Log.d(TAG, "getFilePath: " + context.getApplicationContext().getFilesDir().getPath() + fileName + ".jpg");
        // 内存路径
        return context.getApplicationContext().getFilesDir().getPath() + fileName + ".jpg";
        // 内存卡路径 需要SD卡读取权限
        // return Environment.getExternalStorageDirectory() + "/FaceDetect/" + fileName + ".jpg";
    }
}
