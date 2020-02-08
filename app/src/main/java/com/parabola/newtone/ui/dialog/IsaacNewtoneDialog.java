package com.parabola.newtone.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.parabola.newtone.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

public final class IsaacNewtoneDialog extends BaseDialogFragment {

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.dialog_isaac_newtone, container, false);
        ButterKnife.bind(this, layout);

        return layout;
    }

    @OnClick(R.id.cancel)
    public void onClickCancel() {
        dismiss();
    }
}
