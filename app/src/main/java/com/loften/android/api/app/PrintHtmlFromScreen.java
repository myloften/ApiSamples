package com.loften.android.api.app;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.print.PrintManager;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.loften.android.api.R;

/**
 * WebView对象一般作为Activity布局的一部分，如果应用当前没有使用WebView，我们可以创建一个该类的实例，以便进行打印，步骤如下：

 * 在HTML资源加载完毕后，创建一个WebViewClient用来启动一个打印任务。
 * 加载HTML资源到WebView对象中。
 *
 * -------------------------------------------------------------------------------------------------
 * 注意：当使用WebView打印文档时，有以下限制：
 * 不能为文档添加页眉和页脚，包括页号。
 * HTML文档的打印选项不包含选择打印的页数范围，例如：对于一个10页的HTMl文档，只打印2到4页是不可以的。
 * 一个WebView的实例只能在同一时间处理一个打印任务。
 * 若一个HTML文档包含CSS打印属性，比如一个landscape属性，这是不被支持的。
 * 不能通过一个HTML文档中的JavaScript脚本来激活打印。
 */
public class PrintHtmlFromScreen extends ToolbarActivity {

    private WebView webView;

    private boolean mDataLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_html_from_screen);
        initView();
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mDataLoaded = true;
                invalidateOptionsMenu();
            }
        });

        webView.loadUrl("file:///android_res/raw/motogp_stats.html");
    }

    private void initView() {
        mToolbar.setTitle(R.string.print_html_from_screen);
        webView = (WebView) findViewById(R.id.web_view);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        if (mDataLoaded) {
            getMenuInflater().inflate(R.menu.print_custom_content, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_print) {
            print();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void print() {
        // Get the print manager.
        PrintManager printManager = (PrintManager) getSystemService(
                Context.PRINT_SERVICE);
        // Pass in the ViewView's document adapter.
        printManager.print("MotoGP stats", webView.createPrintDocumentAdapter(), null);
    }
}
