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

extern "C"
JNIEXPORT jlong JNICALL
Java_cn_cbsd_aliveandfacedetect_Func_Func_1Camera_mvp_module_FaceDetectTools_nativeCreateObject
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
    }
    catch (const cv::Exception &e) {
        LOGE("nativeCreateObject caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass(
                "cn/cbsd/aliveandfacedetect/Func/Func_Camera/mvp/module/CvException");
        if (!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...) {
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
Java_cn_cbsd_aliveandfacedetect_Func_Func_1Camera_mvp_module_FaceDetectTools_nativeDestroyObject
        (JNIEnv *jenv, jclass, jlong thiz) {
    LOGD("nativeDestroyObject");

    try {
        if (thiz != 0) {
            ((DetectorAgregator *) thiz)->tracker->stop();
            delete (DetectorAgregator *) thiz;
        }
    }
    catch (const cv::Exception &e) {
        LOGE("nativeestroyObject caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass(
                "cn/cbsd/aliveandfacedetect/Func/Func_Camera/mvp/module/CvException");
        if (!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...) {
        LOGE("nativeDestroyObject caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je,
                       "Unknown exception in JNI code of nativeDestroyObject()");
    }
    LOGD("nativeDestroyObject exit");
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_cbsd_aliveandfacedetect_Func_Func_1Camera_mvp_module_FaceDetectTools_nativeStart
        (JNIEnv *jenv, jclass, jlong thiz) {
    LOGD("nativeStart");

    try {
        ((DetectorAgregator *) thiz)->tracker->run();
    }
    catch (const cv::Exception &e) {
        LOGE("nativeStart caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass(
                "cn/cbsd/aliveandfacedetect/Func/Func_Camera/mvp/module/CvException");
        if (!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...) {
        LOGE("nativeStart caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code of DetectionBasedTracker.nativeStart()");
    }
    LOGD("nativeStart exit");
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_cbsd_aliveandfacedetect_Func_Func_1Camera_mvp_module_FaceDetectTools_nativeStop
        (JNIEnv *jenv, jclass, jlong thiz) {
    LOGD("nativeStop");

    try {
        ((DetectorAgregator *) thiz)->tracker->stop();
    }
    catch (const cv::Exception &e) {
        LOGE("nativeStop caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass(
                "cn/cbsd/aliveandfacedetect/Func/Func_Camera/mvp/module/CvException");
        if (!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...) {
        LOGE("nativeStop caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code of DetectionBasedTracker.nativeStop()");
    }
    LOGD("nativeStop exit");
}

extern "C"
JNIEXPORT void JNICALL
Java_cn_cbsd_aliveandfacedetect_Func_Func_1Camera_mvp_module_FaceDetectTools_nativeDetectRectsRotate90
        (JNIEnv *jenv, jclass, jlong thiz, jbyteArray image, jint w, jint h, jobject list_obj) {

    jclass RectCls = jenv->FindClass("android/graphics/Rect");
    jmethodID Rect_costruct = jenv->GetMethodID(RectCls, "<init>", "()V");
    jmethodID Rect_set = jenv->GetMethodID(RectCls, "set", "(IIII)V");
    jobject Rect_obj = jenv->NewObject(RectCls, Rect_costruct);
    jclass listFcls = jenv->FindClass("java/util/ArrayList");
    jmethodID list_add = jenv->GetMethodID(listFcls, "add", "(Ljava/lang/Object;)Z");

    jbyte *cbuf;
    cbuf = jenv->GetByteArrayElements(image, 0);


    Mat filp_horMat, dst;
    Mat imgData(h, w, CV_8UC1, (unsigned char *) cbuf);
    Point center(imgData.cols / 2, imgData.rows / 2); //旋转中心
    Mat rotMat = getRotationMatrix2D(center, 90.0, 1.0);
    warpAffine(imgData, dst, rotMat, imgData.size());
    flip(dst,filp_horMat,1);

//    int size = filp_horMat.total() * filp_horMat.elemSize();
//    jbyte *bytes = new jbyte[size];  // you will have to delete[] that later
//    memcpy(bytes, filp_horMat.data, size * sizeof(jbyte));
//    jenv->SetByteArrayRegion(image, 0, size, bytes);


    try {
        vector<Rect> RectFaces;
        ((DetectorAgregator *) thiz)->tracker->process(filp_horMat);
        ((DetectorAgregator *) thiz)->tracker->getObjects(RectFaces);
        jenv->ReleaseByteArrayElements(image, cbuf, 0);
        if (RectFaces.size() > 0) {
            __android_log_print(ANDROID_LOG_DEBUG, "人脸数", "FaceSize : %d", RectFaces.size());

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
    }
    catch (const cv::Exception &e) {
        LOGE("nativeCreateObject caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass(
                "cn/cbsd/aliveandfacedetect/Func/Func_Camera/mvp/module/CvException");
        if (!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...) {
        LOGE("nativeDetect caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code DetectionBasedTracker.nativeDetect()");
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_cn_cbsd_aliveandfacedetect_Func_Func_1Camera_mvp_module_FaceDetectTools_nativeDetectRects
        (JNIEnv *jenv, jclass, jlong thiz, jbyteArray image, jint w, jint h, jobject list_obj) {

    jclass RectCls = jenv->FindClass("android/graphics/Rect");
    jmethodID Rect_costruct = jenv->GetMethodID(RectCls, "<init>", "()V");
    jmethodID Rect_set = jenv->GetMethodID(RectCls, "set", "(IIII)V");
    jobject Rect_obj = jenv->NewObject(RectCls, Rect_costruct);
    jclass listFcls = jenv->FindClass("java/util/ArrayList");
    jmethodID list_add = jenv->GetMethodID(listFcls, "add", "(Ljava/lang/Object;)Z");

    jbyte *cbuf;
    cbuf = jenv->GetByteArrayElements(image, 0);


//    Mat dst;
    Mat imgData(h, w, CV_8UC1, (unsigned char *) cbuf);
//    Point center(imgData.cols/2,imgData.rows/2); //旋转中心
//    Mat rotMat = getRotationMatrix2D(center,90.0,1.0);
//    warpAffine(imgData,dst,rotMat,imgData.size());

    try {
        vector<Rect> RectFaces;
        ((DetectorAgregator *) thiz)->tracker->process(imgData);
        ((DetectorAgregator *) thiz)->tracker->getObjects(RectFaces);
        jenv->ReleaseByteArrayElements(image, cbuf, 0);
        if (RectFaces.size() > 0) {
            __android_log_print(ANDROID_LOG_DEBUG, "人脸数", "FaceSize : %d", RectFaces.size());

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

        LOGD("nativeDetectRList END");
        //*((Mat*)faces) = Mat(RectFaces, true);
    }
    catch (const cv::Exception &e) {
        LOGE("nativeCreateObject caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass(
                "cn/cbsd/aliveandfacedetect/Func/Func_Camera/mvp/module/CvException");
        if (!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...) {
        LOGE("nativeDetect caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, "Unknown exception in JNI code DetectionBasedTracker.nativeDetect()");
    }

}

extern "C"
JNIEXPORT void JNICALL
Java_cn_cbsd_aliveandfacedetect_Func_Func_1Camera_mvp_module_FaceDetectTools_nativeSetFaceSize
        (JNIEnv *jenv, jclass, jlong thiz, jint faceSize) {
    LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeSetFaceSize -- BEGIN");

    try {
        if (faceSize > 0) {
            ((DetectorAgregator *) thiz)->mainDetector->setMinObjectSize(Size(faceSize, faceSize));
            //((DetectorAgregator*)thiz)->trackingDetector->setMinObjectSize(Size(faceSize, faceSize));
        }
    }
    catch (const cv::Exception &e) {
        LOGD("nativeStop caught cv::Exception: %s", e.what());
        jclass je = jenv->FindClass(
                "cn/cbsd/aliveandfacedetect/Func/Func_Camera/mvp/module/CvException");
        if (!je)
            je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je, e.what());
    }
    catch (...) {
        LOGD("nativeSetFaceSize caught unknown exception");
        jclass je = jenv->FindClass("java/lang/Exception");
        jenv->ThrowNew(je,
                       "Unknown exception in JNI code of DetectionBasedTracker.nativeSetFaceSize()");
    }
    LOGD("Java_org_opencv_samples_facedetect_DetectionBasedTracker_nativeSetFaceSize -- END");
}
