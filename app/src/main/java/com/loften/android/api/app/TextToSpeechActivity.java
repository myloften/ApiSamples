package com.loften.android.api.app;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.loften.android.api.R;

import java.util.Locale;
import java.util.Random;

/**
 * TextToSpeech必须再被实例化之后才能使用.实现TextToSpeech.OnInitListener方法来获取实例化结果的提醒。
 * 当你已经使用完TextToSpeech实例之后, 应该调用shutdown()方法来释放TextToSpeech所使用的本地资源
 *
 * 朗读文字不需要任何的权限，这个控件的好处是首先不要权限，其次不用联网
 */
public class TextToSpeechActivity extends AppCompatActivity implements View.OnClickListener, TextToSpeech.OnInitListener {

    private static final String TAG = "TextToSpeechDemo";
    private Button againButton;

    private TextToSpeech mTts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_to_speech);
        initView();

        mTts = new TextToSpeech(this, this);
    }

    private void initView() {
        againButton = (Button) findViewById(R.id.again_button);

        againButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.again_button:
                sayHello();
                break;
        }
    }

    /**
     * 用来初始化TextToSpeech引擎
     * status:SUCCESS或ERROR这2个值
     * setLanguage设置语言，帮助文档里面写了有22种
     * TextToSpeech.LANG_MISSING_DATA：表示语言的数据丢失。
     * TextToSpeech.LANG_NOT_SUPPORTED:不支持
     */
    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            //初始化成功
            int result = mTts.setLanguage(Locale.CHINA);
            if(result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e(TAG, "Language is not available.");
            }else{
                againButton.setEnabled(true);
                mTts.setPitch(0.1f);// 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
                sayHello();
            }
        }else{
            // 初始化失败
            Log.e(TAG, "Could not initialize TextToSpeech.");
        }
    }

    private static final Random RANDOM = new Random();
    private static final String[] HELLOS = {
            "你好",
            "打招呼",
            "问候",
            "教唆",
            "奉公守法",
            "阜罗特髻"
    };

    private void sayHello(){
        int helloLenght = HELLOS.length;
        String hello = HELLOS[RANDOM.nextInt(helloLenght)];
        mTts.speak(hello, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    protected void onDestroy() {
        if(mTts != null){
            mTts.stop();// 不管是否正在朗读TTS都被打断
            mTts.shutdown();// 关闭，释放资源
        }
        super.onDestroy();
    }
}
