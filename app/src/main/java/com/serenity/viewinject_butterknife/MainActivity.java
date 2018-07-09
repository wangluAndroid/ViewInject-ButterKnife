package com.serenity.viewinject_butterknife;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.BindView;
import com.example.ButterKnifer;
import com.example.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv_text)
    public TextView textView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnifer.bind(this);


    }

    @OnClick(R.id.tv_text)
    public void testOnClick(View view) {
        Toast.makeText(this, "点击了View", Toast.LENGTH_SHORT).show();
    }
}
