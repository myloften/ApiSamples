package com.loften.android.api.app;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.loften.android.api.R;

/**
 *通常在Activity销毁时和Activity关联的Fragment也会被销毁。当Activity重建时会自动创建相关的Fragment。因此经常在
 * Activity的onCreate 函数中判处savedInstanceState 是否为空，（当Activity 有关联的Fragment时，重建Activity时
 * savedInstanceState不为空）来避免重复创建Fragment。重建的Fragment和之前的Fragment是两个不同的对象。但是如果对
 * Fragment调用setRetainInstance(true)，那么在Activity销毁时（设置改变导致的activity销毁，如横竖屏切换）会保留该Fragment
 * （onDetach会被调用，onDestroy不会被调用），Activity重建时会继续关联该Fragment。即通过FragmentManager 得到的
 * 还是之前的Fragment。可以利用Fragment的这个性质保存Activity的状态。 与通过onSaveInstance或onRetainNonConfiguratinInstance()
 * 方法相比，通过Fragment保存状态很方便。特别是对于比较大的对象如Bitmap或不容易序列化的 对象（如本例中的线程对象)。
 * 用于保存状态的Fragment一般不能有视图（onCreateView 返回null），但是可以设置TargetFragment，可以获取TargetFragment，
 * 更新TargetFragment的UI。
 */
public class FragmentRetainInstance extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null){
            getFragmentManager().beginTransaction().add(android.R.id.content,
                    new UiFragment()).commit();
        }
    }

    public static class UiFragment extends Fragment{
        RetainedFragment mWorkFragment;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_retain_instance, container, false);

            Button button = (Button)v.findViewById(R.id.restart);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mWorkFragment.restart();
                }
            });

            return v;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            FragmentManager fm = getFragmentManager();

            mWorkFragment = (RetainedFragment)fm.findFragmentByTag("work");

            //如果Fragment不为null，那么它就是在配置变化的时候被保存下来的
            if(mWorkFragment == null){
                mWorkFragment = new RetainedFragment();
                mWorkFragment.setTargetFragment(this, 0);
                fm.beginTransaction().add(mWorkFragment, "work").commit();
            }
        }
    }

    /**
     * 这个Fragment开启一个线程，在状态发生变化的时候能够保存下来，不被销毁
     */
    public static class RetainedFragment extends Fragment{
        ProgressBar mProgressBar;
        int mPosition;
        boolean mReady = false;
        boolean mQuiting = false;

        final Thread mThread = new Thread(){
            @Override
            public void run() {
                int max = 10000;

                while (true){
                    synchronized (this){
                        while (!mReady || mPosition >= max){
                            if(mQuiting){
                                return;
                            }
                            try {
                                wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        mPosition++;
                        max = mProgressBar.getMax();
                        mProgressBar.setProgress(mPosition);
                    }
                    synchronized (this){
                        try {
                            wait(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            //在配置变化的时候将这个fragment保存下来
            setRetainInstance(true);

            mThread.start();
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);

            mProgressBar = (ProgressBar)getTargetFragment().getView().findViewById(
                    R.id.progress_horizontal);

            synchronized (mThread){
                mReady = true;
                mThread.notify();
            }
        }

        @Override
        public void onDestroy() {
            synchronized (mThread){
                mReady = false;
                mQuiting = true;
                mThread.notify();
            }
            super.onDestroy();
        }

        @Override
        public void onDetach() {
            synchronized (mThread){
                mProgressBar = null;
                mReady = false;//避免更新进度条
                mThread.notify();
            }
            super.onDetach();
        }

        public void restart(){
            synchronized (mThread){
                mPosition = 0;
                mThread.notify();
            }
        }
    }
}
