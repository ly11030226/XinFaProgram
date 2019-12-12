package com.ads.clientconnection.ui.soccer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ads.clientconnection.R;
import com.gc.materialdesign.views.CheckBox;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SoccerActivity extends AppCompatActivity {

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
    @BindView(R.id.cb1)
    CheckBox cb1;
    @BindView(R.id.cb2)
    CheckBox cb2;
    @BindView(R.id.cb3)
    CheckBox cb3;
    @BindView(R.id.cb4)
    CheckBox cb4;
    @BindView(R.id.cb5)
    CheckBox cb5;
    @BindView(R.id.btn_commit)
    Button btnCommit;

    ArrayList<String> names = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soccer);
        ButterKnife.bind(this);
        try {
            btnCommit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String str1 = tv1.getText().toString();
                    String str2 = tv2.getText().toString();
                    String str3 = tv3.getText().toString();
                    String str4 = tv4.getText().toString();
                    String str5 = tv5.getText().toString();
                    if (cb1.isCheck()) {
                        names.add(str1);
                    }else if (names.contains(str1)) {
                        names.remove(str1);
                    }
                    if (cb2.isCheck()) {
                        names.add(str2);
                    }else if (names.contains(str2)) {
                        names.remove(str2);
                    }
                    if (cb3.isCheck()) {
                        names.add(str3);
                    }else if (names.contains(str3)) {
                        names.remove(str3);
                    }
                    if (cb4.isCheck()) {
                        names.add(str4);
                    }else if (names.contains(str4)) {
                        names.remove(str4);
                    }
                    if (cb5.isCheck()) {
                        names.add(str5);
                    }else if (names.contains(str5)) {
                        names.remove(str5);
                    }
                    Intent intent = new Intent(SoccerActivity.this,ChoiceMatchActivity.class);
                    intent.putExtra("KEY_SOCCER",names);
                    startActivity(intent);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
