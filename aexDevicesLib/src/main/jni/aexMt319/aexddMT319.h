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

typedef void* HKKC;

/**
 * 事件回调函数类型
 * @param code 事件代码，code >>16表示事件的类型，code & 0xffff表示事件的参数
 * @param msg	事件字符串描述
 */
typedef int (*ON_KKCARD_EVENT)(HKKC env,HKKC obj,int code,char *msg);

/**
 * 设置键盘事件
 */
void kkcard_set_event(ON_KKCARD_EVENT oke);
int kkcard_event(HKKC env,HKKC obj,int code,char *pszFormat,...);

int kkcard_recive_packet(HKKC env,HKKC obj,int fd,char *buf,int bufsize,int timeout);
int kkcard_read_loop(HKKC env,HKKC obj,int fd,int timeout);
int kkcard_send_cmd(HKKC env,HKKC obj,int fd,char *cmd,int size);

#endif
