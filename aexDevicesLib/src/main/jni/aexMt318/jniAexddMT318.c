#include <jni.h>
#include     <ctype.h>
#include     <string.h>
#include     <stdlib.h>
#include     <locale.h>
#include     <wchar.h>
#include "com_androidex_devices_aexddMT318.h"
#include "aexddMT318.h"
#include <android/log.h>
#include "../include/utils.h"

#define FALSE 0
#define TRUE  1

/**
 * 密码键盘的全局句柄，在未调用kmy_open的命令前，此变量为NULL。
 * 在调用kmy_close后恢复为NULL
 */
static jclass mt318Provider=NULL;
static jmethodID javakkcEvent=NULL;

JNIEXPORT jint JNICALL aexddMT318_JNI_OnLoad(JavaVM* vm, void* reserved)
{
	return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL aexddMT318_JNI_OnUnload(JavaVM* vm, void* reserved)
{
}

static jclass getProvider(JNIEnv* env)
{
	return (*env)->FindClass(env,"com/androidex/devices/aexddMT318Reader");
}

static jmethodID getMethod(JNIEnv* env,char *func,char *result)
{
	if(mt318Provider==NULL)
		mt318Provider = getProvider(env);
	if(mt318Provider)
	{
		return (*env)->GetMethodID(env, mt318Provider, func,result);
	}else{
		return NULL;
	}
}

/**
 * 调用了Java对应IC卡和银行卡处理方法的函数，此函数会用于密码键盘的事件处理
 */
static int jni_kkcard_event(JNIEnv* env,jobject obj,int code,char *msg)
{
	JNIEnv* jniEnv = (JNIEnv*)env;
	jobject javaObject = (jobject)obj;

    if(jniEnv == NULL && javaObject == NULL) {
        return 0;
    }

	if(javakkcEvent==NULL){
		javakkcEvent = getMethod(env,"onBackCallEvent","(ILjava/lang/String;)V");
	}
	if(javakkcEvent){
		jstring strmsg = (*jniEnv)->NewStringUTF(jniEnv, (const char *)msg);
		(*jniEnv)->CallVoidMethod(jniEnv, javaObject, javakkcEvent,(jint)code,strmsg);
		return TRUE;
	}else{
		return FALSE;
	}
	return TRUE;
}

JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddMT318Reader_mt318ReadCardLoop
        (JNIEnv *env, jobject this, jint fd,jint timeout)
{
    kkcard_set_event(jni_kkcard_event);
    return kkcard_read_loop(env,this,fd,timeout);
}

JNIEXPORT void JNICALL Java_com_androidex_devices_aexddMT318Reader_mt318SendCmd
        (JNIEnv *env, jobject this, jint fd, jstring cmd, jint size)
{
    char *strCmd = (char *) (*env)->GetStringUTFChars(env,cmd, 0);

    kkcard_set_event(jni_kkcard_event);
    kkcard_send_cmd(env,this,fd,strCmd,size);
    (*env)->ReleaseStringUTFChars(env, cmd, strCmd);
}

JNIEXPORT void JNICALL Java_com_androidex_devices_aexddMT318Reader_mt318SendHexCmd
        (JNIEnv *env, jobject this, jint fd, jstring hexcmd)
{
    char *strCmd = (char *) (*env)->GetStringUTFChars(env,hexcmd, 0);
    int len = strlen(strCmd);
    int dlen = len;
    char *buf = malloc(dlen);
    int r = 0;

    kkcard_set_event(jni_kkcard_event);
    if(buf){
        memset(buf,0,dlen);
        HexDecode(strCmd,len,buf,&dlen);
        //__android_log_print(ANDROID_LOG_DEBUG,"MT318","writeHex([%d=>%d]%s)",len,dlen,strCmd);
        r = kkcard_send_cmd(env,this,fd,buf,dlen);
        free(buf);
    }else {
        //__android_log_print(ANDROID_LOG_DEBUG,"MT318","writeHex error: malloc buf error.dlen=%d",dlen);
    }
    (*env)->ReleaseStringUTFChars(env, hexcmd, strCmd);
}
