#ifndef __HZTC__
#define __HZTC__

#ifndef JNI_DEBUG
#define JNI_DEBUG
#endif

typedef enum ztc_event{
	KE_START = 0x10000
	,KE_LOG			//键盘日志
	,KE_WARNING		//键盘警告
	,KE_ERROR		//键盘错误
	,KE_OPENED
	,KE_CLOSED
	,KE_PRESSED = 0x10100
}KMY_EVENT;

typedef void* HZTC;
/**
 * 密码键盘事件回调函数类型
 * @param code 事件代码，code >>16表示事件的类型，code & 0xffff表示事件的参数
 * @param msg	事件字符串描述
 */
typedef int (*ON_ZTC_EVENT)(HZTC env,HZTC obj,int code,char *pszFormat,...);

/**
 * 设置键盘事件
 */
void ztc_set_event(ON_ZTC_EVENT oke);

int ztc_read_key_loop(HZTC env,HZTC obj,int fd,int timeout);
int ztc_send_cmd(HZTC env,HZTC obj,int fd,char *cmd,int size);
int ztc_send_hexcmd(HZTC env,HZTC obj,int fd,char *hexcmd,int size);

#endif
