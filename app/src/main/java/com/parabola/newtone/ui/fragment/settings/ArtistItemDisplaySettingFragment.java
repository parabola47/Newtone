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
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.parabola.domain.settings.ViewSettingsInteractor;
import com.parabola.domain.settings.ViewSettingsInteractor.ArtistItemView;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment;
import com.parabola.newtone.ui.router.MainRouter;
import com.parabola.newtone.util.SeekBarChangeAdapter;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.parabola.newtone.util.AndroidTool.convertDpToPixel;
import static java.util.Objects.requireNonNull;

public final class ArtistItemDisplaySettingFragment extends BaseSwipeToBackFragment {
    private static final String LOG_TAG = ArtistItemDisplaySettingFragment.class.getSimpleName();


    @BindView(R.id.main) TextView titleTxt;
    @BindView(R.id.additional_info) TextView additionalInfoTxt;
    @BindView(R.id.otherInfo) TextView otherInfoTxt;

    @BindView(R.id.artist) TextView artist;
    @BindView(R.id.artist_info) TextView artistInfo;
    @BindView(R.id.artistHolder) View artistHolder;


    @BindView(R.id.textSizeValue) TextView textSizeValue;
    @BindView(R.id.textSizeSeekBar) SeekBar textSizeSeekBar;

    @BindView(R.id.borderPaddingValue) TextView borderPaddingValue;
    @BindView(R.id.borderPaddingSeekBar) SeekBar borderPaddingSeekBar;


    @Inject ViewSettingsInteractor viewSettingsInteractor;
    @Inject MainRouter router;

    public static ArtistItemDisplaySettingFragment newInstance() {
        return new ArtistItemDisplaySettingFragment();
    }


    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        View contentView = inflater.inflate(R.layout.fragment_artist_item_display_setting, container, false);
        ((ViewGroup) root.findViewById(R.id.container)).addView(contentView);
        ButterKnife.bind(this, root);

        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        appComponent.inject(this);

        titleTxt.setText(R.string.artist_item_display_setting_screen_title);
        additionalInfoTxt.setVisibility(View.GONE);
        otherInfoTxt.setVisibility(View.GONE);

        artist.setText(R.string.default_artist);
        artistInfo.setText(R.string.default_artist_info);

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

        ArtistItemView artistItemView = viewSettingsInteractor.getArtistItemViewSettings();

        textSizeSeekBar.setProgress(artistItemView.textSize - TEXT_SIZE_MIN);
        borderPaddingSeekBar.setProgress(artistItemView.borderPadding - BORDER_PADDING_MIN);

        refreshTextSize(textSizeSeekBar.getProgress());
        refreshBorderPadding(borderPaddingSeekBar.getProgress());

        return root;
    }


    @OnClick(R.id.setDefault)
    public void onClickSetDefault() {
        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.reset_settings_dialog_title)
                .setMessage(R.string.artist_item_reset_settings_dialog_message)
                .setPositiveButton(R.string.dialog_reset, (d, which) -> {
                    textSizeSeekBar.setProgress(16 - TEXT_SIZE_MIN);
                    borderPaddingSeekBar.setProgress(16 - BORDER_PADDING_MIN);
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


    private static final int TEXT_SIZE_MIN = 12;
    private static final int BORDER_PADDING_MIN = 8;


    private void refreshTextSize(int progress) {
        int textSizeSp = progress + TEXT_SIZE_MIN;
        textSizeValue.setText(String.valueOf(textSizeSp));

        artist.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp);
        artistInfo.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp - 2);
    }


    private void refreshBorderPadding(int progress) {
        int paddingDp = progress + BORDER_PADDING_MIN;
        int paddingPx = (int) convertDpToPixel(paddingDp, requireContext());
        borderPaddingValue.setText(String.valueOf(paddingDp));
        artistHolder.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
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
                textSizeSeekBar.getProgress() + TEXT_SIZE_MIN,
                borderPaddingSeekBar.getProgress() + BORDER_PADDING_MIN);

        viewSettingsInteractor.setArtistItemViewSettings(artistItemView);
    }

}