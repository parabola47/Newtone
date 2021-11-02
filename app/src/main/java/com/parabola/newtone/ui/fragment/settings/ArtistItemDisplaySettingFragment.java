package com.parabola.newtone.ui.fragment.settings;

import static com.parabola.newtone.util.AndroidTool.convertDpToPixel;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.parabola.domain.settings.ViewSettingsInteractor;
import com.parabola.domain.settings.ViewSettingsInteractor.ArtistItemView;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.databinding.FragmentArtistItemDisplaySettingBinding;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment;
import com.parabola.newtone.ui.dialog.DialogDismissLifecycleObserver;
import com.parabola.newtone.ui.router.MainRouter;
import com.parabola.newtone.util.SeekBarChangeAdapter;

import javax.inject.Inject;

public final class ArtistItemDisplaySettingFragment extends BaseSwipeToBackFragment {
    private static final String LOG_TAG = ArtistItemDisplaySettingFragment.class.getSimpleName();


    private FragmentArtistItemDisplaySettingBinding binding;


    @Inject ViewSettingsInteractor viewSettingsInteractor;
    @Inject MainRouter router;


    public static ArtistItemDisplaySettingFragment newInstance() {
        return new ArtistItemDisplaySettingFragment();
    }


    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentArtistItemDisplaySettingBinding.inflate(inflater, container, false);
        getRootBinding().container.addView(binding.getRoot());


        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        appComponent.inject(this);

        getRootBinding().main.setText(R.string.artist_item_display_setting_screen_title);
        getRootBinding().additionalInfo.setVisibility(View.GONE);
        getRootBinding().otherInfo.setVisibility(View.GONE);

        binding.artistHolder.artist.setText(R.string.default_artist);
        binding.artistHolder.artistInfo.setText(R.string.default_artist_info);

        binding.textSizeSeekBar.setOnSeekBarChangeListener(new SeekBarChangeAdapter() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                refreshTextSize(progress);
            }
        });
        binding.borderPaddingSeekBar.setOnSeekBarChangeListener(new SeekBarChangeAdapter() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                refreshBorderPadding(progress);
            }
        });
        binding.setDefault.setOnClickListener(v -> onClickSetDefault());

        ArtistItemView artistItemView = viewSettingsInteractor.getArtistItemViewSettings();

        binding.textSizeSeekBar.setProgress(artistItemView.textSize - TEXT_SIZE_MIN);
        binding.borderPaddingSeekBar.setProgress(artistItemView.borderPadding - BORDER_PADDING_MIN);

        refreshTextSize(binding.textSizeSeekBar.getProgress());
        refreshBorderPadding(binding.borderPaddingSeekBar.getProgress());

        return root;
    }


    public void onClickSetDefault() {
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.reset_settings_dialog_title)
                .setMessage(R.string.artist_item_reset_settings_dialog_message)
                .setPositiveButton(R.string.dialog_reset, (d, which) -> {
                    binding.textSizeSeekBar.setProgress(16 - TEXT_SIZE_MIN);
                    binding.borderPaddingSeekBar.setProgress(16 - BORDER_PADDING_MIN);
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
        getLifecycle().addObserver(new DialogDismissLifecycleObserver(dialog));
    }


    @Override
    protected void onClickBackButton() {
        router.goBack();
    }


    private static final int TEXT_SIZE_MIN = 12;
    private static final int BORDER_PADDING_MIN = 8;


    private void refreshTextSize(int progress) {
        int textSizeSp = progress + TEXT_SIZE_MIN;
        binding.textSizeValue.setText(String.valueOf(textSizeSp));

        binding.artistHolder.artist.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
        binding.artistHolder.artistInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp - 2);
    }


    private void refreshBorderPadding(int progress) {
        int paddingDp = progress + BORDER_PADDING_MIN;
        int paddingPx = (int) convertDpToPixel(paddingDp, requireContext());
        binding.borderPaddingValue.setText(String.valueOf(paddingDp));
        binding.artistHolder.getRoot().setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
    }


    @Override
    public void onDestroy() {
        if (isRemoving())
            onFinishing();

        super.onDestroy();
    }

    private void onFinishing() {
        //сохраняем состояние
        ArtistItemView artistItemView = new ArtistItemView(
                binding.textSizeSeekBar.getProgress() + TEXT_SIZE_MIN,
                binding.borderPaddingSeekBar.getProgress() + BORDER_PADDING_MIN);

        viewSettingsInteractor.setArtistItemViewSettings(artistItemView);
    }

}
