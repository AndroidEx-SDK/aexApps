// IDoorLockInterface.aidl
package com.androidex;

// Declare any non-default types here with import statements

interface IDoorLockInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    //void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,double aDouble, String aString);
    int openDoor(int index,int delay);
    int closeDoor(int index);
}

