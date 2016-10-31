#include <jni.h>
#include "com_androidex_devices_aexddMT318.h"
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
#include "./include/utils.h"
#include "aexddMT318.h"

#include <android/log.h>

static ON_KKCARD_EVENT on_kkcard_event=NULL;
static in_read_card = FALSE;		//由于读卡进程会持续一段时间，因此，为了避免程序二次进入读卡程序设置该参数。

/**
 * 设置回调函数，在JNI的代码里会调用它来设置处理事件的回调函数
 */
void kkcard_set_event(ON_KKCARD_EVENT oke)
{
	on_kkcard_event = oke;
}

/**
 * 密码键盘事件的入口函数，静态函数只能在本文件中调用
 */
static int kkcard_event(KKCARD_HANDLE kkc,HKKC env,HKKC obj,int code,char *pszFormat,...)
{
	char pszDest[2048];
	va_list args;

	va_start(args, pszFormat);
	vsnprintf(pszDest, sizeof(pszDest), pszFormat, args);
	va_end(args);
	//只有设置了事件回调函数，此函数才会调用事件，否则什么也不做
	if(on_kkcard_event){
		return on_kkcard_event(kkc,env,obj,code,pszDest);
	}else{
	    return 0;
	}
}

/**
 * 对十六进制的缓冲区进行按位异或操作
 */
static int BCC(char *buffer, int count) {
	int i, bcccalc, ch;
	bcccalc = 0;
	for (i = 0; i < count; i++) {
		bcccalc ^= buffer[i];
	}
	return bcccalc;
}

/**
 * 接收信息直到收到定长数据，接收三次如果如果还没收到则返回失败
 * 返回的第一个字符为标志，第二三字符为十六进制表示的数据长度
 * @param fd 串口句柄
 * @param timeout 每次接收的超时时间
 */
static int kkcard_recv_by_len(int fd,char *buf,int bufsize,int timeout)
{
	int len=0;
	int mlen = 0;
	//从键盘获得结果
	memset(buf,0,sizeof(buf));
	len = com_recive(fd,buf,bufsize,timeout);
	while(len < 3){
		//如果接收的长度小于3的时候说明长度参数还没收齐。
		int r = com_recive(fd,buf+len,bufsize-len,timeout);
		if(r>0){
			len += r;
			if(buf[0] != 0x02){
				// __android_log_print(ANDROID_LOG_INFO, "kkcard","kkcard recive prefix error : 0x%02X",buf[0]);
				return -2;		//返回不是以02开头，数据不正确
			}
		}else
			return r;
	}
	int lhex=0;
	lhex = ((unsigned)(buf[1]) << 8) + buf[2];
	mlen = lhex+5;
	{
		char hexbuf[512];
		int dlen=sizeof(hexbuf);

		memset(hexbuf,0,sizeof(hexbuf));
		HexEncode(buf,len,hexbuf,&dlen);
		// __android_log_print(ANDROID_LOG_INFO, "kkcard","kkcard recive by len: len=%d,buf=%s,lhex=%d,mlen=%d",len,hexbuf,lhex,mlen);
	}
	if(lhex > 512-5){
		// __android_log_print(ANDROID_LOG_INFO, "kkcard","kkcard recive len error : %d",lhex);
		return -2;	//长度超出范围，说明格式有问题
	}
	lhex = mlen - len;
	while(lhex > 0){
		len = com_recive(fd,buf+(mlen-lhex),bufsize-(mlen-lhex),timeout);
		if(len > 0){
			lhex -= len;
			if(lhex > 0)
				continue;
			{
				char hexbuf[512];
				int dlen=sizeof(hexbuf);

				memset(hexbuf,0,sizeof(hexbuf));
				HexEncode(buf,(mlen-lhex),hexbuf,&dlen);
				// __android_log_print(ANDROID_LOG_INFO, "kkcard","kkcard recive by len finished: buf=%s",hexbuf);
			}
		}else if(len == -1){
			return -1;		//接收失败
		}else if(len == 0){
			return 0;		//接收超时
		}
	}
	tcflush(fd,TCIOFLUSH);	//清除缓冲区
	return mlen;
}

/**
 *发送键盘命令的函数，发送后会等待键盘返回结果
 * @param fd 串口句柄
 * @param cmd 命令字符串
 */
static int kkcard_send_cmd(int fd,char *cmd,int size)
{
	char chSendCmd[256],*p = chSendCmd;
	unsigned int nLen, ret;
	int i;

	tcflush(fd,TCIOFLUSH);
	memset(chSendCmd, '\0', sizeof(chSendCmd));

	nLen = size;

	*p++ = 0x02;
	*p++ = (nLen>>8)&0xFF;
	*p++ = nLen&0xFF;
	for(i=0;i<size;i++){
		*p++ = cmd[i];
		// __android_log_print(ANDROID_LOG_DEBUG,"kkcard","cmd[%d]=0x%02X",i,(char)cmd[i]);
	}
	*p++ = 0x03;
	ret = BCC(chSendCmd, size+4);
	*p++ = ret&0xFF;

	int r = com_write(fd, chSendCmd, size+5);
	// __android_log_print(ANDROID_LOG_DEBUG,"kkcard","sendCmd(\\x%02X\\x%02X\\x%02X【%s】\\x03\\x%02X) return %d"
	//		,0x02,(nLen>>8)&0xFF,nLen&0xFF, cmd, ret&0xFF,r);
	return ret;
}

/**
 * 世融通读卡器 ,发送命令的函数，发送后会等待设备返回结果
 * @param fd 串口句柄
 * @param cmd 命令字符串
 */
static int rfm13_card_send_cmd(int fd,char *cmd,int size)
{
	char chSendCmd[256],*p = chSendCmd;
	unsigned int nLen, ret;
	int i;

	tcflush(fd,TCIOFLUSH);
	memset(chSendCmd, '\0', sizeof(chSendCmd));

	nLen = size;

	*p++ = 0x02;
	*p++ = 0x00;
	*p++ = nLen&0xFF;
	for(i=0;i<size;i++){
		*p++ = cmd[i];
		// __android_log_print(ANDROID_LOG_DEBUG,"kkcard","cmd[%d]=0x%02X",i,(char)cmd[i]);
	}


	ret = BCC(chSendCmd+1, size+2);
	*p++ = ret&0xFF;

	int r = com_write(fd, chSendCmd, size+4);

//	for(i=0;i<size+4;i++){
//		 __android_log_print(ANDROID_LOG_DEBUG,"kkcard","cmd[%d]=0x%02X",i,(char)chSendCmd[i]);
//	}
	return r;
}

/**
 * 接收信息直到收到定长数据，接收三次如果如果还没收到则返回失败
 * 返回的第一个字符为标志，第二三字符为十六进制表示的数据长度
 * @param fd 串口句柄
 * @param timeout 每次接收的超时时间
 */
static int rfm13_card_recv_by_len(int fd,char *buf,int bufsize,int timeout)
{
	int len=0;
	int mlen = 0;
	//从键盘获得结果
	memset(buf,0,sizeof(buf));
	len = com_recive(fd,buf,bufsize,timeout);
	while(len < 3){
		//如果接收的长度小于3的时候说明长度参数还没收齐。
		int r = com_recive(fd,buf+len,bufsize-len,timeout);
		if(r>0){
			len += r;
			if(buf[0] != 0x02){
				// __android_log_print(ANDROID_LOG_INFO, "kkcard","kkcard recive prefix error : 0x%02X",buf[0]);
				return -2;		//返回不是以02开头，数据不正确
			}
		}else
			return r;
	}
	int lhex=0;
	lhex = buf[2];
	mlen = lhex+4;
	{
		char hexbuf[512];
		int dlen=sizeof(hexbuf);

		memset(hexbuf,0,sizeof(hexbuf));
		HexEncode(buf,len,hexbuf,&dlen);
		// __android_log_print(ANDROID_LOG_INFO, "kkcard","kkcard recive by len: len=%d,buf=%s,lhex=%d,mlen=%d",len,hexbuf,lhex,mlen);
	}
	if(lhex > 512-5){
		// __android_log_print(ANDROID_LOG_INFO, "kkcard","kkcard recive len error : %d",lhex);
		return -2;	//长度超出范围，说明格式有问题
	}
	lhex = mlen - len;
	while(lhex > 0){
		len = com_recive(fd,buf+(mlen-lhex),bufsize-(mlen-lhex),timeout);
		if(len > 0){
			lhex -= len;
			if(lhex > 0)
				continue;
			{
				char hexbuf[512];
				int dlen=sizeof(hexbuf);

				memset(hexbuf,0,sizeof(hexbuf));
				HexEncode(buf,(mlen-lhex),hexbuf,&dlen);
				__android_log_print(ANDROID_LOG_INFO, "kkcard","kkcard recive by len finished: buf=%s",hexbuf);
			}
		}else if(len == -1){
			return -1;		//接收失败
		}else if(len == 0){
			return 0;		//接收超时
		}
	}
	tcflush(fd,TCIOFLUSH);	//清除缓冲区
	return mlen;
}



/*
 * 峰华MF-E读卡器 ,发送命令的函数，发送后会等待设备返回结果
 * @param fd 串口句柄
 * @param cmd 命令字符串
 */
static int mf30_card_send_cmd(int fd,char *cmd,int size)
{
	char chSendCmd[256],*p = chSendCmd;
	unsigned int nLen, ret;
	int i;
	int bcclength = 0;

	tcflush(fd,TCIOFLUSH);
	memset(chSendCmd, '\0', sizeof(chSendCmd));

	nLen = size+3;

	*p++ = 0xAA;
	*p++ = 0xBB;
	*p++ = nLen&0xFF;
	*p++ = 0x00;
	*p++ = 0x00;
	*p++ = 0x00;
	for(i=0;i< size;i++){
		*p++ = cmd[i];
		if(cmd[i] == 0xAA){
			*p++ = 0x00;
			bcclength++;
		}
		// __android_log_print(ANDROID_LOG_DEBUG,"kkcard","cmd[%d]=0x%02X",i,(char)cmd[i]);
	}

	ret = BCC(chSendCmd+3, size+3+bcclength);
	*p++ = ret&0xFF;

	int r = com_write(fd, chSendCmd, size+7+bcclength);

//	for(i=0;i<size+7+bcclength;i++){
//		 __android_log_print(ANDROID_LOG_DEBUG,"kkcard","cmd[%d]=0x%02X",i,(char)chSendCmd[i]);
//	}
	return r;
}

/**
 * 接收信息直到收到定长数据，接收三次如果如果还没收到则返回失败
 * 返回的第一个字符为标志，第二三字符为十六进制表示的数据长度
 * @param fd 串口句柄
 * @param timeout 每次接收的超时时间
 */
static int mf30_card_recv_by_len(int fd,char *buf,int bufsize,int timeout)
{
	int len=0;
	int mlen = 0;
	//从键盘获得结果
	memset(buf,0,sizeof(buf));
	len = com_recive(fd,buf,bufsize,timeout);
	while(len < 3){
		//如果接收的长度小于3的时候说明长度参数还没收齐。
		int r = com_recive(fd,buf+len,bufsize-len,timeout);
		if(r>0){
			len += r;
			if(buf[0] != 0xAA){
				// __android_log_print(ANDROID_LOG_INFO, "kkcard","kkcard recive prefix error : 0x%02X",buf[0]);
				return -2;		//返回不是以02开头，数据不正确
			}
		}else
			return r;
	}
	int lhex=0;
	lhex = buf[2];
	mlen = lhex+4;
	{
		char hexbuf[512];
		int dlen=sizeof(hexbuf);

		memset(hexbuf,0,sizeof(hexbuf));
		HexEncode(buf,len,hexbuf,&dlen);
		//__android_log_print(ANDROID_LOG_INFO, "kkcard","kkcard recive by len: len=%d,buf=%s,lhex=%d,mlen=%d",len,hexbuf,lhex,mlen);
	}
	if(lhex > 512-5){
		// __android_log_print(ANDROID_LOG_INFO, "kkcard","kkcard recive len error : %d",lhex);
		return -2;	//长度超出范围，说明格式有问题
	}
	lhex = mlen - len;
	while(lhex > 0){
		len = com_recive(fd,buf+(mlen-lhex),bufsize-(mlen-lhex),timeout);
		if(len > 0){
			lhex -= len;
			if(lhex > 0)
				continue;
			{
				char hexbuf[512];
				int dlen=sizeof(hexbuf);

				memset(hexbuf,0,sizeof(hexbuf));
				HexEncode(buf,(mlen-lhex),hexbuf,&dlen);
				//__android_log_print(ANDROID_LOG_INFO, "kkcard","kkcard recive by len finished: buf=%s",hexbuf);
			}
		}else if(len == -1){
			return -1;		//接收失败
		}else if(len == 0){
			return 0;		//接收超时
		}
	}
	tcflush(fd,TCIOFLUSH);	//清除缓冲区
	return mlen;
}

KKCARD_HANDLE kkcard_find(HKKC env,HKKC obj,char *path,char *filter,char *arg)
{
	DIR* dir_info; //目录指针
	struct dirent* dir_entry; //目录项信息指针
	//打开一个待扫描的目录
	dir_info = opendir(path);
	if( dir_info )
	{
		//打开目录成功
		while ( (dir_entry = readdir(dir_info)) != NULL)
		{
			//忽略这两个特殊项目
			if(strcmp(dir_entry->d_name, "..")==0 || strcmp(dir_entry->d_name, ".")==0)
				continue;
			if(dir_entry->d_type != DT_DIR){
				if(strstr(dir_entry->d_name,filter)){
					char a[256];
					memset(a,0,sizeof(a));
					sprintf(a,"%s/%s,%s",path,dir_entry->d_name,arg);
					// __android_log_print(ANDROID_LOG_DEBUG,"kkc","find device : %s",a);
					KKCARD_HANDLE kkc = kkcard_open(env,obj,a);
					if(kkc){
						char v[128];
						if(kkcard_reset(kkc,env,obj,v,3000)){
							strcpy(kkc->port,a);
							kkcard_event(kkc,env,obj,CE_FIND_PORT,"{success:true,cardport:\"%s\"}",kkc->port);
							kkcard_event(kkc,env,obj,CE_DEVICE_VER,"{success:true,cardver:\"%s\"}",v);
							closedir(dir_info);
							strcpy(kkc->version,v);
							strcpy(kkc->sn,"");
							// __android_log_print(ANDROID_LOG_DEBUG,"kkc","ok");
							return kkc;
						}else{
							kkcard_close(kkc,env,obj);
							kkc = NULL;
							// __android_log_print(ANDROID_LOG_DEBUG,"kkc","fail");
						}
					}else{
						// __android_log_print(ANDROID_LOG_DEBUG,"kkc","Open %s error",a);
					}
				}
			}
		}
		//使用完毕，关闭目录指针。
		closedir(dir_info);
	}
	return NULL;
}

/**
 * 打开密码键盘，返回密码键盘的句柄
 * @param arg 串口参数字符串，字符串格式为:	com=/dev/ttyUSB0(串口设备字符串),s=9600(波特率),p=N(奇偶校验),b=1(停止位),d=8(数据位数)
 */
KKCARD_HANDLE kkcard_open(HKKC env,HKKC obj,char *arg)
{
	int fd = com_open(arg);
	if(fd > 0){
		KKCARD_HANDLE kkc = (KKCARD_HANDLE)malloc(sizeof(KKCARD_DATA));
		if(kkc){
			memset(kkc,0,sizeof(KKCARD_DATA));
			kkc->fd = fd;
			kkc->mode = 1;
			kkc->max_pin_len = 6;
			strcpy(kkc->port,arg);
			#ifdef	JNI_DEBUG
			{
				char buf[256];
				sprintf(buf,"kkc opened,fd=%d",kkc->fd);
				kkcard_event(kkc,env,obj,KE_OPENED,buf);
			}
			#endif
			in_read_card = FALSE;
			return kkc;
		}else{
			#ifdef	JNI_DEBUG
			{
				char buf[256];
				sprintf(buf,"kkc malloc fail,fd closed,fd=%d",fd);
				kkcard_event(NULL,env,obj,KE_OPENED,buf);
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
void kkcard_close(KKCARD_HANDLE kkc,HKKC env,HKKC obj)
{
	in_read_card = FALSE;
	if(kkc){
		if(kkc->fd)
			com_close(kkc->fd);
		free(kkc);
	}
}

/**
 * 程序复位自检（不破坏密钥区）
 * @param fd 串口句柄
 * @param timeout 等待自检完成的时间，单位秒
 */
int kkcard_reset(KKCARD_HANDLE kkc,HKKC env,HKKC obj,char *v,int timeout)
{
	char buf[512];
	if(!kkc)return FALSE;
	if(kkcard_send_cmd(kkc->fd,"\x30\x40",2)>0){
		int r = kkcard_recv_by_len(kkc->fd,buf,sizeof(buf),timeout);
		if(r > 0){
			int lhex = ((unsigned)(buf[1]) << 8) + buf[2];
			memset(v,0,lhex);
			memcpy(v,buf+5,lhex - 7);
			// __android_log_print(ANDROID_LOG_DEBUG,"kkcard","buf=%s,version=%s",buf,v);
			kkcard_event(kkc,env,obj,CE_RESETED,"{success:true,msg:\"%s\"}","KKC reseted");
			return TRUE;
		}else{
			kkcard_event(kkc,env,obj,CE_WARNING,"{success:true,msg:\"%s\"}","KKC reseted fail");
		}
	}
	return FALSE;
}

int kkcard_pop_card(KKCARD_HANDLE kkc,HKKC env,HKKC obj,int timeout)
{
	char buf[512];
	if(!kkc)return FALSE;
	if(kkcard_send_cmd(kkc->fd,"\x32\x40",2)>0){
		int r = kkcard_recv_by_len(kkc->fd,buf,sizeof(buf),timeout);
		if(r > 0){
			int s1 = buf[5];
			return s1 == 0x59;
		}else{
			kkcard_event(kkc,env,obj,CE_WARNING,"{success:true,msg:\"%s\"}","KKC pop card fail");
		}
	}
	return FALSE;
}

unsigned int kkcard_m1_sn(KKCARD_HANDLE kkc,HKKC env,HKKC obj,int timeout)
{
	char buf[512];
	if(!kkc)return FALSE;
	if(kkcard_send_cmd(kkc->fd,"\x34\x31",2)>0){
		int r = kkcard_recv_by_len(kkc->fd,buf,sizeof(buf),timeout);
		if(r > 0){
			int s1 = buf[5];
			if(s1 == 'Y'){
				unsigned i;
				i = buf[6]<<24 + buf[7]<<16 + buf[8]<<8 + buf[9];
				return i;
			}
		}else{
			kkcard_event(kkc,env,obj,CE_WARNING,"{success:true,msg:\"%s\"}","KKC mq_sn seted fail");
		}
	}
	return 0;
}



//验证扇区密码
//成功返回0，失败返回0x30(寻不到射频卡) 0x33(密码错误)
int kkcard_m1_VerifyPasswd(KKCARD_HANDLE kkc, HKKC env, HKKC obj,int iSectionNo, int isKey, const char *chKeyData,int ilen)
{

	unsigned char ucWriteData[12] = {0};
	unsigned char ucReadData[1024] = {0};
	int iret=0;
	//isKey 为真 验证key_a密码 否则验证key_b密码
	ucWriteData[0] = 0x34;
	ucWriteData[1] = isKey ? 0x32 : 0x39;
	ucWriteData[2] = iSectionNo&0xff;

	if(ilen > 6) return -1;
	memcpy(ucWriteData + 3, chKeyData, ilen);

	if(kkcard_send_cmd(kkc->fd,(char *)ucWriteData,ilen+3)>0){
		int iReturn = kkcard_recv_by_len(kkc->fd,ucReadData,sizeof(ucReadData),5000);
		if(iReturn>0 && ucReadData[6]==0x59){
			kkcard_event(kkc,env,obj,CE_WARNING,"{success:true,msg:\"%s\"}","succ verifypasswd");
			iret= 0;

		}else{
			kkcard_event(kkc,env,obj,CE_ERROR,"{success:true,msg:\"%s\"}","fail to verifypasswd");
		}
	}
	else
		return -1;
	return iret;

}


//初始化值
//成功返回0，失败返回0X31(操作扇区号错) 0x33(密码验证错) 0x4E(操作失败)
int kkcard_m1_InitValue(KKCARD_HANDLE kkc, HKKC env, HKKC obj,int iSectionNo, int iBlockNo, int iValue)
{
	int iReturn = 0,iVerifyRet,iLen;
	unsigned char ucWriteData[22] = {0x00, 0x08, 0x34, 0x36};
	unsigned char ucReadData[1024] = {0};
	unsigned char ucKeyA[20] = { 0xff, 0xff, 0xff, 0xff, 0xff, 0xff };
	iLen = 6;

	ucWriteData[4] = (char)iSectionNo;
	ucWriteData[5] = (char)iBlockNo;
	ucWriteData[6] = iValue&0xff;
	ucWriteData[7] = (iValue>>8)&0xff;
	ucWriteData[8] = (iValue>>16)&0xff;
	ucWriteData[9] = (iValue>>24)&0xff;

	// 必须验证后才能 做读写的操作，  验证码 默认为 0xff 0xff 0xff 0xff 0xff 0xff
	iVerifyRet = kkcard_m1_VerifyPasswd(kkc, env, obj, iSectionNo, 1,(const char *) ucKeyA, iLen);
	if(iVerifyRet!=0){
		return -1;
	}

	if(kkcard_send_cmd(kkc->fd,(char *)ucWriteData,10)>0){
		iReturn = kkcard_recv_by_len(kkc->fd,ucReadData,sizeof(ucReadData),5000);
	}
	//0x59 表示操作成功
	if(0 == iReturn && ucWriteData[7]==0x59)	return 0;
	else	return -1;
}


/*更改值
 * iSectionNo 扇区号  ，iBlockNo 块 ，flag 1增值 0减值 ，ivalue 要增减的数值
 * 成功返回0，失败返回0X30(寻不到RF卡) 0x31(操作扇区号错) 0x32(操作的卡序列号错) 0X33(密码验证错) 0x34(块数据格式错误) 0x35(增值溢出)
*/
int kkcard_m1_UpdateValue(KKCARD_HANDLE kkc, HKKC env, HKKC obj,int iSectionNo, int iBlockNo, int flag, int iValue)
{
	int iReturn = 0,iVerifyRet,iLen;
	unsigned char ucWriteData[22] = {0x00, 0x08, 0x34};
	unsigned char ucReadData[1024] = {0};
	unsigned char ucKeyA[20] = { 0xff, 0xff, 0xff, 0xff, 0xff, 0xff };
	iLen = 6;

	ucWriteData[3] = flag ? 0x37 : 0x38;
	ucWriteData[4] = (char)iSectionNo;
	ucWriteData[5] = (char)iBlockNo;
	ucWriteData[6] = iValue&0xff;
	ucWriteData[7] = (iValue>>8)&0xff;
	ucWriteData[8] = (iValue>>16)&0xff;
	ucWriteData[9] = (iValue>>24)&0xff;

	// 必须验证后才能 做读写的操作，  验证码 默认为 0xff 0xff 0xff 0xff 0xff 0xff
	iVerifyRet = kkcard_m1_VerifyPasswd(kkc, env, obj, iSectionNo, 1,(const char *) ucKeyA, iLen);
	if(iVerifyRet!=0){
		return -1;
	}

	if(kkcard_send_cmd(kkc->fd,(char *)ucWriteData,10)>0){
		iReturn = kkcard_recv_by_len(kkc->fd,ucReadData,sizeof(ucReadData),5000);
	}
	//0x59 表示操作成功
	if(0 == iReturn && ucWriteData[7]==0x59)	return 0;
	else	return -1;
}




//读取指定扇区指定块数据
//成功返回0，失败返回0x30(寻不到RF卡) 0x31(操作扇区号错) 0x32(操作的卡序列号错) 0x33(密码验证错) 0x34(读数据错)
//返回字符格式 0x02 0x00 0x15 0x34 0x33 扇区号 块号 操作状态字P 16byte数据  0x03 BCC
int kkcard_m1_ReadSection(KKCARD_HANDLE kkc,HKKC env,HKKC obj,int iSectionNo, int iBlockNo,char *chData, int iLen,int timeout)
{
	int iReturn = 0,iVerifyRet;
	unsigned char ucWriteData[6] = {0x34, 0x33};
	unsigned char ucReadData[1024] = {0};

	ucWriteData[2] = (char)iSectionNo;
	ucWriteData[3] = (char)iBlockNo;

	unsigned char ucKeyA[20] = { 0xff, 0xff, 0xff, 0xff, 0xff, 0xff };


	memset(ucReadData,0,sizeof(ucReadData));
	// 必须验证后才能 做读写的操作，  验证码 默认为 0xff 0xff 0xff 0xff 0xff 0xff
	iVerifyRet = kkcard_m1_VerifyPasswd(kkc, env, obj, iSectionNo, 1,(const char *) ucKeyA, 6);
	if(iVerifyRet!=0){
		return -1;
	}

	if(kkcard_send_cmd(kkc->fd,( char *)ucWriteData,4)>0){
		iReturn = kkcard_recv_by_len(kkc->fd,ucReadData,sizeof(ucReadData),timeout);
	}
	if(iReturn>6)
	{
		iReturn = ucReadData[7];
		if(iReturn==0x59 && iLen>16)
		{
			// 成功接收，则只将有用的16byte数据传回
			memcpy(chData, ucReadData + 8, 16);
		}
	}
	else	return -1;

	return 0;
}



//写卡数据
int kkcard_m1_WriteData(KKCARD_HANDLE kkc,HKKC env,HKKC obj,int iSectionNo, int iBlockNo, const char *chData, int iLen,int timeout)
{


	int iReturn = 0,iVerifyRet;
	unsigned char ucWriteData[25] = {0x34, 0x34};
	unsigned char ucReadData[1024] = {0};
	unsigned char ucKeyA[20] = { 0xff, 0xff, 0xff, 0xff, 0xff, 0xff };
	iLen = 6;

	// 必须验证后才能 做读写的操作，  验证码 默认为 0xff 0xff 0xff 0xff 0xff 0xff
	iVerifyRet = kkcard_m1_VerifyPasswd(kkc, env, obj, iSectionNo, 1,(const char *) ucKeyA, iLen);
	if(iVerifyRet!=0){
		return -1;
	}

	if(iLen > 16)		return -1;
	ucWriteData[2] = (char)iSectionNo;
	ucWriteData[3] = (char)iBlockNo;
	memcpy(ucWriteData + 4, chData, iLen);
	if(kkcard_send_cmd(kkc->fd,( char *)ucWriteData,4)>0){
		iReturn = kkcard_recv_by_len(kkc->fd,ucWriteData,iLen+4,timeout);
	}

	//0x59 表示操作成功
	if(0 == iReturn && ucWriteData[7]==0x59)	return 0;
	else	return -1;

}


/**
 * 磁卡读写测试函数
 */
int kkcard_m1_readcardno(KKCARD_HANDLE kkc, HKKC env, HKKC obj,char *chdata,int timeout)
{ //RF—M1卡

	int ire,iret=1;
	char hexbuf[128];
	memset(hexbuf,0,sizeof(hexbuf));
	ire=kkcard_m1_ReadSection(kkc,env,obj,15, 0,hexbuf, sizeof(hexbuf),5000);
	if(ire==0){
		memcpy(chdata,hexbuf,8);
	}else{
		memcpy(chdata,0,8);
		iret=-1;
		return FALSE;
	}
	strcat(chdata,"^^");

	memset(hexbuf,0,sizeof(hexbuf));
	ire=kkcard_m1_ReadSection(kkc,env,obj,15, 1,hexbuf, sizeof(hexbuf),5000);
	kkcard_event(kkc,env,obj,CE_WARNING,"CARNO BUFF1=%s",hexbuf);
	if(ire==0){
		strncat(chdata,hexbuf,15);	//纠正下之前的错误，卡号只有15位，第16位为 "?"号
	}else{
		strcat(chdata,"00000000");
		//kkcard_event(kkc,env,obj,CE_ERROR,"fail to read card (iSectionNo:15 BlockNo:1)  in kkcard_m1_readcardno");
		iret=-1;
		return FALSE;
	}
	strcat(chdata,"^^");

	memset(hexbuf,0,sizeof(hexbuf));
	ire=kkcard_m1_ReadSection(kkc,env,obj,15, 2,hexbuf, sizeof(hexbuf),5000);
	if(ire==0){
		strcat(chdata,hexbuf);
	}else{
		strcat(chdata,"00000000");
		//kkcard_event(kkc,env,obj,CE_ERROR,"fail to read card (iSectionNo:15 BlockNo:2)  in kkcard_m1_readcardno");
		iret=-1;
		return FALSE;
	}

	return iret?TRUE:FALSE;
}

int kkcard_magcard(KKCARD_HANDLE kkc,HKKC env,HKKC obj,char *cardno,int timeout)
{
	char buf[512];
	if(!kkc)return FALSE;
	if(kkcard_send_cmd(kkc->fd,"\x3B\x35",2)>0){
		int r = kkcard_recv_by_len(kkc->fd,buf,sizeof(buf),timeout);
		if(r > 0){
			int len = ((unsigned)(buf[1]) << 8) + buf[2];
			if(buf[3] == 0x3B && buf[4] == 0x35){
				if(buf[5] == 0x59){
//					char *p = buf+6,*pc=cardno;
//					memset(cardno,0,len);
//					while(*p != 0x00)p++;
//					p++;
//					while(*p != '=')*pc++ = *p++;
//					*pc = '\0';
					int i=0;
					memset(cardno,0,len+1);
					memcpy(cardno,buf+6,len);
					for(i=0;i<len;i++){
						if(cardno[i] == 0x00)cardno[i] = '&';
					}
					// __android_log_print(ANDROID_LOG_DEBUG,"kkcard","data=%s",cardno);
					int times = 0;
					while(times < 3){
					    int r = kkcard_clearcard(kkc,env,obj,3000);
					    if(r == 0){
					    	times++;
					    }
					    else if(r == 1){
					    	break;
					    }
					}
					return TRUE;
				}else if(buf[5] == 0x4E && buf[6] == 0xE1 && buf[7] == 0x00 && buf[8] != 0xE2){
					int i=0;
					char *p = buf+6,*pc=cardno;
					memset(cardno,0,len);
					while(*p != 0x00)p++;
					p++;
					strcat(cardno,"&");
					memcpy(cardno+1,buf+8,len-3);
					for(i=0;i<len;i++){
						if(cardno[i] == 0x00)cardno[i] = '&';
						if(cardno[i] == 0xE1 || cardno[i] == 0xE2 ||cardno[i] == 0xE3)cardno[i] = ' ';
						if(cardno[i] == 0x03)cardno[i] = '\0';
					}
//					while(*p != '=')*pc++ = *p++;
//					*pc = '\0';
					// __android_log_print(ANDROID_LOG_DEBUG,"kkcard","data=%s",cardno);

					return TRUE;
				}else{
					// __android_log_print(ANDROID_LOG_DEBUG,"kkcard","读卡错误");
				}
			}
		}else{
			//kkcard_event(kkc,env,obj,CE_WARNING,"KKC magcard fail");
		}
	}

	if(kkcard_send_cmd(kkc->fd,"\x3B\x36",2)>0){
		// __android_log_print(ANDROID_LOG_DEBUG,"kkcard","清除磁卡标志成功");
    }
	return FALSE;
}


int kkcard_readcpucard(KKCARD_HANDLE kkc,HKKC env,HKKC obj,char *cardno,int timeout)
{
	char buf[512];
	memset(buf,0,sizeof(buf));
	if(!kkc)return FALSE;
	if(kkcard_send_cmd(kkc->fd,"\x37\x40",2)>0){
		int r = kkcard_recv_by_len(kkc->fd,buf,sizeof(buf),timeout);
		if(r > 0){
			int len = ((unsigned)(buf[1]) << 8) + buf[2];
			int nLen = ((unsigned)(buf[6]) << 8) + buf[7];
			if(buf[3] == 0x37 && buf[4] == 0x40){
				if(buf[5] == 0x59){
//					char *p = buf+6,*pc=cardno;
//					memset(cardno,0,len);
//					while(*p != 0x00)p++;
//					p++;
//					while(*p != '=')*pc++ = *p++;
//					*pc = '\0';
					memset(cardno,0,len+1);
					memcpy(cardno,buf+8,nLen);
					//__android_log_print(ANDROID_LOG_DEBUG,"kkcard","len=%d",nLen);

					//CPU卡下电操作
					kkcard_dormancycard(kkc,env,obj,3000);
					return nLen;
				}else if(buf[5] == 0x4E ){
					return FALSE;
				}else{
					// __android_log_print(ANDROID_LOG_DEBUG,"kkcard","读卡错误");
				}
			}
		}else{
			//kkcard_event(kkc,env,obj,CE_WARNING,"KKC magcard fail");
		}
	}

	if(kkcard_send_cmd(kkc->fd,"\x37\x42",2)>0){
		// __android_log_print(ANDROID_LOG_DEBUG,"kkcard","CPU卡接触式IC卡下成功");
    }
	return FALSE;
}


int kkcard_popcard(KKCARD_HANDLE kkc,HKKC env,HKKC obj,char *cb,int timeout)
{
	char buf[512];
	if(!kkc)return FALSE;
	if(kkcard_send_cmd(kkc->fd,"\x32\x40",2)>0){
		int r = kkcard_recv_by_len(kkc->fd,buf,sizeof(buf),timeout);
		if(r > 0){
			if(buf[5] == 0x59){
				return TRUE;
			}else if(buf[5] == 0x4E){
				kkcard_event(kkc,env,obj,CE_WARNING,"弹卡失败");
				return FALSE;
			}
		}
	}
	return FALSE;
}

//CPU卡休眠下电操作
int kkcard_dormancycard(KKCARD_HANDLE kkc,HKKC env,HKKC obj,int timeout)
{
	char buf[512];
	if(!kkc)return FALSE;
	if(kkcard_send_cmd(kkc->fd,"\x37\x42",2)>0){
		int r = kkcard_recv_by_len(kkc->fd,buf,sizeof(buf),timeout);
		if(r > 0){
			if(buf[5] == 0x59){
				return TRUE;
			}else if(buf[5] == 0x4E){
				kkcard_event(kkc,env,obj,CE_WARNING,"操作失败");
				return FALSE;
			}
			else if(buf[5] == 0x45){
				kkcard_event(kkc,env,obj,CE_WARNING,"卡机内无卡");
			    return FALSE;
		    }
		}
	}
	return FALSE;
}

//清除磁卡标记
int kkcard_clearcard(KKCARD_HANDLE kkc,HKKC env,HKKC obj,int timeout)
{
	char buf[512];
	if(!kkc)return FALSE;
	if(kkcard_send_cmd(kkc->fd,"\x3B\x36",2)>0){
		int r = kkcard_recv_by_len(kkc->fd,buf,sizeof(buf),timeout);
		if(r > 0){
			if(buf[5] == 0x59){
				return TRUE;
			}else if(buf[5] == 0x4E){
				kkcard_event(kkc,env,obj,CE_WARNING,"清理磁卡标志失败");
				return FALSE;
			}
		}
	}
	return FALSE;
}
static int kkcard_handle_status(KKCARD_HANDLE kkc,HKKC env,HKKC obj,char *buf,char *cb,int r)
{
	char hexbuf[512];
	int dlen=sizeof(hexbuf);

	memset(hexbuf,0,sizeof(hexbuf));
	HexEncode(buf,r,hexbuf,&dlen);
	// __android_log_print(ANDROID_LOG_INFO, "kkcard","Read card: buf=%s",hexbuf);
	//sprintf(str,"%s('%c');",cb,buf);

	if(buf[3] == 0x31 && buf[4] == 0x44){
		int s1,s2,s3,s4;
		char *p= buf+5;

		s1 = *p++;
		s2 = *p++;
		s3 = *p++;
		s4 = *p++;
		if(s1 == 0x31 || s1 == 0x30){
			switch(s2){
			case 0x3F: // 卡机内无卡或卡机内有未知类型卡
				kkcard_event(kkc,env,obj,CE_BRUSHED,"{status:\"0x%02X\",msg:\"%s\"}",s2,"卡机内无卡或卡机内有未知类型卡");
				kkcard_popcard(kkc,env,obj,cb,3000);
				break;
			case 0x31: // 接触式cpu卡
				kkcard_event(kkc,env,obj,CE_BRUSHED,"{status:\"0x%02X\",msg:\"%s\"}",s2,"接触式CPU卡");
				memset(hexbuf,0,sizeof(hexbuf));
				int r = kkcard_readcpucard(kkc,env,obj,hexbuf,5000);
				if(r > 0){
					//读卡成功，hexbuf中读到的是卡片数据，将退出读卡程序

					char szTemp[512];
					memset(szTemp,0,sizeof(szTemp));
					int ilen1= sizeof(szTemp);
					HexEncode((unsigned char*)hexbuf,r,&szTemp[0],&ilen1);

					kkcard_event(kkc,env,obj,CE_BRUSHED,"{success:true,status:\"0x%02X\",callback:\"%s\",cardno:\"%s\"}",s2,cb,szTemp);
					kkcard_popcard(kkc,env,obj,cb,3000);
					return TRUE;
				}else{
					//并不退出读卡程序
					kkcard_event(kkc,env,obj,CE_BRUSHED,"{success:false,status:\"0x%02X\",msg:\"读卡失败，请重新插卡\",callback:\"%s\",cardno:\"%s\"}",s2,cb,hexbuf);
					kkcard_popcard(kkc,env,obj,cb,3000);
					return FALSE;
				}
				break;
			case 0x32: // RF--TYPE B CPU卡
				kkcard_event(kkc,env,obj,CE_BRUSHED,"{status:\"0x%02X\",msg:\"%s\"}",s2,"RF--TYPE B CPU卡");
				kkcard_popcard(kkc,env,obj,cb,3000);
				break;
			case 0x33: // RF—TYPE A CPU卡
				kkcard_event(kkc,env,obj,CE_BRUSHED,"{status:\"0x%02X\",msg:\"%s\"}",s2,"RF--TYPE A CPU卡");
				kkcard_popcard(kkc,env,obj,cb,3000);
				break;
			case 0x34: // RF—M1卡
				kkcard_event(kkc,env,obj,CE_BRUSHED,"{status:\"0x%02X\",msg:\"%s\"}",s2,"RF-M1卡");
				//下面读取IC卡的信息
				//int sn = kkcard_m1_sn(kkc,env,obj,5000);
				memset(hexbuf,0,sizeof(hexbuf));
				if(kkcard_m1_readcardno(kkc,env,obj,hexbuf,5000)){
					kkcard_event(kkc,env,obj,CE_BRUSHED,"{success:true,status:\"0x%02X\",callback:\"%s\",cardno:\"%s\"}",s2,cb,hexbuf);
				}else{
					kkcard_event(kkc,env,obj,CE_ERROR,"{success:false,status:\"0x%02X\",msg:\"读卡失败，请重新贴卡\",cardno:\"%s\"}",s2,hexbuf);
					return FALSE;
				}
				//kkcard_m1_test(kkc,env,obj,5);
				return TRUE;
			case 0x37: // 磁卡
				kkcard_event(kkc,env,obj,CE_BRUSHED,"{status:\"0x%02X\",msg:\"%s\"}",s2,"磁卡");
				memset(hexbuf,0,sizeof(hexbuf));
				if(kkcard_magcard(kkc,env,obj,hexbuf,5000)){
					//读卡成功，hexbuf中读到的是卡片数据，将退出读卡程序
					kkcard_event(kkc,env,obj,CE_BRUSHED,"{success:true,status:\"0x%02X\",callback:\"%s\",cardno:\"%s\"}",s2,cb,hexbuf);
					return TRUE;
				}else{
					//并不退出读卡程序
					kkcard_event(kkc,env,obj,CE_BRUSHED,"{success:false,status:\"0x%02X\",msg:\"刷卡失败，请重新刷卡\",callback:\"%s\",cardno:\"%s\"}",s2,cb,hexbuf);
					return FALSE;
				}
			case 0x38: // 磁卡和M1卡
				kkcard_event(kkc,env,obj,CE_BRUSHED,"{status:\"0x%02X\",msg:\"%s\"}",s2,"磁卡和M1卡");
				break;
			case 0x39: // 磁卡和接触式cpu卡
				kkcard_event(kkc,env,obj,CE_BRUSHED,"{status:\"0x%02X\",msg:\"%s\"}",s2,"磁卡和接触式cpu卡");
				kkcard_popcard(kkc,env,obj,cb,3000);
				break;
			case 0x3A: // 接触式cpu卡和M1卡
				kkcard_event(kkc,env,obj,CE_BRUSHED,"{status:\"0x%02X\",msg:\"%s\"}",s2,"接触式cpu卡和M1卡");
				kkcard_popcard(kkc,env,obj,cb,3000);
				break;
			case 0x3B: // 磁卡、接触式cpu卡和M1卡
				kkcard_event(kkc,env,obj,CE_BRUSHED,"{status:\"0x%02X\",msg:\"%s\"}",s2,"磁卡、接触式cpu卡和M1卡");
				kkcard_popcard(kkc,env,obj,cb,3000);
				break;
			default :
				kkcard_event(kkc,env,obj,CE_BRUSHED,"{status:\"0x%02X\",msg:\"%s\"}",s2,"未知代码");
				break;
			}
		}
	}
	return FALSE;
}

int kkcard_query(KKCARD_HANDLE kkc,HKKC env,HKKC obj,char *cb,int timeout)
{
	char buf[512];
	if(!kkc)return FALSE;
	if(kkcard_send_cmd(kkc->fd,"\x31\x44",2)>0){
		int r = kkcard_recv_by_len(kkc->fd,buf,sizeof(buf),timeout);
		if(r > 0){
			return kkcard_handle_status(kkc,env,obj,buf,cb,r);
		}else{
			kkcard_event(kkc,env,obj,CE_WARNING,"{success:true,msg:\"%s\"}","设备复位失败");
		}
	}
	return FALSE;
}

/**
 * 读卡事件返回：
0x02 	0x00 	0x06 	0x31 	0x44 	状态字S1	状态字S2	状态字S3	状态字S4	0x03	BCC
S1：卡座状态
S1=0x30 卡机内无卡（射频卡、磁卡不受此限制）。
S1=0x31 卡机内有卡
S2:卡类型
    S2=0x3F 卡机内无卡或卡机内有未知类型卡
S2=0x31 接触式cpu卡
S2=0x32 RF--TYPE B CPU卡
S2=0x33 RF—TYPE A CPU卡
S2=0x34 RF—M1卡
S2=0x37 磁卡
S2=0x38 磁卡和M1卡
S2=0x39 磁卡和接触式cpu卡
S2=0x3A 接触式cpu卡和M1卡
S2=0x3B 磁卡、接触式cpu卡和M1卡

…
S3：卡状态
    S3=0x30 下电状态
S3=0x32 激活状态
S4：按键状态
    S4=0x30 无按键按下
    S4=0x31 按键1被按下
S4=0x32	按键2被按下
S4=0x33	按键3被按下
 *
 */

int kkcard_read_card(KKCARD_HANDLE kkc,HKKC env,HKKC obj,char *cb,int timeout)
{
	char buf[512],str[512];
	int r = 0;

	if(timeout == 0)
		timeout = -1;
	if(!kkc)return 0;
	if (in_read_card) {
		// __android_log_print(ANDROID_LOG_INFO, "kkcard","already read card.");
		return 0;
	}

	in_read_card = TRUE;
	while(in_read_card){
			memset(buf,0,sizeof(buf));
			memset(str,0,sizeof(str));
			// __android_log_print(ANDROID_LOG_INFO, "kkcard","Reading card,waiting %ds...",timeout);
			r = kkcard_recv_by_len(kkc->fd,buf,sizeof(buf),timeout);
			// __android_log_print(ANDROID_LOG_INFO, "kkcard","r=%d",r);
			if(r>0){
				if(kkcard_handle_status(kkc,env,obj,buf,cb,r)){
					in_read_card = FALSE;
					break;
					//kkcard_clearcard(kkc,env,obj,timeout);
				}else{
					//kkcard_clearcard(kkc,env,obj,timeout);
					continue;
				}
			}else if(r == 0){
				// __android_log_print(ANDROID_LOG_INFO, "kkcard","Read card timeout");
				//kkcard_event(kkc,env,obj,CE_EVENT,"{result:\"%d\",msg:\"%s\"}",r,"读卡超时");
				kkcard_event(kkc,env,obj,CE_EVENT,"{status:\"0x%02X\",result:\"%d\",msg:\"%s\"}",0x10,r,"读卡超时");
				in_read_card = FALSE;
				break;
			}else if(r == -1){
				// __android_log_print(ANDROID_LOG_INFO, "kkcard","Read card error(maybe close fd),r=%d。",r);
				kkcard_event(kkc,env,obj,CE_EVENT,"{result:\"%d\",msg:\"%s\"}",r,"读卡通讯失败");
				in_read_card = FALSE;
				break;
			}else{
				kkcard_event(kkc,env,obj,CE_EVENT,"{result:\"%d\",msg:\"%s\"}",r,"其他错误");
			}
		}
	//while(in_read_card);
	//kkcard_close(kkc, env, obj);
	// __android_log_print(ANDROID_LOG_INFO, "kkcard","end read card,r=%d",r);
	kkcard_event(kkc, env, obj, CE_EVENT,"{success:false,result:\"%d\",msg:\"%s\"}", r,"kkcard exit");
	return r;
}

int serial_write(KKCARD_HANDLE kkc,HKKC env,HKKC obj,char *data,int len)
{
	if(!kkc) return FALSE;
	if(kkc->fd <= 0 )return FALSE;
	int r = com_write(kkc->fd, data, len);
	return r;
}

int serial_read(KKCARD_HANDLE kkc,HKKC env,HKKC obj,char *buf,int timeout)
{
	if(!kkc) return FALSE;
	if(kkc->fd <= 0 )return FALSE;
	int r = 0;
	r =  com_recive(kkc->fd,buf,sizeof(buf),timeout);
	return r;
}

int serial_select(KKCARD_HANDLE kkc,HKKC env,HKKC obj,int sec,int usec)
{
	if(!kkc) return FALSE;
	if(kkc->fd <= 0 )return FALSE;
	int r = 0;
	r = com_select(kkc->fd,sec,usec);
	return r;
}

int kkcard_ring(KKCARD_HANDLE kkc,HKKC env,HKKC obj, int timeout)
{
	if(!kkc)return FALSE;

	char buf[512];
	char data[512];

	memset(buf,0,sizeof(buf));
	memset(data,0,sizeof(data));

	data[0] = 0x2C;
	data[1] = 0x09;

	if(rfm13_card_send_cmd(kkc->fd,data,2)>0){
		int r = rfm13_card_recv_by_len(kkc->fd,buf,sizeof(buf),1000);
		if(r > 0){
			if(buf[3] == 0x00){
				return TRUE;
			}
		}
	}
	return FALSE;
}

int kkcard_read_rfm13_id(KKCARD_HANDLE kkc,HKKC env,HKKC obj, int timeout)
{
	 if(!kkc)return 0;

	clock_t start, finish,internalTime;
	internalTime=timeout*1000;            //超时时间
	start=clock();
	//kkcard_event(kkc,env,obj,CE_START,"{success:false,msg:\"%s\"}","开始读卡");
	while (1) {
		finish = clock();
		if ((finish - start) > internalTime) {
			kkcard_event(kkc,env,obj,CE_EVENT,"{status:\"0x%02X\",msg:\"%s\"}",0x10,"read guid timeout");
			return -1;
		}

		//kkcard_event(kkc,env,obj,CE_BRUSHED,"{success:false,msg:\"%s\"}","开始查找卡");
		// 查找卡
		if(card_request(kkc,env,obj) <= 0) continue;

		//kkcard_event(kkc,env,obj,CE_BRUSHED,"{success:false,msg:\"%s\"}","开始读取序列号");

		char szIDCard[20] = {0};
		memset(szIDCard,0,sizeof(szIDCard));

		//读取卡序列号
		if(card_read_id(kkc,env,obj,szIDCard) <= 0){
			continue;
		}else{
			char szTemp[512] = {0};
			memset(szTemp,0,sizeof(szTemp));
			int ilen1= sizeof(szTemp);
			HexEncode((unsigned char*)szIDCard,4,&szTemp[0],&ilen1);

			kkcard_event(kkc,env,obj,CE_BRUSHED,"{success:true,status:\"0x%02X\",msg:\"%s\"}",0x11,szTemp);
			return TRUE;
		}
	}
	return FALSE;
}

int kkcard_getver_mf30_card(KKCARD_HANDLE kkc,HKKC env,HKKC obj, int timeout)
{
	 if(!kkc)return 0;

	clock_t start, finish,internalTime;
	internalTime=timeout*1000;            //超时时间
	start=clock();

	while (1) {
		finish = clock();
		if ((finish - start) > internalTime) {
			kkcard_event(kkc,env,obj,CE_EVENT,"{status:\"0x%02X\",msg:\"%s\"}",0x10,"get ver timeout");
			return -1;
		}

		char szCardVer[10] = {0};
		memset(szCardVer,0,sizeof(szCardVer));

		//读取版本号
		if(mf30_card_getver(kkc,env,obj,szCardVer) <= 0){
			continue;
		}else{
			char szTemp[20] = {0};
			memset(szTemp,0,sizeof(szTemp));
			int ilen1= sizeof(szTemp);
			HexEncode((unsigned char*)szCardVer,2,&szTemp[0],&ilen1);

			kkcard_event(kkc,env,obj,CE_BRUSHED,"{success:true,status:\"0x%02X\",msg:\"%s\"}",0x12,szTemp);
			return TRUE;
		}
	}
	return FALSE;
}

int kkcard_read_rfm13_card(KKCARD_HANDLE kkc,HKKC env,HKKC obj, int sectorid, int blockid,  int timeout)
{
	   if(!kkc)return 0;

		clock_t start, finish,internalTime;
	    internalTime=timeout*1000;            //超时时间
	    start=clock();
		//kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}","开始读卡");
		while (1) {
			finish = clock();
			if ((finish - start) > internalTime) {
				kkcard_event(kkc,env,obj,CE_EVENT,"{status:\"0x%02X\",msg:\"%s\"}",0x10,"read card timeout");
				return -1;
			}

			//kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}","开始查找卡");
			// 查找卡
			if(card_request(kkc,env,obj) <= 0) continue;

			//kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}","开始读取序列号");

			char szIDCard[20] = {0};
			memset(szIDCard,0,sizeof(szIDCard));

			//读取卡序列号
			if(card_read_id(kkc,env,obj,szIDCard) <=0 ) continue;

			// 序列号
			char szGuid[512] = {0};
			memset(szGuid,0,sizeof(szGuid));
			int length= sizeof(szGuid);
			HexEncode((unsigned char*)szIDCard,4,&szGuid[0],&length);

			//kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\",card:\"%s\"}","开始选卡",szGuid);
			kkcard_event(kkc,env,obj,CE_BRUSHED,"{success:true,status:\"0x%02X\",msg:\"%s\"}",0x11,szGuid);

			//选卡
			if(card_select(kkc,env,obj,szGuid) <=0 ) continue;

			//kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}","开始载入密匙");

			// 载入密匙
			if(card_loadkey(kkc,env,obj,"FFFFFFFFFFFF") <=0 ) continue;

			//kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}","开始验证密匙");
			int mode = 0;
			// 验证卡
			if(card_authentication(kkc,env,obj,mode,sectorid,szGuid) <= 0) continue;

			//kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}","开始读卡");
			char szRecvData[2048] = {0};
			memset(szRecvData,0,sizeof(szRecvData));
			// 读卡
			if(card_read(kkc,env,obj,szRecvData,blockid) <= 0){
				continue;
			}else{
				char szTemp[2048] = {0};
				memset(szTemp,0,sizeof(szTemp));
				int ilen1= sizeof(szTemp);
				HexEncode((unsigned char*)szRecvData,48,&szTemp[0],&ilen1);

				kkcard_event(kkc,env,obj,CE_BRUSHED,"{success:true,status:\"0x%02X\",cardno:\"%s\"}",0x20,szTemp);
				return TRUE;
			}
		}
		return FALSE;
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

int kkcard_write_rfm13_card(KKCARD_HANDLE kkc,HKKC env,HKKC obj, int sectorid, char *data0,int len0, char *data1, int len1, char *data2, int len2)
{
	if(!kkc)return FALSE;

	char buf[512];
	char data[512];
	char szData0[512];
	char szData1[512];
	char szData2[512];

	memset(buf,0,sizeof(buf));
	memset(data,0,sizeof(data));
	memset(szData0,0,sizeof(szData0));
	memset(szData1,0,sizeof(szData1));
	memset(szData2,0,sizeof(szData2));

	CompressAsc(data0,len0,szData0);
	CompressAsc(data1,len1,szData1);
	CompressAsc(data2,len2,szData2);

	data[0] = 0x39;
	data[1] = sectorid&0xff;
	data[2] = 0x03;
	memcpy(data+3,szData0,len0);
	memcpy(data+19,szData1,len1);
	memcpy(data+35,szData2,len2);

	//kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}","开始写卡");

	if(rfm13_card_send_cmd(kkc->fd,data,51)>0){
		int r = rfm13_card_recv_by_len(kkc->fd,buf,sizeof(buf),1000);
		if(r > 0){
			if(buf[3] == 0x00){
				return TRUE;
			}
		}
	}
	return FALSE;
}

int card_read(KKCARD_HANDLE kkc, HKKC env,HKKC obj, char *szRecvData, int sectorid)
{
	if(!kkc)return FALSE;

	char buf[512];
	char data[512];

	memset(buf,0,sizeof(buf));
	memset(data,0,sizeof(data));
	memset(szRecvData,0,sizeof(szRecvData));

	data[0] = 0x38;
	data[1] = sectorid&0xff;
	data[2] = 0x03;

	if(rfm13_card_send_cmd(kkc->fd,data,3)>0){
		int r = rfm13_card_recv_by_len(kkc->fd,buf,sizeof(buf),1000);
		if(r > 0){
			int len = buf[2];
			if(buf[3] == 0x00){
				memcpy(szRecvData,buf+4,len);
				return TRUE;
			}
		}
	}
	return FALSE;
}

int card_request(KKCARD_HANDLE kkc, HKKC env,HKKC obj)
{
	if(!kkc)return FALSE;

	char buf[512];
	char data[512];

	memset(buf,0,sizeof(buf));
	memset(data,0,sizeof(data));

	data[0] = 0x31;
	data[1] = 0x52;

	if(rfm13_card_send_cmd(kkc->fd,data,2)>0){
		int r = rfm13_card_recv_by_len(kkc->fd,buf,sizeof(buf),1000);
		if(r > 0){
			if(buf[3] == 0x00){
				return TRUE;
			}
		}
	}
	return FALSE;
}

int card_get_ver(KKCARD_HANDLE kkc, HKKC env,HKKC obj ,char *szData)
{
	if(!kkc)return FALSE;

	char buf[512];
	char data[512];

	memset(buf,0,sizeof(buf));
	memset(data,0,sizeof(data));
	memset(szData,0,sizeof(szData));

	data[0] = 0x32;
	data[1] = 0x93;

	if(rfm13_card_send_cmd(kkc->fd,data,2)>0){
		int r = rfm13_card_recv_by_len(kkc->fd,buf,sizeof(buf),1000);
		if(r > 0){
			if(buf[3] == 0x00){
				memcpy(szData,buf+4,4);
				return TRUE;
			}
		}
	}
	return FALSE;
}

int card_read_id(KKCARD_HANDLE kkc, HKKC env,HKKC obj ,char *szData)
{
	if(!kkc)return FALSE;

	char buf[512];
	char data[512];

	memset(buf,0,sizeof(buf));
	memset(data,0,sizeof(data));
	memset(szData,0,sizeof(szData));

	data[0] = 0x32;
	data[1] = 0x93;

	if(rfm13_card_send_cmd(kkc->fd,data,2)>0){
		int r = rfm13_card_recv_by_len(kkc->fd,buf,sizeof(buf),1000);
		if(r > 0){
			if(buf[3] == 0x00){
				memcpy(szData,buf+4,4);
				return TRUE;
			}
		}
	}
	return FALSE;
}

int card_select(KKCARD_HANDLE kkc, HKKC env,HKKC obj, char *szData)
{
	if(!kkc)return FALSE;

	char buf[512];
	char data[512];
	char buffer[512];

	memset(buf,0,sizeof(buf));
	memset(data,0,sizeof(data));
	memset(buffer,0,sizeof(buffer));

	CompressAsc(szData,8,buffer);

	data[0] = 0x33;
	data[1] = 0x93;
	memcpy(data+2,buffer,4);

	if(rfm13_card_send_cmd(kkc->fd,data,6)>0){
		int r = rfm13_card_recv_by_len(kkc->fd,buf,sizeof(buf),1000);
		if(r > 0){
			if(buf[3] == 0x00){
				return TRUE;
			}
		}
	}
	return FALSE;
}

int card_loadkey(KKCARD_HANDLE kkc, HKKC env,HKKC obj, char *szData)
{
	if(!kkc)return FALSE;

	char buf[512];
	char data[512];
	char buffer[512];

	memset(buf,0,sizeof(buf));
	memset(data,0,sizeof(data));
	memset(buffer,0,sizeof(buffer));

	CompressAsc(szData,12,buffer);

	data[0] = 0x35;
	memcpy(data+1,buffer,6);

	if(rfm13_card_send_cmd(kkc->fd,data,7)>0){
		int r = rfm13_card_recv_by_len(kkc->fd,buf,sizeof(buf),1000);
		if(r > 0){
			if(buf[3] == 0x00){
				return TRUE;
			}
		}
	}
	return FALSE;
}

int card_authentication(KKCARD_HANDLE kkc , HKKC env,HKKC obj,int mode, int sectorid, char *szData)
{
	if(!kkc)return FALSE;

	char buf[512];
	char data[512];
	char buffer[512];

	memset(buf,0,sizeof(buf));
	memset(data,0,sizeof(data));
	memset(buffer,0,sizeof(buffer));

	CompressAsc(szData,8,buffer);

	data[0] = 0x37;
	if(mode == 0){
		data[1] = 0x60;
	}else if(mode == 1){
        data[1] = 0x61;
	}
	data[2] = sectorid&0xff;
	memcpy(data+3,buffer,4);

	if(rfm13_card_send_cmd(kkc->fd,data,7)>0){
		int r = rfm13_card_recv_by_len(kkc->fd,buf,sizeof(buf),1000);
		if(r > 0){
			if(buf[3] == 0x00){
				return TRUE;
			}
		}
	}
	return FALSE;
}

int kkcard_read_mf30_id(KKCARD_HANDLE kkc,HKKC env,HKKC obj, int timeout)
{
	   if(!kkc)return 0;

		clock_t start, finish,internalTime;
	    internalTime=timeout*1000;            //超时时间
	    start=clock();
		while (1) {
			finish = clock();
			if ((finish - start) > internalTime) {
				kkcard_event(kkc,env,obj,CE_EVENT,"{status:\"0x%02X\",msg:\"%s\"}",0x10,"read mf30 card timeout");
				return -1;
			}

			//kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}","读取序列号_开始查找卡");
			// 查找卡
			if(mf30_card_request(kkc,env,obj) <= 0) continue;

			//kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}","读取序列号_开始读取序列号");

			char szIDCard[20] = {0};
			memset(szIDCard,0,sizeof(szIDCard));

			//读取卡序列号
			if(mf30_card_read_id(kkc,env,obj,szIDCard) <=0 ){
				kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}","读取序列号_读取序列号失败");
				continue;
			}

		    //kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}","读取序列号_读取序列号成功");
			// 序列号
			char szGuid[512] = {0};
			memset(szGuid,0,sizeof(szGuid));
			int length= sizeof(szGuid);
			HexEncode((unsigned char*)szIDCard,4,&szGuid[0],&length);

			//kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\",card:\"%s\"}","开始选卡",szGuid);
			//选卡
			if(mf30_card_select(kkc,env,obj,szGuid) <=0 ) continue;

			kkcard_event(kkc,env,obj,CE_BRUSHED,"{success:true,status:\"0x%02X\",msg:\"%s\"}",0x11,szGuid);
			return TRUE;
		}
		return FALSE;
}

int kkcard_read_mf30_card(KKCARD_HANDLE kkc,HKKC env,HKKC obj, int sectorid, int blockid, int timeout)
{
	   if(!kkc)return 0;

		clock_t start, finish,internalTime;
	    internalTime=timeout*1000;            //超时时间
	    start=clock();
		//kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}","开始读卡");
		while (1) {
			finish = clock();
			if ((finish - start) > internalTime) {
				kkcard_event(kkc,env,obj,CE_EVENT,"{status:\"0x%02X\",msg:\"%s\"}",0x10,"read mf30 card timeout");
				return -1;
			}

			//kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}","开始查找卡");
			// 查找卡
			if(mf30_card_request(kkc,env,obj) <= 0) continue;

			//kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}","开始读取序列号");

			char szIDCard[20] = {0};
			memset(szIDCard,0,sizeof(szIDCard));

			//读取卡序列号
			if(mf30_card_read_id(kkc,env,obj,szIDCard) <=0 ) continue;

			// 序列号
			char szGuid[512] = {0};
			memset(szGuid,0,sizeof(szGuid));
			int length= sizeof(szGuid);
			HexEncode((unsigned char*)szIDCard,4,&szGuid[0],&length);

			//kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\",card:\"%s\"}","开始选卡测试",szGuid);
			//kkcard_event(kkc,env,obj,CE_BRUSHED,"{success:true,status:\"0x%02X\",msg:\"%s\"}",0x11,szGuid);

			//选卡
			if(mf30_card_select(kkc,env,obj,szGuid) <=0 ) continue;

			//kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}","开始验证密匙");

			int mode = 0;
			int authblock = 4*sectorid;
			// 验证卡
			if(mf30_card_authentication(kkc,env,obj,mode,authblock,"FFFFFFFFFFFF") <= 0) {
				kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}","验证密匙失败");
				continue;
			}

			//kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}","开始读卡");
			char szRecvData[2048] = {0};
			memset(szRecvData,0,sizeof(szRecvData));

			int block = sectorid*4+blockid;
			// 读卡
			if(mf30_card_read(kkc,env,obj,szRecvData,block) <= 0){
				kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}","读卡失败");
				continue;
			}else{
				kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}","读卡成功");
				char szTemp[2048] = {0};
				memset(szTemp,0,sizeof(szTemp));
				int ilen1= sizeof(szTemp);
				HexEncode((unsigned char*)szRecvData,16,&szTemp[0],&ilen1);

				kkcard_event(kkc,env,obj,CE_BRUSHED,"{success:true,status:\"0x%02X\",cardno:\"%s\"}",0x20,szTemp);
				return TRUE;
			}
		}
		return FALSE;
}

int kkcard_read_mf30_card_bypwd(KKCARD_HANDLE kkc,HKKC env,HKKC obj, int sectorid, int blockid, char *passwd, int pwdlen, int timeout)
{
	   if(!kkc)return 0;

		clock_t start, finish,internalTime;
	    internalTime=timeout*1000;            //超时时间
	    start=clock();
		//kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}","开始读卡");
		while (1) {
			finish = clock();
			if ((finish - start) > internalTime) {
				kkcard_event(kkc,env,obj,CE_EVENT,"{status:\"0x%02X\",msg:\"%s\"}",0x10,"read mf30 card timeout");
				return -1;
			}

			//kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}","开始查找卡");
			// 查找卡
			if(mf30_card_request(kkc,env,obj) <= 0) continue;

			//kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}","开始读取序列号");

			char szIDCard[20] = {0};
			memset(szIDCard,0,sizeof(szIDCard));

			//读取卡序列号
			if(mf30_card_read_id(kkc,env,obj,szIDCard) <=0 ) continue;

			// 序列号
			char szGuid[512] = {0};
			memset(szGuid,0,sizeof(szGuid));
			int length= sizeof(szGuid);
			HexEncode((unsigned char*)szIDCard,4,&szGuid[0],&length);

			//kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\",card:\"%s\"}","开始选卡测试",szGuid);
			//kkcard_event(kkc,env,obj,CE_BRUSHED,"{success:true,status:\"0x%02X\",msg:\"%s\"}",0x11,szGuid);

			//选卡
			if(mf30_card_select(kkc,env,obj,szGuid) <=0 ) continue;

			//自定义密码
			char szPasswd[50] = {0};
			memset(szPasswd,0,sizeof(szPasswd));
			memcpy(szPasswd,passwd,pwdlen);

			kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}","开始验证密匙");
			//kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}",szPasswd);

			int mode = 0;
			int authblock = 4*sectorid;

			// 验证卡
			if(mf30_card_authentication(kkc,env,obj,mode,authblock,szPasswd) <= 0) continue;

			kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}","开始读卡");
			char szRecvData[2048] = {0};
			memset(szRecvData,0,sizeof(szRecvData));

			int block = sectorid*4+blockid;
			// 读卡
			if(mf30_card_read(kkc,env,obj,szRecvData,block) <= 0){
				//kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}","读卡失败");
				continue;
			}else{
				//kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}","读卡成功");
				char szTemp[2048] = {0};
				memset(szTemp,0,sizeof(szTemp));
				int ilen1= sizeof(szTemp);
				HexEncode((unsigned char*)szRecvData,16,&szTemp[0],&ilen1);

				kkcard_event(kkc,env,obj,CE_BRUSHED,"{success:true,status:\"0x%02X\",cardno:\"%s\"}",0x20,szTemp);
				return TRUE;
			}
		}
		return FALSE;
}

int kkcard_write_mf30_card(KKCARD_HANDLE kkc,HKKC env,HKKC obj, int sectorid,  int blockid, char *data0,int len0)
{
	if(!kkc)return FALSE;

	int mode = 0;
	int authblock = 4*sectorid;
	// 验证卡
	if(mf30_card_authentication(kkc,env,obj,mode,authblock,"FFFFFFFFFFFF") <= 0) return FALSE;

	char buf[512];
	char data[512];
	char szData0[512];

	memset(buf,0,sizeof(buf));
	memset(data,0,sizeof(data));
	memset(szData0,0,sizeof(szData0));

	CompressAsc(data0,len0,szData0);

	int block = sectorid*4+blockid;

	data[0] = 0x09;
	data[1] = 0x02;
	data[2] = block&0xff;
	memcpy(data+3,szData0,len0);

	//kkcard_event(kkc,env,obj,CE_START,"{msg:\"%s\"}","开始写卡");

	if(mf30_card_send_cmd(kkc->fd,data,19)>0){
		int r = mf30_card_recv_by_len(kkc->fd,buf,sizeof(buf),1000);
		if(r > 0){
			if(buf[8] == 0x00){
				return TRUE;
			}
		}
	}
	return FALSE;
}


int mf30_card_read(KKCARD_HANDLE kkc, HKKC env,HKKC obj, char *szRecvData, int sectorid)
{
	if(!kkc)return FALSE;

	char buf[512];
	char data[512];

	memset(buf,0,sizeof(buf));
	memset(data,0,sizeof(data));
	memset(szRecvData,0,sizeof(szRecvData));

	data[0] = 0x08;
	data[1] = 0x02;
	data[2] = sectorid&0xff;

	if(mf30_card_send_cmd(kkc->fd,data,3)>0){
		int r = mf30_card_recv_by_len(kkc->fd,buf,sizeof(buf),1000);
		if(r > 0){
			if(buf[8] == 0x00){
				int totallen = 25;
				int i = 0;
				int j = 0;
				for(i = 9; i<= totallen; i++){
					if(buf[i] == 0xAA){
                        for(j =i ; j <= totallen; j++){
                           buf[j+1] = buf[j+2];
                        }
					}
				}
				memcpy(szRecvData,buf+9,16);
				return TRUE;
			}
		}
	}
	return FALSE;
}

int mf30_card_request(KKCARD_HANDLE kkc, HKKC env,HKKC obj)
{
	if(!kkc)return FALSE;

	char buf[512];
	char data[512];

	memset(buf,0,sizeof(buf));
	memset(data,0,sizeof(data));

	data[0] = 0x01;
	data[1] = 0x02;
	data[2] = 0x52;

	if(mf30_card_send_cmd(kkc->fd,data,3)>0){
		int r = mf30_card_recv_by_len(kkc->fd,buf,sizeof(buf),1000);
		if(r > 0){
			if(buf[8] == 0x00){
				return TRUE;
			}
		}
	}
	return FALSE;
}

int mf30_card_getver(KKCARD_HANDLE kkc, HKKC env,HKKC obj ,char *szData)
{
	if(!kkc)return FALSE;

	char buf[512];
	char data[512];

	memset(buf,0,sizeof(buf));
	memset(data,0,sizeof(data));
	memset(szData,0,sizeof(szData));

	data[0] = 0x04;
	data[1] = 0x01;

	if(mf30_card_send_cmd(kkc->fd,data,2)>0){
		int r = mf30_card_recv_by_len(kkc->fd,buf,sizeof(buf),1000);
		if(r > 0){
			if(buf[8] == 0x00){
				memcpy(szData,buf+9,2);
				return TRUE;
			}
		}
	}
	return FALSE;
}


int mf30_card_read_id(KKCARD_HANDLE kkc, HKKC env,HKKC obj ,char *szData)
{
	if(!kkc)return FALSE;

	char buf[512];
	char data[512];

	memset(buf,0,sizeof(buf));
	memset(data,0,sizeof(data));
	memset(szData,0,sizeof(szData));

	data[0] = 0x02;
	data[1] = 0x02;
	data[2] = 0x04;

	if(mf30_card_send_cmd(kkc->fd,data,3)>0){
		int r = mf30_card_recv_by_len(kkc->fd,buf,sizeof(buf),1000);
		if(r > 0){
			if(buf[8] == 0x00){
				int totallen = 12;
				int i = 9;
				int j = 0;
				for(i = 9; i<= totallen; i++){
					if(buf[i] == 0xAA){
                        for(j =i ; j <= totallen; j++){
                           buf[j+1] = buf[j+2];
                        }
					}
				}
				memcpy(szData,buf+9,4);
				return TRUE;
			}
		}
	}
	return FALSE;
}

int mf30_card_select(KKCARD_HANDLE kkc, HKKC env,HKKC obj, char *szData)
{
	if(!kkc)return FALSE;

	char buf[512];
	char data[512];
	char buffer[512];

	memset(buf,0,sizeof(buf));
	memset(data,0,sizeof(data));
	memset(buffer,0,sizeof(buffer));

	CompressAsc(szData,8,buffer);

	data[0] = 0x03;
	data[1] = 0x02;
	memcpy(data+2,buffer,4);

	if(mf30_card_send_cmd(kkc->fd,data,6)>0){
		int r = mf30_card_recv_by_len(kkc->fd,buf,sizeof(buf),1000);
		if(r > 0){
			if(buf[8] == 0x00){
				return TRUE;
			}
		}
	}
	return FALSE;
}

int mf30_card_authentication(KKCARD_HANDLE kkc , HKKC env,HKKC obj,int mode, int sectorid, char *szData)
{
	if(!kkc)return FALSE;

	char buf[512];
	char data[512];
	char buffer[512];

	memset(buf,0,sizeof(buf));
	memset(data,0,sizeof(data));
	memset(buffer,0,sizeof(buffer));

	CompressAsc(szData,12,buffer);

	data[0] = 0x07;
	data[1] = 0x02;
    data[2] = 0x60;
	data[3] = sectorid&0xff;
	memcpy(data+4,buffer,6);

	if(mf30_card_send_cmd(kkc->fd,data,10)>0){
		int r = mf30_card_recv_by_len(kkc->fd,buf,sizeof(buf),1000);
		if(r > 0){
			if(buf[8] == 0x00){
				return TRUE;
			}
		}
	}
	return FALSE;
}

/**
 * 世融通读卡器 ,发送命令的函数，发送后会等待设备返回结果
 * @param fd 串口句柄
 * @param cmd 命令字符串
 */
static int cpucard_send_cmd(int fd,char *cmd,int size)
{
	char chSendCmd[1024],*p = chSendCmd;
	unsigned int nLen, ret;
	int i;

	tcflush(fd,TCIOFLUSH);
	memset(chSendCmd, '\0', sizeof(chSendCmd));

	nLen = size;

	*p++ = 0xAA;
	for(i=0;i<size;i++){
		*p++ = cmd[i];
		// __android_log_print(ANDROID_LOG_DEBUG,"kkcard","cmd[%d]=0x%02X",i,(char)cmd[i]);
	}


	ret = BCC(chSendCmd, size+1);
	*p++ = ret&0xFF;

	int r = com_write(fd, chSendCmd, size+2);

//	for(i=0;i<size+2;i++){
//		 __android_log_print(ANDROID_LOG_DEBUG,"kkcard","cmd[%d]=0x%02X",i,(char)chSendCmd[i]);
//	}
	return r;
}

/**
 * 接收信息直到收到定长数据，接收三次如果如果还没收到则返回失败
 * 返回的第一个字符为标志，第二三字符为十六进制表示的数据长度
 * @param fd 串口句柄
 * @param timeout 每次接收的超时时间
 */
static int cpucard_recv_by_len(int fd,char *buf,int bufsize,int timeout)
{
	int len=0;
	int mlen = 0;
	//从键盘获得结果
	memset(buf,0,sizeof(buf));
	len = com_recive(fd,buf,bufsize,timeout);
	while(len < 4){
		//如果接收的长度小于4的时候说明长度参数还没收齐。
		int r = com_recive(fd,buf+len,bufsize-len,timeout);
		if(r>0){
			len += r;
			if(buf[0] != 0x55){
				// __android_log_print(ANDROID_LOG_INFO, "kkcard","kkcard recive prefix error : 0x%02X",buf[0]);
				return -2;		//返回不是以02开头，数据不正确
			}
		}else
			return r;
	}
	int lhex=0;
	lhex = ((unsigned)(buf[2]) << 8) + buf[3];
	mlen = lhex+5;
	{
		char hexbuf[512];
		int dlen=sizeof(hexbuf);

		memset(hexbuf,0,sizeof(hexbuf));
		HexEncode(buf,len,hexbuf,&dlen);
		//__android_log_print(ANDROID_LOG_INFO, "kkcard","kkcard recive by len: len=%d,buf=%s,lhex=%d,mlen=%d",len,hexbuf,lhex,mlen);
	}
	if(lhex > 512-5){
		// __android_log_print(ANDROID_LOG_INFO, "kkcard","kkcard recive len error : %d",lhex);
		return -2;	//长度超出范围，说明格式有问题
	}
	lhex = mlen - len;
	while(lhex > 0){
		len = com_recive(fd,buf+(mlen-lhex),bufsize-(mlen-lhex),timeout);
		if(len > 0){
			lhex -= len;
			if(lhex > 0)
				continue;
			{
				char hexbuf[512];
				int dlen=sizeof(hexbuf);

				memset(hexbuf,0,sizeof(hexbuf));
				HexEncode(buf,(mlen-lhex),hexbuf,&dlen);
				//__android_log_print(ANDROID_LOG_INFO, "kkcard","kkcard recive by len finished: buf=%s",hexbuf);
			}
		}else if(len == -1){
			return -1;		//接收失败
		}else if(len == 0){
			return 0;		//接收超时
		}
	}
	tcflush(fd,TCIOFLUSH);	//清除缓冲区
	return mlen;
}

int cpucard_reset(KKCARD_HANDLE kkc,HKKC env,HKKC obj,int timeout)
{
	char buf[1024];
	char buffer[1024];
	if(!kkc)return FALSE;

	memset(buf,0,sizeof(buf));
	memset(buffer,0,sizeof(buffer));

	if(cpucard_send_cmd(kkc->fd,"\x20\x80\x05\x20\x00\x00\x00\x00",8)>0){
		int r = cpucard_recv_by_len(kkc->fd,buf,sizeof(buf),timeout);
		if(r > 0){
			if(buf[1] == 0x00){

				int buflen = 0 ;
				buflen = ((unsigned)(buf[2]) << 8) + buf[3];
				memcpy(buffer,buf+4,buflen);

				char szData[512]={0};
				memset(szData,0,sizeof(szData));
				int dlen=sizeof(szData);

				HexEncode(buffer,buflen,szData,&dlen);
				kkcard_event(kkc,env,obj,CE_READ_CPUCARD,"{success:true,status:\"0x%02X\",data:\"%s\"}",0x21,szData);
				return TRUE;
			}
		}
	}
	return FALSE;
}

int cpucard_poweron(KKCARD_HANDLE kkc,HKKC env,HKKC obj,int timeout)
{
	char buf[1024];
	char buffer[1024];
	if(!kkc)return FALSE;

	memset(buf,0,sizeof(buf));
	memset(buffer,0,sizeof(buffer));

	if(cpucard_send_cmd(kkc->fd,"\xB2\x00\x05\x80\x00\x00\x00\x00",8)>0){
		int r = cpucard_recv_by_len(kkc->fd,buf,sizeof(buf),timeout);
		if(r > 0){
			if(buf[1] == 0x00){
				int buflen = 0 ;
				buflen = ((unsigned)(buf[2]) << 8) + buf[3];
				memcpy(buffer,buf+4,buflen);

				char szData[512]={0};
				memset(szData,0,sizeof(szData));
				int dlen=sizeof(szData);

				HexEncode(buffer,buflen,szData,&dlen);
				kkcard_event(kkc,env,obj,CE_READ_CPUCARD,"{success:true,status:\"0x%02X\",data:\"%s\"}",0x22,szData);
				return TRUE;
			}
		}
	}
	return FALSE;
}

int cpucard_poweroff(KKCARD_HANDLE kkc,HKKC env,HKKC obj,int timeout)
{
	char buf[1024];
	char buffer[1024];
	if(!kkc)return FALSE;

	memset(buf,0,sizeof(buf));
	memset(buffer,0,sizeof(buffer));

	if(cpucard_send_cmd(kkc->fd,"\xB3\x00\x05\x80\x00\x00\x00\x00",8)>0){
		int r = cpucard_recv_by_len(kkc->fd,buf,sizeof(buf),timeout);
		if(r > 0){
			if(buf[1] == 0x00){
				int buflen = 0 ;
				buflen = ((unsigned)(buf[2]) << 8) + buf[3];
				memcpy(buffer,buf+4,buflen);

				char szData[1024]={0};
				memset(szData,0,sizeof(szData));
				int dlen=sizeof(szData);

				HexEncode(buffer,buflen,szData,&dlen);
				kkcard_event(kkc,env,obj,CE_READ_CPUCARD,"{success:true,status:\"0x%02X\",data:\"%s\"}",0x23,szData);
				return TRUE;
			}
		}
	}
	return FALSE;
}

int cpucard_apdu(KKCARD_HANDLE kkc,HKKC env,HKKC obj,char *data, int len, int timeout)
{
	if(!kkc)return FALSE;

	char buf[1024];
	char cmd[1024];
	char buffer[1024];
	char szData[1024];
	char szRecvData[1024];

	unsigned ret;
	int buflen = 0 ;

	memset(buf,0,sizeof(buf));
	memset(cmd,0,sizeof(cmd));
	memset(szData,0,sizeof(szData));
	memset(buffer,0,sizeof(buffer));
	memset(szRecvData,0,sizeof(szRecvData));

	CompressAsc(data,len,szData);

	cmd[0] = 0x21;
	cmd[1] = 0x80;
	cmd[2] = (9 + len/2)&0xff;
	cmd[3] = 0x20;
	cmd[4] = 0x00;
	cmd[5] = 0x00;
	cmd[6] = 0x00;
	cmd[7] = 0x00;
	cmd[8] = 0x00;
	cmd[9] = 0x00;
	cmd[10] = (len/2)&0xff;

	memcpy(cmd+11,szData,len/2);
	ret = BCC(cmd+9, len/2+2);
    cmd[len/2+11] = ret&0xff;


	//读取 EF3 - EF4 文件 单独处理
	if((char)cmd[12] == 0xB0 &&(char)cmd[13] == 0x83 || (char)cmd[12] == 0xB0&&(char)cmd[13] == 0x84){

		if(cpucard_read_EF_block(kkc,env,obj,cmd,szRecvData,&buflen,timeout) > 0)
		{
			char szData[1024]={0};
			memset(szData,0,sizeof(szData));
			int dlen=sizeof(szData);

			HexEncode(szRecvData,buflen,szData,&dlen);
			kkcard_event(kkc,env,obj,CE_READ_CPUCARD,"{success:true,status:\"0x%02X\",data:\"%s\"}",0x31,szData);
			return TRUE;
		}else{
			return FALSE;
		}
	}

	if(cpucard_send_cmd(kkc->fd,cmd,len/2+12)>0){
		int r = cpucard_recv_by_len(kkc->fd,buf,sizeof(buf),timeout);
		if(r > 0){
			if(buf[1] == 0x00){
				buflen = 0 ;
				buflen = ((unsigned)(buf[5]) << 8) + buf[6];
				memcpy(buffer,buf+7,buflen);

				//读取 EF1 - EF2 - EF5 文件
				if((char)buffer[0] == 0x6C && buflen <=2){
					if(cpucard_read_block(kkc,env,obj,buffer,szRecvData,&buflen,timeout) > 0)
					{
						char szData[1024]={0};
						memset(szData,0,sizeof(szData));
						int dlen=sizeof(szData);

						HexEncode(szRecvData,buflen,szData,&dlen);
						kkcard_event(kkc,env,obj,CE_READ_CPUCARD,"{success:true,status:\"0x%02X\",data:\"%s\"}",0x31,szData);
						return TRUE;
					}else{
						return FALSE;
					}
				}

				char szData[1024]={0};
				memset(szData,0,sizeof(szData));
				int dlen=sizeof(szData);

				HexEncode(buffer,buflen,szData,&dlen);
				kkcard_event(kkc,env,obj,CE_READ_CPUCARD,"{success:true,status:\"0x%02X\",data:\"%s\"}",0x31,szData);
				return TRUE;
			}
		}
	}
	return FALSE;
}


// apdu 指令
int cpucard_apdu_cmd(KKCARD_HANDLE kkc,HKKC env,HKKC obj,char *data, int len, char *szRecvData, int *length,int timeout)
{
 	if(!kkc)return FALSE;
    
	char cmd[512];
	char buf[512];
	char buffer[512];
	char szData[512];
	char temp[512];
	unsigned ret = 0;

	int buflen = 0;
    
	memset(cmd,0,sizeof(cmd));
	memset(szData,0,sizeof(szData));
	memset(buffer,0,sizeof(buffer));
	memset(temp,0,sizeof(temp));
	memset(buf,0,sizeof(buf));
    
	CompressAsc(data,len,szData);
    
	cmd[0] = 0x21;
	cmd[1] = 0x80;
	cmd[2] = (9 + len/2)&0xff;
	cmd[3] = 0x20;
	cmd[4] = 0x00;
	cmd[5] = 0x00;
	cmd[6] = 0x00;
	cmd[7] = 0x00;
	cmd[8] = 0x00;
	cmd[9] = 0x00;
	cmd[10] = (len/2)&0xff;
    
	memcpy(cmd+11,szData,len/2);
	ret = BCC(cmd+9, len/2+2);
    cmd[len/2+11] = ret&0xff;
    

	if(cpucard_send_cmd(kkc->fd,cmd,len/2+12)>0){
		int r = cpucard_recv_by_len(kkc->fd,buf,sizeof(buf),timeout);
		if(r > 0){
			if(buf[1] == 0x00){
				int buflen = 0 ;
				buflen = ((unsigned)(buf[5]) << 8) + buf[6];
                *length = buflen;
				memcpy(szRecvData,&buf[7],buflen);

				//读取 EF1 - EF5 文件
				if((char)szRecvData[0] == 0x6C && buflen <=2){
					if(cpucard_read_block(kkc,env,obj,szRecvData,temp,&buflen,timeout) > 0)
					{
						memset(szRecvData,0,sizeof(szRecvData));
						int datalen = buflen;
		                *length = datalen;
						memcpy(szRecvData,&temp[0],datalen);
						return TRUE;
					}
				}

				return TRUE;
			}
		}
	}
	return FALSE;
}

// 选择文件
int cpucard_check_block(KKCARD_HANDLE kkc,HKKC env,HKKC obj,int value, int timeout)
{
	char cmd[1024];
    char szBuffer[1024] = {0};
    int *length = 0;

    memset(szBuffer,0,sizeof(szBuffer));
	memset(cmd,0,sizeof(cmd));

	sprintf(cmd,"%s%02D","00A402000200",value);

	if(cpucard_apdu_cmd(kkc,env,obj,cmd,14,szBuffer,&length,timeout)>0){
		return TRUE;
	}
	return FALSE;
}


// 读取响应数据
int cpucard_read_block(KKCARD_HANDLE kkc,HKKC env,HKKC obj,char *data, char *szRecvData, int *length,int timeout)
{
	char cmd[1024];
    char szBuffer[1024] = {0};
    int *len = 0;
    memset(szBuffer,0,sizeof(szBuffer));
	if(!kkc)return FALSE;

	memset(cmd,0,sizeof(cmd));

	sprintf(cmd,"%s%02X","00C00000",data[1]);

	if(cpucard_apdu_cmd(kkc,env,obj,cmd,10,szBuffer,&len,timeout)>0){
		int buflen = len;
		*length =  buflen;
		memcpy(szRecvData,&szBuffer[0],buflen);
		return TRUE;
	}
	return FALSE;
}

// 读取EF3 或者EF4数据
int cpucard_read_EF_block(KKCARD_HANDLE kkc,HKKC env,HKKC obj,char *data, char *szRecvData, int *length,int timeout)
{
	char cmd1[1024];
	char cmd2[1024];

    char szBuffer[1024] = {0};
    char szTemp[1024] = {0};

    int *len1 = 0;
    int *len2 = 0;

    int buflen1 = 0;
    int buflen2 = 0;

    memset(szBuffer,0,sizeof(szBuffer));
    memset(szTemp,0,sizeof(szTemp));
	memset(cmd1,0,sizeof(cmd1));
	memset(cmd2,0,sizeof(cmd2));

	sprintf(cmd1,"%s%02X%s","00B0",data[13],"0090");
	sprintf(cmd2,"%s%02X%s","00B0",data[13],"9070");


	if(cpucard_apdu_cmd(kkc,env,obj,cmd1,10,szBuffer,&len1,timeout)>0){
		buflen1 = len1;
		*length =  buflen1;

		memcpy(szRecvData,&szBuffer[0],buflen1);


		if(cpucard_apdu_cmd(kkc,env,obj,cmd2,10,szTemp,&len2,timeout)>0){
			buflen2 = len2;
			*length =  buflen1 + buflen2;
			memcpy(szRecvData+buflen1,&szTemp[0],buflen2);
			return TRUE;
		}
	}
	return FALSE;
}

