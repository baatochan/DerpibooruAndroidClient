package derpibooru.derpy.ui.views;

import android.content.Context;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import derpibooru.derpy.R;
import derpibooru.derpy.ui.adapters.ImageBottomBarTabAdapter;
import derpibooru.derpy.ui.adapters.MainActivityTabAdapter;

public class ImageBottomBarView extends FrameLayout {
    private ViewPager mPager;
    private FragmentManager mFragmentManager;

    private static final int[] LAYOUT_BUTTONS = {
            R.id.buttonInfo,
            R.id.buttonComments,
            R.id.buttonFave };

    public ImageBottomBarView(Context context) {
        super(context);
    }

    public ImageBottomBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ImageBottomBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ImageBottomBarView setFragmentManager(FragmentManager fm) {
        mFragmentManager = fm;
        return this;
    }

    /* To be removed */
    public void setInfo(int faves, int comments) {
        init();
        TextView u = (TextView) this.findViewById(R.id.textComments);
        u.setText(Integer.toString(comments));
        TextView d = (TextView) this.findViewById(R.id.textFaves);
        d.setText(Integer.toString(faves));
    }

    public void onButtonTouched(View v) {
        /* deselect other buttons */
        for (int layoutId : LAYOUT_BUTTONS) {
            LinearLayout ll = (LinearLayout) findViewById(layoutId);
            if (!ll.equals(v)) {
                ll.setSelected(false);
            }
        }
    }

    public void onButtonClicked(View v) {
        if (mPager == null) {
            /* set up ViewPager */
            mPager = (ViewPager) findViewById(R.id.bottomTabsPager);
            mPager.setAdapter(new MainActivityTabAdapter(mFragmentManager));
        }

        if (!v.isSelected()) {
            v.setSelected(true);
            /* show ViewPager */
            if (mPager.getVisibility() == View.GONE) {
                mPager.setVisibility(View.VISIBLE);
            }

            /* navigate ViewPager to the corresponding tab */
            switch (v.getId()) {
                case R.id.buttonInfo:
                    mPager.setCurrentItem(ImageBottomBarTabAdapter.ImageBottomBarTabs.ImageInfo.getID(),
                            true);
                    break;
                case R.id.buttonComments:
                    mPager.setCurrentItem(ImageBottomBarTabAdapter.ImageBottomBarTabs.Comments.getID(),
                            true);
                    break;
                case R.id.buttonFave:
                    mPager.setCurrentItem(ImageBottomBarTabAdapter.ImageBottomBarTabs.Faves.getID(),
                            true);
                    break;
            }
        } else {
            v.setSelected(false);
            /* clicking on the active button hides the ViewPager */
            mPager.setVisibility(View.GONE);
        }
    }

    private void init() {
        View view = inflate(getContext(), R.layout.view_image_bottom_bar, null);
        addView(view);

        /* TODO: a cleaner solution */
        for (int layoutId : LAYOUT_BUTTONS) {
            LinearLayout ll = (LinearLayout) findViewById(layoutId);
            ll.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    onButtonTouched(v);
                    return false;
                }
            });
            ll.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onButtonClicked(v);
                }
            });
        }
    }
}
