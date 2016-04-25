package com.example.administrator.gesturelockdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

//public class MainActivity extends AppCompatActivity implements LockPatternView.OnPatternChangedListener {

public class MainActivity extends AppCompatActivity {

//    private TextView lockHint;
//
//    private LockPatternView lockPatternView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        lockHint = (TextView) findViewById(R.id.activity_main_lock_hint);
//        lockPatternView = (LockPatternView) findViewById(R.id.activity_main_lock);
//        lockPatternView.setOnPatternChangedListener(this);

        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, PasswordFragment.newInstance(PasswordFragment.TYPE_SETTING)).commit();

    }

//    @Override
//    public void onPatternChanged(String passwordStr) {
//        if (!TextUtils.isEmpty(passwordStr)) {
//            lockHint.setText(passwordStr);
//        } else {
//            lockHint.setText("至少5个图案");
//        }
//    }
//
//    @Override
//    public void onPatternStart(boolean isStart) {
//        if (isStart) {
//            lockHint.setText("请绘制图案");
//        }
//    }

}
