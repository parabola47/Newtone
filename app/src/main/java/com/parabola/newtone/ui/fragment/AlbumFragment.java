package com.parabola.newtone.ui.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.ListPopupWindow;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerFamily;
import com.parabola.domain.model.Track;
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.ListPopupWindowAdapter;
import com.parabola.newtone.adapter.TrackAdapter;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.AlbumPresenter;
import com.parabola.newtone.mvp.view.AlbumView;
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

public final class AlbumFragment extends BaseSwipeToBackFragment
        implements AlbumView, Sortable, Scrollable {

    @InjectPresenter AlbumPresenter presenter;

    @BindView(R.id.tracks_list) RecyclerView tracksList;


    @BindView(R.id.main) TextView albumTitleTxt;
    @BindView(R.id.additional_info) TextView artistNameTxt;
    @BindView(R.id.image) ShapeableImageView albumCover;

    private final TrackAdapter tracksAdapter = new TrackAdapter();

    private static final String ALBUM_ID_ARG_KEY = "albumId";

    public static AlbumFragment newInstance(int albumId) {
        Bundle args = new Bundle();
        args.putInt(ALBUM_ID_ARG_KEY, albumId);

        AlbumFragment fragment = new AlbumFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public AlbumFragment() {
        // Required empty public constructor
    }


    public int getAlbumId() {
        return requireArguments().getInt(ALBUM_ID_ARG_KEY);
    }


    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        View contentView = inflater.inflate(R.layout.list_track, container, false);
        ((ViewGroup) root.findViewById(R.id.container)).addView(contentView);
        ButterKnife.bind(this, root);

        tracksList.setAdapter(tracksAdapter);
        tracksList.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        albumCover.setVisibility(View.VISIBLE);
        albumCover.setShapeAppearanceModel(albumCover.getShapeAppearanceModel().toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, getResources().getDimension(R.dimen.album_fragment_cover_corner_size))
                .build());

        tracksAdapter.setItemClickListener(position -> presenter.onClickTrackItem(tracksAdapter.getAll(), position));
        tracksAdapter.setItemLongClickListener(this::showTrackContextMenu);

        return root;
    }

    @OnClick(R.id.action_bar)
    public void onClickActionBar() {
        smoothScrollToTop();
    }


    private void showTrackContextMenu(ViewGroup rootView, int x, int y, int itemPosition) {
        Track selectedTrack = tracksAdapter.get(itemPosition);
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
        tempView.setX(x);
        tempView.setY(y);
        rootView.addView(tempView);
        popupWindow.setAnchorView(tempView);

        popupWindow.setModal(true);
        popupWindow.setWidth(adapter.measureContentWidth());
        popupWindow.setOnItemClickListener((parent, view, position, id) -> {
            handleSelectedMenu(adapter.getItem(position), selectedTrack, itemPosition);
            popupWindow.dismiss();
        });
        popupWindow.setOnDismissListener(() -> {
            tracksAdapter.invalidateItem(itemPosition);
            rootView.removeView(tempView);
        });

        popupWindow.show();
        rootView.setBackgroundColor(getResources().getColor(R.color.colorTrackContextMenuBackground));
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
                AlertDialog dialog = createDeleteTrackDialog(requireContext(), (d, w) -> presenter.onClickMenuDeleteTrack(selectedTrack.getId()));
                dialog.show();
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
    public AlbumPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        int albumId = requireArguments().getInt(ALBUM_ID_ARG_KEY);

        return new AlbumPresenter(appComponent, albumId);
    }

    @Override
    public void setAlbumTitle(String albumTitle) {
        albumTitleTxt.setText(albumTitle);
    }

    @Override
    public void setAlbumArtist(String artistName) {
        artistNameTxt.setText(artistName);
    }

    @Override
    public void setAlbumArt(Object artCover) {
        Glide.with(requireActivity().getApplicationContext())
                .load((Bitmap) artCover)
                .placeholder(R.drawable.album_default)
                .into(albumCover);
    }

    @Override
    public void refreshTracks(List<Track> tracks) {
        tracksAdapter.replaceAll(tracks);
    }

    @Override
    public void setItemViewSettings(TrackItemView itemViewSettings) {
        tracksAdapter.setViewSettings(itemViewSettings);
    }

    @Override
    public void setCurrentTrack(int trackId) {
        tracksAdapter.setSelectedCondition(track -> track.getId() == trackId);
    }

    @Override
    public String getListType() {
        return SortingDialog.ALBUM_TRACKS_SORTING;
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
