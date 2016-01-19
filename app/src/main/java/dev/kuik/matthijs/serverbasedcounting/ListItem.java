package dev.kuik.matthijs.serverbasedcounting;

import android.app.LauncherActivity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by Matthijs on 18/01/16.
 */
public class ListItem extends FrameLayout {

    private TextView title;
    private TextView subtitle;

    public ListItem(Context context) {
        super(context);
        init();
    }

    public ListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ListItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    public void setSubtitle(final String subtitle) {
        this.subtitle.setText(subtitle);
    }

    public void setTitle(final String title) {
        this.title.setText(title);
    }

    public TextView getSubtitle() {
        return subtitle;
    }

    public TextView getTitle() {
        return title;
    }

    private void init() {
        inflate(getContext(), R.layout.listitem_2_lines, this);
        title = (TextView) findViewById(R.id.text1);
        subtitle = (TextView) findViewById(R.id.text2);
    }

    private void init(AttributeSet attrs) {
        init();

        TypedArray a = getContext().getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ListItem,
                0, 0);

        try {
            setTitle(a.getString(R.styleable.ListItem_title_text));
            setSubtitle(a.getString(R.styleable.ListItem_subtitle_text));
        } finally {
            a.recycle();
        }
    }
}

