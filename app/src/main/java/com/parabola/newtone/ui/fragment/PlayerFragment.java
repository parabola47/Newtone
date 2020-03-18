package com.parabola.newtone.ui.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.parabola.domain.model.Track;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.ListPopupWindowAdapter;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.PlayerPresenter;
import com.parabola.newtone.mvp.view.PlayerView;
import com.parabola.newtone.ui.view.LockableViewPager;
import com.parabola.newtone.util.TimeFormatterTool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.internal.observers.BiConsumerSingleObserver;
import io.reactivex.schedulers.Schedulers;
import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static com.parabola.newtone.util.AndroidTool.convertDpToPixel;

public final class PlayerFragment extends MvpAppCompatFragment
        implements PlayerView {

    @InjectPresenter PlayerPresenter presenter;

    @BindView(R.id.artist) TextView artist;
    @BindView(R.id.album) TextView album;
    @BindView(R.id.title) TextView title;
    @BindView(R.id.song_duration) TextView duration;
    @BindView(R.id.current_time) TextView currentTime;
    @BindView(R.id.seek_player) SeekBar seekPlayer;
    @BindView(R.id.favourite) ImageButton favourite;

    @BindView(R.id.player_toggle) ImageButton playerToggle;
    @BindView(R.id.loop) ImageButton loopButton;
    @BindView(R.id.shuffle) ImageButton shuffleButton;
    @BindView(R.id.timer) ImageButton timerButton;
    @BindView(R.id.track_settings) ImageButton trackSettings;

    @BindView(R.id.album_container) LockableViewPager albumCoverPager;

    private final AlbumCoverPagerAdapter albumCoverAdapter = new AlbumCoverPagerAdapter();

    public PlayerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_player, container, false);
        ButterKnife.bind(this, layout);

        seekPlayer.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String currentTimeFormatted = TimeFormatterTool.formatMillisecondsToMinutes(progress);
                currentTime.setText(currentTimeFormatted);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                presenter.onStartSeekbarPressed();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                presenter.onStopSeekbarPressed(seekBar.getProgress());
            }
        });
        albumCoverPager.setAdapter(albumCoverAdapter);
        albumCoverPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            private int lastPosition;

            @Override
            public void onPageSelected(int position) {
                lastPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    presenter.onSwipeImage(lastPosition);
                }
            }
        });

        return layout;
    }

    @OnClick(R.id.track_settings)
    public void onClickTrackSettings() {
        ListPopupWindow popupWindow = new ListPopupWindow(requireContext());

        ListPopupWindowAdapter adapter = new ListPopupWindowAdapter(requireContext(), R.menu.player_menu);
        popupWindow.setAdapter(adapter);
        popupWindow.setAnchorView(trackSettings);
        popupWindow.setVerticalOffset(-trackSettings.getWidth());
        popupWindow.setModal(true);
        popupWindow.setWidth(adapter.measureContentWidth());
        popupWindow.setOnItemClickListener((parent, view, position, id) -> {
            handleSelectedMenu(adapter.getItem(position));
            popupWindow.dismiss();
        });

        popupWindow.show();
    }

    private void handleSelectedMenu(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.add_to_playlist:
                presenter.onClickMenuAddTrackToPlaylist();
                break;
            case R.id.lyrics:
                presenter.onClickMenuLyrics();
                break;
            case R.id.additional_info:
                presenter.onClickMenuAdditionalInfo();
                break;
            case R.id.delete:
                AlertDialog dialog = new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.track_menu_delete_dialog_title)
                        .setMessage(R.string.track_menu_delete_dialog_message)
                        .setPositiveButton(R.string.dialog_delete, (d, w) -> presenter.onClickMenuDelete())
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .create();
                dialog.getWindow().setBackgroundDrawable(getResources().getDrawable(R.drawable.dialog_bg));
                dialog.getWindow().setLayout((int) convertDpToPixel(250, requireContext()), ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.show();
                break;
            case R.id.share:
                presenter.onClickMenuShareTrack();
                break;
        }
    }

    @OnClick(R.id.timer)
    public void onClickTimerButton() {
        presenter.onClickTimerButton();
    }

    @OnLongClick(R.id.timer)
    public void onLongClickTimerButton() {
        presenter.onLongClickTimerButton();
    }

    @OnClick(R.id.player_toggle)
    public void onClickPlayButton() {
        presenter.onClickPlayButton();
    }

    @OnClick(R.id.next_track)
    public void onClickNextTrack() {
        presenter.onClickNextTrack();
    }

    @OnClick(R.id.prev_track)
    public void onClickPrevTrack() {
        presenter.onClickPrevTrack();
    }

    @OnClick(R.id.queue)
    public void onClickQueue() {
        presenter.onClickQueue();
    }

    @OnClick(R.id.audio_effects)
    public void onClickAudioEffects() {
        presenter.onClickAudioEffects();
    }

    @OnClick(R.id.favourite)
    public void onClickFavourite() {
        presenter.onClickFavourite();
    }

    @OnLongClick(R.id.favourite)
    public void onLongClickFavourite() {
        presenter.onLongClickFavorite();
    }

    @OnClick(R.id.loop)
    public void onClickLoop() {
        presenter.onClickLoop();
    }

    @OnClick(R.id.shuffle)
    public void onClickShuffle() {
        presenter.onClickShuffle();
    }

    @OnClick(R.id.artist)
    @OnLongClick(R.id.artist)
    public void onClickArtist() {
        presenter.onClickArtist();
    }

    @OnClick(R.id.album)
    @OnLongClick(R.id.album)
    public void onClickAlbum() {
        presenter.onClickAlbum();
    }

    @OnClick(R.id.title)
    @OnLongClick(R.id.title)
    public void onClickTrack() {
        presenter.onClickTrackTitle();
    }


    @ProvidePresenter
    PlayerPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        return new PlayerPresenter(appComponent);
    }


    @Override
    public void setArtist(String artistName) {
        artist.setText(artistName);
    }

    @Override
    public void setAlbum(String albumTitle) {
        album.setText(albumTitle);
    }

    @Override
    public void setTitle(String trackTitle) {
        title.setText(trackTitle);
    }

    @Override
    public void setDurationText(String durationFormatted) {
        duration.setText(durationFormatted);
    }

    @Override
    public void setDurationMs(int durationMs) {
        seekPlayer.setMax(durationMs);
    }

    @Override
    public void setIsFavourite(boolean isFavourite) {
        if (isFavourite) favourite.setImageResource(R.drawable.ic_favourite_select);
        else favourite.setImageResource(R.drawable.ic_favourite);
    }

    @Override
    public void setPlaybackButtonAsPause() {
        playerToggle.setImageResource(R.drawable.ic_pause);
    }


    @Override
    public void setPlaybackButtonAsPlay() {
        playerToggle.setImageResource(R.drawable.ic_play);
    }

    @Override
    public void setLoopEnabling(boolean enable) {
        int colorResId = enable ? R.color.colorPlayerActionIconActive : R.color.colorPlayerActionIconDefault;
        loopButton.setColorFilter(ContextCompat.getColor(requireContext(), colorResId));
    }

    @Override
    public void setShuffleEnabling(boolean enable) {
        int colorResId = enable ? R.color.colorPlayerActionIconActive : R.color.colorPlayerActionIconDefault;
        shuffleButton.setColorFilter(ContextCompat.getColor(requireContext(), colorResId));
    }

    @Override
    public void setCurrentTimeMs(int currentTimeMs) {
        seekPlayer.setProgress(currentTimeMs);

        String currentTimeFormatted = TimeFormatterTool.formatMillisecondsToMinutes(currentTimeMs);
        currentTime.setText(currentTimeFormatted);
    }

    @Override
    public void setTimerColored() {
        timerButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorPlayerActionIconActive));
    }

    @Override
    public void setTimerNotColored() {
        timerButton.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorPlayerActionIconDefault));
    }

    @Override
    public void showToast(String toastMessage) {
        Toast.makeText(requireContext(), toastMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setViewPagerSlide(boolean lock) {
        albumCoverPager.setSwipeLocked(lock);
    }

    @Override
    public void refreshTracks(List<Track> tracks) {
        albumCoverAdapter.tracks.clear();
        albumCoverAdapter.tracks.addAll(tracks);
        albumCoverAdapter.notifyDataSetChanged();
    }


    @Override
    public void setAlbumImagePosition(int currentTrackPosition, boolean smooth) {
        albumCoverPager.setCurrentItem(currentTrackPosition, smooth);
    }

    @Override
    public void moveTrack(int oldPosition, int newPosition) {
        if (oldPosition < newPosition)
            Collections.rotate(albumCoverAdapter.tracks.subList(oldPosition, newPosition + 1), newPosition - oldPosition);
        else
            Collections.rotate(albumCoverAdapter.tracks.subList(newPosition, oldPosition + 1), newPosition - oldPosition);

        albumCoverAdapter.notifyDataSetChanged();
    }

    @Override
    public void removeTrack(int position) {
        albumCoverAdapter.tracks.remove(position);
        albumCoverAdapter.notifyDataSetChanged();
    }

    private static class AlbumCoverPagerAdapter extends PagerAdapter {
        private final List<Track> tracks = new ArrayList<>();

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            ImageView albumCover = new ImageView(container.getContext());

            Single.fromCallable(() -> (Bitmap) tracks.get(position).getArtImage())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new BiConsumerSingleObserver<>(
                            (bitmap, throwable) -> Glide.with(albumCover)
                                    .load(bitmap)
                                    .placeholder(R.drawable.album_default)
                                    .into(albumCover)));

            container.addView(albumCover, 0);

            return albumCover;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return tracks.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }


        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }
    }
}
