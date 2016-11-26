#include <jni.h>
#include "com_androidex_devices_aexddKMY350.h"
#include <memory.h>
#include <pthread.h>

#include <stdio.h>      /*标准输入输出定义*/
#include <stdlib.h>     /*标准函数库定义*/
#include <unistd.h>     /*Unix标准函数定义*/
#include <fcntl.h>      /*文件控制定义*/
#include <termios.h>    /*PPSIX终端控制定义*/
#include <errno.h>      /*错误号定义*/
#include <time.h>
#include <sys/ioctl.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <dirent.h>
#include "Des.h"
#include "../include/utils.h"
#include "aexddKMY350.h"

#include <android/log.h>

/**
 * 相当于java中的具体功能的实现类（认为）
 */

static ON_KMY_EVENT on_kmy_event=NULL;
static in_read_key = FALSE;		//由于读卡进程会持续一段时间，因此，为了避免程序二次进入读卡程序设置该参数。

/**
 * 设置回调函数，在JNI的代码里会调用它来设置处理事件的回调函数
 */
void kmy_set_event(ON_KMY_EVENT oke)
{
	on_kmy_event = oke;
}

/**
 * 密码键盘事件的入口函数，静态函数只能在本文件中调用
 */
static int kmy_event(KMY_HANDLE kmy,HKMY env,HKMY obj,int code,char *pszFormat,...)
{
	char pszDest[2048];
	va_list args;

	va_start(args, pszFormat);
	vsnprintf(pszDest, sizeof(pszDest), pszFormat, args);
	va_end(args);

	//只有设置了事件回调函数，此函数才会调用事件，否则什么也不做
	if(on_kmy_event){
		on_kmy_event(kmy,env,obj,code,pszDest);
	}
	return 0;
}

//例：if sour=0x39 0x30" then return =0x90
static char asc_to_bcd(char *sour) {
	char HBits = sour[0];
	//WriteLogInt("HBits=",HBits);

	if ((HBits >= 65) && (HBits <= 70))
		HBits -= 7; //for A--F
	if ((HBits >= 97) && (HBits <= 102))
		HBits -= 39; //for a--f
	if ((HBits - 0x30) > 0x0f)
		HBits = 0x30;
	else if ((HBits - 0x30) < 0x00)
		HBits = 0x30;
	HBits = (HBits - 0x30) * 0x10;

	char LBits = sour[1];
	//WriteLogInt("LBits=",LBits);

	if ((LBits >= 65) && (LBits <= 70))
		LBits -= 7; //for A--F
	if ((LBits >= 97) && (LBits <= 102))
		LBits -= 39; //for a--f
	if ((LBits - 0x30) > 0x0f)
		LBits = 0x30;
	else if ((LBits - 0x30) < 0x00)
		LBits = 0x30;
	LBits = (LBits - 0x30);

	char bcd = HBits + LBits;

	return bcd;
}

static int CompressAsc(char *soustr, int len, char *desstr) {
	int i = 0;
	int ch;
	char tmpstr[2049];

	sprintf(tmpstr, "%*.*s", len, len, soustr);
	for (i = 0; i < (len + 1) / 2; i++) {
		sscanf(tmpstr + i * 2, "%02X", &ch);
		desstr[i] = ch & 0xFF;
	}
	desstr[i] = '\0';
	return 0;
}

/**
 * 对十六进制的缓冲区进行按位异或操作
 */
static int HexBCC(char *buffer, int count) {
	int i, bcccalc, ch;
	bcccalc = 0;
	for (i = 0; i < count; i++) {
		sscanf(buffer + i * 2, "%02X", &ch);
		bcccalc ^= ch;
	}
	return (bcccalc);
}

/**
 * 对十六进制的缓冲区进行按位异或操作
 */
static int BCC(char *buffer, int size) {
	int i, bcccalc, ch;
	bcccalc = 0;
	for (i = 0; i < size; i++) {
		bcccalc ^= buffer[i];
	}
	return (bcccalc);
}

static char *SplitBcd(char *soustr, int len, char *desstr) {
	int i;

	strcpy(desstr, "");
	for (i = 0; i < len; i++) {
		sprintf(desstr, "%s%02X", desstr, soustr[i] & 0XFF);
		desstr += 2;
	}
	return desstr;
}

//DES加密
static int gDes(char *DataInput, char *key, char *DataOutput) {

	char DataBcd[128], tmp[128], KeyBcd[128];
	int nLen;

	memset(KeyBcd, '\0', 128);
	memset(DataBcd, '\0', 128);
	memset(tmp, '\0', 128);
	nLen = strlen(DataInput);
	if (nLen == 16) {
		CompressAsc(DataInput, 16, DataBcd);
	} else {
		memcpy(DataBcd, DataInput, 8);
	}
	nLen = strlen(key);
	if (nLen == 16) {
		CompressAsc(key, 16, KeyBcd);
	} else {
		memcpy(KeyBcd, key, 8);
	}
	Des((unsigned char *) DataBcd, (unsigned char *) KeyBcd,(unsigned char *) tmp);
	SplitBcd(tmp, 8, DataOutput);
	return 0;
}

//3DES加密
static int gTriDes(char *DataInput, char *key, char *DataOutput) {
	char DataBcd[128] = { 0 }, tmp[128] = { 0 }, left[20] = { 0 }, right[20] = {
			0 }, tmp1[128] = { 0 }, tmp2[128] = { 0 };
	int nLen;

	if (strlen(key) == 32) {
		char cpLeft[17] = { 0 }, cpRight[17] = { 0 };
		memcpy(cpLeft, key, 16);
		memcpy(cpRight, key + 16, 16);
		CompressAsc(cpLeft, 16, left);
		CompressAsc(cpRight, 16, right);
	} else {
		memcpy(left, key, 8);
		memcpy(right, key + 8, 8);
	}

	nLen = strlen(DataInput);
	if (nLen == 16) {
		CompressAsc(DataInput, 16, DataBcd);
	} else {
		memcpy(DataBcd, DataInput, 8);
	}
	Des((unsigned char *) DataBcd, (unsigned char *) left,
			(unsigned char *) tmp1);
	Undes((unsigned char *) tmp1, (unsigned char *) right,
			(unsigned char *) tmp2);
	Des((unsigned char *) tmp2, (unsigned char *) left, (unsigned char *) tmp);
	SplitBcd(tmp, 8, DataOutput);
	return 0;
}

/**
 * 接收信息直到收到特定字符串，接收三次如果如果还没收到则返回失败
 * @param fd 串口句柄
 * @param end 结束字符串
 * @param timeout 每次接收的超时时间
 */
static int kmy_recv_by_end(int fd,char *end,int timeout)
{
	char buf[512];
	int len=0,times=0;
	//从键盘获得结果
	// __android_log_print(ANDROID_LOG_INFO, "kmy", "kmy_recv_by_end(%d,%s,%d)",fd,end,timeout);
	while(times<1){
		memset(buf,0,sizeof(buf));
		len = com_recive(fd,buf,sizeof(buf),timeout);
		// __android_log_print(ANDROID_LOG_INFO, "kmy", "recive %d: %s",len,buf);
		if(len > 0){
			if(strstr(buf,end)){
				return TRUE;
			}
		}
		tcflush(fd,TCIOFLUSH);	//清除缓冲区
		times++;
	}
	return 0;
}

static char *kmy_get_st_msg(unsigned  st){
	switch(st){
	case 0x15:
		return "命令参数错";
	case 0x80:
		return "超时错误";
	case 0xA4:
		return "命令可成功执行,但主密钥无效";
	case 0xB5:
		return "命令无效,且主密钥无效";
	case 0xC4:
		return "命令可成功执行,但电池可能损坏";
	case 0xD5:
		return "命令无效,且电池可能损坏";
	case 0xE0:
		return "无效命令";
	default:
		if(st&0xF0 == 0xF0){
			switch(st&0x0F){
			case 0:
				return "自检错误，CPU错";
			case 1:
				return "自检错误，SRAM错";
			case 2:
				return "自检错误，键盘有短路错";
			case 3:
				return "自检错误，串口电平错";
			case 4:
				return "自检错误，CPU卡出错";
			case 5:
				return "自检错误，电池可能损坏";
			case 6:
				return "自检错误，主密钥失效";
			case 7:
				return "自检错误，杂项错";
			default :
				return "自检错误，未知类型";
			}
		}else{
			return "未知错误代码";
		}
	}
}

static unsigned int kmy_readlen(char * buf){
	char hex[3]="00";
	unsigned int len=0,r=0;

	memset(hex,0,sizeof(hex));
	sprintf(hex,"%c%c",buf[1],buf[2]);
	sscanf(hex,"%x",&len);
	r = ((len&0xFF) << 8);

	memset(hex,0,sizeof(hex));
	sprintf(hex,"%c%c",buf[3],buf[4]);
	sscanf(hex,"%x",&len);
	r = r | (len&0xFF);
	return r;
}

static int kmy_check_return(char *buf){
	char st[3]="00";
	unsigned int lst=0;

	memset(st,0,sizeof(st));
	sprintf(st,"%c%c",buf[3],buf[4]);
	sscanf(st,"%x",&lst);

	if(lst != 0x04){
		// __android_log_print(ANDROID_LOG_INFO, "kmy","Cmd execute error:%s,Buf=%s",kmy_get_st_msg(lst),buf+1);
		return lst>0?-1*lst:-1*0x200;
	}else{
		return lst;
	}
}
/**
 * 接收信息直到收到定长数据，接收三次如果如果还没收到则返回失败
 * 返回的第一个字符为标志，第二三字符为十六进制表示的数据长度
 * @param fd 串口句柄
 * @param timeout 每次接收的超时时间
 */
static int kmy_recv_by_len(int fd,char *buf,int bufsize,int timeout)
{
	int len=0;
	int mlen = 0;
	//从键盘获得结果
	memset(buf,0,sizeof(buf));
	len = com_recive(fd,buf,bufsize,timeout);
	while(len < 5){
		//如果接收的长度小于3的时候说明长度参数还没收齐。
		int r = com_recive(fd,buf+len,bufsize-len,timeout);
		if(r>0){
			len += r;
			if(buf[0] != 0x02){
				// __android_log_print(ANDROID_LOG_INFO, "kmy","kmy recive prefix error : 0x%02X",buf[0]);
				return -2;		//返回不是以02开头，数据不正确
			}
		}else
			return -2;
	}
	if(len > 2){
		char hex[3]="00";
		unsigned int lhex=0,r=0;

		if((r=kmy_check_return(buf))<=0){
			return r;
		}
		memset(hex,0,sizeof(hex));
		sprintf(hex,"%c%c",buf[1],buf[2]);
		sscanf(hex,"%x",&lhex);
		mlen = lhex*2+5;
		//__android_log_print(ANDROID_LOG_INFO, "kmy", "kmy_recv_by_len：len=%d,lhex=%d,mlen=%d,buf=\\0x%02X%s,hex=%s",len,lhex,mlen,buf[0],buf+1,hex);
		if(lhex > 512-5){
			// __android_log_print(ANDROID_LOG_INFO, "kmy","kmy recive len error : %d",lhex);
			return -2;	//长度超出范围，说明格式有问题
		}
		lhex = mlen - len;
		while(lhex > 0){
			len = com_recive(fd,buf+(mlen-lhex),bufsize-(mlen-lhex),timeout);
			if(len > 0){
				lhex -= len;
				if(lhex > 0)
					continue;
			}else if(len == -1){
				return -1;		//接收失败
			}else if(len == 0){
				continue;		//接收超时
			}
		}
		tcflush(fd,TCIOFLUSH);	//清除缓冲区
		__android_log_print(ANDROID_LOG_INFO, "kmy", "kmy_recv_by_len：mlen=%d,buf=(%d)\\0x%02X%s",
				mlen,strlen(buf),buf[0],buf+1);
		buf[mlen] = '\0';	//删除BCC以后的数据
	}else{
		return -2;	//第一次接收的数据有问题，没有发现有效的长度
	}
	return mlen;
}

/**
 *发送键盘命令的函数，发送后会等待键盘返回结果，在发送的函数里对长度、命令和参数都做了Hex编码，因此发送前就不需要做处理了。
 * @param fd 串口句柄
 * @param cmd 命令字符串
 */
int kmy_send_cmd(int fd,char *cmd,int size)
{
	char chSendCmd[256];
	unsigned int nLen, ret;

	tcflush(fd,TCIOFLUSH);
	memset(chSendCmd, '\0', sizeof(chSendCmd));

	nLen = sizeof(chSendCmd);
	strcpy(chSendCmd, "\x02");
	HexEncode(cmd,size,chSendCmd+1,&nLen);
	ret = BCC(cmd, size);
	sprintf(&chSendCmd[nLen+1],"%02X",ret);
	chSendCmd[nLen+3] = '\x03';
	//sprintf(chSendCmd, "%c%s%02X%c", 0x02, cmd, ret,0x03);

	// __android_log_print(ANDROID_LOG_DEBUG,"kmy","sendCmd(\\0x%02X%s\\0x03) return 0x%02X",chSendCmd[0],chSendCmd+1,ret);
	ret = com_write(fd, chSendCmd, strlen(chSendCmd));
	return ret;
}

int kmy_send_hexcmd(int fd,char *hexcmd,int size)
{
	char chSendCmd[256];
	int nLen, ret;

	tcflush(fd,TCIOFLUSH);
	memset(chSendCmd, '\0', sizeof(chSendCmd));

	ret = HexBCC(hexcmd, size);
	sprintf(chSendCmd, "%c%s%02X%c", 0x02, hexcmd, ret,0x03);

	// __android_log_print(ANDROID_LOG_DEBUG,"kmy","sendCmd(\\0x%02X%s\\0x03) return 0x%02X",chSendCmd[0],chSendCmd+1,ret);
	ret = com_write(fd, chSendCmd, strlen(chSendCmd));
	return ret;
}


int kmy_read_key_loop(int fd,char *cb,int timeout)
{
    char buf[512],str[512];
    int times = 6,r = 0,irecvlen=0;


    if(timeout == 0)
        timeout = 5000;
    tcflush(fd, TCIOFLUSH);
    do{
        memset(buf,0,sizeof(buf));
        memset(str,0,sizeof(str));
        r = com_recive(fd,buf,sizeof(buf),timeout);
        if(buf[0] == 0x2A || (buf[0] > '0' && buf[0] < '9')){
            irecvlen++;
            sprintf(str,"%s('%c');",cb,buf[0]);
        }else{
            sprintf(str,"%s('0x%02X');",cb,buf[0]);
        }

        if(buf[0] == 0x1B || buf[0] == 0x0D || buf[0] == 0x00){
            break;
        }else if(buf[0] == 0x08){
            irecvlen--;
            //kmy_event(kmy,env,obj,KE_PRESSED,str);
            times++;	//按了删除键
        }else{
            //kmy_event(kmy,env,obj,KE_PRESSED,str);
        }
        times--;
    }while(r>0 && times > 0 && buf[0] != 0x00);
    // __android_log_print(ANDROID_LOG_INFO, "kmy","end read key");
    if(buf[0] != 0x1B && buf[0] != 0x0D && buf[0] != 0x00){
        sprintf(str,"%s('0x%02X');",cb,0x0D);
    }
    in_read_key = FALSE;
    //kmy_event(kmy,env,obj,KE_PRESSED,str);
    return buf[0] != 0x0D;
}

/**
 * 打开密码键盘，返回密码键盘的句柄
 * @param arg 串口参数字符串，字符串格式为:	com=/dev/ttyUSB0(串口设备字符串),s=9600(波特率),p=N(奇偶校验),b=1(停止位),d=8(数据位数)
 */
KMY_HANDLE kmy_open(HKMY env,HKMY obj,char *arg)
{
    int fd = com_open(arg);//打开串口
    if(fd > 0){
        KMY_HANDLE kmy = (KMY_HANDLE)malloc(sizeof(KMY_DATA));//向系统申请分配空间
        if(kmy){
            memset(kmy,0,sizeof(KMY_DATA));
            kmy->fd = fd;
            kmy->mode = 1;
            kmy->max_pin_len = 6;
            strcpy(kmy->port,arg);
            if(kmy_get_version(kmy,env,obj,kmy->version,3000)){
                if(!kmy_check_device(kmy->version)){
                    kmy_close(kmy,env,obj);		//打开的不是密码键盘
                    return NULL;
                }
            }else{
                kmy_close(kmy,env,obj);		//打开的不是密码键盘
                return NULL;
            }
            kmy_get_sn(kmy,env,obj,kmy->sn,3000);
#ifdef	JNI_DEBUG
            {
                char buf[256];
                sprintf(buf,"port=%s,fd=%d,version=%s,sn=%s",arg,kmy->fd,kmy->version,kmy->sn);
                kmy_event(kmy,env,obj,KE_OPENED,buf);
            }
#endif
            in_read_key = FALSE;
            return kmy;
        }else{
#ifdef	JNI_DEBUG
            {
                char buf[256];
                sprintf(buf,"kmy malloc fail,fd closed,fd=%d",fd);
                kmy_event(NULL,env,obj,KE_OPENED,buf);
            }
#endif
            com_close(fd);
            return NULL;
        }
    }else{
        return NULL;
    }
}

/**
 *关闭密码键盘
 *@param kmy 密码键盘的句柄
 */
void kmy_close(KMY_HANDLE kmy,HKMY env,HKMY obj)
{
    in_read_key = FALSE;
    if(kmy){
        if(kmy->fd){
            com_close(kmy->fd);
#ifdef	JNI_DEBUG
            if(env && obj){
                char buf[256];

                sprintf(buf,"kmy closed,fd=%d",kmy->fd);
                kmy_event(kmy,env,obj,KE_CLOSED,buf);
            }
#endif
        }
        free(kmy);
    }
}

/**
 * 程序复位自检（不破坏密钥区）
 * @param fd 串口句柄
 * @param timeout 等待自检完成的时间，单位秒
 */
int kmy_reset(KMY_HANDLE kmy,HKMY env,HKMY obj,int timeout)
{
	if(!kmy)return FALSE;
	if(kmy_send_cmd(kmy->fd,"\x01\x31",2)>0){
		int r = kmy_recv_by_end(kmy->fd,"\0x30\0x35\0x00",timeout);
		if(r > 0){
			kmy_event(kmy,env,obj,KE_RESETED,"KMY reseted");
			return TRUE;
		}else{
			kmy_event(kmy,env,obj,KE_WARNING,"KMY reseted fail");
		}
	}
//	return FALSE;
	return FALSE;
}

/**
 * 程序复位自检，并重设密钥）
 * @param fd 串口句柄
 * @param timeout 等待自检完成的时间，单位秒
 */
int kmy_reset_with_pin(KMY_HANDLE kmy,HKMY env,HKMY obj,int timeout)
{
//	if(!kmy)return FALSE;
//	if(kmy_send_cmd(kmy->fd,"\x02\x31\x38",3)>0){
//		int r = kmy_recv_by_end(kmy->fd,"\x30\x35\x00",timeout);
//		if(r>0){
//			kmy_event(kmy,env,obj,KE_RESETED_PIN,"KMY reseted with pin");
//			return TRUE;
//		}else{
//			kmy_event(kmy,env,obj,KE_WARNING,"KMY reseted with pin fail");
//			return FALSE;
//		}
//	}
	return FALSE;
}

int kmy_check_device(char *ver){
	return utils_strincmp(ver,"KMY3501",7) == 0 || utils_strincmp(ver,"ZL51S5E",2) == 0;
}
/**
 * 获取产品的额序列号
 *@param kmy 键盘句柄
 *@param sn 存放序列号的缓冲区，缓冲区的空间分配和销毁由调用者处理
 *@param timeout 接收获取操作结果的超时时间，单位秒
 */
int kmy_get_sn(KMY_HANDLE kmy,HKMY env,HKMY obj,char *sn,int timeout)
{
	char buf[512];
	int len = 0;

	if(!kmy)return FALSE;
	memset(buf,0,sizeof(buf));
	if(kmy_send_cmd(kmy->fd,"\x01\x38",2)>0){
		//// __android_log_print(ANDROID_LOG_DEBUG,"kmy","Recive from %d",kmy->fd);
		if((len=kmy_recv_by_len(kmy->fd,buf,sizeof(buf),timeout))>5)
		{
			int snlen = kmy_readlen(buf);
			int l = (snlen & 0xFF00)>>8;

			__android_log_print(ANDROID_LOG_DEBUG,"kmy","get sn : len=%02X,st=%02X",l,snlen&0xFF);
			snlen = 256;
			HexDecode(buf+5,l*2-2,sn,&snlen);
			kmy_event(kmy,env,obj,KE_DEVICE_UUID,sn);
			return TRUE;
		}else{
			//// __android_log_print(ANDROID_LOG_DEBUG,"kmy","Recive %d:%s",len,buf);
			strcpy(sn,"");
			kmy_event(kmy,env,obj,KE_WARNING,"KMY get device uuid fail");
			return FALSE;
		}
	}
	return len;
}

int kmy_set_sn(KMY_HANDLE kmy,HKMY env,HKMY obj,char *sn,int timeout)
{
	char buf[512],cmd[64];
	int len = 0;

	if(!kmy)return FALSE;
	if(strlen(sn)!=8)return FALSE;
	memset(buf,0,sizeof(buf));
	sprintf(cmd,"\x09\x38%s",sn);
	if(kmy_send_cmd(kmy->fd,cmd,sizeof(cmd))>0){
		//// __android_log_print(ANDROID_LOG_DEBUG,"kmy","Recive from %d",kmy->fd);
		if((len=kmy_recv_by_len(kmy->fd,buf,sizeof(buf),timeout))>5)
		{
			//// __android_log_print(ANDROID_LOG_DEBUG,"kmy","Recive %d:%s",len,buf);
			return TRUE;
		}else{
			//// __android_log_print(ANDROID_LOG_DEBUG,"kmy","Recive %d:%s",len,buf);
			strcpy(sn,"");
			kmy_event(kmy,env,obj,KE_WARNING,"KMY set device uuid fail");
			return FALSE;
		}
	}
	return len;
}

/**
 * 获取产品的版本号
 *@param kmy 键盘句柄
 *@param v 存放版本号的缓冲区，缓冲区的空间分配和销毁由调用者处理
 *@param timeout 接收获取操作结果的超时时间，单位秒
 */
int kmy_get_version(KMY_HANDLE kmy,HKMY env,HKMY obj,char *v,int timeout)
{
	char buf[512];
	int len=0;

	if(!kmy)return FALSE;
	memset(buf,0,sizeof(buf));
	if(kmy_send_cmd(kmy->fd,"\x01\x30",2)>0){
		if((len=kmy_recv_by_len(kmy->fd,buf,sizeof(buf),timeout))>24){
			char pTmp[512];
			int l = sizeof(buf);

			//// __android_log_print(ANDROID_LOG_DEBUG,"kmy","Recive ver %d:%s",len,buf);
			memcpy(pTmp, buf + 5, 32);//len - 19);
			pTmp[32] = 0;
			//CompressAsc(pTmp, len - 19, buf);
			memset(buf,0,sizeof(buf));
			HexDecode(pTmp,strlen(pTmp),buf,&l);
			strcpy(v,buf);
			//// __android_log_print(ANDROID_LOG_DEBUG,"kmy","Recive ver %d:%s",strlen(v),v);
			kmy_event(kmy,env,obj,KE_DEVICE_VER,buf);
			return TRUE;
		}else{
			//// __android_log_print(ANDROID_LOG_DEBUG,"kmy","Recive %d:%s",len,buf);
		}
	}
	kmy_event(kmy,env,obj,KE_WARNING,"KMY get version fail");
	return FALSE;
}



/*
 * 设置mac算法模式
 *  @param iewm 01 MAC采用ASNI X9.9算法 *   02 MAC采用SAM卡算法  03 MAC采用银联的算法
 */
static int kmy_set_encrypt_mac(KMY_HANDLE kmy,HKMY env,HKMY obj,int iewm,int timeout){

	char buf[512];
	char *cmd = "";
	if(!kmy)return FALSE;
	if(iewm == 1)
		cmd = "\x03\x46\x06\x01";
	else if(iewm==2)
		cmd = "\x03\x46\x06\x02";
	else
		cmd = "\x03\x46\x06\x03";

	memset(buf,0,sizeof(buf));
	if(kmy_send_cmd(kmy->fd,cmd,4)>0){
		int r = kmy_recv_by_len(kmy->fd,buf,sizeof(buf),timeout);
		if(r>0){
			kmy_event(kmy,env,obj,KE_ENCRYPT_MODE,"KMY set mac encrypt mode success");
			return TRUE;
		}else if(r==0){
			kmy_event(kmy,env,obj,KE_WARNING,"KMY set mac encrypt mode 1 timeout");
			return FALSE;
		}else{
			kmy_event(kmy,env,obj,KE_WARNING,"KMY set mac  encrypt mode 1 fail");
			return FALSE;
		}
	}
	return FALSE;
}


/**
 * 主密钥下载、键盘输入PIN采用加密方式    DES or 3DES
 * @param fd 串口句柄
 * @param ewm 加密模式，0：DES模式，1:3DES模式
 * @param timeout 超时时间，单位秒
 */
int kmy_set_encrypt_mode(KMY_HANDLE kmy,HKMY env,HKMY obj,int ewm,int timeout)
{
	char buf[512];
	char *cmd = "";

	if(!kmy)return FALSE;
	kmy->mode = ewm;
	if(ewm == 0)
		cmd = "\x03\x46\x00\x20";
	else
		cmd = "\x03\x46\x00\x30";
	memset(buf,0,sizeof(buf));
	if(kmy_send_cmd(kmy->fd,cmd,4)>0){
		int r = kmy_recv_by_len(kmy->fd,buf,sizeof(buf),timeout);
		if(r>0){
			if(ewm == 0)
				cmd = "\x03\x46\x01\x20";
			else
				cmd = "\x03\x46\x01\x30";
			if(kmy_send_cmd(kmy->fd,cmd,4)>0){
				int r = kmy_recv_by_len(kmy->fd,buf,sizeof(buf),timeout);
				if(r>0){
					kmy_event(kmy,env,obj,KE_ENCRYPT_MODE,"KMY set encrypt mode success");
					return TRUE;
				}else if(r==0){
					kmy_event(kmy,env,obj,KE_WARNING,"KMY set encrypt mode 1 timeout");
					return FALSE;
				}else{
					kmy_event(kmy,env,obj,KE_WARNING,"KMY set encrypt mode 1 fail");
					return FALSE;
				}
			}
		}else if(r==0){
			kmy_event(kmy,env,obj,KE_WARNING,"KMY set encrypt mode timeout");
			return FALSE;
		}else{
			kmy_event(kmy,env,obj,KE_WARNING,"KMY set encrypt mode fail");
			return FALSE;
		}
	}
	return 0;
}

/**
 *下载主密钥，调用此函数前首先要调用kmy_set_encrypt_mode设置加密方式
 *@param fd 串口句柄
 *@param MKeyNo	 主密钥号 范围0－15
 *@param MKeyAsc	主密钥
 */
int kmy_dl_master_key(KMY_HANDLE kmy,HKMY env,HKMY obj,int MKeyNo, char *MKeyAsc,int timeout)
{
	char buf[512];
	int nLen = 0;

	if(!kmy)return FALSE;
	if ((MKeyNo > 15) || (MKeyNo < 0)) {
		kmy_event(kmy,env,obj,KE_WARNING,"主密钥号输入超出范围");
		return FALSE; //防止超出键盘容量
	}
	nLen = strlen(MKeyAsc);
	if ((nLen != 8) && (nLen != 16) && (nLen != 24) && (nLen != 32)) {
		kmy_event(kmy,env,obj,KE_WARNING,"主密钥长度不符合要求，密钥不需是2/16/24或者32个字符");
		return FALSE;
	}
	memset(buf,0,sizeof(buf));
	if(kmy->mode == 0){//DES
		if (nLen == 16) {
			sprintf(buf,"0A32%02X%s",MKeyNo,MKeyAsc);
		} else {
			char DataBcd[256];
			SplitBcd(MKeyAsc, 8, DataBcd);
			sprintf(buf,"0A32%02X%s",MKeyNo,DataBcd);
		}
	}else{
		//3Des方式下传32位BCD，或从16位ASCII转换为32位BCD
		if (nLen == 32) {
			sprintf(buf,"1232%02X%s",MKeyNo,MKeyAsc);
		} else {
			char DataBcd[256];
			SplitBcd(MKeyAsc, 16, DataBcd);
			sprintf(buf,"1232%02X%s",MKeyNo,DataBcd);
		}
	}
	if(kmy_send_hexcmd(kmy->fd,buf,sizeof(buf))>0){
		int r = kmy_recv_by_len(kmy->fd,buf,sizeof(buf),timeout);
		if(r>0){
			kmy_event(kmy,env,obj,KE_ENCRYPT_MODE,"KMY Load Master Key success");
			return TRUE;
		}else if(r==0){
			kmy_event(kmy,env,obj,KE_WARNING,"KMY Load Master Key timeout");
		}else{
			kmy_event(kmy,env,obj,KE_WARNING,"KMY Load Master Key fail");
		}
	}
	return FALSE;
}

/**
 *  下载工作密钥，调用此函数之前需要先调用设置加密模式的函数
 *@param MKeyNo 主密钥号 范围0－15
 *@param WKeyNo	工作密钥号 范围0－15
 *@param WKeyAsc 工作密钥
 */
int kmy_dl_work_key(KMY_HANDLE kmy,HKMY env,HKMY obj,int MKeyNo, int WKeyNo, char *WKeyAsc,int timeout)
{
	char buf[512];
	int nLen=0;
	if(!kmy)return FALSE;
	if ((MKeyNo > 15) || (MKeyNo < 0)) {
		kmy_event(kmy,env,obj,KE_WARNING,"主密钥号输入超出范围");
		return FALSE; //防止超出键盘容量
	}
	if ((WKeyNo > 15) || (WKeyNo < 0)) {
		kmy_event(kmy,env,obj,KE_WARNING,"工作密钥号输入超出范围");
		return FALSE; //防止超出键盘容量
	}
	nLen = strlen(WKeyAsc);
	if ((nLen != 8) && (nLen != 16) && (nLen != 24) && (nLen != 32)) {
		kmy_event(kmy,env,obj,KE_WARNING,"工作密钥长度不符合要求，密钥不需是2/16/24或者32个字符");
		return FALSE;
	}
	memset(buf,0,sizeof(buf));
	if(kmy->mode == 0){//DES
		if (nLen == 16) {
			sprintf(buf,"0B33%02X%02X%s",MKeyNo,WKeyNo,WKeyAsc);
		} else {
			char DataBcd[256];
			SplitBcd(WKeyAsc, 8, DataBcd);
			sprintf(buf,"0B33%02X%02X%s",MKeyNo,WKeyNo,DataBcd);
		}
	}else{
		//3Des方式下传32位BCD，或从16位ASCII转换为32位BCD
		if (nLen == 32) {
			sprintf(buf,"1333%02X%02X%s",MKeyNo,WKeyNo,WKeyAsc);
		} else {
			char DataBcd[256];
			SplitBcd(WKeyAsc, 16, DataBcd);
			sprintf(buf,"1333%02X%02X%s",MKeyNo,WKeyNo,DataBcd);
		}
	}
	if(kmy_send_hexcmd(kmy->fd,buf,sizeof(buf))>0){
		int r = kmy_recv_by_len(kmy->fd,buf,sizeof(buf),timeout);
		if(r>0){
			kmy_event(kmy,env,obj,KE_ENCRYPT_MODE,"KMY Load work Key success");
			return TRUE;
		}else if(r==0){
			kmy_event(kmy,env,obj,KE_WARNING,"KMY Load work Key timeout");
		}else{
			kmy_event(kmy,env,obj,KE_WARNING,"KMY Load work Key fail");
		}
	}
	return FALSE;
}

/**
 * 激活工作密钥
 *@param kmy 密码键盘句柄
 *@param MKeyNo 主密钥号0-15
 *@param WKeyNo 工作密钥号0-15
 *@param timeout 操作超时时间
 */
int kmy_active_work_key(KMY_HANDLE kmy,HKMY env,HKMY obj,int MKeyNo, int WKeyNo,int timeout)
{
	char buf[512];

	if(!kmy)return FALSE;
	if ((MKeyNo > 15) || (MKeyNo < 0)) {
		kmy_event(kmy,env,obj,KE_WARNING,"主密钥号输入超出范围");
		return FALSE; //防止超出键盘容量
	}

	if ((WKeyNo > 15) || (WKeyNo < 0)) {
		kmy_event(kmy,env,obj,KE_WARNING,"工作密钥号输入超出范围");
		return FALSE; //防止超出键盘容量
	}
	memset(buf,0,sizeof(buf));
	sprintf(buf,"0343%02X%02X",MKeyNo,WKeyNo);
	if(kmy_send_hexcmd(kmy->fd,buf,sizeof(buf))>0){
		int r = kmy_recv_by_len(kmy->fd,buf,sizeof(buf),timeout);
		if(r>0){
			kmy_event(kmy,env,obj,KE_ENCRYPT_MODE,"KMY active work Key success");
			return TRUE;
		}else if(r==0){
			kmy_event(kmy,env,obj,KE_WARNING,"KMY active work Key timeout");
		}else{
			kmy_event(kmy,env,obj,KE_WARNING,"KMY active work Key fail");
		}
	}
	return FALSE;
}

/**
 *发送开关键盘和按键声音
 *@param kmy 键盘句柄
 *@param CTL 关闭键盘:1 打开键盘:2 打开键盘且静音:3 系统键盘:4
 *@param timeout 接收数据超时时间
 */
int kmy_open_keypad(KMY_HANDLE kmy,HKMY env,HKMY obj,int CTL,int timeout)
{
	char buf[512];

	if(!kmy)return FALSE;
	memset(buf,0,sizeof(buf));
	sprintf(buf,"0245%02X",CTL);
	if(kmy_send_hexcmd(kmy->fd,buf,sizeof(buf))>0){
		int r = kmy_recv_by_len(kmy->fd,buf,sizeof(buf),timeout);
		if(r>0){
			kmy_event(kmy,env,obj,KE_ENCRYPT_MODE,"KMY Open kekpad success");
			return TRUE;
		}else if(r==0){
			kmy_event(kmy,env,obj,KE_WARNING,"KMY Open kekpad Key timeout");
		}else{
			kmy_event(kmy,env,obj,KE_WARNING,"KMY Open kekpad Key fail");
		}
	}
	return FALSE;
}

/**
 * 下载银行卡卡号，开始密码输入
 *@param kmy 密码键盘的句柄
 *@param pchCardNo 银行卡号码
 *@param timeout 超时时间
 */
int kmy_dl_card_no(KMY_HANDLE kmy,HKMY env,HKMY obj,char *pchCardNo,int timeout)
{

	char buf[512];
	char CardNoAsc[25], CardNoBcd[13];
	if(!kmy)return FALSE;
	memset(buf,0,sizeof(buf));
	memset(CardNoAsc, '\0', sizeof(CardNoAsc));
	memset(CardNoBcd, '\0', sizeof(CardNoBcd));

	if (strlen(pchCardNo) <= 12) //卡号长度不足13位
	{
		// __android_log_print(ANDROID_LOG_INFO, "kmy", "kmy_dl_card_no pchcardno<=12 " );
		kmy_event(kmy,env,obj,KE_WARNING,"卡号长度不足13位");
		return FALSE;
	}
	memcpy(CardNoBcd, pchCardNo + (strlen(pchCardNo) - 13), 12);
	SplitBcd(CardNoBcd, 12, CardNoAsc);
	sprintf(buf,"0D34%s",CardNoAsc);
	if(kmy_send_hexcmd(kmy->fd,buf,sizeof(buf))>0){
		int r = kmy_recv_by_len(kmy->fd,buf,sizeof(buf),timeout);
		if(r>0){
			// __android_log_print(ANDROID_LOG_INFO, "kmy", "kmy_dl_card_no success " );
			kmy_event(kmy,env,obj,KE_ENCRYPT_MODE,"KMY pin load card no success");
			return TRUE;
		}else if(r==0){
			// __android_log_print(ANDROID_LOG_INFO, "kmy", "kmy_dl_card_no 1 " );
			kmy_event(kmy,env,obj,KE_WARNING,"KMY pin load card no timeout");

		}else{
			// __android_log_print(ANDROID_LOG_INFO, "kmy", "kmy_dl_card_no 2 " );
			kmy_event(kmy,env,obj,KE_WARNING,"KMY pin load card no fail");

		}
	}
	return FALSE;
}

/**
 *开始键盘PIN加密
 *@param kmy 密码键盘句柄
 *@param Pinlen 密码的长度
 *@param DispMode 显示模式
 *@param AddMode
 *@param PromMode
 *@param nTimeOut 输入密码的超时时间
 *@param timeout 接收回应包的时间
 */
int kmy_start_pin(KMY_HANDLE kmy,HKMY env,HKMY obj,short PinLen, short DispMode, short AddMode, short PromMode,short nTimeOut,int timeout)
{
	char buf[512];

	if(!kmy)return FALSE;
	memset(buf,0,sizeof(buf));
	sprintf(buf,"0635%02X%02X%02X%02X%02X",PinLen,DispMode,AddMode,PromMode,nTimeOut);
	if(kmy_send_hexcmd(kmy->fd,buf,sizeof(buf))>0){
		int r = kmy_recv_by_len(kmy->fd,buf,sizeof(buf),timeout);
		if(r>0){
			kmy_event(kmy,env,obj,KE_ENCRYPT_MODE,"KMY start pin add success");
			return TRUE;
		}else if(r==0){
			kmy_event(kmy,env,obj,KE_WARNING,"KMY pin start add timeout");
		}else{
			kmy_event(kmy,env,obj,KE_WARNING,"KMY pin start add fail");
		}
	}
	return FALSE;
}

/**
 *PINBLOCK运算
 *@param kmy 密码键盘句柄
 *@param pchCardNo 银行卡卡号
 *@param timeout 接收包超时时间
 */
int kmy_pin_block(KMY_HANDLE kmy,HKMY env,HKMY obj,char *pchCardNo,int timeout)
{
	int iretu;

	if(!kmy)return FALSE;
	//关闭键盘:1 打开键盘:2 打开键盘且静音:3 系统键盘:4
	iretu = kmy_open_keypad(kmy,env,obj,2,timeout);
	if (!iretu) {
		kmy_event(kmy,env,obj,KE_WARNING,"使键盘发声失败");
		return FALSE;
	}

	iretu = kmy_dl_card_no(kmy,env,obj,pchCardNo,timeout);
	if (!iretu) {
		kmy_event(kmy,env,obj,KE_WARNING,"下载卡号开始密码输入失败");
		return FALSE;
	}
	iretu = kmy_start_pin(kmy,env,obj,kmy->max_pin_len, 1, 1, 0, (10*timeout)/1000,timeout); //与卡号运算加密
	if (!iretu) {
		kmy_event(kmy,env,obj,KE_WARNING,"下载卡号开始密码输入失败");
		return FALSE;
	}
	return TRUE;
}

/**
 * 获取密码密文
 *@param kmy 键盘句柄
 *@param chPin 返回密文的缓冲区
 *@param timeout
 */
int kmy_read_pin(KMY_HANDLE kmy,HKMY env,HKMY obj,char *chPin,char *hexPin,int timeout)
{
	char buf[512];

	if(!kmy)return FALSE;
	memset(buf,0,sizeof(buf));
	sprintf(buf,"\x01\x42");
	if(kmy_send_cmd(kmy->fd,buf,2)>0){
		int r = kmy_recv_by_len(kmy->fd,buf,sizeof(buf),timeout);
		if(r>0 && r){
			char pin[17];
			memset(pin,0,sizeof(pin));
			memcpy(pin,buf + 5,16);
			// __android_log_print(ANDROID_LOG_INFO, "kmy","buf=%s,pin is : %s",buf,pin);
			if(strlen(pin) <  (kmy->max_pin_len)*2 ){
				kmy_event(kmy,env,obj,KE_ENCRYPT_MODE,"KMY read pin too short");
				return FALSE;
			}

			if(hexPin){
				strcpy(hexPin,pin);
			}
			kmy_event(kmy,env,obj,KE_HEX_PIN,pin);
			if(chPin){
				CompressAsc(buf + 5, 16, chPin);
				chPin[8] = 0;
			}
			kmy_event(kmy,env,obj,KE_ENCRYPT_MODE,"KMY read pin success");
			return TRUE;
		}else if(r==0){
			kmy_event(kmy,env,obj,KE_WARNING,"KMY Load read pin timeout");
		}else{
			kmy_event(kmy,env,obj,KE_WARNING,"KMY Load read pin fail");
		}
	}
	return FALSE;
}

int kmy_start_read_key(KMY_HANDLE kmy,HKMY env,HKMY obj,char *cb,int timeout)
{
	char buf[512],str[512];
	int times = 6,r = 0,irecvlen=0;


	if(timeout == 0)
		timeout = 5000;
	if(!kmy) return FALSE;
//	if(in_read_key){
//		// __android_log_print(ANDROID_LOG_INFO, "kmy","already read key.");
//		return 0;
//	}
	in_read_key = TRUE;
	tcflush(kmy->fd, TCIOFLUSH);
	do{
		memset(buf,0,sizeof(buf));
		memset(str,0,sizeof(str));
		if(irecvlen==kmy->max_pin_len) return TRUE;
		r = com_recive(kmy->fd,buf,sizeof(buf),timeout);
		 __android_log_print(ANDROID_LOG_INFO, "kmy","r=%d,times=%d，buf=0x%02X,pinlen=%d",r,times-1,buf[0],kmy->max_pin_len);
		 if(buf[0] == 0x2A || (buf[0] > '0' && buf[0] < '9')){
			irecvlen++;
			sprintf(str,"%s('%c');",cb,buf[0]);
		 }else{
			sprintf(str,"%s('0x%02X');",cb,buf[0]);
		}

		if(buf[0] == 0x1B || buf[0] == 0x0D || buf[0] == 0x00){
			break;
		}else if(buf[0] == 0x08){
			irecvlen--;
			kmy_event(kmy,env,obj,KE_PRESSED,str);
			times++;	//按了删除键
		}else{
			kmy_event(kmy,env,obj,KE_PRESSED,str);
		}
		times--;
	}while(r>0 && times > 0 && buf[0] != 0x00);
	// __android_log_print(ANDROID_LOG_INFO, "kmy","end read key");
	if(buf[0] != 0x1B && buf[0] != 0x0D && buf[0] != 0x00){
		sprintf(str,"%s('0x%02X');",cb,0x0D);
	}
	in_read_key = FALSE;
	kmy_event(kmy,env,obj,KE_PRESSED,str);
	return buf[0] != 0x0D;
}

/**
 * 加密密码
 *@param kmy		键盘句柄
 *@param DataInput	输入数据
 *@param DataOutput	输出数据
 *@param timeout	操作超时时间
 */
int kmy_encrypt(KMY_HANDLE kmy,HKMY env,HKMY obj,char *DataInput, char *DataOutput,char * hexOut,int timeout)
{
	char buf[512];
	int nLen = 0;

	if(!kmy)return FALSE;
	nLen = strlen(DataInput);
	if (nLen % 16 != 0) {
		kmy_event(kmy,env,obj,KE_WARNING,"输入加密的数据长度不正确！");
		return FALSE;
	}
	memset(buf,0,sizeof(buf));
	sprintf(buf,"%02X36",nLen/2+1);
	memcpy(&buf[4], DataInput, nLen);
	if(kmy_send_hexcmd(kmy->fd,buf,sizeof(buf))>0){
		int r = kmy_recv_by_len(kmy->fd,buf,sizeof(buf),timeout);
		if(r>0){
			CompressAsc(buf + 5, 16, DataOutput);
			DataOutput[8] = 0;
			kmy_event(kmy,env,obj,KE_ENCRYPT_MODE,"KMY encrypt success");
			return TRUE;
		}else if(r==0){
			kmy_event(kmy,env,obj,KE_WARNING,"KMY encrypt timeout");
		}else{
			kmy_event(kmy,env,obj,KE_WARNING,"KMY encrypt fail");
		}
	}
	return FALSE;
}

/**
 * 解密密码
 *@param kmy		键盘句柄
 *@param DataInput	输入数据
 *@param DataOutput	输出数据
 *@param timeout	操作超时时间
 */
int kmy_decrypt(KMY_HANDLE kmy,HKMY env,HKMY obj,char *DataInput, char *DataOutput,int timeout)
{
	char buf[512];
	int nLen = 0;

	if(!kmy)return FALSE;
	nLen = strlen(DataInput);
	if (nLen % 16 != 0) {
		kmy_event(kmy,env,obj,KE_WARNING,"输入加密的数据长度不正确！");
		return FALSE;
	}
	memset(buf,0,sizeof(buf));
	sprintf(buf,"%02X37",nLen/2+1);
	memcpy(&buf[4], DataInput, nLen);
	if(kmy_send_hexcmd(kmy->fd,buf,sizeof(buf))>0){
		int r = kmy_recv_by_len(kmy->fd,buf,sizeof(buf),timeout);
		if(r>0){
			CompressAsc(buf + 5, 16, DataOutput);
			DataOutput[8] = 0;
			kmy_event(kmy,env,obj,KE_ENCRYPT_MODE,"KMY decrypt success");
			return TRUE;
		}else if(r==0){
			kmy_event(kmy,env,obj,KE_WARNING,"KMY decrypt timeout");
		}else{
			kmy_event(kmy,env,obj,KE_WARNING,"KMY decrypt fail");
		}
	}
	return FALSE;
}

/**
 * Mac
 *@param kmy		键盘句柄
 *@param DataInput	输入数据
 *@param DataOutput	输出数据
 *@param timeout	操作超时时间
 */
int kmy_calc_mac_data(KMY_HANDLE kmy,HKMY env,HKMY obj,char *DataInput, char *DataOutput,char * hexOut,int timeout)
{
	char buf[512];
	int nLen = 0;

	if(!kmy)return FALSE;

	int ire=kmy_set_encrypt_mode(kmy,env,obj,0,timeout);
	// __android_log_print(ANDROID_LOG_INFO, "kmy","Set encrypt mode %s",ire?"TRUE":"FALSE");
	ire = kmy_active_work_key(kmy,env,obj,0,0,timeout);
	// __android_log_print(ANDROID_LOG_INFO, "kmy","Set active work key %s",ire?"TRUE":"FALSE");
	nLen = strlen(DataInput);
	__android_log_print(ANDROID_LOG_INFO, "kmy","Mac input = (%d)%s",strlen(DataInput),DataInput);
	if (nLen < 5 || nLen > 200) {
		kmy_event(kmy,env,obj,KE_WARNING,"输入MAC运算的数据长度不正确！");
		return FALSE;
	}
	memset(buf,0,sizeof(buf));
	sprintf(buf,"%02X41%s",nLen/2+1,DataInput);
	if(kmy_send_hexcmd(kmy->fd,buf,sizeof(buf))>0){
		int r = kmy_recv_by_len(kmy->fd,buf,sizeof(buf),timeout);
		if(r>0){
			if(hexOut){
				memcpy(hexOut,buf+5,16);
				hexOut[16] = 0;
			}
			if(DataOutput){
				CompressAsc(buf + 5, 16, DataOutput);
				DataOutput[8] = 0;
			}
			memset(buf,0,sizeof(buf));
			HexEncode(DataOutput,8,buf,&nLen);
			// __android_log_print(ANDROID_LOG_INFO, "kmy","Mac output = %s(%s)",hexOut,buf);
			kmy_event(kmy,env,obj,KE_ENCRYPT_MODE,"KMY mac encrypt success");
			return TRUE;
		}else if(r==0){
			kmy_event(kmy,env,obj,KE_WARNING,"KMY mac encrypt timeout");
		}else{
			kmy_event(kmy,env,obj,KE_WARNING,"KMY mac encrypt fail");
		}
	}
	return FALSE;
}

/**
 * 银联计算Mac
 *@param kmy		键盘句柄
 *@param DataInput	输入数据
 *@param DataOutput	输出数据
 *@param timeout	操作超时时间
 */
int yl_calc_mac_data(KMY_HANDLE kmy,HKMY env,HKMY obj,char *DataInput, char *DataOutput,char * hexOut,int timeout)
{
	char buf[512];
	int nLen = 0;

	if(!kmy)return FALSE;

	int ire=kmy_set_encrypt_mode(kmy,env,obj,1,timeout);
	// __android_log_print(ANDROID_LOG_INFO, "kmy","Set encrypt mode %s",ire?"TRUE":"FALSE");
	ire = kmy_active_work_key(kmy,env,obj,0,1,timeout);
	// __android_log_print(ANDROID_LOG_INFO, "kmy","Set active work key %s",ire?"TRUE":"FALSE");
	nLen = strlen(DataInput);
	__android_log_print(ANDROID_LOG_INFO, "kmy","Mac input = (%d)%s",strlen(DataInput),DataInput);
	if (nLen < 5 || nLen > 200) {
		kmy_event(kmy,env,obj,KE_WARNING,"输入MAC运算的数据长度不正确！");
		return FALSE;
	}
	if(yl_get_data(kmy,env,obj,DataInput,DataOutput,timeout) == 0)
	{
		kmy_event(kmy,env,obj,KE_WARNING,"计算银联加密失败！");
		return FALSE;
	}
	int i = 0;
	for(i = 0; i< 8; i++)
	{
		char ch[3] = {0};
		int iTemp = 0;
		ch[0] = DataOutput[i*2];
		ch[1] = DataOutput[i*2 +1];
		sscanf(ch, "%x", &iTemp);
		hexOut[i] = iTemp;
	}
	__android_log_print(ANDROID_LOG_INFO, "kmy","Mac output = %s(%s)",hexOut,buf);
	return TRUE;
}

/*
 * 银联算法手动计算MAC
 */
int yl_get_data(KMY_HANDLE kmy,HKMY env,HKMY obj,char* DataInput, char* DataOutput,int timeout)
{
    int iret = 0;
    int j = 0;
	unsigned char rbuf[60] = {0};
	unsigned int k = strlen((char *)DataInput);
	unsigned int i;
	unsigned char UcData[1024] = {0};
	unsigned char UcDataE[8] = {0};
	unsigned char szData[1024] = {0};

	k = strlen((char *)DataInput);
	memcpy(UcData,DataInput,k);
	k/=2;

	for(i=0;i<k;i++)
	{
	  sscanf((char *)UcData+2*i,"%02X",&iret);
	  szData[i]=iret;
	}
	memset(UcData,0,k+8);
	memcpy(UcData,szData,k);

	k=(k+7)/8;
	for(i=0;i<k;i++)
		for (j=0;j<8;j++)
			UcDataE[j] ^= UcData[8*i+j];

	unsigned char UcDataRe[8] = {0};
	//对最后的8个字节转16字节，取前8个字节MAC运算
	unsigned char UcDataP[8] = {0};
	BcdToAsc(UcDataP,UcDataE,4);

	iret = yl_encrypt_data(kmy,env,obj,UcDataP,UcDataRe,timeout);
	if (iret == 0)
	{
		return iret;
	}

	//将加密后的结果与后8个字节异或
	unsigned char UcDataB[8] = {0};
	BcdToAsc(UcDataB,UcDataE+4,4);
	for (i=0;i<8;i++)
		UcDataB[i] ^= UcDataRe[i];

	//将异或的结果再进行一次MAK加密
	iret = yl_encrypt_data(kmy,env,obj,UcDataB,UcDataRe,timeout);
	if (iret == 0)
	{
		return iret;
	}
	//将运算后的结果转换成16个字节
	unsigned char UcDataR[17] = {0};
	BcdToAsc(UcDataR,UcDataRe,8);

	//结果的前8个字节作为MAC值
	BcdToAsc(DataOutput,UcDataR,8);
	return iret;
}

/**
 * 银联获取密码密文
 *@param kmy 键盘句柄
 *@param chPin 返回密文的缓冲区
 *@param timeout
 */
int yl_read_pin(KMY_HANDLE kmy,HKMY env,HKMY obj,char *chPin,char *hexPin,int timeout)
{
	char buf[512];

	if(!kmy)return FALSE;
	memset(buf,0,sizeof(buf));
	sprintf(buf,"0142");
	if(kmy_send_cmd(kmy->fd,buf,sizeof(buf))>0){
		int r = kmy_recv_by_len(kmy->fd,buf,sizeof(buf),timeout);
		if(r>0 && r){
			char pin[17];
			memset(pin,0,sizeof(pin));
			memcpy(pin,buf + 5,16);
			// __android_log_print(ANDROID_LOG_INFO, "kmy","buf=%s,pin is : %s",buf,pin);
			if(strlen(pin) <  (kmy->max_pin_len)*2 ){
				kmy_event(kmy,env,obj,KE_ENCRYPT_MODE,"KMY read pin too short");
				return FALSE;
			}

			if(hexPin){
				strcpy(hexPin,pin);
			}
			kmy_event(kmy,env,obj,KE_HEX_PIN,pin);
			if(chPin){
				CompressAsc(buf + 5, 16, chPin);
				chPin[8] = 0;
			}
			kmy_event(kmy,env,obj,KE_ENCRYPT_MODE,"KMY read pin success");
			return TRUE;
		}else if(r==0){
			kmy_event(kmy,env,obj,KE_WARNING,"KMY Load read pin timeout");
		}else{
			kmy_event(kmy,env,obj,KE_WARNING,"KMY Load read pin fail");
		}
	}
	return FALSE;
}

/**
 *加密mac数据
 *@param kmy 键盘句柄
 *@param szMac    输入密码键盘数据
 *@param szOutput 输出加密后数据
 *@param timeout 接收数据超时时间
 */
int yl_encrypt_data(KMY_HANDLE kmy,HKMY env,HKMY obj,char *DataInput, char *DataOutput,int timeout)
{
	char buf[512];
	int nLen = 0;
	nLen = strlen(DataInput);

	if(!kmy)return FALSE;
	memset(buf,0,sizeof(buf));
	sprintf(buf,"%02X36%s",nLen/2+1,DataInput);
	if(kmy_send_hexcmd(kmy->fd,buf,sizeof(buf))>0){
		int r = kmy_recv_by_len(kmy->fd,buf,sizeof(buf),timeout);
		if(r>0){
				if(DataOutput){
					CompressAsc(buf + 5, 16, DataOutput);
					DataOutput[8] = 0;
				}
				memset(buf,0,sizeof(buf));
				HexEncode(DataOutput,8,buf,&nLen);
			kmy_event(kmy,env,obj,KE_ENCRYPT_MODE,"YL Encrypt mac  success");
			return TRUE;
		}else if(r==0){
			kmy_event(kmy,env,obj,KE_WARNING,"YL Encrypt mac timeout");
		}else{
			kmy_event(kmy,env,obj,KE_WARNING,"YL Encrypt mac fail");
		}
	}
	return FALSE;
}

int kmy_start_all_step(KMY_HANDLE kmy,HKMY env,HKMY obj,int mKeyNo,int wKeyNo,char *chwKey,char *chcardNo,char *chcb,char *passHex,char *port,int timeout)
{
	if(kmy_get_version(kmy,env,obj,kmy->version,3000)==FALSE){
		if(kmy)
			kmy_close(kmy,env,obj);

		kmy = kmy_open(env,obj,port);
		if(kmy){
			SetKmyHandle(kmy);
		}else{
			return FALSE;
		}
	}
	int ire=kmy_reset(kmy,env,obj,timeout);
	ire=kmy_set_encrypt_mode(kmy,env,obj,0,timeout);
	// __android_log_print(ANDROID_LOG_INFO, "kmy","Set encrypt mode %s",ire?"TRUE":"FALSE");
	ire=kmy_set_encrypt_mac(kmy,env,obj,1,timeout);
	// __android_log_print(ANDROID_LOG_INFO, "kmy","Set mac encrypt mode %s",ire?"TRUE":"FALSE");

	int r = kmy_dl_work_key(kmy,env,obj,mKeyNo,wKeyNo, chwKey,timeout);
	if(r){
		if(kmy_active_work_key(kmy,env,obj,mKeyNo,wKeyNo,timeout)){
			if(kmy_pin_block(kmy,env,obj,chcardNo,timeout)){
				kmy_event(kmy,env,obj,KE_START_PIN,"{success:true,status:\"%d\",msg:\"%s\"}",1,"请提示用户输入密码!");
				if(kmy_start_read_key(kmy,env,obj,chcb,timeout)){
					usleep(100000);
					return kmy_read_pin(kmy,env,obj,NULL,passHex,timeout);
				}else{
					__android_log_print(ANDROID_LOG_INFO, "kmy","kmy_start_read_key fail");
				}
				return FALSE;
			}else{
				return FALSE;
			}
		}else{
			return FALSE;
		}
	}else{
		return FALSE;
	}
}
