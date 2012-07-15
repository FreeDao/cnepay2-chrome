#include <string.h>
#include <jni.h>
#include <stdlib.h>
#include "com_tangye_android_iso8583_POSNative.h"
#include "DES.c"

int xorStr(char dest[], const char * src);

char* js2c(JNIEnv* env, jstring jstr);
jstring c2js(JNIEnv* env, const char* pat) ;

const int LENGTH = 7;
const char * const TAG = "nativeHelper";


jstring JNICALL Java_com_tangye_android_iso8583_POSNative_getNativeK
  (JNIEnv * env, jobject jboj, jstring pin, jstring card)
{
	//***************GET IMEI***********************
	jmethodID mID = (*env)->GetMethodID(env,  (*env)->GetObjectClass(env,jboj),
                                        "getContext", "()Landroid/content/Context;");
	if(mID == 0){
		return (*env)->NewStringUTF(env, "getContext failed");
	}
	jobject context = (*env)->CallObjectMethod(env, jboj, mID);
    
	mID = (*env)->GetMethodID(env, (*env)->GetObjectClass(env, context), "getSystemService", "(Ljava/lang/String;)Ljava/lang/Object;");
	if(mID == 0){
		return (*env)->NewStringUTF(env, "getSystemService failed");
	}
	jobject telManager = (*env)->CallObjectMethod(env, context, mID,  (*env)->NewStringUTF(env, "phone"));
	if(telManager == NULL){
		return (*env)->NewStringUTF(env, "getSystemService call failed");
	}
    
	mID = (*env)->GetMethodID(env, (*env)->GetObjectClass(env, telManager), "getDeviceId", "()Ljava/lang/String;");
	if(mID == 0){
		return (*env)->NewStringUTF(env, "getDeviceId failed");
	}
	jstring s_imei = (jstring)(*env)->CallObjectMethod(env, telManager, mID);
    
	//*****************end get IMEI********************************
    
	//*****************process IMEI, Pin, Card********************
	char * cImei = js2c(env, s_imei);
	char * cPin = js2c(env, pin);
	char * cCard = js2c(env, card);
    
	char destImei[LENGTH];	//保存IMEI处理结果
	if(xorStr(destImei, cImei) == 0){
		return (*env)->NewStringUTF(env, NULL);
	}
    
	char destPin[LENGTH];	//保存Pin处理结果
	if(xorStr(destPin, cPin) == 0){
		return (*env)->NewStringUTF(env, NULL);
	}
    
	char destCard[LENGTH];	//保存Card处理结果
	if(xorStr(destCard, cCard) == 0){
		return (*env)->NewStringUTF(env, NULL);
	}
    
	int i;
	char key[LENGTH];		//保存IMEI、Pin和Card最终处理结果
	for(i = 0; i < LENGTH; i++){
		key[i] = destImei[i] ^ destPin[i] ^ destCard[i];
	}
    
    
	//*************end process IMEI, Pin, Card**********************
    
	//*************DES *************************
    
	char en1[LENGTH + 2]= "iloveyou";
	char de1[LENGTH + 2];
	char en2[LENGTH + 2]= "yunzhoul";
	char de2[LENGTH + 2];
    
    
	keyfc(key);
	des(de1, en1, 0);
	des(de2, en2, 0);
    
	char de[LENGTH * 2 + 3];
	for(i = 0; i < LENGTH + 1; i++){
		de[i] = de1[i];
	}
	for(; i < (LENGTH * 2 + 2); i++){
		de[i] = de2[i - LENGTH - 1];
	}
	de[i] = '\0';
    
	//******************end DES*************************
    
	return c2js(env, de);
}

char* js2c(JNIEnv* env, jstring jstr)
{
    char* rtn = NULL;
    jclass clsstring = (*env)->FindClass(env, "java/lang/String");
    jstring strencode = (*env)->NewStringUTF(env, "ISO-8859-1");
    jmethodID mid = (*env)->GetMethodID(env, clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr= (jbyteArray)(*env)->CallObjectMethod(env, jstr, mid, strencode);
    jsize alen = (*env)->GetArrayLength(env, barr);
    jbyte* ba = (*env)->GetByteArrayElements(env, barr, JNI_FALSE);
    if (alen > 0)
    {
        rtn = (char*)malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    (*env)->ReleaseByteArrayElements(env, barr, ba, 0);
    return rtn;
}


jstring c2js(JNIEnv* env, const char* pat)
{
    jclass strClass = (*env)->FindClass(env, "java/lang/String");
    jmethodID ctorID = (*env)->GetMethodID(env, strClass, "<init>", "([BLjava/lang/String;)V");
    jbyteArray bytes = (*env)->NewByteArray(env, strlen(pat));
    (*env)->SetByteArrayRegion(env, bytes, 0, strlen(pat), (jbyte*)pat);
    jstring encoding = (*env)->NewStringUTF(env, "ISO-8859-1");
    return (jstring)(*env)->NewObject(env, strClass, ctorID, bytes, encoding);
}

int xorStr(char dest[], const char * src)
{
	if(src == NULL){
		return 0;
	}
	int len = strlen(src) / LENGTH;
	int i;
	if(len == 0){
		for(i = 0; i < strlen(src); i++){
			dest[i] = src[i];
		}
		for(; i < LENGTH; i++){
			dest[i] = '0';
		}
		return 1;
	}
	for(i = 0; i < LENGTH; i++){
		dest[i] = src[i];
	}
    
	for(i = 1; i < len; i++){
		int j;
		for(j = 0; j < LENGTH; j++){
			dest[i] = dest[i] ^ src[i * LENGTH + j];
		}
	}
	int left = (strlen(src) - len * LENGTH);
	if(left > 0){
		for(i = 0; i < left; i++){
			dest[i] = dest[i] ^ src[len * LENGTH + i];
		}
		for(; i < LENGTH; i++){
			dest[i] = dest[i] ^ '0';
		}
	}
	return 1;
}
    
    
