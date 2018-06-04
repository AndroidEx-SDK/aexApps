# aexSerial  基于安卓工控KK3X系列和NXP系列的串口调试工具

#### 联系作者：liyp@androidex.cn
#### 主要库 appLibs.aar

 ###串口辅助工具类：SerialHelper
        引用时请注意constructor ，在constructor创建时创建了串口真正操作类---kkserial
        constructor必传参数：sPort --- 串口地址
                             iBaudRate --- 波特率
                             context ---  上下文环境
    主要提供操作有：
        #1、打开串口  --- open()
        #2、关闭串口 ---  close()
        #3、向串口发送字节数据  ---  send(byte[] bOutArray)
        #4、向串口发送16进制字符串数据  --- sendHex(String sHex)
        #5、向串口发送字符串数据  --- sendTxt(String sTxt)
        #6、读取串口数据  --- startReadSerial()
        #7、串口数据回调  --- onDataReceived(ComRecData)
###真正的串口操作类：kkserial
       #1、打开串口 --- serial_open(String sport) 返回值int fd;  fd-->串口标识码，该值大于0即表示串口打开成功否则打开失败
       #2、关闭串口 --- serial_close(int fd)
       #2、读取轮询 --- serial_select(int fd, int timeout)
       #3、读取串口数据 ---  serial_read(int fd, int length, int timeout) 返回值类型为字节数组
       #4、读取串口数据 --- serial_readHex(int fd, int length, int timeout) 返回值类型为16进制字符串
       #5、向串口发送数据  --- serial_write(int fd, byte[] data, int size)  写入参数类型为字节数组
       #6、向串口发送数据  --- serial_writeHex(int fd, String data)   写入参数类型为16进制字符串
       #7、串口数据回调  --- onBackCallEvent(int _code, String _msg)  轮询时串口回掉函数

###串口选择类：SerialPortFinder
       #1、自动获取本机串口接口  ---String[] getAllDevices()  此函数用在NXP的主控上会崩溃，NXP若出现问题请使用默认串口接口 getAllDevicesPath()
       #2、默认串口接口  --- String[] getAllDevicesPath()

##############################










