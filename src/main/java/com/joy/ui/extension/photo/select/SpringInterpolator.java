package com.joy.ui.extension.photo.select;

import android.view.animation.LinearInterpolator;

public class SpringInterpolator extends LinearInterpolator {
    private float factor;

    public SpringInterpolator() {
        factor = 0.4f;
    }

    public SpringInterpolator(float factor) {
        this.factor = factor;
    }

    @Override
    public float getInterpolation(float input) {
        return (float) (Math.pow(2, -10 * input) * Math.sin((input - factor / 4) * (2 * Math.PI) / factor) + 1);
    }
}
