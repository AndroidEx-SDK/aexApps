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
	,KE_RESETED		//键盘复位完成
	,KE_RESETED_PIN	//键盘复位完成，密钥已毁
	,KE_DEVICE_UUID //设备序列号
	,KE_DEVICE_VER	//设备版本号
	,KE_ENCRYPT_MODE	//设置加密模式命令完成
	,KE_PRESSED = 0x10100
	,KE_START_PIN =0X10150	//提示上层，可以提示用户输入信息
	,KE_HEX_PIN
	,KE_FUNC_KEY
	,KE_FIND_PORT = 0x10200
}KMY_EVENT;

typedef struct kmy_data{
	int fd;		//串口句柄
	int mode;	//加密模式0=DES 1=3DES
	int max_pin_len;	//最大的密码长度
	char port[200];		//正在打开的串口设备名称和参数
	char version[17];	//键盘版本号
	char sn[32];		//键盘序列号
}KMY_DATA,*KMY_HANDLE;

typedef void* HKMY;
/**
 * 密码键盘事件回调函数类型
 * @param code 事件代码，code >>16表示事件的类型，code & 0xffff表示事件的参数
 * @param msg	事件字符串描述
 */
typedef int (*ON_KMY_EVENT)(KMY_HANDLE kmy,HKMY env,HKMY obj,int code,char *pszFormat,...);

/**
 * 设置键盘事件
 */
void kmy_set_event(ON_KMY_EVENT oke);

KMY_HANDLE kmy_find(HKMY env,HKMY obj,char *path,char *filter,char *arg);
/**
 * 打开密码键盘，返回密码键盘的句柄
 * @param arg 串口参数字符串，字符串格式为:	com=/dev/ttyUSB0(串口设备字符串),s=9600(波特率),p=N(奇偶校验),b=1(停止位),d=8(数据位数)
 */
KMY_HANDLE kmy_open(HKMY env,HKMY obj,char *arg);
/**
 *关闭密码键盘
 *@param kmy 密码键盘的句柄
 */
void kmy_close(KMY_HANDLE kmy,HKMY env,HKMY obj);
/**
 * 程序复位自检（不破坏密钥区）
 * @param kmy 串口句柄
 * @param timeout 等待自检完成的时间，单位秒
 */
int kmy_reset(KMY_HANDLE kmy,HKMY env,HKMY obj,int timeout);
/**
 * 程序复位自检，并重设密钥）
 * @param kmy 串口句柄
 * @param timeout 等待自检完成的时间，单位秒
 */
int kmy_reset_with_pin(KMY_HANDLE kmy,HKMY env,HKMY obj,int timeout);
/**
 * 获取产品的额序列号
 *@param kmy 键盘句柄
 *@param sn 存放序列号的缓冲区，缓冲区的空间分配和销毁由调用者处理
 *@param timeout 接收获取操作结果的超时时间，单位秒
 */
int kmy_get_sn(KMY_HANDLE kmy,HKMY env,HKMY obj,char *sn,int timeout);
int kmy_set_sn(KMY_HANDLE kmy,HKMY env,HKMY obj,char *sn,int timeout);
/**
 * 获取产品的版本号
 *@param kmy 键盘句柄
 *@param v 存放版本号的缓冲区，缓冲区的空间分配和销毁由调用者处理
 *@param timeout 接收获取操作结果的超时时间，单位秒
 */
int kmy_get_version(KMY_HANDLE kmy,HKMY env,HKMY obj,char *v,int timeout);
/**
 * 主密钥下载、键盘输入PIN采用加密方式    DES or 3DES
 * @param fd 串口句柄
 * @param ewm 加密模式，0：DES模式，1:3DES模式
 * @param timeout 超时时间，单位秒
 */
int kmy_set_encrypt_mode(KMY_HANDLE kmy,HKMY env,HKMY obj,int ewm,int timeout);
/**
 *下载主密钥，调用此函数前首先要调用kmy_set_encrypt_mode设置加密方式
 *@param fd 串口句柄
 *@param MKeyNo	 主密钥号 范围0－15
 *@param MKeyAsc	主密钥
 */
int kmy_dl_master_key(KMY_HANDLE kmy,HKMY env,HKMY obj,int MKeyNo, char *MKeyAsc,int timeout);
/**
 *  下载工作密钥，调用此函数之前需要先调用设置加密模式的函数
 *@param MKeyNo 主密钥号 范围0－15
 *@param WKeyNo	工作密钥号 范围0－15
 *@param WKeyAsc 工作密钥
 */
int kmy_dl_work_key(KMY_HANDLE kmy,HKMY env,HKMY obj,int MKeyNo, int WKeyNo, char *WKeyAsc,int timeout);
/**
 * 激活工作密钥
 *@param kmy 密码键盘句柄
 *@param MKeyNo 主密钥号0-15
 *@param WKeyNo 工作密钥号0-15
 *@param timeout 操作超时时间
 */
int kmy_active_work_key(KMY_HANDLE kmy,HKMY env,HKMY obj,int MKeyNo, int WKeyNo,int timeout);
/**
 *发送开关键盘和按键声音
 *@param kmy 键盘句柄
 *@param CTL 关闭键盘:1 打开键盘:2 打开键盘且静音:3 系统键盘:4
 *@param timeout 接收数据超时时间
 */
int kmy_open_keypad(KMY_HANDLE kmy,HKMY env,HKMY obj,int CTL,int timeout);
/**
 * 下载银行卡卡号，开始密码输入
 *@param kmy 密码键盘的句柄
 *@param pchCardNo 银行卡号码
 *@param timeout 超时时间
 */
int kmy_dl_card_no(KMY_HANDLE kmy,HKMY env,HKMY obj,char *pchCardNo,int timeout);
/**
 *开始键盘PIN加密
 *@param kmy 密码键盘句柄
 *@param Pinlen 密码的长度
 *@param DispMode 显示模式
 *@param AddMode
 *@param PromMode
 *@param nTimeOut 输入密码的超时时间
 *@param timeout 接收回应包的时间
 */
int kmy_start_pin(KMY_HANDLE kmy,HKMY env,HKMY obj,short PinLen, short DispMode, short AddMode, short PromMode,short nTimeOut,int timeout);
/**
 *PINBLOCK运算
 *@param kmy 密码键盘句柄
 *@param pchCardNo 银行卡卡号
 *@param timeout 接收包超时时间
 */
int kmy_pin_block(KMY_HANDLE kmy,HKMY env,HKMY obj,char *pchCardNo,int timeout);
/**
 * 获取密码密文
 *@param kmy 键盘句柄
 *@param chPin 返回密文的缓冲区
 *@param timeout
 */
int kmy_read_pin(KMY_HANDLE kmy,HKMY env,HKMY obj,char *chPin,char *hexPin,int timeout);
/**
 * 加密密码
 *@param kmy		键盘句柄
 *@param DataInput	输入数据
 *@param DataOutput	输出数据
 *@param timeout	操作超时时间
 */
int kmy_encrypt(KMY_HANDLE kmy,HKMY env,HKMY obj,char *DataInput, char *DataOutput,char * hexOut,int timeout);
/**
 * 解密密码
 *@param kmy		键盘句柄
 *@param DataInput	输入数据
 *@param DataOutput	输出数据
 *@param timeout	操作超时时间
 */
int kmy_decrypt(KMY_HANDLE kmy,HKMY env,HKMY obj,char *DataInput, char *DataOutput,int timeout);
/**
 * Mac
 *@param kmy		键盘句柄
 *@param DataInput	输入数据
 *@param DataOutput	输出数据
 *@param timeout	操作超时时间
 */
int kmy_calc_mac_data(KMY_HANDLE kmy,HKMY env,HKMY obj,char *DataInput, char *DataOutput,char * hexOut,int timeout);
int yl_calc_mac_data(KMY_HANDLE kmy,HKMY env,HKMY obj,char *DataInput, char *DataOutput,char * hexOut,int timeout);
int yl_encrypt_data(KMY_HANDLE kmy,HKMY env,HKMY obj,char *DataInput, char *DataOutput,int timeout);
int kmy_start_read_key(KMY_HANDLE kmy,HKMY env,HKMY obj,char *cb,int timeout);
int kmy_start_all_step(KMY_HANDLE kmy,HKMY env,HKMY obj,int mKeyNo,int wKeyNo,char *chwKey,char *chcardNo,char *chcb,char *passHex,char *port,int timeout);
void SetKmyHandle(KMY_HANDLE kmy);
int yl_get_data(KMY_HANDLE kmy,HKMY env,HKMY obj,char* DataInput, char* DataOutput,int timeout);
int yl_read_pin(KMY_HANDLE kmy,HKMY env,HKMY obj,char *chPin,char *hexPin,int timeout);

#endif
