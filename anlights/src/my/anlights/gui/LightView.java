package my.anlights.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import my.anlights.Constants;
import my.anlights.util.MyLog;

/**
 * Created by Andreas on 02.06.13.
 */
public class LightView extends View {

    private static final String TAG = Constants.LOGGING_TAG;

    private Paint paint;

    private int horizontalButtons;
    private int verticalButtons;

    private int buttonWidth;
    private int borderWidth;

    private static int BRIGHTNESS_MAX = 255;
    private static int TEMPERATURE_MAX = 346;

    private Point currPoint;

    private OnLightStateChangeListener changeListener;

    private static final String CLASS_NAME = LightView.class.getCanonicalName();

    //2000k - 6600k in 200k steps
    private int[] COLOR_REFERENCES = {0xff8912, 0xff932c, 0xff9d3f, 0xffa54f, 0xffad5e, 0xffb46b, 0xffbb78, 0xffc184, 0xffc78f,
            0xffcc99, 0xffd1a3, 0xffd5ad, 0xffd9b6, 0xffddbe, 0xffe1c6, 0xffe4ce, 0xffe8d5, 0xffebdc, 0xffeee3, 0xfff0e9, 0xfff3ef,
            0xfff5f5, 0xfff8fb, 0xfef9ff, 0xfef9ff, 0xf9f6ff, 0xf5f3ff, 0xf0f1ff, 0xedefff, 0xe9edff, 0xe6ebff};

    public LightView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initLightView();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        MyLog.entering(CLASS_NAME, "onTouchEvent", event);

        MyLog.i("touched at:" + event.getX() + ":" + event.getY());

        currPoint = getSelectedButton(event.getX(), event.getY());

        int brightness = BRIGHTNESS_MAX - (BRIGHTNESS_MAX / (verticalButtons - 1) * currPoint.y);
        int temperature = TEMPERATURE_MAX / (horizontalButtons - 1) * currPoint.x;

        // start the drawing
        invalidate();

        if (changeListener != null) {
            changeListener.onLightStateChanged(this, brightness, temperature);
        }

        MyLog.exiting(CLASS_NAME, "onTouchEvent");
        return super.onTouchEvent(event);
    }

    private Point getSelectedButton(float x, float y) {
        MyLog.entering(CLASS_NAME, "getSelectedButton", x, y);

        int pointX = (int) Math.floor((x - borderWidth) / buttonWidth);
        int pointY = (int) Math.floor((y - borderWidth) / buttonWidth);

        Point thePoint = new Point(pointX, pointY);

        MyLog.exiting(CLASS_NAME, "getSelectedButton", thePoint);
        return thePoint;
    }

    private void initLightView() {
        paint = new Paint();
        if (getResources().getDisplayMetrics() != null) {
            int density = (int) getResources().getDisplayMetrics().density;
            int GRID_WIDTH_DP = 42;
            buttonWidth = density * GRID_WIDTH_DP;
            int OUTER_GRID_BORDER = 8;
            borderWidth = density * OUTER_GRID_BORDER;
            paint.setAntiAlias(true);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        MyLog.entering(CLASS_NAME, "onDraw", canvas);

        super.onDraw(canvas);

        int hCount = 0;
        int vCount = 0;

        Rect rect;

        for (; vCount < verticalButtons; vCount++) {
            for (; hCount < horizontalButtons; hCount++) {
                if (currPoint != null && currPoint.x == hCount && currPoint.y == vCount) {
                    int highlightBorder = borderWidth / 2;
                    int left = borderWidth + (hCount * buttonWidth) - highlightBorder;
                    int top = borderWidth + (vCount * buttonWidth) - highlightBorder;
                    int right = borderWidth + ((hCount + 1) * buttonWidth) + highlightBorder;
                    int bottom = borderWidth + ((vCount + 1) * buttonWidth) + highlightBorder;
                    rect = new Rect(left, top, right, bottom);
                    paint.setColor(Color.WHITE);
                    canvas.drawRect(rect, paint);
                }

                int left = borderWidth + (hCount * buttonWidth) + borderWidth;
                int top = borderWidth + (vCount * buttonWidth) + borderWidth;
                int right = borderWidth + ((hCount + 1) * buttonWidth) - borderWidth;
                int bottom = borderWidth + ((vCount + 1) * buttonWidth) - borderWidth;
                //Log.i(TAG, "drawing: "+left+" "+top+" "+right+" "+bottom);

                rect = new Rect(left, top, right, bottom);

                paint.setColor(Color.BLACK);
                canvas.drawRect(rect, paint);

                paint.setColor(getColorForCoordinates(hCount, vCount));
                canvas.drawRect(rect, paint);
            }
            hCount = 0;
        }

        MyLog.exiting(CLASS_NAME, "onDraw");
    }

    /**
     * horrizontal : link-kalt -> rechts-warm
     * vertical : oben-hell -> unten aus
     *
     * @param horizontal
     * @param vertical
     * @return
     */
    private int getColorForCoordinates(int horizontal, int vertical) {
        //MyLog.entering(CLASS_NAME, "getColorForCoordinates", horizontal, vertical);
        double minBrightness = 0.25;

        int alpha = BRIGHTNESS_MAX - (vertical * BRIGHTNESS_MAX / (verticalButtons - 1)); // -1 because this number has base 0
        int temp = TEMPERATURE_MAX - (horizontal * TEMPERATURE_MAX / (horizontalButtons - 1));
        int color = getColorForPct(temp);


        alpha = (int) (alpha - (alpha * minBrightness) + (255 * minBrightness));

        int theColor = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));

        //MyLog.exiting(CLASS_NAME, "getColorForCoordinates", theColor);
        return theColor;
    }

    private int getColorForPct(double pct) {
        //MyLog.entering(CLASS_NAME, "getColorForPct", pct);

        double positionInRange = pct / TEMPERATURE_MAX * COLOR_REFERENCES.length;
        int lowValue = (int) Math.floor(positionInRange);
        int highValue = (int) Math.ceil(positionInRange);

        double positionBetween = pct - lowValue * COLOR_REFERENCES.length;


        lowValue = lowValue - 1;
        if (lowValue < 0) {
            lowValue = 0;
        }

        int theColor = COLOR_REFERENCES[lowValue]; // base 0
        //MyLog.exiting(CLASS_NAME, "getColorForCoordinates", theColor);
        return theColor;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        MyLog.entering(CLASS_NAME, "onMeasure", widthMeasureSpec, heightMeasureSpec);
        // take AAAALLLLLL the space
        int width = measureWidth(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);

        this.horizontalButtons = getNumberOfElements(width);
        this.verticalButtons = getNumberOfElements(height);

        int usableHeight = getPreferedDistance(verticalButtons);
        int usableWidth = getPreferedDistance(horizontalButtons);

        MyLog.d("onMessure(" + width + ", " + height + ")");
        MyLog.d("  rounded:" + usableWidth + " x " + usableHeight);
        MyLog.d("  buttons:" + horizontalButtons + " x " + verticalButtons);
        setMeasuredDimension(usableWidth, usableHeight);
        MyLog.exiting(CLASS_NAME, "onMeasure");
    }

    protected int getNumberOfElements(int availibleLength) {
        MyLog.entering(CLASS_NAME, "getNumberOfElements", availibleLength);

        double availibleInnerWidth = availibleLength - (2 * borderWidth);
        int count = (int) Math.floor(availibleInnerWidth / buttonWidth);
        MyLog.exiting(CLASS_NAME, "getNumberOfElements", count);
        return count;
    }

    protected int getPreferedDistance(int numberOfElements) {
        MyLog.entering(CLASS_NAME, "getPreferedWidth", numberOfElements);
        int width = numberOfElements * buttonWidth + (2 * borderWidth);
        MyLog.exiting(CLASS_NAME, "getPreferedWidth", width);
        return width;
    }


    /**
     * Determines the width of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureWidth(int measureSpec) {
        MyLog.entering(CLASS_NAME, "measureWidth", measureSpec);
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        }

        MyLog.exiting(CLASS_NAME, "measureWidth", result);
        return result;
    }

    /**
     * Determines the height of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureHeight(int measureSpec) {
        MyLog.entering(CLASS_NAME, "measureHeight", measureSpec);
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        MyLog.d("height mode:" + specMode);
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        }

        MyLog.exiting(CLASS_NAME, "measureHeight", result);
        return result;
    }

    public void setOnLightStateChangeListener(OnLightStateChangeListener changeListener) {
        this.changeListener = changeListener;
    }


    public static interface OnLightStateChangeListener {
        void onLightStateChanged(LightView lightView, int brightness, int temperature);
    }

}
