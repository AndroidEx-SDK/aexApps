#include <jni.h>
#include "com_androidex_devices_aexddMT319.h"
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
#include "../include/utils.h"
#include "aexddMT319.h"

#include <android/log.h>

static ON_KKCARD_EVENT on_kkcard_event=NULL;
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
int kkcard_event(HKKC env,HKKC obj,int code,char *pszFormat,...)
{
	char pszDest[2048];
	va_list args;

	va_start(args, pszFormat);
	vsnprintf(pszDest, sizeof(pszDest), pszFormat, args);
	va_end(args);
	//只有设置了事件回调函数，此函数才会调用事件，否则什么也不做
	if(on_kkcard_event){
		return on_kkcard_event(env,obj,code,pszDest);
	}else{
	    return 0;
	}
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
static int kkcard_recv_by_len(HKKC env,HKKC obj,int fd,char *buf,int bufsize,int timeout)
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
 * 读取一个数据包的函数，抛弃前面的无效数据直到找到一个0x02的数据包头。
 * 然后读取一个完整的数据包：0x02+Len(2字节的长度)+Data(Len字节)+0x03+BCC
 * 返回：
 *      > 0     数据包总长度
 *      == 0    读取超时
 *      < 0     读取错误
 */
int kkcard_recive_packet(HKKC env,HKKC obj,int fd,char *buf,int bufsize,int timeout)
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
    if (len <= 0){
        //如果超时或者发生错误则返回
        return len;
    }
    //找到了数据包头0x02
    p++;    //移到下一字节处
    len = com_recive(fd, p, 2, timeout);    //读长度
    if(len == 2){
        //读到一个字节
        mlen = (*p++) << 8;
        mlen |= (*p++);
    }else{
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
    return mlen + 5;    //返回总数据包长
}

/**
 *发送命令的函数，发送后会等待键盘返回结果
 * @param fd 串口句柄
 * @param cmd 命令字符串
 */
int kkcard_send_cmd(HKKC env,HKKC obj,int fd,char *cmd,int size)
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
	}
	*p++ = 0x03;
	ret = BCC(chSendCmd, size+4);
	*p++ = ret&0xFF;

	int r = com_write(fd, chSendCmd, size+5);
	return ret;
}

int kkcard_read_loop(HKKC env,HKKC obj,int fd,int timeout)
{
	char buf[512],str[512];
	int r = 0;

	if(timeout == 0)
		timeout = -1;
	while(1){
        memset(buf,0,sizeof(buf));
        memset(str,0,sizeof(str));
        r = kkcard_recv_by_len(env,obj,fd,buf,sizeof(buf),timeout);
        if(r>0){
            //if(kkcard_handle_status(kkc,env,obj,buf,r)){
            //    break;
            //}else{
            //    continue;
            //}
        }else if(r == 0){
            kkcard_event(env,obj,CE_EVENT,"{status:\"0x%02X\",result:\"%d\",msg:\"%s\"}",0x10,r,"读卡超时");
            break;
        }else if(r == -1){
            kkcard_event(env,obj,CE_EVENT,"{result:\"%d\",msg:\"%s\"}",r,"读卡通讯失败");
            break;
        }else{
            kkcard_event(env,obj,CE_EVENT,"{result:\"%d\",msg:\"%s\"}",r,"其他错误");
        }
    }
	return r;
}
