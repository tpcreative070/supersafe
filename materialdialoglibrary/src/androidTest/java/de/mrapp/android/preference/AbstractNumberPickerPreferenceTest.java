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
package de.mrapp.android.preference;

import android.content.Context;
import android.os.Build;
import android.os.Parcelable;
import android.test.AndroidTestCase;
import android.util.AttributeSet;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;

/**
 * Tests the functionality of the class {@link AbstractNumberPickerPreference}.
 *
 * @author Michael Rapp
 */
public class AbstractNumberPickerPreferenceTest extends AndroidTestCase {

    /**
     * Tests, if all properties are set correctly by the constructor, which expects a context as a
     * parameter.
     */
    public final void testConstructorWithContextParameter() {
        boolean defaultUseInputMethod = getContext().getResources()
                .getBoolean(R.bool.number_picker_preference_default_use_input_method);
        boolean defaultWrapSelectorWheel = getContext().getResources()
                .getBoolean(R.bool.number_picker_preference_default_wrap_selector_wheel);
        Context context = getContext();
        AbstractNumberPickerPreference numberPickerPreference =
                new AbstractNumberPickerPreferenceImplementation(context);
        assertEquals(0, numberPickerPreference.getNumber());
        assertEquals(defaultUseInputMethod, numberPickerPreference.isInputMethodUsed());
        assertEquals(defaultWrapSelectorWheel, numberPickerPreference.isSelectorWheelWrapped());
        assertNull(numberPickerPreference.getUnit());
    }

    /**
     * Tests, if all properties are set correctly by the constructor, which expects a context and an
     * attribute set as parameters.
     */
    public final void testConstructorWithContextAndAttributeSetParameters() {
        boolean defaultUseInputMethod = getContext().getResources()
                .getBoolean(R.bool.number_picker_preference_default_use_input_method);
        boolean defaultWrapSelectorWheel = getContext().getResources()
                .getBoolean(R.bool.number_picker_preference_default_wrap_selector_wheel);
        Context context = getContext();
        XmlPullParser xmlPullParser =
                context.getResources().getXml(R.xml.abstract_number_picker_preference);
        AttributeSet attributeSet = Xml.asAttributeSet(xmlPullParser);
        AbstractNumberPickerPreference numberPickerPreference =
                new AbstractNumberPickerPreferenceImplementation(context, attributeSet);
        assertEquals(0, numberPickerPreference.getNumber());
        assertEquals(defaultUseInputMethod, numberPickerPreference.isInputMethodUsed());
        assertEquals(defaultWrapSelectorWheel, numberPickerPreference.isSelectorWheelWrapped());
        assertNull(numberPickerPreference.getUnit());
    }

    /**
     * Tests, if all properties are set correctly by the constructor, which expects a context, an
     * attribute set and a default style as parameters.
     */
    public final void testConstructorWithContextAttributeSetAndDefaultStyleParameters() {
        boolean defaultUseInputMethod = getContext().getResources()
                .getBoolean(R.bool.number_picker_preference_default_use_input_method);
        boolean defaultWrapSelectorWheel = getContext().getResources()
                .getBoolean(R.bool.number_picker_preference_default_wrap_selector_wheel);
        Context context = getContext();
        int defaultStyle = 0;
        XmlPullParser xmlPullParser =
                context.getResources().getXml(R.xml.abstract_number_picker_preference);
        AttributeSet attributeSet = Xml.asAttributeSet(xmlPullParser);
        AbstractNumberPickerPreference numberPickerPreference =
                new AbstractNumberPickerPreferenceImplementation(context, attributeSet,
                        defaultStyle);
        assertEquals(0, numberPickerPreference.getNumber());
        assertEquals(defaultUseInputMethod, numberPickerPreference.isInputMethodUsed());
        assertEquals(defaultWrapSelectorWheel, numberPickerPreference.isSelectorWheelWrapped());
        assertNull(numberPickerPreference.getUnit());
    }

    /**
     * Tests, if all properties are set correctly by the constructor, which expects a context, an
     * attribute set, a default style and a default style attribute as parameters.
     */
    public final void testConstructorWithContextAttributeSetAndDefaultStyleAndDefaultStyleAttributeParameters() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean defaultUseInputMethod = getContext().getResources()
                    .getBoolean(R.bool.number_picker_preference_default_use_input_method);
            boolean defaultWrapSelectorWheel = getContext().getResources()
                    .getBoolean(R.bool.number_picker_preference_default_wrap_selector_wheel);
            Context context = getContext();
            int defaultStyle = 0;
            int defaultStyleAttribute = 0;
            XmlPullParser xmlPullParser =
                    context.getResources().getXml(R.xml.abstract_number_picker_preference);
            AttributeSet attributeSet = Xml.asAttributeSet(xmlPullParser);
            AbstractNumberPickerPreference numberPickerPreference =
                    new AbstractNumberPickerPreferenceImplementation(context, attributeSet,
                            defaultStyle, defaultStyleAttribute);
            assertEquals(0, numberPickerPreference.getNumber());
            assertEquals(defaultUseInputMethod, numberPickerPreference.isInputMethodUsed());
            assertEquals(defaultWrapSelectorWheel, numberPickerPreference.isSelectorWheelWrapped());
            assertNull(numberPickerPreference.getUnit());
        }
    }

    /**
     * Tests the functionality of the method, which allows to set the preference's number.
     */
    public final void testSetNumber() {
        int number = 2;
        AbstractNumberPickerPreference numberPickerPreference =
                new AbstractNumberPickerPreferenceImplementation(getContext());
        numberPickerPreference.showValueAsSummary(true);
        numberPickerPreference.setNumber(number);
        assertEquals(number, numberPickerPreference.getNumber());
        assertEquals(Integer.toString(number), numberPickerPreference.getSummary());
    }

    /**
     * Tests the functionality of the method, which allows to set, whether an input method should be
     * used, or not.
     */
    public final void testUseInputMethod() {
        boolean useInputMethod = true;
        AbstractNumberPickerPreference numberPickerPreference =
                new AbstractNumberPickerPreferenceImplementation(getContext());
        numberPickerPreference.useInputMethod(useInputMethod);
        assertEquals(useInputMethod, numberPickerPreference.isInputMethodUsed());
    }

    /**
     * Tests the functionality of the method, which allows to set, whether the selector wheel should
     * be wrapped, or not.
     */
    public final void testWrapSelectorWheel() {
        boolean wrapSelectorWheel = false;
        AbstractNumberPickerPreference numberPickerPreference =
                new AbstractNumberPickerPreferenceImplementation(getContext());
        numberPickerPreference.wrapSelectorWheel(wrapSelectorWheel);
        assertEquals(wrapSelectorWheel, numberPickerPreference.isSelectorWheelWrapped());
    }

    /**
     * Tests the functionality of the method, which allows to set the unit, which should be used for
     * textual representation of the preference's number.
     */
    public final void testSetUnit() {
        CharSequence unit = "unit";
        AbstractNumberPickerPreference numberPickerPreference =
                new AbstractNumberPickerPreferenceImplementation(getContext());
        numberPickerPreference.setUnit(unit);
        assertEquals(unit, numberPickerPreference.getUnit());
    }

    /**
     * Tests the functionality of the method, which allows to set the unit, which should be used for
     * textual representation of the preference's number, and expects a resource id as a parameter.
     */
    public final void setUnitWithResourceIdParameter() {
        CharSequence unit = getContext().getText(android.R.string.cancel);
        AbstractNumberPickerPreference numberPickerPreference =
                new AbstractNumberPickerPreferenceImplementation(getContext());
        numberPickerPreference.setUnit(unit);
        assertEquals(unit, numberPickerPreference.getUnit());
    }

    /**
     * Tests the functionality of the onSaveInstanceState-method.
     */
    public final void testOnSaveInstanceState() {
        int number = 2;
        AbstractNumberPickerPreference numberPickerPreference =
                new AbstractNumberPickerPreferenceImplementation(getContext());
        numberPickerPreference.setNumber(number);
        numberPickerPreference.setPersistent(false);
        AbstractNumberPickerPreference.SavedState savedState =
                (AbstractNumberPickerPreference.SavedState) numberPickerPreference
                        .onSaveInstanceState();
        assertEquals(number, savedState.number);
    }

    /**
     * Tests the functionality of the onRestoreInstanceState-method.
     */
    public final void testOnRestoreInstanceState() {
        int number = 2;
        AbstractNumberPickerPreference numberPickerPreference =
                new AbstractNumberPickerPreferenceImplementation(getContext());
        numberPickerPreference.setNumber(number);
        numberPickerPreference.setPersistent(false);
        Parcelable parcelable = numberPickerPreference.onSaveInstanceState();
        AbstractNumberPickerPreference restoredNumberPickerPreference =
                new AbstractNumberPickerPreferenceImplementation(getContext());
        restoredNumberPickerPreference.onRestoreInstanceState(parcelable);
        assertEquals(number, restoredNumberPickerPreference.getNumber());
    }

}