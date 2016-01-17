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
public class ServerIconView extends ImageView {

    private Paint paint = new Paint();

    public ServerIconView(Context context) {
        super(context);
    }

    public ServerIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ServerIconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setColor(int color) {
        paint.setColor(color);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Point center = new Point(getWidth() / 2, getHeight() / 2);
        final float radius = Math.min(center.x, center.y);
        canvas.drawCircle(center.x, center.y, radius, paint);
        super.onDraw(canvas);
    }
}
