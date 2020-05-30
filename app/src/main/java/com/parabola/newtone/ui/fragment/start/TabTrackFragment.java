package com.parabola.newtone.ui.fragment.start;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.parabola.domain.model.Track;
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.ListPopupWindowAdapter;
import com.parabola.newtone.adapter.TrackAdapter;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.TabTrackPresenter;
import com.parabola.newtone.mvp.view.TabTrackView;
import com.parabola.newtone.ui.base.BaseDialogFragment;
import com.parabola.newtone.ui.dialog.SortingDialog;
import com.parabola.newtone.ui.fragment.Scrollable;
import com.parabola.newtone.ui.fragment.Sortable;

import java.util.List;
import java.util.OptionalInt;

import butterknife.BindView;
import butterknife.ButterKnife;
import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static com.parabola.newtone.util.AndroidTool.createDeleteTrackDialog;
import static java.util.Objects.requireNonNull;

public final class TabTrackFragment extends MvpAppCompatFragment
        implements TabTrackView, Sortable, Scrollable {

    private static final String LOG_TAG = TabTrackFragment.class.getSimpleName();

    @BindView(R.id.tracks_list) RecyclerView tracksList;

    @InjectPresenter TabTrackPresenter presenter;

    private final TrackAdapter tracksAdapter = new TrackAdapter();

    public TabTrackFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.list_track, container, false);
        ButterKnife.bind(this, layout);


        tracksList.setAdapter(tracksAdapter);

        tracksAdapter.setOnItemClickListener(position -> presenter.onClickTrackItem(tracksAdapter.getAll(), position));
        tracksAdapter.setOnItemLongClickListener(this::showTrackContextMenu);

        return layout;
    }


    public void scrollToCurrentTrack() {
        OptionalInt selectedTrackPosition = tracksAdapter.getSelectedPosition();
        if (selectedTrackPosition.isPresent()) {
            tracksList.scrollToPosition(selectedTrackPosition.getAsInt());
        }
    }


    private void showTrackContextMenu(int position) {
        Track selectedTrack = tracksAdapter.get(position);
        ListPopupWindowAdapter menuAdapter = new ListPopupWindowAdapter(requireContext(), R.menu.track_menu);

        menuAdapter.setMenuVisibility(menuItem -> {
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

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.track_menu_title, selectedTrack.getArtistName(), selectedTrack.getTitle()))
                .setAdapter(menuAdapter, (d, which) ->
                        handleSelectedMenu(menuAdapter.getItem(which), selectedTrack, position))
                .create();
        dialog.setOnShowListener(d -> tracksAdapter.setContextSelected(position));
        dialog.setOnDismissListener(d -> tracksAdapter.clearContextSelected());
        dialog.show();
    }


    private void handleSelectedMenu(MenuItem menuItem, Track selectedTrack, int itemPosition) {
        switch (menuItem.getItemId()) {
            case R.id.play:
                List<Track> tracks = tracksAdapter.getAll();
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
                AlertDialog dialog = createDeleteTrackDialog(requireContext(), (d, w) ->
                        presenter.onClickMenuDeleteTrack(selectedTrack.getId()));

                DialogFragment dialogFragment = BaseDialogFragment.build(dialog);
                dialogFragment.show(requireActivity().getSupportFragmentManager(), null);
                break;
        }
    }


    @ProvidePresenter
    public TabTrackPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        return new TabTrackPresenter(appComponent);
    }

    @Override
    public void refreshTracks(List<Track> tracks) {
        tracksAdapter.replaceAll(tracks);
    }

    @Override
    public void setItemViewSettings(TrackItemView viewSettings) {
        tracksAdapter.setViewSettings(viewSettings);
    }

    @Override
    public void setCurrentTrack(int trackId) {
        tracksAdapter.setSelectedCondition(track -> track.getId() == trackId);
    }

    @Override
    public void setSectionShowing(boolean enable) {
        tracksAdapter.setSectionEnabled(enable);
    }

    @Override
    public void removeTrack(int trackId) {
        tracksAdapter.removeWithCondition(track -> track.getId() == trackId);
    }

    @Override
    public String getListType() {
        return SortingDialog.ALL_TRACKS_SORTING;
    }

    @Override
    public void smoothScrollToTop() {
        LinearLayoutManager layoutManager = requireNonNull((LinearLayoutManager) tracksList.getLayoutManager());
        int firstItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
        int screenItemsCount = layoutManager.findLastVisibleItemPosition() - layoutManager.findFirstVisibleItemPosition();

        if (firstItemPosition > screenItemsCount * 3) {
            tracksList.scrollToPosition(screenItemsCount * 3);
        }

        tracksList.smoothScrollToPosition(0);
    }

}
