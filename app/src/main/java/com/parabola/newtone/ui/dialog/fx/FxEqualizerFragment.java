package com.parabola.newtone.ui.dialog.fx;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.parabola.domain.interactor.player.AudioEffectsInteractor.EqBand;
import com.parabola.newtone.MainApplication;
import com.parabola.newtone.databinding.ItemEqBandBinding;
import com.parabola.newtone.databinding.TabFxEqBinding;
import com.parabola.newtone.di.app.AppComponent;
import com.parabola.newtone.mvp.presenter.fx.TabEqualizerPresenter;
import com.parabola.newtone.mvp.view.fx.TabEqualizerView;
import com.parabola.newtone.util.SeekBarChangeAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public final class FxEqualizerFragment extends MvpAppCompatFragment
        implements TabEqualizerView {

    @InjectPresenter TabEqualizerPresenter presenter;

    private final BandAdapter bandsAdapter = new BandAdapter();

    private TabFxEqBinding binding;


    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = TabFxEqBinding.inflate(inflater, container, false);

        binding.eqBands.setAdapter(bandsAdapter);
        binding.eqSwitcher.setOnCheckedChangeListener((buttonView, isChecked) -> presenter.onClickEqSwitcher(isChecked));
        binding.showPresetsButton.setOnClickListener(v -> presenter.onClickShowPresets());

        return binding.getRoot();
    }


    @ProvidePresenter
    TabEqualizerPresenter providePresenter() {
        AppComponent appComponent = ((MainApplication) requireActivity().getApplication()).getAppComponent();
        return new TabEqualizerPresenter(appComponent);
    }

    @Override
    public void setEqChecked(boolean enabled) {
        binding.eqSwitcher.setChecked(enabled);
        bandsAdapter.setEnabling(enabled);
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
        private boolean enabling;

        private void setEnabling(boolean enabling) {
            this.enabling = enabling;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemEqBandBinding binding = ItemEqBandBinding
                    .inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            EqBand band = bands.get(holder.getAdapterPosition());

            holder.binding.eqSeekBar.setMax(maxEqLevel - minEqLevel);
            holder.binding.eqSeekBar.setProgress(band.currentLevel - minEqLevel);
            holder.binding.eqSeekBar.setEnabled(enabling);
            holder.binding.eqHz.setText(String.valueOf(band.frequency < 1000 ? band.frequency : (band.frequency / 1000 + "k")));
            holder.binding.eqDb.setText(String.format(Locale.getDefault(), "%d", band.currentLevel));


            holder.binding.eqSeekBar.setOnSeekBarChangeListener(new SeekBarChangeAdapter() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    int newLevel = progress + minEqLevel;
                    band.currentLevel = newLevel;
                    presenter.onChangeBandLevel(band.id, newLevel);

                    holder.binding.eqDb.setText(String.format(Locale.getDefault(), "%d", newLevel));
                }
            });
        }

        @Override
        public int getItemCount() {
            return bands.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private final ItemEqBandBinding binding;

            public ViewHolder(@NonNull ItemEqBandBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
