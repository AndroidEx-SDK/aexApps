#include <jni.h>
#include "com_androidex_devices_aexddZTC70.h"
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
#include "../include/Des.h"
#include "../include/utils.h"
#include "aexddZTC70.h"

#include <android/log.h>

/**
 * 相当于java中的具体功能的实现类（认为）
 */

static ON_ZTC_EVENT on_ztc_event=NULL;

/**
 * 设置回调函数，在JNI的代码里会调用它来设置处理事件的回调函数
 */
void ztc_set_event(ON_ZTC_EVENT oke)
{
	on_ztc_event = oke;
}

/**
 * 密码键盘事件的入口函数，静态函数只能在本文件中调用
 */
static int ztc_event(HZTC env,HZTC obj,int code,char *pszFormat,...)
{
	char pszDest[2048];
	va_list args;

	va_start(args, pszFormat);
	vsnprintf(pszDest, sizeof(pszDest), pszFormat, args);
	va_end(args);

	//只有设置了事件回调函数，此函数才会调用事件，否则什么也不做
	if(on_ztc_event){
		on_ztc_event(env,obj,code,pszDest);
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
static int ztc_recv_by_end(int fd,char *end,int timeout)
{
	char buf[512];
	int len=0,times=0;
	//从键盘获得结果
	// __android_log_print(ANDROID_LOG_INFO, "kmy", "ztc_recv_by_end(%d,%s,%d)",fd,end,timeout);
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

static char *ztc_get_st_msg(unsigned  st){
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

static unsigned int ztc_readlen(char * buf){
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

static int ztc_check_return(char *buf){
	char st[3]="00";
	unsigned int lst=0;

	memset(st,0,sizeof(st));
	sprintf(st,"%c%c",buf[3],buf[4]);
	sscanf(st,"%x",&lst);

	if(lst != 0x04){
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
static int ztc_recv_by_len(int fd,char *buf,int bufsize,int timeout)
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

		if((r = ztc_check_return(buf))<=0){
			return r;
		}
		memset(hex,0,sizeof(hex));
		sprintf(hex,"%c%c",buf[1],buf[2]);
		sscanf(hex,"%x",&lhex);
		mlen = lhex*2+5;
		//__android_log_print(ANDROID_LOG_INFO, "kmy", "ztc_recv_by_len：len=%d,lhex=%d,mlen=%d,buf=\\0x%02X%s,hex=%s",len,lhex,mlen,buf[0],buf+1,hex);
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
		__android_log_print(ANDROID_LOG_INFO, "kmy", "ztc_recv_by_len：mlen=%d,buf=(%d)\\0x%02X%s", mlen,strlen(buf),buf[0],buf+1);
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
int ztc_send_cmd(HZTC env,HZTC obj,int fd,char *cmd,int size)
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

int ztc_send_hexcmd(HZTC env,HZTC obj,int fd,char *hexcmd,int size)
{
	char chSendCmd[256];
	int nLen, ret;

	tcflush(fd,TCIOFLUSH);
	memset(chSendCmd, '\0', sizeof(chSendCmd));
	ret = HexBCC(hexcmd, size);
	sprintf(chSendCmd, "%c%s%02X%c", 0x02, hexcmd, ret,0x03);
	 __android_log_print(ANDROID_LOG_DEBUG,"zt","sendCmd(\\0x%02X%s\\0x03) return 0x%02X",chSendCmd[0],chSendCmd+1,ret);
	ret = com_write(fd, chSendCmd, strlen(chSendCmd));
	return ret;
}

int ztc_read_key_loop(HZTC env,HZTC obj,int fd,int timeout)
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
        if(buf[0] == 0x2A || (buf[0] >= '0' && buf[0] <= '9')){
            irecvlen++;
            sprintf(str,"{key:'%c',keyhex:'0x%02X'}",buf[0],buf[0]);
        }else{
            sprintf(str,"{key:'%02X',keyhex:'0x%02X'}",buf[0],buf[0]);
        }

        if(buf[0] == 0x1B || buf[0] == 0x0D || buf[0] == 0x00){
            ztc_event(env,obj,KE_PRESSED,str);
            break;
        }else if(buf[0] == 0x08){
            irecvlen--;
            ztc_event(env,obj,KE_PRESSED,str);
            times++;	//按了删除键
        }else{
            ztc_event(env,obj,KE_PRESSED,str);
        }
        times--;
    }while(r>0 && times > 0 && buf[0] != 0x00);
    return buf[0] != 0x0D;
}

/**
 * 读取一个数据包的函数，抛弃前面的无效数据直到找到一个0x02的数据包头。
 * 然后读取一个完整的数据包：0x02+Len(1字节的长度)+Data(Len字节)+BCC+0x03
 * 返回：
 *      > 0     数据包总长度
 *      == 0    读取超时
 *      < 0     读取错误
 */
int ztc_recive_packet(HZTC env,HZTC obj,int fd,char *buf,int bufsize,int timeout)
{
    int len = 0;
    int mlen = 0;
    char *p = buf;

    memset(p, 0, bufsize);
    len = com_recive(fd, p, 1, timeout);

    while (len == 1 && *p != 0x02) {
        //丢弃数据包头之前的数据
        len = com_recive(fd, p, 1, timeout);
    }
	
    if (len <=0){
        //如果超时或者发生错误则返回
        return len;
    }
    //找到了数据包头0x02
    p++;    //移到下一字节处
    len = com_recive(fd, p, 1, timeout);    //读长度

    if(len == 1){
        //读到一个字节
        mlen = (*p++);
    }else if (len==0){
	} else{
        //读取长度错误
        return -1000;
    }
    int lhex = mlen + 2;
    while(lhex > 0){
        //读取包内容、包尾及BCC
        len = com_recive(fd,p,lhex,timeout);
        if(len > 0){
            lhex -= len;        //欲读取的字节数减少
            p += len;           //缓冲区移位
            if(lhex > 0)        //剩余的字节数不为0，则继续读
                continue;
        }else if(len == -1){
            return -1;		//接收失败
        }else if(len == 0){
            return 0;		//接收超时
        }
    }
    return mlen + 4;    //返回总数据包长
}

