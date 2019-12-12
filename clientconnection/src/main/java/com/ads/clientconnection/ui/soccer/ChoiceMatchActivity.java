package com.ads.clientconnection.ui.soccer;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ads.clientconnection.R;

import java.util.ArrayList;
import java.util.Random;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ChoiceMatchActivity extends AppCompatActivity {

    ArrayList<String> names;
    @BindView(R.id.tv1)
    TextView tv1;
    @BindView(R.id.tv2)
    TextView tv2;
    @BindView(R.id.tv3)
    TextView tv3;
    @BindView(R.id.tv4)
    TextView tv4;
    @BindView(R.id.tv5)
    TextView tv5;
    @BindView(R.id.btnleft)
    Button btnLeft;
    @BindView(R.id.btnright)
    Button btnRight;

    String[] strs;
    ArrayList<Integer> temp = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice_match);
        ButterKnife.bind(this);
        try {
            if (getIntent()!=null && getIntent().hasExtra("KEY_SOCCER")) {
                names = (ArrayList<String>) getIntent().getSerializableExtra("KEY_SOCCER");
                if (names!=null && names.size()>0) {
                    strs = new String[names.size()];
                }
            }
            btnLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getResult();
                }
            });

            btnRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getResult(){
        start();
        switch (strs.length) {
            case 1:
                tv1.setVisibility(View.VISIBLE);
                tv2.setVisibility(View.GONE);
                tv3.setVisibility(View.GONE);
                tv4.setVisibility(View.GONE);
                tv5.setVisibility(View.GONE);
                tv1.setText("1-"+strs[0]);
                break;
            case 2:
                tv1.setVisibility(View.VISIBLE);
                tv2.setVisibility(View.VISIBLE);
                tv3.setVisibility(View.GONE);
                tv4.setVisibility(View.GONE);
                tv5.setVisibility(View.GONE);
                tv1.setText("1-"+strs[0]);
                tv2.setText("2-"+strs[1]);
                break;
            case 3:
                tv1.setVisibility(View.VISIBLE);
                tv2.setVisibility(View.VISIBLE);
                tv3.setVisibility(View.VISIBLE);
                tv4.setVisibility(View.GONE);
                tv5.setVisibility(View.GONE);
                tv1.setText("1-"+strs[0]);
                tv2.setText("2-"+strs[1]);
                tv3.setText("3-"+strs[2]);
                break;
            case 4:
                tv1.setVisibility(View.VISIBLE);
                tv2.setVisibility(View.VISIBLE);
                tv3.setVisibility(View.VISIBLE);
                tv4.setVisibility(View.VISIBLE);
                tv5.setVisibility(View.GONE);
                tv1.setText("1-"+strs[0]);
                tv2.setText("2-"+strs[1]);
                tv3.setText("3-"+strs[2]);
                tv4.setText("4-"+strs[3]);
                break;
            case 5:
                tv1.setVisibility(View.VISIBLE);
                tv2.setVisibility(View.VISIBLE);
                tv3.setVisibility(View.VISIBLE);
                tv4.setVisibility(View.VISIBLE);
                tv5.setVisibility(View.VISIBLE);
                tv1.setText("1-"+strs[0]);
                tv2.setText("2-"+strs[1]);
                tv3.setText("3-"+strs[2]);
                tv4.setText("4-"+strs[3]);
                tv5.setText("5-"+strs[4]);
                break;
            default:
                break;
        }

    }


    private void start(){
        temp.clear();
        for(int i=0;i<names.size();i++){
            int number;
            while(!temp.contains(number = getNumber())){
                strs[i] = names.get(number-1);
                temp.add(number);
            }
        }
    }

    private int getNumber(){
        return new Random().nextInt(names.size()) + 1;
    }
}
