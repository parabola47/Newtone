package com.parabola.newtone.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parabola.newtone.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public final class FolderPickAdapter extends SimpleListAdapter<FolderPickAdapter.FolderPickerItem, FolderPickAdapter.FolderViewHolder> {


    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inflateByViewType(parent.getContext(), R.layout.dialog_folder_list_item, parent);
        return new FolderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        FolderPickerItem item = get(holder.getAdapterPosition());

        holder.folderName.setText(item.getFilename());
        if (position == 0 && item.getFilename().startsWith("..")) {
            holder.folderAdd.setVisibility(View.INVISIBLE);
        } else {
            holder.folderAdd.setVisibility(View.VISIBLE);
        }

        holder.folderAdd.setOnClickListener(v -> onFolderPickListener.onFolderPick(item.getLocation()));
    }


    @Override
    public char getSection(int position) {
        return Character.toUpperCase(get(position).getFilename().charAt(0));
    }


    static class FolderViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.folderName) TextView folderName;
        @BindView(R.id.folder_add) ImageView folderAdd;

        private FolderViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
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
