package com.semye.scratchcard;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ScratchView.OnScratchCompleteListener {

    ScratchView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWidget();
        initData();
    }


    private void initWidget() {
        view = (ScratchView) findViewById(R.id.scratchview);
        view.setOnScratchCompleteListener(this);
    }

    private void initData() {
        view.showTopText(true);
        view.showBottomText(true);
//        view.setTopText("刮开涂层");
//        view.setBottomText("恭喜您中了10元!");
    }


    @Override
    public void complete() {
        Toast.makeText(this,"刮开了涂层",Toast.LENGTH_SHORT).show();
    }
}
