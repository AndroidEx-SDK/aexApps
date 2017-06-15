package com.tencent.device;

//import com.tencent.mobileqq.msf.sdk.QLogImpl;

import android.content.Context;


/**
 * 
 * 手机QQ客户端日志统一输出系统<br>
 * 所有的日志输出都可以在Console中看到<br>
 * QLog定义了日志级别<br>
 * -  DEV 开发者<br>
 * -  USR 用户<br>
 * -  CLR 染色用户<br>
 * 定义成USR级别，都会输出到日志文件中<br>
 * 定义成CLR级别，后台配置染色了的用户才会输出到日志文件中，否则只在consloe中输出<br>
 * 定义成DEV级别，只会在consloe中输出
 * 
 * <p>
 * 日志文件的存储在/sdcard/tencent/com/tencent/mobileqq目录中<br>
 * 日志文件命名规则，一个小时生成一个文件，文件的签字是进程名+日期<br>
 * <br>
 * <br>
 * 日志文件系统采用cahce机制，LOG_ITEM_MAX_CACHE_SIZE是默认cahce大小<br>
 * 超过这个大小继续加入日志时触发写文件。<br>
 * 调用dumpCacheToFile可以及时将缓存的日志写到文件系统中。
 * @author albertzhu,logluo
 */
public class QLog {

	/**
	 * 日志规范：日志中不可恢复的错误或异常的统一标识符。<br>
	 * 方便异常日志问题查找。
	 */
	public final static String ERR_KEY = "qq_error|";
	
	public static void init(Context context) {
		//QLogImpl.init(context);
	}
	/**
	 * 日志级别  Developer  <br>
	 * 开发者级别，日常开发使用的级别，只是用来调试用，不希望在用户的机器上输出 <br>
	 * 此级别的日志会输出console中，但不会输出到日志文件系统中 <br>
	 * 需要输出到文件系统的时候请使用 USR，或者 CLR <br>
	 * 当uin是开发者 此日志会输出并上报 <br>
	 */
	public final static int DEV = 4;
	
//	/**
//	 * 当uin是内部用户 此日志会输出并上报
//	 */
//	public final static int REPORTLEVEL_INTERNALUSER = 3;
	/**
	 * Color user 染色用户日志级别 <br>
	 *  动态在后台配置染色用户时，本级别日志输出到文件，否则只输出到console<br>
	 *  染色用户主要用来跟踪问题，通常是问题比较不好复现，但用户可以复现<br>
	 *  此级别的日志会输出console中，同时输出到日志文件系统中<br>
	 *  当uin是开发者 此日志会输出并上报<br>
	 */
	public final static int CLR = 2;

	/**
	 *  user 普通手机QQ用户级别 <br>
	 *  通常用来追踪一些很不可以理解的情况但确实有可能会发生 <br>
	 *  这种情况用户和测试都不容易出现 <br>
	 * 此级别的日志会输出console中，同时输出到日志文件系统中<br>
	 * 当uin是开发者 此日志会输出并上报<br>
	 */
	public final static int USR = 1;
	
	/**
	 * 文件日志系统，缓存机制的缓存日志最大条数
	 */
	public final static int LOG_ITEM_MAX_CACHE_SIZE =50;
	
	/**
	* QQ 版本号号输出到日志文件，避免体验版本多难于区分，
	* 本地版本号要与服务器版本号区分开
	* UI侧赋值
	*/
		
	public  static String sBuildNumber="";
	
	
	/**
	 * 是否是染色用户级别 如果是 可以多输出一些日志
	 * @return result 
	 */
	public static boolean isColorLevel() {
		return true;
		//return QLogImpl.isColorUser();
	}
	
	/**
	 * 是否是开发者级别 如果是 可以输出详细的日志
	 * @return
	 */
	public static boolean isDevelopLevel() {
		return true;
		//return QLogImpl.isDEVELOPER();
	}

	
	/**
	 * 只在console中输出的log 不会写文件 
	 * @param tag
	 * @param msg
	 */
	public static void p(String tag, String msg) {
		//QLogImpl.p(tag, msg);
	}
	
	/**
	 * 得到异常堆栈
	 * @param tr
	 * @return
	 */
	 public static String getStackTraceString(Throwable tr) {
		 return android.util.Log.getStackTraceString(tr);
	 }
	
	
	/**
	 *  日志输出到console
	 * @param tag
	 * @param reportLevel
	 *     USR,CLR,DEV
	 *     USR默认都会输出到日志文件，
     *     CLR后台配置好只后才会输出到日志文件
	 *     DEV不会输出到文件
	 * @param msg
	 *     消息体 
	 */
	public static void e(String tag, int reportLevel, String msg) {
		android.util.Log.e(tag, "[" + getReportLevel(reportLevel) + "]" + msg);

//		QLogImpl.e(tag, reportLevel, msg, null);
	}
	/**
	 *  日志输出到console
	 * @param tag
	 * @param reportLevel
	 *     USR,CLR,DEV
	 *     USR默认都会输出到日志文件，
     *     CLR后台配置好只后才会输出到日志文件
	 *     DEV不会输出到文件
	 * @param msg
	 *     消息体 
	 *  @param tr
	 *  
	 */
	public static void e(String tag, int reportLevel, String msg, Throwable tr) {
		e(tag, reportLevel, msg);
	}
	/**
	 *  日志输出到console<br>
	 *  默认日志的级别是DEV<br>
	 *  不会输出到日志文件中，需要输出到文件日志中请使用<br>
	 *  w(String tag, int reportLevel, String msg)<br>
	 *  或者<br>
	 *  w(String tag, int reportLevel, String msg, Throwable tr)<br>
	 * @param tag
	 * @param msg
	 *     消息体 
	 *  
	 */
	/*public static void w(String tag, String msg) {
		QLogImpl.w(tag, DEV, msg, null);
	}*/
	/**
	 *  日志输出到console，<br>
	 *  默认日志的级别是DEV<br>
	 *  不会输出到日志文件中，需要输出到文件日志中请使用<br>
	 *  w(String tag, int reportLevel, String msg)<br>
	 *  或者<br>
	 *  w(String tag, int reportLevel, String msg, Throwable tr)<br>
	 *  
	 * @param tag
	 * @param msg
	 *     消息体 
	 * @param tr
	 * 	    
	 *  
	 */
	/*public static void w(String tag, String msg, Throwable tr) {
		QLogImpl.w(tag, DEV, msg, tr);
	}*/
	/**
	 *  日志输出到console<br>
	 * @param tag
	 * @param reportLevel
	 *     USR,CLR,DEV<br>
	 *     USR默认都会输出到日志文件，<br>
     *     CLR后台配置好只后才会输出到日志文件<br>
	 *     DEV不会输出到文件<
	 * @param msg
	 *     消息体 
	 *  
	 */
	public static void w(String tag, int reportLevel, String msg) {
		android.util.Log.w(tag, "[" + getReportLevel(reportLevel) + "]" + msg);
		//QLogImpl.w(tag, reportLevel, msg, null);
	}
	/**
	 *  日志输出到console<br>
	 * @param tag
	 * @param reportLevel
	 *     USR,CLR,DEV<br>
	 *     USR默认都会输出到日志文件，<br>
     *     CLR后台配置好只后才会输出到日志文件<br>
	 *     DEV不会输出到文件<br>
	 * @param msg
	 *     消息体 
	 *  @param tr
	 *  
	 */
	public static void w(String tag, int reportLevel, String msg, Throwable tr) {
		android.util.Log.w(tag, "[" + getReportLevel(reportLevel) + "]" + msg);
		//QLogImpl.w(tag, reportLevel, msg, tr);
	}

	/**
	 *  日志输出到console<br>
	 * @param tag
	 * @param reportLevel
	 *     USR,CLR,DEV<br>
	 *     USR默认都会输出到日志文件，<br>
     *     CLR后台配置好只后才会输出到日志文件<br>
	 *     DEV不会输出到文件<br>
	 * @param msg
	 *     消息体 
	 *  
	 */
	public static void i(String tag, int reportLevel, String msg) {
		android.util.Log.i(tag, "[" + getReportLevel(reportLevel) + "]" + msg);
		//QLogImpl.i(tag, reportLevel, msg, null);
	}
	/**
	 *  日志输出到console<br>
	 * @param tag
	 * @param reportLevel
	 *     USR,CLR,DEV<br>
	 *     USR默认都会输出到日志文件<br>
     *     CLR后台配置好只后才会输出到日志文件<br>
	 *     DEV不会输出到文件<br>
	 * @param msg
	 *     消息体 
	 *  @param tr
	 *  
	 */
	public static void i(String tag, int reportLevel, String msg, Throwable tr) {
		android.util.Log.i(tag, "[" + getReportLevel(reportLevel) + "]" + msg);
		//QLogImpl.i(tag, reportLevel, msg, tr);
	}
	
	/**
	 *  日志输出到console<br>
	 *  默认日志的级别是DEV<br>
	 *  不会输出到日志文件中，需要输出到文件日志中请使用<br>
	 *  i(String tag, int reportLevel, String msg)<br>
	 *  或者<br>
	 *  i(String tag, int reportLevel, String msg, Throwable tr)<br>
	 *  
	 * @param tag
	 * @param msg
	 *     消息体 
	 * 	    
	 *  
	 */
	/*public static void i(String tag, String msg) {
		QLogImpl.i(tag, DEV, msg, null);
	}*/

	/**
	 *  日志输出到console<br>
	 *  默认日志的级别是DEV
	 *  不会输出到日志文件中，需要输出到文件日志中请使用<br>
	 *  i(String tag, int reportLevel, String msg)<br>
	 *  或者<br>
	 *  i(String tag, int reportLevel, String msg, Throwable tr)<br>
	 *  
	 * @param tag
	 * @param msg
	 *     消息体 
	 * @param tr
	 * 	    
	 *  
	 */
	/*public static void i(String tag, String msg, Throwable tr) {
		QLogImpl.i(tag, DEV, msg, tr);
	}*/

	/**
	 *  日志输出到console，<br>
	 *  默认日志的级别是DEV<br>
	 *  不会输出到日志文件中，需要输出到文件日志中请使用<br>
	 *  d(String tag, int reportLevel, String msg)<br>
	 *  或者<br>
	 *  d(String tag, int reportLevel, String msg, Throwable tr)<br>
	 *  
	 * @param tag
	 * @param msg
	 *     消息体 
	 */
	/*public static void d(String tag, String msg) {
		QLogImpl.d(tag, DEV, msg, null);
	}*/
	/**
	 *  日志输出到console<br>
	 *  默认日志的级别是DEV<br>
	 *  不会输出到日志文件中，需要输出到文件日志中请使用<br>
	 *  d(String tag, int reportLevel, String msg)<br>
	 *  或者<br>
	 *  d(String tag, int reportLevel, String msg, Throwable tr)<br>
	 *  
	 * @param tag
	 * @param msg
	 *     消息体 
	 * 	    
	 *  
	 */
	/*public static void d(String tag, String msg, Throwable tr) {
		QLogImpl.d(tag, DEV, msg, tr);
	}*/
	/**
	 *  日志输出到console<br>
	 *  
	 * @param tag
	 * @param reportLevel
	 *     USR,CLR,DEV<br>
	 *     USR默认都会输出到日志文件<br>
     *     CLR后台配置好只后才会输出到日志文件<br>
	 *     DEV不会输出到文件<br>
	 * @param msg
	 *     消息体 
	 *  
	 */
	public static void d(String tag, int reportLevel, String msg) {
		android.util.Log.d(tag, "[" + getReportLevel(reportLevel) + "]" + msg);
		//QLogImpl.d(tag, reportLevel, msg, null);
	}
	/**
	 *  日志输出到console
	 * 
	 * @param tag
	 * @param reportLevel
	 *     USR,CLR,DEV<br>
	 *     USR默认都会输出到日志文件<br>
     *     CLR后台配置好只后才会输出到日志文件<br>
	 *     DEV不会输出到文件<br>
	 * @param msg
	 *     消息体 
	 *  @param tr
	 *  
	 */
	public static void d(String tag, int reportLevel, String msg, Throwable tr) {
		android.util.Log.d(tag, "[" + getReportLevel(reportLevel) + "]" + msg);
		//QLogImpl.d(tag, reportLevel, msg, tr);
	}
	
	/**
	 * 当uin是开发者 此日志会输出并上报
	 */
	public final static String TAG_REPORTLEVEL_DEVELOPER = "D";
//	/**
//	 * 当uin是内部用户 此日志会输出并上报
//	 */
//	public final static String TAG_REPORTLEVEL_INTERNALUSER = "I";
	/**
	 * 当uin是染色用户 此日志会输出并上报
	 */
	public final static String TAG_REPORTLEVEL_COLORUSER = "W";
	/**
	 * 当uin是普通用户 此日志会输出并上报
	 */
	public final static String TAG_REPORTLEVEL_USER = "E";
	public static String getReportLevel(int reportLevel) {
		switch (reportLevel) {
		case QLog.CLR:
			return TAG_REPORTLEVEL_COLORUSER;
		case QLog.DEV:
			return TAG_REPORTLEVEL_DEVELOPER;
//		case QLog.REPORTLEVEL_INTERNALUSER:
//			return TAG_REPORTLEVEL_INTERNALUSER;
		case QLog.USR:
			return TAG_REPORTLEVEL_USER;
		default:
			return TAG_REPORTLEVEL_USER;
		}
	}
//	/**
//	 * 主动上报近两个小时的log
//	 * @param cmdstr
//	 * @param opinfo
//	 */
//	public static void doReportLogSelf(int appid,String cmdstr , String opinfo) {
//		QLogImpl.doReportLogSelf(appid,cmdstr, opinfo);
//	}
//	
//	/**
//	 * 阻塞方法 上报指定位置的log
//	 * @param appid
//	 * @param logFilePath
//	 * @param cmdstr
//	 * @param opinfo
//	 */
//	public static void syncReportLogSelf(int appid, String logFilePath, String cmdstr, String opinfo) {
//		QLogImpl.syncReportLogSelf(appid,logFilePath,cmdstr, opinfo);
//	}
	
	/**
	 * 启用了日志写文件的方式<br>
	 * 日志系统会把日志按条缓存在一个队列中<br>
	 * 需要把缓存队列的消息写到文件中时调用dumpCacheToFile<br>
	 * 1.退出程序前需要调用<br>
	 * 2.异常发生时需要调用 <br>
	 */
	public static void dumpCacheToFile(){
		
	}
	
//	public static void setLogToFile(boolean isWriteLogFile) {
//		QLogImpl.setLogToFile(isWriteLogFile);
//	}
//	
//	/**
//	 * 将这些tag的log输出
//	 * 最多持续30分钟
//	 * @param tags 要输出的tag
//	 */
//	public static void startColorLog(String[] tags) {
//		QLogImpl.startColorLog(tags);
//	}
//	
//	/**
//	 * 结束特定tag log的输出
//	 * @param tags 要输出的tag
//	 * @param appid
//	 * @param needUpload 是否需要上报给server
//	 * @param opinfo 上报时候的自定义标识 用于区分
//	 */
//	public static void endColorLog(String[] tags,int appid,boolean needUpload, String opinfo ) {
//		QLogImpl.endColorLog(tags,appid,needUpload,opinfo);
//	}
}
