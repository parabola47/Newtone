package com.parabola.newtone.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.parabola.newtone.BuildConfig
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import com.parabola.newtone.databinding.FragmentSettingBinding
import com.parabola.newtone.presentation.base.BaseSwipeToBackFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import java.util.Locale

class SettingFragment : BaseSwipeToBackFragment(), SettingView {

    companion object {
        private var VERSION_INFO: String = String.format(
            Locale.getDefault(),
            "v.%s. build-%d",
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE
        )
    }

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    @InjectPresenter
    lateinit var presenter: SettingPresenter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentSettingBinding.inflate(inflater, container, false)

        rootBinding.container.addView(binding.root)

        rootBinding.main.setText(R.string.setting_title)
        rootBinding.additionalInfo.visibility = View.GONE
        rootBinding.otherInfo.visibility = View.GONE

        binding.apply {
            appInfoVersion.text = VERSION_INFO

            colorThemeBar.setOnClickListener { presenter.onClickColorThemeSettings() }
            notificationColorBar.setOnClickListener { presenter.onClickNotificationColorSetting() }
            notificationArtworkShowBar.setOnClickListener { presenter.onClickNotificationArtworkShowSetting() }
            showItemDividerBar.setOnClickListener { presenter.onClickShowItemDivider() }
            excludedFoldersBar.setOnClickListener { presenter.onClickExcludedFolders() }
            trackItemViewBar.setOnClickListener { presenter.onClickTrackItemViewSettings() }
            albumItemViewBar.setOnClickListener { presenter.onClickAlbumItemViewSettings() }
            artistItemViewBar.setOnClickListener { presenter.onClickArtistItemViewSettings() }
            privacyPolicyBar.setOnClickListener { presenter.onClickPrivacyPolicy() }
            contactDevelopersBar.setOnClickListener { presenter.onClickContactDevelopers() }
            appInfoBar.setOnClickListener { presenter.onClickAppInfoBar() }
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onClickBackButton() {
        presenter.onClickBack()
    }

    override fun setNotificationArtworkSwitchChecked(isChecked: Boolean) {
        binding.notificationArtworkShowSwitch.isChecked = isChecked
        setNotificationColorBarEnabling(isChecked)
    }

    private fun setNotificationColorBarEnabling(checked: Boolean) {
        binding.notificationColorBar.isEnabled = checked
    }

    override fun setNotificationColorSwitchChecked(isChecked: Boolean) {
        binding.notificationColorSwitch.isChecked = isChecked
    }

    override fun setShowListItemDividerSwitchChecked(isChecked: Boolean) {
        binding.showItemDividerSwitch.isChecked = isChecked
    }

    @ProvidePresenter
    fun providePresenter(): SettingPresenter {
        val appComponent = (requireActivity().application as MainApplication).appComponent
        return SettingPresenter(appComponent)
    }
}
