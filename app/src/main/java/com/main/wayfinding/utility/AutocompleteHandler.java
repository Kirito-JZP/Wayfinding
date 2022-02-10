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
    public static final int TRIGGER_DEPT_MSG = 0;
    public static final int TRIGGER_DEST_MSG = 1;
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
        switch (msg.what) {
            case TRIGGER_DEPT_MSG:
                fragment.queryDeptAutocomplete();
                break;
            case TRIGGER_DEST_MSG:
                fragment.queryDestAutocomplete();
                break;
            default:
        }
    }
}
