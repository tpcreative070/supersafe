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
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.view.View;
import androidx.annotation.CallSuper;
import androidx.annotation.ColorInt;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;
import de.mrapp.android.dialog.model.ButtonBarDialog;
import de.mrapp.android.util.ThemeUtil;

/**
 * An abstract base class for all builders, which allow to create and show dialogs, which are
 * designed according to Android Material Design guidelines even on pre-Lollipop devices and may
 * contain up to three buttons.
 *
 * @param <DialogType>
 *         The type of the dialog, which is created by the builder
 * @param <BuilderType>
 *         The type of the builder
 * @author Michael Rapp
 * @since 3.3.0
 */
public abstract class AbstractButtonBarDialogBuilder<DialogType extends ButtonBarDialog, BuilderType extends AbstractButtonBarDialogBuilder<DialogType, ?>>
        extends AbstractValidateableDialogBuilder<DialogType, BuilderType> {

    /**
     * Obtains the button text color from a specific theme.
     *
     * @param themeResourceId
     *         The resource id of the theme, the color should be obtained from, as an {@link
     *         Integer} value
     */
    private void obtainButtonTextColor(@StyleRes final int themeResourceId) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(themeResourceId,
                new int[]{R.attr.materialDialogButtonTextColor});
        int defaultColor = ThemeUtil.getColor(getContext(), themeResourceId, R.attr.colorAccent);
        setButtonTextColor(typedArray.getColor(0, defaultColor));
    }

    /**
     * Obtains the disabled button text color from a specific theme.
     *
     * @param themeResourceId
     *         The resource id of the theme, the color should be obtained from, as an {@link
     *         Integer} value
     */
    private void obtainDisabledButtonTextColor(@StyleRes final int themeResourceId) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(themeResourceId,
                new int[]{R.attr.materialDialogDisabledButtonTextColor});
        int defaultColor = ContextCompat
                .getColor(getContext(), R.color.dialog_button_disabled_text_color_light);
        setDisabledButtonTextColor(typedArray.getColor(0, defaultColor));
    }

    /**
     * Obtains, whether the divider, which is located above the dialog's buttons, should be shown,
     * or not, from a specific theme.
     *
     * @param themeResourceId
     *         The resource id of the theme, the visibility should be obtained from, as an {@link
     *         Integer} value
     */
    private void obtainShowButtonBarDivider(@StyleRes final int themeResourceId) {
        TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(themeResourceId,
                new int[]{R.attr.materialDialogShowButtonBarDivider});
        showButtonBarDivider(typedArray.getBoolean(0, false));
    }

    /**
     * Creates a new builder, which allows to create dialogs, which allow to create and show
     * dialogs, which are designed according to Android 5's Material Design guidelines even on
     * pre-Lollipop devices and may contain up to three buttons.
     *
     * @param context
     *         The context, which should be used by the builder, as an instance of the class {@link
     *         Context}. The context may not be null
     */
    public AbstractButtonBarDialogBuilder(@NonNull final Context context) {
        super(context);
    }

    /**
     * Creates a new builder, which allows to create dialogs, which allow to create and show
     * dialogs, which are designed according to Android 5's Material Design guidelines even on
     * pre-Lollipop devices and may contain up to three buttons.
     *
     * @param context
     *         The context, which should be used by the builder, as an instance of the class {@link
     *         Context}. The context may not be null
     * @param themeResourceId
     *         The resource id of the theme, which should be used by the dialog, as an {@link
     *         Integer} value. The resource id must correspond to a valid theme
     */
    public AbstractButtonBarDialogBuilder(@NonNull final Context context,
                                          @StyleRes final int themeResourceId) {
        super(context, themeResourceId);
    }

    /**
     * Sets the text color of the buttons of the dialog, which is created by the builder.
     *
     * @param color
     *         The color, which should be set, as an {@link Integer} value
     * @return The builder, the method has been called upon, as an instance of the generic type
     * BuilderType
     */
    public final BuilderType setButtonTextColor(@ColorInt final int color) {
        getProduct().setButtonTextColor(color);
        return self();
    }

    /**
     * Sets the text color of the buttons of the dialog, which is created by the builder, when
     * disabled.
     *
     * @param color
     *         The color, which should be set, as an {@link Integer} value
     * @return The builder, the method has been called upon, as an instance of the generic type
     * BuilderType
     */
    public final BuilderType setDisabledButtonTextColor(@ColorInt final int color) {
        getProduct().setDisabledButtonTextColor(color);
        return self();
    }

    /**
     * Sets, whether the buttons of the dialog, which is created by the builder, should be aligned
     * vertically, or not.
     *
     * @param stackButtons
     *         True, if the buttons of the dialog, which is created by the builder, should be
     *         aligned vertically, false otherwise
     * @return The builder, the method has been called upon, as an instance of the generic type
     * BuilderType
     */
    public final BuilderType stackButtons(final boolean stackButtons) {
        getProduct().stackButtons(stackButtons);
        return self();
    }

    /**
     * Sets the text of the negative button of the dialog, which is created by the builder.
     *
     * Note, that the attached listener is not stored using a dialog's
     * <code>onSaveInstanceState</code>-method, because it is not serializable. Therefore this
     * method must be called again after configuration changes, e.g when the orientation of the
     * device has changed, in order to re-register the listener.
     *
     * @param text
     *         The text, which should be set, as an instance of the type {@link CharSequence} or
     *         null, if no negative button should be shown
     * @param listener
     *         The listener, which should be notified, when the negative button is clicked, as an
     *         instance of the type {@link DialogInterface.OnClickListener} or null, if no listener
     *         should be notified
     * @return The builder, the method has been called upon, as an instance of the generic type
     * BuilderType
     */
    public final BuilderType setNegativeButton(@Nullable final CharSequence text,
                                               @Nullable final DialogInterface.OnClickListener listener) {
        getProduct().setNegativeButton(text, listener);
        return self();
    }

    /**
     * Sets the text of the negative button of the dialog, which is created by the builder.
     *
     * Note, that the attached listener is not stored using a dialog's
     * <code>onSaveInstanceState</code>-method, because it is not serializable. Therefore this
     * method must be called again after configuration changes, e.g when the orientation of the
     * device has changed, in order to re-register the listener.
     *
     * @param resourceId
     *         The resource id of the text, which should be set, as an {@link Integer} value. The
     *         resource id must correspond to a valid string resource
     * @param listener
     *         The listener, which should be notified, when the negative button is clicked, as an
     *         instance of the type {@link DialogInterface.OnClickListener} or null, if no listener
     *         should be notified
     * @return The builder, the method has been called upon, as an instance of the generic type
     * BuilderType
     */
    public final BuilderType setNegativeButton(@StringRes final int resourceId,
                                               @Nullable final DialogInterface.OnClickListener listener) {
        getProduct().setNegativeButton(resourceId, listener);
        return self();
    }

    /**
     * Sets the text of the positive button of the dialog, which is created by the builder.
     *
     * Note, that the attached listener is not stored using a dialog's
     * <code>onSaveInstanceState</code>-method, because it is not serializable. Therefore this
     * method must be called again after configuration changes, e.g when the orientation of the
     * device has changed, in order to re-register the listener.
     *
     * @param text
     *         The text, which should be set, as an instance of the type {@link CharSequence} or
     *         null, if no positive button should be shown
     * @param listener
     *         The listener, which should be notified, when the positive button is clicked, as an
     *         instance of the type {@link DialogInterface.OnClickListener} or null, if no listener
     *         should be notified
     * @return The builder, the method has been called upon, as an instance of the generic type
     * BuilderType
     */
    public final BuilderType setPositiveButton(@Nullable final CharSequence text,
                                               @Nullable final DialogInterface.OnClickListener listener) {
        getProduct().setPositiveButton(text, listener);
        return self();
    }

    /**
     * Sets the text of the positive button of the dialog, which is created by the builder.
     *
     * Note, that the attached listener is not stored using a dialog's
     * <code>onSaveInstanceState</code>-method, because it is not serializable. Therefore this
     * method must be called again after configuration changes, e.g when the orientation of the
     * device has changed, in order to re-register the listener.
     *
     * @param resourceId
     *         The resource id of the text, which should be set, as an {@link Integer} value. The
     *         resource id must correspond to a valid string resource
     * @param listener
     *         The listener, which should be notified, when the positive button is clicked, as an
     *         instance of the type {@link DialogInterface.OnClickListener} or null, if no listener
     *         should be notified
     * @return The builder, the method has been called upon, as an instance of the generic type
     * BuilderType
     */
    public final BuilderType setPositiveButton(@StringRes final int resourceId,
                                               @Nullable final DialogInterface.OnClickListener listener) {
        getProduct().setPositiveButton(resourceId, listener);
        return self();
    }

    /**
     * Sets the text of the neutral button of the dialog, which is created by the builder.
     *
     * Note, that the attached listener is not stored using a dialog's
     * <code>onSaveInstanceState</code>-method, because it is not serializable. Therefore this
     * method must be called again after configuration changes, e.g when the orientation of the
     * device has changed, in order to re-register the listener.
     *
     * @param text
     *         The text, which should be set, as an instance of the type {@link CharSequence} or
     *         null, if no neutral button should be shown
     * @param listener
     *         The listener, which should be notified, when the neutral button is clicked, as an
     *         instance of the type {@link DialogInterface.OnClickListener} or null, if no listener
     *         should be notified
     * @return The builder, the method has been called upon, as an instance of the generic type
     * BuilderType
     */
    public final BuilderType setNeutralButton(@Nullable final CharSequence text,
                                              @Nullable final DialogInterface.OnClickListener listener) {
        getProduct().setNeutralButton(text, listener);
        return self();
    }

    /**
     * Sets the text of the neutral button of the dialog, which is created by the builder.
     *
     * Note, that the attached listener is not stored using a dialog's
     * <code>onSaveInstanceState</code>-method, because it is not serializable. Therefore this
     * method must be called again after configuration changes, e.g when the orientation of the
     * device has changed, in order to re-register the listener.
     *
     * @param resourceId
     *         The resource id of the text, which should be set, as an {@link Integer} value. The
     *         resource id must correspond to a valid string resource
     * @param listener
     *         The listener, which should be notified, when the neutral button is clicked, as an
     *         instance of the type {@link DialogInterface.OnClickListener} or null, if no listener
     *         should be notified
     * @return The builder, the method has been called upon, as an instance of the generic type
     * BuilderType
     */
    public final BuilderType setNeutralButton(@StringRes final int resourceId,
                                              @Nullable final DialogInterface.OnClickListener listener) {
        getProduct().setNeutralButton(resourceId, listener);
        return self();
    }

    /**
     * Sets, whether the divider, which is located above the buttons of the dialog, which is created
     * by the builder, should be shown, or not.
     *
     * @param show
     *         True, if the divider, which is located above the dialog's buttons, should be show,
     *         false otherwise
     * @return The builder, the method has been called upon, as an instance of the generic type
     * BuilderType
     */
    public final BuilderType showButtonBarDivider(final boolean show) {
        getProduct().showButtonBarDivider(show);
        return self();
    }

    /**
     * Sets the custom view, which should be used to show the buttons of the dialog, which is
     * created by the builder.
     *
     * @param resourceId
     *         The resource id of the view, which should be set, as an {@link Integer} value. The
     *         resource id must correspond to a valid layout resource
     * @return The builder, the method has been called upon, as an instance of the generic type
     * BuilderType
     */
    public final BuilderType setCustomButtonBar(@LayoutRes int resourceId) {
        getProduct().setCustomButtonBar(resourceId);
        return self();
    }

    /**
     * Sets the custom view, which should be used to show the buttons of the dialog, which is
     * created by the builder.
     *
     * @param view
     *         The view, which should be set, as an instance of the class {@link View} or null, if
     *         no custom view should be used to show the title
     * @return The builder, the method has been called upon, as an instance of the generic type
     * BuilderType
     */
    public final BuilderType setCustomButtonBar(@Nullable View view) {
        getProduct().setCustomButtonBar(view);
        return self();
    }

    @CallSuper
    @Override
    protected void obtainStyledAttributes(@StyleRes final int themeResourceId) {
        super.obtainStyledAttributes(themeResourceId);
        obtainButtonTextColor(themeResourceId);
        obtainDisabledButtonTextColor(themeResourceId);
        obtainShowButtonBarDivider(themeResourceId);
    }

}