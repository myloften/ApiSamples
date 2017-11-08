package com.loften.android.api.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.loften.android.api.R;

public class DialogActivity extends Activity implements View.OnClickListener {

    private TextView text;
    private Button add;
    private Button remove;
    private LinearLayout innerContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.activity_dialog);
        initView();
        getWindow().setTitle("This is just a test");

        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
                android.R.drawable.ic_dialog_alert);
    }

    private void initView() {
        text = (TextView) findViewById(R.id.text);
        add = (Button) findViewById(R.id.add);
        remove = (Button) findViewById(R.id.remove);
        innerContent = (LinearLayout) findViewById(R.id.inner_content);

        add.setOnClickListener(this);
        remove.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add:
                ImageView iv = new ImageView(this);
                iv.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.head));
                iv.setPadding(4,4,4,4);
                innerContent.addView(iv);
                break;
            case R.id.remove:
                int num = innerContent.getChildCount();
                if(num > 0){
                    innerContent.removeViewAt(num-1);
                }
                break;
            default:
                break;
        }
    }
}
