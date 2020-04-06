package com.parabola.newtone.ui.fragment.settings;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.parabola.domain.settings.ViewSettingsInteractor;
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment;
import com.parabola.newtone.ui.router.MainRouter;
import com.parabola.newtone.util.SeekBarChangeAdapter;

import javax.inject.Inject;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.parabola.newtone.util.AndroidTool.convertDpToPixel;
import static java.util.Objects.requireNonNull;

public final class TrackItemDisplaySettingFragment extends BaseSwipeToBackFragment {
    private static final String LOG_CAT = TrackItemDisplaySettingFragment.class.getSimpleName();

    @BindView(R.id.main) TextView titleTxt;
    @BindView(R.id.additional_info) TextView additionalInfoTxt;
    @BindView(R.id.otherInfo) TextView otherInfoTxt;


    @BindView(R.id.cover) ShapeableImageView cover;
    @BindView(R.id.track_title) TextView trackTitle;
    @BindView(R.id.additionalTrackInfo) TextView additionalTrackInfo;
    @BindView(R.id.song_duration) TextView duration;
    @BindView(R.id.trackHolder) View trackHolder;

    @BindView(R.id.coverCornersValue) TextView coverCornersValue;
    @BindView(R.id.coverCornersSeekBar) SeekBar coverCornersSeekBar;

    @BindView(R.id.coverSizeValue) TextView coverSizeValue;
    @BindView(R.id.coverSizeSeekBar) SeekBar coverSizeSeekBar;

    @BindView(R.id.textSizeValue) TextView textSizeValue;
    @BindView(R.id.textSizeSeekBar) SeekBar textSizeSeekBar;

    @BindView(R.id.borderPaddingValue) TextView borderPaddingValue;
    @BindView(R.id.borderPaddingSeekBar) SeekBar borderPaddingSeekBar;

    @BindView(R.id.showCoverSwitch) SwitchCompat showCoverSwitch;
    @BindView(R.id.showAlbumTitleSwitch) SwitchCompat showAlbumTitleSwitch;

    @BindString(R.string.default_artist) String defaultArtist;
    @BindString(R.string.default_album) String defaultAlbum;

    @Inject ViewSettingsInteractor viewSettingsInteractor;
    @Inject MainRouter router;


    public static TrackItemDisplaySettingFragment newInstance() {
        return new TrackItemDisplaySettingFragment();
    }


    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        View contentView = inflater.inflate(R.layout.fragment_track_item_display_setting, container, false);
        ((ViewGroup) root.findViewById(R.id.container)).addView(contentView);
        ButterKnife.bind(this, root);

        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        appComponent.inject(this);

        titleTxt.setText(R.string.track_item_display_setting_screen_title);
        additionalInfoTxt.setVisibility(View.GONE);
        otherInfoTxt.setVisibility(View.GONE);


        cover.setImageResource(R.drawable.album_default);
        trackTitle.setText(R.string.default_track_title);
        duration.setText(R.string.default_duration);
        trackHolder.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorListItemDefaultBackground));


        coverSizeSeekBar.setOnSeekBarChangeListener(new SeekBarChangeAdapter() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                refreshCoverSize(progress);
            }
        });
        coverCornersSeekBar.setOnSeekBarChangeListener(new SeekBarChangeAdapter() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                refreshCoverCorners(progress);
            }
        });
        textSizeSeekBar.setOnSeekBarChangeListener(new SeekBarChangeAdapter() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                refreshTextSize(progress);
            }
        });
        borderPaddingSeekBar.setOnSeekBarChangeListener(new SeekBarChangeAdapter() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                refreshBorderPadding(progress);
            }
        });
        showCoverSwitch.setOnCheckedChangeListener((switchButton, isChecked) -> {
            refreshCoverShow(isChecked);
            onChangeCoverShow(isChecked);
        });
        showAlbumTitleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> refreshAlbumTitleShow(isChecked));

        TrackItemView trackItemView = viewSettingsInteractor.getTrackItemViewSettings();

        textSizeSeekBar.setProgress(trackItemView.textSize - TEXT_SIZE_MIN);
        borderPaddingSeekBar.setProgress(trackItemView.borderPadding - BORDER_PADDING_MIN);
        showAlbumTitleSwitch.setChecked(trackItemView.isAlbumTitleShows);
        showCoverSwitch.setChecked(trackItemView.isCoverShows);
        coverSizeSeekBar.setProgress(trackItemView.coverSize - COVER_SIZE_MIN);
        coverCornersSeekBar.setProgress(trackItemView.coverCornersRadius);


        refreshTextSize(textSizeSeekBar.getProgress());
        refreshBorderPadding(borderPaddingSeekBar.getProgress());
        refreshAlbumTitleShow(showAlbumTitleSwitch.isChecked());
        boolean isShowCoverChecked = showCoverSwitch.isChecked();
        refreshCoverShow(isShowCoverChecked);
        onChangeCoverShow(isShowCoverChecked);
        refreshCoverSize(coverSizeSeekBar.getProgress());
        refreshCoverCorners(coverCornersSeekBar.getProgress());

        return root;
    }


    @OnClick(R.id.setDefault)
    public void onClickSetDefault() {
        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.reset_settings_dialog_title)
                .setMessage(R.string.track_item_reset_settings_dialog_message)
                .setPositiveButton(R.string.dialog_reset, (d, which) -> {
                    coverSizeSeekBar.setProgress(40 - COVER_SIZE_MIN);
                    coverCornersSeekBar.setProgress(4);
                    showAlbumTitleSwitch.setChecked(false);
                    textSizeSeekBar.setProgress(16 - TEXT_SIZE_MIN);
                    borderPaddingSeekBar.setProgress(16 - BORDER_PADDING_MIN);
                    showCoverSwitch.setChecked(true);
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .create();

        Window window = requireNonNull(alertDialog.getWindow());
        window.getDecorView().setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.dialog_bg));
        int widthPx = (int) getResources().getDimension(R.dimen.alert_dialog_min_width);
        window.setLayout(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT);

        alertDialog.show();
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
        coverSizeValue.setText(String.valueOf(coverSizeDp));
        int coverSizePx = (int) convertDpToPixel(coverSizeDp, requireContext());
        ViewGroup.LayoutParams params = cover.getLayoutParams();
        params.width = coverSizePx;
        params.height = coverSizePx;
        cover.setLayoutParams(params);
    }

    private void refreshCoverCorners(int progress) {
        float cornerSizePx = convertDpToPixel(progress, requireContext());

        cover.setShapeAppearanceModel(cover.getShapeAppearanceModel().toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, cornerSizePx)
                .build());

        coverCornersValue.setText(String.valueOf(progress));
    }

    private void refreshTextSize(int progress) {
        int textSizeSp = progress + TEXT_SIZE_MIN;
        textSizeValue.setText(String.valueOf(textSizeSp));

        trackTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
        additionalTrackInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp - 2);
        duration.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp - 4);
    }

    private void refreshCoverShow(boolean show) {
        cover.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void refreshAlbumTitleShow(boolean show) {
        String text = show ? getString(R.string.track_item_artist_with_album, defaultArtist, defaultAlbum)
                : getString(R.string.default_artist);

        additionalTrackInfo.setText(text);
    }

    private void onChangeCoverShow(boolean show) {
        coverSizeSeekBar.setEnabled(show);
        coverCornersSeekBar.setEnabled(show);
    }

    private void refreshBorderPadding(int progress) {
        int paddingDp = progress + BORDER_PADDING_MIN;
        int paddingPx = (int) convertDpToPixel(paddingDp, requireContext());
        borderPaddingValue.setText(String.valueOf(paddingDp));
        trackHolder.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
    }


    @OnClick(R.id.showCoverBar)
    public void onClickShowCoverBar() {
        showCoverSwitch.toggle();
    }


    @OnClick(R.id.showAlbumTitleBar)
    public void onClickShowAlbumBar() {
        showAlbumTitleSwitch.toggle();
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
                textSizeSeekBar.getProgress() + TEXT_SIZE_MIN,
                borderPaddingSeekBar.getProgress() + BORDER_PADDING_MIN,
                showAlbumTitleSwitch.isChecked(),
                showCoverSwitch.isChecked(),
                coverSizeSeekBar.getProgress() + COVER_SIZE_MIN,
                coverCornersSeekBar.getProgress());

        viewSettingsInteractor.setTrackItemView(trackItemView);
    }

}
