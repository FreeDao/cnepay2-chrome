LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE := libPOSNative
LOCAL_SRC_FILES := com_tangye_android_iso8583_POSNative.c
include $(BUILD_SHARED_LIBRARY)