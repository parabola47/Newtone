package com.parabola.newtone.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
import com.parabola.newtone.mvp.presenter.FolderPresenter;
import com.parabola.newtone.mvp.view.FolderView;
import com.parabola.newtone.ui.base.BaseDialogFragment;
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment;
import com.parabola.newtone.ui.dialog.SortingDialog;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static com.parabola.newtone.util.AndroidTool.createDeleteTrackDialog;
import static java.util.Objects.requireNonNull;

public final class FolderFragment extends BaseSwipeToBackFragment
        implements FolderView, Sortable, Scrollable {

    private final TrackAdapter tracksAdapter = new TrackAdapter();

    @InjectPresenter FolderPresenter presenter;

    @BindView(R.id.additional_info) TextView tracksCount;
    @BindView(R.id.tracks_list) RecyclerView tracksList;
    @BindView(R.id.main) TextView folderTxt;

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        View contentView = inflater.inflate(R.layout.list_track, container, false);
        ((ViewGroup) root.findViewById(R.id.container)).addView(contentView);
        ButterKnife.bind(this, root);

        tracksList.setAdapter(tracksAdapter);
        tracksAdapter.setOnItemClickListener(position -> presenter.onTrackClick(tracksAdapter.getAll(), position));
        tracksAdapter.setOnItemLongClickListener(this::showTrackContextMenu);

        return root;
    }

    @OnClick(R.id.action_bar)
    public void onClickActionBar() {
        smoothScrollToTop();
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


    @Override
    public void refreshTracks(List<Track> tracks) {
        tracksAdapter.replaceAll(tracks);

        String tracksCountStr = getResources()
                .getQuantityString(R.plurals.tracks_count, tracks.size(), tracks.size());

        tracksCount.setText(tracksCountStr);
    }

    @Override
    public void setItemViewSettings(TrackItemView viewSettings) {
        tracksAdapter.setViewSettings(viewSettings);
    }

    @Override
    public void setSectionShowing(boolean enable) {
        tracksAdapter.setSectionEnabled(enable);
    }

    @Override
    public void setCurrentTrack(int trackId) {
        tracksAdapter.setSelectedCondition(track -> track.getId() == trackId);
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
    public void setFolderPath(String folderPath) {
        folderTxt.setText(folderPath);
    }

    @ProvidePresenter
    FolderPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        String folderPath = requireArguments().getString("folderPath");
        return new FolderPresenter(appComponent, folderPath);
    }

    @Override
    public String getListType() {
        return SortingDialog.FOLDER_TRACKS_SORTING;
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
