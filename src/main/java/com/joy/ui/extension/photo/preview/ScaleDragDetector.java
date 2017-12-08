package com.joy.ui.extension.photo.preview;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

/**
 * ****************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * *****************************************************************************
 */
public class ScaleDragDetector implements ScaleGestureDetector.OnScaleGestureListener {

    private static final int INVALID_POINTER_ID = -1;

    private final float mTouchSlop;
    private final float mMinimumVelocity;
    private final ScaleGestureDetector mScaleDetector;
    private final OnScaleDragGestureListener mScaleDragGestureListener;

    private VelocityTracker mVelocityTracker;
    private boolean mIsDragging;
    private float mLastTouchX;
    private float mLastTouchY;
    private int mActivePointerId = INVALID_POINTER_ID;
    private int mActivePointerIndex = 0;

    public ScaleDragDetector(Context context, OnScaleDragGestureListener scaleDragGestureListener) {
        mScaleDetector = new ScaleGestureDetector(context, this);
        mScaleDragGestureListener = scaleDragGestureListener;

        ViewConfiguration configuration = ViewConfiguration.get(context);
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mTouchSlop = configuration.getScaledTouchSlop();
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        float scaleFactor = detector.getScaleFactor();
        if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor)) {
            return false;
        }
        mScaleDragGestureListener.onScale(scaleFactor, detector.getFocusX(), detector.getFocusY());
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        mScaleDragGestureListener.onScaleEnd();
    }

    public boolean isScaling() {
        return mScaleDetector.isInProgress();
    }

    public boolean isDragging() {
        return mIsDragging;
    }

    private float getActiveX(MotionEvent ev) {
        if (mActivePointerIndex >= 0 && mActivePointerIndex < ev.getPointerCount()) {
            return ev.getX(mActivePointerIndex);
        } else {
            return ev.getX();
        }
    }

    private float getActiveY(MotionEvent ev) {
        if (mActivePointerIndex >= 0 && mActivePointerIndex < ev.getPointerCount()) {
            return ev.getY(mActivePointerIndex);
        } else {
            return ev.getY();
        }
    }

    public boolean onTouchEvent(MotionEvent ev) {
        mScaleDetector.onTouchEvent(ev);
        int action = ev.getActionMasked();
        onTouchActivePointer(action, ev);
        onTouchDragEvent(action, ev);
        return true;
    }

    private void onTouchActivePointer(int action, MotionEvent ev) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mActivePointerId = INVALID_POINTER_ID;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                final int pointerIndex = ev.getActionIndex();
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    final int newPointerIndex = (pointerIndex == 0) ? 1 : 0;
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                }
                break;
        }
        mActivePointerIndex = ev.findPointerIndex(
                mActivePointerId != INVALID_POINTER_ID ? mActivePointerId : 0);
    }

    private void onTouchDragEvent(int action, MotionEvent ev) {
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mVelocityTracker = VelocityTracker.obtain();
                if (mVelocityTracker != null) {
                    mVelocityTracker.addMovement(ev);
                }
                mLastTouchX = getActiveX(ev);
                mLastTouchY = getActiveY(ev);
                mIsDragging = false;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final float x = getActiveX(ev);
                final float y = getActiveY(ev);
                final float dx = x - mLastTouchX;
                final float dy = y - mLastTouchY;
                if (!mIsDragging) {
                    mIsDragging = Math.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
                }
                if (mIsDragging) {
                    mScaleDragGestureListener.onDrag(dx, dy);
                    mLastTouchX = x;
                    mLastTouchY = y;
                    if (mVelocityTracker != null) {
                        mVelocityTracker.addMovement(ev);
                    }
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (mIsDragging) {
                    if (mVelocityTracker != null) {
                        mLastTouchX = getActiveX(ev);
                        mLastTouchY = getActiveY(ev);

                        mVelocityTracker.addMovement(ev);
                        mVelocityTracker.computeCurrentVelocity(1000);

                        final float vX = mVelocityTracker.getXVelocity();
                        final float vY = mVelocityTracker.getYVelocity();
                        if (Math.max(Math.abs(vX), Math.abs(vY)) >= mMinimumVelocity) {
                            mScaleDragGestureListener.onFling(mLastTouchX, mLastTouchY, -vX, -vY);
                        }
                    }
                }
                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                break;
            }
        }
    }
}