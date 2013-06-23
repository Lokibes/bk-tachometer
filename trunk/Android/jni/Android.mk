LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)


LOCAL_MODULE    := tachometer_core
LOCAL_SRC_FILES := 	tachometer_core_wrap.c

LOCAL_ARM_NEON := true
LOCAL_CFLAGS := -marm -march=armv7-a -mfloat-abi=softfp -mtune=cortex-a9
LOCAL_LDLIBS := -L$(LOCAL_PATH) -L/home/oneadmin/android-ndk-r8e/sources/cxx-stl/gnu-libstdc++/4.7/libs/armeabi-v7a -ltachometer_core_armv7_a_cortex_a9 -lfftw3f -lgnustl_static

include $(BUILD_SHARED_LIBRARY)
