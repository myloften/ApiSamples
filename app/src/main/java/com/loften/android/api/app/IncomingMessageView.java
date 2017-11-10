package com.loften.android.api.app;

import android.app.NotificationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.loften.android.api.R;

public class IncomingMessageView extends AppCompatActivity {

    static final public String KEY_FROM = "from";

    static final public String KEY_MESSAGE = "message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_message_view);

        ((TextView)findViewById(R.id.from)).setText(
                getIntent().getCharSequenceExtra(KEY_FROM));
        ((TextView)findViewById(R.id.message)).setText(
                getIntent().getCharSequenceExtra(KEY_MESSAGE));

        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(R.string.imcoming_message_ticker_text);
    }
}
