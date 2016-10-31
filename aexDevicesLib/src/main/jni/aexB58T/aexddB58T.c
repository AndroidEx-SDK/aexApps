#include     <stdio.h>      /*标准输入输出定义*/
#include     <unistd.h>     /*Unix标准函数定义*/
#include     <sys/types.h>  /**/
#include     <sys/stat.h>   /**/
#include     <fcntl.h>      /*文件控制定义*/
#include     <termios.h>    /*PPSIX终端控制定义*/
#include     <errno.h>      /*错误号定义*/
#include     <sys/ioctl.h>
#include     <ctype.h>
#include     <string.h>
#include	 <stdlib.h>

#include <android/log.h>
#include <dirent.h>

#include "aexddB58T.h"
#include	"../include/utils.h"


#define MAX_BUFF 2048

static ON_PRINT_EVENT on_print_event=NULL;

/**
 * 设置回调函数，在JNI的代码里会调用它来设置处理事件的回调函数
 */
void aexddB58T_set_event(ON_PRINT_EVENT oke)
{
	on_print_event = oke;
}

/**
 * 打印机事件的入口函数，静态函数只能在本文件中调用
 */
static int aexddB58T_event(PRINT_HANDLE print,HKKP env,HKKP obj,int code,char *pszFormat,...)
{
	char pszDest[MAX_BUFF];
	va_list args;

	va_start(args, pszFormat);
	vsnprintf(pszDest, sizeof(pszDest), pszFormat, args);
	va_end(args);
	//只有设置了事件回调函数，此函数才会调用事件，否则什么也不做
	if(on_print_event){
		return on_print_event(print,env,obj,code,pszDest);
	}else{
	    return 0;
	}
}


/*
 * 说明：将java传入的字符串指令转换位16进制指令
 * */
//void aexddB58T_hexEncode(const unsigned char *pbSrcData, int nSrcLen, char *szDest, int *pnDestLen)

int aexddB58T_hexDecode(PRINT_HANDLE print,HKKP env,HKKP obj)
{
	char *pin2="1122334455667788";
	char pout2[10]={0};
	int ilen2=sizeof(pout2);

	int i;
	char buff[MAX_BUFF] = {0};

	memset(buff, 0, sizeof(buff));
	HexDecode(pin2,strlen(pin2),(unsigned char*)&pout2[0],&ilen2);

	for(i=0;i<ilen2;i++){
		//__android_log_print(ANDROID_LOG_DEBUG,"kkp","pout2[%d]=%02x",i,pout2[i]);
		sprintf(&buff[0],"\\x%02x",pout2[i]);
	}
	//__android_log_print(ANDROID_LOG_DEBUG,"kkp","buff=%s",buff);

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

static int print_send_cmd(int fd,char *cmd,int size)
{
	char chSendCmd[512];
	unsigned int nLen, ret;

	tcflush(fd,TCIOFLUSH);
	memset(chSendCmd, '\0', sizeof(chSendCmd));

	nLen = sizeof(chSendCmd);
	HexEncode(cmd,size,chSendCmd,&nLen);

	 __android_log_print(ANDROID_LOG_DEBUG,"print","sendCmd(%s)",chSendCmd);
	ret = com_write(fd, chSendCmd, strlen(chSendCmd));
	return ret;
}

/**
 * 打开打印机，返回0失败   其他成功
 * @aexddB58Tram arg 串口参数字符串，字符串格式为:com=/dev/ttyUSB0(串口设备字符串),s=115200(波特率),p=N(奇偶校验),b=1(停止位),d=8(数据位数)
 */
PRINT_HANDLE aexddB58T_open(HKKP env,HKKP obj,char* arg)
{
	int fd = com_open(arg);
	//__android_log_print(ANDROID_LOG_DEBUG,"kkp","Open fd= %d,path= %s ",fd,arg);

	if(fd > 0){
		PRINT_HANDLE print = (PRINT_HANDLE)malloc(sizeof(PRINT_DATA));
		if(print){
			memset(print,0,sizeof(PRINT_DATA));
			print->fd = fd;
			strcpy(print->port,arg);
			//__android_log_print(ANDROID_LOG_DEBUG,"kkp","open success,print=%d",print);
			return print;
		}else{
			com_close(fd);
			//return NULL;
			//__android_log_print(ANDROID_LOG_DEBUG,"kkp","open fail,print=%d",print);
			return NULL;
		}
	}else{
		//return NULL;
		//__android_log_print(ANDROID_LOG_DEBUG,"kkp","open fail,print=%d",print);
		return NULL;
	}
}

/**
 * 函数名：aexddB58T_close
 * 参数：
 * 返回值：0，成功；其他为失败；
 * 说明：关闭打印机串口
 */
int aexddB58T_close(PRINT_HANDLE print,HKKP env,HKKP obj)
{
	if(print){
		if(print->fd)
			com_close(print->fd);
		free(print);
		return 0;
	}
	return -1;
}

/****************************************标准ESC/POS指令集******************************
命 令 名 称
@1.PM58,ET58 LF 打印并换行                                          0x0a
@2.PM58,ET58 ESC ! n 设置字符打印方式                                1B 21 n
@3.PM58,ET58 ESC 3 n 设置行间距为n点行（n/203英寸）                    1B 33 n ,设置行间距为n点行。n=0～255
@4.PM58,ET58 ESC @ 初始化打印机                                     1B 40
@5.PM58,ET58 ESC SO 设置字符倍宽打印                                 1B 0E ,在一行内该命令之后的所有字符均以正常宽度的2倍打
@6.PM58,ET58 ESC DC4 取消字符倍宽打印                                1B 14 ,执行此命令后，字符恢复正常宽度打
@7.PM58,ET58 ESC * m n1 n2 d1⋯dk 设定点图命令                       1B 2A m n1 n2 [d]k,
@8.PM58,ET58 GS w n 设置条码宽度                                     1D 77 n,设置条形码水平尺寸，2 £  n £ 6
@9.PM58,ET58 GS h n 设置条形码高度                                   1D 68 n,设置条形码高度，1 £  n £ 255。
@10.PM58,ET58 GS k m d1 ... dk NUL ② GS k m n d1 ... dn 打印条形码   1D 6B m n d1 .. dn
@11.PM58,ET58 打印英文  写入缓冲区直接打印  0x0a
****PM58,ET58****相同功能，指令不同************
@51.PM58 US <7> H [cn] +[data] 设置和打印QR条码                  1F 07 48，cn : 01 设置版本, 02 纠错等级，03 模块宽, 04传送QR数据, 05 打印QR数据 , 08设置是否为连接模式
    ET58 十六进制码   1D      6B     m    d1 ... dk   00  ,1D      6B     m    n    d1 ... dn
@52.PM58 GS V m 选择切纸方式并切纸                                1D 56 m，m ＝ 0，全切纸；
	ET58 ESC  I or ESC m十六进制：1b 69 或1b 6d
@53.ET58 十六进制：1B    64    n打印行缓冲器里的数据并向前走纸n字符行。n=0～255
	PM58 ESC J n 打印并进纸n点行     1B 4A n  ,n=0～255。该命令只在本行打印有效
@54.打印中文
    PM58 写入缓冲区直接打印  0x0a
	ET58 进入汉字模式 十六进制：1C   26
**********************************************************************************/
/*@1.PM58,ET58 LF 打印并换行                                         0x0a
 * 打印中文或字母	更改打印规则，该函数打印后自动换行。
 * 比如 ： "你"字对应的 gb2312编码为  0xc4 0xe3 ,"好"字对应的gb2312码为   0xba 0xc3 , "!"符号对应的 ascll码为 0x21 , "a"字母对应0x61
 * 要打印 "你好!abc" 则  *pch="\0xc4\0xe3\0xba\0xc3\0x21\0x61\0x62\0x63" ibytelen=strlen(pch);
 *
 */
/*
 * 打印并换行
 * n  行数
 * 为防止n过大导致资源浪费，n最大为10
 */
int aexddB58T_newline(PRINT_HANDLE print,HKKP env,HKKP obj,int n){
	int i;
	char buff[MAX_BUFF];
	if( !print || n<1 || n>10) return -1;

	memset(buff, 0, sizeof(buff));
	for(i=0;i<n;i++){
		buff[i]=0x0a;
	}
	if(com_write(print->fd, buff, n))
		return 0;
	else
		return -1;
}

/**
 * 选择字符大小
 * @2.PM58,ET58 ESC ! n 设置字符打印方式                                1B 21 n
 * 字符打印方式设置命令，用于选择打印字符的大小。
 * n的范围0≤n≤255,0 ≤ n ≤ 255(1 ≤ 垂直倍数 ≤ 8, 1 ≤ 水平倍数 ≤ 8)
 * 用0到2位设定字符高度，4到6位设定字符宽度。如下所示：
 * @param n 设置字符大小
 * 10  2倍宽    01 2倍高
 */
int aexddB58T_set_fontsize(PRINT_HANDLE print,HKKP env,HKKP obj,int isize){
	char buff[MAX_BUFF];
	if (!print)
		return -1;
	memset(buff, 0, sizeof(buff));
	sprintf(&buff[0], "\x1d\x21");
	buff[2]=isize&0xff;

	if(com_write(print->fd, buff,3))
		return 0;
	else
		return -1;
}

/**
 * 设置汉字字符模式
 * 04  选择倍宽
 * 08  选择倍高
 *
 * @param n 设置字符大小
 * 10  2倍宽    01 2倍高    00  取消倍宽倍高
 */
int aexddB58T_setmode(PRINT_HANDLE print,HKKP env,HKKP obj,int isize){
	char buff[MAX_BUFF];
	if (!print)
		return -1;
	memset(buff, 0, sizeof(buff));
	sprintf(&buff[0], "\x1b\x21");
	buff[2]=isize&0xff;

	if(com_write(print->fd, buff, 3))
		return 0;
	else
		return -1;
}

/**
 * @3.PM58,ET58 ESC 3 n 设置行间距为n点行（n/203英寸）                    1B 33 n ,设置行间距为n点行。n=0～255
 * 设置行间距为n 点行 （n ∕203 英寸） 默认n=30
 * @param n 行间距
 */
int aexddB58T_set_linewide(PRINT_HANDLE print,HKKP env,HKKP obj,int isize){
	char buff[MAX_BUFF];
	if (!print)
		return -1;
	memset(buff, 0, sizeof(buff));
	sprintf(&buff[0], "\x1b\x33");
	buff[2]=isize&0xff;

	if(com_write(print->fd, buff, 3))
		return 0;
	else
		return -1;
}

/**
 *@4.PM58,ET58 ESC @ 初始化打印机                                     1B 40
 * 函数名：aexddB58T_initialize
 * 参数：
 * 返回值：0，成功；其他为；
 * 说明：初始化打印机
 */
int aexddB58T_initialize(PRINT_HANDLE print,HKKP env,HKKP obj)
{
	char buff[MAX_BUFF] ;
	if (!print)
		return -1;
	memset(buff,0,sizeof(buff));
	sprintf(&buff[0],"\x1b\x40");
	//__android_log_print(ANDROID_LOG_DEBUG,"kkp","aexddB58T_initialize");

	if(com_write(print->fd, buff, 2))
		return 0;
	else
		return -1;
}

/*
 * @5.PM58,ET58 ESC SO 设置字符倍宽打印                                 1B 0E ,在一行内该命令之后的所有字符均以正常宽度的2倍打
 * 在一行内该命令之后的所有字符均以正常宽度的2 倍打印
 */
int aexddB58T_set_charDSize(PRINT_HANDLE print,HKKP env,HKKP obj)
{
	char buff[MAX_BUFF];

	if(!print)	return 0;
	memset(buff, 0, sizeof(buff));
	sprintf(&buff[0],"\x1b\x0e");
	if(com_write(print->fd, buff, 2)){
		return 0;
	}else{
		return -1;
	}
}

/*
*@6.PM58,ET58 ESC DC4 取消字符倍宽打印                                1B 14 ,执行此命令后，字符恢复正常宽度打
*
* 恢复正常宽度打印
*/
int aexddB58T_set_charNSize(PRINT_HANDLE print,HKKP env,HKKP obj)
{
	char buff[MAX_BUFF];

	if(!print)	return 0;
	memset(buff, 0, sizeof(buff));
	sprintf(&buff[0],"\x1b\x14");
	if(com_write(print->fd, buff, 2)){
	    return 0;
	}else{
		return -1;
	}
}

/*
@7.PM58,ET58 ESC * m n1 n2 d1⋯dk 设定点图命令                       1B 2A m n1 n2 [d]k,
@设定点图命令
*/
int aexddB58T_set_graph(PRINT_HANDLE print,HKKP env,HKKP obj ,char m, char n1, char n2, char* chGPI)
{
	char buff[MAX_BUFF];

	if(!print)	return 0;
	memset(buff, 0, sizeof(buff));
	sprintf(&buff[0],"\x1b\x2a%c%c%c",m,n1,n2);
	com_write(print->fd, buff, 5);
	if(com_write(print->fd, (char*) chGPI, strlen(chGPI))){
		return 0;
	}else{
		return -1;
	}
}

/*
 * @8.PM58,ET58 GS w n 设置条码宽度                                     1D 77 n,设置条形码水平尺寸，2 £  n £ 6
 *
 * */
int aexddB58T_set_barcodeWide(PRINT_HANDLE print,HKKP env,HKKP obj, char n)
{
	char buff[MAX_BUFF];

	if(!print)	return 0;
	memset(buff, 0, sizeof(buff));
	sprintf(&buff[0],"\x1b\x77%c",n);
	if(com_write(print->fd, buff, 3)){
		return 0;
	}else{
		return -1;
	}
}

/*
 *@9.PM58,ET58 GS h n 设置条形码高度                                   1D 68 n,设置条形码高度，1 £  n £ 255。
 *设定条码宽度,水平方向点数 2<=n<=6,缺省值为3
 * */
int aexddB58T_set_barcodeHigh(PRINT_HANDLE print,HKKP env,HKKP obj, char n)
{
	char buff[MAX_BUFF];

	if(!print)	return 0;
	memset(buff, 0, sizeof(buff));
	sprintf(&buff[0],"\x1d\x68%c",n);
	if(com_write(print->fd, buff, 4)){
		return 0;
	}else{
		return -1;
	}
}

/*
 * @10.PM58,ET58 GS k m d1 ... dk NUL ② GS k m n d1 ... dn 打印条形码   1D 6B m n d1 .. dn
 * 打印条形码
 * */
int aexddB58T_print_barcode(PRINT_HANDLE print,HKKP env,HKKP obj,char wide ,char high,char code,char* data, int len)
{
	char buff[MAX_BUFF];

	if(!print)	return 0;
	if(len > 255 && len <1 ) return 0;
	memset(buff, 0, sizeof(buff));
	sprintf(&buff[0],"\x1d\x77%c\x1d\x68%c\x1d\x6b%c",wide,high,code);
	buff[9]=len&0xff;

	strncat(&buff[0],data,strlen(data));
	strncat(buff,"\xa",1);

	if(com_write(print->fd, &buff[0], strlen(buff))){
		return 0;
	}else{
		return -1;
	}
}

/*
 @11.PM58,ET58 打印英文  写入缓冲区直接打印  0x0a
 */
/*打印英文*/
int aexddB58T_print_en(PRINT_HANDLE print,HKKP env,HKKP obj,char* pEn,int length)
{
	char buff[MAX_BUFF] ;
	if(!print)	return 0;
	memset(buff,0,sizeof(buff));
	strncpy(&buff[0],pEn,length);
	if(com_write(print->fd, buff, strlen(buff))){
		return 0;
	}else{
		return -1;
	}
}

/******************************************
 * **PM58,ET58****相同功能，指令不同***********
 ***************************************** */
/*
@51.PM58 US <7> H [cn] +[data] 设置和打印QR条码   1F 07 48，cn : 01 设置版本, 02 纠错等级，03 模块宽, 04传送QR数据, 05 打印QR数据 , 08设置是否为连接模式
    ET58 十六进制码   1D      6B     m    d1 ... dk   00  ,1D      6B     m    n    d1 ... dn
 * 打印二位码
 * @param ch	二维码参数	size,ver,lv,nl,nh
 * @param size  打印点大小(3-6)  ver:QR码型号(0-40)  lv:QR纠错等级(0-3)  nl,nh:QR码的打印数据长度 (nl+nh*256)<400
 *
 */
int aexddB58T_print_2Dimensional(PRINT_HANDLE print,HKKP env,HKKP obj,char *content,int ilen){
    char buff[MAX_BUFF];
    if (!print || ilen>400)
        return -1;
    memset(buff, 0, sizeof(buff));
    sprintf(&buff[0], "\x1f\x1c\x08\x01");

    buff[4]=ilen&0xff;
    buff[5]= 0x00;

    if(com_write(print->fd, buff, 6)){
        com_write(print->fd, content, ilen);
        return 0;
    }
    else
        return -1;
}

/*
QRCODE二维条码功能:
10、QR Code：设置单元大小
--------------------------------------------------------------------------------------------
 【指令代码 】
    ASCII   ：GS  (  0  g  n
    十进制  ：29  40  107  48  103  n
    十六进制：1D  28  6B  30  67   n

   功能描述   设置QR Code 的单元大小为n 点
   参数范围   1  ≤ n ≤ 16
   默认值     n = 3
   注意事项   n 点 = 单元宽度 = 单元高度
              当ESC @、打印机复位、断电后，本指令的设置失效

11、QR Code：设置错误纠正等级
--------------------------------------------------------------------------------------------
 【指令代码 】
    ASCII   ：GS  (  0  i  n
    十进制  ：29  40  107  48  105  n
    十六进制：1D  28  6B  30  69   n

   功能描述   设置QR Code的错误纠正等级，n参数意义如下：

n	说明	纠正比例（%）
48	 等级L	7
49	 等级M	15
50	 等级Q	25
51	 等级H	30

   参数范围   48  ≤ n  ≤ 51
   默认值     n = 48
   注意事项   QR Code 采用RS算法生成错误纠正码；
              当打印机复位、断电后，本指令的设置失效

12、QR Code：传输数据至编码缓存
--------------------------------------------------------------------------------------------
 【指令代码 】
    ASCII   ：GS  (  0  €  nL nH d1...dk
    十进制  ：29  40  107  48  128  nL nH d1...dk
    十六进制：1D  28  6B  30  80   nL nH d1...dk

   功能描述   传输QR Code的数据（d1…dk）到编码缓存
   参数范围   4  ≤ (nL + nH×256) ≤ 2710
              32  ≤ d  ≤ 255
   默认值     无
   注意事项   接收后，数据保留至下次重新设置
    k字节d1…dk 被视为编码数据
    d1…dk不能包含以下表格除外的数据
字符集	 包含的字符
数字	“0”~“9”
字母和数字	“0”~“9”，“A”~“Z”，SP，$，%，*，+，-，。，、，：

汉字	GB18030-2000, Shift-JIS（JISX0208标准)
8位数据	00H~FFH
    当ESC @、打印机复位、断电后，本指令的设置失效

13、QR Code：打印编码缓存的二维条码
--------------------------------------------------------------------------------------------
 【指令代码 】
    ASCII   ：GS  (  0  ?
    十进制  ：29  40  107  48  129
    十六进制：1D  28  6B  30  81

   功能描述   打印QR Code编码缓存的编码数据
   参数范围   无
   默认值     无
   注意事项   若二维码尺寸超出打印区域，打印任务将取消
            若编码缓存数据为空，打印任务将取消
*/

//   打印二维码例程
//   #HEX
//   1D 28 6B 30 67 07
//   1D 28 6B 30 69 48
//   1D 28 6B 30 80 0a 00
//   #TXT
//   热敏打印机
//   #HEX
//   1D 28 6B 30 81

int aexddB58T_SGT801_print_2Dimensional(PRINT_HANDLE print,HKKP env,HKKP obj,int isize,char *content,int ilen){
    char buff[MAX_BUFF];
    char value[MAX_BUFF];
    char data[MAX_BUFF];
    if (!print || ilen>400)
        return -1;
    memset(buff, 0, sizeof(buff));
    memset(value, 0, sizeof(value));
    memset(data, 0, sizeof(data));
    sprintf(&buff[0], "\x1D\x28\x6B\x30\x67\x07\x1D\x28\x6B\x30\x69\x48\x1D\x28\x6B\x30\x80\0a\x00");
    buff[5] = isize & 0xff;
    buff[17] = ilen & 0xff;
    sprintf(&data[0],content,ilen);

    strncpy(&value[0],"\x1D\x28\x6B\x30\x81",5);


    if(com_write(print->fd, buff, 19)){
        if(com_write(print->fd, data, ilen))
            com_write(print->fd, value, 5);
        return 0;
    }
    else
        return -1;
}

// 打印位图
int aexddB58T_print_bitmap(PRINT_HANDLE print,HKKP env,HKKP obj,const unsigned char *data, int iLen, int iLine){


    int iNum = 1;
    int m = 21;
    int iLeft = iLen;
    unsigned char printBuff[1024] = {0};
    memset(printBuff,0,sizeof(printBuff));

    int nL = 0;
    int nH = 0;

    nH = iLine/256;
    nL = iLine - nH*256;

    // 开始打印图片

    //初始化打印机
    aexddB58T_initialize(print,env,obj);

    // 设置行距
    if(aexddB58T_setLineinterval(print,env,obj,10))
    {
        return -1;
    }
    // 设置打印区域
    if(aexddB58T_setprintarea(print,env,obj,nL+1, nH))
    {
        return -1;
    }

    //__android_log_print(ANDROID_LOG_DEBUG,"print","data=%s",printBuff);

    while (iLeft > 0)
    {
        memset(printBuff,0,sizeof(printBuff));
        if (iLeft >= iLine)
        {
            memcpy(printBuff, data+(iNum * iLine), iLine);
            if (PrintBitmap(print,env,obj,m, printBuff, iLine))
            {
                return -1;
            }

        }
        iLeft -= iLine;
        iNum++;
    }
    /*
      aexddB58T_stepline(print,env,obj,"\x1b\x4a",30);        // 进纸(10*.0125mm)

        //==开始打印图片
        int iNum = 0;
        int m = 1;
        int iLeft = iLen;
        unsigned char printBuff[2048] = {'\0'};

        int nL = 0;
        int nH = 0;

        nH = iLine/256;
        nL = iLine - nH*256;

        //初始化打印机
        aexddB58T_initialize(print,env,obj);

        // 设置行距
        if(aexddB58T_setLineinterval(print,env,obj,10))
        {
            return -1;
        }
        // 设置打印区域
        if(aexddB58T_setprintarea(print,env,obj,nL+1, nH))
        {
            return -1;
        }

        while (iLeft > 0)
        {
            memset(printBuff, '\0', sizeof(printBuff));

            if (iLeft >= iLine)
            {
                memcpy(printBuff, data+(iNum * iLine), iLine);
                if (PrintBitmap(print,env,obj,m, printBuff, iLine))
                {
                    return -1;
                }
            }
            iLeft -= iLine;
            iNum++;
        }
        //===打印图片结束
    */

    return 0;
}

// 打印位图
int PrintBitmap(PRINT_HANDLE print,HKKP env,HKKP obj,int m,const unsigned char *pBitmap, int iLen)
{
    if (NULL == pBitmap)
    {
        return -1;
    }

    int nL = 0;
    int nH = 0;
    unsigned char chTemp[1024] = {'\0'};
    memset(chTemp,0,sizeof(chTemp));

    nH = iLen/256;
    nL = iLen - (nH * 256);

    chTemp[0] = (char)0x1B;
    chTemp[1] = (char)0x2A;
    chTemp[2] = (char)m;
    chTemp[3] = (char)nL;
    chTemp[4] = (char)nH;

    memcpy(chTemp+5, pBitmap, iLen);
    if(com_write(print->fd, chTemp,  iLen +5))
        return 0;
    else
        return -1;
}

// 设置间距
int aexddB58T_setLineinterval(PRINT_HANDLE print,HKKP env,HKKP obj,int n){
    if (!print) return -1;

    char buff[20];
    if (!print)
        return -1;
    memset(buff, 0, sizeof(buff));
    sprintf(&buff[0], "\x1B\x33");
    buff[2] = (char)n;
    if(com_write(print->fd, buff, 3))
        return 0;
    else
        return -1;
}

// 设置打印区域
int aexddB58T_setprintarea(PRINT_HANDLE print,HKKP env,HKKP obj,int nL,int nH){
    if (!print) return -1;

    char buff[20];
    if (!print)
        return -1;
    memset(buff, 0, sizeof(buff));
    sprintf(&buff[0], "\x1D\x57");
    buff[2] = (char)nL;
    buff[3] = (char)nH;
    if(com_write(print->fd, buff, 4))
        return 0;
    else
        return -1;
}

/**
 @52.PM58 GS V m 选择切纸方式并切纸                                1D 56 m，m ＝ 0，全切纸；
     ET58 ESC  I or ESC m十六进制：1b 69 或1b 6d
 * 切纸命令
 * @param n = 1全切纸  0部分切纸
 */
int aexddB58T_cut(PRINT_HANDLE print,HKKP env,HKKP obj, char* pcode,int iflag){
    char buff[20] = {0};

    if (!print)
        return -1;
    memset(buff, 0, sizeof(buff));

    if(iflag==1)
      sprintf(&buff[0], "\x1b\x69");
    else if(iflag==0)
       sprintf(&buff[0], "\x1b\x6d");
    else
        return -1;

    if(com_write(print->fd, buff, 2))
        return 0;
    else
        return -1;

}

/**
 @53.ET58 十六进制：1B    64    n打印行缓冲器里的数据并向前走纸n字符行。n=0～255
     PM58 ESC J n 打印并进纸n点行     1B 4A n  ,n=0～255。该命令只在本行打印有效
 * 打印行缓冲器里的内容，并向前n行   n=0--255
 * param n 走动的行数
 */
int aexddB58T_stepline(PRINT_HANDLE print,HKKP env,HKKP obj, char* pcode, int n)
{
    char buff[20] = {0};

    if(!print)	return 0;
    memset(buff,0,sizeof(buff));
    sprintf(&buff[0],"%s",pcode);
    buff[2]=n&0xff;

    if(com_write(print->fd, buff, 3)){
       return 0;
    }else{
       return -1;
    }
}

/*
 @54.打印中文
    PM58 写入缓冲区直接打印  0x0a
    ET58 进入汉字模式 十六进制：1C   26
 */
/*打印中文*/
int aexddB58T_print_ch(PRINT_HANDLE print,HKKP env,HKKP obj,char* pch,int ibytelen)
{
    char buff[MAX_BUFF] = {0} ;
    if (!print ||ibytelen <1)
        return -1;
    memset(buff,0,sizeof(buff));
    sprintf(&buff[0],"\x1c\x26");
    memcpy(&buff[2],pch,ibytelen);
    memcpy(&buff[2+ibytelen],"\x0a",1);

//		int i= 0;
//		for(i=0;i<ibytelen+3;i++){
//			 __android_log_print(ANDROID_LOG_DEBUG,"print","cmd[%d]=0x%02X, len=%d",i,(char)buff[i],ibytelen);
//		}

    if(com_write(print->fd, buff, ibytelen+3)){
        return 0;
    }
    else
        return -1;
}

/**************************** 厂商自定义指令集**************
* 一 .PM58
* ***************************************/
/*
@1,PM58 ESC 2 设置字符行间距为1/6英寸                            1B 32 设置行间距为1/6英寸
 * 设置字符行间距为 1/6 英寸
 */
int aexddB58T_set_charwide(PRINT_HANDLE print,HKKP env,HKKP obj)
{
	if(!print)	return 0;
	char ch[] = "\x1b\x32";
	if(com_write(print->fd, &ch[0], strlen(ch))){
		return 0;
	}else{
		return -1;
	}
}

/*@2，获取打印机厂商信息
 * 函数名：printGetFactory
 * 参数：@n = 1/49: 打印模式ID ，n = 2/50: 类型ID ，n = 3/51: 软件版本 ，n = 66: 生产厂商信息，n=67:打印机型号，n＝69:支持字符集
	GS I n 查询厂商信息命令                                  1D 49 n
	n值 打印机ID类型
	1，49 打印模式 ID
	2，50 类型 ID
	3，51 软件版本
	66 生产厂商信息
	67 打印机信号
	69 支持字符集
 * 说明：获取打印机厂商信息
 * 返回值：大于0，成功；0，失败；
 * */
int aexddB58T_getfactory(PRINT_HANDLE print, HKKP env, HKKP obj, int n)
{
	char buff[20];
	char rbuff[128];
	if (!print)
		return -1;
	memset(buff, 0, sizeof(buff));
	sprintf(&buff[0], "\x1D\x49");
	buff[2] = n & 0xff;
	if(com_write(print->fd, buff, strlen(buff))){
		if (com_recive(print->fd, &rbuff[0], 1, 1000)) {
				if(rbuff[0] & 0x60){		//检测 5、6位是否为1
					//__android_log_print(ANDROID_LOG_DEBUG,"print","无纸");

					return -1;
				}else{
					//__android_log_print(ANDROID_LOG_DEBUG,"print","有纸");
					return 0;
				}
	}else
		return -2;
	}
	return 0;
}

/*
@3，PM58 DLE EOT n 实时状态传送                               10 04 n ，1≤ n ≤ 4，该命令实时地传送打印机状态。参数n指定的打印机状态如下表所列：
n 值（十进制） 返回状态
1 打印机状态
2 脱机状态
3 错误状态
4 卷纸传感器状态
* */
/*
 * 获取打印机状态
 * n = 1: 传送打印机状态 ，n = 2: 传送脱机状态 ，n = 3: 传送错误状态 ，n = 4: 传送卷纸传感器状态
 * 目前获取状态失败，未返回任何数据，
 */
int aexddB58T_getstatus(PRINT_HANDLE print, HKKP env, HKKP obj, int n)
{
	char buff[20];
	char rbuff[128];
	if (!print)
		return -1;
	memset(buff, 0, sizeof(buff));
	memset(rbuff, 0, sizeof(rbuff));

	sprintf(&buff[0], "\x10\x04");
	buff[2] = n & 0xff;

	if(com_write(print->fd, buff, 3)){

		if (com_recive(print->fd, &rbuff[0],1, 1000)) {

			if(rbuff[0] & 0x60){		//检测 5、6位是否为1
				//__android_log_print(ANDROID_LOG_DEBUG,"print","无纸");

				return -1;
			}else{
				//__android_log_print(ANDROID_LOG_DEBUG,"print","有纸");
				return 1;
			}
		}
	}else
		return -2;
	return 0;
}

/*************************厂商自定义指令集*************************
 * 二 .ET58
 * *************************************************************/
/*
 * 设置排列方式:0-左对齐,1-居中,2-右对齐
 */
int aexddB58T_set_align(PRINT_HANDLE print,HKKP env,HKKP obj,int iflag){
	char buff[MAX_BUFF];
	if (!print || iflag<0 || iflag>2)
		return -1;
	memset(buff, 0, sizeof(buff));
	sprintf(&buff[0], "\x1b\x61");
	buff[2]=iflag&0xff;

	if(com_write(print->fd, buff, 3))
		return 0;
	else
		return -1;
}

///*
// * 打印并走纸点数
// * @param iflag  1向前走纸 ，0 退纸
// * @param isetp  走纸点数
// */
//int aexddB58T_steppoint(PRINT_HANDLE print,HKKP env,HKKP obj,int iflag,int istep)
//{
//	char buff[MAX_BUFF];
//	if (!print || istep<1)
//		return -1;
//
//	memset(buff, 0, sizeof(buff));
//
//	if(iflag==1)
//		sprintf(&buff[0], "\x1b\x4a");
//	else if(iflag==0)
//		sprintf(&buff[0], "\x1b\x6a");
//	else
//		return -1;
//
//	buff[2]=istep&0xff;
//	if(com_write(print->fd, buff, strlen(buff)))
//		return 0;
//	else
//		return -1;
//}

int aexddB58T_T_500AP_print_barcode(PRINT_HANDLE print,HKKP env,HKKP obj,char wide ,char high,char code,char* data, int len)
{
	char buff[MAX_BUFF];

	if(!print)	return 0;
	if(len > 255 && len <1 ) return 0;
	memset(buff, 0, sizeof(buff));
	sprintf(&buff[0],"\x1d\x77%c\x1d\x68%c\x1d\x6b%c",wide,high,code);
	buff[9]=((len+2)&0xff);
	buff[10]=0x78;
	buff[11]=0x42;

	strncat(&buff[0],data,strlen(data));
//	strncat(buff,"\xa",1);

	if(com_write(print->fd, &buff[0], strlen(buff))){
		return 0;
	}else{
		return -1;
	}
}

int aexddB58T_T_500AP_print_2Dimensional(PRINT_HANDLE print,HKKP env,HKKP obj,char *content,int ilen)
{
	char buff[MAX_BUFF];
	char temp[20] = {0};
	if (!print || ilen>400)
		return -1;
	memset(buff, 0, sizeof(buff));
	memset(temp, 0, sizeof(temp));
	sprintf(&buff[0], "\x1d\x01\x03\x06\x1d\x01\x04\x32\x1d\x01\x01");

	buff[11]=ilen&0xff;
	buff[12]= 0x00;

	sprintf(&temp[0], "\x1d\x01\x02\x0a");

	if(com_write(print->fd, buff, 13)){
		com_write(print->fd, content, ilen);
		com_write(print->fd, temp, 4);
		return 0;
	}
	else{
		return -1;
	}
}

int aexddB58T_RG_CB532_print_barcode(PRINT_HANDLE print,HKKP env,HKKP obj,char wide ,char high,char code,char* data, int len)
{
	char buff[MAX_BUFF];

	if(!print)	return 0;
	if(len > 255 && len <1 ) return 0;
	memset(buff, 0, sizeof(buff));
	sprintf(&buff[0],"\x1d\x77%c\x1d\x68%c\x1d\x6b%c",wide,high,code);
	buff[9]=((len+2)&0xff);
	//buff[10]=0x78;
	//buff[11]=0x42;
	//CODE B  字符集
	buff[10]=0x7B;
	buff[11]=0x42;

	strncat(&buff[0],data,strlen(data));
//	strncat(buff,"\xa",1);

	if(com_write(print->fd, &buff[0], strlen(buff))){
		return 0;
	}else{
		return -1;
	}
}

int aexddB58T_RG_CB532_print_2Dimensional(PRINT_HANDLE print,HKKP env,HKKP obj,char *content,int ilen)
{
	char buff[MAX_BUFF];
	char temp[20] = {0};
	if (!print || ilen>400)
		return -1;
	memset(buff, 0, sizeof(buff));
	memset(temp, 0, sizeof(temp));
	sprintf(&buff[0], "\x1d\x5a\x02\x1d\x77\06\x1d\x6b\x61\x00\x01");

	buff[11]=ilen&0xff;
	buff[12]= 0x00;

	sprintf(&temp[0], "\x0a");

	if(com_write(print->fd, buff, 13)){
		com_write(print->fd, content, ilen);
		com_write(print->fd, temp, 4);
		return 0;
	}
	else{
		return -1;
	}
}

int aexddB58T_TA500_print_2Dimensional(PRINT_HANDLE print,HKKP env,HKKP obj,char *content,int ilen)
{
	char command1[256]={0};
	char command2[256]={0};
	char command3[256]={0};
	char command4[256]={0};
	char command5[256]={0};
	char command6[256]={0};

//	if (!print || ilen>400)
//		return -1;

	memset(command1, 0, sizeof(command1));
	memset(command2, 0, sizeof(command2));
	memset(command3, 0, sizeof(command3));
	memset(command4, 0, sizeof(command4));
	memset(command5, 0, sizeof(command5));
	memset(command6, 0, sizeof(command6));

	command1[0] = 0x1d;
	command1[1] = 0x28;
	command1[2] = 0x6b;
	command1[3] = 0x04;
	command1[4] = 0x00;
	command1[5] = 0x31;
	command1[6] = 0x41;
	command1[7] = 0x32;
	command1[8] = 0x00;

	command2[0] = 0x1d;
	command2[1] = 0x28;
	command2[2] = 0x6b;
	command2[3] = 0x03;
	command2[4] = 0x00;
	command2[5] = 0x31;
	command2[6] = 0x43;
	command2[7] = 0x08;

	command3[0] = 0x1d;
	command3[1] = 0x28;
	command3[2] = 0x6b;
	command3[3] = 0x03;
	command3[4] = 0x00;
	command3[5] = 0x31;
	command3[6] = 0x45;
	command3[7] = 0x30;


	command4[0] = 0x1d;
	command4[1] = 0x28;
	command4[2] = 0x6b;
	command4[3]= (ilen+3)&0xff;
	command4[4]= 0x00;
	command4[5]= 0x31;
	command4[6]= 0x50;
	command4[7]= 0x30;

	command5[0] = 0x1d;
	command5[1] = 0x28;
	command5[2] = 0x6b;
	command5[3] = 0x03;
	command5[4] = 0x00;
	command5[5] = 0x31;
	command5[6] = 0x51;
	command5[7] = 0x30;

	if(com_write(print->fd, command1, 9)){
		com_write(print->fd, command2, 8);
		com_write(print->fd, command3, 8);
		com_write(print->fd, command4, 8);
		com_write(print->fd, content, ilen);
		com_write(print->fd, command5, 8);
		//com_write(print->fd, command6, 4);
		return 0;
	}
	else{
		return -1;
	}
}

int aexddB58T_TA500_cut(PRINT_HANDLE print,HKKP env,HKKP obj,int n){
	char buff[20] = {0};

	if (!print)
		return -1;
	memset(buff, 0, sizeof(buff));

	if(n==1)
      sprintf(&buff[0], "\x1d\x56\x42\x00");
	else if(n==0)
	   sprintf(&buff[0], "\x1d\x56\x42\x00");
	else
		return -1;

	if(com_write(print->fd, buff, 4))
		return 0;
	else
		return -1;
}






