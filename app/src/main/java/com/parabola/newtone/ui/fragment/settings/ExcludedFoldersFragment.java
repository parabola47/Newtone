package com.parabola.newtone.ui.fragment.settings;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Environment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import com.parabola.domain.repository.ExcludedFolderRepository;
import com.parabola.domain.repository.FolderRepository;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.ExcludedFolderAdapter;
import com.parabola.newtone.adapter.FolderPickAdapter.FolderPickerItem;
import com.parabola.newtone.databinding.FragmentExcludedFoldersBinding;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment;
import com.parabola.newtone.ui.dialog.FolderPickerDialog;
import com.parabola.newtone.ui.router.MainRouter;

import java.util.function.Function;

import javax.inject.Inject;


public final class ExcludedFoldersFragment extends BaseSwipeToBackFragment {
    private static final String LOG_CAT = ExcludedFoldersFragment.class.getSimpleName();

    private FragmentExcludedFoldersBinding binding;


    private ImageButton addFolderButton;

    private final ExcludedFolderAdapter adapter = new ExcludedFolderAdapter();


    @Inject MainRouter router;
    @Inject ExcludedFolderRepository excludedFolderRepo;
    @Inject FolderRepository folderRepo;

    public static ExcludedFoldersFragment newInstance() {
        return new ExcludedFoldersFragment();
    }

    public ExcludedFoldersFragment() {
        setRetainInstance(true);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        binding = FragmentExcludedFoldersBinding.inflate(inflater, container, false);
        getRootBinding().container.addView(binding.getRoot());

        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        appComponent.inject(this);

        initAddFolderButton();

        binding.excludedFoldersView.setAdapter(adapter);
        adapter.setOnRemoveClickListener(adapter::remove);
        getRootBinding().main.setText(R.string.setting_excluded_folders_title);
        getRootBinding().additionalInfo.setVisibility(View.GONE);
        getRootBinding().otherInfo.setVisibility(View.GONE);
        if (savedInstanceState == null) {
            adapter.addAll(excludedFolderRepo.getExcludedFolders());
        }

        return root;
    }


    private void initAddFolderButton() {
        addFolderButton = new AppCompatImageButton(requireContext());
        addFolderButton.setImageResource(R.drawable.ic_add);
        int imageSize = (int) getResources().getDimension(R.dimen.add_new_excluded_folder_button_size);
        addFolderButton.setLayoutParams(new LinearLayout.LayoutParams(imageSize, imageSize));
        getRootBinding().actionBar.addView(addFolderButton);
        addFolderButton.setOnClickListener(v -> {
            Function<FolderPickerItem, String> tracksCountInFolderMapper = folderItem -> {
                long tracksCountInFolder = folderRepo.tracksCountInFolderRecursively(folderItem.getLocation());
                return getResources().getQuantityString(R.plurals.tracks_count, (int) tracksCountInFolder, tracksCountInFolder);
            };

            FolderPickerDialog dialog = FolderPickerDialog.newInstance(
                    Environment.getExternalStorageDirectory().getAbsolutePath(), tracksCountInFolderMapper);
            dialog.setItemSelectionListener(folderPath -> {
                boolean isNew = adapter.getAll().stream()
                        .noneMatch(folderPath::equals);

                if (isNew) adapter.add(folderPath);
            });
            dialog.show(requireActivity().getSupportFragmentManager(), null);
        });

        TypedValue typedValue = new TypedValue();
        requireContext().getTheme().resolveAttribute(R.attr.selectableItemBackgroundBorderless, typedValue, true);
        addFolderButton.setBackgroundResource(typedValue.resourceId);

        ColorStateList imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.colorNewtoneIconTint);
        ImageViewCompat.setImageTintList(addFolderButton, imageTintList);
    }


    @Override
    protected void onClickBackButton() {
        router.goBack();
    }

    @Override
    public void onDestroy() {
        if (isRemoving())
            onFinishing();

        super.onDestroy();
    }

    private void onFinishing() {
        //сохраняем новые исключённые папки
        excludedFolderRepo.refreshExcludedFolders(adapter.getAll())
                .subscribe();
    }

}
