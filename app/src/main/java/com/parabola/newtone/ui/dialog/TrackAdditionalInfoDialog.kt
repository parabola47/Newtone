package com.parabola.newtone.ui.dialog;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.shape.CornerFamily;
import com.parabola.domain.model.Track;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.databinding.DialogTrackAdditionInfoBinding;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.TrackAdditionalInfoPresenter;
import com.parabola.newtone.mvp.view.TrackAdditionalInfoView;
import com.parabola.newtone.util.TimeFormatterTool;

import moxy.MvpAppCompatDialogFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public final class TrackAdditionalInfoDialog extends MvpAppCompatDialogFragment
        implements TrackAdditionalInfoView {

    @InjectPresenter TrackAdditionalInfoPresenter presenter;

    private DialogTrackAdditionInfoBinding binding;

    private static final String TRACK_ID_BUNDLE_KEY = "track id";

    public static TrackAdditionalInfoDialog newInstance(int trackId) {
        Bundle args = new Bundle();
        args.putInt(TRACK_ID_BUNDLE_KEY, trackId);

        TrackAdditionalInfoDialog fragment = new TrackAdditionalInfoDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DialogTrackAdditionInfoBinding
                .inflate(LayoutInflater.from(requireContext()));

        binding.albumCover.setShapeAppearanceModel(binding.albumCover.getShapeAppearanceModel().toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, getResources().getDimension(R.dimen.track_add_info_dialog_cover_corner_size))
                .build());

        return new MaterialAlertDialogBuilder(requireContext())
                .setView(binding.getRoot())
                .create();
    }


    @ProvidePresenter
    TrackAdditionalInfoPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        int trackId = requireArguments().getInt(TRACK_ID_BUNDLE_KEY);
        return new TrackAdditionalInfoPresenter(appComponent, trackId);
    }

    @Override
    public void setTrack(Track track) {
        binding.artist.setText(track.getArtistName());
        binding.album.setText(track.getAlbumTitle());
        binding.albumPosition.setText(String.valueOf(track.getPositionInCd()));
        binding.title.setText(track.getTitle());

        binding.duration.setText(TimeFormatterTool.formatMillisecondsToMinutes(track.getDurationMs()));
        if (track.getGenreName().isEmpty())
            binding.genreWrapper.setVisibility(View.GONE);
        else
            binding.genre.setText(track.getGenreName());
        binding.year.setText(String.valueOf(track.getYear()));
        binding.filepath.setText(track.getFilePath());

        binding.fileSize.setText(getString(R.string.track_add_info_file_size_format, track.getFileSizeKilobytes()));
        binding.bitrate.setText(getString(R.string.track_add_info_bitrate_format, track.getBitrate()));
        binding.sampleRate.setText(getString(R.string.track_add_info_sample_rate_format, track.getSampleRate()));

        Bitmap artImage = track.getArtImage();

        if (artImage != null)
            binding.albumCover.setImageBitmap(artImage);
        else binding.albumCover.setImageResource(R.drawable.album_default);
    }

    @Override
    public void closeScreen() {
        dismiss();
    }
}
