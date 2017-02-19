# Android Makefile Parser

The purpose of this is to start with a device tree directory and output a hierarchy of dependent makefiles.

This can be useful when you are getting build errors and you are getting lost following all the includes back and forth.

It requires a minimum of Java 7.

To run the program you pass the root of your ROM directory and the directory of your device tree:

    java -jar mk-parser-1.0-SNAPSHOT.jar /home/buildbot/android/ /home/buildbot/android/device/samsung/maguro/
    