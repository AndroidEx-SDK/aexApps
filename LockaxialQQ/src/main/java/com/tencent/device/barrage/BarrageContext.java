package com.tencent.device.barrage;

/**
 * 弹幕消息常量类
 * @author dapingyu
 *
 */
public class BarrageContext {

    /**
     * 消息类型
     */
    public final static int MSG_TEXT = 1;   //文本
    
    public final static int MSG_VOICE = 4;  //语音
    
    public final static int MSG_FACE = 2;   //表情
    
    public final static int MSG_IMAGE = 3;  //图片
        
    public final static int MSG_OTHER = 0;  //其他, 暂时预留
    
    
//    element_none            = 0,
//    	    element_text            = 1,        // 文本消息
//    	    element_face            = 2,        // 表情
//    	    element_image           = 3,        // 图片
//    	    element_audio           = 4,        // 语音消息
}
