package com.testing.myapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
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
        boolean isSpotify  = "spotify".equals(platform);

        // Start/mid colors (vivid platform colors)
        int colorStart, colorMid;
        if (isYouTube) {
            colorStart = context.getResources().getColor(R.color.widget_gradient_yt_start);
            colorMid   = context.getResources().getColor(R.color.widget_gradient_yt_mid);
        } else if (isSpotify) {
            colorStart = context.getResources().getColor(R.color.widget_gradient_sp_start);
            colorMid   = context.getResources().getColor(R.color.widget_gradient_sp_center);
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

    /**
     * Spotify widget — aurora glow shadow + concentric waveform arcs.
     *
     * Layer 1: Deep dark-green base (dark) / white (light)
     * Layer 2: Radial aurora glow from bottom-left (speaker spotlight / shadow)
     * Layer 3: 9 concentric arcs sweeping upper-right (speaker-cone waveform)
     */
    public static Bitmap createSpotifyMusicBackground(Context context, int width, int height, boolean isDarkTheme) {
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        // ── Clip rounded corners ──────────────────────────────────────────────
        Path clipPath = new Path();
        clipPath.addRoundRect(new RectF(0, 0, width, height), 44f, 44f, Path.Direction.CW);
        canvas.clipPath(clipPath);

        // ── LAYER 1 : Base fill ───────────────────────────────────────────────
        canvas.drawColor(isDarkTheme ? 0xFF0A1410 : 0xFFFFFFFF);

        // ── LAYER 2 : Radial aurora glow — bottom-left origin ─────────────────
        Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        RadialGradient aurora = new RadialGradient(
            width * 0.05f, height * 1.05f, width * 1.15f,
            new int[]{
                isDarkTheme ? 0x661DB954 : 0x331DB954,
                isDarkTheme ? 0x221DB954 : 0x141DB954,
                0x00000000
            },
            new float[]{ 0f, 0.45f, 1f },
            Shader.TileMode.CLAMP
        );
        glowPaint.setShader(aurora);
        canvas.drawRect(0, 0, width, height, glowPaint);

        // ── LAYER 3 : 3 bold concentric arcs — fills any widget size ─────────
        // Radii and stroke widths are derived from the widget diagonal so they
        // always span the full card, whether the widget is tiny or 5-column wide.
        Paint arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);

        // Arc origin: off-screen bottom-left corner (same quadrant as before)
        float arcOriginX = width * -0.08f;
        float arcOriginY = height * 1.20f;

        // Diagonal of the widget — use this as the scale reference
        float diag = (float) Math.sqrt((double)(width * width) + (double)(height * height));

        // 3 arcs: inner fills left/bottom, middle covers centre, outer reaches top-right corner
        float[] radii    = { diag * 0.32f, diag * 0.62f, diag * 0.96f };
        int[]   alphas   = { 216,           165,           100           }; // 85% → 65% → 39%
        float[] strokes  = { diag * 0.07f,  diag * 0.052f, diag * 0.036f };

        for (int i = 0; i < 3; i++) {
            arcPaint.setColor(Color.argb(alphas[i], 0, 0, 0));
            arcPaint.setStrokeWidth(strokes[i]);

            RectF arcRect = new RectF(
                arcOriginX - radii[i], arcOriginY - radii[i],
                arcOriginX + radii[i], arcOriginY + radii[i]
            );
            canvas.drawArc(arcRect, 222f, 108f, false, arcPaint);
        }

        // ── Border ────────────────────────────────────────────────────────────
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2.5f);
        borderPaint.setColor(isDarkTheme ? 0x2A1DB954 : 0x3A1DB954);
        canvas.drawRoundRect(new RectF(1.5f, 1.5f, width - 1.5f, height - 1.5f), 44f, 44f, borderPaint);

        return output;
    }

    public static Bitmap createRibbonWavesBackground(Context context, int width, int height, String platform, boolean isDarkTheme) {
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        // Clip corners to 22dp approx -> 44px
        Path clipPath = new Path();
        clipPath.addRoundRect(new RectF(0, 0, width, height), 44f, 44f, Path.Direction.CW);
        canvas.clipPath(clipPath);

        // Card background — near-black (dark) or light grey (light)
        canvas.drawColor(context.getResources().getColor(
                isDarkTheme ? R.color.widget_card_base : R.color.widget_card_base_light));

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
    public static Bitmap createTextBitmap(Context context, String text, int color, float spSize) {
        if (text == null || text.isEmpty()) text = " ";
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(spSize * context.getResources().getDisplayMetrics().scaledDensity);
        
        try {
            android.graphics.Typeface typeface = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.ballet);
            if (typeface != null) {
                paint.setTypeface(typeface);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        android.graphics.Rect bounds = new android.graphics.Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);

        // measureText provides the exact full-length width required for cursive text to not get chopped
        float exactWidth = paint.measureText(text);
        // bounds.height() provides the exact ink height, stripping away massive cursive font metrics blank space
        float inkHeight = bounds.height();
        
        // Safety bounds
        if (exactWidth <= 0) exactWidth = 1;
        if (inkHeight <= 0) inkHeight = 1;

        // Add 8px of safety padding on all sides to absolutely prevent clipping any wild cursive swashes
        float padding = 8f;

        Bitmap bitmap = Bitmap.createBitmap((int) (exactWidth + padding * 2), (int) (inkHeight + padding * 2), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        
        // Draw the text exactly offsetting any internal bounds shift so it sits tightly flush to the start
        canvas.drawText(text, -bounds.left + padding, -bounds.top + padding, paint);
        
        return bitmap;
    }

    /**
     * Spotify configure page banner.
     * 4 spread-out ribbon banners, each taking a different route:
     * some cross the full screen, some loop around locally.
     * Colors read from resources so user can tweak them in colors.xml.
     * Text color = app bg color → cutout text effect on the ribbon.
     */
    public static Bitmap createSpotifyBannerBitmap(Context context, int width, int height) {
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawColor(0x00000000);

        float ribbonStroke = height * 0.065f;
        float textSz       = ribbonStroke * 0.50f;
        float W = width;  float H = height;

        // ribbon: { startX, startY, cp1X, cp1Y, cp2X, cp2Y, endX, endY }
        // Each entry from a different edge, exits a different side
        // Deliberately spread across vertical thirds so they don't cluster
        float[][] ribbons = {
            // Ribbon 1 — TOP region (~0–30% height)
            // Enters from LEFT mid-top, swoops down to mid-screen, exits RIGHT top
            { -40,    H*0.08f,   W*0.30f, H*0.40f,  W*0.65f, H*-0.05f, W+40, H*0.18f },

            // Ribbon 2 — UPPER-MID region (~25–55% height)
            // Enters from TOP-RIGHT, curves down-left, exits BOTTOM (doesn't reach right edge)
            { W*0.75f, -40,      W*0.85f, H*0.45f,  W*0.25f, H*0.60f,  W*0.15f, H+40 },

            // Ribbon 3 — LOWER-MID region (~45–75% height)
            // Enters from LEFT at mid-low, waves up slightly then back down, exits BOTTOM-RIGHT
            { -40,    H*0.55f,   W*0.35f, H*0.35f,  W*0.60f, H*0.75f,  W*0.92f, H+40 },

            // Ribbon 4 — BOTTOM region (~70–100% height)
            // Enters from RIGHT-BOTTOM, curves hard left across the bottom, exits LEFT
            { W+40,   H*0.82f,   W*0.70f, H*0.65f,  W*0.25f, H*0.95f,  -40,    H*0.78f },
        };

        // Read ribbon colors from resources — user-tunable in colors.xml
        int[] ribbonColors = {
            context.getResources().getColor(R.color.spotify_banner_ribbon_1),
            context.getResources().getColor(R.color.spotify_banner_ribbon_2),
            context.getResources().getColor(R.color.spotify_banner_ribbon_3),
            context.getResources().getColor(R.color.spotify_banner_ribbon_4),
        };
        // Text color = bg color for cutout feel (change spotify_banner_text in colors.xml)
        int textColor = context.getResources().getColor(R.color.spotify_banner_text);

        Paint ribbonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ribbonPaint.setStyle(Paint.Style.STROKE);
        ribbonPaint.setStrokeCap(Paint.Cap.BUTT);
        ribbonPaint.setStrokeJoin(Paint.Join.ROUND);
        ribbonPaint.setStrokeWidth(ribbonStroke);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSz);
        textPaint.setLetterSpacing(0.08f);
        textPaint.setTypeface(
            android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.BOLD)
        );

        String label = "  EXPERIMENTAL  EXPERIMENTAL  EXPERIMENTAL  EXPERIMENTAL  ";

        for (int i = 0; i < ribbons.length; i++) {
            float[] r = ribbons[i];
            Path path = new Path();
            path.moveTo(r[0], r[1]);
            path.cubicTo(r[2], r[3], r[4], r[5], r[6], r[7]);

            ribbonPaint.setColor(ribbonColors[i]);
            canvas.drawPath(path, ribbonPaint);

            // Text sits at vertical center of ribbon
            canvas.drawTextOnPath(label, path, textSz * 0.4f, textSz * 0.33f, textPaint);
        }

        return output;
    }
}
