package com.parabola.newtone.ui.fragment.settings;

import static com.parabola.newtone.util.AndroidTool.calculateAlbumColumnCount;
import static com.parabola.newtone.util.AndroidTool.convertDpToPixel;
import static com.parabola.newtone.util.AndroidTool.getScreenWidthPx;
import static java.util.Objects.requireNonNull;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.parabola.domain.settings.ViewSettingsInteractor;
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView;
import com.parabola.domain.settings.ViewSettingsInteractor.AlbumItemView.AlbumViewType;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.databinding.FragmentAlbumItemDisplaySettingBinding;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment;
import com.parabola.newtone.ui.dialog.DialogDismissLifecycleObserver;
import com.parabola.newtone.ui.router.MainRouter;
import com.parabola.newtone.util.SeekBarChangeAdapter;

import javax.inject.Inject;

public final class AlbumItemDisplaySettingFragment extends BaseSwipeToBackFragment {
    private static final String LOG_CAT = AlbumItemDisplaySettingFragment.class.getSimpleName();


    private FragmentAlbumItemDisplaySettingBinding binding;

    private static final String VIEW_TYPE_BUNDLE_CHECK_POSITION_KEY = "VIEW_TYPE_BUNDLE_CHECK_POSITION";

    @Inject ViewSettingsInteractor viewSettingsInteractor;
    @Inject MainRouter router;


    public static AlbumItemDisplaySettingFragment newInstance() {
        return new AlbumItemDisplaySettingFragment();
    }


    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentAlbumItemDisplaySettingBinding.inflate(inflater, container, false);
        getRootBinding().container.addView(binding.getRoot());

        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        appComponent.inject(this);

        getRootBinding().main.setText(R.string.album_item_display_setting_screen_title);
        getRootBinding().additionalInfo.setVisibility(View.GONE);
        getRootBinding().otherInfo.setVisibility(View.GONE);

        binding.albumListHolder.albumTitle.setText(R.string.default_album);
        binding.albumListHolder.author.setText(R.string.default_artist);
        binding.albumListHolder.tracksCount.setText(R.string.default_album_tracks_count);
        binding.albumListHolder.albumCover.setImageResource(R.drawable.album_default);

        binding.albumGridHolder.albumTitle.setText(R.string.default_album);
        binding.albumGridHolder.author.setText(R.string.default_artist);
        binding.albumGridHolder.albumCover.setImageResource(R.drawable.album_default);
        ViewGroup.LayoutParams layoutParams = binding.albumGridHolder.albumCover.getLayoutParams();
        layoutParams.width = getGridAlbumWidth();
        binding.albumGridHolder.albumCover.setLayoutParams(layoutParams);

        binding.viewTypeToggle.addOnButtonCheckedListener((group, checkedButtonId, isChecked) -> {
            if (isChecked) refreshViewType(checkedButtonId);
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
        binding.setDefault.setOnClickListener(v -> onClickSetDefault());

        AlbumItemView albumItemView = viewSettingsInteractor.getAlbumItemViewSettings();

        int checkedButtonId;
        if (savedInstanceState != null)
            checkedButtonId = savedInstanceState.getInt(VIEW_TYPE_BUNDLE_CHECK_POSITION_KEY);
        else checkedButtonId = albumItemView.viewType == AlbumViewType.GRID
                ? R.id.gridButton : R.id.listButton;

        binding.viewTypeToggle.check(checkedButtonId);
        binding.textSizeSeekBar.setProgress(albumItemView.textSize - TEXT_SIZE_MIN);
        binding.borderPaddingSeekBar.setProgress(albumItemView.borderPadding - BORDER_PADDING_MIN);
        binding.coverSizeSeekBar.setProgress(albumItemView.coverSize - COVER_SIZE_MIN);
        binding.coverCornersSeekBar.setProgress(albumItemView.coverCornersRadius);

        refreshViewType(checkedButtonId);
        refreshTextSize(binding.textSizeSeekBar.getProgress());
        refreshBorderPadding(binding.borderPaddingSeekBar.getProgress());
        refreshCoverSize(binding.coverSizeSeekBar.getProgress());
        refreshCoverCorners(binding.coverCornersSeekBar.getProgress());

        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(VIEW_TYPE_BUNDLE_CHECK_POSITION_KEY, binding.viewTypeToggle.getCheckedButtonId());
    }


    public void onClickSetDefault() {
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.reset_settings_dialog_title)
                .setMessage(R.string.album_item_reset_settings_dialog_message)
                .setPositiveButton(R.string.dialog_reset, (d, w) -> {
                    binding.viewTypeToggle.check(R.id.gridButton);
                    binding.textSizeSeekBar.setProgress(16 - TEXT_SIZE_MIN);
                    binding.borderPaddingSeekBar.setProgress(16 - BORDER_PADDING_MIN);
                    binding.coverSizeSeekBar.setProgress(64 - COVER_SIZE_MIN);
                    binding.coverCornersSeekBar.setProgress(4);
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


    private void refreshViewType(int checkedButtonId) {
        if (checkedButtonId == R.id.gridButton) {
            binding.albumGridHolder.getRoot().setVisibility(View.VISIBLE);
            binding.albumListHolder.getRoot().setVisibility(View.INVISIBLE);
            binding.borderPaddingBar.setEnabled(false);
            binding.borderPaddingSeekBar.setEnabled(false);
            binding.coverSizeBar.setEnabled(false);
            binding.coverSizeSeekBar.setEnabled(false);
        } else if (checkedButtonId == R.id.listButton) {
            binding.albumGridHolder.getRoot().setVisibility(View.INVISIBLE);
            binding.albumListHolder.getRoot().setVisibility(View.VISIBLE);
            binding.borderPaddingBar.setEnabled(true);
            binding.borderPaddingSeekBar.setEnabled(true);
            binding.coverSizeBar.setEnabled(true);
            binding.coverSizeSeekBar.setEnabled(true);
        } else {
            throw new IllegalArgumentException("checkedButtonId equals to " + checkedButtonId);
        }
    }


    private void refreshTextSize(int progress) {
        int textSizeSp = progress + TEXT_SIZE_MIN;
        binding.textSizeValue.setText(String.valueOf(textSizeSp));

        binding.albumListHolder.albumTitle.setTextSize(textSizeSp);
        binding.albumListHolder.author.setTextSize(textSizeSp - 2);
        binding.albumListHolder.tracksCount.setTextSize(textSizeSp - 4);

        binding.albumGridHolder.albumTitle.setTextSize(textSizeSp + 2);
        binding.albumGridHolder.author.setTextSize(textSizeSp - 4);
    }

    private void refreshBorderPadding(int progress) {
        int paddingDp = progress + BORDER_PADDING_MIN;
        int paddingPx = (int) convertDpToPixel(paddingDp, requireContext());
        binding.borderPaddingValue.setText(String.valueOf(paddingDp));

        binding.albumListHolder.getRoot().setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
    }

    private void refreshCoverSize(int progress) {
        int coverSizeDp = progress + COVER_SIZE_MIN;
        binding.coverSizeValue.setText(String.valueOf(coverSizeDp));
        int coverSizePx = (int) convertDpToPixel(coverSizeDp, requireContext());

        ViewGroup.LayoutParams params = binding.albumListHolder.albumCover.getLayoutParams();
        params.width = coverSizePx;
        params.height = coverSizePx;
        binding.albumListHolder.albumCover.setLayoutParams(params);
    }

    private void refreshCoverCorners(int progress) {
        float cornerSizePx = convertDpToPixel(progress, requireContext());

        ShapeableImageView gridCover = binding.albumGridHolder.albumCover;
        gridCover.setShapeAppearanceModel(gridCover.getShapeAppearanceModel().toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, cornerSizePx)
                .build());

        ShapeableImageView listCover = binding.albumListHolder.albumCover;
        listCover.setShapeAppearanceModel(gridCover.getShapeAppearanceModel().toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, cornerSizePx)
                .build());

        binding.coverCornersValue.setText(String.valueOf(progress));
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
        AlbumViewType albumViewType = binding.viewTypeToggle.getCheckedButtonId() == R.id.gridButton
                ? AlbumViewType.GRID
                : AlbumViewType.LIST;


        AlbumItemView albumItemView = new AlbumItemView(
                requireNonNull(albumViewType),
                binding.textSizeSeekBar.getProgress() + TEXT_SIZE_MIN,
                binding.borderPaddingSeekBar.getProgress() + BORDER_PADDING_MIN,
                binding.coverSizeSeekBar.getProgress() + COVER_SIZE_MIN,
                binding.coverCornersSeekBar.getProgress());

        viewSettingsInteractor.setAlbumItemViewSettings(albumItemView);
    }
}
