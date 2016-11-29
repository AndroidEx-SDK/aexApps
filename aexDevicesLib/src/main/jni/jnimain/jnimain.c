//
// Created by 杨军 on 16/3/26.
//
#include <jni.h>
#include <stdio.h>      /*标准输入输出定义*/
#include <stdlib.h>
#include <android/log.h>
#include "../aexKMY350/com_androidex_devices_aexddKMY350.h"
#include "../aexMt319/com_androidex_devices_aexddMT319.h"
#include "../aexddZTC70/com_androidex_devices_aexddZTC70.h"


jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    jint res;

    res = aexddKMY350_JNI_OnLoad(vm,reserved);
    res = aexddMT319_JNI_OnLoad(vm,reserved);
    res = aexddZTC70_JNI_OnLoad(vm,reserved);
    return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM* vm, void* reserved)
{
    aexddKMY350_JNI_OnUnload(vm,reserved);
    aexddMT319_JNI_OnUnload(vm,reserved);
    aexddZTC70_JNI_OnLoad(vm,reserved);
}
