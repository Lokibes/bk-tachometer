CONFIG_FILE := arm_linux_config.xml
SYSROOT := /home/oneadmin/android-ndk-r8b/platforms/android-8/arch-arm
ABI := -marm -march=armv7-a -mfloat-abi=softfp
VECTORIZE_OPTS := -ftree-vectorize -ftree-vectorizer-verbose=2
CFLAGS  := -c -O3 $(VECTORIZE_OPTS) -fsingle-precision-constant -Warmcc,--cpu=Cortex-A9,--fpu=VFPv3,--diag_warning=optimizations,--c99 --sysroot=$(SYSROOT) $(ABI)
CPPFLAGS := -mfpu=neon -marm -march=armv7-a -mfloat-abi=softfp -mtune=cortex-a9 -O3 $(VECTORIZE_OPTS) -fsingle-precision-constant -std=gnu++0x -c --sysroot=$(SYSROOT)
MORE_INCLUDES := -I/mnt/ramdisk/fftw3_cortex_a9/include -I./include -I./wavelets \
                 -I/mnt/ramdisk/Softfoundry/trunk/doubango/thirdparties/android/include/webrtc
CPP_MORE_INCLUDES := -I/home/oneadmin/android-ndk-r8b/sources/cxx-stl/gnu-libstdc++/4.4.3/include \
                     -I/home/oneadmin/android-ndk-r8b/sources/cxx-stl/gnu-libstdc++/4.4.3/libs/armeabi-v7a/include
                     
CLDFLAGS := --sysroot=$(SYSROOT) -L/mnt/ramdisk/Thesis/fftw3_cortex_a9/lib \
            -lfftw3f -lm
CPPLDFLAGS := --sysroot=$(SYSROOT) \
              -L/home/oneadmin/android-ndk-r8b/sources/cxx-stl/gnu-libstdc++/4.4.3/libs/armeabi-v7a \
              -lgnustl_static
#FAST_MATH := -ffast-math
FAST_MATH :=
STRIPPED_DIR := stripped
################################################################################

CC := armcc --arm_linux_config_file=$(CONFIG_FILE) --translate_gcc --arm
CPP := arm-linux-androideabi-g++
AR  := armar -r
STRIP_APP := fromelf --strip debug,comment,symbols --elf
STRIP_LIB := fromelf --strip debug,comment --elf

################################################################################

CC := armcc --arm_linux_config_file=$(CONFIG_FILE) --translate_gcc --arm
CPP := arm-linux-androideabi-g++

################################################################################

TARGET := libtachometer_core_armv7_a_cortex_a9.a
SRCS_C := tachometer_history.c \
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
RM=rm -rf $(1)
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
	$(call RM,$(STRIPPED_DIR))
	$(call RM,$(ASSEMBLY_FILES))
	$(call RM,output.txt)
	$(call RM,*~)
	
	
%.o: %.c $(CONFIG_FILE)
	$(CC) -MMD -MP -MF $@.d $(CFLAGS) $(FAST_MATH) $(MORE_INCLUDES) $< -o $@
	
%.o: %.cpp
	$(CPP) -MMD -MP -MF $@.d $(CPPFLAGS) $(MORE_INCLUDES) $(CPP_MORE_INCLUDES) $< -o $@
	
$(TARGET): $(OBJS) $(CONFIG_FILE)
	$(call MD,$(STRIPPED_DIR))
	$(AR) $(TARGET) $(OBJS)
	$(STRIP_LIB) $(TARGET) -o $(STRIPPED_DIR)/$(TARGET)
