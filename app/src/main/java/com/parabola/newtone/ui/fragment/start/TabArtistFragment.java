package com.parabola.newtone.ui.fragment.start;

import static java.util.Objects.requireNonNull;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.parabola.domain.model.Artist;
import com.parabola.domain.settings.ViewSettingsInteractor.ArtistItemView;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.ArtistAdapter;
import com.parabola.newtone.adapter.ListPopupWindowAdapter;
import com.parabola.newtone.databinding.FragmentTabArtistBinding;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.TabArtistPresenter;
import com.parabola.newtone.mvp.view.TabArtistView;
import com.parabola.newtone.ui.dialog.DialogDismissLifecycleObserver;
import com.parabola.newtone.ui.dialog.SortingDialog;
import com.parabola.newtone.ui.fragment.Scrollable;
import com.parabola.newtone.ui.fragment.Sortable;

import java.util.List;

import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public final class TabArtistFragment extends MvpAppCompatFragment
        implements TabArtistView, Sortable, Scrollable {
    private static final String LOG_TAG = TabArtistFragment.class.getSimpleName();

    @InjectPresenter TabArtistPresenter presenter;

    private FragmentTabArtistBinding binding;

    private final ArtistAdapter artistsAdapter = new ArtistAdapter();
    private DividerItemDecoration itemDecoration;

    public TabArtistFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTabArtistBinding.inflate(inflater, container, false);

        binding.artistsList.setAdapter(artistsAdapter);
        itemDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);

        artistsAdapter.setOnItemClickListener(position -> presenter.onItemClick(artistsAdapter.get(position).getId()));
        artistsAdapter.setOnItemLongClickListener(this::showArtistContextMenu);

        return binding.getRoot();
    }

    private void showArtistContextMenu(int position) {
        Artist selectedArtist = artistsAdapter.get(position);
        ListPopupWindowAdapter menuAdapter = new ListPopupWindowAdapter(requireContext(), R.menu.artist_menu);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(selectedArtist.getName())
                .setAdapter(menuAdapter, (d, which) ->
                        handleSelectedMenu(menuAdapter.getItem(which), selectedArtist))
                .create();
        dialog.setOnShowListener(d -> artistsAdapter.setContextSelected(position));
        dialog.setOnDismissListener(d -> artistsAdapter.clearContextSelected());
        getLifecycle().addObserver(new DialogDismissLifecycleObserver(dialog));
        dialog.show();
    }

    private void handleSelectedMenu(MenuItem menuItem, Artist selectedArtist) {
        switch (menuItem.getItemId()) {
            case R.id.shuffle:
                presenter.onClickMenuShuffle(selectedArtist.getId());
                break;
            case R.id.add_to_playlist:
                presenter.onClickMenuAddToPlaylist(selectedArtist.getId());
                break;
        }
    }


    @ProvidePresenter
    public TabArtistPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        return new TabArtistPresenter(appComponent);
    }

    public void scrollTo(int artistId) {
        for (int i = 0; i < artistsAdapter.size(); i++) {
            if (artistsAdapter.get(i).getId() == artistId) {
                binding.artistsList.scrollToPosition(i);
                return;
            }
        }
    }

    @Override
    public void refreshArtists(List<Artist> artists) {
        artistsAdapter.replaceAll(artists);
    }

    @Override
    public void setItemViewSettings(ArtistItemView viewSettings) {
        artistsAdapter.setViewSettings(viewSettings);
    }

    @Override
    public void setItemDividerShowing(boolean showed) {
        binding.artistsList.removeItemDecoration(itemDecoration);

        if (showed)
            binding.artistsList.addItemDecoration(itemDecoration);
    }

    @Override
    public void setSectionShowing(boolean enable) {
        artistsAdapter.setSectionEnabled(enable);
    }

    @Override
    public String getListType() {
        return SortingDialog.ALL_ARTISTS_SORTING;
    }

    @Override
    public void smoothScrollToTop() {
        LinearLayoutManager layoutManager = requireNonNull((LinearLayoutManager) binding.artistsList.getLayoutManager());
        int firstItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
        int screenItemsCount = layoutManager.findLastVisibleItemPosition() - layoutManager.findFirstVisibleItemPosition();

        if (firstItemPosition > screenItemsCount * 3) {
            binding.artistsList.scrollToPosition(screenItemsCount * 3);
        }

        binding.artistsList.smoothScrollToPosition(0);
    }

}
