package com.tencent.av.camera;

public class VideoSettings {

	private static int initial_width = 320;//320;//192;//144;//176;
	private static int initial_height = 240;//240;//240;//192;//144;
	public static int format = 0;
	
    public static int width = initial_width;
    public static int height = initial_height;
    
    public static final byte DEFLECT_ANGLE_0 = 0x00;
	public static final byte DEFLECT_ANGLE_90 = 0x01;
	public static final byte DEFLECT_ANGLE_180 = 0x02;
	public static final byte DEFLECT_ANGLE_270 = 0x03;

}
