package com.example.projectkrs.weather;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Random;

public class WeatherOverlayView extends View {

    public enum WeatherType { NONE, RAIN, SNOW, NIGHT, SUN }

    private class Particle {
        float x, y, speed, length;
    }

    private ArrayList<Particle> particles = new ArrayList<>();
    private Paint paint = new Paint();
    private Random random = new Random();
    private WeatherType weatherType = WeatherType.NONE;

    public WeatherOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setAntiAlias(true);
    }

    public void setWeather(WeatherType type) {
        weatherType = type;
        particles.clear();
        if (weatherType == WeatherType.RAIN) initRain();
        else if (weatherType == WeatherType.SNOW) initSnow();
        invalidate();
    }

    private void initRain() {
        paint.setColor(0x88FFFFFF);
        paint.setStrokeWidth(3f);
        for (int i = 0; i < 120; i++) {
            Particle p = new Particle();
            p.x = random.nextInt(1000);
            p.y = random.nextInt(2000);
            p.speed = 10 + random.nextInt(15);
            p.length = 20 + random.nextInt(20);
            particles.add(p);
        }
    }

    private void initSnow() {
        paint.setColor(0xCCFFFFFF);
        paint.setStrokeWidth(2f);
        for (int i = 0; i < 100; i++) {
            Particle p = new Particle();
            p.x = random.nextInt(1000);
            p.y = random.nextInt(2000);
            p.speed = 3 + random.nextInt(5);
            p.length = 10 + random.nextInt(10);
            particles.add(p);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (weatherType == WeatherType.NONE) return;

        for (Particle p : particles) {
            if (weatherType == WeatherType.RAIN) {
                canvas.drawLine(p.x, p.y, p.x, p.y + p.length, paint);
                p.y += p.speed;
                if (p.y > getHeight()) {
                    p.y = 0;
                    p.x = random.nextInt(getWidth());
                }
            } else if (weatherType == WeatherType.SNOW) {
                canvas.drawCircle(p.x, p.y, p.length / 2, paint);
                p.y += p.speed;
                p.x += (random.nextFloat() - 0.5) * 2;
                if (p.y > getHeight()) p.y = 0;
                if (p.x < 0) p.x = getWidth();
                if (p.x > getWidth()) p.x = 0;
            }
        }

        // Night overlay
        if (weatherType == WeatherType.NIGHT) {
            canvas.drawColor(0x88000044);
        }
        // Sun overlay
        if (weatherType == WeatherType.SUN) {
            canvas.drawColor(0x33FFFF00);
        }

        invalidate();
    }
}