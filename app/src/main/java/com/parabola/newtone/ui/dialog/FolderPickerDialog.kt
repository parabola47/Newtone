package com.parabola.newtone.ui.dialog;

import static java.util.Objects.requireNonNull;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.parabola.newtone.R;
import com.parabola.newtone.adapter.BaseAdapter;
import com.parabola.newtone.adapter.FolderPickAdapter;
import com.parabola.newtone.adapter.FolderPickAdapter.FolderPickerItem;
import com.parabola.newtone.databinding.DialogFolderPickerBinding;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;


public class FolderPickerDialog extends DialogFragment
        implements BaseAdapter.OnItemClickListener {

    private DialogFolderPickerBinding binding;

    private final FolderPickAdapter folderPickAdapter = new FolderPickAdapter();

    @Nullable
    private Function<FolderPickerItem, String> additionalInfoMapper;


    private static final String START_DIRECTORY_ARG_KEY = "START_DIRECTORY";
    private File startDirectory;

    private File currentDirectory;


    public static FolderPickerDialog newInstance(String startDirectory,
                                                 @Nullable Function<FolderPickerItem, String> additionalInfoMapper) {
        Bundle args = new Bundle();
        args.putString(START_DIRECTORY_ARG_KEY, startDirectory);

        FolderPickerDialog fragment = new FolderPickerDialog();
        fragment.additionalInfoMapper = additionalInfoMapper;
        fragment.setArguments(args);
        return fragment;
    }


    public FolderPickerDialog() {
        setRetainInstance(true);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        startDirectory = new File(requireNonNull(requireArguments().getString(START_DIRECTORY_ARG_KEY)));
        binding = DialogFolderPickerBinding.inflate(LayoutInflater.from(requireContext()));

        folderPickAdapter.setFolderAdditionalInfoMapper(additionalInfoMapper);
        binding.foldersListView.setAdapter(folderPickAdapter);
        DividerItemDecoration itemDecoration = new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL);
        binding.foldersListView.addItemDecoration(itemDecoration);

        folderPickAdapter.setOnFolderPickListener(folderPath -> {
            if (itemSelectionListener != null) {
                itemSelectionListener.onSelectedFolderPath(folderPath);
            }
            dismiss();
        });

        return new MaterialAlertDialogBuilder(requireContext())
                .setView(binding.getRoot())
                .setNegativeButton(R.string.dialog_cancel, null)
                .create();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (currentDirectory == null) {
            currentDirectory = new File(startDirectory.getAbsolutePath());
        }
        binding.directoryPath.setText(currentDirectory.getAbsolutePath());

        folderPickAdapter.replaceAll(prepareFolderListEntries(currentDirectory));
        folderPickAdapter.setOnItemClickListener(this);

        return super.onCreateView(inflater, container, savedInstanceState);
    }


    @Override
    public void onItemClick(int position) {
        FolderPickerItem item = folderPickAdapter.get(position);
        currentDirectory = new File(item.getLocation());

        binding.directoryPath.setText(currentDirectory.getAbsolutePath());

        folderPickAdapter.replaceAll(prepareFolderListEntries(currentDirectory));
    }


    private List<FolderPickerItem> prepareFolderListEntries(File interDirectory) {
        List<FolderPickerItem> internalList = new ArrayList<>();

        if (!interDirectory.getName().equals(startDirectory.getName())) {
            FolderPickerItem parent = new FolderPickerItem();
            parent.setFilename("..");
            parent.setLocation(requireNonNull(interDirectory.getParentFile()).getAbsolutePath());
            internalList.add(parent);
        }

        FileFilter filter = file -> file.canRead() && file.isDirectory() && !file.isHidden();

        for (File folder : requireNonNull(interDirectory.listFiles(filter))) {
            FolderPickerItem item = new FolderPickerItem();
            item.setFilename(folder.getName());
            item.setLocation(folder.getAbsolutePath());
            internalList.add(item);
        }
        Collections.sort(internalList);

        return internalList;
    }


    private ItemSelectionListener itemSelectionListener;

    public void setItemSelectionListener(ItemSelectionListener itemSelectionListener) {
        this.itemSelectionListener = itemSelectionListener;
    }

    public interface ItemSelectionListener {
        void onSelectedFolderPath(String folder);
    }

}
