package com.util;

/**
 * 常量类
 * Created by cts on 17/5/31.
 */

public class Constant {

    public static final int MSG_RTC_NEWCALL = 10000;

    public static final int MSG_RTC_ONVIDEO = 10001;
    public static final int MSG_RTC_DISCONNECT = 10002;
    public static final int MSG_PASSWORD_CHECK = 10003;
    public static final int MSG_LOCK_OPENED = 10004;
    public static final int MSG_CALLMEMBER_ERROR = 10005;
    public static final int MSG_CALLMEMBER_TIMEOUT = 11005;
    public static final int MSG_CALLMEMBER_NO_ONLINE = 12005;
    public static final int MSG_CALLMEMBER_SERVER_ERROR = 12105;
    public static final int MSG_CALLMEMBER_TIMEOUT_AND_TRY_DIRECT = 13005;
    public static final int MSG_CALLMEMBER_DIRECT_TIMEOUT = 14005;
    public static final int MSG_CALLMEMBER_DIRECT_DIALING = 15005;
    public static final int MSG_CALLMEMBER_DIRECT_SUCCESS = 16005;
    public static final int MSG_CALLMEMBER_DIRECT_FAILED = 17005;
    public static final int MSG_CALLMEMBER_DIRECT_COMPLETE = 18005;

    public static final int MSG_CONNECT_ERROR = 10007;
    public static final int MSG_CONNECT_SUCCESS = 10008;
    public static final int ON_YUNTONGXUN_INIT_ERROR = 10009;
    public static final int ON_YUNTONGXUN_LOGIN_SUCCESS = 10010;
    public static final int ON_YUNTONGXUN_LOGIN_FAIL = 10011;
    public static final int MSG_CANCEL_CALL_COMPLETE = 10012;

    public static final int MSG_ADVERTISE_REFRESH = 10013;
    public static final int MSG_ADVERTISE_IMAGE = 10014;
    public static final int MSG_INVALID_CARD = 10015;//无效房卡
    public static final int MSG_CHECK_BLOCKNO = 10016;//检测楼栋编号
    public static final int MSG_FINGER_CHECK = 10017;
    public static final int MSG_REFRESH_DATA = 10018;
    public static final int MSG_REFRESH_COMMUNITYNAME = 10019;
    public static final int MSG_REFRESH_LOCKNAME = 10020;
    public static final int MSG_INPUT_CARDINFO_SUCCEED = 0x01;//录入卡信息成功
    public static final int MSG_INPUT_CARDINFO_FAIL = 0x02;//录入卡信息失败
    public static final int MSG_INPUT_CARDINFO_REPETITION = 0x03;//重复录入卡信息

    public static final int CALL_MODE = 1;
    public static final int PASSWORD_MODE = 2;
    public static final int CALLING_MODE = 3;
    public static final int ONVIDEO_MODE = 4;
    public static final int DIRECT_MODE = 5;
    public static final int ERROR_MODE = 6;
    public static final int DIRECT_CALLING_MODE = 7;
    public static final int DIRECT_CALLING_TRY_MODE = 8;
    public static final int PASSWORD_CHECKING_MODE = 9;
    public static final int CALL_CANCEL_MODE = 10;

}
