package com.example.projectkrs.weather;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class RainView extends View {

    private class Drop {
        float x, y, speed, length;
    }

    private ArrayList<Drop> drops = new ArrayList<>();
    private Paint paint = new Paint();
    private Random random = new Random();

    public RainView(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint.setColor(0x88FFFFFF);
        paint.setStrokeWidth(3f);

        for (int i = 0; i < getInitialDropCount(); i++) {
            Drop d = new Drop();
            d.x = random.nextInt(1000);
            d.y = random.nextInt(2000);
            d.speed = 10 + random.nextInt(15);
            d.length = 20 + random.nextInt(20);
            drops.add(d);
        }
    }


    static int getInitialDropCount() {
        return 120;
    }

    static boolean shouldResetDrop(float currentY, int viewHeight) {
        return currentY > viewHeight;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (Drop d : drops) {
            canvas.drawLine(d.x, d.y, d.x, d.y + d.length, paint);
            d.y += d.speed;
            if (shouldResetDrop(d.y, getHeight())) {
                d.y = 0;
                d.x = random.nextInt(getWidth());
            }
        }
        invalidate();
    }
}