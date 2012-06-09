#include<jni.h>
#include<string.h>
#include"com_tangye_android_iso8583_POSNative.h"


JNIEXPORT jstring JNICALL Java_com_tangye_android_iso8583_POSNative_getNativeK
  (JNIEnv *env, jobject obj, jstring pin, jstring card) {
    return (*env)->NewStringUTF(env,(char*)"Hello,JNITest");
}