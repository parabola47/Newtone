package com.parabola.newtone.ui.fragment.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.parabola.domain.settings.ViewSettingsInteractor.ColorTheme;
import com.parabola.domain.settings.ViewSettingsInteractor.PrimaryColor;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.ColorThemeSelectorPresenter;
import com.parabola.newtone.mvp.view.ColorThemeSelectorView;
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public final class ColorThemeSelectorFragment extends BaseSwipeToBackFragment
        implements ColorThemeSelectorView {

    @InjectPresenter ColorThemeSelectorPresenter presenter;


    @BindView(R.id.main) TextView titleTxt;
    @BindView(R.id.additional_info) TextView additionalInfoTxt;
    @BindView(R.id.otherInfo) TextView otherInfoTxt;

    @BindView(R.id.themeToggle) MaterialButtonToggleGroup themeToggle;
    @BindView(R.id.primaryColorRadioGroup) RadioGroup primaryColorRadioGroup;


    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        View contentView = inflater.inflate(R.layout.fragment_color_theme_selector, container, false);
        ((ViewGroup) root.findViewById(R.id.container)).addView(contentView);
        ButterKnife.bind(this, root);

        titleTxt.setText(R.string.setting_color_theme_title);
        additionalInfoTxt.setVisibility(View.GONE);
        otherInfoTxt.setVisibility(View.GONE);

        themeToggle.addOnButtonCheckedListener((group, checkedButtonId, isChecked) -> {
            if (isChecked) {
                ColorTheme colorTheme = checkedButtonId == R.id.darkButton ? ColorTheme.DARK : ColorTheme.LIGHT;
                presenter.onDarkLightSelection(colorTheme);
            }
        });
        primaryColorRadioGroup.setOnCheckedChangeListener((radioGroup, checkedButtonId) -> {
            PrimaryColor primaryColor;
            switch (checkedButtonId) {
                case R.id.primaryColorNewtone: primaryColor = PrimaryColor.NEWTONE; break;
                case R.id.primaryColorArium: primaryColor = PrimaryColor.ARIUM; break;
                case R.id.primaryColorBlues: primaryColor = PrimaryColor.BLUES; break;
                case R.id.primaryColorFloyd: primaryColor = PrimaryColor.FLOYD; break;
                case R.id.primaryColorPurple: primaryColor = PrimaryColor.PURPLE; break;
                case R.id.primaryColorPassion: primaryColor = PrimaryColor.PASSION; break;
                default: throw new IllegalStateException();
            }
            presenter.onPrimaryColorSelection(primaryColor);
        });

        return root;
    }

    @Override
    protected void onClickBackButton() {
        presenter.onClickBackButton();
    }

    @Override
    public void setDarkLightTheme(ColorTheme colorTheme) {
        switch (colorTheme) {
            case DARK: themeToggle.check(R.id.darkButton); break;
            case LIGHT: themeToggle.check(R.id.lightButton); break;
            default: throw new IllegalArgumentException();
        }
    }


    @Override
    public void setPrimaryColor(PrimaryColor primaryColor) {
        switch (primaryColor) {
            case NEWTONE: primaryColorRadioGroup.check(R.id.primaryColorNewtone); break;
            case ARIUM: primaryColorRadioGroup.check(R.id.primaryColorArium); break;
            case BLUES: primaryColorRadioGroup.check(R.id.primaryColorBlues); break;
            case FLOYD: primaryColorRadioGroup.check(R.id.primaryColorFloyd); break;
            case PURPLE: primaryColorRadioGroup.check(R.id.primaryColorPurple); break;
            case PASSION: primaryColorRadioGroup.check(R.id.primaryColorPassion); break;
            default: throw new IllegalArgumentException();
        }
    }

    @ProvidePresenter
    ColorThemeSelectorPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        return new ColorThemeSelectorPresenter(appComponent);
    }
}
