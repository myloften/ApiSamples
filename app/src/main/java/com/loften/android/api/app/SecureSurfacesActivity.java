package com.loften.android.api.app;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.loften.android.api.R;
import com.loften.android.api.graphics.CubeRenderer;

public class SecureSurfacesActivity extends AppCompatActivity implements View.OnClickListener {

    private Button dialog;
    private GLSurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secure_surfaces);

        //1.activity下设置安全模式
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        initView();
    }

    private void initView() {
        dialog = (Button) findViewById(R.id.dialog);
        surfaceView = (GLSurfaceView) findViewById(R.id.surface_view);
        surfaceView.setRenderer(new CubeRenderer(false));
        //2.surface view设置安全模式
        surfaceView.setSecure(true);
        dialog.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog:
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setPositiveButton(android.R.string.ok, null)
                        .setMessage(R.string.secure_dialog_text)
                        .create();
                //3.dialog设置安全模式
                dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE);
                dialog.show();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        surfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        surfaceView.onPause();
    }
}
