package org.citra.emu.settings.view;

import org.citra.emu.settings.model.FloatSetting;
import org.citra.emu.settings.model.IntSetting;
import org.citra.emu.settings.model.Setting;
import org.citra.emu.utils.Log;

public final class SliderSetting extends SettingsItem {
    private int mMin;
    private int mMax;
    private int mDefaultValue;

    private String mUnits;

    public SliderSetting(String key, String section, int titleId, int descriptionId,
                         int min, int max, String units, int defaultValue, Setting setting) {
        super(key, section, setting, titleId, descriptionId);
        mMin = min;
        mMax = max;
        mUnits = units;
        mDefaultValue = defaultValue;
    }

    public int getMin() {
        return mMin;
    }

    public int getMax() {
        return mMax;
    }

    public int getDefaultValue() {
        return mDefaultValue;
    }

    public int getSelectedValue() {
        Setting setting = getSetting();

        if (setting == null) {
            return mDefaultValue;
        }

        if (setting instanceof IntSetting) {
            IntSetting intSetting = (IntSetting) setting;
            return intSetting.getValue();
        } else if (setting instanceof FloatSetting) {
            FloatSetting floatSetting = (FloatSetting) setting;
            return Math.round(floatSetting.getValue());
        } else {
            Log.error("[SliderSetting] Error casting setting type.");
            return -1;
        }
    }

    /**
     * Write a value to the backing int. If that int was previously null,
     * initializes a new one and returns it, so it can be added to the Hashmap.
     *
     * @param selection New value of the int.
     * @return null if overwritten successfully otherwise; a newly created IntSetting.
     */
    public IntSetting setSelectedValue(int selection) {
        if (getSetting() == null) {
            IntSetting setting = new IntSetting(getKey(), getSection(), selection);
            setSetting(setting);
            return setting;
        } else {
            IntSetting setting = (IntSetting) getSetting();
            setting.setValue(selection);
            return null;
        }
    }

    /**
     * Write a value to the backing float. If that float was previously null,
     * initializes a new one and returns it, so it can be added to the Hashmap.
     *
     * @param selection New value of the float.
     * @return null if overwritten successfully otherwise; a newly created FloatSetting.
     */
    public FloatSetting setSelectedValue(float selection) {
        if (getSetting() == null) {
            FloatSetting setting = new FloatSetting(getKey(), getSection(), selection);
            setSetting(setting);
            return setting;
        } else {
            FloatSetting setting = (FloatSetting) getSetting();
            setting.setValue(selection);
            return null;
        }
    }

    public String getUnits() {
        return mUnits;
    }

    @Override
    public int getType() {
        return TYPE_SLIDER;
    }
}
