package com.example.administrator.gesturelockdemo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

/**
 * 欢迎界面
 */
public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_welcome);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sp = getSharedPreferences("sp", Context.MODE_PRIVATE);
                String passwordStr = sp.getString("password", "");
                // 没有设置密码
                if (TextUtils.isEmpty(passwordStr)) {
                    startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                    finish();
                // 密码检查
                } else {
                    getSupportFragmentManager().beginTransaction().replace(android.R.id.content, PasswordFragment.newInstance(PasswordFragment.TYPE_CHECK)).commit();
                }
            }
        }, 1000);
    }

}
