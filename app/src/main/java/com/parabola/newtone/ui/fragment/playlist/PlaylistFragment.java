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
import com.parabola.newtone.mvp.presenter.PlaylistPresenter;
import com.parabola.newtone.mvp.view.PlaylistView;
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

public final class PlaylistFragment extends BaseSwipeToBackFragment
        implements PlaylistView, Scrollable {

    @InjectPresenter PlaylistPresenter presenter;

    private final TrackAdapter tracksAdapter = new TrackAdapter();

    @BindView(R.id.action_bar) LinearLayout actionBar;

    @BindView(R.id.tracks_list) RecyclerView tracksList;
    @BindView(R.id.additional_info) TextView songsCountTxt;
    @BindView(R.id.main) TextView playlistTitleTxt;
    private ImageButton dragSwitcherButton;
    private DividerItemDecoration itemDecoration;


    private static final String SELECTED_PLAYLIST_ID = "playlist id";

    public static PlaylistFragment newInstance(int playlistId) {
        Bundle args = new Bundle();
        args.putInt(SELECTED_PLAYLIST_ID, playlistId);

        PlaylistFragment fragment = new PlaylistFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        View contentView = inflater.inflate(R.layout.list_track, container, false);
        ((ViewGroup) root.findViewById(R.id.container)).addView(contentView);
        ButterKnife.bind(this, root);

        initDragSwitcherButton();

        tracksList.setAdapter(tracksAdapter);
        itemDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);

        tracksAdapter.setOnItemClickListener(position -> presenter.onClickTrackItem(tracksAdapter.getAll(), position));
        tracksAdapter.setOnRemoveClickListener(position -> {
            int removedTrackId = tracksAdapter.get(position).getId();
            tracksAdapter.remove(position);
            presenter.onRemoveItem(removedTrackId);
        });

        return root;
    }

    private void initDragSwitcherButton() {
        dragSwitcherButton = new AppCompatImageButton(requireContext());
        dragSwitcherButton.setImageResource(R.drawable.ic_drag);
        int imageSize = (int) getResources().getDimension(R.dimen.playlist_fragment_drag_switcher_size);
        dragSwitcherButton.setLayoutParams(new LinearLayout.LayoutParams(imageSize, imageSize));
        actionBar.addView(dragSwitcherButton);
        dragSwitcherButton.setOnClickListener(v -> {
            if (tracksList.getScrollState() == RecyclerView.SCROLL_STATE_IDLE)
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


    private void showTrackContextMenu(int position) {
        Track selectedTrack = tracksAdapter.get(position);
        ListPopupWindowAdapter menuAdapter = new ListPopupWindowAdapter(requireContext(), R.menu.track_menu);

        menuAdapter.setMenuVisibility(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.add_to_favorites:
                    return !selectedTrack.isFavourite();
                case R.id.remove_from_favourites:
                    return selectedTrack.isFavourite();
                case R.id.add_to_playlist:
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
        getLifecycle().addObserver(new DialogDismissLifecycleObserver(dialog));
        dialog.show();
    }


    private void handleSelectedMenu(MenuItem menuItem, Track selectedTrack, int itemPosition) {
        switch (menuItem.getItemId()) {
            case R.id.play:
                List<Track> tracks = tracksAdapter.getAll();
                presenter.onClickMenuPlay(tracks, itemPosition);
                break;
            case R.id.remove_from_playlist:
                presenter.onClickMenuRemoveFromCurrentPlaylist(selectedTrack.getId());
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

    @ProvidePresenter
    public PlaylistPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        int playlistId = requireArguments().getInt(SELECTED_PLAYLIST_ID);

        return new PlaylistPresenter(appComponent, playlistId);
    }


    @Override
    public void setPlaylistChangerActivation(boolean activate) {
        dragSwitcherButton.setSelected(activate);
        tracksAdapter.setMoveItemIconVisibility(activate);
        tracksAdapter.setRemoveItemIconVisibility(activate);

        if (activate) {
            tracksAdapter.setOnItemLongClickListener(null);
            tracksAdapter.setOnSwipeItemListener(position -> {
                int removedTrackId = tracksAdapter.get(position).getId();
                tracksAdapter.remove(position);
                presenter.onRemoveItem(removedTrackId);
            });
            tracksAdapter.setOnMoveItemListener(presenter::onMoveItem);
        } else {
            tracksAdapter.setOnItemLongClickListener(this::showTrackContextMenu);
            tracksAdapter.setOnSwipeItemListener(null);
            tracksAdapter.setOnMoveItemListener(null);
        }
    }

    @Override
    public void setTracksCount(int playlistSize) {
        songsCountTxt.setText(getResources().getQuantityString(R.plurals.tracks_count, playlistSize, playlistSize));
    }

    @Override
    public void setPlaylistTitle(String playlistTitle) {
        playlistTitleTxt.setText(playlistTitle);
    }

    @Override
    public void refreshTracks(List<Track> tracks) {
        if (!isTracklistsIdentical(tracks, tracksAdapter.getAll())) {
            tracksAdapter.replaceAll(tracks);
        }
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
    public void setCurrentTrack(int trackId) {
        tracksAdapter.setSelectedCondition(track -> track.getId() == trackId);
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
