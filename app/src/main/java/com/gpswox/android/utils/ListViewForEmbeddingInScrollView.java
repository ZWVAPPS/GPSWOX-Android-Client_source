package com.gpswox.android.utils;

/**
 * Created by Mantas Liutkevicius on 23/02/2017.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class ListViewForEmbeddingInScrollView extends ListView
{
    public ListViewForEmbeddingInScrollView(Context context) {
        super(context);
    }

    public ListViewForEmbeddingInScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListViewForEmbeddingInScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 4, MeasureSpec.AT_MOST));
    }
}
