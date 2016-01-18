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
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class ThemeBackground extends View {
    private final int defaultColor = Color.BLACK;
    private int accentColor = Color.WHITE;
    private int foregroundColor = defaultColor;
    private int backgroundColor = defaultColor;
    private float scale = 0;
    private float ping_scale = 0;
    private int timeout = 1000;
    private Point center;
    private Paint paint = new Paint();
    private boolean active = false;
    private ValueAnimator animator;
    private ValueAnimator ping_animator;

    public boolean isActive() {
        return scale > 0;
    }

    public void setCenter(Point center) {
        this.center = center;
    }

    public boolean isGoingActive() {
        return scale > 0 && scale < 1;
    }

    public ThemeBackground(Context context) {
        super(context);
    }

    public ThemeBackground(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThemeBackground(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setScale(float scale) {
        if (scale >= 0 && scale <= 1) {
            this.scale = scale;
            if (scale == 1) setBackgroundColor(getForegroundColor());
        }
    }

    public void setPingScale(float scale) {
        if (scale >= 0 && scale <= 1) {
            this.ping_scale = scale;
        }
    }

    public void setForegroundColor(int foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getForegroundColor() {
        return foregroundColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public int getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(int accentColor) {
        this.accentColor = accentColor;
    }

    public int getDefaultColor() {
        return defaultColor;
    }

    public void ping() {
        Log.i("theme", "ping");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            if (ping_animator != null && ping_animator.isRunning()) ping_animator.cancel();
            ping_animator = ValueAnimator.ofFloat(0, 1);
            ping_animator.setDuration(timeout * 2);
            ping_animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Float value = (Float) (animation.getAnimatedValue());
                    setPingScale(value);
                    invalidate();
                }
            });
            ping_animator.start();
        }
    }

    public void activate() {
        active = true;
        Log.i("theme", "activate");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            if (animator != null && animator.isRunning()) animator.cancel();
            animator = ValueAnimator.ofFloat(0, 1);
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
        } else {
            setScale(1);
            invalidate();
        }
    }

    public void deactivate() {
        active = false;
        Log.i("theme", "deactivate");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            if (animator != null && animator.isRunning()) animator.cancel();
            animator = ValueAnimator.ofFloat(1, 0);
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
        } else {
            setScale(0);
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setAntiAlias(true);
        if (center == null) center = new Point(getWidth() / 2, getHeight() / 2);

        if (scale == 0) {
            canvas.drawColor(active ? getBackgroundColor() : getDefaultColor());
        } else if (scale == 1) {
            canvas.drawColor(getForegroundColor());
        } else {
            canvas.drawColor(isActive() ? getBackgroundColor() : getDefaultColor());
            final float radius = (float) ((Math.sqrt(Math.pow(Math.max(center.x, getWidth() - center.x), 2)
                    + Math.pow(Math.max(center.y, getHeight() - center.y), 2))) * (scale));
            paint.setAlpha(255);
            paint.setColor(getForegroundColor());
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(center.x, center.y, radius, paint);
            return;
        }
        
        if (ping_scale != 0 && ping_scale != 1) {
            final float radius = (float) ((Math.sqrt(Math.pow(Math.max(center.x, getWidth() - center.x), 2)
                    + Math.pow(Math.max(center.y, getHeight() - center.y), 2))) * (ping_scale));
            final int alpha = (int) (255 - 255 * ping_scale);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(1);
            paint.setColor(getAccentColor());
            paint.setAlpha(alpha);
            canvas.drawCircle(center.x, center.y, radius, paint);
        }
    }
}