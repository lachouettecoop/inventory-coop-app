package org.coop.inventory.mixin;

import android.util.Base64;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class JWTUtils {

    public static JSONObject decoded(String JWTEncoded) throws Exception {
        String[] split = JWTEncoded.split("\\.");
        JSONObject payload = new JSONObject(getJson(split[1]));
        return payload;
    }

    private static String getJson(String strEncoded) throws UnsupportedEncodingException{
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }
}
