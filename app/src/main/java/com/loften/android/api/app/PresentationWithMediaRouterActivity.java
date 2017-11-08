package com.loften.android.api.app;

import android.annotation.TargetApi;
import android.app.MediaRouteActionProvider;
import android.app.Presentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.media.MediaRouter;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.loften.android.api.R;
import com.loften.android.api.graphics.CubeRenderer;


public class PresentationWithMediaRouterActivity extends AppCompatActivity {

    private final String TAG = "PresentationWithMediaRouterActivity";

    private MediaRouter mMediaRouter;
    private DemoPresentation mPresentation;
    /**
     * 定义一个GLSurfaceView类，用于显示3D视图
     */
    private GLSurfaceView mSurfaceView;
    private TextView mInfoTextView;
    /**
     * 应用是否是可见状态的标志位
     */
    private boolean mPaused;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
         * 获取到媒体路由，当媒体路由被选择或取消选择或者路由首选的presentation显示屏幕发生变化时，
         * 它都会发送通知消息。一个应用程序可以非常简单通过地观察这些通知消息来自动地在首选的presentation
         * 显示屏幕上显示或隐藏一个presentation。
         */
        mMediaRouter = (MediaRouter)getSystemService(Context.MEDIA_ROUTER_SERVICE);

        setContentView(R.layout.activity_presentation_with_media_router);

        mSurfaceView = (GLSurfaceView)findViewById(R.id.surface_view);
        // 设置我们要渲染的图形为CubeRenderer，一个矩形3D模型
        mSurfaceView.setRenderer(new CubeRenderer(false));

        mInfoTextView = (TextView)findViewById(R.id.info);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //设置对媒体路由变化的监听
        mMediaRouter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouterCallback);

        mPaused = false;
        updatePresentation();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 取消媒体路由的监听
        mMediaRouter.removeCallback(mMediaRouterCallback);

        // 更新显示内容
        mPaused = true;
        updateContents();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mPresentation != null) {
            mPresentation.dismiss();
            mPresentation = null;
        }
    }

    private void updatePresentation() {
        //获取到被选中的媒体路由，类型为视频路由
        MediaRouter.RouteInfo route = mMediaRouter.getSelectedRoute(
                MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
        //获取到路由推荐的presentation display
        Display presentationDisplay = route != null ? route.getPresentationDisplay() : null;

        // 如果presentation已经存在且它的显示屏不是媒体路由推荐的显示屏，则取消presentation
        if (mPresentation != null && mPresentation.getDisplay() != presentationDisplay) {
            mPresentation.dismiss();
            mPresentation = null;
        }

        // 显示resentation
        if (mPresentation == null && presentationDisplay != null) {
            mPresentation = new DemoPresentation(this, presentationDisplay);
            //设置presentation的解除监听
            mPresentation.setOnDismissListener(mOnDismissListener);
            try {
                mPresentation.show();
            } catch (WindowManager.InvalidDisplayException ex) {
                mPresentation = null;
            }
        }

        // Update the contents playing in this activity.
        updateContents();
    }

    /**
     * 更新presentation中显示的内容，如果presentation已经创建并显示则将内容显示在presentation上
     * 如果没有创建presentation则将内容显示在主屏幕上
     */
    private void updateContents() {
        //显示在次级屏蔽上
        if (mPresentation != null) {
            mInfoTextView.setText(getResources().getString(
                    R.string.presentation_with_media_router_now_playing_remotely,
                    mPresentation.getDisplay().getName()));
            //隐藏主屏幕的surfaceView
            mSurfaceView.setVisibility(View.INVISIBLE);
            mSurfaceView.onPause();
            //如果当前应用处于不可见状态
            if (mPaused) {
                mPresentation.getSurfaceView().onPause();
            } else {
                mPresentation.getSurfaceView().onResume();
            }
        } else {
            //将内容显示到本机屏幕上
            mInfoTextView.setText(getResources().getString(
                    R.string.presentation_with_media_router_now_playing_locally,
                    getWindowManager().getDefaultDisplay().getName()));
            mSurfaceView.setVisibility(View.VISIBLE);
            if (mPaused) {
                mSurfaceView.onPause();
            } else {
                mSurfaceView.onResume();
            }
        }
    }

    private final MediaRouter.SimpleCallback mMediaRouterCallback =
            new MediaRouter.SimpleCallback() {
                @Override
                public void onRouteSelected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
                    updatePresentation();
                }

                @Override
                public void onRouteUnselected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
                    updatePresentation();
                }

                @Override
                public void onRoutePresentationDisplayChanged(MediaRouter router, MediaRouter.RouteInfo info) {
                    updatePresentation();
                }
            };

    /**
     * 监听presentations是否dismissed.
     */
    private final DialogInterface.OnDismissListener mOnDismissListener =
            new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (dialog == mPresentation) {
                        mPresentation = null;
                        updateContents();
                    }
                }
            };

    private final static class DemoPresentation extends Presentation {
        private GLSurfaceView mSurfaceView;

        public DemoPresentation(Context context, Display display) {
            super(context, display);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.presentation_with_media_router_content);
            mSurfaceView = (GLSurfaceView)findViewById(R.id.surface_view);
            //加载并渲染长方体模型
            mSurfaceView.setRenderer(new CubeRenderer(false));
        }

        public GLSurfaceView getSurfaceView() {
            return mSurfaceView;
        }
    }
}
