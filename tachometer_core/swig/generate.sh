#! /bin/sh 
# ./generate.sh
#

swig -java -package vn.edu.hcmut.tachometer.core -outdir . -o tachometer_core_wrap.c ../tachometer_process.i
