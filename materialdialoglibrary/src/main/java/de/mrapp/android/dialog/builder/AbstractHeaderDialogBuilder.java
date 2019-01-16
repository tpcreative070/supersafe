/*
 * Copyright 2014 - 2018 Michael Rapp
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
package de.mrapp.android.dialog.builder;
import de.mrapp.android.preference.R;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.support.annotation.CallSuper;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.content.ContextCompat;
import android.view.View;
import de.mrapp.android.preference.R;
import de.mrapp.android.dialog.model.HeaderDialog;
import de.mrapp.android.util.ThemeUtil;

/**
 * An abstract base class for all builders, which allow to create and show dialogs, which are
 * designed according to Android 5's Material Design guidelines even on pre-Lollipop devices and may
 * contain a header.
 *
 * @param <DialogType>
 *         The type of the dialog, which is created by the builder
 * @param <BuilderType>
 *         The type of the builder
 * @author Michael Rapp
 * @since 3.3.0
 */
public abstract class AbstractHeaderDialogBuilder<DialogType extends HeaderDialog, BuilderType extends AbstractHeaderDialogBuilder<DialogType, ?>>
        extends AbstractMaterialDialogBuilder<DialogType, BuilderType> {

    /**
     * Obtains, whether the dialog's header should be shown, or not, from a specific theme.
     *
     * @param themeResourceId
     *         The resource id of the theme, the visibility should be obtained from, as an {@link
     *         Integer} value
     */
    private void obtainShowHeader(@StyleRes final int themeResourceId) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(themeResourceId,
                new int[]{R.attr.materialDialogShowHeader});
        showHeader(typedArray.getBoolean(0, false));
    }

    /**
     * Obtains the height of the dialog's header from a specific theme.
     *
     * @param themeResourceId
     *         The resource id of the theme, the height should be obtained from, as an {@link
     *         Integer} value
     */
    private void obtainHeaderHeight(@StyleRes final int themeResourceId) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(themeResourceId,
                new int[]{R.attr.materialDialogHeaderHeight});
        int defaultHeight =
                getContext().getResources().getDimensionPixelSize(R.dimen.dialog_header_height);
        setHeaderHeight(typedArray.getDimensionPixelSize(0, defaultHeight));
    }

    /**
     * Obtains the background of the dialog's header from a specific theme.
     *
     * @param themeResourceId
     *         The resource id of the theme, the background should be obtained from, as an {@link
     *         Integer} value
     */
    private void obtainHeaderBackground(@StyleRes final int themeResourceId) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(themeResourceId,
                new int[]{R.attr.materialDialogHeaderBackground});
        int resourceId = typedArray.getResourceId(0, 0);

        if (resourceId != 0) {
            setHeaderBackground(resourceId);
        } else {
            setHeaderBackgroundColor(
                    ThemeUtil.getColor(getContext(), themeResourceId, R.attr.colorPrimary));
        }
    }

    /**
     * Obtains the icon of the dialog's header from a specific theme.
     *
     * @param themeResourceId
     *         The resource id of the theme, the icon should be obtained from, as an {@link Integer}
     *         value
     */
    private void obtainHeaderIcon(@StyleRes final int themeResourceId) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(themeResourceId,
                new int[]{R.attr.materialDialogHeaderIcon});
        int resourceId = typedArray.getResourceId(0, 0);

        if (resourceId != 0) {
            setHeaderIcon(resourceId);
        }
    }

    /**
     * Obtains the color state list, which is used to tint the header icon of the dialog, from a
     * specific theme.
     *
     * @param themeResourceId
     *         The resource id of the theme, the color state list should be obtained from, as an
     *         {@link Integer} value
     */
    private void obtainHeaderIconTintList(@StyleRes final int themeResourceId) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(themeResourceId,
                new int[]{R.attr.materialDialogHeaderIconTint});
        setHeaderIconTintList(typedArray.getColorStateList(0));
    }

    /**
     * Obtains the color of the divider of the dialog's header from a specific theme.
     *
     * @param themeResourceId
     *         The resource id of the theme, the color should be obtained from, as an {@link
     *         Integer} value
     */
    private void obtainHeaderDividerColor(@StyleRes final int themeResourceId) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(themeResourceId,
                new int[]{R.attr.materialDialogHeaderDividerColor});
        int defaultColor = ContextCompat.getColor(getContext(), R.color.header_divider_color);
        setHeaderDividerColor(typedArray.getColor(0, defaultColor));
    }

    /**
     * Obtains, whether the divider of the dialog's header should be shown, or not, from a specific
     * theme.
     *
     * @param themeResourceId
     *         The resource id of the theme, the visibility should be obtained from, as an {@link
     *         Integer} value
     */
    private void obtainShowHeaderDivider(@StyleRes final int themeResourceId) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(themeResourceId,
                new int[]{R.attr.materialDialogShowHeaderDivider});
        showHeaderDivider(typedArray.getBoolean(0, true));
    }

    /**
     * Creates a new builder, which allows to create dialogs, which are designed according to
     * Android 5's Material Design guidelines even on pre-Lollipop devices and may contain a
     * header.
     *
     * @param context
     *         The context, which should be used by the builder, as an instance of the class {@link
     *         Context}. The context may not be null
     */
    public AbstractHeaderDialogBuilder(@NonNull final Context context) {
        super(context);
    }

    /**
     * Creates a new builder, which allows to create dialogs, which are designed according to
     * Android 5's Material Design guidelines even on pre-Lollipop devices and may contain a
     * header.
     *
     * @param context
     *         The context, which should be used by the builder, as an instance of the class {@link
     *         Context}. The context may not be null
     * @param themeResourceId
     *         The resource id of the theme, which should be used by the dialog, as an {@link
     *         Integer} value. The resource id must correspond to a valid theme
     */
    public AbstractHeaderDialogBuilder(@NonNull final Context context,
                                       @StyleRes final int themeResourceId) {
        super(context, themeResourceId);
    }

    /**
     * Sets, whether the header of the dialog, which is created by the builder, should be shown, or
     * not.
     *
     * @param show
     *         True, if the header of the dialog should be shown, false otherwise
     * @return The builder, the method has been called upon, as an instance of the generic type
     * BuilderType
     */
    public final BuilderType showHeader(final boolean show) {
        getProduct().showHeader(show);
        return self();
    }

    /**
     * Sets the custom view, which should be used to show the header of the dialog, which is created
     * by the builder.
     *
     * @param view
     *         The view, which should be set, as an instance of the class {@link View} or null, if
     *         no custom view should be used to show the header
     * @return The builder, the method has been called upon, as an instance of the generic type
     * BuilderType
     */
    public final BuilderType setCustomHeader(@Nullable final View view) {
        getProduct().setCustomHeader(view);
        return self();
    }

    /**
     * Sets the custom view, which should be used to show the header of the dialog, which is created
     * by the builder.
     *
     * @param resourceId
     *         The resource id of the view, which should be set, as an {@link Integer} value. The
     *         resource id must correspond to a valid layout resource
     * @return The builder, the method has been called upon, as an instance of the generic type
     * BuilderType
     */
    public final BuilderType setCustomHeader(@LayoutRes final int resourceId) {
        getProduct().setCustomHeader(resourceId);
        return self();
    }

    /**
     * Sets the height of the header of the dialog, which is created by the builder.
     *
     * @param height
     *         The height, which should be set, in pixels as an {@link Integer} value. The height
     *         must be at least 0
     * @return The builder, the method has been called upon, as an instance of the generic type
     * BuilderType
     */
    public final BuilderType setHeaderHeight(final int height) {
        getProduct().setHeaderHeight(height);
        return self();
    }

    /**
     * Sets the background color of the header of the dialog, which is created by the builder.
     *
     * @param color
     *         The background color, which should be set, as an {@link Integer} value
     * @return The builder, the method has been called upon, as an instance of the generic type
     * BuilderType
     */
    public final BuilderType setHeaderBackgroundColor(@ColorInt final int color) {
        getProduct().setHeaderBackgroundColor(color);
        return self();
    }

    /**
     * Sets the background of the header of the dialog, which is created by the builder.
     *
     * @param resourceId
     *         The resource id of the background, which should be set, as an {@link Integer} value.
     *         The resource id must correspond to a valid drawable resource
     * @return The builder, the method has been called upon, as an instance of the generic type
     * BuilderType
     */
    public final BuilderType setHeaderBackground(@DrawableRes final int resourceId) {
        getProduct().setHeaderBackground(resourceId);
        return self();
    }

    /**
     * Sets the background of the header of the dialog, which is created by the builder.
     *
     * @param background
     *         The background, which should be set, as an instance of the class {@link Bitmap}. The
     *         background may not be null
     * @return The builder, the method has been called upon, as an instance of the generic type
     * BuilderType
     */
    public final BuilderType setHeaderBackground(@NonNull final Bitmap background) {
        getProduct().setHeaderBackground(background);
        return self();
    }

    /**
     * Sets the icon of the header of the dialog, which is created by the builder.
     *
     * @param resourceId
     *         The resource id of the icon, which should be set, as an {@link Integer} value. The
     *         resource id must correspond to a valid drawable resource
     * @return The builder, the method has been called upon, as an instance of the generic type
     * BuilderType
     */
    public final BuilderType setHeaderIcon(@DrawableRes final int resourceId) {
        getProduct().setHeaderIcon(resourceId);
        return self();
    }

    /**
     * Sets the icon of the header of the dialog, which is created by the builder.
     *
     * @param icon
     *         The icon, which should be set, as an instance of the class {@link Bitmap} or null, if
     *         no icon should be set
     * @return The builder, the method has been called upon, as an instance of the generic type
     * BuilderType
     */
    public final BuilderType setHeaderIcon(@Nullable final Bitmap icon) {
        getProduct().setHeaderIcon(icon);
        return self();
    }

    /**
     * Sets the color, which should be used to tint the header icon of the dialog, which is created
     * by the builder.
     *
     * @param color
     *         The color, which should be set, as an {@link Integer} value
     * @return The builder, the method has been called upon, as an instance of the generic type
     * BuilderType
     */
    public final BuilderType setHeaderIconTint(@ColorInt final int color) {
        getProduct().setHeaderIconTint(color);
        return self();
    }

    /**
     * Sets the color state list, which should be used to tint the header icon of the dialog, which
     * is created by the builder.
     *
     * @param tintList
     *         The color state list, which should be set, as an instance of the class {@link
     *         ColorStateList} or null, if no color state list should be set
     * @return The builder, the method has been called upon, as an instance of the generic type
     * BuilderType
     */
    public final BuilderType setHeaderIconTintList(@Nullable final ColorStateList tintList) {
        getProduct().setHeaderIconTintList(tintList);
        return self();
    }

    /**
     * Sets the mode, which should be used to tint the icon of the dialog, which is created by the
     * builder.
     *
     * @param mode
     *         The mode, which should be set, as a value of the enum {@link PorterDuff.Mode}. The
     *         mode may not be null
     * @return The builder, the method has been called upon, as an instance of the generic type
     * BuilderType
     */
    public final BuilderType setHeaderIconTintMode(@NonNull final PorterDuff.Mode mode) {
        getProduct().setHeaderIconTintMode(mode);
        return self();
    }

    /**
     * Sets the color of the divider of the header of the dialog, which is created by the builder.
     *
     * @param color
     *         The color, which should be set, as an {@link Integer} value
     * @return The builder, the method has been called upon, as an instance of the generic type
     * BuilderType
     */
    public final BuilderType setHeaderDividerColor(@ColorInt final int color) {
        getProduct().setHeaderDividerColor(color);
        return self();
    }

    /**
     * Sets, wehther the divider of the header of the dialog, which is created by the builder,
     * should be shown, or not.
     *
     * @param show
     *         True, if the divider of the dialog's header should be shown, false otherwise
     * @return The builder the method has been called upon, as an instance of the generic type
     * BuilderType
     */
    public final BuilderType showHeaderDivider(final boolean show) {
        getProduct().showHeaderDivider(show);
        return self();
    }

    @CallSuper
    @Override
    protected void obtainStyledAttributes(@StyleRes final int themeResourceId) {
        super.obtainStyledAttributes(themeResourceId);
        obtainShowHeader(themeResourceId);
        obtainHeaderHeight(themeResourceId);
        obtainHeaderBackground(themeResourceId);
        obtainHeaderIcon(themeResourceId);
        obtainHeaderIconTintList(themeResourceId);
        obtainHeaderDividerColor(themeResourceId);
        obtainShowHeaderDivider(themeResourceId);
    }

}