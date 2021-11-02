package com.parabola.newtone.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.parabola.newtone.R;
import com.parabola.newtone.databinding.DialogFolderListItemBinding;

import java.util.function.Function;


public final class FolderPickAdapter extends SimpleListAdapter<FolderPickAdapter.FolderPickerItem, FolderPickAdapter.FolderViewHolder> {

    @Nullable
    private Function<FolderPickerItem, String> additionalInfoMapper;

    public void setFolderAdditionalInfoMapper(@Nullable Function<FolderPickerItem, String> additionalInfoMapper) {
        this.additionalInfoMapper = additionalInfoMapper;
    }


    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflateByViewType(parent.getContext(), R.layout.dialog_folder_list_item, parent);
        return new FolderViewHolder(DialogFolderListItemBinding.bind(view));
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        FolderPickerItem item = get(holder.getAdapterPosition());

        holder.binding.folderName.setText(item.getFilename());
        if (position == 0 && item.getFilename().startsWith("..")) {
            holder.binding.folderAdd.setVisibility(View.GONE);
            holder.binding.folderAddInfo.setVisibility(View.GONE);
        } else {
            holder.binding.folderAdd.setVisibility(View.VISIBLE);
            if (additionalInfoMapper == null) {
                holder.binding.folderAddInfo.setVisibility(View.GONE);
            } else {
                holder.binding.folderAddInfo.setVisibility(View.VISIBLE);
                holder.binding.folderAddInfo.setText(additionalInfoMapper.apply(item));
            }
        }

        holder.binding.folderAdd.setOnClickListener(v -> onFolderPickListener.onFolderPick(item.getLocation()));
    }


    @Override
    public char getSection(int position) {
        return Character.toUpperCase(get(position).getFilename().charAt(0));
    }


    static class FolderViewHolder extends RecyclerView.ViewHolder {
        private final DialogFolderListItemBinding binding;


        private FolderViewHolder(DialogFolderListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }


    private OnFolderPickListener onFolderPickListener;

    public interface OnFolderPickListener {
        void onFolderPick(String folderPath);
    }

    public void setOnFolderPickListener(OnFolderPickListener onFolderPickListener) {
        this.onFolderPickListener = onFolderPickListener;
    }

    public static class FolderPickerItem implements Comparable<FolderPickerItem> {

        private String filename, location;


        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }


        @Override
        public int compareTo(FolderPickerItem otherItem) {
            return filename.toLowerCase().compareTo(otherItem.getFilename().toLowerCase());
        }

    }
}
