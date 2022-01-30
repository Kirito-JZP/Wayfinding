package com.main.wayfinding.utility;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.main.wayfinding.fragment.map.MapFragment;

/**
 * Description
 *
 * @author Zehua Guo
 * @author Last Modified By Zehua Guo
 * @version Revision: 0
 * Date: 2022/1/30 0:28
 */
public class AutocompleteHandler extends Handler {
    public static final int TRIGGER_MSG = 0;
    private MapFragment fragment;

    public MapFragment getFragment() {
        return fragment;
    }

    public void setFragment(MapFragment fragment) {
        this.fragment = fragment;
    }


    @Override
    public void handleMessage(@NonNull Message msg) {
        super.handleMessage(msg);
        if (msg.what == TRIGGER_MSG) {
            fragment.queryAutocomplete();
        }
    }
}
