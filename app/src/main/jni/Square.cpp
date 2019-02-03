#include <jni.h>
#include <opencv2/core/core.hpp>
#include "opencv2/core/utility.hpp"
#include "opencv2/core/ocl.hpp"
#include "opencv2/imgcodecs.hpp"
#include "opencv2/highgui.hpp"
#include "opencv2/calib3d.hpp"
#include <opencv2/imgproc/imgproc.hpp>
#include <vector>
#include <iostream>
#include "opencv2/objdetect/objdetect.hpp"
#include "opencv2/xfeatures2d/nonfree.hpp"
#include "opencv2/core.hpp"
#include "com_example_user_myapplication_PictureDetector.h"

using namespace cv;
using namespace cv::xfeatures2d;


std::vector<Point2f> approx;


int getHeigt(){
    int max = 0;
    int min = 987654321;
    for(auto i : approx){
        if ( i.y> max){
            max = i.y;
        }
        if (i.y < min){
            min = i.y;
        }
    }
    return max - min;
}
int getWidth(){
    std::vector<Point>::const_iterator pos;
    int max = 0;
    int min = 987654321;
    for(auto i : approx){
        if ( i.x > max){
            max = i.x;
        }
        if (i.x < min){
            min = i.x;
        }
    }
    return max - min;
}

int GetAngleABC(Point a, Point b, Point c)
{

    Point ab;
    Point cb;
    ab.x = b.x - a.x;
    ab.y = b.y - a.y;
    cb.x = b.x - c.x;
    cb.y = b.y - c.y;
    float dot = (ab.x * cb.x + ab.y * cb.y); // dot product
    float cross = (ab.x * cb.y - ab.y * cb.x); // cross product

    float alpha = atan2(cross, dot);
    return (int)floor(alpha * 180.0 / CV_PI + 0.5);
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_user_myapplication_PictureDetector_Square(JNIEnv *env, jobject instance,
                                                        jlong matAddrInput, jlong matAddrResult) {
    approx.clear();

    Point2f ptSource[4], ptPrespective[4];
    std::vector<std::vector<Point> > co_ordinates;

        std::vector<KeyPoint> obj_corners(4);

    Mat &inputMat = *(Mat *)matAddrInput;
    Mat &resultMat = *(Mat *)matAddrResult;
    Mat imageGray;

    int row = inputMat.rows / 2;
    int col = inputMat.cols / 2;
    std::vector<Point2d> center(4);
    center[0].x = col-0.1;
    center[0].y = row+0.1;
    center[1].x = col+0.1;
    center[1].y = row+0.1;
    center[2].x = col+0.1;
    center[2].y = row-0.1;
    center[3].x = col-0.1;
    center[3].y = row-0.1;


    int kernel_size = 3;
    int ratio = 3;
    int nThreshold = 150;

    //medianBlur(inputMat, imageGray, 9);
    cvtColor(inputMat, imageGray, COLOR_RGBA2GRAY);

    Mat mask(imageGray);

    //adaptiveThreshold(imageGray, resultMat, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, 5, 10);
    threshold(imageGray, imageGray, 0, 255, THRESH_BINARY | THRESH_OTSU);

    std::vector<std::vector<Point> > contours;
    findContours(imageGray, contours, RETR_LIST, CHAIN_APPROX_SIMPLE);

    //contour를 근사화한다.


    resultMat = inputMat;
    int min = 987654321;
    std::vector<Point2f> tmpApprox;
    for (int k = 0; k < center.size() - 1; k++)
        line(resultMat, center[k], center[k + 1], Scalar(255, 0, 0), 3);
    for (size_t i = 0; i < contours.size(); i++) {
        approxPolyDP(Mat(contours[i]), tmpApprox, arcLength(Mat(contours[i]), true) * 0.1, true);
        int size = tmpApprox.size();
        int area = fabs(contourArea(Mat(tmpApprox)));
        if (size != 4 )
            continue;
        if ( area < 1000 )
            continue;
        if (!(center[0].x > tmpApprox[1].x) || !(center[2].x < tmpApprox[3].x) ||!(center[0].y < tmpApprox[1].y) || !(center[2].y > tmpApprox[3].y))
            continue;

        if (area < min){
            min = area;
            approx = tmpApprox;
        }
    }


    if(approx.size() != 0) {
        /*
        std::vector<int> angle;
        for (int k = 0; k < 4; k++) {
            int ang = GetAngleABC(approx[k], approx[(k + 1) % 4], approx[(k + 2) % 4]);
            angle.push_back(ang);
        }
        sort(angle.begin(), angle.end());
        int minAngle = angle.front();
        int maxAngle = angle.back();
        int threshold = 8;
*/

        line(resultMat, approx[0], approx[approx.size() - 1], Scalar(0, 255, 0), 3);
        for (int k = 0; k < 4 - 1; k++)
            line(resultMat, approx[k], approx[k + 1], Scalar(0, 255, 0), 3);
/*
        if(videoFunc) {

            Mat warpResult;

            ptSource[0] = cvPoint2D32f(0, 0);
            ptSource[1] = cvPoint2D32f(object.cols, 0);
            ptSource[2] = cvPoint2D32f(object.cols, object.rows);
            ptSource[3] = cvPoint2D32f(0, object.rows);

            ptPrespective[0] = cvPoint2D32f(approx[0].x, approx[0].y);
            ptPrespective[1] = cvPoint2D32f(approx[3].x, approx[3].y);
            ptPrespective[2] = cvPoint2D32f(approx[2].x, approx[2].y);
            ptPrespective[3] = cvPoint2D32f(approx[1].x, approx[1].y);


            Mat mat = getPerspectiveTransform(ptSource, ptPrespective);
            Size size(inputMat.cols, inputMat.rows);
            warpPerspective(object, warpResult, mat, size);

            Mat mask(inputMat.rows, inputMat.cols, CV_8UC1, cv::Scalar(0));


            co_ordinates.push_back(std::vector<Point>());
            co_ordinates[0].push_back(Point(approx[1].x, approx[1].y));
            co_ordinates[0].push_back(Point(approx[0].x, approx[0].y));
            co_ordinates[0].push_back(Point(approx[3].x, approx[3].y));
            co_ordinates[0].push_back(Point(approx[2].x, approx[2].y));
            drawContours(mask, co_ordinates, 0, Scalar(255), CV_FILLED, 8);
            warpResult.copyTo(resultMat, mask);
        }*/

    } else {
        return false;
    }
    return true;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_example_user_myapplication_PictureDetector_drawOnCamera(JNIEnv *env, jobject instance,
                                                                 jlong matAddrInput,
                                                                 jlong matAddrResult,
                                                                 jlong objectAddr) {

    Mat &inputMat = *(Mat *)matAddrInput;
    Mat &resultMat = *(Mat *)matAddrResult;
    Mat &object = *(Mat *)objectAddr;

    line(resultMat, approx[0], approx[approx.size() - 1], Scalar(0, 255, 0), 3);
    for (int k = 0; k < 4 - 1; k++)
        line(resultMat, approx[k], approx[k + 1], Scalar(0, 255, 0), 3);

    Point2f ptSource[4], ptPrespective[4];

    ptSource[0] = cvPoint2D32f(0, 0);
    ptSource[1] = cvPoint2D32f(object.cols, 0);
    ptSource[2] = cvPoint2D32f(object.cols, object.rows);
    ptSource[3] = cvPoint2D32f(0, object.rows);

    ptPrespective[0] = cvPoint2D32f(approx[0].x, approx[0].y);
    ptPrespective[1] = cvPoint2D32f(approx[3].x, approx[3].y);
    ptPrespective[2] = cvPoint2D32f(approx[2].x, approx[2].y);
    ptPrespective[3] = cvPoint2D32f(approx[1].x, approx[1].y);

    Mat warpResult;
    Mat mat = getPerspectiveTransform(ptSource, ptPrespective);
    Size size(inputMat.cols, inputMat.rows);
    warpPerspective(object, warpResult, mat, size);

    Mat mask(inputMat.rows, inputMat.cols, CV_8UC1, cv::Scalar(0));

    std::vector< std::vector<Point> >  co_ordinates;
    co_ordinates.push_back(std::vector<Point>());
    co_ordinates[0].push_back(Point(approx[1].x, approx[1].y));
    co_ordinates[0].push_back(Point(approx[0].x, approx[0].y));
    co_ordinates[0].push_back(Point(approx[3].x, approx[3].y));
    co_ordinates[0].push_back(Point(approx[2].x, approx[2].y));
    drawContours( mask,co_ordinates,0, Scalar(255),CV_FILLED, 8 );
    warpResult.copyTo(resultMat,mask);

}extern "C"
JNIEXPORT void JNICALL
Java_com_example_user_myapplication_PictureDetector_getImageFromCamera(JNIEnv *env,
                                                                       jobject instance,
                                                                       jlong matAddrInput,
                                                                       jlong matResultAddr) {

    Mat &inputMat = *(Mat *)matAddrInput;
    Mat &resultMat = *(Mat *)matResultAddr;

    Mat mask(inputMat.rows, inputMat.cols, CV_8UC1, cv::Scalar(0));

    Point2f dst[4], src[4];

    dst[0] = cvPoint2D32f(0, 0);
    dst[1] = cvPoint2D32f(getWidth(), 0);
    dst[2] = cvPoint2D32f(getWidth(), getHeigt());
    dst[3] = cvPoint2D32f(0, getHeigt());

    src[0] = cvPoint2D32f(approx[0].x, approx[0].y);
    src[1] = cvPoint2D32f(approx[3].x, approx[3].y);
    src[2] = cvPoint2D32f(approx[2].x, approx[2].y);
    src[3] = cvPoint2D32f(approx[1].x, approx[1].y);

    std::vector< std::vector<Point> >  co_ordinates;
    co_ordinates.push_back(std::vector<Point>());
    co_ordinates[0].push_back(Point(approx[1].x, approx[1].y));
    co_ordinates[0].push_back(Point(approx[0].x, approx[0].y));
    co_ordinates[0].push_back(Point(approx[3].x, approx[3].y));
    co_ordinates[0].push_back(Point(approx[2].x, approx[2].y));
    drawContours( mask,co_ordinates,0, Scalar(255),CV_FILLED, 8 );

    Mat mat = getPerspectiveTransform(src, dst);
    Size warpSize(getWidth(), getHeigt());
    warpPerspective(inputMat, resultMat, mat, warpSize);

    //Mat test;
    //inputMat.copyTo(test,mask);
//    resultMat = test;
}

