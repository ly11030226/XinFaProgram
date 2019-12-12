package com.ads.xinfa;

import android.app.Activity;
import android.view.View;

/**
 * Created by Administrator on 2017/5/26.
 */

public class HideNavigationBar {
    //隐藏按键
    public void hideNavigationBar(Activity activity) {
        int uiFlags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN; // hide status bar

        if( android.os.Build.VERSION.SDK_INT >= 19 ){
            uiFlags |= 0x00001000;    //SYSTEM_UI_FLAG_IMMERSIVE_STICKY: hide navigation bars - compatibility: building API level is lower thatn 19, use magic number directly for higher API target level
        } else {
            uiFlags |= View.SYSTEM_UI_FLAG_LOW_PROFILE;
        }
        activity.getWindow().getDecorView().setSystemUiVisibility(uiFlags);
    }
}
