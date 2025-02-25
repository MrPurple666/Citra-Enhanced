/**
 * Copyright 2013 Dolphin Emulator Project
 * Licensed under GPLv2+
 * Refer to the license.txt file included.
 */

package org.citra.emu.overlay;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;

import org.citra.emu.NativeLibrary.ButtonType;
import org.citra.emu.utils.EmulationMenuSettings;

/**
 * Custom {@link BitmapDrawable} that is capable
 * of storing it's own ID.
 */
public final class InputOverlayJoystick {
    private final int[] mAxisIDs = {0, 0, 0, 0};
    private final float[] mAxises = {0f, 0f};
    private int mTrackId = -1;
    private int mJoystickType;
    private int mControlPositionX, mControlPositionY;
    private int mPreviousTouchX, mPreviousTouchY;
    private int mWidth;
    private int mHeight;
    private int mAlpha;
    private Rect mVirtBounds;
    private Rect mOrigBounds;
    private BitmapDrawable mOuterBitmap;
    private BitmapDrawable mDefaultStateInnerBitmap;
    private BitmapDrawable mPressedStateInnerBitmap;
    private BitmapDrawable mBoundsBoxBitmap;
    private boolean mPressedState = false;

    /**
     * Constructor
     *
     * @param res                {@link Resources} instance.
     * @param bitmapOuter        {@link Bitmap} which represents the outer non-movable part of the joystick.
     * @param bitmapInnerDefault {@link Bitmap} which represents the default inner movable part of the joystick.
     * @param bitmapInnerPressed {@link Bitmap} which represents the pressed inner movable part of the joystick.
     * @param rectOuter          {@link Rect} which represents the outer joystick bounds.
     * @param rectInner          {@link Rect} which represents the inner joystick bounds.
     * @param joystick           Identifier for which joystick this is.
     * @param alpha              0-255 int alpha value
     */
    public InputOverlayJoystick(Resources res, Bitmap bitmapOuter,
                                        Bitmap bitmapInnerDefault, Bitmap bitmapInnerPressed,
                                        Rect rectOuter, Rect rectInner, int joystick,
                                        int alpha) {
        mAxisIDs[0] = joystick + 1; // Up
        mAxisIDs[1] = joystick + 2; // Down
        mAxisIDs[2] = joystick + 3; // Left
        mAxisIDs[3] = joystick + 4; // Right
        mJoystickType = joystick;

        mOuterBitmap = new BitmapDrawable(res, bitmapOuter);
        mDefaultStateInnerBitmap = new BitmapDrawable(res, bitmapInnerDefault);
        mPressedStateInnerBitmap = new BitmapDrawable(res, bitmapInnerPressed);
        mBoundsBoxBitmap = new BitmapDrawable(res, bitmapOuter);
        mWidth = bitmapOuter.getWidth();
        mHeight = bitmapOuter.getHeight();
        mAlpha = alpha;

        setBounds(rectOuter);
        mDefaultStateInnerBitmap.setBounds(rectInner);
        mPressedStateInnerBitmap.setBounds(rectInner);
        mVirtBounds = getBounds();
        mOrigBounds = mOuterBitmap.copyBounds();
        mBoundsBoxBitmap.setAlpha(0);
        mBoundsBoxBitmap.setBounds(getVirtBounds());
        SetInnerBounds();

        mDefaultStateInnerBitmap.setAlpha(alpha);
        mPressedStateInnerBitmap.setAlpha(alpha);
        mOuterBitmap.setAlpha(alpha);
    }

    /**
     * Gets this InputOverlayJoystick's button ID.
     *
     * @return this InputOverlayJoystick's button ID.
     */
    public int getId() {
        return mJoystickType;
    }

    public void draw(Canvas canvas) {
        mOuterBitmap.draw(canvas);
        getCurrentStateBitmapDrawable().setAlpha(mAlpha);
        getCurrentStateBitmapDrawable().draw(canvas);
        mBoundsBoxBitmap.draw(canvas);
    }

    public void TrackEvent(MotionEvent event) {
        int pointerIndex = event.getActionIndex();

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (getBounds().contains((int) event.getX(pointerIndex), (int) event.getY(pointerIndex))) {
                    mPressedState = true;
                    mOuterBitmap.setAlpha(0);
                    mBoundsBoxBitmap.setAlpha(mAlpha);
                    if (EmulationMenuSettings.getJoystickRelCenter()) {
                        getVirtBounds().offset((int) event.getX(pointerIndex) - getVirtBounds().centerX(),
                                (int) event.getY(pointerIndex) - getVirtBounds().centerY());
                    }
                    mBoundsBoxBitmap.setBounds(getVirtBounds());
                    mTrackId = event.getPointerId(pointerIndex);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                if (mTrackId == event.getPointerId(pointerIndex)) {
                    mPressedState = false;
                    mAxises[0] = mAxises[1] = 0.0f;
                    mOuterBitmap.setAlpha(mAlpha);
                    mBoundsBoxBitmap.setAlpha(0);
                    setVirtBounds(new Rect(mOrigBounds.left, mOrigBounds.top, mOrigBounds.right,
                            mOrigBounds.bottom));
                    setBounds(new Rect(mOrigBounds.left, mOrigBounds.top, mOrigBounds.right,
                            mOrigBounds.bottom));
                    SetInnerBounds();
                    mTrackId = -1;
                }
                break;
        }

        if (mTrackId == -1)
            return;

        for (int i = 0; i < event.getPointerCount(); i++) {
            if (mTrackId == event.getPointerId(i)) {
                float touchX = event.getX(i);
                float touchY = event.getY(i);
                float maxY = getVirtBounds().bottom;
                float maxX = getVirtBounds().right;
                touchX -= getVirtBounds().centerX();
                maxX -= getVirtBounds().centerX();
                touchY -= getVirtBounds().centerY();
                maxY -= getVirtBounds().centerY();
                final float AxisX = touchX / maxX;
                final float AxisY = touchY / maxY;

                // Clamp the circle pad input to a circle
                final float angle = (float) Math.atan2(AxisY, AxisX);
                float radius = (float) Math.sqrt(AxisX * AxisX + AxisY * AxisY);
                if(radius > 1.0f)
                {
                    radius = 1.0f;
                }
                mAxises[0] = ((float)Math.cos(angle) * radius);
                mAxises[1] = ((float)Math.sin(angle) * radius);
                SetInnerBounds();
            }
        }
    }

    public boolean onConfigureTouch(MotionEvent event) {
        int pointerIndex = event.getActionIndex();
        int fingerPositionX = (int) event.getX(pointerIndex);
        int fingerPositionY = (int) event.getY(pointerIndex);

        int scale = 1;
        if (mJoystickType == ButtonType.STICK_C) {
            // C-stick is scaled down to be half the size of the circle pad
            scale = 2;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPreviousTouchX = fingerPositionX;
                mPreviousTouchY = fingerPositionY;
                break;
            case MotionEvent.ACTION_MOVE:
                int deltaX = fingerPositionX - mPreviousTouchX;
                int deltaY = fingerPositionY - mPreviousTouchY;
                mControlPositionX += deltaX;
                mControlPositionY += deltaY;
                setBounds(new Rect(mControlPositionX, mControlPositionY,
                        mOuterBitmap.getIntrinsicWidth() / scale + mControlPositionX,
                        mOuterBitmap.getIntrinsicHeight() / scale + mControlPositionY));
                setVirtBounds(new Rect(mControlPositionX, mControlPositionY,
                        mOuterBitmap.getIntrinsicWidth() / scale + mControlPositionX,
                        mOuterBitmap.getIntrinsicHeight() / scale + mControlPositionY));
                SetInnerBounds();
                setOrigBounds(new Rect(new Rect(mControlPositionX, mControlPositionY,
                        mOuterBitmap.getIntrinsicWidth() / scale + mControlPositionX,
                        mOuterBitmap.getIntrinsicHeight() / scale + mControlPositionY)));
                mPreviousTouchX = fingerPositionX;
                mPreviousTouchY = fingerPositionY;
                break;
        }
        return true;
    }


    public float[] getAxisValues() {
        return mAxises;
    }

    public int[] getAxisIDs() {
        return mAxisIDs;
    }

    private void SetInnerBounds() {
        int X = getVirtBounds().centerX() + (int) ((mAxises[0]) * (getVirtBounds().width() / 2));
        int Y = getVirtBounds().centerY() + (int) ((mAxises[1]) * (getVirtBounds().height() / 2));

        if (mJoystickType == ButtonType.STICK_LEFT) {
            X += 1;
            Y += 1;
        }

        if (X > getVirtBounds().centerX() + (getVirtBounds().width() / 2))
            X = getVirtBounds().centerX() + (getVirtBounds().width() / 2);
        if (X < getVirtBounds().centerX() - (getVirtBounds().width() / 2))
            X = getVirtBounds().centerX() - (getVirtBounds().width() / 2);
        if (Y > getVirtBounds().centerY() + (getVirtBounds().height() / 2))
            Y = getVirtBounds().centerY() + (getVirtBounds().height() / 2);
        if (Y < getVirtBounds().centerY() - (getVirtBounds().height() / 2))
            Y = getVirtBounds().centerY() - (getVirtBounds().height() / 2);

        int width = mPressedStateInnerBitmap.getBounds().width() / 2;
        int height = mPressedStateInnerBitmap.getBounds().height() / 2;
        mDefaultStateInnerBitmap.setBounds(X - width, Y - height, X + width, Y + height);
        mPressedStateInnerBitmap.setBounds(mDefaultStateInnerBitmap.getBounds());
    }

    public void setPosition(int x, int y) {
        mControlPositionX = x;
        mControlPositionY = y;
    }

    private BitmapDrawable getCurrentStateBitmapDrawable() {
        return mPressedState ? mPressedStateInnerBitmap : mDefaultStateInnerBitmap;
    }

    public Rect getBounds() {
        return mOuterBitmap.getBounds();
    }

    public void setBounds(Rect bounds) {
        mOuterBitmap.setBounds(bounds);
    }

    private void setOrigBounds(Rect bounds) {
        mOrigBounds = bounds;
    }

    private Rect getVirtBounds() {
        return mVirtBounds;
    }

    private void setVirtBounds(Rect bounds) {
        mVirtBounds = bounds;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }
}
