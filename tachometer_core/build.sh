#! /bin/sh 
# ./build.sh
#

PROJECT_DIR=/mnt/ramdisk/Thesis/trunk/tachometer_core
MOBILE_DIR=/home/minhluan/dev
JNI_DIR=/mnt/ramdisk/Thesis/trunk/Android/jni

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
	test_neon)
		echo "Making a test with neon version ..." &&\
		cd $PROJECT_DIR &&\
		cp -f ./Makefile_test_neon ./Makefile &&\
		make clean &&\
		make &&\
		adb push $PROJECT_DIR/tacho_test_neon $MOBILE_DIR/tacho_test_neon
		;;
	test_no_neon)
		echo "Making a test without neon version ..." &&\
		cd $PROJECT_DIR &&\
		cp -f ./Makefile_test_no_neon ./Makefile &&\
		make clean &&\
		make &&\
		adb push $PROJECT_DIR/tacho_test_no_neon $MOBILE_DIR/tacho_test_no_neon
		;;
  *)
    echo "Usage: build.sh {clean|deploy|debug|release}"
    exit 1
    ;;
esac

exit 0
