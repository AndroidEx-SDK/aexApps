package com.tencent.device.barrage;

/**
 * 弹幕回调接口
 * @author dapingyu
 *
 */

public interface IBarrageListener {
    
    /**
     * 接收的消息.
     * @param msg
     */
    public void onReceiveMsg(BarrageMsg msg);
    
    /**
     * 弹幕关闭开启
     * @param groupId   群组名称
     * @param isBarrageOn   群消息开关
     * @param isVoiceOn     语音开关, 仅在群消息开启下有效.
     */
    public void onBarrageSwitched(long groupId, boolean isBarrageOn, boolean isVoiceOn);
    
}
