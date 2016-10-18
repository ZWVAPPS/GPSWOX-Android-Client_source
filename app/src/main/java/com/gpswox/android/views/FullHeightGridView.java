package com.gpswox.android.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.GridView;

public class FullHeightGridView extends GridView
{
    public FullHeightGridView(Context context)
    {
        super(context);
    }

    public FullHeightGridView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public FullHeightGridView(Context context, AttributeSet attrs,
                              int defStyle)
    {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
		// Calculate entire height by providing a very large height hint.
		// View.MEASURED_SIZE_MASK represents the largest height possible.
		int expandSpec = MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK,
		        MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
		
		ViewGroup.LayoutParams params = getLayoutParams();
		params.height = getMeasuredHeight();
    }
}