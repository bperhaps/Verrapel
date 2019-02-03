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
#include "com_example_user_myapplication_MainActivity.h"


using namespace cv;
using namespace cv::xfeatures2d;

const int GOOD_PTS_MAX = 30;
const float GOOD_PORTION = 0.10f;
std::vector<KeyPoint> keypoints3;

static Mat drawGoodMatches(
        const Mat &img1,
        const Mat &img2,
        const std::vector<KeyPoint> &keypoints1,
        const std::vector<KeyPoint> &keypoints2,
        std::vector<DMatch> good_matches
) {


    Mat result;

    std::vector<Point2f> obj;
    std::vector<Point2f> scene;

    for (size_t i = 0; i < good_matches.size(); i++) {
        //-- Get the keypoints from the good matches
        obj.push_back(keypoints1[good_matches[i].queryIdx].pt);
        scene.push_back(keypoints2[good_matches[i].trainIdx].pt);
    }
    //-- Get the corners from the image_1 ( the object to be "detected" )
    std::vector<Point2f> obj_corners(4);
    obj_corners[0] = Point(0, 0);
    obj_corners[1] = Point(img1.cols, 0);
    obj_corners[2] = Point(img1.cols, img1.rows);
    obj_corners[3] = Point(0, img1.rows);
    std::vector<Point2f> scene_corners(4);

    Mat H = findHomography(obj, scene, RANSAC);
    result = img2;

    if (!H.empty())
        perspectiveTransform(obj_corners, scene_corners, H);

    line(result,
         scene_corners[0], scene_corners[1],
         Scalar(255, 0, 0), 2, LINE_AA);
    line(result,
         scene_corners[1], scene_corners[2],
         Scalar(255, 0, 0), 2, LINE_AA);
    line(result,
         scene_corners[2], scene_corners[3],
         Scalar(255, 0, 0), 2, LINE_AA);
    line(result,
         scene_corners[3], scene_corners[0],
         Scalar(255, 0, 0), 2, LINE_AA);

    return result;

}

extern "C" {
JNIEXPORT int JNICALL
Java_com_example_user_myapplication_MainActivity_MatchingViaSurf(
        JNIEnv *env,
        jobject instance,
        jlong matAddrInput,
        jlong matAddrResult,
        jlong objectAddr) {

    Mat object = *(Mat *) objectAddr;
    Mat input = *(Mat *) matAddrInput;
    Mat &output = *(Mat *) matAddrResult;
    Mat bobject;
    Mat binput;

    std::vector<KeyPoint> keypoints1, keypoints2;
    UMat _descriptors1, _descriptors2;
    Mat descriptors1 = _descriptors1.getMat(ACCESS_RW),
            descriptors2 = _descriptors2.getMat(ACCESS_RW);

    Ptr<SURF> detector = SURF::create(300);
    //Ptr<SurfDescriptorExtractor> extractor = SurfDescriptorExtractor::create();
    //SurfFeatureDetector detector;
    Ptr<FastFeatureDetector> extractor = FastFeatureDetector::create();
    //detector->c

    cvtColor(object, bobject, CV_RGBA2GRAY);
    cvtColor(input, binput, CV_RGBA2GRAY);

    if (keypoints3.size() == 0) {

        detector->detect(bobject, keypoints3);
        extractor->detect(bobject, keypoints1);
        extractor->compute(bobject, keypoints3, descriptors1);
        //actor->compute(bobject, keypoints3, descriptors1);
        keypoints1 = keypoints3;

    } else {
        keypoints1 = keypoints3;
    }

    detector->detect(binput, keypoints2);
    extractor->compute(binput, keypoints2, descriptors2);

    std::vector<DMatch> good_matches;
    std::vector<std::vector<DMatch> > matches2;
    FlannBasedMatcher matcher;
    matcher.knnMatch(descriptors1, descriptors2, matches2, 2, noArray(), false);

    std::sort(matches2.begin(), matches2.end());
    int ptsPairs = std::min(GOOD_PTS_MAX, (int) (matches2.size() * GOOD_PORTION));


    for (int i = 0; i < ptsPairs; i++) {
        good_matches.push_back(matches2[i][0]);
    }

    std::vector<Point2f> corner;
    output = drawGoodMatches(object, input, keypoints1, keypoints2, good_matches);

    return 0;
}
}