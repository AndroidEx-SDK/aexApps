/**
 * 公用函数库
 */
#include <stdio.h>      /*标准输入输出定义*/
#include <stdlib.h>     /*标准函数库定义*/
#include <unistd.h>     /*Unix标准函数定义*/
#include <fcntl.h>      /*文件控制定义*/
#include <errno.h>      /*错误号定义*/
#include <time.h>
#include <sys/ioctl.h>
#include <sys/stat.h>
#include <sys/types.h>
//#include <sys/dirent.h>
#include "../include/termios_user.h"    /*PPSIX终端控制定义*/
#include "../include/utils.h"

#include <android/log.h>
#include <linux/ioctl.h>
#include <sys/ioctl.h>

typedef unsigned char BYTE;
static int DevicesFlag = 0;

int HexEncodeGetRequiredLength(int nSrcLen)
{
	return 2 * nSrcLen + 1;
}

int HexDecodeGetRequiredLength(int nSrcLen)
{
	return nSrcLen/2;
}

int HexEncode(const unsigned char *pbSrcData, int nSrcLen, char *szDest, int *pnDestLen)
{
	int nRead = 0;
	int nWritten = 0;
	BYTE ch;
	static const char s_chHexChars[16] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'A', 'B', 'C', 'D', 'E', 'F'};

	if (!pbSrcData || !szDest || !pnDestLen)
	{
		return FALSE;
	}

	if(*pnDestLen < HexEncodeGetRequiredLength(nSrcLen))
	{
		return FALSE;
	}

	while (nRead < nSrcLen)
	{
		ch = *pbSrcData++;
		nRead++;
		*szDest++ = s_chHexChars[(ch >> 4) & 0x0F];
		*szDest++ = s_chHexChars[ch & 0x0F];
		nWritten += 2;
	}

	*pnDestLen = nWritten;

	return TRUE;
}

#define HEX_INVALID ((char)-1)

//Get the decimal value of a hexadecimal character
char GetHexValue(char ch)
{
	if (ch >= '0' && ch <= '9')
		return (ch - '0');
	if (ch >= 'A' && ch <= 'F')
		return (ch - 'A' + 10);
	if (ch >= 'a' && ch <= 'f')
		return (ch - 'a' + 10);
	return HEX_INVALID;
}

int HexDecode(const char *pSrcData, int nSrcLen, unsigned char *pbDest, int* pnDestLen)
{
	int nRead = 0;
	int nWritten = 0;

	if (!pSrcData || !pbDest || !pnDestLen)
	{
		return 0;
	}

	if(*pnDestLen < HexDecodeGetRequiredLength(nSrcLen))
	{
		return 0;
	}

	while (nRead < nSrcLen)
	{
		char ch1,ch2;

		if((char)*pSrcData == '\r' || (char)*pSrcData == '\n')break;
		ch1 = GetHexValue((char)*pSrcData++);
		ch2 = GetHexValue((char)*pSrcData++);
		if ((ch1==HEX_INVALID) || (ch2==HEX_INVALID))
		{
			return 0;
		}
		*pbDest++ = (unsigned char)(16*ch1+ch2);
		nWritten++;
		nRead += 2;
	}

	*pnDestLen = nWritten;
	return 1;
}

int utils_strincmp(const char *s1,const char *s2,int n)
{
	/* case insensitive comparison */
	int d;
	while (--n >= 0) {
#ifdef ASCII_CTYPE
	  if (!isascii(*s1) || !isascii(*s2))
	    d = *s1 - *s2;
	  else
#endif
	    d = (tolower((unsigned char)*s1) - tolower((unsigned char)*s2));
	  if ( d != 0 || *s1 == '\0' || *s2 == '\0' )
	    return d;
	  ++s1;
	  ++s2;
	}
	return(0);
}

/*
	function SplitArguments split string argument to a array.
	parameters:
		arginfo	: Asterisk introduction arguments list.It is compart by char '|' .
		argArray : a string array,it will store each argument.
		maxsize	: the max size of argArray.
		spliter : it is a char witch arginfo used.default is '|'.
*/
int split_arguments(const char *arginfo,char argArray[][MAX_ARG_LEN],int maxsize,char spliter)
{
	int index =0;
	char *ps, *pe;

	//printf("\nSplitArguments  %s\n",arginfo);
	ps = arginfo;//strchr(arginfo,spliter);
	while(ps && index < maxsize)
	{
		if(*ps == spliter)ps++;
		pe = strchr(ps,spliter);
		if(pe){
			int len = pe-ps;
			strncpy(argArray[index++],ps,len < MAX_ARG_LEN? len : MAX_ARG_LEN-1);
			ps = pe + 1;//strchr(pe,spliter);
		}else{
			if(strlen(ps) > 0)
				strncpy(argArray[index++],ps,MAX_ARG_LEN-1);
			break;
		}
	}
	return index;
}


/**
 * 串口操作的函数库
 */

/*
 * 设置串口的波特率参数
 */
static void com_set_speed(int fd, int speed) {
	int i;
	int status;
	struct termios Opt;
	int speed_arr[] = {B576000, B500000, B460800, B230400,B115200, B57600, B38400, B19200,
			B9600, B4800, B2400, B1800,  B1200 ,B600, B300, };
	int name_arr[] = {576000, 500000, 460800, 230400, 115200, 57600, 38400, 19200,
			9600, 4800, 2400,1800, 1200, 600, 300, };

	//检查串口是否打开如果没有打开则返回，什么也不做
	if(fd<=0){
		return;
	}
	tcgetattr(fd, &Opt); //用来得到机器原端口的默认设置
	for (i = 0; i < sizeof(speed_arr) / sizeof(int); i++) {
		if (speed == name_arr[i]) {
			tcflush(fd, TCIOFLUSH); //刷新输入输出缓冲
			cfsetispeed(&Opt, speed_arr[i]);
			cfsetospeed(&Opt, speed_arr[i]);
			status = tcsetattr(fd, TCSANOW, &Opt);
			if (status != 0)
				perror("set com speed error:tcsetattr.");
			tcflush(fd, TCIOFLUSH);
			return;
		}
	}
}

/**
 * 设置串口优先级
 *@param  fd     类型  int  打开的串口文件句柄*
 *@param  databits 类型  int 数据位   取值 为 7 或者8*
 *@param  stopbits 类型  int 停止位   取值为 1 或者2*
 *@param  parity  类型  int  效验类型 取值为N,E,O,,S
 */
static int com_set_parity(int fd, int databits, int stopbits, char parity) {
	struct termios options;

	//检查串口是否打开如果没有打开则返回，什么也不做
	//__android_log_print(ANDROID_LOG_INFO, "utils", "com_set_parity(%d,数据位%d,停止位%d,校验类型%c)",fd,databits,stopbits,parity);
	if(fd<=0){
		return FALSE;
	}
	if (tcgetattr(fd, &options) != 0) {
		perror("SetupSerial 1");
		return FALSE;
	}
	options.c_cflag &= ~CSIZE;
	switch (databits) /*设置数据位数*/
	{
	case 7:
		options.c_cflag |= CS7;
		break;
	case 8:
		options.c_cflag |= CS8;
		break;
	default:
		perror("Unsupported data size\n");
		return FALSE;
	}
	switch (parity) {
	case 'n':
	case 'N':
		options.c_cflag &= ~PARENB; //Clear parity enable
		options.c_iflag &= ~INPCK; //Enable parity checking
		break;
	case 'E':
		options.c_cflag |= PARENB; // Enable parity
		options.c_cflag &= ~PARODD; // 转换为偶效验
		options.c_iflag |= INPCK; // Disnable parity checking
		break;
	default:
		perror("Unsupported parity\n");
		return FALSE;
	}

	/* 设置停止位*/
	switch (stopbits) {
	case 1:
		options.c_cflag &= ~CSTOPB;
		break;
	case 2:
		options.c_cflag |= CSTOPB;
		break;
	default:
		perror("Unsupported stop bits\n");
		return FALSE;
	}

	options.c_cc[VTIME] = 0; //150; // 15 seconds
	options.c_cc[VMIN] = 0;
	//options.c_cc[VMIN] = 1;                  //read()到一个char时就返回
	tcflush(fd, TCIFLUSH); //Update the options and do it NOW

	options.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG | IEXTEN);
	options.c_iflag &= ~(BRKINT | ICRNL | INPCK | ISTRIP | IXON);
	options.c_oflag &= ~(OPOST);

	if (tcsetattr(fd, TCSANOW, &options) != 0) //使端口属性设置生效
	{
		perror("SetupSerial 3");
		return FALSE;
	}
	tcflush(fd, TCIOFLUSH);

	return TRUE;
}

/**
 * 打开串口的函数
 * @param Dev  串口字符串，字符串格式为:	com=/dev/ttyUSB0(串口设备字符串),s=9600(波特率),p=N(奇偶校验),b=1(停止位),d=8(数据位数)
 * @return
 * 		返回串口句柄，如果失败则返回值<=0
 */
int com_open(char *dev) {
	int fd;
	char arg[5][MAX_ARG_LEN];
	char *com="/dev/ttyUSB0",*s="115200",*p="N",*b="1",*d="8";

	int r = 0;
	memset(arg,0,5*MAX_ARG_LEN);
	int argc = split_arguments(dev,arg,5,',');

	switch(argc){
	case 0:
		break;
	case 1:
		com = arg[0];
		break;
	case 2:
		com = arg[0];
		s = arg[1];
		break;
	case 3:
		com = arg[0];
		s = arg[1];
		p = arg[2];
		break;
	case 4:
		com = arg[0];
		s = arg[1];
		p = arg[2];
		b = arg[3];
		break;
	default:
		com = arg[0];
		s = arg[1];
		p = arg[2];
		b = arg[3];
		d = arg[4];
		break;
	}
	fd = open(com, O_RDWR | O_NOCTTY | O_NDELAY);

	if (fd<=0) {
		__android_log_print(ANDROID_LOG_INFO, "utils","Can't Open Serial Port %s:%s\n",com,strerror(errno));
	} else {
		//int pn = (p[0] != 'N') || (p[0] != 'n');
		char pn= p[0];
		//__android_log_print(ANDROID_LOG_INFO, "utils", "波特率 %s",s);
		com_set_speed(fd, atoi(s));
		if (com_set_parity(fd, atoi(d), atoi(b), pn)==FALSE)
		{
			perror("设置串口参数出错");
			com_close(fd);
			return 0;
		}
	}
	return fd;
}

/**
 * 枚举所有可用串口的函数,标准C中的文件结构中文件名是13个字符。
 * @param filter 查找串口的过滤字符串，如"/dev/ttyUSB*"表示查找所有以ttyUSB开头的设备
 * @param  ofc  回调函数
 * @param param 调用回调函数时提供的上下文相关的参数
 */
void each_comm(char *filter,ON_FIND_COMM ofc,void *param)
{
	/*struct ffblk ff;
	int done;

	if(ofc == NULL)return;
	done = findfirst(filter,&ff,0);
	while(!done)
	{
		if(ff.ff_attrib&FA_DIREC != FA_DIREC)
			ofc(param,ff.ff_name);
		done=findnext(&ff);
	}*/
}

void com_close(int fd)
{
	//检查串口是否打开如果没有打开则返回，什么也不做
	if(fd>0){
		if(close(fd)==-1)
			__android_log_print(ANDROID_LOG_INFO, "utils","Close Serial Port:%s\n",strerror(errno));
	}
}
/**
 * 从串口接收信息的函数
 * @param fd  串口句柄
 * @param buf 存放接收内容的缓冲区
 * @param maxLen 存放接收内容缓冲区的大小
 */
int com_recive(int fd,char *buf,int maxLen,int timeout)
{
	fd_set rfds;
	int len=0,r=0;
	struct timeval tv;

	//检查串口是否打开如果没有打开则返回，什么也不做
	if(fd<=0){
		return -1;
	}

	FD_ZERO(&rfds);
	FD_SET(fd,&rfds);
	//__android_log_print(ANDROID_LOG_INFO, "utils", "Select %d",fd);
	if(timeout == -1){
		r = select(fd+1,&rfds,NULL,NULL,NULL);
	}else{
		memset(&tv,0,sizeof(tv));

	    tv.tv_sec = timeout/1000000;
	    tv.tv_usec = timeout%1000000;//1000000us = 1s
		r = select(fd+1,&rfds,NULL,NULL,&tv);
	}
	if(r==-1){
		//发生错误
		tcflush(fd, TCIOFLUSH);
		//__android_log_print(ANDROID_LOG_INFO, "utils", "Select return %d(timeout=%d)",r,timeout);
		return r;
	}else if (r==0)
	{
		//等待超时
		tcflush(fd, TCIOFLUSH);
		//__android_log_print(ANDROID_LOG_INFO, "utils", "Select return %d(timeout=%d)",r,timeout);
		return r;
    }else{
        if(FD_ISSET(fd,&rfds)){
        	len = read(fd,buf,maxLen);
        	if(len > 0 && 0){
    			char hexbuf[512];
    			int dlen=sizeof(hexbuf);

    			memset(hexbuf,0,sizeof(hexbuf));
    			HexEncode(buf,len,hexbuf,&dlen);
        		__android_log_print(ANDROID_LOG_INFO, "utils", "Recive data(%d) from fd=%d,hexbuf=%s",len,fd,hexbuf);
        	}
			return len;
        }else{
        	return -1;
        }
    }
	return r;
}

int com_write(int fd,char *buf,int len)
{
	//检查串口是否打开如果没有打开则返回，什么也不做
	if(fd<=0){
		return -1;
	}
	if(0){
		char hexbuf[512];
		int dlen=sizeof(hexbuf);

		memset(hexbuf,0,sizeof(hexbuf));
		HexEncode(buf,len,hexbuf,&dlen);
		__android_log_print(ANDROID_LOG_INFO, "utils", "Send data(%d) from fd=%d,hexbuf=%s",len,fd,hexbuf);
	}
	return write(fd,buf,len);
}

int com_select(int fd,int sec, int usec)
{
	fd_set rfds;
	int len=0,r=0;
	struct timeval tv;

	//检查串口是否打开如果没有打开则返回，什么也不做
		if(fd<=0){
			return -1;
		}

		FD_ZERO(&rfds);
		FD_SET(fd,&rfds);
		//__android_log_print(ANDROID_LOG_INFO, "utils", "Select %d",fd);

		memset(&tv,0,sizeof(tv));

		tv.tv_sec = sec;
		tv.tv_usec = usec;   //1000000us = 1s
		r = select(fd+1,&rfds,NULL,NULL,&tv);

		if(r==-1){
			//发生错误
			tcflush(fd, TCIOFLUSH);
			//__android_log_print(ANDROID_LOG_INFO, "utils", "Select return %d(timeout=%d)",r,timeout);
			return r;
		}else if (r==0)
		{
			//等待超时
			tcflush(fd, TCIOFLUSH);
			//__android_log_print(ANDROID_LOG_INFO, "utils", "Select return %d(timeout=%d)",r,timeout);
			return r;
	    }else{
	    	return TRUE;
	    }
	return r;
}

unsigned int get_ip_addr(char *ipaddr)
{
	unsigned int iip = 0;
	unsigned char *ip = (unsigned char *)&iip;
	char arg[4][MAX_ARG_LEN];
	memset(arg,0,4*MAX_ARG_LEN);
	split_arguments(ipaddr,arg,4,'.');
	ip[0] = atoi(arg[0]);
	ip[1] = atoi(arg[1]);
	ip[2] = atoi(arg[2]);
	ip[3] = atoi(arg[3]);
	return iip;
}

/*
	**************************************************************************
	概述
		解析网络传输地址
	**************************************************************************
*/
unsigned int ParseIPAddr(char *cAddr,unsigned short *port)
{
	char Buf[50];
	char *p=Buf,*ppos=NULL;

	strcpy(Buf,cAddr);
	ppos = strchr(p,':');
	if(ppos == NULL)
		*port = 0;
	else{
		*port = atoi(ppos+1);
		*ppos = '\0';
	}
	return get_ip_addr(Buf);
}

// 获取当前微秒数
int GetUsecTime()
{
	int time = 0;
	struct timeval t;

    gettimeofday(&t, 0);

    time = t.tv_sec * 1000 * 1000 + t.tv_usec;
    return time;
}

