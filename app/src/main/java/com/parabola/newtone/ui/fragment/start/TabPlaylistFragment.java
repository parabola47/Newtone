package com.parabola.newtone.ui.fragment.start;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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
import com.parabola.newtone.adapter.ListPopupWindowAdapter;
import com.parabola.newtone.adapter.PlaylistAdapter;
import com.parabola.newtone.databinding.FragmentTabPlaylistBinding;
import com.parabola.newtone.databinding.ItemSystemPlaylistBinding;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.TabPlaylistPresenter;
import com.parabola.newtone.mvp.view.TabPlaylistView;
import com.parabola.newtone.ui.base.BaseDialogFragment;
import com.parabola.newtone.ui.dialog.DialogDismissLifecycleObserver;
import com.parabola.newtone.ui.fragment.Scrollable;

import java.util.List;

import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public final class TabPlaylistFragment extends MvpAppCompatFragment
        implements TabPlaylistView, Scrollable {
    private static final String LOG_TAG = TabPlaylistFragment.class.getSimpleName();

    private FragmentTabPlaylistBinding binding;

    private DividerItemDecoration itemDecoration;

    private final PlaylistAdapter playlistAdapter = new PlaylistAdapter();
    private final SystemPlaylistAdapter sysPlaylistAdapter = new SystemPlaylistAdapter();

    @InjectPresenter TabPlaylistPresenter presenter;

    public TabPlaylistFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTabPlaylistBinding.inflate(inflater, container, false);


        binding.playlists.setAdapter(playlistAdapter);
        binding.sysPlaylists.setAdapter(sysPlaylistAdapter);
        itemDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);

        playlistAdapter.setOnItemClickListener(position -> presenter.onClickPlaylistItem(playlistAdapter.get(position).getId()));
        playlistAdapter.setOnItemLongClickListener(this::showPlaylistContextMenu);

        return binding.getRoot();
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
        getLifecycle().addObserver(new DialogDismissLifecycleObserver(dialog));
        dialog.show();
    }

    private void handleSelectedMenu(MenuItem menuItem, int selectedPlaylistId) {
        switch (menuItem.getItemId()) {
            case R.id.rename:
                presenter.onClickMenuRename(selectedPlaylistId);
                break;
            case R.id.shuffle:
                presenter.onClickMenuShuffle(selectedPlaylistId);
                break;
            case R.id.delete:
                AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.delete_playlist_title)
                        .setMessage(R.string.delete_playlist_desc)
                        .setPositiveButton(R.string.dialog_delete, (d, w) -> presenter.onClickMenuDelete(selectedPlaylistId))
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
        binding.myPlaylistsTxt.setVisibility(playlists.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public void setItemDividerShowing(boolean showed) {
        binding.sysPlaylists.removeItemDecoration(itemDecoration);
        binding.playlists.removeItemDecoration(itemDecoration);

        if (showed) {
            binding.sysPlaylists.addItemDecoration(itemDecoration);
            binding.playlists.addItemDecoration(itemDecoration);
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
            ItemSystemPlaylistBinding binding = ItemSystemPlaylistBinding
                    .inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new SystemPlaylistViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull SystemPlaylistViewHolder holder, int position) {
            switch (holder.getAdapterPosition()) {
                case 0:
                    holder.binding.icon.setImageResource(R.drawable.ic_favourite_white);
                    holder.binding.title.setText(R.string.playlist_favourites);
                    holder.binding.getRoot().setOnClickListener(v -> presenter.onClickFavourites());
                    break;
                case 1:
                    holder.binding.icon.setImageResource(R.drawable.ic_recent_add);
                    holder.binding.title.setText(R.string.playlist_recently_added);
                    holder.binding.getRoot().setOnClickListener(v -> presenter.onClickRecentlyAdded());
                    break;
                case 2:
                    holder.binding.icon.setImageResource(R.drawable.ic_queue);
                    holder.binding.title.setText(R.string.playlist_queue);
                    holder.binding.getRoot().setOnClickListener(v -> presenter.onClickQueue());
                    break;
                case 3:
                    holder.binding.icon.setImageResource(R.drawable.ic_folder);
                    holder.binding.title.setText(R.string.playlist_folders);
                    holder.binding.getRoot().setOnClickListener(v -> presenter.onClickFolders());
                    break;
                default:
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }

        class SystemPlaylistViewHolder extends RecyclerView.ViewHolder {
            private final ItemSystemPlaylistBinding binding;

            SystemPlaylistViewHolder(ItemSystemPlaylistBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
