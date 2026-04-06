package com.testing.myapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import com.testing.myapp.R;

public class WidgetCanvasRenderer {

    public static Bitmap createMinimalBackground(Context context, int width, int height, boolean isDarkTheme) {
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        // Dark mode: near-black card; Light mode: off-white card
        int bgColor = isDarkTheme
            ? context.getResources().getColor(R.color.widget_minimal_bg_dark)
            : context.getResources().getColor(R.color.widget_minimal_bg_light);
        canvas.drawColor(bgColor);
        return output;
    }

    /**
     * Draws the platform gradient for the Gradient style.
     * Dark mode: platform colors → black
     * Light mode: platform colors → white
     */
    public static Bitmap createGradientBackground(Context context, int width, int height, String platform, boolean isDarkTheme) {
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        // Rounded corners clip
        Path clipPath = new Path();
        clipPath.addRoundRect(new RectF(0, 0, width, height), 44f, 44f, Path.Direction.CW);
        canvas.clipPath(clipPath);

        boolean isYouTube = "youtube".equals(platform);

        // Start/mid colors (vivid platform colors)
        int colorStart, colorMid;
        if (isYouTube) {
            colorStart = context.getResources().getColor(R.color.widget_gradient_yt_start);
            colorMid   = context.getResources().getColor(R.color.widget_gradient_yt_mid);
        } else {
            colorStart = context.getResources().getColor(R.color.widget_gradient_ig_start);
            colorMid   = context.getResources().getColor(R.color.widget_gradient_ig_center);
        }

        // End color: black for dark mode, white for light mode
        int colorEnd = isDarkTheme ? 0xFF0A0A0A : 0xFFF5F5F5;

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        LinearGradient gradient = new LinearGradient(
            0, 0, width, 0,
            new int[]{ colorStart, colorMid, colorEnd },
            new float[]{ 0f, 0.45f, 1f },
            Shader.TileMode.CLAMP
        );
        paint.setShader(gradient);
        canvas.drawRect(0, 0, width, height, paint);

        return output;
    }

    public static Bitmap createRibbonWavesBackground(Context context, int width, int height, String platform) {
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        // Clip corners to 22dp approx -> 44px
        Path clipPath = new Path();
        clipPath.addRoundRect(new RectF(0, 0, width, height), 44f, 44f, Path.Direction.CW);
        canvas.clipPath(clipPath);

        // Card background
        canvas.drawColor(context.getResources().getColor(R.color.widget_card_base));

        // Lock aspect ratio logic to prevent line squeezing. Let small sizes clip natural lines.
        float w = Math.max(width, height * 2f);
        float h = w / 2f;

        Paint p1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        p1.setStyle(Paint.Style.STROKE);
        p1.setStrokeCap(Paint.Cap.ROUND);
        p1.setStrokeJoin(Paint.Join.ROUND);

        boolean isYouTube = "youtube".equals(platform);

        // Ribbon 1 — wide S-curve, dominant stroke
        Path ribbon1 = new Path();
        ribbon1.moveTo(-0.025f*w, 0.53f*h);
        ribbon1.cubicTo(0.075f*w, 0.375f*h,  0.15f*w, 0.19f*h,  0.25f*w, 0.34f*h);
        ribbon1.cubicTo(0.35f*w, 0.5f*h,     0.39f*w, 0.75f*h,  0.49f*w, 0.625f*h);
        ribbon1.cubicTo(0.59f*w, 0.5f*h,     0.64f*w, 0.22f*h,  0.75f*w, 0.31f*h);
        ribbon1.cubicTo(0.83f*w, 0.375f*h,   0.89f*w, 0.5f*h,   1.05f*w, 0.28f*h);

        LinearGradient grad1;
        if (isYouTube) {
            grad1 = new LinearGradient(0, 0, w, 0, 
                new int[]{ context.getResources().getColor(R.color.widget_ribbon_yt_t0), context.getResources().getColor(R.color.widget_ribbon_yt_t1), context.getResources().getColor(R.color.widget_ribbon_yt_t2) }, 
                new float[]{ 0f, 0.4f, 1f }, Shader.TileMode.CLAMP);
        } else {
            grad1 = new LinearGradient(0, 0, w, 0, 
                new int[]{ context.getResources().getColor(R.color.widget_ribbon_ig_t0), context.getResources().getColor(R.color.widget_ribbon_ig_t1), context.getResources().getColor(R.color.widget_ribbon_ig_t2) }, 
                new float[]{ 0f, 0.4f, 1f }, Shader.TileMode.CLAMP);
        }
        p1.setShader(grad1);
        p1.setStrokeWidth(w * 0.05f); 
        canvas.drawPath(ribbon1, p1);

        // Ribbon 2 — enters upper-right, dips low, exits lower-left
        Path ribbon2 = new Path();
        ribbon2.moveTo(1.05f*w, 0.19f*h);
        ribbon2.cubicTo(0.95f*w, 0.31f*h,  0.85f*w, 0.56f*h,  0.725f*w, 0.44f*h);
        ribbon2.cubicTo(0.60f*w, 0.31f*h,  0.55f*w, 0.69f*h,  0.425f*w, 0.81f*h);
        ribbon2.cubicTo(0.325f*w, 0.91f*h, 0.20f*w, 0.75f*h,  0.05f*w,  0.91f*h);
        ribbon2.cubicTo(0f, 0.95f*h, -0.025f*w, 0.99f*h, -0.025f*w, h);

        LinearGradient grad2;
        if (isYouTube) {
            grad2 = new LinearGradient(w, 0, 0, 0, 
                new int[]{ context.getResources().getColor(R.color.widget_ribbon_yt_t3), context.getResources().getColor(R.color.widget_ribbon_yt_t4), context.getResources().getColor(R.color.widget_ribbon_yt_t5) }, 
                new float[]{ 0f, 0.5f, 1f }, Shader.TileMode.CLAMP);
        } else {
            grad2 = new LinearGradient(w, 0, 0, 0, 
                new int[]{ context.getResources().getColor(R.color.widget_ribbon_ig_t3), context.getResources().getColor(R.color.widget_ribbon_ig_t4), context.getResources().getColor(R.color.widget_ribbon_ig_t5) }, 
                new float[]{ 0f, 0.5f, 1f }, Shader.TileMode.CLAMP);
        }
        p1.setShader(grad2);
        p1.setStrokeWidth(w * 0.035f);
        canvas.drawPath(ribbon2, p1);

        // Ribbon 3 — thin accent, bottom-left to upper-right
        Path ribbon3 = new Path();
        ribbon3.moveTo(-0.025f*w, 0.875f*h);
        ribbon3.cubicTo(0.10f*w,  0.69f*h,  0.20f*w, 0.47f*h,  0.325f*w, 0.59f*h);
        ribbon3.cubicTo(0.44f*w,  0.70f*h,  0.50f*w, 0.94f*h,  0.625f*w, 0.84f*h);
        ribbon3.cubicTo(0.74f*w,  0.75f*h,  0.83f*w, 0.44f*h,  0.95f*w,  0.375f*h);
        ribbon3.lineTo(1.075f*w, 0.31f*h);

        LinearGradient grad3;
        if (isYouTube) {
            grad3 = new LinearGradient(0, 0, w, 0, 
                new int[]{ context.getResources().getColor(R.color.widget_ribbon_yt_t3), context.getResources().getColor(R.color.widget_ribbon_yt_t6), context.getResources().getColor(R.color.widget_ribbon_yt_t7) }, 
                new float[]{ 0f, 0.6f, 1f }, Shader.TileMode.CLAMP);
        } else {
            grad3 = new LinearGradient(0, 0, w, 0, 
                new int[]{ context.getResources().getColor(R.color.widget_ribbon_ig_t3), context.getResources().getColor(R.color.widget_ribbon_ig_t6), context.getResources().getColor(R.color.widget_ribbon_ig_t7) }, 
                new float[]{ 0f, 0.6f, 1f }, Shader.TileMode.CLAMP);
        }
        p1.setShader(grad3);
        p1.setStrokeWidth(w * 0.021f);
        canvas.drawPath(ribbon3, p1);
        
        // 1dp inner border 
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(3f); 
        borderPaint.setColor(0x12FFFFFF); // Fixed slight gray hex buffer
        
        RectF insetRect = new RectF(1.5f, 1.5f, width - 1.5f, height - 1.5f);
        canvas.drawRoundRect(insetRect, 44f, 44f, borderPaint);

        return output;
    }
}
