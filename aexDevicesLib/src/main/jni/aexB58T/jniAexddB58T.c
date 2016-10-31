#include <jni.h>
#include <stdio.h>      /*标准输入输出定义*/
#include <stdlib.h>
#include <android/log.h>
#include "com_androidex_devices_aexddB58T.h"
#include "aexddB58T.h"
#include "../include/utils.h"


static PRINT_HANDLE s_print = NULL;

/*提供给回调使用*/
static jclass printProvider=NULL;
static jmethodID javaprintEvent=NULL;

JNIEXPORT jint JNICALL aexddB58T_JNI_OnLoad(JavaVM* vm, void* reserved)
{
	return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL aexddB58T_JNI_OnUnload(JavaVM* vm, void* reserved)
{
	if(s_print){
		aexddB58T_close(s_print,NULL,NULL);
        s_print = NULL;
	}
}

/*
 *This function loads a locally-defined class.
 *这个函数加载一个本地定义的类
 * */
static jclass getProvider(JNIEnv *env)
{
	return (*env)->FindClass(env,"com/androidex/devices/aexddB58Printer");
}


/*
 *Returns the method ID for an instance (nonstatic) method of a class or interface.

 *返回类或接口实例（非静态）方法的方法 ID。方法可在某个 clazz 的超类中定义，也可从 clazz 继承。该方法由其名称和签名决定。

 *GetMethodID() 可使未初始化的类初始化。
 * */
static jmethodID getMethod(JNIEnv *env, char *func,char *result)
{
	if(printProvider==NULL)
		printProvider = getProvider(env);
	if(printProvider)
	{
		return (*env)->GetMethodID(env, printProvider, func,result);
	}
    return NULL;
}

/**
 * 调用了Java对应打印机处理方法的函数，此函数会用于打印机的事件处理
 */
static int jni_print_event(PRINT_HANDLE print,JNIEnv *env, jobject obj,int code,char *msg)
{
	JNIEnv* jniEnv = (JNIEnv*)env;
	jobject javaObject = (jobject)obj;

    if(jniEnv == NULL && javaObject == NULL) {
        return 0;
    }

	if(javaprintEvent==NULL){
		javaprintEvent = getMethod(env,"onBackCallEvent","(ILjava/lang/String;)V");
	}
	if(printProvider && javaprintEvent){
		jstring strmsg = (*jniEnv)->NewStringUTF(jniEnv, (const char *)msg);
		/*调用一个由methodID定义的实例的Java方法，可选择传递参数（args）的数组到这个方法。*/
		(*jniEnv)->CallVoidMethod(jniEnv, javaObject, javaprintEvent,(jint)code,strmsg);
		return 0;
	}
	else
		return -1;
	return 0;
}

JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_native_1open
(JNIEnv *env, jobject this, jstring strarg)
{

	char *charg = (char *)(*env)->GetStringUTFChars(env, strarg, 0);
	if(!s_print){
		s_print = aexddB58T_open(env,this,charg);
	}

	(*env)->ReleaseStringUTFChars(env, strarg, charg);
	if(s_print){
		aexddB58T_set_event(jni_print_event);
		return TRUE;
	}else{
		return FALSE;
	}
}

JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_native_1close
  (JNIEnv *env, jobject this)
{
	int iret=aexddB58T_close(s_print,env,this);
	s_print=NULL;
	return iret;
}

JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_initialize
(JNIEnv *env, jobject this)
{
	int iret = aexddB58T_initialize(s_print,env,this);
    return iret;
}

JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_getstatus
  (JNIEnv *env, jobject this, jint n)
{
	int iret=aexddB58T_getstatus(s_print,env,this ,n);
	return iret;
}


JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_getfactory
(JNIEnv *env, jobject this, jint n)
{
	int iret=aexddB58T_getfactory(s_print,env,this ,n);
	return iret;
}


JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_newline
  (JNIEnv *env, jobject this, jint n)
{
	return aexddB58T_newline(s_print,env,this,n);
}


//JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_steppoint
//  (JNIEnv *env, jobject this, jint iflag, jint n)
//{
//	return aexddB58T_steppoint(s_print,env ,this ,iflag ,n);
//}


JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_stepline
(JNIEnv *env, jobject this,jbyteArray xcode,jint istep)
{
	char *pcode = (char *)(*env)->GetByteArrayElements(env, xcode, 0);
	int iret = aexddB58T_stepline(s_print,env,this,pcode,istep);
	(*env)->ReleaseByteArrayElements(env,xcode, pcode,0);
	return iret;
}

JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_cut
  (JNIEnv *env, jobject this,jbyteArray xcode,jint iflag)
{
	char* chch = (char*) (*env)->GetByteArrayElements(env, xcode, 0);
	int iret=aexddB58T_cut(s_print,env,this,chch,iflag);
	(*env)->ReleaseByteArrayElements(env,xcode, chch,0);
    return iret;
}


JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_set_1align
  (JNIEnv *env, jobject this, jint iflag)
{
	return aexddB58T_set_align(s_print,env ,this ,iflag);
}


JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_set_1fontsize
  (JNIEnv *env, jobject this, jint isize)
{
	return aexddB58T_set_fontsize(s_print,env ,this ,isize);
}

JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_set_1linewide
  (JNIEnv *env, jobject this, jint isize)
{
	return aexddB58T_set_linewide(s_print,env,this ,isize);
}


JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_set_1charwide
(JNIEnv *env, jobject this)
{
	return aexddB58T_set_charwide(s_print,env,this);
}

JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_set_1charDSize
(JNIEnv *env, jobject this)
{
	return aexddB58T_set_charDSize(s_print,env,this);
}

JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_set_1charNSize
(JNIEnv *env, jobject this)
{
	return aexddB58T_set_charNSize(s_print,env,this);
}

JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_print_1ch
  (JNIEnv *env, jobject this, jbyteArray bytech,int length)
{
	char* chch = (char*) (*env)->GetByteArrayElements(env, bytech, 0);
	int iret=aexddB58T_print_ch(s_print,env,this,chch,length);
	(*env)->ReleaseByteArrayElements(env,bytech, chch,0);
	return iret;
}



JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_print_1en
(JNIEnv *env, jobject this, jbyteArray byteen, jint length)
{
	char *chen = (char *)(*env)->GetByteArrayElements(env, byteen, 0);
	int iret = aexddB58T_print_en(s_print,env,this,chen,length);
	(*env)->ReleaseByteArrayElements(env,byteen, chen,0);
    return iret;
}


JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_set_1graph
(JNIEnv *env, jobject this, jchar m, jchar n1, jchar n2, jstring  strbmppath)

{
	char *chbmppath = (char *)(*env)->GetStringUTFChars(env, strbmppath, 0);
	int iret = aexddB58T_set_graph(s_print,env,this,m,n1,n2,chbmppath);
	(*env)->ReleaseStringUTFChars(env, strbmppath, chbmppath);
	return iret;
}


JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_set_1barcodeHigh
(JNIEnv *env, jobject this, jchar n)
{
	return aexddB58T_set_barcodeHigh(s_print,env,this,n);
}

JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_set_1barcodeWide
(JNIEnv *env, jobject this, jchar n)
{
	return aexddB58T_set_barcodeWide(s_print,env,this,n);
}


JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_print_1barcode
(JNIEnv *env, jobject this,jchar wide ,jchar high, jchar code, jstring strcode, jint len)
{
	char *chen = (char *)(*env)->GetStringUTFChars(env, strcode, 0);
	int iret = aexddB58T_print_barcode(s_print,env,this,wide,high,code,chen,len);
	(*env)->ReleaseStringUTFChars(env, strcode, chen);
	return iret;
}


JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_print_12Dimensional
  (JNIEnv *env, jobject this, jstring strcode, jint ilen)
{
	char  *pstrcode =(char *) (*env)->GetStringUTFChars(env, strcode, 0);
	int iret=aexddB58T_print_2Dimensional(s_print,env,this,pstrcode,ilen);
	(*env)->ReleaseStringUTFChars(env, strcode, pstrcode);
	return iret;
}

JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_SGT801_1out_12Dimensional
  (JNIEnv *env, jobject this, jint isize, jbyteArray bycode, jint ilen)
{
	char *pstrcode = (char*) (*env)->GetByteArrayElements(env, bycode, 0);
	int iret=aexddB58T_SGT801_print_2Dimensional(s_print,env,this,isize,pstrcode,ilen);
	(*env)->ReleaseByteArrayElements(env,bycode, pstrcode,0);
	return iret;
}

JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_print_1bitmap
  (JNIEnv *env, jobject this, jbyteArray data, jint size, jint bmpwidth, jint bmphigh, jint width, jint high)
{
	char *szData = (unsigned char*) (*env)->GetByteArrayElements(env, data, 0);
	int iret=aexddB58T_print_bitmap(s_print,env,this,szData,size, bmpwidth);
	(*env)->ReleaseByteArrayElements(env,data, szData,0);
	return iret;
}

/*
 * Class:     com_androidex_devices_aexddB58Printer
 * Method:    serial_recive
 * Signature: (II)Ljava/lang/String;
 */
JNIEXPORT jbyteArray JNICALL Java_com_androidex_devices_aexddB58Printer_serial_1read
  (JNIEnv *env, jobject this,jint timeout)
{
	char buf[2048];
	memset(buf,0,sizeof(buf));
	int len = serial_read(s_print,env,this,buf,timeout);
	if(len>0){
		jbyte* byte = (jbyte*)buf;
		jbyteArray jarray = (*env)->NewByteArray(env,len);
		(*env)->SetByteArrayRegion(env,jarray, 0, len, byte);
		return jarray;
	}

	return NULL;
}

/*
 * Class:     com_androidex_devices_aexddB58Printer
 * Method:    serial_write
 * Signature: (ILjava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_serial_1write
  (JNIEnv *env, jobject this, jbyteArray data,jint len)
{
	jbyte * arrayBody = (*env)->GetByteArrayElements(env,data,0);
	char* szdata = (char*)arrayBody;
	int r = serial_write(s_print,env,this,szdata,len);
	(*env)->ReleaseByteArrayElements(env,data, szdata,0);
	return r;
}

/*
 * Class:     com_androidex_devices_aexddB58Printer
 * Method:    serial_select
 * Signature: (III)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_serial_1select
  (JNIEnv *env, jobject this, jint sec, jint usec)
{
	int r = serial_select(s_print,env,this,sec,usec);
	return r;
}


/*
 * Class:     com_androidex_devices_aexddB58Printer
 * Method:    T_500AP_print_barcode
 * Signature: (CCCLjava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_T_1500AP_1out_1barcode
  (JNIEnv *env, jobject this,jchar wide ,jchar high, jchar code, jstring strcode, jint len)
{
	char *chen = (char *)(*env)->GetStringUTFChars(env, strcode, 0);
	int iret = aexddB58T_T_500AP_print_barcode(s_print,env,this,wide,high,code,chen,len);
	(*env)->ReleaseStringUTFChars(env, strcode, chen);
	return iret;
}

/*
 * Class:     com_androidex_devices_aexddB58Printer
 * Method:    T_500AP_print_2Dimensional
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_T_1500AP_1out_12Dimensional
(JNIEnv *env, jobject this, jstring strcode, jint ilen)
{
	char  *pstrcode =(char *) (*env)->GetStringUTFChars(env, strcode, 0);
	int iret=aexddB58T_T_500AP_print_2Dimensional(s_print,env,this,pstrcode,ilen);
	(*env)->ReleaseStringUTFChars(env, strcode, pstrcode);
	return iret;
}

/*
 * Class:     com_androidex_devices_aexddB58Printer
 * Method:    RG_CB532_print_barcode
 * Signature: (CCCLjava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_RG_1CB532_1out_1barcode
  (JNIEnv *env, jobject this,jchar wide ,jchar high, jchar code, jstring strcode, jint len)
{
	char *chen = (char *)(*env)->GetStringUTFChars(env, strcode, 0);
	int iret = aexddB58T_RG_CB532_print_barcode(s_print,env,this,wide,high,code,chen,len);
	(*env)->ReleaseStringUTFChars(env, strcode, chen);
	return iret;
}

/*
 * Class:     com_androidex_devices_aexddB58Printer
 * Method:    RG_CB532_print_2Dimensional
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_RG_1CB532_1out_12Dimensional
(JNIEnv *env, jobject this, jstring strcode, jint ilen)
{
	char  *pstrcode =(char *) (*env)->GetStringUTFChars(env, strcode, 0);
	int iret= aexddB58T_RG_CB532_print_2Dimensional(s_print,env,this,pstrcode,ilen);
	(*env)->ReleaseStringUTFChars(env, strcode, pstrcode);
	return iret;
}
/*
 * Class:     com_androidex_devices_aexddB58Printer
 * Method:    TA_500_print_2Dimensional
 * Signature: (Ljava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_TA_1500_1out_12Dimensional
(JNIEnv *env, jobject this, jstring strcode, jint ilen)
{
	char  *pstrcode =(char *) (*env)->GetStringUTFChars(env, strcode, 0);
	int iret= aexddB58T_TA500_print_2Dimensional(s_print,env,this,pstrcode,ilen);
	(*env)->ReleaseStringUTFChars(env, strcode, pstrcode);
	return iret;
}

/*
 * Class:     com_androidex_devices_aexddB58Printer
 * Method:    TA_500_cut
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_androidex_devices_aexddB58Printer_TA_1500_1cut
  (JNIEnv *env, jobject this,jint n)
{
	int iret=aexddB58T_TA500_cut(s_print,env,this,n);
    return iret;
}

