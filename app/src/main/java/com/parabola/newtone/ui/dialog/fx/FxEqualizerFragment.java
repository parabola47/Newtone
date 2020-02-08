package com.parabola.newtone.ui.dialog.fx;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.parabola.domain.interactors.player.AudioEffectsInteractor.EqBand;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.R;
import com.parabola.newtone.mvp.presenter.fx.TabEqualizerPresenter;
import com.parabola.newtone.mvp.view.fx.TabEqualizerView;
import com.parabola.newtone.util.SeekBarChangeAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public final class FxEqualizerFragment extends MvpAppCompatFragment
        implements TabEqualizerView {

    @BindView(R.id.eq_bands) RecyclerView eqBandList;
    @BindView(R.id.eq_switcher) Switch eqSwitch;


    private BandAdapter bandsAdapter = new BandAdapter();

    @InjectPresenter TabEqualizerPresenter presenter;


    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.tab_fx_eq, container, false);
        ButterKnife.bind(this, layout);

        eqBandList.setAdapter(bandsAdapter);

        return layout;
    }

    @OnClick(R.id.eq_switcher)
    public void onClickEqSwitcher() {
        boolean isChecked = eqSwitch.isChecked();
        presenter.onClickEqSwitcher(isChecked);

    }


    @ProvidePresenter
    TabEqualizerPresenter provideTabEqualizerPresenter() {
        return new TabEqualizerPresenter(MainApplication.getComponent());
    }

    @Override
    public void setEqChecked(boolean enabled) {
        eqSwitch.setChecked(enabled);
    }

    private int maxEqLevel;
    private int minEqLevel;

    @Override
    public void setMaxEqLevel(int level) {
        maxEqLevel = level;
    }

    @Override
    public void setMinEqLevel(int level) {
        minEqLevel = level;
    }

    @Override
    public void refreshBands(List<EqBand> bands) {
        bandsAdapter.bands.clear();
        bandsAdapter.bands.addAll(bands);
        bandsAdapter.notifyDataSetChanged();
    }

    class BandAdapter extends RecyclerView.Adapter<BandAdapter.ViewHolder> {

        private final List<EqBand> bands = new ArrayList<>();

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_eq_band, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            EqBand band = bands.get(holder.getAdapterPosition());

            holder.eqSeekbar.setMax(maxEqLevel - minEqLevel);
            holder.eqSeekbar.setProgress(band.currentLevel - minEqLevel);
            holder.eqHz.setText(String.valueOf(band.frequency < 1000 ? band.frequency : (band.frequency / 1000 + "k")));
            holder.eqDb.setText(String.format(Locale.getDefault(), "%d", band.currentLevel));


            holder.eqSeekbar.setOnSeekBarChangeListener(new SeekBarChangeAdapter() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    int newLevel = progress + minEqLevel;
                    presenter.onChangeBandLevel(band.id, (short) newLevel);

                    holder.eqDb.setText(String.format(Locale.getDefault(), "%d", newLevel));
                }
            });
        }

        @Override
        public int getItemCount() {
            return bands.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.eq_seek_bar) SeekBar eqSeekbar;
            @BindView(R.id.eq_hz) TextView eqHz;
            @BindView(R.id.eq_db) TextView eqDb;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
