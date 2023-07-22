package com.parabola.newtone.presentation.audioeffects

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.parabola.newtone.R
import com.parabola.newtone.databinding.DialogAudioEffectsBinding
import com.parabola.newtone.presentation.audioeffects.settings.FxAudioSettingsFragment
import com.parabola.newtone.presentation.audioeffects.equalizer.FxEqualizerFragment
import moxy.MvpAppCompatDialogFragment


private const val TABS_COUNT = 2


class AudioEffectsDialog : MvpAppCompatDialogFragment() {

    private lateinit var audioEffectsPagerAdapter: AudioEffectsPagerAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DialogAudioEffectsBinding.inflate(inflater, container, false)

        audioEffectsPagerAdapter = AudioEffectsPagerAdapter(childFragmentManager)
        binding.apply {
            audioEffectsPager.adapter = audioEffectsPagerAdapter
            audioEffectsPager.offscreenPageLimit = audioEffectsPagerAdapter.count
            tabs.setupWithViewPager(binding.audioEffectsPager)
            tabs.getTabAt(0)!!.setIcon(R.drawable.fx_eq_icon)
            tabs.getTabAt(1)!!.setIcon(R.drawable.fx_ic_tune)
        }

        //берём старые фрагменты, если экран не создаётся с нуля
        if (savedInstanceState != null) {
            val tabFragments = arrayOf(
                childFragmentManager.fragments[0],
                childFragmentManager.fragments[1],
            )
            audioEffectsPagerAdapter.initTabsFragments(tabFragments)
        }

        return binding.root
    }


    private class AudioEffectsPagerAdapter(fm: FragmentManager) :
        FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val fragments = arrayOfNulls<Fragment>(TABS_COUNT)


        fun initTabsFragments(tabFragments: Array<Fragment>) {
            require(tabFragments.size == TABS_COUNT) { "Size of array tabFragments is " + tabFragments.size + ". It must be " + TABS_COUNT }
            System.arraycopy(tabFragments, 0, fragments, 0, fragments.size)
        }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> {
                    if (fragments[0] == null) fragments[0] = FxEqualizerFragment()
                    fragments[0]!!
                }
                1 -> {
                    if (fragments[1] == null) fragments[1] = FxAudioSettingsFragment()
                    fragments[1]!!
                }
                else -> throw IllegalArgumentException("Fragment on position $position not exists")
            }
        }

        override fun getCount(): Int {
            return fragments.size
        }
    }
}