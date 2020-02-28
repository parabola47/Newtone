package com.parabola.newtone.ui.dialog;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.bumptech.glide.Glide;
import com.parabola.domain.model.Track;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.mvp.presenter.TrackAdditionalInfoPresenter;
import com.parabola.newtone.mvp.view.TrackAdditionalInfoView;
import com.parabola.newtone.util.TimeFormatterTool;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class TrackAdditionalInfoDialog extends BaseDialogFragment
        implements TrackAdditionalInfoView {

    @BindView(R.id.artist) TextView artistTextView;
    @BindView(R.id.albumTitle) TextView albumTitleTextView;
    @BindView(R.id.albumPosition) TextView albumPositionTextView;
    @BindView(R.id.title) TextView titleTextView;

    @BindView(R.id.duration) TextView durationTextView;
    @BindView(R.id.genreTitle) TextView genreTextView;
    @BindView(R.id.year) TextView yearTextView;
    @BindView(R.id.filepath) TextView filepathTextView;

    @BindView(R.id.fileSize) TextView fileSizeTextView;
    @BindView(R.id.bitrate) TextView bitrateTextView;
    @BindView(R.id.sampleRate) TextView sampleRateTextView;

    @BindView(R.id.albumCover) ImageView albumCoverImageView;


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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.dialog_track_addition_info, container, false);
        ButterKnife.bind(this, layout);

        return layout;
    }

    @OnClick(R.id.cancel)
    public void onClickCancel() {
        presenter.onClickCancel();
    }

    @ProvidePresenter
    TrackAdditionalInfoPresenter providePresenter() {
        int trackId = requireArguments().getInt(TRACK_ID_BUNDLE_KEY);
        return new TrackAdditionalInfoPresenter(MainApplication.getComponent(), trackId);
    }

    @Override
    public void setTrack(Track track) {
        artistTextView.setText(track.getArtistName());
        albumTitleTextView.setText(track.getAlbumTitle());
        albumPositionTextView.setText(String.valueOf(track.getPositionInCd()));
        titleTextView.setText(track.getTitle());

        durationTextView.setText(TimeFormatterTool.formatMillisecondsToMinutes(track.getDurationMs()));
        genreTextView.setText(track.getGenreName());
        yearTextView.setText(String.valueOf(track.getYear()));
        filepathTextView.setText(track.getFilePath());

        fileSizeTextView.setText(getString(R.string.track_add_info_file_size_format, track.getFileSizeKilobytes()));
        bitrateTextView.setText(getString(R.string.track_add_info_bitrate_format, track.getBitrate()));
        sampleRateTextView.setText(getString(R.string.track_add_info_sample_rate_format, track.getSampleRate()));

        Glide.with(this)
                .load((Bitmap) track.getArtImage())
                .placeholder(R.drawable.album_holder)
                .into(albumCoverImageView);
    }

    @Override
    public void closeScreen() {
        dismiss();
    }
}
