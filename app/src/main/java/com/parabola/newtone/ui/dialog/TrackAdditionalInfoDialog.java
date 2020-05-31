package com.parabola.newtone.ui.dialog;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.parabola.domain.model.Track;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.TrackAdditionalInfoPresenter;
import com.parabola.newtone.mvp.view.TrackAdditionalInfoView;
import com.parabola.newtone.util.TimeFormatterTool;

import butterknife.BindView;
import butterknife.ButterKnife;
import moxy.MvpAppCompatDialogFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public final class TrackAdditionalInfoDialog extends MvpAppCompatDialogFragment
        implements TrackAdditionalInfoView {

    @BindView(R.id.artist) TextView artistTextView;
    @BindView(R.id.album) TextView albumTextView;
    @BindView(R.id.albumPosition) TextView albumPositionTextView;
    @BindView(R.id.title) TextView titleTextView;

    @BindView(R.id.duration) TextView durationTextView;
    @BindView(R.id.genreWrapper) ViewGroup genreWrapper;
    @BindView(R.id.genre) TextView genreTextView;
    @BindView(R.id.year) TextView yearTextView;
    @BindView(R.id.filepath) TextView filepathTextView;

    @BindView(R.id.fileSize) TextView fileSizeTextView;
    @BindView(R.id.bitrate) TextView bitrateTextView;
    @BindView(R.id.sampleRate) TextView sampleRateTextView;

    @BindView(R.id.albumCover) ShapeableImageView albumCoverImageView;


    @InjectPresenter TrackAdditionalInfoPresenter presenter;


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
        View layout = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_track_addition_info, null);
        ButterKnife.bind(this, layout);

        albumCoverImageView.setShapeAppearanceModel(albumCoverImageView.getShapeAppearanceModel().toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, getResources().getDimension(R.dimen.track_add_info_dialog_cover_corner_size))
                .build());

        return new MaterialAlertDialogBuilder(requireContext())
                .setView(layout)
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
        artistTextView.setText(track.getArtistName());
        albumTextView.setText(track.getAlbumTitle());
        albumPositionTextView.setText(String.valueOf(track.getPositionInCd()));
        titleTextView.setText(track.getTitle());

        durationTextView.setText(TimeFormatterTool.formatMillisecondsToMinutes(track.getDurationMs()));
        if (track.getGenreName().isEmpty())
            genreWrapper.setVisibility(View.GONE);
        else
            genreTextView.setText(track.getGenreName());
        yearTextView.setText(String.valueOf(track.getYear()));
        filepathTextView.setText(track.getFilePath());

        fileSizeTextView.setText(getString(R.string.track_add_info_file_size_format, track.getFileSizeKilobytes()));
        bitrateTextView.setText(getString(R.string.track_add_info_bitrate_format, track.getBitrate()));
        sampleRateTextView.setText(getString(R.string.track_add_info_sample_rate_format, track.getSampleRate()));

        Bitmap artImage = track.getArtImage();

        if (artImage != null)
            albumCoverImageView.setImageBitmap(artImage);
        else albumCoverImageView.setImageResource(R.drawable.album_default);
    }

    @Override
    public void closeScreen() {
        dismiss();
    }
}
