package com.main.wayfinding.utility;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Utils for setting notice widgets.
 *
 * @author jia72
 * @author Last Modified By jia72
 * @version Revision: 0
 * Date: 2022/3/20 21:53
 */
public class NoticeUtils {
    /**
     * Method for creating a toast
     * */
    public static void createToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Method for creating normal alertdialog.
     */
    public static void createAlertDialog(Context context, String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        builder.setMessage(msg);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                alertDialog.dismiss();
                t.cancel();
            }
        }, 2000);
    }
}
