package com.androidex;

import com.androidex.plugins.kkaexparams;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cts on 17/2/15.
 * 得到userinfo里面的信息
 */

public class GetUserInfo {
    public static JSONObject get_authinfo()
    {
        kkaexparams aexparams = new kkaexparams();
        JSONObject userinfo = new JSONObject();
        try {
            int flag0 = aexparams.get_flag0();
            if(flag0 != 0 && flag0 != 0xFF) {
                String ui = aexparams.get_userinfo();
                if (ui != null)
                    userinfo = new JSONObject(ui);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return userinfo;
    }
    /**
     * 得到pid
     */
    public static String getPid(){
     return get_authinfo().optString("pid");
    }

    /**
     * 得到 sn
     */
    public static String getSn(){
        return get_authinfo().optString("sn");
    }
    /**
     * 得到liense
     */
    public static String getLicense(){
        return get_authinfo().optString("license");
    }
    /**
     * 得到pubkey
     */
    public static String getPubkey(){
        return  get_authinfo().optString("pubkey");
    }
}
