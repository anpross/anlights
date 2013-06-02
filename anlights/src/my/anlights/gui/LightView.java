package my.anlights.gui;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import my.anlights.Constants;
import android.util.Log;

/**
 * Created by Andreas on 02.06.13.
 */
public class LightView extends View {

    private static final String TAG = Constants.LOGGING_TAG;

    private Paint textPaint;

    private int height;
    private int width;

    private int usableHeight;
    private int usableWidth;

    private int horizontalButtons;
    private int verticalButtons;

    private int buttonWidth;
    private int borderWidth;

    private static int GRID_WIDTH_DP = 42;
    private static int OUTER_GRID_BORDER = 8;

    private static int BRIGHTNESS_MAX = 255;
    private static int TEMPERATURE_MAX = 346;

    private int brightness;
    private int temperature;

    private Point currPoint;

    private OnLightStateChangeListener changeListener;

    //2000k - 7000k in 200k steps
    private int[] COLOR_REFERENCES = {0xff8912,0xff932c,0xff9d3f,0xffa54f,0xffad5e,0xffb46b,0xffbb78,0xffc184,0xffc78f,
            0xffcc99,0xffd1a3,0xffd5ad,0xffd9b6,0xffddbe,0xffe1c6,0xffe4ce,0xffe8d5,0xffebdc,0xffeee3,0xfff0e9,0xfff3ef,
            0xfff5f5,0xfff8fb,0xfef9ff,0xfef9ff,0xf9f6ff,0xf5f3ff,0xf0f1ff,0xedefff,0xe9edff,0xe6ebff,0xe3e9ff,0xe0e7ff  };
    public LightView(Context context, AttributeSet attrs) {
        super(context, attrs);

        initLightView();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i(TAG, "touched at:"+event.getX()+":"+event.getY());

        currPoint = getSelectedButton(event.getX(),event.getY());
        Log.i(TAG, "thats button:"+ currPoint);

        brightness = BRIGHTNESS_MAX - (BRIGHTNESS_MAX / (verticalButtons - 1) * currPoint.y); // -1 because of base 0
        temperature = TEMPERATURE_MAX / (horizontalButtons - 1) * currPoint.x; // -1 because of base 0
        // start the drawing
        invalidate();

        if(changeListener != null) {
            changeListener.onLightStateChanged(this, brightness, temperature);
        }

        return super.onTouchEvent(event);
    }

    private Point getSelectedButton(float x, float y){
        int pointX = (int) Math.floor((x - borderWidth) / buttonWidth);
        int pointY = (int) Math.floor((y - borderWidth) / buttonWidth);
        return new Point(pointX,pointY);
    }

    private void initLightView() {
        textPaint = new Paint();
        buttonWidth = (int) getResources().getDisplayMetrics().density * GRID_WIDTH_DP;
        borderWidth = (int) getResources().getDisplayMetrics().density * OUTER_GRID_BORDER;
        textPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int hCount = 0;
        int vCount = 0;

//        textPaint.setColor(Color.DKGRAY);
//        canvas.drawRect(new Rect(0,0,usableWidth,usableHeight),textPaint);
        Rect rect;
        for (; vCount < verticalButtons; vCount++){
            for(; hCount < horizontalButtons; hCount++){
                if(currPoint != null && currPoint.x == hCount && currPoint.y == vCount){
                    int highlightBorder = borderWidth / 2;
                    int left = borderWidth + (hCount * buttonWidth)-highlightBorder;
                    int top = borderWidth + (vCount * buttonWidth)-highlightBorder;
                    int right = borderWidth + ((hCount + 1) * buttonWidth)+highlightBorder;
                    int bottom = borderWidth + ((vCount + 1) * buttonWidth)+highlightBorder;
                    rect = new Rect(left, top, right, bottom);
                    textPaint.setColor(Color.WHITE);
                    canvas.drawRect(rect, textPaint);
                }
                int left = borderWidth + (hCount * buttonWidth)+borderWidth;
                int top = borderWidth + (vCount * buttonWidth)+borderWidth;
                int right = borderWidth + ((hCount + 1) * buttonWidth)-borderWidth;
                int bottom = borderWidth + ((vCount + 1) * buttonWidth)-borderWidth;
                //Log.i(TAG, "drawing: "+left+" "+top+" "+right+" "+bottom);

                rect = new Rect(left, top, right, bottom);
                textPaint.setColor(Color.BLACK);
                canvas.drawRect(rect, textPaint);
                textPaint.setColor(getColorForCoordinates(hCount,vCount));
                canvas.drawRect(rect, textPaint);
            }
            hCount = 0;
        }
        //canvas.drawText("hello world, i'm "+width+" by "+height+ " density is:"+getResources().getDisplayMetrics().density, 0, 100, textPaint);
    }

    private int getColorForCoordinates(int horizontal, int vertical) {
        // horrizontal : link-kalt -> rechts-warm
        // vertical : oben-hell -> unten aus
        int alpha = BRIGHTNESS_MAX - (vertical * BRIGHTNESS_MAX / (verticalButtons - 1 )); // -1 because this number has base 0
        //Log.i(TAG, "return alpha:"+alpha+" for v-pos:"+vertical);
        int temp = TEMPERATURE_MAX - (horizontal * TEMPERATURE_MAX / (horizontalButtons - 1));
        int color = getColorForPct(temp);

        return Color.argb(alpha,Color.red(color), Color.green(color), Color.blue(color));
    }

    private int getColorForPct(double pct){
//        Log.i(TAG, "getColorForPct:"+pct);

        double positionInRange = pct / COLOR_REFERENCES.length;
        int lowValue = (int) Math.floor(positionInRange);
        int highValue = (int) Math.ceil(positionInRange);

        double positionBetween = pct - lowValue * COLOR_REFERENCES.length;

        //Log.i(TAG, "pct:"+pct+" l:"+lowValue+" h:"+highValue+" pos:"+positionBetween);

        return COLOR_REFERENCES[lowValue];
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // take AAAALLLLLL the space
        this.width = measureWidth(widthMeasureSpec);
        this.height = measureHeight(heightMeasureSpec);

        this.horizontalButtons = getNumberOfElements(width);
        this.verticalButtons = getNumberOfElements(height);

        this.usableHeight = getPreferedWidth(verticalButtons);
        this.usableWidth = getPreferedWidth(horizontalButtons);

        Log.d(TAG, "onMessure("+width+", "+height+")");
        Log.d(TAG, "  rounded:"+usableWidth+" x "+usableHeight);
        Log.d(TAG, "  buttons:"+horizontalButtons+" x "+ verticalButtons);
        setMeasuredDimension(usableWidth, usableHeight);
    }

    protected int getNumberOfElements (int availibleWidth) {
        double availibleInnerWidth = availibleWidth - (2 * borderWidth);
        return (int) Math.floor(availibleInnerWidth / buttonWidth);
    }
    protected int getPreferedWidth(int numberOfElements) {
        return numberOfElements * buttonWidth + (2 * borderWidth);
    }


    /**
     * Determines the width of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        }

        return result;
    }

    /**
     * Determines the height of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        Log.i(TAG,"height mode:"+specMode);
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        }
        return result;
    }

    public void setOnLightStateChangeListener(OnLightStateChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    public static interface OnLightStateChangeListener {
        void onLightStateChanged(LightView lightView, int brightness, int temperature);
    }

}
