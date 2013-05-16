TOOLCHAIN := armcc
CONFIG_FILE := arm_linux_config.xml
SYSROOT := /home/oneadmin/android-ndk-r8b/platforms/android-8/arch-arm
ABI := -marm -march=armv7-a -mfloat-abi=softfp
VECTORIZE_OPTS := -ftree-vectorize -ftree-vectorizer-verbose=2
CC_OPTS := -c -O3 $(VECTORIZE_OPTS) -fsingle-precision-constant -Warmcc,--cpu=Cortex-A9,--fpu=VFPv3,--diag_warning=optimizations,--c99 --sysroot=$(SYSROOT) $(ABI)
MORE_INCLUDES := -I/mnt/ramdisk/Thesis/fftw3_cortex_a9/include -I./include
FAST_MATH := -ffast-math
#DEBUG_FLAGS := -DBUFFER_DEBUG
DEBUG_FLAGS :=
################################################################################

CC := armcc --arm_linux_config_file=$(CONFIG_FILE) --translate_gcc --arm
AR  := armar -r
STRIP_APP := fromelf --strip debug,comment,symbols --elf
STRIP_LIB := fromelf --strip debug,comment --elf

################################################################################

TARGET := libtachometer_core_armv7_a_cortex_a9.a

SRCS := auto_correlation.c \
		max_absolute_16_c.c \
		tachometer_history.c \
		tachometer_audio_buffer.c \
		tachometer_process.c
		
OBJS := $(SRCS:.c=.o)
DEPEND_FILES := $(SRCS:.c=.o.d)
ASSEMBLY_FILES := $(SRCS:.c=.s)

STRIPPED_DIR := stripped
		
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
	$(CC) -MMD -MP -MF $@.d $(CC_OPTS) $(FAST_MATH) $(DEBUG_FLAGS) $(MORE_INCLUDES) $< -o $@
	$(CC) -MMD -MP -MF $@.d $(CC_OPTS) $(FAST_MATH) $(DEBUG_FLAGS) $(MORE_INCLUDES) $< -S
	
$(TARGET): $(OBJS) $(CONFIG_FILE)
	$(call MD,$(STRIPPED_DIR))
	$(AR) $(TARGET) $(OBJS)
	$(STRIP_LIB) $(TARGET) -o $(STRIPPED_DIR)/$(TARGET)
