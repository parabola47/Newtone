package com.parabola.newtone.ui.fragment.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.parabola.domain.settings.ViewSettingsInteractor.ColorTheme
import com.parabola.domain.settings.ViewSettingsInteractor.PrimaryColor
import com.parabola.domain.settings.ViewSettingsInteractor.PrimaryColor.*
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import com.parabola.newtone.databinding.FragmentColorThemeSelectorBinding
import com.parabola.newtone.mvp.presenter.ColorThemeSelectorPresenter
import com.parabola.newtone.mvp.view.ColorThemeSelectorView
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class ColorThemeSelectorFragment : BaseSwipeToBackFragment(),
    ColorThemeSelectorView {
    @InjectPresenter
    lateinit var presenter: ColorThemeSelectorPresenter

    private var _binding: FragmentColorThemeSelectorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentColorThemeSelectorBinding.inflate(inflater, container, false)

        rootBinding.container.addView(binding.root)
        rootBinding.main.setText(R.string.setting_color_theme_title)
        rootBinding.additionalInfo.visibility = View.GONE
        rootBinding.otherInfo.visibility = View.GONE

        binding.themeToggle.addOnButtonCheckedListener { _, checkedButtonId: Int, isChecked: Boolean ->
            if (isChecked) {
                val colorTheme =
                    if (checkedButtonId == R.id.darkButton) ColorTheme.DARK
                    else ColorTheme.LIGHT
                presenter.onDarkLightSelection(colorTheme)
            }
        }
        binding.primaryColorRadioGroup.setOnCheckedChangeListener { _, checkedButtonId: Int ->
            val primaryColor = when (checkedButtonId) {
                R.id.primaryColorNewtone -> NEWTONE
                R.id.primaryColorArium -> ARIUM
                R.id.primaryColorBlues -> BLUES
                R.id.primaryColorFloyd -> FLOYD
                R.id.primaryColorPurple -> PURPLE
                R.id.primaryColorPassion -> PASSION
                R.id.primaryColorSky -> SKY
                else -> throw IllegalStateException()
            }
            presenter.onPrimaryColorSelection(primaryColor)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onClickBackButton() {
        presenter.onClickBackButton()
    }

    override fun setDarkLightTheme(colorTheme: ColorTheme) {
        when (colorTheme) {
            ColorTheme.DARK -> binding.themeToggle.check(R.id.darkButton)
            ColorTheme.LIGHT -> binding.themeToggle.check(R.id.lightButton)
            else -> throw IllegalArgumentException()
        }
    }

    override fun setPrimaryColor(primaryColor: PrimaryColor) {
        when (primaryColor) {
            NEWTONE -> binding.primaryColorRadioGroup.check(R.id.primaryColorNewtone)
            ARIUM -> binding.primaryColorRadioGroup.check(R.id.primaryColorArium)
            BLUES -> binding.primaryColorRadioGroup.check(R.id.primaryColorBlues)
            FLOYD -> binding.primaryColorRadioGroup.check(R.id.primaryColorFloyd)
            PURPLE -> binding.primaryColorRadioGroup.check(R.id.primaryColorPurple)
            PASSION -> binding.primaryColorRadioGroup.check(R.id.primaryColorPassion)
            SKY -> binding.primaryColorRadioGroup.check(R.id.primaryColorSky)
            else -> throw IllegalArgumentException()
        }
    }

    @ProvidePresenter
    fun providePresenter(): ColorThemeSelectorPresenter {
        val appComponent = (requireActivity().application as MainApplication).appComponent
        return ColorThemeSelectorPresenter(appComponent)
    }
}
