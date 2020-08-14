package com.parabola.newtone.ui.fragment.playlist;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.parabola.domain.model.Folder;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.BaseAdapter;
import com.parabola.newtone.adapter.FolderAdapter;
import com.parabola.newtone.adapter.ListPopupWindowAdapter;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.FoldersListPresenter;
import com.parabola.newtone.mvp.view.FoldersListView;
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment;
import com.parabola.newtone.ui.dialog.DialogDismissLifecycleObserver;
import com.parabola.newtone.ui.fragment.Scrollable;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static com.parabola.domain.utils.TracklistTool.isFolderListsIdentical;
import static java.util.Objects.requireNonNull;


public final class FoldersListFragment extends BaseSwipeToBackFragment
        implements FoldersListView, Scrollable {

    @InjectPresenter FoldersListPresenter presenter;

    public FoldersListFragment() {
        // Required empty public constructor
    }

    @BindView(R.id.folders_lv) RecyclerView foldersList;
    private DividerItemDecoration itemDecoration;

    private final BaseAdapter<Folder> foldersAdapter = new FolderAdapter();


    @BindView(R.id.main) TextView foldersTitle;
    @BindView(R.id.additional_info) TextView foldersCount;


    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        View contentView = inflater.inflate(R.layout.fragment_folders_list, container, false);
        ((ViewGroup) root.findViewById(R.id.container)).addView(contentView);
        ButterKnife.bind(this, root);


        foldersList.setAdapter((RecyclerView.Adapter) foldersAdapter);
        itemDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);

        foldersAdapter.setOnItemClickListener(position ->
                presenter.onClickFolderItem(foldersAdapter.get(position).getAbsolutePath()));
        foldersAdapter.setOnItemLongClickListener(this::showArtistContextMenu);

        foldersTitle.setText(R.string.playlist_folders);

        return root;
    }


    private void showArtistContextMenu(int position) {
        Folder selectedFolder = foldersAdapter.get(position);
        ListPopupWindowAdapter menuAdapter = new ListPopupWindowAdapter(requireContext(), R.menu.folder_menu);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(selectedFolder.getFolderName())
                .setAdapter(menuAdapter, (d, which) ->
                        handleSelectedMenu(menuAdapter.getItem(which), selectedFolder))
                .create();
        dialog.setOnShowListener(d -> foldersAdapter.setContextSelected(position));
        dialog.setOnDismissListener(d -> foldersAdapter.clearContextSelected());
        getLifecycle().addObserver(new DialogDismissLifecycleObserver(dialog));
        dialog.show();
    }

    private void handleSelectedMenu(MenuItem item, Folder selectedFolder) {
        switch (item.getItemId()) {
            case R.id.shuffle:
                presenter.onClickMenuShuffle(selectedFolder.getAbsolutePath());
                break;
            case R.id.add_to_playlist:
                presenter.onClickMenuAddToPlaylist(selectedFolder.getAbsolutePath());
                break;
            case R.id.exclude_folder:
                AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.folder_menu_exclude_folder_dialog_title)
                        .setMessage(getString(R.string.folder_menu_exclude_folder_dialog_desc, selectedFolder.getFolderName()))
                        .setPositiveButton(R.string.dialog_exclude, (dialogInterface, i) -> {
                            foldersAdapter.removeWithCondition(selectedFolder::equals);
                            presenter.onClickMenuExcludeFolder(selectedFolder.getAbsolutePath());
                        })
                        .setNegativeButton(R.string.dialog_cancel, null)
                        .show();
                getLifecycle().addObserver(new DialogDismissLifecycleObserver(dialog));
                break;
        }
    }

    @OnClick(R.id.action_bar)
    public void onClickActionBar() {
        smoothScrollToTop();
    }


    @Override
    public void refreshFolders(List<Folder> folders) {
        String foldersCountStr = getResources()
                .getQuantityString(R.plurals.folders_count, folders.size(), folders.size());
        foldersCount.setText(foldersCountStr);
        if (!isFolderListsIdentical(foldersAdapter.getAll(), folders)) {
            foldersAdapter.replaceAll(folders);
        }
    }

    @Override
    public void setItemDividerShowing(boolean showed) {
        foldersList.removeItemDecoration(itemDecoration);

        if (showed)
            foldersList.addItemDecoration(itemDecoration);
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
    FoldersListPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        return new FoldersListPresenter(appComponent);
    }


    @Override
    public void smoothScrollToTop() {
        LinearLayoutManager layoutManager = requireNonNull((LinearLayoutManager) foldersList.getLayoutManager());
        int firstItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
        int screenItemsCount = layoutManager.findLastVisibleItemPosition() - layoutManager.findFirstVisibleItemPosition();

        if (firstItemPosition > screenItemsCount * 3) {
            foldersList.scrollToPosition(screenItemsCount * 3);
        }

        foldersList.smoothScrollToPosition(0);
    }

}
