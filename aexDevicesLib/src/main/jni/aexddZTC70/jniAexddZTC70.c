#include <jni.h>
#include     <ctype.h>
#include     <string.h>
#include     <stdlib.h>
#include     <locale.h>
#include     <wchar.h>
#include "com_androidex_devices_aexddZTC70.h"
#include "aexddZTC70.h"
#include <android/log.h>

#define FALSE 0
#define TRUE  1

#define tag "ZTC"
static jclass openkkProvider=NULL;
static jmethodID javaEvent=NULL;

JNIEXPORT jint JNICALL aexddZTC70_JNI_OnLoad(JavaVM *vm, void *reserved)
{
     return JNI_VERSION_1_4; /* the required JNI version */
}

JNIEXPORT void JNICALL aexddZTC70_JNI_OnUnload(JavaVM* vm, void* reserved)
{
}

static jclass getProvider(JNIEnv* env)
{
	return (*env)->FindClass(env,"com/androidex/devices/aexddZTC70");
}

static jmethodID getMethod(JNIEnv* env,char *func,char *result)
{
	if(openkkProvider==NULL)
		openkkProvider = getProvider(env);
	if(openkkProvider)
	{
		__android_log_print(ANDROID_LOG_DEBUG, tag, "GetMethodId");
		return (*env)->GetMethodID(env, openkkProvider, func,result);
	}else{
		__android_log_print(ANDROID_LOG_DEBUG, tag, "mt318Provider is NULL");
		return NULL;
	}
}

/**
 * 调用了Java对应密码键盘处理方法的函数，此函数会用于密码键盘的事件处理
 */
static int jni_ztc_event(HZTC env,HZTC obj,int code,char *msg)
{
	JNIEnv* jniEnv = (JNIEnv*)env;
	jobject javaObject = (jobject)obj;

    if(jniEnv == NULL && javaObject == NULL) {
        return 0;
    }
	if(javaEvent==NULL){
		javaEvent = getMethod(env,"onBackCallEvent","(ILjava/lang/String;)V");
	}
	if(javaEvent){
		jstring strmsg = (*jniEnv)->NewStringUTF(jniEnv, (const char *)msg);
		(*jniEnv)->CallVoidMethod(jniEnv, javaObject, javaEvent,(jint)code,strmsg);
		return TRUE;
	}else{
		return FALSE;
	}
	return TRUE;
}

JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddZTC70_ztReadKeyLoop
        (JNIEnv *env, jobject this, jint fd,jint timeout)
{
    ztc_set_event(jni_ztc_event);
    return ztc_read_key_loop(env,this,fd,timeout);
}

JNIEXPORT void JNICALL Java_com_androidex_devices_aexddZTC70_ztSendCmd
        (JNIEnv *env, jobject this, jint fd, jstring cmd, jint size)
{
    char *strCmd = (char *) (*env)->GetStringUTFChars(env,cmd, 0);

    ztc_set_event(jni_ztc_event);
    ztc_send_cmd(env,this,fd,strCmd,size);
    (*env)->ReleaseStringUTFChars(env, cmd, strCmd);
}

JNIEXPORT void JNICALL Java_com_androidex_devices_aexddZTC70_ztSendHexCmd
        (JNIEnv *env, jobject this, jint fd, jstring hexcmd, jint size)
{
    char *strCmd = (char *) (*env)->GetStringUTFChars(env,hexcmd, 0);

    ztc_set_event(jni_ztc_event);
    ztc_send_hexcmd(env,this,fd,strCmd,size);
    (*env)->ReleaseStringUTFChars(env, hexcmd, strCmd);
}
