/*
 * Copyright 2015 - 2018 Michael Rapp
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package de.mrapp.android.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static de.mrapp.android.util.Condition.ensureAtLeast;
import static de.mrapp.android.util.Condition.ensureAtMaximum;
import static de.mrapp.android.util.Condition.ensureFileIsNoDirectory;
import static de.mrapp.android.util.Condition.ensureGreater;
import static de.mrapp.android.util.Condition.ensureNotEmpty;
import static de.mrapp.android.util.Condition.ensureNotNull;
import static de.mrapp.android.util.Condition.ensureSmaller;
import static de.mrapp.android.util.DisplayUtil.getDensity;

/**
 * An utility class, which provides static methods, which allow to create and edit bitmaps.
 *
 * @author Michael Rapp
 * @since 1.0.0
 */
public final class BitmapUtil {

    /**
     * The angle of a complete arc.
     */
    private static final int COMPLETE_ARC_ANGLE = 360;

    /**
     * Calculates the sample size, which should be used to downsample an image to a maximum width
     * and height.
     *
     * @param imageDimensions
     *         A pair, which contains the width and height of the image, which should be
     *         downsampled, as an instance of the class Pair. The pair may not be null
     * @param maxWidth
     *         The maximum width in pixels as an {@link Integer} value. The maximum width must be at
     *         least 1
     * @param maxHeight
     *         The maximum height in pixels as an{@link Integer} value. The maximum height must be
     *         at least 1
     * @return The sample size, which has been calculated, as an {@link Integer} value
     */
    private static int getSampleSize(@NonNull final Pair<Integer, Integer> imageDimensions,
                                     final int maxWidth, final int maxHeight) {
        ensureNotNull(imageDimensions, "The image dimensions may not be null");
        ensureAtLeast(maxWidth, 1, "The maximum width must be at least 1");
        ensureAtLeast(maxHeight, 1, "The maximum height must be at least 1");
        int width = imageDimensions.first;
        int height = imageDimensions.second;
        int sampleSize = 1;

        if (width > maxWidth || height > maxHeight) {
            int halfWidth = width / 2;
            int halfHeight = height / 2;

            while ((halfWidth / sampleSize) > maxWidth && (halfHeight / sampleSize) > maxHeight) {
                sampleSize *= 2;
            }
        }

        return sampleSize;
    }

    /**
     * Creates a new utility class, which provides static methods, which allow to create and edit
     * bitmaps.
     */
    private BitmapUtil() {

    }

    /**
     * Clips the corners of a bitmap in order to transform it into a round shape. Bitmaps, whose
     * width and height are not equal, will be clipped to a square beforehand.
     *
     * @param bitmap
     *         The bitmap, which should be clipped, as an instance of the class {@link Bitmap}. The
     *         bitmap may not be null
     * @return The clipped bitmap as an instance of the class {@link Bitmap}
     */
    public static Bitmap clipCircle(@NonNull final Bitmap bitmap) {
        ensureNotNull(bitmap, "The bitmap may not be null");
        return clipCircle(bitmap,
                bitmap.getWidth() >= bitmap.getHeight() ? bitmap.getHeight() : bitmap.getWidth());
    }

    /**
     * Clips the corners of a bitmap in order to transform it into a round shape. Additionally, the
     * bitmap is resized to a specific size. Bitmaps, whose width and height are not equal, will be
     * clipped to a square beforehand.
     *
     * @param bitmap
     *         The bitmap, which should be clipped, as an instance of the class {@link Bitmap}. The
     *         bitmap may not be null
     * @param size
     *         The size, the bitmap should be resized to, as an {@link Integer} value in pixels. The
     *         size must be at least 1
     * @return The clipped bitmap as an instance of the class {@link Bitmap}
     */
    public static Bitmap clipCircle(@NonNull final Bitmap bitmap, final int size) {
        Bitmap squareBitmap = clipSquare(bitmap, size);
        int squareSize = squareBitmap.getWidth();
        float radius = (float) squareSize / 2.0f;
        Bitmap clippedBitmap = Bitmap.createBitmap(squareSize, squareSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(clippedBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        canvas.drawCircle(radius, radius, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(squareBitmap, new Rect(0, 0, squareSize, squareSize),
                new Rect(0, 0, squareSize, squareSize), paint);
        return clippedBitmap;
    }

    /**
     * Clips the corners of a bitmap in order to transform it into a round shape. Additionally, a
     * border will be added. Bitmaps, whose width and height are not equal, will be clipped to a
     * square beforehand.
     *
     * @param bitmap
     *         The bitmap, which should be clipped, as an instance of the class {@link Bitmap}. The
     *         bitmap may not be null
     * @param borderWidth
     *         The width of the border as an {@link Integer} value in pixels. The width must be at
     *         least 0
     * @param borderColor
     *         The color of the border as an {@link Integer} value
     * @return The clipped bitmap as an instance of the class {@link Bitmap}
     */
    public static Bitmap clipCircle(@NonNull final Bitmap bitmap, final int borderWidth,
                                    @ColorInt final int borderColor) {
        ensureNotNull(bitmap, "The bitmap may not be null");
        return clipCircle(bitmap,
                bitmap.getWidth() >= bitmap.getHeight() ? bitmap.getHeight() : bitmap.getWidth(),
                borderWidth, borderColor);
    }

    /**
     * Clips the corners of a bitmap in order to transform it into a round shape. Additionally, the
     * bitmap is resized to a specific size and a border will be added. Bitmaps, whose width and
     * height are not equal, will be clipped to a square beforehand.
     *
     * @param bitmap
     *         The bitmap, which should be clipped, as an instance of the class {@link Bitmap}. The
     *         bitmap may not be null
     * @param size
     *         The size, the bitmap should be resized to, as an {@link Integer} value in pixels. The
     *         size must be at least 1
     * @param borderWidth
     *         The width of the border as an {@link Integer} value in pixels. The width must be at
     *         least 0
     * @param borderColor
     *         The color of the border as an {@link Integer} value
     * @return The clipped bitmap as an instance of the class {@link Bitmap}
     */
    public static Bitmap clipCircle(@NonNull final Bitmap bitmap, final int size,
                                    final int borderWidth, @ColorInt final int borderColor) {
        ensureAtLeast(borderWidth, 0, "The border width must be at least 0");
        Bitmap clippedBitmap = clipCircle(bitmap, size);
        Bitmap result = Bitmap.createBitmap(clippedBitmap.getWidth(), clippedBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        float offset = borderWidth / 2.0f;
        Rect src = new Rect(0, 0, clippedBitmap.getWidth(), clippedBitmap.getHeight());
        RectF dst =
                new RectF(offset, offset, result.getWidth() - offset, result.getHeight() - offset);
        canvas.drawBitmap(clippedBitmap, src, dst, null);

        if (borderWidth > 0 && Color.alpha(borderColor) != 0) {
            Paint paint = new Paint();
            paint.setFilterBitmap(false);
            paint.setAntiAlias(true);
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(borderWidth);
            paint.setColor(borderColor);
            offset = borderWidth / 2.0f;
            RectF bounds = new RectF(offset, offset, result.getWidth() - offset,
                    result.getWidth() - offset);
            canvas.drawArc(bounds, 0, COMPLETE_ARC_ANGLE, false, paint);
        }

        return result;
    }

    /**
     * Clips the long edge of a bitmap, if its width and height are not equal, in order to form it
     * into a square.
     *
     * @param bitmap
     *         The bitmap, which should be clipped, as an instance of the class {@link Bitmap}. The
     *         bitmap may not be null
     * @return The clipped bitmap as an instance of the class {@link Bitmap}
     */
    public static Bitmap clipSquare(@NonNull final Bitmap bitmap) {
        ensureNotNull(bitmap, "The bitmap may not be null");
        return clipSquare(bitmap,
                bitmap.getWidth() >= bitmap.getHeight() ? bitmap.getHeight() : bitmap.getWidth());
    }

    /**
     * Clips the long edge of a bitmap, if its width and height are not equal, in order to form it
     * into a square. Additionally, the bitmap is resized to a specific size.
     *
     * @param bitmap
     *         The bitmap, which should be clipped, as an instance of the class {@link Bitmap}. The
     *         bitmap may not be null
     * @param size
     *         The size, the bitmap should be resized to, as an {@link Integer} value in pixels. The
     *         size must be at least 1
     * @return The clipped bitmap as an instance of the class {@link Bitmap}
     */
    @SuppressWarnings("SuspiciousNameCombination")
    public static Bitmap clipSquare(@NonNull final Bitmap bitmap, final int size) {
        ensureNotNull(bitmap, "The bitmap may not be null");
        ensureAtLeast(size, 1, "The size must be at least 1");
        Bitmap clippedBitmap = bitmap;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width > height) {
            clippedBitmap = Bitmap.createBitmap(bitmap, width / 2 - height / 2, 0, height, height);
        } else if (bitmap.getWidth() < bitmap.getHeight()) {
            clippedBitmap =
                    Bitmap.createBitmap(bitmap, 0, bitmap.getHeight() / 2 - width / 2, width,
                            width);
        }

        if (clippedBitmap.getWidth() != size) {
            clippedBitmap = resize(clippedBitmap, size, size);
        }

        return clippedBitmap;
    }

    /**
     * Clips the long edge of a bitmap, if its width and height are not equal, in order to transform
     * it into a square. Additionally, a border will be added.
     *
     * @param bitmap
     *         The bitmap, which should be clipped, as an instance of the class {@link Bitmap}. The
     *         bitmap may not be null
     * @param borderWidth
     *         The width of the border as an {@link Integer} value in pixels. The width must be at
     *         least 0
     * @param borderColor
     *         The color of the border as an {@link Integer} value
     * @return The clipped bitmap as an instance of the class {@link Bitmap}
     */
    public static Bitmap clipSquare(@NonNull final Bitmap bitmap, final int borderWidth,
                                    @ColorInt final int borderColor) {
        ensureNotNull(bitmap, "The bitmap may not be null");
        return clipSquare(bitmap,
                bitmap.getWidth() >= bitmap.getHeight() ? bitmap.getHeight() : bitmap.getWidth(),
                borderWidth, borderColor);
    }

    /**
     * Clips the long edge of a bitmap, if its width and height are not equal, in order to transform
     * it into a square. Additionally, the bitmap is resized to a specific size and a border will be
     * added.
     *
     * @param bitmap
     *         The bitmap, which should be clipped, as an instance of the class {@link Bitmap}. The
     *         bitmap may not be null
     * @param size
     *         The size, the bitmap should be resized to, as an {@link Integer} value in pixels. The
     *         size must be at least 1
     * @param borderWidth
     *         The width of the border as an {@link Integer} value in pixels. The width must be at
     *         least 0
     * @param borderColor
     *         The color of the border as an {@link Integer} value
     * @return The clipped bitmap as an instance of the class {@link Bitmap}
     */
    public static Bitmap clipSquare(@NonNull final Bitmap bitmap, final int size,
                                    final int borderWidth, @ColorInt final int borderColor) {
        ensureAtLeast(borderWidth, 0, "The border width must be at least 0");
        Bitmap clippedBitmap = clipSquare(bitmap, size);
        Bitmap result = Bitmap.createBitmap(clippedBitmap.getWidth(), clippedBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        float offset = borderWidth / 2.0f;
        Rect src = new Rect(0, 0, clippedBitmap.getWidth(), clippedBitmap.getHeight());
        RectF dst =
                new RectF(offset, offset, result.getWidth() - offset, result.getHeight() - offset);
        canvas.drawBitmap(clippedBitmap, src, dst, null);

        if (borderWidth > 0 && Color.alpha(borderColor) != 0) {
            Paint paint = new Paint();
            paint.setFilterBitmap(false);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(borderWidth);
            paint.setColor(borderColor);
            offset = borderWidth / 2.0f;
            RectF bounds = new RectF(offset, offset, result.getWidth() - offset,
                    result.getWidth() - offset);
            canvas.drawRect(bounds, paint);
        }

        return result;
    }

    /**
     * Resizes a bitmap to a specific width and height. If the ratio between width and height
     * differs from the bitmap's original ratio, the bitmap is stretched.
     *
     * @param bitmap
     *         The bitmap, which should be resized, as an instance of the class {@link Bitmap}. The
     *         bitmap may not be null
     * @param width
     *         The width, the bitmap should be resized to, as an {@link Integer} value in pixels.
     *         The width must be at least 1
     * @param height
     *         The height, the bitmap should be resized to, as an {@link Integer} value in pixels.
     *         The height must be at least 1
     * @return The resized bitmap as an instance of the class {@link Bitmap}
     */
    public static Bitmap resize(@NonNull final Bitmap bitmap, final int width, final int height) {
        ensureNotNull(bitmap, "The bitmap may not be null");
        ensureAtLeast(width, 1, "The width must be at least 1");
        ensureAtLeast(height, 1, "The height must be at least 1");
        return Bitmap.createScaledBitmap(bitmap, width, height, false);
    }

    /**
     * Splits a specific bitmap horizontally at half.
     *
     * @param bitmap
     *         The bitmap, which should be split, as an instance of the class {@link Bitmap}. The
     *         bitmap may not be null
     * @return A pair, which contains the two bitmaps, which the original bitmap has been split
     * into, as an instance of the class Pair
     */
    public static Pair<Bitmap, Bitmap> splitHorizontally(@NonNull final Bitmap bitmap) {
        return splitHorizontally(bitmap, bitmap.getHeight() / 2);
    }

    /**
     * Splits a specific bitmap horizontally at a specific split point.
     *
     * @param bitmap
     *         The bitmap, which should be split, as an instance of the class {@link Bitmap}. The
     *         bitmap may not be null
     * @param splitPoint
     *         The row, the bitmap should be split at, counted from the top edge in pixels as an
     *         {@link Integer} value
     * @return A pair, which contains the two bitmaps, the original bitmap has been split into, as
     * an instance of the class Pair
     */
    public static Pair<Bitmap, Bitmap> splitHorizontally(@NonNull final Bitmap bitmap,
                                                         final int splitPoint) {
        ensureNotNull(bitmap, "The bitmap may not be null");
        ensureGreater(splitPoint, 0, "The split point must be greater than 0");
        ensureSmaller(splitPoint, bitmap.getHeight(),
                "The split point must be smaller than " + bitmap.getHeight());
        Bitmap topBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), splitPoint);
        Bitmap bottomBitmap = Bitmap.createBitmap(bitmap, 0, splitPoint, bitmap.getWidth(),
                bitmap.getHeight() - splitPoint);
        return new Pair<>(topBitmap, bottomBitmap);
    }

    /**
     * Splits a specific bitmap vertically at half.
     *
     * @param bitmap
     *         The bitmap, which should be split, as an instance of the class {@link Bitmap}. The
     *         bitmap may not be null
     * @return A pair, which contains the two bitmaps, the original bitmap has been split into, as
     * an instance of the class Pair
     */
    public static Pair<Bitmap, Bitmap> splitVertically(@NonNull final Bitmap bitmap) {
        return splitVertically(bitmap, bitmap.getWidth() / 2);
    }

    /**
     * Splits a specific bitmap vertically at a specific split point.
     *
     * @param bitmap
     *         The bitmap, which should be split, as an instance of the class {@link Bitmap}. The
     *         bitmap may not be null
     * @param splitPoint
     *         The column, the bitmap should be split at, counted from the left edge in pixels as an
     *         {@link Integer} value
     * @return A pair, which contains the two bitmaps, the original bitmap has been split into, as
     * an instance of the class Pair
     */
    public static Pair<Bitmap, Bitmap> splitVertically(@NonNull final Bitmap bitmap,
                                                       final int splitPoint) {
        ensureNotNull(bitmap, "The bitmap may not be null");
        ensureGreater(splitPoint, 0, "The split point must be greater than 0");
        ensureSmaller(splitPoint, bitmap.getWidth(),
                "The split point must be smaller than " + bitmap.getWidth());
        Bitmap leftBitmap = Bitmap.createBitmap(bitmap, 0, 0, splitPoint, bitmap.getHeight());
        Bitmap rightBitmap =
                Bitmap.createBitmap(bitmap, splitPoint, 0, bitmap.getWidth() - splitPoint,
                        bitmap.getHeight());
        return new Pair<>(leftBitmap, rightBitmap);
    }

    /**
     * Creates and returns a bitmap with a specific width and height by tiling another bitmap.
     *
     * @param bitmap
     *         The bitmap, which should be tiled, as an instance of the class {@link Bitmap}. The
     *         bitmap may not be null
     * @param width
     *         The width of the bitmap as an {@link Integer} value in pixels. The width must be at
     *         least 1
     * @param height
     *         The height of the bitmap as an {@link Integer} value in pixels. The height must be at
     *         least 1
     * @return The bitmap, which has been created as an instance of the class {@link Bitmap}
     */
    public static Bitmap tile(final Bitmap bitmap, final int width, final int height) {
        ensureNotNull(bitmap, "The bitmap may not be null");
        ensureAtLeast(width, 1, "The width must be at least 1");
        ensureAtLeast(height, 1, "The height must be at least 1");
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();

        for (int x = 0; x < width; x += originalWidth) {
            for (int y = 0; y < width; y += originalHeight) {
                int copyWidth = (width - x >= originalWidth) ? originalWidth : width - x;
                int copyHeight = (height - y >= originalHeight) ? originalHeight : height - y;
                Rect src = new Rect(0, 0, copyWidth, copyHeight);
                Rect dest = new Rect(x, y, x + copyWidth, y + copyHeight);
                canvas.drawBitmap(bitmap, src, dest, null);
            }
        }

        return result;
    }

    /**
     * Creates and returns a bitmap by overlaying it with a specific color.
     *
     * @param bitmap
     *         The bitmap, which should be tinted, as an instance of the class {@link Bitmap}. The
     *         bitmap may not be null
     * @param color
     *         The color, which should be used for tinting, as an {@link Integer} value
     * @return The bitmap, which has been created, as an instance of the class {@link Bitmap}
     */
    public static Bitmap tint(@NonNull final Bitmap bitmap, @ColorInt final int color) {
        ensureNotNull(bitmap, "The bitmap may not be null");
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), paint);
        return bitmap;
    }

    /**
     * Creates and returns a bitmap from a specific drawable.
     *
     * @param drawable
     *         The drawable, which should be converted, as an instance of the class {@link
     *         Drawable}. The drawable may not be null
     * @return The bitmap, which has been created, as an instance of the class {@link Bitmap}
     */
    public static Bitmap drawableToBitmap(@NonNull final Drawable drawable) {
        ensureNotNull(drawable, "The drawable may not be null");

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;

            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        Bitmap bitmap;

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap =
                    Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                            Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    /**
     * Creates and returns a bitmap from a specific text. The text is centered.
     *
     * @param context
     *         The context, which should be used, as an instance of the class {@link Context}. The
     *         context may not be null
     * @param width
     *         The width of the bitmap, which should be created, in pixels as an {@link Integer}
     *         value
     * @param height
     *         The height of the bitmap, which should be created, in pixels as an {@link Integer}
     *         value
     * @param backgroundColor
     *         The background color of the bitmap, which should be created, as an {@link Integer}
     *         value
     * @param text
     *         The text, the bitmap should be created from, as an instance of the type {@link
     *         CharSequence}. The text may neither be null, nor empty
     * @param textSize
     *         The text size, which should be used, in sp as an {@link Integer} value
     * @param textColor
     *         The text color, which should be used, as an {@link Integer} value The color of the
     *         text
     * @return The bitmap, which has been created, as an instance of the class {@link Bitmap}
     */
    public static Bitmap textToBitmap(@NonNull final Context context, final int width,
                                      final int height, @ColorInt final int backgroundColor,
                                      @NonNull final CharSequence text, final float textSize,
                                      @ColorInt final int textColor) {
        return textToBitmap(context, width, height, backgroundColor, text, textSize, textColor,
                null);
    }

    /**
     * Creates and returns a bitmap from a specific text. The text is centered.
     *
     * @param context
     *         The context, which should be used, as an instance of the class {@link Context}. The
     *         context may not be null
     * @param width
     *         The width of the bitmap, which should be created, in pixels as an {@link Integer}
     *         value
     * @param height
     *         The height of the bitmap, which should be created, in pixels as an {@link Integer}
     *         value
     * @param backgroundColor
     *         The background color of the bitmap, which should be created, as an {@link Integer}
     *         value
     * @param text
     *         The text, the bitmap should be created from, as an instance of the type {@link
     *         CharSequence}. The text may neither be null, nor empty
     * @param textSize
     *         The text size, which should be used, in sp as an {@link Integer} value
     * @param textColor
     *         The text color, which should be used, as an {@link Integer} value The color of the
     *         text
     * @param typeface
     *         The typeface, which should be used, as a value of the enum {@link Typeface} or null,
     *         if the default typeface should be used
     * @return The bitmap, which has been created, as an instance of the class {@link Bitmap}
     */
    public static Bitmap textToBitmap(@NonNull final Context context, final int width,
                                      final int height, @ColorInt final int backgroundColor,
                                      @NonNull final CharSequence text, final float textSize,
                                      @ColorInt final int textColor,
                                      @Nullable final Typeface typeface) {
        ensureNotNull(context, "The context may not be null");
        ensureNotNull(text, "The text may not be null");
        ensureNotEmpty(text, "The text may not be empty");
        ensureAtLeast(textSize, 1, "The text size must be at least 1");
        Bitmap bitmap = colorToBitmap(width, height, backgroundColor);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(textColor);
        paint.setTextSize(textSize * getDensity(context));
        paint.setTextAlign(Align.CENTER);

        if (typeface != null) {
            paint.setTypeface(typeface);
        }

        int x = bitmap.getWidth() / 2;
        int y = (int) ((bitmap.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2));
        canvas.drawText(text.toString(), x, y, paint);
        return bitmap;
    }

    /**
     * Creates and returns a bitmap from a specific color.
     *
     * @param width
     *         The width of the bitmap, which should be created, in pixels as an {@link Integer}
     *         value. The width must be at least 1
     * @param height
     *         The height of the bitmap, which should be created, in pixels as an {@link Integer}
     *         value. The height must be at least 1
     * @param color
     *         The color, which should be used, as an {@link Integer} value
     * @return The bitmap, which has been created, as an instance of the class {@link Bitmap}
     */
    public static Bitmap colorToBitmap(final int width, final int height,
                                       @ColorInt final int color) {
        ensureAtLeast(width, 1, "The width must be at least 1");
        ensureAtLeast(height, 1, "The height must be at least 1");
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(0, 0, width, height, paint);
        return bitmap;
    }

    /**
     * Returns the width and height of a specific image file.
     *
     * @param file
     *         The image file, whose width and height should be returned, as an instance of the
     *         class {@link File}. The file may not be null. The file must exist and must not be a
     *         directory
     * @return A pair, which contains the width and height of the given image file, as an instance
     * of the class Pair
     * @throws IOException
     *         The exception, which is thrown, if an error occurs while decoding the image file
     */
    public static Pair<Integer, Integer> getImageDimensions(@NonNull final File file)
            throws IOException {
        ensureNotNull(file, "The file may not be null");
        ensureFileIsNoDirectory(file, "The file must exist and must not be a directory");
        String path = file.getPath();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int width = options.outWidth;
        int height = options.outHeight;

        if (width == -1 || height == -1) {
            throw new IOException("Failed to decode image \"" + path + "\"");
        }

        return Pair.create(width, height);
    }

    /**
     * Returns the width and height of a specific image resource.
     *
     * @param context
     *         The context, which should be used, as an instance of the class {@link Context}. The
     *         context may not be null
     * @param resourceId
     *         The resource id of the image resource, whose width and height should be returned, as
     *         an {@link Integer} value. The resource id must correspond to a valid drawable
     *         resource
     * @return A pair, which contains the width and height of the given image resource, as an
     * instance of the class Pair
     * @throws IOException
     *         The exception, which is thrown, if an error occurs while decoding the image resource
     */
    public static Pair<Integer, Integer> getImageDimensions(@NonNull final Context context,
                                                            @DrawableRes final int resourceId)
            throws IOException {
        ensureNotNull(context, "The context may not be null");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        int width = options.outWidth;
        int height = options.outHeight;

        if (width == -1 || height == -1) {
            throw new IOException("Failed to decode image resource with id " + resourceId);
        }

        return Pair.create(width, height);
    }

    /**
     * Loads a downsampled thumbnail of a specific image file while maintaining its aspect ratio.
     *
     * @param file
     *         The image file, which should be loaded, as an instance of the class {@link File}. The
     *         file may not be null. The file must exist and must not be a directory
     * @param maxWidth
     *         The maximum width of the thumbnail in pixels as an {@link Integer} value. The maximum
     *         width must be at least 1
     * @param maxHeight
     *         The maximum height of the thumbnail in pixels as an {@link Integer} value. The
     *         maximum height must be at least 1
     * @return The thumbnail, which has been loaded, as an instance of the class {@link Bitmap}
     * @throws IOException
     *         The exception, which is thrown, if an error occurs while decoding the image file
     */
    public static Bitmap loadThumbnail(@NonNull final File file, final int maxWidth,
                                       final int maxHeight) throws IOException {
        Pair<Integer, Integer> imageDimensions = getImageDimensions(file);
        int sampleSize = getSampleSize(imageDimensions, maxWidth, maxHeight);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;
        String path = file.getAbsolutePath();
        Bitmap thumbnail = BitmapFactory.decodeFile(path, options);

        if (thumbnail == null) {
            throw new IOException("Failed to decode image \"" + path + "\"");
        }

        return thumbnail;
    }

    /**
     * Loads a downsampled thumbnail of a specific image resource while maintaining its aspect
     * ratio.
     *
     * @param context
     *         The context, which should be used, as an instance of the class {@link Context}. The
     *         context may not be null
     * @param resourceId
     *         The resource id of the image resource, which should be loaded, as an {@link Integer}
     *         value. The resource id must correspond to a valid drawable resource
     * @param maxWidth
     *         The maximum width of the thumbnail in pixels as an {@link Integer} value. The maximum
     *         width must be at least 1
     * @param maxHeight
     *         The maximum height of the thumbnail in pixels as an {@link Integer} value. The
     *         maximum height must be at least 1
     * @return The thumbnail, which has been loaded, as an instance of the class {@link Bitmap}
     * @throws IOException
     *         The exception, which is thrown, if an error occurs while decoding the image resource
     */
    public static Bitmap loadThumbnail(@NonNull Context context, @DrawableRes final int resourceId,
                                       final int maxWidth, final int maxHeight) throws IOException {
        Pair<Integer, Integer> imageDimensions = getImageDimensions(context, resourceId);
        int sampleSize = getSampleSize(imageDimensions, maxWidth, maxHeight);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;
        Bitmap thumbnail =
                BitmapFactory.decodeResource(context.getResources(), resourceId, options);

        if (thumbnail == null) {
            throw new IOException("Failed to decode image resource with id " + resourceId);
        }

        return thumbnail;
    }

    /**
     * Compresses a specific bitmap and stores it within a file.
     *
     * @param bitmap
     *         The bitmap, which should be compressed, as an instance of the class {@link Bitmap}.
     *         The bitmap may not be null
     * @param file
     *         The file, the bitmap should be compressed to, as an instance of the class {@link
     *         File}. The file may not be null
     * @param format
     *         The format, which should be used to compress the bitmap, as a value of the enum
     *         {@link CompressFormat}. The format must either be <code>JPEG</code>, <code>PNG</code>
     *         or <code>WEBP</code>
     * @param quality
     *         The quality, which should be used to compress the bitmap, as an {@link Integer}
     *         value. The quality must be at least 0 (lowest quality) and at maximum 100 (highest
     *         quality)
     * @throws FileNotFoundException
     *         The exception, which is thrown, if the given file could not be opened for writing
     * @throws IOException
     *         The exception, which is thrown, if an error occurs while compressing the bitmap
     */
    public static void compressToFile(@NonNull final Bitmap bitmap, @NonNull final File file,
                                      @NonNull final CompressFormat format, final int quality)
            throws IOException {
        ensureNotNull(bitmap, "The bitmap may not be null");
        ensureNotNull(file, "The file may not be null");
        ensureNotNull(format, "The format may not be null");
        ensureAtLeast(quality, 0, "The quality must be at least 0");
        ensureAtMaximum(quality, 100, "The quality must be at maximum 100");
        OutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(file);
            boolean result = bitmap.compress(format, quality, outputStream);

            if (!result) {
                throw new IOException(
                        "Failed to compress bitmap to file \"" + file + "\" using format " +
                                format + " and quality " + quality);
            }
        } finally {
            StreamUtil.close(outputStream);
        }
    }

    /**
     * Compresses a specific bitmap and returns it as a byte array.
     *
     * @param bitmap
     *         The bitmap, which should be compressed, as an instance of the class {@link Bitmap}.
     *         The bitmap may not be null
     * @param format
     *         The format, which should be used to compress the bitmap, as a value of the enum
     *         {@link CompressFormat}. The format must either be <code>JPEG</code>, <code>PNG</code>
     *         or <code>WEBP</code>
     * @param quality
     *         The quality, which should be used to compress the bitmap, as an {@link Integer}
     *         value. The quality must be at least 0 (lowest quality) and at maximum 100 (highest
     *         quality)
     * @return The byte array, the given bitmap has been compressed to, as a {@link Byte} array
     * @throws IOException
     *         The exception, which is thrown, if an error occurs while compressing the bitmap
     */
    public static byte[] compressToByteArray(@NonNull final Bitmap bitmap,
                                             @NonNull final CompressFormat format,
                                             final int quality) throws IOException {
        ensureNotNull(bitmap, "The bitmap may not be null");
        ensureNotNull(format, "The format may not be null");
        ensureAtLeast(quality, 0, "The quality must be at least 0");
        ensureAtMaximum(quality, 100, "The quality must be at maximum 100");
        ByteArrayOutputStream outputStream = null;

        try {
            outputStream = new ByteArrayOutputStream();
            boolean result = bitmap.compress(format, quality, outputStream);

            if (result) {
                return outputStream.toByteArray();
            }

            throw new IOException("Failed to compress bitmap to byte array using format " + format +
                    " and quality " + quality);
        } finally {
            StreamUtil.close(outputStream);
        }
    }

}