package com.parabola.newtone.ui.fragment.playlist;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.ImageViewCompat;
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
import com.parabola.newtone.mvp.presenter.FavouritesPlaylistPresenter;
import com.parabola.newtone.mvp.view.FavouritesPlaylistView;
import com.parabola.newtone.ui.base.BaseDialogFragment;
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment;
import com.parabola.newtone.ui.dialog.DialogDismissLifecycleObserver;
import com.parabola.newtone.ui.fragment.Scrollable;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static com.parabola.domain.utils.TracklistTool.isTracklistsIdentical;
import static com.parabola.newtone.util.AndroidTool.createDeleteTrackDialog;
import static java.util.Objects.requireNonNull;

public final class FavoritesPlaylistFragment extends BaseSwipeToBackFragment
        implements FavouritesPlaylistView, Scrollable {

    private final TrackAdapter tracklistAdapter = new TrackAdapter();
    private DividerItemDecoration itemDecoration;

    @InjectPresenter FavouritesPlaylistPresenter presenter;

    @BindView(R.id.action_bar) LinearLayout actionBar;
    @BindView(R.id.tracks_list) RecyclerView tracklistView;
    @BindView(R.id.main) TextView playlistTitle;
    @BindView(R.id.additional_info) TextView tracksCountTxt;
    private ImageButton dragSwitcherButton;


    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        View contentView = inflater.inflate(R.layout.list_track, container, false);
        ((ViewGroup) root.findViewById(R.id.container)).addView(contentView);
        ButterKnife.bind(this, root);

        initDragSwitcherButton();

        tracklistView.setAdapter(tracklistAdapter);
        itemDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);

        tracklistAdapter.setOnItemClickListener(position -> presenter.onClickTrackItem(
                tracklistAdapter.getAll(), position));

        playlistTitle.setText(R.string.playlist_favourites);

        return root;
    }

    private void initDragSwitcherButton() {
        dragSwitcherButton = new AppCompatImageButton(requireContext());
        dragSwitcherButton.setImageResource(R.drawable.ic_drag);
        int imageSize = (int) getResources().getDimension(R.dimen.playlist_fragment_drag_switcher_size);
        dragSwitcherButton.setLayoutParams(new LinearLayout.LayoutParams(imageSize, imageSize));
        actionBar.addView(dragSwitcherButton);
        dragSwitcherButton.setOnClickListener(v -> {
            if (tracklistView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE)
                presenter.onClickDragSwitcher();
        });

        ColorStateList backgroundTintList = ContextCompat.getColorStateList(requireContext(), R.color.drag_button_background_color_selector);
        ViewCompat.setBackgroundTintList(dragSwitcherButton, backgroundTintList);
        ColorStateList imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.colorNewtoneIconTint);
        ImageViewCompat.setImageTintList(dragSwitcherButton, imageTintList);
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
    public void setPlaylistChangerActivation(boolean activate) {
        dragSwitcherButton.setSelected(activate);
        tracklistAdapter.setMoveItemIconVisibility(activate);

        if (activate) {
            tracklistAdapter.setOnItemLongClickListener(null);
            tracklistAdapter.setOnSwipeItemListener(position -> {
                int removedTrackId = tracklistAdapter.get(position).getId();
                tracklistAdapter.remove(position);
                presenter.onRemoveItem(removedTrackId);
            });
            tracklistAdapter.setOnMoveItemListener(presenter::onMoveItem);
        } else {
            tracklistAdapter.setOnItemLongClickListener(this::showTrackContextMenu);
            tracklistAdapter.setOnSwipeItemListener(null);
            tracklistAdapter.setOnMoveItemListener(null);
        }
    }

    @Override
    public void refreshTracks(List<Track> trackList) {
        String tracksCount = getResources()
                .getQuantityString(R.plurals.tracks_count, trackList.size(), trackList.size());
        tracksCountTxt.setText(tracksCount);
        if (!isTracklistsIdentical(trackList, tracklistAdapter.getAll())) {
            tracklistAdapter.replaceAll(trackList);
        }
    }

    @Override
    public void setItemViewSettings(TrackItemView viewSettings) {
        tracklistAdapter.setViewSettings(viewSettings);
    }

    @Override
    public void setItemDividerShowing(boolean showed) {
        tracklistView.removeItemDecoration(itemDecoration);

        if (showed)
            tracklistView.addItemDecoration(itemDecoration);
    }


    @Override
    public void setCurrentTrack(int trackId) {
        tracklistAdapter.setSelectedCondition(track -> track.getId() == trackId);
    }


    @ProvidePresenter
    FavouritesPlaylistPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        return new FavouritesPlaylistPresenter(appComponent);
    }

    private void showTrackContextMenu(int position) {
        Track selectedTrack = tracklistAdapter.get(position);
        ListPopupWindowAdapter menuAdapter = new ListPopupWindowAdapter(requireContext(), R.menu.track_menu);

        menuAdapter.setMenuVisibility(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.add_to_favorites:
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
        dialog.setOnShowListener(d -> tracklistAdapter.setContextSelected(position));
        dialog.setOnDismissListener(d -> tracklistAdapter.clearContextSelected());
        getLifecycle().addObserver(new DialogDismissLifecycleObserver(dialog));
        dialog.show();
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
                AlertDialog dialog = createDeleteTrackDialog(requireContext(), (d, w) -> {
                    tracklistAdapter.remove(itemPosition);
                    presenter.onClickMenuDeleteTrack(selectedTrack.getId());
                });

                DialogFragment dialogFragment = BaseDialogFragment.build(dialog);
                dialogFragment.show(requireActivity().getSupportFragmentManager(), null);
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
