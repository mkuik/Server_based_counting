package dev.kuik.matthijs.serverbasedcounting;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Matthijs on 17/01/16.
 */
public class IconView extends ImageView {

    private Paint paint = new Paint();
    private Point center;

    public IconView(Context context) {
        super(context);
        init();
    }

    public IconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public IconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        center = new Point(w / 2, h / 2);
    }

    private void init() {
        paint.setAntiAlias(true);
    }

    public void setColor(int color) {
        paint.setColor(color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final float radius = Math.min(center.x, center.y);
        canvas.drawCircle(center.x, center.y, radius, paint);
        super.onDraw(canvas);
    }
}
