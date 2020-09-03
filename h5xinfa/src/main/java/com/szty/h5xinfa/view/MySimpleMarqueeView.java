package com.szty.h5xinfa.view;

import android.content.Context;
import android.util.AttributeSet;

import com.gongwen.marqueen.MarqueeFactory;
import com.gongwen.marqueen.SimpleMF;
import com.gongwen.marqueen.SimpleMarqueeView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ly
 */
public class MySimpleMarqueeView extends SimpleMarqueeView {
    public MySimpleMarqueeView(Context context) {
        this(context,null);
    }

    public MySimpleMarqueeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        try {
            List<String> content = new ArrayList<String>();
            content.add("秉承 笃守诚信,创造卓越 的核心价值观");
            content.add("打造一流数字生态银行");
            content.add("让金融为美好生活创造价值");
            content.add("购买国债安全理财 绿色金融共创美好生活");
            MarqueeFactory mf = new SimpleMF<String>(context);
            mf.setData(content);
            setMarqueeFactory(mf);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
