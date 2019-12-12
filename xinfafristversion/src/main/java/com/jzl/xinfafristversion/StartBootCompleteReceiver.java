package com.jzl.xinfafristversion;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jzl.xinfafristversion.ui.main.MainActivity;


public class StartBootCompleteReceiver extends BroadcastReceiver {
    static final String action_boot ="android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(action_boot)){
            Intent intent1 = new Intent(context, MainActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
        }
    }
}
