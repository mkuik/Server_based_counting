package dev.kuik.matthijs.serverbasedcounting;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.widget.Button;

/**
 * Created by Matthijs Kuik on 23-12-2015.
 */
public class ThemeButton extends Button {

    private float scale = 0;
    private int color = Color.BLACK;
    private int linewidth = 3;
    private int timeout = 600;
    private Point pointOfCircleOrigin;

    public void setColor(int color) {
        this.color = color;
    }

    public void setPointOfCircleOrigin(Point pointOfCircleOrigin) {
        this.pointOfCircleOrigin = pointOfCircleOrigin;
    }

    public ThemeButton(Context context) {
        super(context);
        init();
    }

    public ThemeButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ThemeButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void init() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        pointOfCircleOrigin = new Point((int)event.getX(), (int)event.getY());
        startRippleAnimation();
        return super.onTouchEvent(event);
    }

    public void startRippleAnimation() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
            animator.setDuration(timeout);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Float value = (Float) (animation.getAnimatedValue());
                    setScale(value);
                    invalidate();
                }
            });
            animator.start();
        }
    }

    public Pair<Point, Point> getIntersection(final int c, final int p, final int q, final float r) {
        final float m = 0;

        final double A = (Math.pow(m, 2) + 1);
        final double B = 2 * (m * c - m * q - p);
        final double C = (Math.pow(q, 2) - Math.pow(r, 2) + Math.pow(p, 2) - (2 * c * q) + Math.pow(c, 2));
        final double D = Math.pow(B, 2) - 4 * A * C;

        Point x1 = null, x2 = null;
        if (D == 0) {
            x1 = new Point((int) (((B * -1) - Math.sqrt(D)) / (2 * A)), c);
        } else if (D < 0) {

        } else {
            x1 = new Point((int) (((B * -1) - Math.sqrt(D)) / (2 * A)), c);
            x2 = new Point((int) (((B * -1) + Math.sqrt(D)) / (2 * A)), c);
        }

        return new Pair<>(x1, x2);
    };


    @Override
    protected void onDraw(Canvas canvas) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            if (pointOfCircleOrigin == null)
                pointOfCircleOrigin = new Point(getWidth() / 2, getHeight() / 2);

            if (scale > 0) {
                final int cx = pointOfCircleOrigin.x;
                final int cy = pointOfCircleOrigin.y;
                final float radius = (float) ((Math.sqrt(Math.pow(Math.max(cx, getWidth() - cx), 2)
                        + Math.pow(Math.max(cy, getHeight() - cy), 2))) * (scale));
                final int alpha = (int) (100 - Math.pow(10 * scale, 2));
                final int x0 = linewidth / 2;
                final int x1 = getWidth() - linewidth / 2;
                final int y0 = x0;
                final int y1 = getHeight() - linewidth / 2;

                Paint paint = new Paint();
                paint.setColor(color);
                paint.setAlpha(alpha);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(linewidth);
                canvas.drawCircle(cx, cy, radius, paint);

                final Pair<Point, Point> lineBottom = getIntersection(y1, cx, cy, radius);
                final Pair<Point, Point> lineTop = getIntersection(y0, cx, cy, radius);
                final Pair<Point, Point> lineLeft = getIntersection(x1, cy, cx, radius);
                final Pair<Point, Point> lineRight = getIntersection(x0, cy, cx, radius);
                if (lineBottom.second != null)
                    canvas.drawLine(lineBottom.first.x, lineBottom.first.y, lineBottom.second.x, lineBottom.second.y, paint);
                if (lineTop.second != null)
                    canvas.drawLine(lineTop.first.x, lineTop.first.y, lineTop.second.x, lineTop.second.y, paint);
                if (lineLeft.second != null)
                    canvas.drawLine(lineLeft.first.y, lineLeft.first.x, lineLeft.second.y, lineLeft.second.x, paint);
                if (lineRight.second != null)
                    canvas.drawLine(lineRight.first.y, lineRight.first.x, lineRight.second.y, lineRight.second.x, paint);
                Log.i("intersection", String.format("scale:%.2f alpha:%d radius:%.2f", scale, paint.getAlpha(), radius));
            }
        }
        super.onDraw(canvas);
    }

    public void setScale(float scale) {
        if (scale >= 0 && scale <= 1) {
            this.scale = scale;
        }
    }
}
