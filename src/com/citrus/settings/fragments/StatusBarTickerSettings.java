/*
 * Copyright (C) 2015 The VRToxin Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.citrus.settings.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.citrus.settings.preference.CustomSeekBarPreference;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBarTickerSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String SHOW_TICKER =
            "status_bar_show_ticker";
    private static final String CAT_COLORS =
            "ticker_colors";
    private static final String TEXT_COLOR =
            "status_bar_ticker_text_color";
    private static final String ICON_COLOR =
            "status_bar_ticker_icon_color";
    private static final String STATUS_BAR_TICKER_FONT_STYLE =
            "status_bar_ticker_font_style";
    private static final String STATUS_BAR_TICKER_FONT_SIZE  =
            "status_bar_ticker_font_size";

    private static final int WHITE                  = 0xffffffff;
    private static final int CITRUS_GREEN           = 0xff7CB342;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DLG_RESET  = 0;

    private SwitchPreference mShowTicker;
    private ColorPickerPreference mTextColor;
    private ColorPickerPreference mIconColor;
    private CustomSeekBarPreference mTickerFontSize;
    private ListPreference mTickerFontStyle;

    private ContentResolver mResolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refreshSettings();
    }

    public void refreshSettings() {
        PreferenceScreen prefs = getPreferenceScreen();
        if (prefs != null) {
            prefs.removeAll();
        }

        addPreferencesFromResource(R.xml.ticker);
        mResolver = getActivity().getContentResolver();

        int intColor;
        String hexColor;

        boolean showTicker = Settings.System.getInt(mResolver,
                Settings.System.STATUS_BAR_SHOW_TICKER, 0) == 1;

        mShowTicker =
                (SwitchPreference) findPreference(SHOW_TICKER);
        mShowTicker.setChecked(showTicker);
        mShowTicker.setOnPreferenceChangeListener(this);

        PreferenceCategory catColors =
                (PreferenceCategory) findPreference(CAT_COLORS);

        if (showTicker) {
            mTextColor =
                    (ColorPickerPreference) findPreference(TEXT_COLOR);
            intColor = Settings.System.getInt(mResolver,
                    Settings.System.STATUS_BAR_TICKER_TEXT_COLOR,
                    WHITE);
            mTextColor.setNewPreviewColor(intColor);
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mTextColor.setSummary(hexColor);
            mTextColor.setNewPreviewColor(intColor);
            mTextColor.setOnPreferenceChangeListener(this);

            mIconColor =
                    (ColorPickerPreference) findPreference(ICON_COLOR);
            intColor = Settings.System.getInt(mResolver,
                    Settings.System.STATUS_BAR_TICKER_ICON_COLOR,
                    WHITE);
            mIconColor.setNewPreviewColor(intColor);
            String iconHexColor = String.format("#%08x", (0xffffffff & intColor));
            mIconColor.setSummary(hexColor);
            mIconColor.setSummary(iconHexColor);
            mIconColor.setOnPreferenceChangeListener(this);

            mTickerFontSize =
                     (CustomSeekBarPreference) findPreference(STATUS_BAR_TICKER_FONT_SIZE);
            mTickerFontSize.setValue(Settings.System.getInt(getActivity().getContentResolver(),
                      Settings.System.STATUS_BAR_TICKER_FONT_SIZE, 14));
            mTickerFontSize.setOnPreferenceChangeListener(this);

            mTickerFontStyle = (ListPreference) findPreference(STATUS_BAR_TICKER_FONT_STYLE);
            mTickerFontStyle.setOnPreferenceChangeListener(this);
            mTickerFontStyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                    .getContentResolver(), Settings.System.STATUS_BAR_TICKER_FONT_STYLE, 0)));
            mTickerFontStyle.setSummary(mTickerFontStyle.getEntry());
        } else {
            removePreference("status_bar_ticker_font_size");
            removePreference(CAT_COLORS);
            removePreference("status_bar_ticker_font_style");
        }

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(R.drawable.ic_action_reset)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                showDialogInner(DLG_RESET);
                return true;
             default:
                return super.onContextItemSelected(item);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String hex;
        int intHex;

        if (preference == mShowTicker) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_SHOW_TICKER,
                    value ? 1 : 0);
            refreshSettings();
            return true;
        } else if (preference == mTextColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_TICKER_TEXT_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mIconColor) {
            hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                    Settings.System.STATUS_BAR_TICKER_ICON_COLOR,
                    intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mTickerFontSize) {
             int width = ((Integer)newValue).intValue();
              Settings.System.putInt(getActivity().getContentResolver(),
                      Settings.System.STATUS_BAR_TICKER_FONT_SIZE, width);
              return true;
        } else if (preference == mTickerFontStyle) {
            int val = Integer.parseInt((String) newValue);
            int index = mTickerFontStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_TICKER_FONT_STYLE, val);
            mTickerFontStyle.setSummary(mTickerFontStyle.getEntries()[index]);
            return true;
        }
        return false;
    }

    private void showDialogInner(int id) {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(id);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int id) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            frag.setArguments(args);
            return frag;
        }

        StatusBarTickerSettings getOwner() {
            return (StatusBarTickerSettings) getTargetFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            switch (id) {
                case DLG_RESET:
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.reset)
                    .setMessage(R.string.reset_colors)
                    .setNegativeButton(R.string.cancel, null)
                    .setNeutralButton(R.string.reset_android,
                            new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_SHOW_TICKER, 0);
                           Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_TICKER_FONT_SIZE, 14);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_TICKER_FONT_STYLE, 0);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_TICKER_TEXT_COLOR,
                                    WHITE);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_TICKER_ICON_COLOR,
                                    WHITE);
                            getOwner().refreshSettings();
                        }
                    })
                    .setPositiveButton(R.string.reset_citrus,
                            new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_SHOW_TICKER, 1);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_TICKER_FONT_SIZE, 16);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_TICKER_FONT_STYLE, 24);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_TICKER_TEXT_COLOR,
                                    CITRUS_GREEN);
                            Settings.System.putInt(getOwner().mResolver,
                                    Settings.System.STATUS_BAR_TICKER_ICON_COLOR,
                                    CITRUS_GREEN);
                            getOwner().refreshSettings();
                        }
                    })
                    .create();
            }
            throw new IllegalArgumentException("unknown id " + id);
        }

        @Override
        public void onCancel(DialogInterface dialog) {

        }
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.CUSTOM_SQUASH;
    }
}
