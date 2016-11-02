#ifndef __HKKC__
#define __HKKC__

typedef enum kkcard_event{
	CE_START 		= 0x10000
	,CE_LOG	 		= 0x10001		//日志
	,CE_WARNING		= 0x10002		//警告
	,CE_ERROR		= 0x10003		//错误
	,CE_OPENED		= 0x10004
	,CE_RESETED		= 0x10005		//复位完成
	,CE_DEVICE_UUID = 0x10006		//设备序列号
	,CE_DEVICE_VER	= 0x10007		//设备版本号
	,CE_EVENT		= 0x10008
	,CE_BRUSHED		= 0x10100
	,CE_FIND_PORT 	= 0x10200
	,CE_READ_ICCARD   = 0x10201       // 读取RFM-13 读卡器数据
	,CE_READ_CPUCARD  = 0x10202       // 读取S3 cpu读卡器数据
}KKCARD_EVENT;

typedef struct kkcard_data{
	int fd;		//串口句柄
	int mode;	//加密模式0=DES 1=3DES
	int max_pin_len;	//最大的密码长度
	char port[200];		//正在打开的串口设备名称和参数
	char version[17];	//版本号
	char sn[32];		//序列号

}KKCARD_DATA,*KKCARD_HANDLE;

typedef void* HKKC;

/**
 * 事件回调函数类型
 * @param code 事件代码，code >>16表示事件的类型，code & 0xffff表示事件的参数
 * @param msg	事件字符串描述
 */
typedef int (*ON_KKCARD_EVENT)(KKCARD_HANDLE kkc,HKKC env,HKKC obj,int code,char *msg);

/**
 * 设置键盘事件
 */
void kkcard_set_event(ON_KKCARD_EVENT oke);

KKCARD_HANDLE kkcard_find(HKKC env,HKKC obj,char *path,char *filter,char *arg);
/**
 * 打开，返回句柄
 * @param arg 串口参数字符串，字符串格式为:	com=/dev/ttyUSB0(串口设备字符串),s=9600(波特率),p=N(奇偶校验),b=1(停止位),d=8(数据位数)
 */
KKCARD_HANDLE kkcard_open(HKKC env,HKKC obj,char *arg);
/**
 *关闭密码键盘
 *@param kmy 密码键盘的句柄
 */
void kkcard_close(KKCARD_HANDLE kkc,HKKC env,HKKC obj);
/**
 * 程序复位自检（不破坏密钥区）
 * @param kmy 串口句柄
 * @param timeout 等待自检完成的时间，单位秒
 */
int kkcard_reset(KKCARD_HANDLE kkc,HKKC env,HKKC obj,char *v,int timeout);
int kkcard_read_card(KKCARD_HANDLE kkc,HKKC env,HKKC obj,char *cb,int timeout);

int serial_read(KKCARD_HANDLE kkc,HKKC env,HKKC obj,char *buf, int timeout);
int serial_write(KKCARD_HANDLE kkc,HKKC env,HKKC obj,char *data,int len);
int serial_select(KKCARD_HANDLE kkc,HKKC env,HKKC obj,int sec, int usec);

int kkcard_ring(KKCARD_HANDLE kkc,HKKC env,HKKC obj, int timeout);
int kkcard_read_rfm13_id(KKCARD_HANDLE kkc,HKKC env,HKKC obj, int timeout);
int kkcard_read_rfm13_card(KKCARD_HANDLE kkc,HKKC env,HKKC obj,int sectorid, int blockid, int timeout);
int kkcard_write_rfm13_card(KKCARD_HANDLE kkc,HKKC env,HKKC obj,int sectorid, char *data0,int len0, char *data1, int len1, char *data2, int len2);

int kkcard_read_mf30_card(KKCARD_HANDLE kkc,HKKC env,HKKC obj,int sectorid, int blockid, int timeout);
int kkcard_read_mf30_card_bypwd(KKCARD_HANDLE kkc,HKKC env,HKKC obj,int sectorid, int blockid, char *passwd, int pwdlen, int timeout);
int kkcard_read_mf30_id(KKCARD_HANDLE kkc,HKKC env,HKKC obj, int timeout);
int kkcard_write_mf30_card(KKCARD_HANDLE kkc,HKKC env,HKKC obj,int sectorid, int blockid, char *data0,int len0);
int kkcard_getver_mf30_card(KKCARD_HANDLE kkc,HKKC env,HKKC obj, int timeout);

int card_read(KKCARD_HANDLE kkc, HKKC env,HKKC obj,char *szRecvData,int sectorid);
int card_request(KKCARD_HANDLE kkc,HKKC env,HKKC obj);
int card_read_id(KKCARD_HANDLE kkc,HKKC env,HKKC obj, char *szData);
int card_select(KKCARD_HANDLE kkc,HKKC env,HKKC obj ,char *szData);
int card_loadkey(KKCARD_HANDLE kkc, HKKC env,HKKC obj ,char *szData);
int card_authentication(KKCARD_HANDLE kkc,HKKC env,HKKC obj , int mode, int sectorid, char *szData);

// cpu 卡
int cpucard_reset(KKCARD_HANDLE kkc,HKKC env,HKKC obj,int timeout);
int cpucard_poweron(KKCARD_HANDLE kkc,HKKC env,HKKC obj,int timeout);
int cpucard_poweroff(KKCARD_HANDLE kkc,HKKC env,HKKC obj,int timeout);
int cpucard_apdu(KKCARD_HANDLE kkc,HKKC env,HKKC obj,char *data, int len,int timeout);

int cpucard_apdu_cmd(KKCARD_HANDLE kkc,HKKC env,HKKC obj,char *data, int len, char *szRecvData, int *length,int timeout);

//
//// 选择文件
int cpucard_check_block(KKCARD_HANDLE kkc,HKKC env,HKKC obj,int value, int timeout);

// 读取响应数据
int cpucard_read_block(KKCARD_HANDLE kkc,HKKC env,HKKC obj,char *data,char *szRecvData, int *length, int timeout);

int kkcard_send_cmd(int fd,char *cmd,int size);

#endif
