package com.parabola.newtone.ui.dialog.fx

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import com.parabola.domain.interactor.player.AudioEffectsInteractor
import com.parabola.newtone.MainApplication
import com.parabola.newtone.databinding.ItemEqBandBinding
import com.parabola.newtone.databinding.TabFxEqBinding
import com.parabola.newtone.mvp.presenter.fx.TabEqualizerPresenter
import com.parabola.newtone.mvp.view.fx.TabEqualizerView
import com.parabola.newtone.util.SeekBarChangeAdapter
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class FxEqualizerFragment : MvpAppCompatFragment(),
    TabEqualizerView {

    @InjectPresenter
    lateinit var presenter: TabEqualizerPresenter

    private val bandsAdapter = BandAdapter()

    private var _binding: TabFxEqBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TabFxEqBinding.inflate(inflater, container, false)

        binding.apply {
            eqBands.adapter = bandsAdapter
            eqSwitcher.setOnCheckedChangeListener { _, isChecked ->
                presenter.onClickEqSwitcher(isChecked)
            }
            showPresetsButton.setOnClickListener { presenter.onClickShowPresets() }
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    @ProvidePresenter
    fun providePresenter(): TabEqualizerPresenter {
        val appComponent = (requireActivity().application as MainApplication).appComponent
        return TabEqualizerPresenter(appComponent)
    }


    override fun setEqChecked(checked: Boolean) {
        binding.eqSwitcher.isChecked = checked
        bandsAdapter.setEnabling(checked)
    }

    private var maxEqLevel = 0
    private var minEqLevel = 0

    override fun setMaxEqLevel(level: Int) {
        maxEqLevel = level
    }

    override fun setMinEqLevel(level: Int) {
        minEqLevel = level
    }

    override fun refreshBands(bands: List<AudioEffectsInteractor.EqBand>) {
        bandsAdapter.bands.clear()
        bandsAdapter.bands.addAll(bands)
        bandsAdapter.notifyDataSetChanged()
    }

    inner class BandAdapter : RecyclerView.Adapter<BandAdapter.ViewHolder>() {

        val bands = mutableListOf<AudioEffectsInteractor.EqBand>()
        private var enabling = false

        fun setEnabling(enabling: Boolean) {
            this.enabling = enabling
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemEqBandBinding
                .inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.binding.apply {
                val band = bands[holder.adapterPosition]

                eqSeekBar.max = maxEqLevel - minEqLevel
                eqSeekBar.progress = band.currentLevel - minEqLevel
                eqSeekBar.isEnabled = enabling
                val eqHzText =
                    if (band.frequency < 1000) band.frequency.toString()
                    else "${band.frequency / 1000}k"
                eqHz.text = eqHzText
                eqDb.text = band.currentLevel.toString()

                eqSeekBar.setOnSeekBarChangeListener(object : SeekBarChangeAdapter() {
                    override fun onProgressChanged(
                        seekBar: SeekBar,
                        progress: Int,
                        fromUser: Boolean,
                    ) {
                        val newLevel = progress + minEqLevel
                        band.currentLevel = newLevel
                        presenter.onChangeBandLevel(band.id, newLevel)

                        holder.binding.eqDb.text = newLevel.toString()
                    }
                })
            }
        }

        override fun getItemCount(): Int {
            return bands.size
        }

        inner class ViewHolder(val binding: ItemEqBandBinding) :
            RecyclerView.ViewHolder(binding.root)
    }
}
