#include <jni.h>
#include     <ctype.h>
#include     <string.h>
#include     <stdlib.h>
#include     <locale.h>
#include     <wchar.h>
#include "com_androidex_devices_aexddKMY350.h"
#include "aexddKMY350.h"
#include <android/log.h>

#define FALSE 0
#define TRUE  1

static jclass openkkProvider=NULL;
static jmethodID javakmyEvent=NULL;

JNIEXPORT jint JNICALL aexddKMY350_JNI_OnLoad(JavaVM *vm, void *reserved)
{
     return JNI_VERSION_1_4; /* the required JNI version */
}

JNIEXPORT void JNICALL aexddKMY350_JNI_OnUnload(JavaVM* vm, void* reserved)
{
}

static jclass getProvider(JNIEnv* env)
{
	return (*env)->FindClass(env,"com/androidex/devices/aexddKMY350");
}

static jmethodID getMethod(JNIEnv* env,char *func,char *result)
{
	if(openkkProvider==NULL)
		openkkProvider = getProvider(env);
	if(openkkProvider)
	{
		__android_log_print(ANDROID_LOG_DEBUG,"kmy","GetMethodId");
		return (*env)->GetMethodID(env, openkkProvider, func,result);
	}else{
		__android_log_print(ANDROID_LOG_DEBUG,"kmy","mt318Provider is NULL");
		return NULL;
	}
}

/**
 * 调用了Java对应密码键盘处理方法的函数，此函数会用于密码键盘的事件处理
 */
static int jni_kmy_event(HKMY env,HKMY obj,int code,char *msg)
{
	JNIEnv* jniEnv = (JNIEnv*)env;
	jobject javaObject = (jobject)obj;

    if(jniEnv == NULL && javaObject == NULL) {
        return 0;
    }
	if(javakmyEvent==NULL){
		javakmyEvent = getMethod(env,"onBackCallEvent","(ILjava/lang/String;)V");
	}
	if(javakmyEvent){
		jstring strmsg = (*jniEnv)->NewStringUTF(jniEnv, (const char *)msg);
		(*jniEnv)->CallVoidMethod(jniEnv, javaObject, javakmyEvent,(jint)code,strmsg);
		return TRUE;
	}else{
		return FALSE;
	}
	return TRUE;
}

JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddKMY350_kmyReadKeyLoop
        (JNIEnv *env, jobject this, jint fd,jint timeout)
{
    kmy_set_event(jni_kmy_event);
    return kmy_read_key_loop(env,this,fd,timeout);
}

JNIEXPORT void JNICALL Java_com_androidex_devices_aexddKMY350_kmySendCmd
        (JNIEnv *env, jobject this, jint fd, jstring cmd, jint size)
{
    char *strCmd = (char *) (*env)->GetStringUTFChars(env,cmd, 0);

    kmy_set_event(jni_kmy_event);
    kmy_send_cmd(env,this,fd,strCmd,size);
    (*env)->ReleaseStringUTFChars(env, cmd, strCmd);
}

JNIEXPORT void JNICALL Java_com_androidex_devices_aexddKMY350_kmySendHexCmd
        (JNIEnv *env, jobject this, jint fd, jstring hexcmd, jint size)
{
    char *strCmd = (char *) (*env)->GetStringUTFChars(env,hexcmd, 0);

    kmy_set_event(jni_kmy_event);
    kmy_send_hexcmd(env,this,fd,strCmd,size);
    (*env)->ReleaseStringUTFChars(env, hexcmd, strCmd);
}

JNIEXPORT jbyteArray JNICALL Java_com_androidex_devices_aexddKMY350_kmyReadPacket
        (JNIEnv *env, jobject this, jint fd,jint timeout)
{
    char buf[255];

    kmy_set_event(jni_kmy_event);
    int ret = kmy_recive_packet(env,this,fd,buf,sizeof(buf),timeout);
    if(ret > 0){
        jbyteArray  r = (*env)->NewByteArray(env,ret);
        (*env)->SetByteArrayRegion(env,r, 0, ret, buf);
        return r;
    }else{
        return NULL;
    }
}