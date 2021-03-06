package derpibooru.derpy.ui.views.htmltextview;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;

import com.google.common.base.Objects;

/**
 * @author http://stackoverflow.com/a/20905824/1726690
 */
class CustomFormattingMovementMethod extends LinkMovementMethod {
    private SpoilerSpan mPressedSpan;

    @Override
    public boolean onTouchEvent(TextView textView, Spannable spannable, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mPressedSpan = getPressedSpan(textView, spannable, event);
            if (mPressedSpan != null) {
                mPressedSpan.unspoiler();
                Selection.setSelection(spannable, spannable.getSpanStart(mPressedSpan),
                                       spannable.getSpanEnd(mPressedSpan));
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            SpoilerSpan touchedSpan = getPressedSpan(textView, spannable, event);
            if (!Objects.equal(touchedSpan, mPressedSpan)) {
                mPressedSpan = null;
                Selection.removeSelection(spannable);
            }
        } else {
            mPressedSpan = null;
            Selection.removeSelection(spannable);
        }
        return super.onTouchEvent(textView, spannable, event);
    }

    private SpoilerSpan getPressedSpan(TextView textView, Spannable spannable, MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();

        x -= textView.getTotalPaddingLeft();
        y -= textView.getTotalPaddingTop();

        x += textView.getScrollX();
        y += textView.getScrollY();

        Layout layout = textView.getLayout();
        int line = layout.getLineForVertical(y);
        int off = layout.getOffsetForHorizontal(line, x);

        SpoilerSpan[] link = spannable.getSpans(off, off, SpoilerSpan.class);
        SpoilerSpan touchedSpan = null;
        if (link.length > 0) {
            touchedSpan = link[0];
        }
        return touchedSpan;
    }
}
