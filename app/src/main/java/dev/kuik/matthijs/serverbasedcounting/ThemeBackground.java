package dev.kuik.matthijs.serverbasedcounting;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
    private float scale;
    private int timeout = 500;
    public enum MODE {
        OFF,
        TURNING_OFF,
        TURNING_ON,
        ON,
    };
    private MODE mode = MODE.OFF;

    public boolean isActive() {
        return mode == MODE.ON || mode == MODE.TURNING_ON;
    }

    public MODE getMode() {
        return mode;
    }

    public boolean isInactive() {
        return mode == MODE.OFF || mode == MODE.TURNING_OFF;
    }

    public boolean isAnimating() { return mode == MODE.TURNING_ON || mode == MODE.TURNING_OFF; }

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

    public void activate(Animator.AnimatorListener listener) {
        if (mode == MODE.TURNING_ON) return;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            if (mode != MODE.ON) mode = MODE.TURNING_ON;
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
            if (listener != null) animator.addListener(listener);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mode = MODE.ON;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();
        } else {
            setScale(1);
            invalidate();
        }
    }

    public void deactivate(Animator.AnimatorListener listener) {
        if (mode == MODE.TURNING_OFF || mode == MODE.OFF) return;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            mode = MODE.TURNING_OFF;
            ValueAnimator animator = ValueAnimator.ofFloat(1, 0);
            animator.setDuration(timeout);
            if (listener != null) animator.addListener(listener);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @TargetApi(Build.VERSION_CODES.HONEYCOMB)
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    Float value = (Float) (animation.getAnimatedValue());
                    setScale(value);
                    invalidate();
                }
            });
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mode = MODE.OFF;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

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
        Paint paint = new Paint();
        if (scale <= 0) {
            canvas.drawColor(isInactive() ? defaultColor : backgroundColor);
        } else if (scale >= 1) {
            canvas.drawColor(foregroundColor);
        } else {
            canvas.drawColor(isActive() ? backgroundColor : defaultColor);
            final int cx = getWidth() / 2;
            final int cy = getHeight() / 2;
            final float radius = (float) (Math.sqrt(Math.pow(cx, 2) + Math.pow(cy, 2))) * scale;
            final int alpha = (int) (100 - 100 * scale);
            paint.setColor(foregroundColor);
            canvas.drawCircle(cx, cy, radius, paint);
            if (backgroundColor == foregroundColor && isActive()) {
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2);
                paint.setColor(accentColor);
                paint.setAlpha(alpha);
                canvas.drawCircle(cx, cy, radius, paint);
            }
        }
    }
}