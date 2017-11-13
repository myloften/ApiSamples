package com.loften.android.api.app;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ListActivity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.pdf.PdfDocument;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.print.pdf.PrintedPdfDocument;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.loften.android.api.R;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 打印自定义文档
 *
 * 为了能够创建自定义打印文档，我们需要构建和打印框架可以相互通信的组件，调整打印参数，绘制页面元素并管理多个页面的打印。
 */
public class PrintCustomContent extends ListActivity {

    private static final int MILS_IN_INCH = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new MotoGpStatAdapter(loadMotoGpStats(), getLayoutInflater()));
        print();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void print(){
        PrintManager printManager = (PrintManager) getSystemService(
                Context.PRINT_SERVICE);

        /**
         * print函数第二个参数为继承了抽象类PrintDocumentAdapter 的适配器类，第三个参数为 PrintAttributes对象，
         * 可以用来设置一些打印时的属性
         * 打印适配器与Android系统的打印框架进行交互，处理打印的生命周期方法。打印过程主要有以下生命周期方法：
         * onStart():当打印过程开始的时候调用；
         * onLayout():当用户更改打印设置导致打印结果改变时调用，如更改纸张尺寸，纸张方向等；
         * onWrite():当将要打印的结果写入到文件中时调用，该方法在每次onLayout（）调用后会调用一次或多次；
         * onFinish()：当打印过程结束时调用。
         * 注：关键方法有onLayout()和onWrite()，这些方法默认都是在主线程中调用，因此如果打印过程比较耗时，应该在后台线程中进行。
         */
        printManager.print("MotoGP stats",
                new PrintDocumentAdapter() {
                    private int mRenderPageWidth;
                    private int mRenderPageHeight;

                    private PrintAttributes mPrintAttributes;
                    private PrintDocumentInfo mDocumentInfo;
                    private Context mPrintContext;

                    /**
                     * 在onLayout()方法中，你的适配器需要告诉系统框架文本类型，总页数等信息
                     * 注：onLayout（）方法的执行有完成，取消，和失败三种结果，你必须通过调用
                     * PrintDocumentAdapter.LayoutResultCallback类
                     * 的适当回调方法表明执行结果， onLayoutFinished()方法的布尔型参数指示布局内容是否已经改变。
                     *
                     * onLayout()方法的主要任务就是计算在新的设置下，需要打印的页数，如通过打印的方向决定页数
                     */
                    @SuppressLint("StaticFieldLeak")
                    @Override
                    public void onLayout(final PrintAttributes oldAttributes,
                                         final PrintAttributes newAttributes,
                                         final CancellationSignal cancellationSignal,
                                         final LayoutResultCallback callback,
                                         final Bundle metadata) {

                        //如果我们取消，则不做任何工作
                        if(cancellationSignal.isCanceled()){
                            callback.onLayoutCancelled();
                            return;
                        }

                        //如果打印属性改变，将会改变布局，这样我们就得重新布局
                        boolean layoutNeeded = false;

                        final int density = Math.max(newAttributes.getResolution().getHorizontalDpi(),
                                newAttributes.getResolution().getVerticalDpi());

                        // Note that we are using the PrintedPdfDocument class which creates
                        // a PDF generating canvas whose size is in points (1/72") not screen
                        // pixels. Hence, this canvas is pretty small compared to the screen.
                        // The recommended way is to layout the content in the desired size,
                        // in this case as large as the printer can do, and set a translation
                        // to the PDF canvas to shrink in. Note that PDF is a vector format
                        // and you will not lose data during the transformation.

                        // The content width is equal to the page width minus the margins times
                        // the horizontal printer density. This way we get the maximal number
                        // of pixels the printer can put horizontally.
                        final int marginLeft = (int) (density * (float) newAttributes.getMinMargins()
                                .getLeftMils() / MILS_IN_INCH);
                        final int marginRight = (int) (density * (float) newAttributes.getMinMargins()
                                .getRightMils() / MILS_IN_INCH);
                        final int contentWidth = (int) (density * (float) newAttributes.getMediaSize()
                                .getWidthMils() / MILS_IN_INCH) - marginLeft - marginRight;
                        if (mRenderPageWidth != contentWidth) {
                            mRenderPageWidth = contentWidth;
                            layoutNeeded = true;
                        }

                        // The content height is equal to the page height minus the margins times
                        // the vertical printer resolution. This way we get the maximal number
                        // of pixels the printer can put vertically.
                        final int marginTop = (int) (density * (float) newAttributes.getMinMargins()
                                .getTopMils() / MILS_IN_INCH);
                        final int marginBottom = (int) (density * (float) newAttributes.getMinMargins()
                                .getBottomMils() / MILS_IN_INCH);
                        final int contentHeight = (int) (density * (float) newAttributes.getMediaSize()
                                .getHeightMils() / MILS_IN_INCH) - marginTop - marginBottom;
                        if (mRenderPageHeight != contentHeight) {
                            mRenderPageHeight = contentHeight;
                            layoutNeeded = true;
                        }

                        // Create a context for resources at printer density. We will
                        // be inflating views to render them and would like them to use
                        // resources for a density the printer supports.
                        if (mPrintContext == null || mPrintContext.getResources()
                                .getConfiguration().densityDpi != density) {
                            Configuration configuration = new Configuration();
                            configuration.densityDpi = density;
                            mPrintContext = createConfigurationContext(
                                    configuration);
                            mPrintContext.setTheme(android.R.style.Theme_Holo_Light);
                        }

                        // If no layout is needed that we did a layout at least once and
                        // the document info is not null, also the second argument is false
                        // to notify the system that the content did not change. This is
                        // important as if the system has some pages and the content didn't
                        // change the system will ask, the application to write them again.
                        if (!layoutNeeded) {
                            callback.onLayoutFinished(mDocumentInfo, false);
                            return;
                        }

                        // For demonstration purposes we will do the layout off the main
                        // thread but for small content sizes like this one it is OK to do
                        // that on the main thread.

                        // Store the data as we will layout off the main thread.
                        final List<MotoGpStatItem> items = ((MotoGpStatAdapter)
                                getListAdapter()).cloneItems();

                        new AsyncTask<Void, Void, PrintDocumentInfo>() {
                            @Override
                            protected void onPreExecute() {
                                // First register for cancellation requests.
                                cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
                                    @Override
                                    public void onCancel() {
                                        cancel(true);
                                    }
                                });
                                // Stash the attributes as we will need them for rendering.
                                mPrintAttributes = newAttributes;
                            }

                            @Override
                            protected PrintDocumentInfo doInBackground(Void... params) {
                                try {
                                    // Create an adapter with the stats and an inflater
                                    // to load resources for the printer density.
                                    MotoGpStatAdapter adapter = new MotoGpStatAdapter(items,
                                            (LayoutInflater) mPrintContext.getSystemService(
                                                    Context.LAYOUT_INFLATER_SERVICE));

                                    int currentPage = 0;
                                    int pageContentHeight = 0;
                                    int viewType = -1;
                                    View view = null;
                                    LinearLayout dummyParent = new LinearLayout(mPrintContext);
                                    dummyParent.setOrientation(LinearLayout.VERTICAL);

                                    final int itemCount = adapter.getCount();
                                    for (int i = 0; i < itemCount; i++) {
                                        // Be nice and respond to cancellation.
                                        if (isCancelled()) {
                                            return null;
                                        }

                                        // Get the next view.
                                        final int nextViewType = adapter.getItemViewType(i);
                                        if (viewType == nextViewType) {
                                            view = adapter.getView(i, view, dummyParent);
                                        } else {
                                            view = adapter.getView(i, null, dummyParent);
                                        }
                                        viewType = nextViewType;

                                        // Measure the next view
                                        measureView(view);

                                        // Add the height but if the view crosses the page
                                        // boundary we will put it to the next page.
                                        pageContentHeight += view.getMeasuredHeight();
                                        if (pageContentHeight > mRenderPageHeight) {
                                            pageContentHeight = view.getMeasuredHeight();
                                            currentPage++;
                                        }
                                    }

                                    // Create a document info describing the result.
                                    PrintDocumentInfo info = new PrintDocumentInfo
                                            .Builder("MotoGP_stats.pdf")
                                            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                                            .setPageCount(currentPage + 1)
                                            .build();

                                    // We completed the layout as a result of print attributes
                                    // change. Hence, if we are here the content changed for
                                    // sure which is why we pass true as the second argument.
                                    callback.onLayoutFinished(info, true);
                                    return info;
                                } catch (Exception e) {
                                    // An unexpected error, report that we failed and
                                    // one may pass in a human readable localized text
                                    // for what the error is if known.
                                    callback.onLayoutFailed(null);
                                    throw new RuntimeException(e);
                                }
                            }

                            @Override
                            protected void onPostExecute(PrintDocumentInfo result) {
                                // Update the cached info to send it over if the next
                                // layout pass does not result in a content change.
                                mDocumentInfo = result;
                            }

                            @Override
                            protected void onCancelled(PrintDocumentInfo result) {
                                // Task was cancelled, report that.
                                callback.onLayoutCancelled();
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);

                    }

                    /**
                     * 当需要将打印结果输出到文件中时，系统会调用onWrite（）方法，该方法的参数指明要打印的页
                     * 以及结果写入的文件，你的方法实现需要将页面的内容写入到一个多页面的PDF文档中，当这个过程完成时，
                     * 需要调用onWriteFinished() 方法
                     */
                    @SuppressLint("StaticFieldLeak")
                    @Override
                    public void onWrite(final PageRange[] pages,
                                        final ParcelFileDescriptor destination,
                                        final CancellationSignal cancellationSignal,
                                        final WriteResultCallback callback) {
                        if (cancellationSignal.isCanceled()) {
                            callback.onWriteCancelled();
                            return;
                        }

                        // Store the data as we will layout off the main thread.
                        final List<MotoGpStatItem> items = ((MotoGpStatAdapter)
                                getListAdapter()).cloneItems();

                        new AsyncTask<Void, Void, Void>() {
                            private final SparseIntArray mWrittenPages = new SparseIntArray();
                            private final PrintedPdfDocument mPdfDocument = new PrintedPdfDocument(
                                    PrintCustomContent.this, mPrintAttributes);

                            @Override
                            protected void onPreExecute() {

                                cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
                                    @Override
                                    public void onCancel() {
                                        cancel(true);
                                    }
                                });
                            }

                            /**
                             * PrintedPdfDocument类使用Canvas对象来在PDF页面上绘制元素
                             */
                            @Override
                            protected Void doInBackground(Void... params) {
                                // Go over all the pages and write only the requested ones.
                                // Create an adapter with the stats and an inflater
                                // to load resources for the printer density.
                                MotoGpStatAdapter adapter = new MotoGpStatAdapter(items,
                                        (LayoutInflater) mPrintContext.getSystemService(
                                                Context.LAYOUT_INFLATER_SERVICE));

                                int currentPage = -1;
                                int pageContentHeight = 0;
                                int viewType = -1;
                                View view = null;
                                PdfDocument.Page page = null;
                                LinearLayout dummyParent = new LinearLayout(mPrintContext);
                                dummyParent.setOrientation(LinearLayout.VERTICAL);

                                // The content is laid out and rendered in screen pixels with
                                // the width and height of the paper size times the print
                                // density but the PDF canvas size is in points which are 1/72",
                                // so we will scale down the content.
                                final float scale =  Math.min(
                                        (float) mPdfDocument.getPageContentRect().width()
                                                / mRenderPageWidth,
                                        (float) mPdfDocument.getPageContentRect().height()
                                                / mRenderPageHeight);

                                final int itemCount = adapter.getCount();
                                for (int i = 0; i < itemCount; i++) {
                                    // Be nice and respond to cancellation.
                                    if (isCancelled()) {
                                        return null;
                                    }

                                    // Get the next view.
                                    final int nextViewType = adapter.getItemViewType(i);
                                    if (viewType == nextViewType) {
                                        view = adapter.getView(i, view, dummyParent);
                                    } else {
                                        view = adapter.getView(i, null, dummyParent);
                                    }
                                    viewType = nextViewType;

                                    // Measure the next view
                                    measureView(view);

                                    // Add the height but if the view crosses the page
                                    // boundary we will put it to the next one.
                                    pageContentHeight += view.getMeasuredHeight();
                                    if (currentPage < 0 || pageContentHeight > mRenderPageHeight) {
                                        pageContentHeight = view.getMeasuredHeight();
                                        currentPage++;
                                        // Done with the current page - finish it.
                                        if (page != null) {
                                            mPdfDocument.finishPage(page);
                                        }
                                        // If the page is requested, render it.
                                        if (containsPage(pages, currentPage)) {
                                            page = mPdfDocument.startPage(currentPage);
                                            page.getCanvas().scale(scale, scale);
                                            // Keep track which pages are written.
                                            mWrittenPages.append(mWrittenPages.size(), currentPage);
                                        } else {
                                            page = null;
                                        }
                                    }

                                    // If the current view is on a requested page, render it.
                                    if (page != null) {
                                        // Layout an render the content.
                                        view.layout(0, 0, view.getMeasuredWidth(),
                                                view.getMeasuredHeight());
                                        view.draw(page.getCanvas());
                                        // Move the canvas for the next view.
                                        page.getCanvas().translate(0, view.getHeight());
                                    }
                                }

                                // Done with the last page.
                                if (page != null) {
                                    mPdfDocument.finishPage(page);
                                }

                                // Write the data and return success or failure.
                                try {
                                    mPdfDocument.writeTo(new FileOutputStream(
                                            destination.getFileDescriptor()));
                                    // Compute which page ranges were written based on
                                    // the bookkeeping we maintained.
                                    PageRange[] pageRanges = computeWrittenPageRanges(mWrittenPages);
                                    callback.onWriteFinished(pageRanges);
                                } catch (IOException ioe) {
                                    callback.onWriteFailed(null);
                                } finally {
                                    mPdfDocument.close();
                                }

                                return null;
                            }

                            @Override
                            protected void onCancelled(Void result) {
                                // Task was cancelled, report that.
                                callback.onWriteCancelled();
                                mPdfDocument.close();
                            }
                        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
                    }

                    private void measureView(View view) {
                        final int widthMeasureSpec = ViewGroup.getChildMeasureSpec(
                                View.MeasureSpec.makeMeasureSpec(mRenderPageWidth,
                                        View.MeasureSpec.EXACTLY), 0, view.getLayoutParams().width);
                        final int heightMeasureSpec = ViewGroup.getChildMeasureSpec(
                                View.MeasureSpec.makeMeasureSpec(mRenderPageHeight,
                                        View.MeasureSpec.EXACTLY), 0, view.getLayoutParams().height);
                        view.measure(widthMeasureSpec, heightMeasureSpec);
                    }

                    //计算打印文档的页数，并把它作为打印参数交给打印机，打印页数是根据打印方向确定的。
                    private PageRange[] computeWrittenPageRanges(SparseIntArray writtenPages) {
                        List<PageRange> pageRanges = new ArrayList<PageRange>();

                        int start = -1;
                        int end = -1;
                        final int writtenPageCount = writtenPages.size();
                        for (int i = 0; i < writtenPageCount; i++) {
                            if (start < 0) {
                                start = writtenPages.valueAt(i);
                            }
                            int oldEnd = end = start;
                            while (i < writtenPageCount && (end - oldEnd) <= 1) {
                                oldEnd = end;
                                end = writtenPages.valueAt(i);
                                i++;
                            }
                            PageRange pageRange = new PageRange(start, end);
                            pageRanges.add(pageRange);
                            start = end = -1;
                        }

                        PageRange[] pageRangesArray = new PageRange[pageRanges.size()];
                        pageRanges.toArray(pageRangesArray);
                        return pageRangesArray;
                    }

                    private boolean containsPage(PageRange[] pageRanges, int page) {
                        final int pageRangeCount = pageRanges.length;
                        for (int i = 0; i < pageRangeCount; i++) {
                            if (pageRanges[i].getStart() <= page
                                    && pageRanges[i].getEnd() >= page) {
                                return true;
                            }
                        }
                        return false;
                    }

                }, null);

    }

    private static final class MotoGpStatItem{
        String year;
        String champion;
        String constructor;
    }

    private List<MotoGpStatItem> loadMotoGpStats(){
        String[] years = getResources().getStringArray(R.array.motogp_years);
        String[] champions = getResources().getStringArray(R.array.motogp_champions);
        String[] constructors = getResources().getStringArray(R.array.motogp_constructors);

        List<MotoGpStatItem> items = new ArrayList<>();

        final int itemCount = years.length;
        for (int i = 0; i < itemCount; i++) {
            MotoGpStatItem item = new MotoGpStatItem();
            item.year = years[i];
            item.champion = champions[i];
            item.constructor = constructors[i];
            items.add(item);
        }

        return items;
    }

    private class MotoGpStatAdapter extends BaseAdapter{
        private final List<MotoGpStatItem> mItems;
        private final LayoutInflater mInflater;

        public MotoGpStatAdapter(List<MotoGpStatItem> items, LayoutInflater inflater) {
            mItems = items;
            mInflater = inflater;
        }

        public List<MotoGpStatItem> cloneItems(){
            return new ArrayList<>(mItems);
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int i) {
            return mItems.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = mInflater.inflate(R.layout.motogp_stat_item, parent, false);
            }

            MotoGpStatItem item = (MotoGpStatItem)getItem(position);

            TextView yearView = (TextView) convertView.findViewById(R.id.year);
            yearView.setText(item.year);

            TextView championView = (TextView) convertView.findViewById(R.id.champion);
            championView.setText(item.champion);

            TextView constructorView = (TextView) convertView.findViewById(R.id.constructor);
            constructorView.setText(item.constructor);

            return convertView;
        }
    }
}
