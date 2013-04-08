LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)


LOCAL_MODULE    := tachometer_core
LOCAL_SRC_FILES := 	tachometer_core_wrap.c

LOCAL_ARM_NEON := true
LOCAL_CFLAGS := -marm -march=armv7-a -mfloat-abi=softfp -mtune=cortex-a9
LOCAL_LDLIBS := -L$(LOCAL_PATH) -ltachometer_core_armv7_a_cortex_a9 -lfftw3f

include $(BUILD_SHARED_LIBRARY)
