package com.mouad.frontend.Services;

import javafx.animation.AnimationTimer;
import javafx.scene.control.Label;

public class CounterService {
    private static final long ANIMATION_DURATION_MS = 2000; // 2 seconds animation

    public static void animateCounter(Label label, int targetValue, String prefix) {
        final long startTime = System.nanoTime();
        final int startValue = 0;
        
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long elapsedNanos = now - startTime;
                double progress = (double) elapsedNanos / (ANIMATION_DURATION_MS * 1_000_000);
                
                if (progress >= 1.0) {
                    label.setText(String.valueOf(targetValue));
                    this.stop();
                } else {
                    // Use easeOut function for smoother animation
                    double easedProgress = 1 - Math.pow(1 - progress, 3);
                    int currentValue = startValue + (int) (easedProgress * (targetValue - startValue));
                    label.setText(String.valueOf(currentValue));
                }
            }
        };
        
        timer.start();
    }
}
