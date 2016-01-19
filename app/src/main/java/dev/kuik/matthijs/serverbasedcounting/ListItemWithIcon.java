package dev.kuik.matthijs.serverbasedcounting;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * Created by Matthijs on 18/01/16.
 */
public class ListItemWithIcon extends FrameLayout {

    private IconView icon;
    private TextView title;
    private TextView subtitle;

    public ListItemWithIcon(Context context) {
        super(context);
        init();
    }

    public ListItemWithIcon(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ListItemWithIcon(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setIcon(Bitmap bitmap) {
        icon.setImageBitmap(bitmap);
    }

    public void setIconBackground(int color) {
        icon.setColor(color);
    }

    public void setSubtitle(final String subtitle) {
        this.subtitle.setText(subtitle);
    }

    public void setTitle(final String title) {
        this.title.setText(title);
    }

    private void init() {
        inflate(getContext(), R.layout.listitem_2_lines_with_icon, this);
        icon = (IconView) findViewById(R.id.icon);
        title = (TextView) findViewById(R.id.text1);
        subtitle = (TextView) findViewById(R.id.text2);
    }

}
