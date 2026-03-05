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
        if (hasParticles(weatherType)) {
            if (weatherType == WeatherType.RAIN) initRain();
            else if (weatherType == WeatherType.SNOW) initSnow();
        }
        invalidate();
    }

    private void initRain() {
        paint.setColor(0x88FFFFFF);
        paint.setStrokeWidth(3f);
        for (int i = 0; i < getParticleCountForWeather(WeatherType.RAIN); i++) {
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
        for (int i = 0; i < getParticleCountForWeather(WeatherType.SNOW); i++) {
            Particle p = new Particle();
            p.x = random.nextInt(1000);
            p.y = random.nextInt(2000);
            p.speed = 3 + random.nextInt(5);
            p.length = 10 + random.nextInt(10);
            particles.add(p);
        }
    }


    static int getParticleCountForWeather(WeatherType type) {
        if (type == WeatherType.RAIN) return 120;
        if (type == WeatherType.SNOW) return 100;
        return 0;
    }

    static boolean hasParticles(WeatherType type) {
        return type == WeatherType.RAIN || type == WeatherType.SNOW;
    }

    static boolean isOverlayOnly(WeatherType type) {
        return type == WeatherType.NIGHT || type == WeatherType.SUN;
    }

    static int getOverlayColor(WeatherType type) {
        if (type == WeatherType.NIGHT) return 0x88000044;
        if (type == WeatherType.SUN) return 0x33FFFF00;
        return 0;
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
        if (isOverlayOnly(weatherType)) {
            canvas.drawColor(getOverlayColor(weatherType));
        }

        invalidate();
    }
}