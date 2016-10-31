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
/**
 * 密码键盘的全局句柄，在未调用kmy_open的命令前，此变量为NULL。
 * 在调用kmy_close后恢复为NULL
 */
KMY_HANDLE s_kmy = NULL;
jclass openkkProvider=NULL;
jmethodID javakmyEvent=NULL;

void SetKmyHandle(KMY_HANDLE kmy)
{
	s_kmy = kmy;
}

JNIEXPORT jint JNICALL aexddKMY350_JNI_OnLoad(JavaVM *vm, void *reserved)
{
     return JNI_VERSION_1_4; /* the required JNI version */
}

JNIEXPORT void JNICALL aexddKMY350_JNI_OnUnload(JavaVM* vm, void* reserved)
{
	if(s_kmy){
		kmy_close(s_kmy,NULL,NULL);
		s_kmy = NULL;
	}
}

static jclass getProvider(JNIEnv* env)
{
	return (*env)->FindClass(env,"com/eztor/plugins/kkkmy");
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
		__android_log_print(ANDROID_LOG_DEBUG,"kmy","openkkProvider is NULL");
		return NULL;
	}
}

/**
 * 将char*字符串转换为Jstring。
 */
jstring stringToJstring(JNIEnv* env, const char* pat)
{
	jclass strClass = (*env)->FindClass(env,"Ljava/lang/String;");
	if(!strClass){
		return (jstring)(*env)->NewString(env,(jchar*)"没有发现Class",strlen("没有发现Class"));
	}
	jmethodID ctorID = (*env)->GetMethodID(env,strClass, "<init>", "([BLjava/lang/String;)V");
	jbyteArray bytes = (*env)->NewByteArray(env,strlen(pat));
	(*env)->SetByteArrayRegion(env,bytes, 0, strlen(pat), (jbyte*)pat);
	jstring encoding = (*env)->NewStringUTF(env,"utf-8");
	return (jstring)(*env)->NewObject(env,strClass, ctorID, bytes, encoding);
}

/**
 * jstring转换为char*的函数，切记使用后需要释放char*的缓冲区。
 */
char* jstringTostring(JNIEnv* env, jstring jstr)
{
	char* rtn = NULL;
	jclass clsstring = (*env)->FindClass(env,"java/lang/String");
	jstring strencode = (*env)->NewStringUTF(env,"utf-8");
	jmethodID mid = (*env)->GetMethodID(env,clsstring, "getBytes", "(Ljava/lang/String;)[B");
	jbyteArray barr= (jbyteArray)(*env)->CallObjectMethod(env,jstr, mid, strencode);
	jsize alen = (*env)->GetArrayLength(env,barr);
	jbyte* ba = (*env)->GetByteArrayElements(env,barr, JNI_FALSE);
	if (alen > 0)
	{
		rtn = (char*)malloc(alen + 1);

		memcpy(rtn, ba, alen);
		rtn[alen] = 0;
	}
	(*env)->ReleaseByteArrayElements(env,barr, ba, 0);
	return rtn;
}
/**
 * 调用了Java对应密码键盘处理方法的函数，此函数会用于密码键盘的事件处理
 */
static int jni_kmy_event(KMY_HANDLE kmy,HKMY env,HKMY obj,int code,char *msg)
{
	//__android_log_print(ANDROID_LOG_DEBUG,"kmy","javakmyEvent %s",msg);
	JNIEnv* jniEnv = (JNIEnv*)env;
	jobject javaObject = (jobject)obj;

    if(jniEnv == NULL && javaObject == NULL) {
        return 0;
    }

	if(javakmyEvent==NULL){
		//__android_log_print(ANDROID_LOG_DEBUG,"kmy","getMethod");
		javakmyEvent = getMethod(env,"OnKmyKeypadEvent","(ILjava/lang/String;)V");
	}
	if(javakmyEvent){
		//__android_log_print(ANDROID_LOG_DEBUG,"kmy","NewStringUTF");
		jstring strmsg = (*jniEnv)->NewStringUTF(jniEnv, (const char *)msg);
		//__android_log_print(ANDROID_LOG_DEBUG,"kmy","CallVoidMethod");
		(*jniEnv)->CallVoidMethod(jniEnv, javaObject, javakmyEvent,(jint)code,strmsg);
		//__android_log_print(ANDROID_LOG_DEBUG,"kmy","CallVoidMethod end.");
		return TRUE;
	}else{
		//__android_log_print(ANDROID_LOG_DEBUG,"kmy","javakmyEvent is NULL");
		return FALSE;
	}
	return TRUE;
}

/*
 * Class:     com_eztor_openkk_jnikmykeyboard
 * Method:    kmyOpen
 * Signature: ([C)I
 */
JNIEXPORT jstring JNICALL Java_com_androidex_devices_aexddKMY350_kmyOpen
  (JNIEnv *env, jobject this, jstring arg)
{
	char r[512];
	kmy_set_event(jni_kmy_event);
	char  *charg =(char *) (*env)->GetStringUTFChars(env, arg, 0);
	if(s_kmy){
		kmy_close(s_kmy,env,this);
		s_kmy = NULL;
	}
	s_kmy = kmy_open(env,this,charg);
	(*env)->ReleaseStringUTFChars(env, arg, charg);
	if(s_kmy){
		sprintf(r,"{success:true,fd:%d,Version:\"%s\",Serial:\"%s\",port:\"%s\"}",s_kmy->fd,s_kmy->version,s_kmy->sn,s_kmy->port);
	}else{
		sprintf(r,"{success:false}");
	}
	return (*env)->NewStringUTF(env,(const char*)r);
}

/*
 * Class:     com_eztor_openkk_jnikmykeyboard
 * Method:    kmyClose
 * Signature: ()I
 */
JNIEXPORT void JNICALL Java_com_androidex_devices_aexddKMY350_kmyClose
  (JNIEnv *env, jobject this)
{
	kmy_close(s_kmy,env,this);
	s_kmy = NULL;
	return ;
}

/*
 * Class:     com_eztor_openkk_jnikmykeyboard
 * Method:    kmyReset
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddKMY350_kmyReset
  (JNIEnv *env, jobject this, jint timeout)
{
	if(s_kmy && s_kmy->fd > 0){
		return kmy_reset(s_kmy,env,this,timeout);
	}else{
		return FALSE;
	}
}

/*
 * Class:     com_eztor_openkk_jnikmykeyboard
 * Method:    kmyResetWithPpin
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddKMY350_kmyResetWithPpin
  (JNIEnv *env, jobject this, jint timeout)
{
	if(s_kmy && s_kmy->fd > 0){
		return kmy_reset_with_pin(s_kmy,env,this,timeout);
	}else{
		return FALSE;
	}
}

/*
 * Class:     com_eztor_openkk_jnikmykeyboard
 * Method:    kmyGetSn
 * Signature: ([CI)I
 */
JNIEXPORT jstring JNICALL Java_com_androidex_devices_aexddKMY350_kmyGetSn
  (JNIEnv *env, jobject this, jint timeout)
{
	char sn[256];
	jstring rsn = "";

	if(s_kmy && s_kmy->fd > 0){
		memset(sn,0,256);
		if(!kmy_get_sn(s_kmy,env,this,sn,timeout)){
			__android_log_print(ANDROID_LOG_DEBUG,"kmy","sn=%s",sn);
			rsn = (*env)->NewStringUTF(env,"");
		}else{
			__android_log_print(ANDROID_LOG_DEBUG,"kmy","sn=%s",sn);
			rsn = (*env)->NewStringUTF(env,(const char*)sn);
		}
		__android_log_print(ANDROID_LOG_DEBUG,"kmy","getSn(%d)=%s",timeout,sn);
	}
	return rsn;
}

JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddKMY350_kmySetSn
  (JNIEnv *env, jobject this, jstring sn,jint timeout)
{
	char  *chsn =(char *) (*env)->GetStringUTFChars(env, sn, 0);

	if(s_kmy && s_kmy->fd > 0){
		return kmy_set_sn(s_kmy,env,this,chsn,timeout);
	}
	return 0;
}

/*
 * Class:     com_eztor_openkk_jnikmykeyboard
 * Method:    kmyGetVersion
 * Signature: ([CI)I
 */
JNIEXPORT jstring JNICALL Java_com_androidex_devices_aexddKMY350_kmyGetVersion
  (JNIEnv *env, jobject this, jint timeout)
{
	char v[256];
	if(!kmy_get_version(s_kmy,env,this,v,timeout)){
		strcpy(v,"");
	}
	return (*env)->NewStringUTF(env, (const char*)v);
}

/*
 * Class:     com_eztor_openkk_jnikmykeyboard
 * Method:    kmySetEncryptMode
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddKMY350_kmySetEncryptMode
  (JNIEnv *env, jobject this, jint ewm, jint timeout)
{
	if(!(s_kmy && s_kmy->fd > 0)){
		return FALSE;
	}
	return kmy_set_encrypt_mode(s_kmy,env,this,ewm,timeout);
}

/*
 * Class:     com_eztor_openkk_jnikmykeyboard
 * Method:    kmyDlMasterKey
 * Signature: (I[CI)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddKMY350_kmyDlMasterKey
  (JNIEnv *env, jobject this, jint mk_no, jstring mk, jint timeout)
{

	if(!(s_kmy && s_kmy->fd > 0)){
		return FALSE;
	}else{
		char *chmk = (char *)(*env)->GetStringUTFChars(env, mk, 0);
		int r= kmy_dl_master_key(s_kmy,env,this,mk_no,chmk,timeout);
		(*env)->ReleaseStringUTFChars(env, mk, chmk);
		return r;
	}
}

/*
 * Class:     com_eztor_openkk_jnikmykeyboard
 * Method:    kmyDlWorkKey
 * Signature: (II[CI)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddKMY350_kmyDlWorkKey
  (JNIEnv *env, jobject this, jint mkno, jint wkno, jstring wk, jint timeout)
{
	if(!(s_kmy && s_kmy->fd > 0)){
			return FALSE;
	}else{
		char *chwk =(char *) (*env)->GetStringUTFChars(env, wk, 0);
		int r = kmy_dl_work_key(s_kmy,env,this,mkno,wkno,chwk,timeout);
		(*env)->ReleaseStringUTFChars(env, wk, chwk);
		return r;
	}
}

/*
 * Class:     com_eztor_openkk_jnikmykeyboard
 * Method:    kmyActiveWorkKey
 * Signature: (III)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddKMY350_kmyActiveWorkKey
  (JNIEnv *env, jobject this, jint mkno, jint wkno, jint timeout)
{
	if(!(s_kmy && s_kmy->fd > 0)){
		return FALSE;
	}
	return kmy_active_work_key(s_kmy,env,this,mkno,wkno,timeout);
}

/*
 * Class:     com_eztor_openkk_jnikmykeyboard
 * Method:    kmyOpenKeypad
 * Signature: (II)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddKMY350_kmyOpenKeypad
  (JNIEnv *env, jobject this, jint ctl, jint timeout)
{
	if(!(s_kmy && s_kmy->fd > 0)){
		return FALSE;
	}
	return kmy_open_keypad(s_kmy,env,this,ctl,timeout);
}

/*
 * Class:     com_eztor_openkk_jnikmykeyboard
 * Method:    kmyDlCardNo
 * Signature: ([CI)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddKMY350_kmyDlCardNo
  (JNIEnv *env, jobject this, jstring card, jint timeout)
{
	if(!(s_kmy && s_kmy->fd > 0)){
		return FALSE;
	}else{
		char  *chcard =(char *) (*env)->GetStringUTFChars(env, card, 0);
		int r = kmy_dl_card_no(s_kmy,env,this,chcard,timeout);
		(*env)->ReleaseStringUTFChars(env, card, chcard);
		return r;
	}
}

/*
 * Class:     com_eztor_openkk_jnikmykeyboard
 * Method:    kmyStartPin
 * Signature: (SSSSSI)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddKMY350_kmyStartPin
  (JNIEnv *env, jobject this, jshort pl, jshort dm, jshort am, jshort pm, jshort t, jint timeout)
{
	if(!(s_kmy && s_kmy->fd > 0)){
		return FALSE;
	}else{
		return kmy_start_pin(s_kmy,env,this,pl,dm,am,pm,t,timeout);
	}
}

/*
 * Class:     com_eztor_openkk_jnikmykeyboard
 * Method:    kmyPinBlock
 * Signature: ([CI)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddKMY350_kmyPinBlock
  (JNIEnv *env, jobject this, jstring pchCardNo, jint timeout)
{
	if(!(s_kmy && s_kmy->fd > 0)){
		return FALSE;
	}else{
		char *chCardNo =(char *) (*env)->GetStringUTFChars(env, pchCardNo, 0);
		int r = kmy_pin_block(s_kmy,env,this,chCardNo,timeout);
		(*env)->ReleaseStringUTFChars(env, pchCardNo, chCardNo);
		return r;
	}
}

/*
 * Class:     com_eztor_openkk_jnikmykeyboard
 * Method:    kmyReadPin
 * Signature: ([CI)I
 */
JNIEXPORT jstring JNICALL Java_com_androidex_devices_aexddKMY350_kmyReadPin
  (JNIEnv *env, jobject this, jint t)
{
	char pin[256] = "",hexpin[256] = "";
	if(s_kmy && s_kmy->fd > 0){
		if(!kmy_read_pin(s_kmy,env,this,pin,hexpin,t)){
			strcpy(pin,"");
			strcpy(hexpin,"");
		}
	}
	return (*env)->NewStringUTF(env, (const char*)hexpin);
}

/*
 * Class:     com_eztor_openkk_jnikmykeyboard
 * Method:    kmyEncrypt
 * Signature: ([C[CI)I
 */
JNIEXPORT jstring JNICALL Java_com_androidex_devices_aexddKMY350_kmyEncrypt
  (JNIEnv *env, jobject this, jstring in, jint t)
{
	char out[256] = "";
	if(s_kmy && s_kmy->fd > 0){
		char  *chin =(char *) (*env)->GetStringUTFChars(env, in, 0);
		int r = kmy_encrypt(s_kmy,env,this,chin,out,NULL,t);
		if(!r){
			strcpy(out,"");
		}
		(*env)->ReleaseStringUTFChars(env, in, chin);
	}
	return (*env)->NewStringUTF(env, (const char*)out);
}

/*
 * Class:     com_eztor_openkk_jnikmykeyboard
 * Method:    kmyDecrypt
 * Signature: ([C[CI)I
 */
JNIEXPORT jstring JNICALL Java_com_androidex_devices_aexddKMY350_kmyDecrypt
  (JNIEnv *env, jobject this, jstring in, jint t)
{
	char out[256] = "";
	if(s_kmy && s_kmy->fd > 0){
		char  *chin =(char *) (*env)->GetStringUTFChars(env, in, 0);
		int r = kmy_decrypt(s_kmy,env,this,chin,out,t);
		if(!r){
			strcpy(out,"");
		}
		(*env)->ReleaseStringUTFChars(env, in, chin);
	}
	return (*env)->NewStringUTF(env, (const char*)out);
}

/*
 * Class:     com_eztor_openkk_jnikmykeyboard
 * Method:    kmyCalcMacData
 * Signature: ([C[CI)I
 */
JNIEXPORT jstring JNICALL Java_com_androidex_devices_aexddKMY350_kmyCalcMacData
  (JNIEnv *env, jobject this, jstring in, jint t)
{
	char out[256] = "",hexOut[256] = "";
	if(s_kmy && s_kmy->fd > 0){
		char  *chin =(char *) (*env)->GetStringUTFChars(env, in, 0);
		int r = kmy_calc_mac_data(s_kmy,env,this,chin,out,hexOut,t);
		if(!r){
			strcpy(hexOut,"");
		}
		(*env)->ReleaseStringUTFChars(env, in, chin);
	}
	return (*env)->NewStringUTF(env, (const char*)hexOut);
}

JNIEXPORT void JNICALL Java_com_androidex_devices_aexddKMY350_kmyStartReadKey
  (JNIEnv *env, jobject this, jstring cb, jint timeout)
{
	if(s_kmy && s_kmy->fd > 0){
		char  *chcb =(char *) (*env)->GetStringUTFChars(env, cb, 0);
		kmy_start_read_key(s_kmy,env,this,chcb,timeout);
		(*env)->ReleaseStringUTFChars(env, cb, chcb);
	}
}

/*
 * Class:     com_androidex_devices_aexddKMY350
 * Method:    kmyStartAllStep
 * Signature: (IILjava/lang/String;Ljava/lang/String;Ljava/lang/String;I)V
 */
JNIEXPORT jstring JNICALL Java_com_androidex_devices_aexddKMY350_kmyStartAllStep
  (JNIEnv *env, jobject this, jint mKeyNo, jint wKeyNo, jstring wKey, jstring cardNo, jstring callback, jstring port,jint timeout)
{
	int r=0;
	char passHex[17] = "";
	if(s_kmy && s_kmy->fd > 0){
		memset(passHex,0,sizeof(passHex));
		char  *chwKey =(char *) (*env)->GetStringUTFChars(env, wKey, 0);
		char  *chcardNo =(char *) (*env)->GetStringUTFChars(env, cardNo, 0);
		char  *chcb =(char *) (*env)->GetStringUTFChars(env, callback, 0);
		char  *chport =(char *) (*env)->GetStringUTFChars(env, port, 0);
		r = kmy_start_all_step(s_kmy,env,this,mKeyNo,wKeyNo,chwKey,chcardNo,chcb,passHex,chport,timeout);
		(*env)->ReleaseStringUTFChars(env, wKey, chwKey);
		(*env)->ReleaseStringUTFChars(env, cardNo, chcardNo);
		(*env)->ReleaseStringUTFChars(env, callback, chcb);
		(*env)->ReleaseStringUTFChars(env, port, chport);
		if(!r)
			strcpy(passHex,"");
	}
	return (*env)->NewStringUTF(env, (const char*)passHex);
}
