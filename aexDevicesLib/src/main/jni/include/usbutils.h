#ifndef __APP_USBUTILS__
#define __APP_USBUTILS__

#include <utils.h>

/**
 * 打开串口的函数
 * @param Dev  串口字符串，字符串格式为:	com=/dev/ttyUSB0(串口设备字符串),s=9600(波特率),p=N(奇偶校验),b=1(停止位),d=8(数据位数)
 * @return
 * 		返回串口句柄，如果失败则返回值<=0
 */
int usb_open(char *dev);
void usbclose(int fd);
/**
 * 从串口接收信息的函数
 * @param fd  串口句柄
 * @param buf 存放接收内容的缓冲区
 * @param maxLen 存放接收内容缓冲区的大小
 */
int usb_recive(int fd,char *buf,int maxLen,int timeout);
int usb_write(int fd,char *buf,int len);
int usb_select(int fd, int sec, int usec);

#endif
