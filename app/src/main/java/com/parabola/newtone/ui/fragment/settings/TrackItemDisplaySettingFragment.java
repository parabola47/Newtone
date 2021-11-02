package com.parabola.newtone.ui.fragment.settings;

import static com.parabola.newtone.util.AndroidTool.convertDpToPixel;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.shape.CornerFamily;
import com.parabola.domain.settings.ViewSettingsInteractor;
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.databinding.FragmentTrackItemDisplaySettingBinding;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment;
import com.parabola.newtone.ui.dialog.DialogDismissLifecycleObserver;
import com.parabola.newtone.ui.router.MainRouter;
import com.parabola.newtone.util.SeekBarChangeAdapter;

import javax.inject.Inject;

public final class TrackItemDisplaySettingFragment extends BaseSwipeToBackFragment {
    private static final String LOG_CAT = TrackItemDisplaySettingFragment.class.getSimpleName();


    @Inject ViewSettingsInteractor viewSettingsInteractor;
    @Inject MainRouter router;

    private FragmentTrackItemDisplaySettingBinding binding;


    public static TrackItemDisplaySettingFragment newInstance() {
        return new TrackItemDisplaySettingFragment();
    }


    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentTrackItemDisplaySettingBinding.inflate(inflater, container, false);
        getRootBinding().container.addView(binding.getRoot());

        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        appComponent.inject(this);

        getRootBinding().main.setText(R.string.track_item_display_setting_screen_title);
        getRootBinding().additionalInfo.setVisibility(View.GONE);
        getRootBinding().otherInfo.setVisibility(View.GONE);


        binding.trackHolder.cover.setImageResource(R.drawable.album_default);
        binding.trackHolder.trackTitle.setText(R.string.default_track_title);
        binding.trackHolder.durationTxt.setText(R.string.default_duration);
        binding.trackHolder.getRoot().setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorListItemDefaultBackground));


        binding.coverSizeSeekBar.setOnSeekBarChangeListener(new SeekBarChangeAdapter() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                refreshCoverSize(progress);
            }
        });
        binding.coverCornersSeekBar.setOnSeekBarChangeListener(new SeekBarChangeAdapter() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                refreshCoverCorners(progress);
            }
        });
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
        binding.showCoverSwitch.setOnCheckedChangeListener((switchButton, isChecked) -> {
            refreshCoverShow(isChecked);
            onChangeCoverShow(isChecked);
        });
        binding.showAlbumTitleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> refreshAlbumTitleShow(isChecked));

        TrackItemView trackItemView = viewSettingsInteractor.getTrackItemViewSettings();

        binding.textSizeSeekBar.setProgress(trackItemView.textSize - TEXT_SIZE_MIN);
        binding.borderPaddingSeekBar.setProgress(trackItemView.borderPadding - BORDER_PADDING_MIN);
        binding.showAlbumTitleSwitch.setChecked(trackItemView.isAlbumTitleShows);
        binding.showCoverSwitch.setChecked(trackItemView.isCoverShows);
        binding.coverSizeSeekBar.setProgress(trackItemView.coverSize - COVER_SIZE_MIN);
        binding.coverCornersSeekBar.setProgress(trackItemView.coverCornersRadius);

        binding.setDefault.setOnClickListener(v -> onClickSetDefault());
        binding.showCoverBar.setOnClickListener(v -> binding.showCoverSwitch.toggle());
        binding.showAlbumTitleBar.setOnClickListener(v -> binding.showAlbumTitleSwitch.toggle());


        refreshTextSize(binding.textSizeSeekBar.getProgress());
        refreshBorderPadding(binding.borderPaddingSeekBar.getProgress());
        refreshAlbumTitleShow(binding.showAlbumTitleSwitch.isChecked());
        boolean isShowCoverChecked = binding.showCoverSwitch.isChecked();
        refreshCoverShow(isShowCoverChecked);
        onChangeCoverShow(isShowCoverChecked);
        refreshCoverSize(binding.coverSizeSeekBar.getProgress());
        refreshCoverCorners(binding.coverCornersSeekBar.getProgress());

        return root;
    }


    public void onClickSetDefault() {
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.reset_settings_dialog_title)
                .setMessage(R.string.track_item_reset_settings_dialog_message)
                .setPositiveButton(R.string.dialog_reset, (d, which) -> {
                    binding.coverSizeSeekBar.setProgress(40 - COVER_SIZE_MIN);
                    binding.coverCornersSeekBar.setProgress(4);
                    binding.showAlbumTitleSwitch.setChecked(false);
                    binding.textSizeSeekBar.setProgress(16 - TEXT_SIZE_MIN);
                    binding.borderPaddingSeekBar.setProgress(16 - BORDER_PADDING_MIN);
                    binding.showCoverSwitch.setChecked(true);
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .show();
        getLifecycle().addObserver(new DialogDismissLifecycleObserver(dialog));
    }


    @Override
    protected void onClickBackButton() {
        router.goBack();
    }

    private static final int COVER_SIZE_MIN = 32;
    private static final int TEXT_SIZE_MIN = 12;
    private static final int BORDER_PADDING_MIN = 8;


    private void refreshCoverSize(int progress) {
        int coverSizeDp = progress + COVER_SIZE_MIN;
        binding.coverSizeValue.setText(String.valueOf(coverSizeDp));
        int coverSizePx = (int) convertDpToPixel(coverSizeDp, requireContext());
        ViewGroup.LayoutParams params = binding.trackHolder.cover.getLayoutParams();
        params.width = coverSizePx;
        params.height = coverSizePx;
        binding.trackHolder.cover.setLayoutParams(params);
    }

    private void refreshCoverCorners(int progress) {
        float cornerSizePx = convertDpToPixel(progress, requireContext());

        binding.trackHolder.cover.setShapeAppearanceModel(binding.trackHolder.cover.getShapeAppearanceModel().toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, cornerSizePx)
                .build());

        binding.coverCornersValue.setText(String.valueOf(progress));
    }

    private void refreshTextSize(int progress) {
        int textSizeSp = progress + TEXT_SIZE_MIN;
        binding.textSizeValue.setText(String.valueOf(textSizeSp));

        binding.trackHolder.trackTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
        binding.trackHolder.additionalTrackInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp - 2);
        binding.trackHolder.durationTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp - 4);
    }

    private void refreshCoverShow(boolean show) {
        binding.trackHolder.cover.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void refreshAlbumTitleShow(boolean show) {
        String defaultArtist = getString(R.string.default_artist);
        String defaultAlbum = getString(R.string.default_album);

        String text = show ? getString(R.string.track_item_artist_with_album, defaultArtist, defaultAlbum)
                : defaultArtist;

        binding.trackHolder.additionalTrackInfo.setText(text);
    }

    private void onChangeCoverShow(boolean show) {
        binding.coverSizeBar.setEnabled(show);
        binding.coverSizeSeekBar.setEnabled(show);
        binding.coverCornersBar.setEnabled(show);
        binding.coverCornersSeekBar.setEnabled(show);
    }

    private void refreshBorderPadding(int progress) {
        int paddingDp = progress + BORDER_PADDING_MIN;
        int paddingPx = (int) convertDpToPixel(paddingDp, requireContext());
        binding.borderPaddingValue.setText(String.valueOf(paddingDp));
        binding.trackHolder.trackContent.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
    }


    @Override
    public void onDestroy() {
        if (isRemoving())
            onFinishing();

        super.onDestroy();
    }

    private void onFinishing() {
        //сохраняем состояние
        TrackItemView trackItemView = new TrackItemView(
                binding.textSizeSeekBar.getProgress() + TEXT_SIZE_MIN,
                binding.borderPaddingSeekBar.getProgress() + BORDER_PADDING_MIN,
                binding.showAlbumTitleSwitch.isChecked(),
                binding.showCoverSwitch.isChecked(),
                binding.coverSizeSeekBar.getProgress() + COVER_SIZE_MIN,
                binding.coverCornersSeekBar.getProgress());

        viewSettingsInteractor.setTrackItemView(trackItemView);
    }

}
