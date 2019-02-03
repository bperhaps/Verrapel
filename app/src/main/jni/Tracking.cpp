#include <opencv2/opencv.hpp>
#include <opencv2/tracking.hpp>
#include <opencv2/core/ocl.hpp>
#include <jni.h>
#include "com_example_user_myapplication_MainActivity.h"

using namespace cv;
using namespace std;

jfieldID jx;
jfieldID jy;
jfieldID jwidth;
jfieldID jheight;
Rect2d bbox;
Ptr<Tracker> tracker;

// Convert to string
#define SSTR( x ) static_cast< std::ostringstream & >( \
( std::ostringstream() << std::dec << x ) ).str()

JNIEXPORT jboolean JNICALL Java_com_example_user_myapplication_MainActivity_Tracking
        (JNIEnv *env, jobject instance, jlong matAddrInput, jlong matAddrResult, jobject rect, jboolean trackerInit)
{
    Mat &inputMat = *(Mat *)matAddrInput;
    Mat &resultMat = *(Mat *)matAddrResult;
    Mat frame;

    cvtColor(inputMat, resultMat, CV_RGBA2GRAY);
    frame = inputMat;
    if( !trackerInit ) {
        tracker = TrackerKCF::create();

        jclass rectClass = env->GetObjectClass(rect);
        jx = env->GetFieldID(rectClass, "x", "D");
        jy = env->GetFieldID(rectClass, "y", "D");
        jwidth = env->GetFieldID(rectClass, "width", "D");
        jheight = env->GetFieldID(rectClass, "height", "D");

        bbox.x = env->GetDoubleField(rect, jx);
        bbox.y = env->GetDoubleField(rect, jy);
        bbox.width = env->GetDoubleField(rect, jwidth);
        bbox.height = env->GetDoubleField(rect, jheight);

        tracker->init(resultMat, bbox);

    }


    // Uncomment the line below to select a different bounding box
//    bbox = selectROI(frame, false);

    // Display bounding box.
    rectangle(frame, bbox, Scalar(255, 0, 0), 2, 1);


    double timer = (double) getTickCount();

    // Update the tracking result
    bool ok = tracker->update(resultMat, bbox);

    if (ok) {
        // Tracking success : Draw the tracked object
        rectangle(frame, bbox, Scalar(255, 0, 0), 2, 1);
    } else {
        // Tracking failure detected.
        putText(frame, "Tracking failure detected", Point(100, 80), FONT_HERSHEY_SIMPLEX, 0.75,
                Scalar(0, 0, 255), 2);
        resultMat = frame;
        return false;
    }

    resultMat = frame;

    return true;
}