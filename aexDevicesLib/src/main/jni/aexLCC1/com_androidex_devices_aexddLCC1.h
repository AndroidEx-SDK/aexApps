/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_androidex_devices_aexddMT319 */

#ifndef _Included_com_androidex_devices_aexddLCC1
#define _Included_com_androidex_devices_aexddLCC1
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL aexddLCC1_JNI_OnLoad(JavaVM* vm, void* reserved);
void JNICALL aexddLCC1_JNI_OnUnload(JavaVM* vm, void* reserved);

JNIEXPORT jbyteArray JNICALL Java_com_androidex_devices_aexddLCC1Reader_lcc1ReadPacket
        (JNIEnv *, jobject, jint,jint);
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddLCC1Reader_lcc1ReadCardLoop
        (JNIEnv *, jobject, jint,jint);
JNIEXPORT void JNICALL Java_com_androidex_devices_aexddLCC1Reader_lcc1SendCmd
        (JNIEnv *, jobject, jint, jbyteArray , jint);
JNIEXPORT void JNICALL Java_com_androidex_devices_aexddLCC1Reader_lcc1SendHexCmd
        (JNIEnv *, jobject, jint, jstring);

#ifdef __cplusplus
}
#endif
#endif
