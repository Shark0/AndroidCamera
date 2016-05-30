package com.shark.androidcamera;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindCameraButton();
    }

    private void bindCameraButton() {
        findViewById(R.id.activityMain_cameraButton).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        onCameraButtonClick();
    }

    private void onCameraButtonClick() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }
}
