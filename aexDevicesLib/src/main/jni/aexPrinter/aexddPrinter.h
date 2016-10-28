#ifndef __HPRINT__
#define __HPRINT__

typedef long                LONG;
typedef unsigned short      WORD;
typedef unsigned long       DWORD;

typedef enum print_event{
	PE_START 		= 0x10000
	,PE_ERROR 		= 0x10001
	,PE_FIND_PORT 	= 0x10200		//发现端口事件
	,PE_STATUS		= 0x10201
}PRINT_EVENT;

typedef struct print_data{
	int fd;		//串口句柄
	char port[200];		//正在打开的串口设备名称和参数
	char version[17];	//版本号
	char sn[32];		//序列号

}PRINT_DATA,*PRINT_HANDLE;


typedef struct BITMAPINFO_ {
  DWORD biSize;
  LONG  biWidth;
  LONG  biHeight;
  WORD  biPlanes;
  WORD  biBitCount;
} BITMAP_DATA, *BITMAP_HANDLE;

typedef void* HKKP;

typedef (*ON_PRINT_EVENT)(PRINT_HANDLE print,HKKP env,HKKP obj,int code,char *msg);

static int aexddPrinter_event(PRINT_HANDLE print,HKKP env,HKKP obj,int code,char *pszFormat,...);

void aexddPrinter_set_event(ON_PRINT_EVENT oke);

PRINT_HANDLE aexddPrinter_find(HKKP env,HKKP obj,char *aexddPrinterth,char *filter,char *arg);

/**
 * 打开打印机，返回0失败   其他成功
 * @aexddPrinterram arg 串口参数字符串，字符串格式为:com=/dev/ttyUSB0(串口设备字符串),s=115200(波特率),p=N(奇偶校验),b=1(停止位),d=8(数据位数)
 */
PRINT_HANDLE aexddPrinter_open(HKKP env,HKKP obj,char* arg);

/**
 *关闭打印机，返回0失败   其他成功
 */
int aexddPrinter_close(PRINT_HANDLE print,HKKP env,HKKP obj); //程序退出时调用

// 初始化打印机
int aexddPrinter_initialize(PRINT_HANDLE print,HKKP env,HKKP obj);

// 获取打印机状态
int aexddPrinter_getstatus(PRINT_HANDLE print, HKKP env, HKKP obj, int n);

// 获取打印机厂商
int aexddPrinter_getfactory(PRINT_HANDLE print, HKKP env, HKKP obj, int n);

// 打印行缓冲器里的内容，iflag  1按点向前走纸 ，0 退纸
int aexddPrinter_steppoint(PRINT_HANDLE print,HKKP env,HKKP obj,int ifalg ,int n);

// 打印行缓冲器里的内容，并向前n行   n=0--255
int aexddPrinter_stepline(PRINT_HANDLE print,HKKP env,HKKP obj,char* pcode,int n);

// 设置切纸方式
int aexddPrinter_cut(PRINT_HANDLE print,HKKP env,HKKP obj,char* pcode,int iflag);

// 打印换行
int aexddPrinter_newline(PRINT_HANDLE print,HKKP env,HKKP obj,int n);

// 设置排列方式
int aexddPrinter_set_align(PRINT_HANDLE print,HKKP env,HKKP obj,int iflag);

// 设置字体大小
int aexddPrinter_set_fontsize(PRINT_HANDLE print,HKKP env,HKKP obj,int isize);

// 设置打印模式
int aexddPrinter_set_mode(PRINT_HANDLE print,HKKP env,HKKP obj,int isize);

// 设置行间距为n 点行 （n ∕203 英寸） 默认n=30
int aexddPrinter_set_linewide(PRINT_HANDLE print,HKKP env,HKKP obj,int isize);

// 设置字符行间距为 1/6 英寸
int aexddPrinter_set_charwide(PRINT_HANDLE print,HKKP env,HKKP obj);

// 在一行内该命令之后的所有字符均以正常宽度的2 倍打印
int aexddPrinter_set_charDSize(PRINT_HANDLE print,HKKP env,HKKP obj);

// 恢复正常宽度打印
int aexddPrinter_set_charNSize(PRINT_HANDLE print,HKKP env,HKKP obj);

//输入中文
int aexddPrinter_print_ch(PRINT_HANDLE print,HKKP env,HKKP obj,char* pch,int ibytelen);

//输出英文
int aexddPrinter_print_en(PRINT_HANDLE print,HKKP env,HKKP obj,char* pEn,int length);

// 打印账单
//int aexddPrinter_printbill(PRINT_HANDLE print,HKKP env,HKKP obj, char* pch, int ichlen, char* pchd, int ichdlen);

//设定点图命令
int aexddPrinter_set_graph(PRINT_HANDLE print,HKKP env,HKKP obj, char m, char n1, char n2,  char* chGPI);

//设置条码高度
int aexddPrinter_set_barcodeHigh(PRINT_HANDLE print,HKKP env,HKKP obj, char n);

//设定条码宽度,水平方向点数 2<=n<=6,缺省值为3
int aexddPrinter_set_barcodeWide(PRINT_HANDLE print,HKKP env,HKKP obj, char n);

// 打印条形码
int aexddPrinter_print_barcode(PRINT_HANDLE print,HKKP env,HKKP obj,char wide,char high,char code,char* data, int len);

// 打印二维码
int aexddPrinter_print_2Dimensional(PRINT_HANDLE print,HKKP env,HKKP obj,char *content,int ilen);

// SGT-801 打印二维码
int aexddPrinter_SGT801_print_2Dimensional(PRINT_HANDLE print,HKKP env,HKKP obj,int isize,char *content,int ilen);

// 打印位图
int aexddPrinter_print_bitmap(PRINT_HANDLE print,HKKP env,HKKP obj,const unsigned char *data, int iLen, int iLine);

int PrintBitmap(PRINT_HANDLE print,HKKP env,HKKP obj,int m, const unsigned char *pBitmap, int iLen);

// 设置间距
int aexddPrinter_setLineinterval(PRINT_HANDLE print,HKKP env,HKKP obj,int n);

// 设置打印区域
int aexddPrinter_setprintarea(PRINT_HANDLE print,HKKP env,HKKP obj,int nL,int nH);

int serial_read(PRINT_HANDLE print,HKKP env,HKKP obj,char *buf, int timeout);

int serial_write(PRINT_HANDLE print,HKKP env,HKKP obj,char *data,int len);

int serial_select(PRINT_HANDLE print,HKKP env,HKKP obj, int sec, int usec);

// 打印条形码
int aexddPrinter_T_500AP_print_barcode(PRINT_HANDLE print,HKKP env,HKKP obj,char wide,char high,char code,char* data, int len);
// 打印二维码
int aexddPrinter_T_500AP_print_2Dimensional(PRINT_HANDLE print,HKKP env,HKKP obj,char *content,int ilen);
// 打印条形码
int aexddPrinter_RG_CB532_print_barcode(PRINT_HANDLE print,HKKP env,HKKP obj,char wide,char high,char code,char* data, int len);
// 打印二维码
int aexddPrinter_RG_CB532_print_2Dimensional(PRINT_HANDLE print,HKKP env,HKKP obj,char *content,int ilen);
// 打印二维码
int aexddPrinter_TA500_print_2Dimensional(PRINT_HANDLE print,HKKP env,HKKP obj,char *content,int ilen);
// 设置切纸方式
int aexddPrinter_TA500_cut(PRINT_HANDLE print,HKKP env,HKKP obj,int n);
#endif
