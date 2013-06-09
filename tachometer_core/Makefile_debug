SYSROOT := /home/oneadmin/android-ndk-r8e/platforms/android-8/arch-arm
MORE_INCLUDES := -I/mnt/ramdisk/Thesis/fftw3_cortex_a9/include -I./include -I./wavelets \
                 -I/mnt/ramdisk/Softfoundry/trunk/doubango/thirdparties/android/include/webrtc
CC := arm-linux-androideabi-gcc
CPP := arm-linux-androideabi-g++
CFLAGS := -mfpu=neon -marm -march=armv7-a -mfloat-abi=softfp -mtune=cortex-a9 -O0 -g -std=gnu99 -c --sysroot=$(SYSROOT)
CPPFLAGS := -mfpu=neon -marm -march=armv7-a -mfloat-abi=softfp -mtune=cortex-a9 -O0 -g -std=gnu++0x -c --sysroot=$(SYSROOT)
CPP_MORE_INCLUDES := -I/home/oneadmin/android-ndk-r8e/sources/cxx-stl/gnu-libstdc++/4.4.3/include \
                     -I/home/oneadmin/android-ndk-r8e/sources/cxx-stl/gnu-libstdc++/4.4.3/libs/armeabi-v7a/include
                     
CLDFLAGS := --sysroot=$(SYSROOT) -L/mnt/ramdisk/Thesis/fftw3_cortex_a9/lib \
            -lfftw3f -lm
CPPLDFLAGS := --sysroot=$(SYSROOT) \
              -L/home/oneadmin/android-ndk-r8e/sources/cxx-stl/gnu-libstdc++/4.4.3/libs/armeabi-v7a \
              -lgnustl_static
#DEBUG_FLAGS := -DBUFFER_DEBUG
DEBUG_FLAGS := -DTACHO_DEBUG
################################################################################

TARGET := fftw3_test
SRCS_C := fftw3_prb.c \
		max_absolute_16_c.c \
		tachometer_history.c \
		tachometer_audio_buffer.c \
		tachometer_process.c
		
SRCS_CPP := wavelets/tachometer_wavelet1d.cpp \
            wavelets/wavelet2s.cpp \
            wavelets/tachometer_wavelet_denoiser.cpp

OBJS_C := $(SRCS_C:.c=.o)
OBJS_CPP := $(SRCS_CPP:.cpp=.o)
OBJS := $(OBJS_C) $(OBJS_CPP)

DEPEND_FILES := $(OBJS:.o=.o.d)
ASSEMBLY_FILES := $(OBJS:.o=.s)

#  Building on Linux
RPATH='$$ORIGIN'
DONE=@if [ -f $(1) ]; then echo Build completed.; fi
RM=rm -f $(1)
MD=@if [ ! -d $(1) ]; then mkdir $(1); fi
CP=cp

#######################################################

all: $(TARGET)
		$(call DONE,$(TARGET))
		
rebuild: clean all

clean:
	$(call RM,$(OBJS))
	$(call RM,$(DEPEND_FILES))
	$(call RM,$(TARGET))
	$(call RM,$(ASSEMBLY_FILES))
	$(call RM,output.txt)
	$(call RM,*~)

%.o: %.c
	$(CC) -MMD -MP -MF $@.d $(CFLAGS) $(DEBUG_FLAGS) $(MORE_INCLUDES) $< -o $@
	
%.o: %.cpp
	$(CPP) -MMD -MP -MF $@.d $(CPPFLAGS) $(DEBUG_FLAGS) $(MORE_INCLUDES) $(CPP_MORE_INCLUDES) $< -o $@

$(TARGET): $(OBJS)
	$(CPP) $(OBJS_C) $(CLDFLAGS) $(OBJS_CPP) $(CPPLDFLAGS) $(MORE_INCLUDES) -o $@
