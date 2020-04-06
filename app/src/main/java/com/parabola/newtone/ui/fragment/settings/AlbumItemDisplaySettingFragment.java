package com.parabola.newtone.ui.fragment.settings;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.llollox.androidtoggleswitch.widgets.ToggleSwitch;
import com.parabola.domain.settings.ViewSettingsInteractor;
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView;
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView.AlbumViewType;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment;
import com.parabola.newtone.ui.router.MainRouter;
import com.parabola.newtone.util.SeekBarChangeAdapter;

import javax.inject.Inject;

import butterknife.BindDrawable;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.parabola.newtone.util.AndroidTool.calculateAlbumColumnCount;
import static com.parabola.newtone.util.AndroidTool.convertDpToPixel;
import static com.parabola.newtone.util.AndroidTool.getScreenWidthPx;
import static java.util.Objects.requireNonNull;

public final class AlbumItemDisplaySettingFragment extends BaseSwipeToBackFragment {
    private static final String LOG_CAT = AlbumItemDisplaySettingFragment.class.getSimpleName();

    @BindView(R.id.main) TextView titleTxt;
    @BindView(R.id.additional_info) TextView additionalInfoTxt;
    @BindView(R.id.otherInfo) TextView otherInfoTxt;

    @BindView(R.id.viewTypeToggle) ToggleSwitch viewTypeToggle;
    private static final String VIEW_TYPE_BUNDLE_CHECK_POSITION_KEY = "VIEW_TYPE_BUNDLE_CHECK_POSITION";

    @BindView(R.id.albumGridHolder) ViewGroup albumGridHolder;
    @BindView(R.id.albumListHolder) ViewGroup albumListHolder;


    @BindView(R.id.textSizeValue) TextView textSizeValue;
    @BindView(R.id.textSizeSeekBar) SeekBar textSizeSeekBar;

    @BindView(R.id.borderPaddingValue) TextView borderPaddingValue;
    @BindView(R.id.borderPaddingSeekBar) SeekBar borderPaddingSeekBar;

    @BindView(R.id.coverSizeValue) TextView coverSizeValue;
    @BindView(R.id.coverSizeSeekBar) SeekBar coverSizeSeekBar;

    @BindView(R.id.coverCornersValue) TextView coverCornersValue;
    @BindView(R.id.coverCornersSeekBar) SeekBar coverCornersSeekBar;


    @BindString(R.string.default_artist) String defaultArtist;
    @BindString(R.string.default_album) String defaultAlbum;
    @BindDrawable(R.drawable.album_default) Drawable defaultAlbumCover;


    @Inject ViewSettingsInteractor viewSettingsInteractor;
    @Inject MainRouter router;


    public static AlbumItemDisplaySettingFragment newInstance() {
        return new AlbumItemDisplaySettingFragment();
    }


    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        View contentView = inflater.inflate(R.layout.fragment_album_item_display_setting, container, false);
        ((ViewGroup) root.findViewById(R.id.container)).addView(contentView);
        ButterKnife.bind(this, root);

        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        appComponent.inject(this);

        titleTxt.setText(R.string.album_item_display_setting_screen_title);
        additionalInfoTxt.setVisibility(View.GONE);
        otherInfoTxt.setVisibility(View.GONE);

        ((TextView) albumListHolder.findViewById(R.id.album_title)).setText(defaultAlbum);
        ((TextView) albumListHolder.findViewById(R.id.author)).setText(defaultArtist);
        ((TextView) albumListHolder.findViewById(R.id.tracks_count)).setText(R.string.default_album_tracks_count);
        ((ImageView) albumListHolder.findViewById(R.id.albumCover)).setImageDrawable(defaultAlbumCover);

        ((TextView) albumGridHolder.findViewById(R.id.album_title)).setText(defaultAlbum);
        ((TextView) albumGridHolder.findViewById(R.id.author)).setText(defaultArtist);
        ((ImageView) albumGridHolder.findViewById(R.id.albumCover)).setImageDrawable(defaultAlbumCover);
        ViewGroup.LayoutParams layoutParams = albumGridHolder.findViewById(R.id.albumCover).getLayoutParams();
        layoutParams.width = getGridAlbumWidth();
        albumGridHolder.findViewById(R.id.albumCover).setLayoutParams(layoutParams);

        viewTypeToggle.setOnChangeListener(this::refreshViewType);
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

        AlbumItemView albumItemView = viewSettingsInteractor.getAlbumItemViewSettings();

        int viewTypeCheckPosition = savedInstanceState != null
                ? savedInstanceState.getInt(VIEW_TYPE_BUNDLE_CHECK_POSITION_KEY)
                : albumItemView.viewType.ordinal();
        viewTypeToggle.setCheckedPosition(viewTypeCheckPosition);
        textSizeSeekBar.setProgress(albumItemView.textSize - TEXT_SIZE_MIN);
        borderPaddingSeekBar.setProgress(albumItemView.borderPadding - BORDER_PADDING_MIN);
        coverSizeSeekBar.setProgress(albumItemView.coverSize - COVER_SIZE_MIN);
        coverCornersSeekBar.setProgress(albumItemView.coverCornersRadius);

        refreshViewType(viewTypeCheckPosition);
        refreshTextSize(textSizeSeekBar.getProgress());
        refreshBorderPadding(borderPaddingSeekBar.getProgress());
        refreshCoverSize(coverSizeSeekBar.getProgress());
        refreshCoverCorners(coverCornersSeekBar.getProgress());


        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(VIEW_TYPE_BUNDLE_CHECK_POSITION_KEY, viewTypeToggle.getCheckedPosition());
    }

    @OnClick(R.id.setDefault)
    public void onClickSetDefault() {
        AlertDialog alertDialog = new AlertDialog.Builder(requireContext())
                .setTitle(R.string.reset_settings_dialog_title)
                .setMessage(R.string.album_item_reset_settings_dialog_message)
                .setPositiveButton(R.string.dialog_reset, (d, which) -> {
                    viewTypeToggle.setCheckedPosition(0);
                    refreshViewType(0);
                    textSizeSeekBar.setProgress(16 - TEXT_SIZE_MIN);
                    borderPaddingSeekBar.setProgress(16 - BORDER_PADDING_MIN);
                    coverSizeSeekBar.setProgress(64 - COVER_SIZE_MIN);
                    coverCornersSeekBar.setProgress(4);
                })
                .setNegativeButton(R.string.dialog_cancel, null)
                .create();

        Window window = requireNonNull(alertDialog.getWindow());
        window.getDecorView().setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.dialog_bg));
        int widthPx = (int) requireContext().getResources().getDimension(R.dimen.alert_dialog_min_width);
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

    //0 - grid, 1 - list
    private void refreshViewType(int viewType) {
        if (viewType == 0) {
            albumGridHolder.setVisibility(View.VISIBLE);
            albumListHolder.setVisibility(View.INVISIBLE);
            borderPaddingSeekBar.setEnabled(false);
            coverSizeSeekBar.setEnabled(false);
        } else if (viewType == 1) {
            albumGridHolder.setVisibility(View.INVISIBLE);
            albumListHolder.setVisibility(View.VISIBLE);
            borderPaddingSeekBar.setEnabled(true);
            coverSizeSeekBar.setEnabled(true);
        } else {
            throw new IllegalArgumentException("viewType equals to " + viewType);
        }
    }


    private void refreshTextSize(int progress) {
        int textSizeSp = progress + TEXT_SIZE_MIN;
        textSizeValue.setText(String.valueOf(textSizeSp));

        ((TextView) albumListHolder.findViewById(R.id.album_title)).setTextSize(textSizeSp);
        ((TextView) albumListHolder.findViewById(R.id.author)).setTextSize(textSizeSp - 2);
        ((TextView) albumListHolder.findViewById(R.id.tracks_count)).setTextSize(textSizeSp - 4);

        ((TextView) albumGridHolder.findViewById(R.id.album_title)).setTextSize(textSizeSp + 2);
        ((TextView) albumGridHolder.findViewById(R.id.author)).setTextSize(textSizeSp - 4);
    }

    private void refreshBorderPadding(int progress) {
        int paddingDp = progress + BORDER_PADDING_MIN;
        int paddingPx = (int) convertDpToPixel(paddingDp, requireContext());
        borderPaddingValue.setText(String.valueOf(paddingDp));

        albumListHolder.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
    }

    private void refreshCoverSize(int progress) {
        int coverSizeDp = progress + COVER_SIZE_MIN;
        coverSizeValue.setText(String.valueOf(coverSizeDp));
        int coverSizePx = (int) convertDpToPixel(coverSizeDp, requireContext());

        ViewGroup.LayoutParams params = albumListHolder.findViewById(R.id.albumCover).getLayoutParams();
        params.width = coverSizePx;
        params.height = coverSizePx;
        albumListHolder.findViewById(R.id.albumCover).setLayoutParams(params);
    }

    private void refreshCoverCorners(int progress) {
        float cornerSizePx = convertDpToPixel(progress, requireContext());

        ShapeableImageView gridCover = albumGridHolder.findViewById(R.id.albumCover);
        gridCover.setShapeAppearanceModel(gridCover.getShapeAppearanceModel().toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, cornerSizePx)
                .build());

        ShapeableImageView listCover = albumListHolder.findViewById(R.id.albumCover);
        listCover.setShapeAppearanceModel(gridCover.getShapeAppearanceModel().toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, cornerSizePx)
                .build());

        coverCornersValue.setText(String.valueOf(progress));
    }


    private int getGridAlbumWidth() {
        return (int) (getScreenWidthPx(requireActivity().getWindowManager()) / calculateAlbumColumnCount(requireActivity()));
    }


    @Override
    public void onDestroy() {
        if (isRemoving())
            onFinishing();

        super.onDestroy();
    }

    private void onFinishing() {
        //сохраняем состояние
        AlbumViewType albumViewType = null;
        for (AlbumViewType item : AlbumViewType.values()) {
            if (item.ordinal() == viewTypeToggle.getCheckedPosition()) {
                albumViewType = item;
                break;
            }
        }

        AlbumItemView albumItemView = new AlbumItemView(
                requireNonNull(albumViewType),
                textSizeSeekBar.getProgress() + TEXT_SIZE_MIN,
                borderPaddingSeekBar.getProgress() + BORDER_PADDING_MIN,
                coverSizeSeekBar.getProgress() + COVER_SIZE_MIN,
                coverCornersSeekBar.getProgress());

        viewSettingsInteractor.setAlbumItemViewSettings(albumItemView);
    }
}
