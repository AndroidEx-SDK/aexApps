package com.tencent.device;

interface ITXDeviceService
{
    long getSelfDin();
    byte[] getVideoChatSignature();
    void notifyVideoServiceStarted();
    void sendVideoCall(long peerUin, in byte[] msg);
    void sendVideoCallM2M(long peerUin, in byte[] msg);
    void sendVideoCMD(long peerUin, in byte[] msg);    
}