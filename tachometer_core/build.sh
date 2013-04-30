#! /bin/sh 
# ./build.sh
#

PROJECT_DIR=/mnt/ramdisk/Thesis/bk-tachometer/trunk/tachometer_core
MOBILE_DIR=/home/minhluan/dev
JNI_DIR=/mnt/ramdisk/Thesis/bk-tachometer/trunk/Android/jni

case "$1" in
	clean)
		cd $PROJECT_DIR &&\
		make clean
		;;
  deploy)
    echo "Making a release version ..." &&\
		cd $PROJECT_DIR &&\
		cp -f ./Makefile_deploy ./Makefile &&\
		make clean &&\
		make &&\
		adb push $PROJECT_DIR/fftw3_test $MOBILE_DIR/fftw3_test
    ;;
	debug)
		echo "Making a debug version ..." &&\
		cd $PROJECT_DIR &&\
		cp -f ./Makefile_debug ./Makefile &&\
		make clean &&\
		make &&\
		adb push $PROJECT_DIR/fftw3_test $MOBILE_DIR/fftw3_test
		;;
	release)
    echo "Making a release version ..." &&\
		cd $PROJECT_DIR &&\
		cp -f ./Makefile_release ./Makefile &&\
		make clean &&\
		make &&\
		cp -f $PROJECT_DIR/stripped/libtachometer_core_armv7_a_cortex_a9.a $JNI_DIR/libtachometer_core_armv7_a_cortex_a9.a
    ;;
  *)
    echo "Usage: build.sh {clean|deploy|debug|release}"
    exit 1
    ;;
esac

exit 0
