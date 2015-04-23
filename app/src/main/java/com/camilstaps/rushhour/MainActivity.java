package com.camilstaps.rushhour;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClickHandler(View v) {
        switch (v.getId()) {
            case R.id.action_start:
                Intent intent = new Intent(this, GamePlayActivity.class);
                startActivity(intent);
                break;
        }
    }

}
