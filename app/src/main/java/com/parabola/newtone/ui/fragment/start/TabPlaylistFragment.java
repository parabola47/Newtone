package com.parabola.newtone.ui.fragment.start;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.parabola.domain.model.Playlist;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.BaseAdapter;
import com.parabola.newtone.adapter.PlaylistAdapter;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.TabPlaylistPresenter;
import com.parabola.newtone.mvp.view.TabPlaylistView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public final class TabPlaylistFragment extends MvpAppCompatFragment
        implements TabPlaylistView {

    @BindView(R.id.playlists) RecyclerView playlists;
    @BindView(R.id.sys_playlists) RecyclerView sysPlaylists;

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
        playlists.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        playlistAdapter.setItemClickListener(position -> presenter.onClickPlaylistItem(playlistAdapter.getAll().get(position).getId()));
        playlistAdapter.setItemLongClickListener(this::showTrackContextMenu);

        sysPlaylists.setAdapter(sysPlaylistAdapter);
        sysPlaylists.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        return layout;
    }


    public void showTrackContextMenu(ViewGroup rootView, float x, float y, int itemPosition) {
        final View tmpView = new View(requireContext());
        tmpView.setLayoutParams(new ViewGroup.LayoutParams(1, 1));
        tmpView.setBackgroundColor(Color.TRANSPARENT);
        tmpView.setX(x);
        tmpView.setY(y);

        rootView.addView(tmpView);


        PopupMenu popupMenu = new PopupMenu(requireContext(), tmpView, Gravity.CENTER);
        popupMenu.inflate(R.menu.playlist_menu);
        popupMenu.setOnDismissListener(menu -> rootView.removeView(tmpView));


        popupMenu.setOnMenuItemClickListener(item -> {
            int playlistId = playlistAdapter.get(itemPosition).getId();

            switch (item.getItemId()) {
                case R.id.rename_playlist:
                    presenter.onClickMenuRenamePlaylist(playlistId);
                    return true;
                case R.id.delete_playlist:
                    presenter.onClickMenuDeletePlaylist(playlistId);
                    return true;
                default: return false;
            }
        });

        popupMenu.show();
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

    public class SystemPlaylistAdapter extends RecyclerView.Adapter<SystemPlaylistAdapter.SystemPlaylistVh> {
        @NonNull
        @Override
        public SystemPlaylistAdapter.SystemPlaylistVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_system_playlist, parent, false);
            return new SystemPlaylistVh(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SystemPlaylistAdapter.SystemPlaylistVh holder, int position) {
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

        public class SystemPlaylistVh extends RecyclerView.ViewHolder {
            @BindView(R.id.icon) ImageView icon;
            @BindView(R.id.title) TextView title;

            public SystemPlaylistVh(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
