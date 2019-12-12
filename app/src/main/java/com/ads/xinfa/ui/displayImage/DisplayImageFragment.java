package com.ads.xinfa.ui.displayImage;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ads.xinfa.R;
import com.ads.xinfa.base.MyFragment;
import com.ads.xinfa.bean.MyBean;
import com.bumptech.glide.Glide;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.loader.ImageLoader;

import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DisplayImageFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DisplayImageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DisplayImageFragment extends MyFragment {
    private static final String TAG = "DisplayImageFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private ArrayList<String> files = new ArrayList<>();
    private ArrayList<String> titles = new ArrayList<>();
    View view;
    private Unbinder unbinder;
    @BindView(R.id.banner)
    Banner mBanner;
    private boolean isDisplay = false;
    MyBean.GroupsBean.GroupBean.AreasBean.AreaBean areaBean;

    public DisplayImageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment DisplayImageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DisplayImageFragment newInstance(String param1) {
        DisplayImageFragment fragment = new DisplayImageFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            type = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            view = inflater.inflate(R.layout.fragment_display_image,container,false);
            unbinder = ButterKnife.bind(this, view);
            initBanner();
            if (isDisplay) {
                ViewGroup.LayoutParams params = mBanner.getLayoutParams();
                if (areaBean!=null) {
                    params.height = Integer.valueOf(areaBean.getInfo().getHeight());
                    params.width = Integer.valueOf(areaBean.getInfo().getWidth());
                }
                mBanner.setImages(files);
                //设置标题集合（当banner样式有显示title时）
//                mBanner.setBannerTitles(titles);
                mBanner.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return view;
    }

    private void initBanner() {
        //设置banner样式
//        mBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE);
        //设置图片加载器
        mBanner.setImageLoader(new GlideImageLoader());
        //设置banner动画效果
        mBanner.setBannerAnimation(Transformer.BackgroundToForeground);
        //设置自动轮播，默认为true
        mBanner.isAutoPlay(true);
        //设置轮播时间
        mBanner.setDelayTime(3000);
        //设置指示器位置（当banner模式中有指示器时）
        mBanner.setIndicatorGravity(BannerConfig.CENTER);
    }

    @Override
    public void doInit(MyBean.GroupsBean.GroupBean.AreasBean.AreaBean areaBean) {
        this.areaBean = areaBean;
        if (areaBean!=null) {
            files.clear();
            final MyBean.GroupsBean.GroupBean.AreasBean.AreaBean.FilesBean filesBean = areaBean.getFiles();
            final ArrayList<MyBean.GroupsBean.GroupBean.AreasBean.AreaBean.FilesBean.FileBean> fileBeanList= filesBean.getFile();
            final int length = fileBeanList.size();
            int[] time = new int[length];
            for (int i = 0; i < length; i++) {
                String path = fileBeanList.get(i).getPath();
//                String fileName = path.substring(path.lastIndexOf("/"));
                //File file =new File(Resource_DIR+fileName);
//                MyLogger.i(TAG,"path ... " + FileManager.Resource_DIR + fileName);
//                files.add(FileManager.Resource_DIR + fileName);
                files.add(path);
                String s = fileBeanList.get(i).getTime();
                time[i] = Integer.parseInt(s) * 1000;
            }
            isDisplay = true;
        }
    }

    @Override
    public void display() {
        if (mBanner!=null) {
            mBanner.start();
        }
    }


    class GlideImageLoader extends ImageLoader {

        @Override
        public void displayImage(Context context, Object path, ImageView imageView) {
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            Glide.with(context).load(path).into(imageView);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder!=null) {
            unbinder.unbind();
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }



    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
