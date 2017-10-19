package ru.trinitydigital.textselecting;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ReplacementSpan;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

public class WordAnswerView extends android.support.v7.widget.AppCompatTextView {
    // тут будем хранить исходную строку
    private final String originalText;

    // это число определяет, сколько пикселей отведено на трекинг (расстояние между буквами)
    private float tracking = convertDpToPx(16);
    // цвет выделения
    private int selectionColor = Color.parseColor("#5591F6");

    // специальное значение для отсуттсвия выделения
    private static final int NO_SELECTION = -1;
    // начало и конец выделения (индексы символов в строке)
    private int selectionBegin = NO_SELECTION, selectionEnd = NO_SELECTION;

    // та самая штука, которая отвечает за отрисовку трекинга и выделения
    private SelectionTrackingSpan selectionTrackingSpan = new SelectionTrackingSpan();

    // понадобится позже, чтобы определять, на какую букву был клик
    private int baseWidth;

    public WordAnswerView(Context context, CharSequence text, int textSizeDp) {
        super(context);
        // запоминаем текст
        originalText = text.toString();
        setText(originalText);

        setTextSize(convertDpToPx(textSizeDp));
        setTextColor(Color.BLACK);

//        setBackgroundColor(Color.GRAY);
//        setWidth(200);
//        setHeight(100);

        // это нужно для того, чтобы на каждую букву приходилась одинаковая ширина,
        // так будет гораздо удобней отрисовывать морфемы
        setTypeface(Typeface.MONOSPACE);
        setPadding((int) tracking, 0, (int) tracking, 0);

        // на всю строку устанавливаем наш спан, который будет отвечать за форматирование
        SpannableString s = new SpannableString(originalText);
        s.setSpan(selectionTrackingSpan, 0, originalText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        setText(s);

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        // считаем индекс символа, на который кликнули
                        int index = (int) (event.getX() / baseWidth);
                        // и устанавливаем границы выделения согласно описанным выше правилам
                        if (selectionBegin == index && selectionEnd == NO_SELECTION) {
                            selectionBegin = NO_SELECTION;
                            selectionEnd = NO_SELECTION;
                            invalidate();
                            break;
                        }
                        if (selectionBegin == NO_SELECTION)
                            selectionBegin = index;
                        else if (selectionEnd == NO_SELECTION) {
                            selectionEnd = index;
                            if (selectionBegin > selectionEnd) {
                                int tmp = selectionBegin;
                                selectionBegin = selectionEnd;
                                selectionEnd = tmp;
                            }
                        } else {
                            selectionBegin = index;
                            selectionEnd = NO_SELECTION;
                        }
                        invalidate();
                        break;
                }
                return false;
            }
        });

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        baseWidth = w / originalText.length();
    }

    private int convertDpToPx(int dp) {
        return Math.round(dp * (getContext().getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private class SelectionTrackingSpan extends ReplacementSpan {
        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
            Rect bounds = new Rect();
            paint.getTextBounds(text.toString(), start, end, bounds);

            if (fm != null) {
                fm.ascent = -bounds.height();
                fm.descent = 0;
                fm.top = fm.ascent;
                fm.bottom = 0;
            }

            // размер будет достаточный для того чтобы нарисовать буквы + расстояния между ними
            return (int) (bounds.width() + tracking * (end - start));
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x,
                         int top, int y, int bottom, Paint paint) {
            float dx = x;
            for (int i = start; i < end; i++) {
                // если символ не попадает в выделенную часть, будем рисовать его просто черным
                if (i < selectionBegin || i >= (selectionEnd != NO_SELECTION ? selectionEnd + 1 : selectionBegin + 1))
                    paint.setColor(Color.BLACK);
                else
                    paint.setColor(selectionColor);
                canvas.drawText(text, i, i + 1, dx, y, paint);
                dx += paint.measureText(text, i, i + 1) + tracking;
            }
        }
    }

}
