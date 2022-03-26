package com.main.wayfinding.utility;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Description
 *
 * @author hu
 * @author Last Modified By hu
 * version Revision:0
 * Date:2022/3/26 20:38
 */
public class FileReaderUtils {
    public static String initAssets(Context context,String fileName) {
        String str = null;
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            str = getString(inputStream);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return str;
    }
    public static String getString(InputStream inputStream) {
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(inputStreamReader);
        StringBuffer sb = new StringBuffer("");
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\r\n\r\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();

    }
}
