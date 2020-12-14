#include <opencv2/core.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/objdetect.hpp>
#include <string>
#include <vector>
#include <android/log.h>
#include <opencv2/imgproc.hpp>
#include <opencv2/imgproc/imgproc_c.h>
#include <opencv2/opencv.hpp>
#include <opencv2/core/types_c.h>
#include <jni.h>
#include <android/log.h>

using namespace std;
using namespace cv;

#define LOG_TAG "FaceDetectTools"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__))

class CascadeDetectorAdapter : public DetectionBasedTracker::IDetector {
public:
    CascadeDetectorAdapter(cv::Ptr<cv::CascadeClassifier> detector) :
            IDetector(),
            Detector(detector) {
        LOGD("CascadeDetectorAdapter::Detect::Detect");
        CV_Assert(detector);
    }

    void detect(const cv::Mat &Image, std::vector<cv::Rect> &objects) {
        LOGD("CascadeDetectorAdapter::Detect: begin");
        LOGD("CascadeDetectorAdapter::Detect: scaleFactor=%.2f, minNeighbours=%d, minObjSize=(%dx%d), maxObjSize=(%dx%d)",
             scaleFactor, minNeighbours, minObjSize.width, minObjSize.height, maxObjSize.width,
             maxObjSize.height);
        Detector->detectMultiScale(Image, objects, scaleFactor, minNeighbours, 0, minObjSize,
                                   maxObjSize);
        LOGD("CascadeDetectorAdapter::Detect: end");
    }

    virtual ~CascadeDetectorAdapter() {
        LOGD("CascadeDetectorAdapter::Detect::~Detect");
    }

private:
    CascadeDetectorAdapter();

    cv::Ptr<cv::CascadeClassifier> Detector;
};

struct DetectorAgregator {
    cv::Ptr<CascadeDetectorAdapter> mainDetector;
    cv::Ptr<CascadeDetectorAdapter> trackingDetector;

    cv::Ptr<DetectionBasedTracker> tracker;

    DetectorAgregator(cv::Ptr<CascadeDetectorAdapter> &_mainDetector,
                      cv::Ptr<CascadeDetectorAdapter> &_trackingDetector) :
            mainDetector(_mainDetector),
            trackingDetector(_trackingDetector) {
        CV_Assert(_mainDetector);
        CV_Assert(_trackingDetector);

        DetectionBasedTracker::Parameters DetectorParams;
        tracker = makePtr<DetectionBasedTracker>(mainDetector, trackingDetector, DetectorParams);
    }
};

JNIEXPORT

extern "C"
JNIEXPORT jlong JNICALL
Java_cn_cbsd_FaceUitls_FaceDetectTools_nativeCreateObject
        (JNIEnv *jenv, jclass, jstring jFileName, jint faceSize) {
    const char *jnamestr = jenv->GetStringUTFChars(jFileName, NULL);
    string stdFileName(jnamestr);
    jlong result = 0;

    LOGD("nativeCreateObject");

    try {
        cv::Ptr<CascadeDetectorAdapter> mainDetector = makePtr<CascadeDetectorAdapter>(
                makePtr<CascadeClassifier>(stdFileName));
        cv::Ptr<CascadeDetectorAdapter> trackingDetector = makePtr<CascadeDetectorAdapter>(
                makePtr<CascadeClassifier>(stdFileName));
        result = (jlong) new DetectorAgregator(mainDetector, trackingDetector);
        if (faceSize > 0) {
            mainDetector->setMinObjectSize(Size(faceSize, faceSize));
            //trackingDetector->setMinObjectSize(Size(faceSize, faceSize));
        }
    } catch (...) {
        LOGE("nativeCreateObject caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je,
                       "Unknown exception in JNI code of nativeCreateObject()");
        return 0;
    }

    LOGD("nativeCreateObject exit");
    return result;
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_cbsd_FaceUitls_FaceDetectTools_nativeDestroyObject
        (JNIEnv *jenv, jclass, jlong thiz) {
    LOGD("nativeDestroyObject");

    try {
        if (thiz != 0) {
            ((DetectorAgregator *) thiz)->tracker->stop();
            delete (DetectorAgregator *) thiz;
        }
    } catch (...) {
        LOGE("nativeDestroyObject caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je,
                       "Unknown exception in JNI code of nativeDestroyObject()");
    }
    LOGD("nativeDestroyObject exit");
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_cbsd_FaceUitls_FaceDetectTools_nativeStart
        (JNIEnv *jenv, jclass, jlong thiz) {
    LOGD("nativeStart");

    try {
        ((DetectorAgregator *) thiz)->tracker->run();
    } catch (...) {
        LOGE("nativeStart caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code of DetectionBasedTracker.nativeStart()");
    }
    LOGD("nativeStart exit");
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_cbsd_FaceUitls_FaceDetectTools_nativeStop
        (JNIEnv *jenv, jclass, jlong thiz) {
    LOGD("nativeStop");

    try {
        ((DetectorAgregator *) thiz)->tracker->stop();
    } catch (...) {
        LOGE("nativeStop caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code of DetectionBasedTracker.nativeStop()");
    }
    LOGD("nativeStop exit");
}


extern "C"
JNIEXPORT void JNICALL
Java_cn_cbsd_FaceUitls_FaceDetectTools_nativeSetFaceSize
        (JNIEnv *jenv, jclass, jlong thiz, jint faceSize) {
    LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeSetFaceSize -- BEGIN");

    try {
        if (faceSize > 0) {
            ((DetectorAgregator *) thiz)->mainDetector->setMinObjectSize(Size(faceSize, faceSize));
            //((DetectorAgregator*)thiz)->trackingDetector->setMinObjectSize(Size(faceSize, faceSize));
        }
    } catch (...) {
        LOGD("nativeSetFaceSize caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je,
                       "Unknown exception in JNI code of DetectionBasedTracker.nativeSetFaceSize()");
    }
    LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeSetFaceSize -- END");
}



//extern "C"
//JNIEXPORT void JNICALL
//Java_cn_cbsd_FaceUitls_FaceDetectTools_nativeDetectRects
//        (JNIEnv *jenv, jclass, jlong thiz, jbyteArray image, jint w, jint h, jobject list_obj,
//         jint rotation, jboolean mirror) {
//
//    LOGD("nativeDetectRects -- Start");
//
//    jclass RectCls = jenv->FindClass("android/graphics/Rect");
//    jmethodID Rect_costruct = jenv->GetMethodID(RectCls, "<init>", "()V");
//    jmethodID Rect_set = jenv->GetMethodID(RectCls, "set", "(IIII)V");
//    jobject Rect_obj = jenv->NewObject(RectCls, Rect_costruct);
//    jclass listFcls = jenv->FindClass("java/util/ArrayList");
//    jmethodID list_add = jenv->GetMethodID(listFcls, "add", "(Ljava/lang/Object;)Z");
//
//    jbyte *cbuf;
//    cbuf = jenv->GetByteArrayElements(image, 0);
//
//    Mat Mat_dst;
//    Mat imgData(h, w, CV_8UC1, (unsigned char *) cbuf);
//    if (mirror) {
//        flip(imgData, imgData, 0);
//    }
//    if (rotation == 0) {
//        Mat_dst = imgData;
//
////        Point center(imgData.cols / 2, imgData.rows / 2); //旋转中心
////        Mat Mat_rot = getRotationMatrix2D(center, rotation, scale);
////        warpAffine(imgData, Mat_dst, Mat_rot, imgData.size());
//
//    } else if (rotation == 90) {
//        transpose(imgData, Mat_dst);
//        flip(Mat_dst, Mat_dst, 0);
//    } else if (rotation == 180) {
//        transpose(imgData, Mat_dst);
//        flip(Mat_dst, Mat_dst, -1);
//    } else if (rotation == 270) {
//        transpose(imgData, Mat_dst);
//        flip(Mat_dst, Mat_dst, 1);
//    }
//
//
//    int size = Mat_dst.total() * Mat_dst.elemSize();
//    jbyte *bytes = new jbyte[size];  // you will have to delete[] that later
//    memcpy(bytes, Mat_dst.data, size * sizeof(jbyte));
//    jenv->SetByteArrayRegion(image, 0, size, bytes);
//
//
//    try {
//        vector<Rect> RectFaces;
//        ((DetectorAgregator *) thiz)->tracker->process(Mat_dst);
//        ((DetectorAgregator *) thiz)->tracker->getObjects(RectFaces);
//        jenv->ReleaseByteArrayElements(image, cbuf, 0);
//        if (RectFaces.size() > 0) {
//            for (int i = 0; i < RectFaces.size(); i++) {
//                if (mirror) {
//                    jenv->CallVoidMethod(Rect_obj, Rect_set,
//                                         RectFaces[i].tl().x,
//                                         RectFaces[i].tl().y,
//                                         RectFaces[i].br().x,
//                                         RectFaces[i].br().y);
//
//                    jenv->CallBooleanMethod(list_obj, list_add, Rect_obj);
//                } else {
//                    jenv->CallVoidMethod(Rect_obj, Rect_set,
//                                         h - RectFaces[i].tl().x,
//                                         RectFaces[i].tl().y,
//                                         h - RectFaces[i].br().x,
//                                         RectFaces[i].br().y);
//
//                    jenv->CallBooleanMethod(list_obj, list_add, Rect_obj);
//                }
//
//            }
//        }
//        jenv->DeleteLocalRef(Rect_obj);
//        jenv->DeleteLocalRef(RectCls);
//        jenv->DeleteLocalRef(listFcls);
//        jenv->ReleaseByteArrayElements(image, cbuf, 0);
//        LOGD("nativeDetectRList END");
//    } catch (...) {
//        LOGE("nativeDetect caught unknown exception");
//        jclass je = jenv->FindClass("java/lang/Exception");
//        jenv->ThrowNew(je, "Unknown exception in JNI code DetectionBasedTracker.nativeDetect()");
//    }
//}



extern "C"
JNIEXPORT void JNICALL
Java_cn_cbsd_FaceUitls_FaceDetectTools_nativeDetectRects
        (JNIEnv *jenv, jclass, jlong thiz, jbyteArray image, jint w, jint h, jobject list_obj,
         jint rotation, jboolean mirror) {

    LOGD("nativeDetectRects -- Start");

    jclass RectCls = jenv->FindClass("android/graphics/Rect");
    jmethodID Rect_costruct = jenv->GetMethodID(RectCls, "<init>", "()V");
    jmethodID Rect_set = jenv->GetMethodID(RectCls, "set", "(IIII)V");
    jobject Rect_obj = jenv->NewObject(RectCls, Rect_costruct);
    jclass listFcls = jenv->FindClass("java/util/ArrayList");
    jmethodID list_add = jenv->GetMethodID(listFcls, "add", "(Ljava/lang/Object;)Z");

    jbyte *cbuf;
    cbuf = jenv->GetByteArrayElements(image, 0);

    Mat Mat_dst;
    Mat imgData(h, w, CV_8UC1, (unsigned char *) cbuf);

    if ((rotation == 0)) {
        if (mirror) {
            flip(imgData, imgData, 1);
        }
        Mat_dst = imgData;
    } else {
        if (mirror) {
            flip(imgData, imgData, 0);
        }
        if (rotation == 90) {
            transpose(imgData, Mat_dst);
            flip(Mat_dst, Mat_dst, 0);
        } else if (rotation == 180) {
            transpose(imgData, Mat_dst);
            flip(Mat_dst, Mat_dst, -1);
        } else if (rotation == 270) {
            transpose(imgData, Mat_dst);
            flip(Mat_dst, Mat_dst, 1);
        }
    }

//        Point center(imgData.cols / 2, imgData.rows / 2); //旋转中心
//        Mat Mat_rot = getRotationMatrix2D(center, rotation, scale);
//        warpAffine(imgData, Mat_dst, Mat_rot, imgData.size());


//    int size = Mat_dst.total() * Mat_dst.elemSize();
//    jbyte *bytes = new jbyte[size];  // you will have to delete[] that later
//    memcpy(bytes, Mat_dst.data, size * sizeof(jbyte));
//    jenv->SetByteArrayRegion(image, 0, size, bytes);


    try {
        vector<Rect> RectFaces;
        ((DetectorAgregator *) thiz)->tracker->process(Mat_dst);
        ((DetectorAgregator *) thiz)->tracker->getObjects(RectFaces);
        jenv->ReleaseByteArrayElements(image, cbuf, 0);
        if (RectFaces.size() > 0) {
            for (int i = 0; i < RectFaces.size(); i++) {
                jenv->CallVoidMethod(Rect_obj, Rect_set,
                                     RectFaces[i].tl().x,
                                     RectFaces[i].tl().y,
                                     RectFaces[i].br().x,
                                     RectFaces[i].br().y);

                jenv->CallBooleanMethod(list_obj, list_add, Rect_obj);
            }
        }
        jenv->DeleteLocalRef(Rect_obj);
        jenv->DeleteLocalRef(RectCls);
        jenv->DeleteLocalRef(listFcls);
        jenv->ReleaseByteArrayElements(image, cbuf, 0);
        LOGD("nativeDetectRList END");
    } catch (...) {
        LOGE("nativeDetect caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code DetectionBasedTracker.nativeDetect()");
    }
}