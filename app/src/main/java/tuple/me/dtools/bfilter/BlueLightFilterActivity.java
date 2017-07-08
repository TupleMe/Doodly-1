package tuple.me.dtools.bfilter;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.SeekBar;

import java.util.Arrays;

import tuple.me.dtools.R;
import tuple.me.dtools.base.BaseActivity;
import tuple.me.dtools.constants.PrefConstants;
import tuple.me.lily.Contexter;
import tuple.me.lily.ThemeEngine;
import tuple.me.lily.adapters.ColorAdapter;
import tuple.me.lily.views.CustomTextView;

/**
 * Blue light filter module.
 */
public class BlueLightFilterActivity extends BaseActivity {
    boolean isEnabled = false;
    int alpha;
    int color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blue_light_filter);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //noinspection ConstantConditions
        getSupportActionBar().setTitle(R.string.screen_filter);
        getSupportActionBar().setBackgroundDrawable(Contexter.getColorDrawable(R.color.colorPrimaryDark));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        isEnabled = prefs.getBoolean(PrefConstants.BLUE_LIGHT_ENABLED, false);
        alpha = prefs.getInt(PrefConstants.BLUE_LIGHT_OPACITY, 60);
        color = prefs.getInt(PrefConstants.BLUE_LIGHT_COLOR, 5);
        setFilter();
        final CustomTextView opacityLevel = (CustomTextView) findViewById(R.id.opacity_level);

        //noinspection AndroidLintSetTextI18n
        opacityLevel.setText(alpha + "%");

        SwitchCompat onOff = (SwitchCompat) findViewById(R.id.on_off);
        onOff.setChecked(isEnabled);
        onOff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.set(PrefConstants.BLUE_LIGHT_ENABLED, isChecked);
                isEnabled = isChecked;
                setFilter();
            }
        });
        GridView grid = (GridView) findViewById(R.id.color_grid);
        ColorAdapter adapter = new ColorAdapter(this, Arrays.asList(ThemeEngine.colors), prefs.getInt("bl_color", 0));
        grid.setAdapter(adapter);
        adapter.listener = new ColorAdapter.onClickListener() {
            @Override
            public void onClick(int position) {
                prefs.set(PrefConstants.BLUE_LIGHT_COLOR, position);
                color = position;
                setFilter();
            }
        };

        SeekBar opacitySeek = (SeekBar) findViewById(R.id.opacity_seekbar);
        opacitySeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    alpha = progress;
                    //noinspection AndroidLintSetTextI18n
                    opacityLevel.setText(alpha + "%");
                    setFilter();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                prefs.set(PrefConstants.BLUE_LIGHT_OPACITY, seekBar.getProgress());
            }
        });
    }

    public void setFilter() {
        if (isEnabled) {
            if (ScreenFilterService.service != null && ScreenFilterService.service.mView != null) {
                ScreenFilterService.service.mView.setBackgroundColor(ScreenFilterService.getColorFilter(Color.parseColor(ThemeEngine.colors[color]), alpha));
            } else if (!ScreenFilterService.isServiceRunning) {
                startService(new Intent(getApplicationContext(), ScreenFilterService.class));
            }
        } else if (ScreenFilterService.isServiceRunning) {
            stopService(new Intent(getApplicationContext(), ScreenFilterService.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFilter();
    }
}
