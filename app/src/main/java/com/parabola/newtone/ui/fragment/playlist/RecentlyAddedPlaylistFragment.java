package com.parabola.newtone.ui.fragment.playlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parabola.domain.model.Track;
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.ListPopupWindowAdapter;
import com.parabola.newtone.adapter.TrackAdapter;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.RecentlyAddedPlaylistPresenter;
import com.parabola.newtone.mvp.view.RecentlyAddedPlaylistView;
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment;
import com.parabola.newtone.ui.fragment.Scrollable;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static com.parabola.newtone.util.AndroidTool.createDeleteTrackDialog;
import static java.util.Objects.requireNonNull;

public final class RecentlyAddedPlaylistFragment extends BaseSwipeToBackFragment
        implements RecentlyAddedPlaylistView, Scrollable {

    private final TrackAdapter tracklistAdapter = new TrackAdapter();
    @InjectPresenter RecentlyAddedPlaylistPresenter presenter;


    @BindView(R.id.tracks_list) RecyclerView tracklistView;
    @BindView(R.id.main) TextView playlistTitle;
    @BindView(R.id.additional_info) TextView songsCount;


    public RecentlyAddedPlaylistFragment() {
    }


    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        View contentView = inflater.inflate(R.layout.list_track, container, false);
        ((ViewGroup) root.findViewById(R.id.container)).addView(contentView);
        ButterKnife.bind(this, root);


        tracklistView.setAdapter(tracklistAdapter);
        tracklistView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        tracklistAdapter.setItemClickListener(position -> presenter.onClickTrackItem(
                tracklistAdapter.getAll(), position));
        tracklistAdapter.setItemLongClickListener(this::showTrackContextMenu);

        playlistTitle.setText(R.string.playlist_recently_added);

        return root;
    }

    @OnClick(R.id.action_bar)
    public void onClickActionBar() {
        smoothScrollToTop();
    }

    @Override
    protected void onClickBackButton() {
        presenter.onClickBack();
    }

    @Override
    protected void onEndSlidingAnimation() {
        presenter.onEnterSlideAnimationEnded();
    }

    @Override
    public void refreshTracks(List<Track> trackList) {
        tracklistAdapter.replaceAll(trackList);
        String tracksCount = getResources()
                .getQuantityString(R.plurals.tracks_count, trackList.size(), trackList.size());
        songsCount.setText(tracksCount);
    }

    @Override
    public void setItemViewSettings(TrackItemView viewSettings) {
        tracklistAdapter.setViewSettings(viewSettings);
    }

    @Override
    public void removeTrack(int trackId) {
        tracklistAdapter.removeWithCondition(track -> track.getId() == trackId);
        String tracksCount = getResources()
                .getQuantityString(R.plurals.tracks_count, tracklistAdapter.size(), tracklistAdapter.size());
        songsCount.setText(tracksCount);
    }

    @Override
    public void setCurrentTrack(int trackId) {
        tracklistAdapter.setSelectedCondition(track -> track.getId() == trackId);
    }

    @Override
    public void clearCurrentItem() {
        tracklistAdapter.clearSelected();
    }

    @Override
    public void clearItems() {
        tracklistAdapter.clear();
    }

    @ProvidePresenter
    RecentlyAddedPlaylistPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        return new RecentlyAddedPlaylistPresenter(appComponent);
    }

    private void showTrackContextMenu(ViewGroup rootView, float x, float y, int itemPosition) {
        Track selectedTrack = tracklistAdapter.get(itemPosition);
        ListPopupWindow popupWindow = new ListPopupWindow(requireContext());
        ListPopupWindowAdapter adapter = new ListPopupWindowAdapter(requireContext(), R.menu.track_menu);

        adapter.setMenuVisibility(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.add_to_favorites:
                    return !selectedTrack.isFavourite();
                case R.id.remove_from_favourites:
                    return selectedTrack.isFavourite();
                case R.id.remove_from_playlist:
                    return false;
                default: return true;
            }
        });
        popupWindow.setAdapter(adapter);

        View tempView = new View(requireContext());
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(0, 0);
        tempView.setLayoutParams(lp);
        tempView.setX(x - rootView.getPaddingLeft());
        tempView.setY(y - rootView.getPaddingTop());
        rootView.addView(tempView);
        popupWindow.setAnchorView(tempView);

        popupWindow.setModal(true);
        popupWindow.setWidth(adapter.measureContentWidth());
        popupWindow.setOnItemClickListener((parent, view, position, id) -> {
            handleSelectedMenu(adapter.getItem(position), selectedTrack, itemPosition);
            popupWindow.dismiss();
        });
        popupWindow.setOnDismissListener(() -> {
            tracklistAdapter.invalidateItem(itemPosition);
            rootView.removeView(tempView);
        });

        popupWindow.show();
        rootView.setBackgroundColor(getResources().getColor(R.color.colorTrackContextMenuBackground));
    }

    private void handleSelectedMenu(MenuItem menuItem, Track selectedTrack, int itemPosition) {
        switch (menuItem.getItemId()) {
            case R.id.play:
                List<Track> tracks = tracklistAdapter.getAll();
                presenter.onClickMenuPlay(tracks, itemPosition);
                break;
            case R.id.add_to_playlist:
                presenter.onClickMenuAddToPlaylist(selectedTrack.getId());
                break;
            case R.id.add_to_favorites:
                presenter.onClickMenuAddToFavourites(selectedTrack.getId());
                break;
            case R.id.remove_from_favourites:
                presenter.onClickMenuRemoveFromFavourites(selectedTrack.getId());
                break;
            case R.id.share_track:
                presenter.onClickMenuShareTrack(selectedTrack);
                break;
            case R.id.additional_info:
                presenter.onClickMenuAdditionalInfo(selectedTrack.getId());
                break;
            case R.id.delete_track:
                AlertDialog dialog = createDeleteTrackDialog(requireContext(), (d, w) -> presenter.onClickMenuDeleteTrack(selectedTrack.getId()));
                dialog.show();
                break;
        }
    }


    @Override
    public void smoothScrollToTop() {
        LinearLayoutManager layoutManager = requireNonNull((LinearLayoutManager) tracklistView.getLayoutManager());
        int firstItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
        int screenItemsCount = layoutManager.findLastVisibleItemPosition() - layoutManager.findFirstVisibleItemPosition();

        if (firstItemPosition > screenItemsCount * 3) {
            tracklistView.scrollToPosition(screenItemsCount * 3);
        }

        tracklistView.smoothScrollToPosition(0);
    }

}
