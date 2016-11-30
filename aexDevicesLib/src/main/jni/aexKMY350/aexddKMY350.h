#ifndef __HKMY__
#define __HKMY__

#ifndef JNI_DEBUG
#define JNI_DEBUG
#endif

typedef enum kmy_event{
	KE_START = 0x10000
	,KE_LOG			//键盘日志
	,KE_WARNING		//键盘警告
	,KE_ERROR		//键盘错误
	,KE_OPENED
	,KE_CLOSED
	,KE_PRESSED = 0x10100
}KMY_EVENT;

typedef void* HKMY;
/**
 * 密码键盘事件回调函数类型
 * @param code 事件代码，code >>16表示事件的类型，code & 0xffff表示事件的参数
 * @param msg	事件字符串描述
 */
typedef int (*ON_KMY_EVENT)(HKMY env,HKMY obj,int code,char *pszFormat,...);

/**
 * 设置键盘事件
 */
void kmy_set_event(ON_KMY_EVENT oke);

int kmy_recive_packet(HKMY env,HKMY obj,int fd,char *buf,int bufsize,int timeout);
int kmy_read_key_loop(HKMY env,HKMY obj,int fd,int timeout);
int kmy_send_cmd(HKMY env,HKMY obj,int fd,char *cmd,int size);
int kmy_send_hexcmd(HKMY env,HKMY obj,int fd,char *hexcmd,int size);

#endif
