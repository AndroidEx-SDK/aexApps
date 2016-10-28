//
// Created by 杨军 on 16/3/26.
//
#include <jni.h>
#include <stdio.h>      /*标准输入输出定义*/
#include <stdlib.h>
#include <android/log.h>
#include "../aexPasswordKeypad/com_androidex_devices_aexddPasswordKeypad.h"
#include "../aexPoscReader/com_androidex_devices_aexddPoscReader.h"
#include "../aexPrinter/com_androidex_devices_aexddPrinter.h"


jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    jint res;

    res = aexddPasswordKeypad_JNI_OnLoad(vm,reserved);
    res = aexddPoscReader_JNI_OnLoad(vm,reserved);
    res = aexddPrinter_JNI_OnLoad(vm,reserved);
    return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved)
{
    aexddPasswordKeypad_JNI_OnUnload(vm,reserved);
    aexddPoscReader_JNI_OnUnload(vm,reserved);
    aexddPrinter_JNI_OnUnload(vm,reserved);
}
