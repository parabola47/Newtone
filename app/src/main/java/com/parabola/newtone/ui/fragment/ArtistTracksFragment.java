package com.parabola.newtone.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
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
import com.parabola.newtone.mvp.presenter.ArtistTracksPresenter;
import com.parabola.newtone.mvp.view.ArtistTracksView;
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

public final class ArtistTracksFragment extends BaseSwipeToBackFragment
        implements ArtistTracksView, Sortable, Scrollable {

    @InjectPresenter ArtistTracksPresenter presenter;

    @BindView(R.id.tracks_list) RecyclerView tracksList;
    private DividerItemDecoration itemDecoration;

    @BindView(R.id.main) TextView artistTxt;
    @BindView(R.id.additional_info) TextView tracksCountTxt;

    private final TrackAdapter tracksAdapter = new TrackAdapter();

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        View contentView = inflater.inflate(R.layout.list_track, container, false);
        ((ViewGroup) root.findViewById(R.id.container)).addView(contentView);
        ButterKnife.bind(this, root);

        tracksList.setAdapter(tracksAdapter);
        itemDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);

        tracksAdapter.setOnItemClickListener(position -> presenter.onClickTrackItem(tracksAdapter.getAll(), position));
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
    protected void onClickBackButton() {
        presenter.onClickBack();
    }

    @Override
    protected void onEndSlidingAnimation() {
        presenter.onEnterSlideAnimationEnded();
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
    public void setItemDividerShowing(boolean showed) {
        tracksList.removeItemDecoration(itemDecoration);

        if (showed)
            tracksList.addItemDecoration(itemDecoration);
    }


    @Override
    public void setSectionShowing(boolean enable) {
        tracksAdapter.setSectionEnabled(enable);
    }

    @Override
    public void setArtistName(String artistName) {
        artistTxt.setText(artistName);
    }

    @Override
    public void setTracksCountTxt(String tracksCountStr) {
        tracksCountTxt.setText(tracksCountStr);
    }


    @Override
    public void setCurrentTrack(int trackId) {
        tracksAdapter.setSelectedCondition(track -> track.getId() == trackId);
    }

    @ProvidePresenter
    ArtistTracksPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        int artistId = requireArguments().getInt("artistId");
        return new ArtistTracksPresenter(appComponent, artistId);
    }

    @Override
    public String getListType() {
        return SortingDialog.ARTIST_TRACKS_SORTING;
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
