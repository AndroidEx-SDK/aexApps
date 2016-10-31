#include <jni.h>
#include     <ctype.h>
#include     <string.h>
#include     <stdlib.h>
#include     <locale.h>
#include     <wchar.h>
#include "com_androidex_devices_aexddMT318.h"
#include "aexddMT318.h"
#include <android/log.h>

#define FALSE 0
#define TRUE  1
/**
 * 密码键盘的全局句柄，在未调用kmy_open的命令前，此变量为NULL。
 * 在调用kmy_close后恢复为NULL
 */
static KKCARD_HANDLE s_kkc = NULL;
static jclass openkkProvider=NULL;
static jmethodID javakkcEvent=NULL;

JNIEXPORT jint JNICALL aexddMT318_JNI_OnLoad(JavaVM* vm, void* reserved)
{
	return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL aexddMT318_JNI_OnUnload(JavaVM* vm, void* reserved)
{
//	jint res;
//	JNIEnv* env;
//
//	res = (*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_4);
	if(s_kkc){
		kkcard_close(s_kkc,NULL,NULL);
		s_kkc = NULL;
	}
}

static jclass getProvider(JNIEnv* env)
{
	return (*env)->FindClass(env,"com/androidex/devices/aexddMT318Reader");
}

static jmethodID getMethod(JNIEnv* env,char *func,char *result)
{
	if(openkkProvider==NULL)
		openkkProvider = getProvider(env);
	if(openkkProvider)
	{
		return (*env)->GetMethodID(env, openkkProvider, func,result);
	}else{
		return NULL;
	}
}

/**
 * 调用了Java对应IC卡和银行卡处理方法的函数，此函数会用于密码键盘的事件处理
 */
static int jni_kkcard_event(KKCARD_HANDLE kkc,JNIEnv* env,jobject obj,int code,char *msg)
{
	JNIEnv* jniEnv = (JNIEnv*)env;
	jobject javaObject = (jobject)obj;

    if(jniEnv == NULL && javaObject == NULL) {
        return 0;
    }

	if(javakkcEvent==NULL){
		javakkcEvent = getMethod(env,"OnJniEvent","(ILjava/lang/String;)V");
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

/*
 * Class:     com_androidex_devices_aexddMT318
 * Method:    Open
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddMT318Reader_native_1Open
  (JNIEnv *env, jobject this, jstring arg)
{
	kkcard_set_event(jni_kkcard_event);
	char  *charg =(char *) (*env)->GetStringUTFChars(env, arg, 0);
	if(s_kkc){
		kkcard_close(s_kkc,env,this);
		s_kkc = NULL;
	}
	s_kkc = kkcard_open(env,this,charg);
	(*env)->ReleaseStringUTFChars(env, arg, charg);
	if(s_kkc){
		return TRUE;
	}else{
		return FALSE;
	}
}

/*
 * Class:     com_androidex_devices_aexddMT318
 * Method:    Close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_androidex_devices_aexddMT318Reader_native_1Close
	(JNIEnv *env, jobject this)
{
	kkcard_close(s_kkc,env,this);
	s_kkc = NULL;
	return ;
}

/*
 * Class:     com_androidex_devices_aexddMT318
 * Method:    Reset
 * Signature: (I)I
 */
JNIEXPORT jstring JNICALL Java_com_androidex_devices_aexddMT318Reader_Reset
	(JNIEnv *env, jobject this, jint timeout)
{
	char v[256]="";
	if(s_kkc && s_kkc->fd > 0){
		if(!kkcard_reset(s_kkc,env,this,v,timeout)){
			strcpy(v,"");
		}
	}
	return (*env)->NewStringUTF(env, (const char*)v);
}

/*
 * Class:     com_androidex_devices_aexddMT318
 * Method:    ReadCard
 * Signature: (Ljava/lang/String;I)V
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddMT318Reader_ReadCard
	(JNIEnv *env, jobject this, jstring cb, jint timeout)
{
	int r = 0;
	if(s_kkc && s_kkc->fd > 0){
		char  *chcb =(char *) (*env)->GetStringUTFChars(env, cb, 0);
		r = kkcard_read_card(s_kkc,env,this,chcb,timeout);
		(*env)->ReleaseStringUTFChars(env, cb, chcb);
	}
	return r;
}

/*
 * Class:     com_androidex_devices_aexddMT318
 * Method:    RFM_13_Ring
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddMT318Reader_RFM_113_1Ring
	(JNIEnv *env, jobject this, jint timeout)
{
	int r = 0;
	if(s_kkc && s_kkc->fd > 0){
		r = kkcard_ring(s_kkc,env,this,timeout);
	}
	return r;
}

/*
 * Class:     com_androidex_devices_aexddMT318
 * Method:    RFM_13_ReadGuid
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddMT318Reader_RFM_113_1ReadGuid
	(JNIEnv *env, jobject this, jint timeout)
{
	int r = 0;
	if(s_kkc && s_kkc->fd > 0){
		r = kkcard_read_rfm13_id(s_kkc,env,this,timeout);
	}
	return r;
}

/*
 * Class:     com_androidex_devices_aexddMT318
 * Method:    MF_30_ReadGuid
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddMT318Reader_MF_130_1ReadGuid
    (JNIEnv *env, jobject this, jint timeout)
{
	int r = 0;
	if(s_kkc && s_kkc->fd > 0){
		r = kkcard_read_mf30_id(s_kkc,env,this,timeout);
	}
	return r;
}

/*
 * Class:     com_androidex_devices_aexddMT318
 * Method:    RFM_13_ReadCard
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddMT318Reader_RFM_113_1ReadCard
	(JNIEnv *env, jobject this, jint sectorid, jint blockid,jint timeout)
{
	int r = 0;
	if(s_kkc && s_kkc->fd > 0){
		r = kkcard_read_rfm13_card(s_kkc,env,this,sectorid,blockid,timeout);
	}
	return r;
}

/*
 * Class:     com_androidex_devices_aexddMT318
 * Method:    RFM_13_WriteCard
 * Signature: (I[BI[BI[BI)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddMT318Reader_RFM_113_1WriteCard
	(JNIEnv *env, jobject this, jint sectorid, jbyteArray data0, jint len0, jbyteArray data1, jint len1,jbyteArray data2, jint len2)
{
	int r = 0;
	if(s_kkc && s_kkc->fd > 0){
		jbyte * arraydata0 = (*env)->GetByteArrayElements(env,data0,0);
		jbyte * arraydata1 = (*env)->GetByteArrayElements(env,data1,0);
		jbyte * arraydata2 = (*env)->GetByteArrayElements(env,data2,0);
		char* szdata0 = (char*)arraydata0;
		char* szdata1 = (char*)arraydata1;
		char* szdata2 = (char*)arraydata2;
		r = kkcard_write_rfm13_card(s_kkc,env,this,sectorid,szdata0,len0,szdata1,len1,szdata2,len2);
		(*env)->ReleaseByteArrayElements(env,data0, arraydata0,0);
		(*env)->ReleaseByteArrayElements(env,data1, arraydata1,0);
		(*env)->ReleaseByteArrayElements(env,data2, arraydata2,0);
	}
	return r;
}

/*
 * Class:     com_androidex_devices_aexddMT318
 * Method:    MF_30_ReadCard
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddMT318Reader_MF_130_1ReadCard
	(JNIEnv *env, jobject this, jint sectorid, jint blockid, jint timeout)
{
	int r = 0;
	if(s_kkc && s_kkc->fd > 0){
		r = kkcard_read_mf30_card(s_kkc,env,this,sectorid,blockid,timeout);
	}
	return r;
}

/*
 * Class:     com_androidex_devices_aexddMT318
 * Method:    MF_30_ReadCardbyPwd
 * Signature: (II[BI)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddMT318Reader_MF_130_1ReadCardbyPwd
	(JNIEnv *env, jobject this, jint sectorid, jint blockid, jbyteArray passwd, jint pwdlen, jint timeout)
{
	int r = 0;
	if(s_kkc && s_kkc->fd > 0){
		jbyte * arraydata0 = (*env)->GetByteArrayElements(env,passwd,0);
		char* szdata0 = (char*)arraydata0;
		r = kkcard_read_mf30_card_bypwd(s_kkc,env,this,sectorid,blockid,szdata0,pwdlen,timeout);
		(*env)->ReleaseByteArrayElements(env,passwd, arraydata0,0);
	}
	return r;
}

/*
 * Class:     com_androidex_devices_aexddMT318
 * Method:    MF_30_GetVer
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddMT318Reader_MF_130_1GetVer
	(JNIEnv *env, jobject this,  jint timeout)
{
	int r = 0;
	if(s_kkc && s_kkc->fd > 0){
		r = kkcard_getver_mf30_card(s_kkc,env,this,timeout);
	}
	return r;
}

/*
 * Class:     com_androidex_devices_aexddMT318
 * Method:    MF_30_WriteCard
 * Signature: (I[BI[BI[BI)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddMT318Reader_MF_130_1WriteCard
	(JNIEnv *env, jobject this, jint sectorid, jint blockid, jbyteArray data0, jint len0)
{
	int r = 0;
	if(s_kkc && s_kkc->fd > 0){
		jbyte * arraydata0 = (*env)->GetByteArrayElements(env,data0,0);
		char* szdata0 = (char*)arraydata0;
		r = kkcard_write_mf30_card(s_kkc,env,this,sectorid,blockid,szdata0,len0);
		(*env)->ReleaseByteArrayElements(env,data0, arraydata0,0);
	}
	return r;
}

/*
 * Class:     com_androidex_devices_aexddMT318
 * Method:    CPU_Reset
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddMT318Reader_CPU_1Reset
  (JNIEnv *env, jobject this, jint timeout)
{
	int r = 0;
	if(s_kkc && s_kkc->fd > 0){
		r = cpucard_reset(s_kkc,env,this,timeout);
	}
	return r;
}

/*
 * Class:     com_androidex_devices_aexddMT318
 * Method:    CPU_PowerOn
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddMT318Reader_CPU_1PowerOn
  (JNIEnv *env, jobject this, jint timeout)
{
	int r = 0;
	if(s_kkc && s_kkc->fd > 0){
		r = cpucard_poweron(s_kkc,env,this,timeout);
	}
	return r;
}

/*
 * Class:     com_androidex_devices_aexddMT318
 * Method:    CPU_PowerOff
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddMT318Reader_CPU_1PowerOff
  (JNIEnv *env, jobject this, jint timeout)
{
	int r = 0;
	if(s_kkc && s_kkc->fd > 0){
		r = cpucard_poweroff(s_kkc,env,this,timeout);
	}
	return r;
}

/*
 * Class:     com_androidex_devices_aexddMT318
 * Method:    CPU_Apdu
 * Signature: ([CI[C[II)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddMT318Reader_CPU_1Apdu
  (JNIEnv *env, jobject this, jbyteArray data, jint len, jint timeout)
{
	int r = 0;

	jbyteArray * arrayBody = (*env)->GetByteArrayElements(env,data,0);
	char* szdata = (char*)arrayBody;
	if(s_kkc && s_kkc->fd > 0){
		r = cpucard_apdu(s_kkc,env,this,szdata,len,timeout);
	}

	(*env)->ReleaseByteArrayElements(env,data, arrayBody,0);
	return r;
}


// 标准串口操作

/*
 * Class:     com_androidex_devices_aexddMT318
 * Method:    serial_recive
 * Signature: (II)Ljava/lang/String;
 */
JNIEXPORT jbyteArray JNICALL Java_com_androidex_devices_aexddMT318Reader_serial_1read
  (JNIEnv *env, jobject this,jint timeout)
{
	char buf[2048];
	memset(buf,0,sizeof(buf));
	int len = serial_read(s_kkc,env,this,buf,timeout);
	if(len>0){
		jbyte* byte = (jbyte*)buf;
		jbyteArray jarray = (*env)->NewByteArray(env,len);
		(*env)->SetByteArrayRegion(env,jarray, 0, len, byte);
		return jarray;
	}

	return NULL;
}

/*
 * Class:     com_androidex_devices_aexddMT318
 * Method:    serial_write
 * Signature: (ILjava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddMT318Reader_serial_1write
  (JNIEnv *env, jobject this, jbyteArray data,jint len)
{
	jbyte * arrayBody = (*env)->GetByteArrayElements(env,data,0);
	char* szdata = (char*)arrayBody;
	int r = serial_write(s_kkc,env,this,szdata,len);
	(*env)->ReleaseByteArrayElements(env,data, arrayBody,0);
	return r;
}

/*
 * Class:     com_androidex_devices_aexddMT318
 * Method:    serial_select
 * Signature: (III)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddMT318Reader_serial_1select
  (JNIEnv *env, jobject this, jint usec)
{
	int r = serial_select(s_kkc,env,this,0,usec);
	return r;
}
