package com.kilogramm.mattermost.tools;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Parcel;
import android.text.Layout;
import android.text.style.QuoteSpan;

/**
 * Created by Evgeny on 15.09.2016.
 */
public class HrSpannable extends QuoteSpan {
    private final int mColor;

    /**
     * Constructor
     */
    public HrSpannable() {
        super();
        mColor = 0xff0000ff;
    }

    /**
     * Constructor
     *
     * @param color {@link QuoteSpan}
     */
    public HrSpannable(int color) {
        super(color);
        mColor = color;
    }

    /**
     * Constructor
     *
     * @param src {@link QuoteSpan}
     */
    public HrSpannable(Parcel src) {
        super(src);
        mColor = src.readInt();
    }

    @Override
    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir,
                                  int top, int baseline, int bottom,
                                  CharSequence text, int start, int end, boolean first, Layout layout) {
        Paint.Style style = p.getStyle();
        int color = p.getColor();

        p.setStyle(Paint.Style.FILL);
        p.setColor(mColor);

        int height = bottom - top;
        int width = layout.getWidth();

        RectF rectF = new RectF(x, top + height * 2 / 5, x + width, bottom - height * 2 / 5);
        c.drawRoundRect(rectF, height / 2, height / 2, p);

        p.setStyle(style);
        p.setColor(color);
    }
}
