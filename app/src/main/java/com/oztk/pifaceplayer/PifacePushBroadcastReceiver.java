package com.oztk.pifaceplayer;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.parse.ParsePushBroadcastReceiver;

/**
 * @author Paul Duguet
 * @version 1
 */
public class PifacePushBroadcastReceiver extends ParsePushBroadcastReceiver {
    @Override
    protected void onPushReceive(Context context, Intent intent) {
        Toast.makeText(context, "Push received", Toast.LENGTH_LONG).show();
    }
}
