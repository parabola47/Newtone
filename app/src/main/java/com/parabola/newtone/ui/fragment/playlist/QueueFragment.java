package com.parabola.newtone.ui.fragment.playlist;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parabola.domain.model.Track;
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.BaseAdapter;
import com.parabola.newtone.adapter.QueueAdapter;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.QueuePresenter;
import com.parabola.newtone.mvp.view.QueueView;
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public final class QueueFragment extends BaseSwipeToBackFragment
        implements QueueView {

    @InjectPresenter QueuePresenter presenter;

    @BindView(R.id.main) TextView playlistTitle;
    @BindView(R.id.additional_info) TextView songsCount;

    @BindView(R.id.tracks_list) RecyclerView queueList;

    private final QueueAdapter queueAdapter = new QueueAdapter();

    public QueueFragment() {
        // Required empty public constructor
    }


    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = super.onCreateView(inflater, container, savedInstanceState);
        View contentView = inflater.inflate(R.layout.list_track, container, false);
        ((ViewGroup) root.findViewById(R.id.container)).addView(contentView);
        ButterKnife.bind(this, root);
        playlistTitle.setText(R.string.playlist_queue);

        queueList.setAdapter(queueAdapter);
        queueAdapter.setItemClickListener(position -> presenter.onClickTrackItem(queueAdapter.getAll(), position));
        queueAdapter.setRemoveClickListener(presenter::onRemoveItem);
        queueAdapter.setDragListener(new BaseAdapter.DragListener() {
            @Override
            public void onSwipeItem(int position) {
                presenter.onRemoveItem(position);
            }

            @Override
            public void onMoveItem(int oldPosition, int newPosition) {
                presenter.onMoveItem(oldPosition, newPosition);
            }
        });

        return root;
    }

    @OnClick(R.id.action_bar)
    public void onClickActionBar() {
        presenter.onClickActionBar();
    }

    @Override
    protected void onClickBackButton() {
        presenter.onClickBack();
    }

    @Override
    protected void onEndSlidingAnimation() {
        presenter.onEnterSlideAnimationEnded();
    }

    @ProvidePresenter
    public QueuePresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        return new QueuePresenter(appComponent);
    }

    @Override
    public void refreshTracks(List<Track> tracks) {
        if (!isNewTracksIdenticalWithAdapter(tracks)) {
            queueAdapter.replaceAll(tracks);
        }
    }

    @Override
    public void setItemViewSettings(TrackItemView viewSettings) {
        queueAdapter.setViewSettings(viewSettings);
    }


    private boolean isNewTracksIdenticalWithAdapter(List<Track> tracks) {
        if (tracks.size() != queueAdapter.size()) {
            return false;
        }
        for (int i = 0; i < tracks.size(); i++) {
            if (tracks.get(i).getId() != queueAdapter.get(i).getId())
                return false;
        }
        return true;
    }

    @Override
    public void setTrackCount(int tracksCount) {
        String tracksCountStr = getResources()
                .getQuantityString(R.plurals.tracks_count, tracksCount, tracksCount);

        songsCount.setText(tracksCountStr);
    }

    @Override
    public void setCurrentTrackPosition(int currentTrackPosition) {
        try {
            queueAdapter.setSelected(currentTrackPosition);
        } catch (IndexOutOfBoundsException ignored) {
            queueAdapter.clearSelected();
        }
    }

    @Override
    public void removeTrackByPosition(int position) {
        queueAdapter.remove(position);
    }

    @Override
    public void goToItem(int itemPosition) {
        queueList.scrollToPosition(itemPosition);
        queueList.stopScroll();
    }
}
