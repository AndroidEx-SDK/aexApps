package com.phone.utils;

import org.json.JSONException;
import org.json.JSONObject;

public class Ajax {
    public static JSONObject getJSONObject(String str) {
        if (str == null || str.trim().length() == 0)
            return null;
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(str);
        } catch (JSONException e) {
            e.printStackTrace(System.err);
        }
        return jsonObject;
    }
}