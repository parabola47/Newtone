package com.parabola.newtone.ui.fragment;

import static com.parabola.domain.utils.TracklistTool.isTracklistsIdentical;
import static com.parabola.newtone.util.AndroidTool.getStyledColor;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.parabola.domain.interactor.player.PlayerInteractor.RepeatMode;
import com.parabola.domain.model.Track;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.ListPopupWindowAdapter;
import com.parabola.newtone.databinding.FragmentPlayerBinding;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.PlayerPresenter;
import com.parabola.newtone.mvp.view.PlayerView;
import com.parabola.newtone.util.TimeFormatterTool;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.internal.observers.ConsumerSingleObserver;
import io.reactivex.schedulers.Schedulers;
import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public final class PlayerFragment extends MvpAppCompatFragment
        implements PlayerView {
    private static final String LOG_TAG = PlayerFragment.class.getSimpleName();

    @InjectPresenter PlayerPresenter presenter;

    private FragmentPlayerBinding binding;

    private final AlbumCoverPagerAdapter albumCoverAdapter = new AlbumCoverPagerAdapter();

    public PlayerFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentPlayerBinding.inflate(inflater, container, false);

        binding.durationProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String currentTimeFormatted = TimeFormatterTool.formatMillisecondsToMinutes(progress);
                binding.currentTime.setText(currentTimeFormatted);
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
        binding.albumCoverContainer.setAdapter(albumCoverAdapter);
        binding.albumCoverContainer.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
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

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        binding.queue.setOnClickListener(v -> presenter.onClickQueue());
        binding.audioEffects.setOnClickListener(v -> presenter.onClickAudioEffects());
        binding.audioEffects.setOnLongClickListener(v -> {
            presenter.onLongClickAudioEffects();
            return true;
        });
        binding.dropDown.setOnClickListener(v -> presenter.onClickDropDown());
        binding.favourite.setOnClickListener(v -> presenter.onClickFavourite());
        binding.favourite.setOnLongClickListener(v -> {
            presenter.onLongClickFavorite();
            return true;
        });
        binding.timer.setOnClickListener(v -> presenter.onClickTimerButton());
        binding.timer.setOnLongClickListener(v -> {
            presenter.onLongClickTimerButton();
            return true;
        });
        binding.trackSettings.setOnClickListener(v -> onClickTrackSettings());


        binding.artist.setOnClickListener(v -> presenter.onClickArtist());
        binding.artist.setOnLongClickListener(v -> {
            presenter.onClickArtist();
            return true;
        });
        binding.album.setOnClickListener(v -> presenter.onClickAlbum());
        binding.album.setOnLongClickListener(v -> {
            presenter.onClickAlbum();
            return true;
        });
        binding.title.setOnClickListener(v -> presenter.onClickTrackTitle());
        binding.title.setOnLongClickListener(v -> {
            presenter.onClickTrackTitle();
            return true;
        });


        binding.playerToggle.setOnClickListener(v -> presenter.onClickPlayButton());
        binding.prevTrack.setOnClickListener(v -> presenter.onClickPrevTrack());
        binding.nextTrack.setOnClickListener(v -> presenter.onClickNextTrack());
        binding.loop.setOnClickListener(v -> presenter.onClickLoop());
        binding.shuffle.setOnClickListener(v -> presenter.onClickShuffle());
    }


    public void onClickTrackSettings() {
        ListPopupWindow popupWindow = new ListPopupWindow(requireContext());

        ListPopupWindowAdapter adapter = new ListPopupWindowAdapter(requireContext(), R.menu.player_menu);
        popupWindow.setAdapter(adapter);
        popupWindow.setAnchorView(requireView().findViewById(R.id.menu_tmp));
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
            case R.id.timer:
                presenter.onClickMenuTimer();
                break;
            case R.id.lyrics:
                presenter.onClickMenuLyrics();
                break;
            case R.id.share:
                presenter.onClickMenuShareTrack();
                break;
            case R.id.additional_info:
                presenter.onClickMenuAdditionalInfo();
                break;
            case R.id.delete:
                presenter.onClickMenuDelete();
                break;
            case R.id.settings:
                presenter.onClickMenuSettings();
                break;
        }
    }


    @ProvidePresenter
    PlayerPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        return new PlayerPresenter(appComponent);
    }


    @Override
    public void setArtist(String artistName) {
        binding.artist.setText(artistName);
    }

    @Override
    public void setAlbum(String albumTitle) {
        binding.album.setText(albumTitle);
    }

    @Override
    public void setTitle(String trackTitle) {
        binding.title.setText(trackTitle);
    }

    @Override
    public void setDurationText(String durationFormatted) {
        binding.durationTxt.setText(durationFormatted);
    }

    @Override
    public void setDurationMs(int durationMs) {
        binding.durationProgress.setMax(durationMs);
    }

    @Override
    public void setIsFavourite(boolean isFavourite) {
        if (isFavourite) binding.favourite.setImageResource(R.drawable.ic_favourite_select);
        else binding.favourite.setImageResource(R.drawable.ic_favourite);
    }

    @Override
    public void setPlaybackButtonAsPause() {
        binding.playerToggle.setImageResource(R.drawable.ic_pause);
    }


    @Override
    public void setPlaybackButtonAsPlay() {
        binding.playerToggle.setImageResource(R.drawable.ic_play);
    }

    @Override
    public void setRepeatMode(RepeatMode repeatMode) {
        switch (repeatMode) {
            case OFF:
                binding.loop.setImageResource(R.drawable.ic_loop);
                binding.loop.setColorFilter(ContextCompat.getColor(requireContext(), R.color.colorPlayerActionIconDefault));
                break;
            case ALL:
                binding.loop.setImageResource(R.drawable.ic_loop);
                binding.loop.setColorFilter(getStyledColor(requireContext(), R.attr.colorPrimary));
                break;
            case ONE:
                binding.loop.setImageResource(R.drawable.ic_loop_one);
                binding.loop.setColorFilter(getStyledColor(requireContext(), R.attr.colorPrimary));
                break;
        }

    }

    @Override
    public void setShuffleEnabling(boolean enable) {
        int color = enable ? getStyledColor(requireContext(), R.attr.colorPrimary)
                : ContextCompat.getColor(requireContext(), R.color.colorPlayerActionIconDefault);

        binding.shuffle.setColorFilter(color);
    }

    @Override
    public void setCurrentTimeMs(int currentTimeMs) {
        binding.durationProgress.setProgress(currentTimeMs);

        String currentTimeFormatted = TimeFormatterTool.formatMillisecondsToMinutes(currentTimeMs);
        binding.currentTime.setText(currentTimeFormatted);
    }

    @Override
    public void setTimerButtonVisibility(boolean visible) {
        binding.timer.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setViewPagerSlide(boolean lock) {
        binding.albumCoverContainer.setSwipeLocked(lock);
    }

    @Override
    public void refreshTracks(List<Track> tracks) {
        if (isTracklistsIdentical(tracks, albumCoverAdapter.tracks)) return;

        albumCoverAdapter.tracks.clear();
        albumCoverAdapter.tracks.addAll(tracks);
        try {
            albumCoverAdapter.notifyDataSetChanged();
        } catch (NullPointerException ignored) {
        }
    }


    @Override
    public void setAlbumImagePosition(int currentTrackPosition, boolean smooth) {
        binding.albumCoverContainer.setCurrentItem(currentTrackPosition, smooth);
    }

    @Override
    public void setTrackSettingsRotation(float rotation) {
        binding.trackSettings.setRotation(rotation);
    }

    @Override
    public void setRootViewOpacity(float alpha) {
        View root = getView();
        if (root != null)
            root.setAlpha(alpha);
    }

    @Override
    public void setRootViewVisibility(boolean visible) {
        View root = getView();
        if (root != null)
            root.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private static class AlbumCoverPagerAdapter extends PagerAdapter {
        private final List<Track> tracks = new ArrayList<>();

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            ShapeableImageView albumCover = new ShapeableImageView(container.getContext());
            float cornerSizePx = container.getContext().getResources().getDimension(R.dimen.player_fragment_album_cover_corner_size);
            albumCover.setShapeAppearanceModel(albumCover.getShapeAppearanceModel().toBuilder()
                    .setAllCorners(CornerFamily.ROUNDED, cornerSizePx)
                    .build());

            Single.fromCallable(() -> (Bitmap) tracks.get(position).getArtImage())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new ConsumerSingleObserver<>(
                            albumCover::setImageBitmap,
                            error -> albumCover.setImageResource(R.drawable.album_default)));

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
