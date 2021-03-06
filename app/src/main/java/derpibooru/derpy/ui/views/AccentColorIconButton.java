package derpibooru.derpy.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import derpibooru.derpy.R;

public class AccentColorIconButton extends RelativeLayout {
    private TextViewButton mTextViewWithIcon;
    private OnClickListener mButtonListener;

    private boolean mToggleIconTintOnTouch = true;

    public AccentColorIconButton(Context context) {
        super(context);
        mTextViewWithIcon = new TextViewButton(context);
        init();
    }

    public AccentColorIconButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTextViewWithIcon = new TextViewButton(context, attrs);
        init();
    }

    private void init() {
        super.addView(mTextViewWithIcon, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        super.setGravity(Gravity.CENTER);
        super.setClickable(true);
        super.setOnTouchListener(new ButtonTouchListener());
        super.setOnClickListener(new ButtonClickListener());
    }

    @Override
    public void setOnClickListener(OnClickListener listener) {
        mButtonListener = listener;
    }

    public void setText(CharSequence text) {
        mTextViewWithIcon.setText(text);
    }

    public CharSequence getText() {
        return mTextViewWithIcon.getText();
    }

    public void setActive(boolean active) {
        mTextViewWithIcon.setKeepActive(active);
        mTextViewWithIcon.setActive(active);
    }

    public boolean isActive() {
        return mTextViewWithIcon.isActive();
    }

    public void setToggleIconTintOnTouch(boolean toggle) {
        mToggleIconTintOnTouch = toggle;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mTextViewWithIcon.setEnabled(enabled);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return (isEnabled() && super.onTouchEvent(event));
    }

    private class ButtonTouchListener implements OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!mToggleIconTintOnTouch) {
                return false;
            }
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                mTextViewWithIcon.toggleActive(true);
            } else if (event.getActionMasked() == MotionEvent.ACTION_UP
                    || event.getActionMasked() == MotionEvent.ACTION_POINTER_UP
                    || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                mTextViewWithIcon.toggleActive(false);
            }
            return false;
        }
    }

    private class ButtonClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            if (mButtonListener != null) {
                mButtonListener.onClick(v);
            }
        }
    }

    private static class TextViewButton extends TextView {
        private static final int ICON_PADDING_IN_DIP = 5;

        private int mActiveColorResId;
        private boolean mKeepColorFilter;
        private boolean mColorFilterSet;

        TextViewButton(Context context) {
            super(context);
            init(context, null);
        }

        TextViewButton(Context context, AttributeSet attrs) {
            super(context, attrs);
            init(context, attrs);
        }

        private void init(Context context, @Nullable AttributeSet attrs) {
            if (attrs != null) {
                TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AccentColorIconButton);
                try {
                    setButtonText(a.getString(R.styleable.AccentColorIconButton_buttonText));
                    if (a.getDrawable(R.styleable.AccentColorIconButton_buttonIcon) != null) {
                        setIcon(a.getDrawable(R.styleable.AccentColorIconButton_buttonIcon));
                    }
                } finally {
                    a.recycle();
                }
            }
            mActiveColorResId = ContextCompat.getColor(context, R.color.colorAccent);
        }

        public void toggleActive(boolean makeActive) {
            setActive((makeActive && !mColorFilterSet)
                              || (!makeActive && mKeepColorFilter));
        }

        public void setActive(boolean active) {
            if (getIcon() == null) return;
            if (active) {
                mColorFilterSet = true;
                getIcon().setColorFilter(mActiveColorResId, PorterDuff.Mode.SRC_IN);
            } else {
                mColorFilterSet = false;
                getIcon().clearColorFilter();
            }
        }

        public boolean isActive() {
            return mKeepColorFilter;
        }

        public void setButtonText(CharSequence text) {
            super.setText(text);
            if (text.length() > 0) {
                super.setGravity(Gravity.CENTER);
                super.setCompoundDrawablePadding((int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, ICON_PADDING_IN_DIP, getResources().getDisplayMetrics()));
            }
        }

        public void setKeepActive(boolean keepActive) {
            mKeepColorFilter = keepActive;
        }

        @Nullable
        private Drawable getIcon() {
            return super.getCompoundDrawables()[0];
        }

        private void setIcon(Drawable icon) {
            Drawable mutableIcon = icon.mutate();
            mutableIcon.setBounds(0, 0, mutableIcon.getIntrinsicWidth(), mutableIcon.getIntrinsicHeight());
            setCompoundDrawables(mutableIcon, null, null, null);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            return (isEnabled() && super.onTouchEvent(event));
        }
    }
}
