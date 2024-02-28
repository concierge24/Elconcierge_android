package com.trava.user.ui.home.comfirmbooking.payment;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.ReplacementSpan;

import androidx.annotation.NonNull;

public class ExpiryDateTextWatcher implements TextWatcher {
    private int maxLength = 4;
    private boolean internalStopFormatFlag;

    public ExpiryDateTextWatcher() {}

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (internalStopFormatFlag) {
            return;
        }
        internalStopFormatFlag = true;
        formatExpiryDate(s, maxLength);
        internalStopFormatFlag = false;
    }

    public static void formatExpiryDate(@NonNull Editable expiryDate, int maxLength) {
        int textLength = expiryDate.length();
        // first remove any previous span
        SlashSpan[] spans = expiryDate.getSpans(0, expiryDate.length(), SlashSpan.class);
        for (int i = 0; i < spans.length; ++i) {
            expiryDate.removeSpan(spans[i]);
        }
        // then truncate to max length
        if (maxLength > 0 && textLength > maxLength - 1) {
            expiryDate.replace(maxLength, textLength, "");
            --textLength;
        }
        // finally add margin spans
        for (int i = 1; i <= ((textLength - 1) / 2); ++i) {
            int end = i * 2 + 1;
            int start = end - 1;
            SlashSpan marginSPan = new SlashSpan();
            expiryDate.setSpan(marginSPan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public static class SlashSpan extends ReplacementSpan {

        public SlashSpan() {}

        @Override
        public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
            float[] widths = new float[end - start];
            float[] slashWidth = new float[1];
            paint.getTextWidths(text, start, end, widths);
            paint.getTextWidths("/", slashWidth);
            int sum = (int) slashWidth[0];
            for (int i = 0; i < widths.length; ++i) {
                sum += widths[i];
            }
            return sum;
        }

        @Override
        public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
            String xtext = "/" + text.subSequence(start, end);
            canvas.drawText(xtext, 0, xtext.length(), x, y, paint);
        }
    }
}