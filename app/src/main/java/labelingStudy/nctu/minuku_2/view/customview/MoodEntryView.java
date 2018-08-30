/*
 * Copyright (c) 2016.
 *
 * DReflect and Minuku Libraries by Shriti Raj (shritir@umich.edu) and Neeraj Kumar(neerajk@uci.edu) is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * Based on a work at https://github.com/Shriti-UCI/Minuku-2.
 *
 *
 * You are free to (only if you meet the terms mentioned below) :
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 *
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package labelingStudy.nctu.minuku_2.view.customview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import labelingStudy.nctu.minuku.logger.Log;
import labelingStudy.nctu.minuku_2.R;

public class MoodEntryView extends View
{
    Float X1 = -50.0f, Y1 = -50.0f;
    Float X2 = -50.0f, Y2 = -50.0f;
    int viewWidth = 0;
    Bitmap background, tapCircleBlue;
    float rectX1 = 0, rectY1 = 0, rectX2 = 0, rectY2 = 0;
    Paint p, p1;
    boolean isBackgroundCroped = false;
    float scale;
    Context mContext = null;

    int colorCode = 0;
    int backgroundImageId = R.drawable.graph_new;

    public MoodEntryView(Context context, AttributeSet attrs, int passedInBackgroundImageId) {
        super(context, attrs);
        this.backgroundImageId = passedInBackgroundImageId;
    }

    public MoodEntryView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.mContext = context;

        tapCircleBlue = BitmapFactory.decodeResource(context.getResources(), getColorId());
        background = BitmapFactory.decodeResource(mContext.getResources(), this.backgroundImageId);

        scale = context.getResources().getDisplayMetrics().density;
        Float t = 25 * scale;
        tapCircleBlue = Bitmap.createScaledBitmap(tapCircleBlue, t.intValue(), t.intValue(), true);

        p = new Paint();
        p.setAntiAlias(true);
        p.setColor(Color.BLUE);

        p1 = new Paint();
        p1.setColor(Color.parseColor("#4c6385"));
    }

    private int getColorId()
    {
        Log.d("getColor id ", "************" + colorCode);
        switch (colorCode) {
            case 0:

                return R.drawable.blue_circle;

            case 1:
                Log.d("getColor id ", "************ RedCircle");
                return R.drawable.red_circle;

            case 2:
                return R.drawable.blue_circle;

            case 3:
                return R.drawable.pink_circle;

            case 4:
                return R.drawable.yellow_circle;

            default:
                return R.drawable.blue_circle;
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isBackgroundCroped) {
            viewWidth = this.getWidth();
            background = Bitmap.createScaledBitmap(background, viewWidth, viewWidth, true);
            isBackgroundCroped = true;
        }
        canvas.drawColor(Color.parseColor("#b5c2d4"));
        canvas.drawBitmap(background, 0, 0, null);
        drawMoods(canvas);
        super.onDraw(canvas);
    }

    private void drawMoods(Canvas canvas) {
        canvas.drawBitmap(tapCircleBlue, X1.intValue(), Y1.intValue(), null);
        canvas.drawRect(rectX1, rectY1, rectX2, rectY2, p1);
    }


    public void setFirstMood(float f, float g) {
        X1 = f;
        Y1 = g;
        invalidate();
    }

    public void setRect(float x1, float x2, float y1, float y2) {
        rectX1 = x1;
        rectX2 = x2;
        rectY1 = y1;
        rectY2 = y2;
    }

    public void setPointsColor(int cCode) {
        Log.d("setPointsColor ", "************" + cCode);
        this.colorCode = cCode;
        tapCircleBlue = BitmapFactory.decodeResource(mContext.getResources(), getColorId());
        scale = mContext.getResources().getDisplayMetrics().density;
        Float t = 25 * scale;
        tapCircleBlue = Bitmap.createScaledBitmap(tapCircleBlue, t.intValue(), t.intValue(), true);
    }

    public void setBackgroundImageId(int backgroundImageId) {
        this.backgroundImageId = backgroundImageId;
        background = BitmapFactory.decodeResource(mContext.getResources(), this.backgroundImageId);
        isBackgroundCroped = false;
    }
}
