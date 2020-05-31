package com.parabola.newtone.ui.fragment.start;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.parabola.domain.model.Playlist;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.BaseAdapter;
import com.parabola.newtone.adapter.ListPopupWindowAdapter;
import com.parabola.newtone.adapter.PlaylistAdapter;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.TabPlaylistPresenter;
import com.parabola.newtone.mvp.view.TabPlaylistView;
import com.parabola.newtone.ui.base.BaseDialogFragment;
import com.parabola.newtone.ui.fragment.Scrollable;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public final class TabPlaylistFragment extends MvpAppCompatFragment
        implements TabPlaylistView, Scrollable {
    private static final String LOG_TAG = TabPlaylistFragment.class.getSimpleName();

    @BindView(R.id.playlists) RecyclerView playlists;
    @BindView(R.id.sys_playlists) RecyclerView sysPlaylists;
    private DividerItemDecoration itemDecoration;

    private final BaseAdapter<Playlist> playlistAdapter = new PlaylistAdapter();
    private final SystemPlaylistAdapter sysPlaylistAdapter = new SystemPlaylistAdapter();

    @InjectPresenter TabPlaylistPresenter presenter;

    public TabPlaylistFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_tab_playlist, container, false);
        ButterKnife.bind(this, layout);

        playlists.setAdapter((RecyclerView.Adapter) playlistAdapter);
        sysPlaylists.setAdapter(sysPlaylistAdapter);
        itemDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);

        playlistAdapter.setOnItemClickListener(position -> presenter.onClickPlaylistItem(playlistAdapter.get(position).getId()));
        playlistAdapter.setOnItemLongClickListener(this::showPlaylistContextMenu);

        return layout;
    }


    private void showPlaylistContextMenu(int position) {
        Playlist playlist = playlistAdapter.get(position);
        ListPopupWindowAdapter menuAdapter = new ListPopupWindowAdapter(requireContext(), R.menu.playlist_menu);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(playlist.getTitle())
                .setAdapter(menuAdapter, (d, which) ->
                        handleSelectedMenu(menuAdapter.getItem(which), playlist.getId()))
                .create();
        dialog.setOnShowListener(d -> playlistAdapter.setContextSelected(position));
        dialog.setOnDismissListener(d -> playlistAdapter.clearContextSelected());
        dialog.show();
    }

    private void handleSelectedMenu(MenuItem menuItem, int selectedPlaylistId) {
        switch (menuItem.getItemId()) {
            case R.id.rename_playlist:
                presenter.onClickMenuRenamePlaylist(selectedPlaylistId);
                break;
            case R.id.delete_playlist:
                AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.delete_playlist_title)
                        .setMessage(R.string.delete_playlist_desc)
                        .setPositiveButton(R.string.dialog_delete, (d, w) -> presenter.onClickMenuDeletePlaylist(selectedPlaylistId))
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .create();

                DialogFragment dialogFragment = BaseDialogFragment.build(dialog);
                dialogFragment.show(requireActivity().getSupportFragmentManager(), null);
                break;
        }
    }

    @ProvidePresenter
    public TabPlaylistPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        return new TabPlaylistPresenter(appComponent);
    }


    @Override
    public void refreshPlaylists(List<Playlist> playlists) {
        playlistAdapter.replaceAll(playlists);
    }

    @Override
    public void setItemDividerShowing(boolean showed) {
        sysPlaylists.removeItemDecoration(itemDecoration);
        playlists.removeItemDecoration(itemDecoration);

        if (showed) {
            sysPlaylists.addItemDecoration(itemDecoration);
            playlists.addItemDecoration(itemDecoration);
        }
    }


    @Override
    public void smoothScrollToTop() {
        ((NestedScrollView) requireView()).smoothScrollTo(0, 0);
    }

    public class SystemPlaylistAdapter extends RecyclerView.Adapter<SystemPlaylistAdapter.SystemPlaylistViewHolder> {
        @NonNull
        @Override
        public SystemPlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_system_playlist, parent, false);
            return new SystemPlaylistViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SystemPlaylistViewHolder holder, int position) {
            switch (holder.getAdapterPosition()) {
                case 0:
                    holder.icon.setImageResource(R.drawable.ic_favourite_white);
                    holder.title.setText(R.string.playlist_favourites);
                    holder.itemView.setOnClickListener(v -> presenter.onClickFavourites());
                    break;
                case 1:
                    holder.icon.setImageResource(R.drawable.ic_recent_add);
                    holder.title.setText(R.string.playlist_recently_added);
                    holder.itemView.setOnClickListener(v -> presenter.onClickRecentlyAdded());
                    break;
                case 2:
                    holder.icon.setImageResource(R.drawable.ic_queue);
                    holder.title.setText(R.string.playlist_queue);
                    holder.itemView.setOnClickListener(v -> presenter.onClickQueue());
                    break;
                case 3:
                    holder.icon.setImageResource(R.drawable.ic_folder);
                    holder.title.setText(R.string.playlist_folders);
                    holder.itemView.setOnClickListener(v -> presenter.onClickFolders());
                    break;
                default:
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }

        class SystemPlaylistViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.icon) ImageView icon;
            @BindView(R.id.title) TextView title;

            SystemPlaylistViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
