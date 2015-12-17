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
    private int activeColor;
    private int backgroundColor;
    private int inacitiveColor;

    public int getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(int accentColor) {
        this.accentColor = accentColor;
    }

    private int accentColor;
    private boolean active = false;
    private float scale;
    private int timeout = 800;

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public ThemeBackground(Context context) {
        super(context);
    }

    public ThemeBackground(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ThemeBackground,
                0, 0);

        try {
            activeColor = a.getColor(R.styleable.ThemeBackground_primaryColor, 0);
            backgroundColor = a.getColor(R.styleable.ThemeBackground_secondaryColor, 0);
            scale = a.getFloat(R.styleable.ThemeBackground_scale, 0);
        } finally {
            a.recycle();
        }
    }

    public ThemeBackground(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setScale(float scale) {
        if (scale >= 0 && scale <= 1) {
            this.scale = scale;
            if (scale == 1) {
                backgroundColor = activeColor;
            }
        }
    }

    public void setActiveColor(int color) {
        activeColor = color;
    }

    public void setInacitiveColor(int color) {
        inacitiveColor = color;
    }

    public void setColor(int color) {
        backgroundColor = activeColor;
        activeColor = color;
        scale = 0;
    }

    public void activate(Animator.AnimatorListener listener) {
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
            if (listener != null) animator.addListener(listener);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    setActive(true);
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
        if (isActive()) {
            setActive(false);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
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
                animator.start();
            } else {
                setScale(0);
                invalidate();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (scale <= 0) {
            canvas.drawColor(active ? backgroundColor : inacitiveColor);
        } else if (scale >= 1) {
            canvas.drawColor(activeColor);
        } else {
            final int cx = getWidth() / 2;
            final int cy = getHeight() / 2;
            final float radius = (float) (Math.sqrt(Math.pow(cx, 2) + Math.pow(cy, 2))) * scale;

            canvas.drawColor(active ? backgroundColor : inacitiveColor);
            Paint paint = new Paint();
            paint.setColor(activeColor);
            canvas.drawCircle(cx, cy, radius, paint);
            if (active && activeColor == backgroundColor) {
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2);
                paint.setColor(accentColor);
                paint.setAlpha((int) (90 * (1 - scale)));
                canvas.drawCircle(cx, cy, radius, paint);
            }
        }
    }
}