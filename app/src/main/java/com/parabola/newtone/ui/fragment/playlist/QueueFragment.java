package com.parabola.newtone.ui.fragment.playlist;


import static com.parabola.domain.utils.TracklistTool.isTracklistsIdentical;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.parabola.domain.model.Track;
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.TrackAdapter;
import com.parabola.newtone.databinding.ListTrackBinding;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.QueuePresenter;
import com.parabola.newtone.mvp.view.QueueView;
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment;

import java.util.List;
import java.util.OptionalInt;

import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public final class QueueFragment extends BaseSwipeToBackFragment
        implements QueueView {

    @InjectPresenter QueuePresenter presenter;

    private ListTrackBinding binding;

    private final TrackAdapter queueAdapter = new TrackAdapter();
    private DividerItemDecoration itemDecoration;

    public QueueFragment() {
        // Required empty public constructor
    }


    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        binding = ListTrackBinding.inflate(inflater, container, false);
        getRootBinding().container.addView(binding.getRoot());

        getRootBinding().main.setText(R.string.playlist_queue);

        binding.tracksList.setAdapter(queueAdapter);
        itemDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);

        queueAdapter.setMoveItemIconVisibility(true);
        queueAdapter.setOnItemClickListener(position -> presenter.onClickTrackItem(queueAdapter.getAll(), position));
        queueAdapter.setOnMoveItemListener(presenter::onMoveItem);
        queueAdapter.setOnSwipeItemListener(position -> {
            queueAdapter.remove(position);
            presenter.onRemoveItem(position);
        });
        getRootBinding().actionBar.setOnClickListener(v -> presenter.onClickActionBar());

        return root;
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
        if (!isTracklistsIdentical(tracks, queueAdapter.getAll())) {
            queueAdapter.replaceAll(tracks);
        }
    }

    @Override
    public void setItemViewSettings(TrackItemView viewSettings) {
        queueAdapter.setViewSettings(viewSettings);
    }

    @Override
    public void setItemDividerShowing(boolean showed) {
        binding.tracksList.removeItemDecoration(itemDecoration);

        if (showed)
            binding.tracksList.addItemDecoration(itemDecoration);
    }


    @Override
    public void setTrackCount(int tracksCount) {
        String tracksCountStr = getResources()
                .getQuantityString(R.plurals.tracks_count, tracksCount, tracksCount);

        getRootBinding().additionalInfo.setText(tracksCountStr);
    }

    @Override
    public void setCurrentTrackPosition(int currentTrackPosition) {
        OptionalInt oldSelectedPosition = queueAdapter.getSelectedPosition();
        if (oldSelectedPosition.isPresent()
                && oldSelectedPosition.getAsInt() == currentTrackPosition) {
            return;
        }
        try {
            queueAdapter.setSelected(currentTrackPosition);
        } catch (IndexOutOfBoundsException ignored) {
            queueAdapter.clearSelected();
        }
    }

    @Override
    public void goToItem(int itemPosition) {
        binding.tracksList.scrollToPosition(itemPosition);
        binding.tracksList.stopScroll();
    }
}
