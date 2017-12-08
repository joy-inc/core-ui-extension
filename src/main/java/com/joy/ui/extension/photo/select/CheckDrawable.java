package com.joy.ui.extension.photo.select;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;

import com.joy.ui.extension.R;

/**
 * Created by Daisw on 2017/11/29.
 */

public class CheckDrawable extends BitmapDrawable {

    private boolean isSelect;

    private Paint mBgPaint;
    private Paint mStrokePaint;
    private Paint mFillPaint;

    private float mOriginRadius;
    private float mCenterX, mCenterY;

    private ValueAnimator mAnimator;
    private boolean isAnimStarted;
    private float mAnimRadius;
    private boolean mAnimEnable = true;

    public CheckDrawable(Context context) {
        super(context.getResources(), BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_check));
        initPaint(context);
        initAnimator();
    }

    private void initPaint(Context context) {
        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setColor(Color.argb(75, 0, 0, 0));
        mBgPaint.setStyle(Paint.Style.FILL);

        mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mStrokePaint.setColor(Color.WHITE);
        mStrokePaint.setStyle(Paint.Style.STROKE);

        mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mFillPaint.setColor(context.getResources().getColor(R.color.color_accent));
        mFillPaint.setStyle(Paint.Style.FILL);
    }

    private void initAnimator() {
        mAnimator = ValueAnimator.ofFloat(0f, 1.0f);
        mAnimator.setDuration(1000);
        mAnimator.setInterpolator(new SpringInterpolator());
        mAnimator.addUpdateListener(animation -> {
            float animaValue = (float) animation.getAnimatedValue();
            mAnimRadius = mOriginRadius * animaValue;
            invalidateSelf();
        });
    }

    public void setSelect(boolean select) {
        setSelect(select, true);
    }

    public void setSelect(boolean select, boolean animEnable) {
        this.isSelect = select;
        if (!select) {
            isAnimStarted = false;
        }
        mAnimEnable = animEnable;
        invalidateSelf();
    }

    public boolean isSelect() {
        return isSelect;
    }

    public boolean toggleSelected() {
        setSelect(!isSelect);
        return isSelect;
    }

    @Override
    public void draw(Canvas canvas) {
        if (mOriginRadius == 0) {
            final Rect rect = getBounds();
            mOriginRadius = rect.width() >> 1;
            mCenterX = rect.centerX();
            mCenterY = rect.centerY();
        }
        if (isSelect) {
            drawSelected(canvas);
        } else {
            drawNormal(canvas);
        }
        super.draw(canvas);
    }

    private void drawSelected(Canvas canvas) {
        if (mAnimEnable) {
            if (!mAnimator.isRunning() && !isAnimStarted) {
                isAnimStarted = true;
                mAnimator.start();
            } else if (mAnimRadius > 0) {
                canvas.drawCircle(mCenterX, mCenterY, mAnimRadius, mFillPaint);
            }
        } else {
            canvas.drawCircle(mCenterX, mCenterY, mOriginRadius, mFillPaint);
        }
    }

    private void drawNormal(Canvas canvas) {
        float strokeRadius = mOriginRadius + 2;// border 2px
        canvas.drawCircle(mCenterX, mCenterY, mOriginRadius, mBgPaint);
        canvas.drawCircle(mCenterX, mCenterY, strokeRadius, mStrokePaint);
    }
}
