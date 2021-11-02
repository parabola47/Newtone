package com.parabola.newtone.ui.fragment.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.parabola.domain.settings.ViewSettingsInteractor.ColorTheme;
import com.parabola.domain.settings.ViewSettingsInteractor.PrimaryColor;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.databinding.FragmentColorThemeSelectorBinding;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.ColorThemeSelectorPresenter;
import com.parabola.newtone.mvp.view.ColorThemeSelectorView;
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment;

import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public final class ColorThemeSelectorFragment extends BaseSwipeToBackFragment
        implements ColorThemeSelectorView {

    @InjectPresenter ColorThemeSelectorPresenter presenter;

    private FragmentColorThemeSelectorBinding binding;


    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentColorThemeSelectorBinding.inflate(inflater, container, false);
        getRootBinding().container.addView(binding.getRoot());


        getRootBinding().main.setText(R.string.setting_color_theme_title);
        getRootBinding().additionalInfo.setVisibility(View.GONE);
        getRootBinding().otherInfo.setVisibility(View.GONE);

        binding.themeToggle.addOnButtonCheckedListener((group, checkedButtonId, isChecked) -> {
            if (isChecked) {
                ColorTheme colorTheme = checkedButtonId == R.id.darkButton ? ColorTheme.DARK : ColorTheme.LIGHT;
                presenter.onDarkLightSelection(colorTheme);
            }
        });
        binding.primaryColorRadioGroup.setOnCheckedChangeListener((radioGroup, checkedButtonId) -> {
            PrimaryColor primaryColor;
            switch (checkedButtonId) {
                case R.id.primaryColorNewtone: primaryColor = PrimaryColor.NEWTONE; break;
                case R.id.primaryColorArium: primaryColor = PrimaryColor.ARIUM; break;
                case R.id.primaryColorBlues: primaryColor = PrimaryColor.BLUES; break;
                case R.id.primaryColorFloyd: primaryColor = PrimaryColor.FLOYD; break;
                case R.id.primaryColorPurple: primaryColor = PrimaryColor.PURPLE; break;
                case R.id.primaryColorPassion: primaryColor = PrimaryColor.PASSION; break;
                case R.id.primaryColorSky: primaryColor = PrimaryColor.SKY; break;
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
            case DARK: binding.themeToggle.check(R.id.darkButton); break;
            case LIGHT: binding.themeToggle.check(R.id.lightButton); break;
            default: throw new IllegalArgumentException();
        }
    }


    @Override
    public void setPrimaryColor(PrimaryColor primaryColor) {
        switch (primaryColor) {
            case NEWTONE: binding.primaryColorRadioGroup.check(R.id.primaryColorNewtone); break;
            case ARIUM: binding.primaryColorRadioGroup.check(R.id.primaryColorArium); break;
            case BLUES: binding.primaryColorRadioGroup.check(R.id.primaryColorBlues); break;
            case FLOYD: binding.primaryColorRadioGroup.check(R.id.primaryColorFloyd); break;
            case PURPLE: binding.primaryColorRadioGroup.check(R.id.primaryColorPurple); break;
            case PASSION: binding.primaryColorRadioGroup.check(R.id.primaryColorPassion); break;
            case SKY: binding.primaryColorRadioGroup.check(R.id.primaryColorSky); break;
            default: throw new IllegalArgumentException();
        }
    }

    @ProvidePresenter
    ColorThemeSelectorPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        return new ColorThemeSelectorPresenter(appComponent);
    }
}
