package com.ads.xinfa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.ads.xinfa.ui.welcome.CustomWelcomeActivity;

public class StartBootCompleteReceiver extends BroadcastReceiver {
    static final String action_boot ="android.intent.action.BOOT_COMPLETED";
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(action_boot)){
            Intent intent1 = new Intent(context, CustomWelcomeActivity.class);
            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent1);
        }
    }
}
