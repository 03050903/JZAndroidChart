package cn.jingzhuan.lib.chart;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.jingzhuan.lib.chart.component.Highlight;
import cn.jingzhuan.lib.chart.event.HighlightStatusChangeListener;
import cn.jingzhuan.lib.chart.event.OnHighlightListener;
import cn.jingzhuan.lib.chart.renderer.AbstractDataRenderer;
import cn.jingzhuan.lib.chart.renderer.AxisRenderer;

/**
 * Created by Donglua on 17/7/17.
 */

public class BaseChart extends Chart {

    protected AbstractDataRenderer mRenderer;
    private List<AxisRenderer> mAxisRenderers;

    protected Highlight[] mHighlights;
    private HighlightStatusChangeListener mHighlightStatusChangeListener;
    private OnHighlightListener mHighlightListener;

    protected WeakReference<Bitmap> mDrawBitmap;
    protected Canvas mBitmapCanvas;
    protected Bitmap.Config mBitmapConfig = Bitmap.Config.ARGB_8888;

    public BaseChart(Context context) {
        super(context);
    }

    public BaseChart(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public BaseChart(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void initChart() {

        mAxisRenderers = new ArrayList<>(4);

        mAxisRenderers.add(new AxisRenderer(this, mAxisTop));
        mAxisRenderers.add(new AxisRenderer(this, mAxisBottom));
        mAxisRenderers.add(new AxisRenderer(this, mAxisLeft));
        mAxisRenderers.add(new AxisRenderer(this, mAxisRight));
    }

    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mBitmapCanvas != null) {
            mBitmapCanvas.setBitmap(null);
            mBitmapCanvas = null;
        }
        if (mDrawBitmap != null) {
            if (mDrawBitmap.get() != null) mDrawBitmap.get().recycle();
            mDrawBitmap.clear();
            mDrawBitmap = null;
        }
    }

    @Override protected void createBitmapCache(Canvas canvas) {
        int width = getContentRect().width() + getContentRect().left;
        int height = getContentRect().height();

        if (mDrawBitmap == null
            || (mDrawBitmap.get().getWidth() != width)
            || (mDrawBitmap.get().getHeight() != height)) {

            if (width > 0 && height > 0) {
                mDrawBitmap = new WeakReference<>(Bitmap.createBitmap(width, height, mBitmapConfig));
                mBitmapCanvas = new Canvas(mDrawBitmap.get());
            } else
                return;
        }

        mDrawBitmap.get().eraseColor(Color.TRANSPARENT);
    }

    @Override protected Bitmap getDrawBitmap() {
        return mDrawBitmap.get();
    }

    @Override protected Paint getRenderPaint() {
        return mRenderer.getRenderPaint();
    }

    @Override
    public Canvas getBitmapCanvas() {
        return mBitmapCanvas;
    }

    @Override
    protected void drawAxis(Canvas canvas) {
        for (AxisRenderer axisRenderer : mAxisRenderers) {
            axisRenderer.renderer(canvas);
        }
    }

    @Override protected void drawGridLine(Canvas canvas) {
        for (AxisRenderer axisRenderer : mAxisRenderers) {
            axisRenderer.drawGridLines(canvas);
        }
    }

    @Override
    protected void drawLabels(Canvas canvas) {
        for (AxisRenderer axisRenderer : mAxisRenderers) {
            axisRenderer.drawLabels(canvas);
        }
    }

    @Override
    protected void onTouchPoint(float x, float y) {
        for (OnTouchPointChangeListener touchPointChangeListener : mTouchPointChangeListeners) {
            touchPointChangeListener.touch(x, y);
        }
    }

    @Override
    public void highlightValue(Highlight highlight) {

        if (highlight == null) return;

        final Highlight[] highlights = new Highlight[] { highlight };

        if (mHighlightStatusChangeListener != null) {
            mHighlightStatusChangeListener.onHighlightShow(highlights);
        }

        if (mHighlightListener != null) {
            mHighlightListener.highlight(highlights);
        }

        mHighlights = highlights;

        invalidate();
    }

    @Override
    public void cleanHighlight() {
        mHighlights = null;

        if (mHighlightStatusChangeListener != null)
            mHighlightStatusChangeListener.onHighlightHide();

        invalidate();
    }

    public void setRenderer(AbstractDataRenderer renderer) {
        this.mRenderer = renderer;
    }


    @Override
    protected final void render(final Canvas canvas) {
        if (mRenderer != null) {
            mRenderer.renderer(canvas);
        }
        drawEdgeEffectsUnclipped(canvas);
        renderHighlighted(canvas);
    }

    public void renderHighlighted(Canvas canvas) {
        if (mRenderer != null && getHighlights() != null) {
            mRenderer.renderHighlighted(canvas, getHighlights());
        }
    }

    public void setHighlightColor(int color) {
        mRenderer.setHighlightColor(color);
    }

    public int getHighlightColor() {
        return mRenderer.getHighlightColor();
    }

    public Highlight[] getHighlights() {
        return mHighlights;
    }

    public void setHighlights(Highlight[] highlights) {

        this.mHighlights = highlights;
    }

    public void setOnHighlightStatusChangeListener(HighlightStatusChangeListener mHighlightStatusChangeListener) {
        this.mHighlightStatusChangeListener = mHighlightStatusChangeListener;
    }

    public HighlightStatusChangeListener getOnHighlightStatusChangeListener() {
        return mHighlightStatusChangeListener;
    }

    public void setOnHighlightListener(OnHighlightListener highlightListener) {
        this.mHighlightListener = highlightListener;
    }

    public void enableHighlightDashPathEffect(float intervals[], float phase) {
        this.mRenderer.enableDashPathEffect(intervals, phase);
    }

    public void setMinVisibleEntryCount(int minVisibleEntryCount) {
        mRenderer.setMinVisibleEntryCount(minVisibleEntryCount);
    }

    public void setMaxVisibleEntryCount(int maxVisibleEntryCount) {
        mRenderer.setMaxVisibleEntryCount(maxVisibleEntryCount);
    }

    public void setDefaultVisibleEntryCount(int defaultVisibleEntryCount) {
        mRenderer.setDefaultVisibleEntryCount(defaultVisibleEntryCount);
    }

    @Override protected int getEntryIndexByCoordinate(float x, float y) {
        return mRenderer.getEntryIndexByCoordinate(x, y);
    }

    public void releaseBitmap() {
        if (mBitmapCanvas != null) {
            mBitmapCanvas.setBitmap(null);
            mBitmapCanvas = null;
        }
        if (mDrawBitmap != null) {
            if (mDrawBitmap.get() != null) mDrawBitmap.get().recycle();
            mDrawBitmap.clear();
            mDrawBitmap = null;
        }
    }
}

