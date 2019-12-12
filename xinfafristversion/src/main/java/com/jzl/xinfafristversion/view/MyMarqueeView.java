package com.jzl.xinfafristversion.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.jzl.xinfafristversion.R;
import com.jzl.xinfafristversion.base.MyLogger;
import com.jzl.xinfafristversion.bean.MyBean;
import com.jzl.xinfafristversion.myInterface.IMarqueeItem;
import com.jzl.xinfafristversion.utils.ColorTool;
import com.jzl.xinfafristversion.utils.Tools;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.AnimRes;
import androidx.annotation.FontRes;
import androidx.core.content.res.ResourcesCompat;

/**
 *
 */
public class MyMarqueeView<T> extends ViewFlipper {
    private static final String TAG = "MyMarqueeView";

    public int interval = 12 * 1000;
    public boolean hasSetAnimDuration = false;
    public int animDuration = 1000;
    public int textSize = 14;
    public int textColor = 0xff000000;
    public boolean singleLine = false;

    private int gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
    private static final int GRAVITY_LEFT = 0;
    private static final int GRAVITY_CENTER = 1;
    private static final int GRAVITY_RIGHT = 2;

    private int direction = DIRECTION_BOTTOM_TO_TOP;
    private static final int DIRECTION_BOTTOM_TO_TOP = 0;
    private static final int DIRECTION_TOP_TO_BOTTOM = 1;
    private static final int DIRECTION_RIGHT_TO_LEFT = 2;
    private static final int DIRECTION_LEFT_TO_RIGHT = 3;

    public Typeface typeface;
    public MyBean.GroupsBean.GroupBean.AreasBean.AreaBean areaBean;


    @AnimRes
    private int inAnimResId = R.anim.anim_bottom_in;
    @AnimRes
    private int outAnimResId = R.anim.anim_top_out;

    private int position;
    private List<T> messages = new ArrayList<>();
    private MyMarqueeView.OnItemClickListener onItemClickListener;

    public MyMarqueeView(Context context, MyBean.GroupsBean.GroupBean.AreasBean.AreaBean areaBean) {
        this(context, areaBean, null);

    }

    public MyMarqueeView(Context context, MyBean.GroupsBean.GroupBean.AreasBean.AreaBean areaBean, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, areaBean);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, MyBean.GroupsBean.GroupBean.AreasBean.AreaBean areaBean) {
        this.areaBean = areaBean;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MarqueeViewStyle, defStyleAttr, 0);

        interval = typedArray.getInteger(R.styleable.MarqueeViewStyle_mvInterval, interval);
        hasSetAnimDuration = typedArray.hasValue(R.styleable.MarqueeViewStyle_mvAnimDuration);
        animDuration = typedArray.getInteger(R.styleable.MarqueeViewStyle_mvAnimDuration, animDuration);
        singleLine = typedArray.getBoolean(R.styleable.MarqueeViewStyle_mvSingleLine, false);
        if (typedArray.hasValue(R.styleable.MarqueeViewStyle_mvTextSize)) {
            textSize = (int) typedArray.getDimension(R.styleable.MarqueeViewStyle_mvTextSize, textSize);
            textSize = Tools.px2sp(context, textSize);
        }
        textColor = typedArray.getColor(R.styleable.MarqueeViewStyle_mvTextColor, textColor);
        @FontRes int fontRes = typedArray.getResourceId(R.styleable.MarqueeViewStyle_mvFont, 0);
        if (fontRes != 0) {
            typeface = ResourcesCompat.getFont(context, fontRes);
        }
        int gravityType = typedArray.getInt(R.styleable.MarqueeViewStyle_mvGravity, GRAVITY_LEFT);
        switch (gravityType) {
            case GRAVITY_LEFT:
                gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
                break;
            case GRAVITY_CENTER:
                gravity = Gravity.CENTER;
                break;
            case GRAVITY_RIGHT:
                gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
                break;
        }

        if (typedArray.hasValue(R.styleable.MarqueeViewStyle_mvDirection)) {
            direction = typedArray.getInt(R.styleable.MarqueeViewStyle_mvDirection, direction);
            switch (direction) {
                case DIRECTION_BOTTOM_TO_TOP:
                    inAnimResId = R.anim.anim_bottom_in;
                    outAnimResId = R.anim.anim_top_out;
                    break;
                case DIRECTION_TOP_TO_BOTTOM:
                    inAnimResId = R.anim.anim_top_in;
                    outAnimResId = R.anim.anim_bottom_out;
                    break;
                case DIRECTION_RIGHT_TO_LEFT:
                    inAnimResId = R.anim.anim_right_in;
                    outAnimResId = R.anim.anim_left_out;
                    break;
                case DIRECTION_LEFT_TO_RIGHT:
                    inAnimResId = R.anim.anim_left_in;
                    outAnimResId = R.anim.anim_right_out;
                    break;
            }
        } else {
            inAnimResId = R.anim.anim_bottom_in;
            outAnimResId = R.anim.anim_top_out;
        }

        typedArray.recycle();
        setFlipInterval(interval);
    }

    /**
     * 根据字符串，启动翻页公告
     *
     * @param notice 字符串
     */
    public void startWithText(String notice) {
        startWithText(notice, inAnimResId, outAnimResId);
    }

    /**
     * 根据字符串，启动翻页公告
     *
     * @param notice       字符串
     * @param inAnimResId  进入动画的resID
     * @param outAnimResID 离开动画的resID
     */
    public void startWithText(final String notice, final @AnimRes int inAnimResId, final @AnimRes int outAnimResID) {
        if (TextUtils.isEmpty(notice))
            return;
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                startWithFixedWidth(notice, inAnimResId, outAnimResID);
            }
        });
    }

    /**
     * 根据字符串和宽度，启动翻页公告
     *
     * @param notice 字符串
     */
    private void startWithFixedWidth(String notice, @AnimRes int inAnimResId, @AnimRes int outAnimResID) {
        int noticeLength = notice.length();
        int width = Tools.px2dip(getContext(), getWidth());
        if (width == 0) {
            throw new RuntimeException("Please set the width of MarqueeView !");
        }
        int limit = width / textSize;
        List list = new ArrayList();

        if (noticeLength <= limit) {
            list.add(notice);
        } else {
            int size = noticeLength / limit + (noticeLength % limit != 0 ? 1 : 0);
            for (int i = 0; i < size; i++) {
                int startIndex = i * limit;
                int endIndex = ((i + 1) * limit >= noticeLength ? noticeLength : (i + 1) * limit);
                list.add(notice.substring(startIndex, endIndex));
            }
        }

        if (messages == null)
            messages = new ArrayList<>();
        messages.clear();
        messages.addAll(list);
        postStart(inAnimResId, outAnimResID);
    }

    /**
     * 根据字符串列表，启动翻页公告
     *
     * @param messages 字符串列表
     */
    public void startWithList(List<T> messages) {
        startWithList(messages, inAnimResId, outAnimResId);
    }

    /**
     * 根据字符串列表，启动翻页公告
     *
     * @param messages     字符串列表
     * @param inAnimResId  进入动画的resID
     * @param outAnimResID 离开动画的resID
     */
    public void startWithList(List<T> messages, @AnimRes int inAnimResId, @AnimRes int outAnimResID) {
        if (Tools.isEmpty(messages))
            return;
        setMessages(messages);
        postStart(inAnimResId, outAnimResID);
    }

    private void postStart(final @AnimRes int inAnimResId, final @AnimRes int outAnimResID) {
        post(new Runnable() {
            @Override
            public void run() {
                start(inAnimResId, outAnimResID);
            }
        });
    }

    private boolean isAnimStart = false;

    private void start(final @AnimRes int inAnimResId, final @AnimRes int outAnimResID) {
        removeAllViews();
        clearAnimation();
        // 检测数据源
        if (messages == null || messages.isEmpty()) {
            throw new RuntimeException("The messages cannot be empty!");
        }
        position = 0;
        addView(createTextView(messages.get(position)));


        if (messages.size() > 1) {
            setInAndOutAnimation(inAnimResId, outAnimResID);
            startFlipping();
        }

        if (getInAnimation() != null) {
            getInAnimation().setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    MyLogger.i(TAG,"in animation start");
                    if (isAnimStart) {
                        animation.cancel();
                    }
                    isAnimStart = true;
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    MyLogger.i(TAG,"in animation end");
                    position++;
                    if (position >= messages.size()) {
                        position = 0;
                    }
                    View view = createTextView(messages.get(position));
                    if (view.getParent() == null) {
                        addView(view);
                    }
                    isAnimStart = false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            if (getOutAnimation()!=null) {
                getOutAnimation().setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                        MyLogger.i(TAG,"out animation start");
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        MyLogger.i(TAG,"out animation end");
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
            }
        }
    }

    public void setTextSize(int size) {
        this.textSize = size;
    }

    public void setTextColor(int color) {
        this.textColor = color;
    }

    public void setAnimDuration(int duration) {
        this.animDuration = duration;
    }


    private TextView createTextView(T marqueeItem) {
        int index = (getDisplayedChild() + 1) % 3;
        MyLogger.i(TAG,"createTextView child at index ..."+index);
        TextView textView = (TextView) getChildAt(index);
        if (textView == null) {
            textView = new TextView(getContext());
            textView.setGravity(gravity | Gravity.CENTER_VERTICAL);
            textView.setTextColor(textColor);
            textView.setTextSize(textSize);
            textView.setIncludeFontPadding(true);
            textView.setSingleLine(singleLine);
            if (singleLine) {
                textView.setMaxLines(1);
                textView.setEllipsize(TextUtils.TruncateAt.END);
            }
            if (typeface != null) {
                textView.setTypeface(typeface);
            }
            textView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(getPosition(), (TextView) v);
                    }
                }
            });
        }
        CharSequence message = "";
        if (marqueeItem instanceof CharSequence) {
            message = (CharSequence) marqueeItem;
        } else if (marqueeItem instanceof IMarqueeItem) {
            message = ((IMarqueeItem) marqueeItem).marqueeMessage();
        }
                        textView.setText(message);
        textView.setTag(position);
        return textView;
    }

    public int getPosition() {
        return (int) getCurrentView().getTag();
    }

    public List<T> getMessages() {
        return messages;
    }

    public void setMessages(List<T> messages) {
        this.messages = messages;
    }

    public void setOnItemClickListener(MyMarqueeView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, TextView textView);
    }

    /**
     * 设置进入动画和离开动画
     *
     * @param inAnimResId  进入动画的resID
     * @param outAnimResID 离开动画的resID
     */
    private void setInAndOutAnimation(@AnimRes int inAnimResId, @AnimRes int outAnimResID) {
        Animation inAnim = AnimationUtils.loadAnimation(getContext(), inAnimResId);
        MyLogger.i(TAG, "animDuration ... " + animDuration);
        if (hasSetAnimDuration)
            inAnim.setDuration(animDuration);
        setInAnimation(inAnim);

        Animation outAnim = AnimationUtils.loadAnimation(getContext(), outAnimResID);
        if (hasSetAnimDuration)
            outAnim.setDuration(animDuration);
        setOutAnimation(outAnim);
    }

    public void setTypeface(Typeface typeface) {
        this.typeface = typeface;
    }

    public void startMarqueeView() {
        if (areaBean == null) {
            return;
        }
        //跑马灯FileBean的报文都一样，因此去第一个FileBean即可
        MyBean.GroupsBean.GroupBean.AreasBean.AreaBean.FilesBean.FileBean fileBean = areaBean.getFiles().getFile().get(0);
        T content = (T) fileBean.getPath();
        hasSetAnimDuration = true;
        String time = fileBean.getTime();
        animDuration = 1000 * Integer.valueOf(time);

        //设置字体颜色
        String textColor = areaBean.getInfo().getFcolor();
        this.textColor = Color.parseColor(new ColorTool().colorChange(textColor));
        //设置字体大小
        String textSize = areaBean.getInfo().getFsize();
        this.textSize = Integer.valueOf(textSize);
        //设置背景颜色
        String bgColor = areaBean.getInfo().getBgcolor();
        setBackgroundColor(Color.parseColor("#" + bgColor));
        //设置进场出场方式
        String rollType = areaBean.getInfo().getType1();
        int animIn;
        int animOut;
        if ("right".equals(rollType)) {
            animIn = R.anim.anim_right_in;
            animOut = R.anim.anim_left_out;
            gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
        } else if ("left".equals(rollType)) {
            animIn = R.anim.anim_left_in;
            animOut = R.anim.anim_right_out;
            gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        } else {
            animIn = R.anim.anim_bottom_in;
            animOut = R.anim.anim_top_out;
            gravity = GRAVITY_CENTER;
        }
        List<T> list = new ArrayList<>();
        list.add(content);
        list.add(content);
        startWithList(list, animIn, animOut);
    }

    public MyBean.GroupsBean.GroupBean.AreasBean.AreaBean getAreaBean() {
        return this.areaBean;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (areaBean == null) {
            return;
        }
        int width = Integer.valueOf(areaBean.getInfo().getWidth());
        int height = Integer.valueOf(areaBean.getInfo().getHeight());
        measureChildren(MeasureSpec.makeMeasureSpec(width, MeasureSpec.getMode(widthMeasureSpec)), MeasureSpec.makeMeasureSpec(height, MeasureSpec.getMode(heightMeasureSpec)));
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }
    //     XML
    //    <com.sunfusheng.marqueeview.MarqueeView
    //    android:id="@+id/marqueeView"
    //    android:layout_width="match_parent"
    //    android:layout_height="30dp"
    //    app:mvAnimDuration="1000"
    //    app:mvDirection="bottom_to_top"
    //    app:mvInterval="3000"
    //    app:mvTextColor="@color/white"
    //    app:mvTextSize="14sp"
    //    app:mvSingleLine="true"
    //    app:mvFont="@font/huawenxinwei"/>

    //    设置字符串列表数据，或者设置自定义的Model数据类型
    //    MarqueeView marqueeView = (MarqueeView) findViewById(R.id.marqueeView);
    //
    //    List<String> messages = new ArrayList<>();
    //    messages.add("1. 大家好，我是孙福生。");
    //    messages.add("2. 欢迎大家关注我哦！");
    //    messages.add("3. GitHub帐号：sunfusheng");
    //    messages.add("4. 新浪微博：孙福生微博");
    //    messages.add("5. 个人博客：sunfusheng.com");
    //    messages.add("6. 微信公众号：孙福生");
    //    marqueeView.startWithList(messages);
    //
    //    // 或者设置自定义的Model数据类型
    //    public class CustomModel implements IMarqueeItem {
    //        @Override
    //        public CharSequence marqueeMessage() {
    //            return "...";
    //        }
    //    }
    //
    //    List<CustomModel> messages = new ArrayList<>();
    //    marqueeView.startWithList(messages);
    //
    //    // 在代码里设置自己的动画
    //    marqueeView.startWithList(messages, R.anim.anim_bottom_in, R.anim.anim_top_out);
    //    设置字符串数据
    //    String message = "心中有阳光，脚底有力量！心中有阳光，脚底有力量！心中有阳光，脚底有力量！";
    //    marqueeView.startWithText(message);
    //
    //    // 在代码里设置自己的动画
    //    marqueeView.startWithText(message, R.anim.anim_bottom_in, R.anim.anim_top_out);
    //    设置事件监听
    //    marqueeView.setOnItemClickListener(new MarqueeView.OnItemClickListener() {
    //        @Override
    //        public void onItemClick(int position, TextView textView) {
    //            Toast.makeText(getApplicationContext(), String.valueOf(marqueeView1.getPosition()) + ". " + textView.getText(), Toast.LENGTH_SHORT).show();
    //        }
    //    });
    //    重影问题可参考以下解决方案
    //    在 Activity 或 Fragment 中
    //
    //    @Override
    //    public void onStart() {
    //        super.onStart();
    //        marqueeView.startFlipping();
    //    }
    //
    //    @Override
    //    public void onStop() {
    //        super.onStop();
    //        marqueeView.stopFlipping();
    //    }
    //    在 ListView 或 RecyclerView 的 Adapter 中
    //
    //    @Override
    //    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
    //        super.onViewDetachedFromWindow(holder);
    //        holder.marqueeView.stopFlipping();
    //    }


}
