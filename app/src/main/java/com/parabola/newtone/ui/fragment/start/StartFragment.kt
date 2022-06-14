package com.parabola.newtone.ui.fragment.start

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import com.parabola.newtone.adapter.ListPopupWindowAdapter
import com.parabola.newtone.adapter.StartFragmentPagerAdapter
import com.parabola.newtone.databinding.FragmentStartBinding
import com.parabola.newtone.mvp.presenter.StartPresenter
import com.parabola.newtone.mvp.view.StartView
import com.parabola.newtone.ui.dialog.DialogDismissLifecycleObserver
import com.parabola.newtone.ui.fragment.Scrollable
import com.parabola.newtone.util.AndroidTool
import com.parabola.newtone.util.OnTabSelectedAdapter
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class StartFragment : MvpAppCompatFragment(), StartView {

    @InjectPresenter
    lateinit var presenter: StartPresenter

    private var _binding: FragmentStartBinding? = null
    private val binding get() = _binding!!

    private lateinit var fragmentPagerAdapter: StartFragmentPagerAdapter

    private var selectedTabIconTint = 0
    private var defaultTabIconTint = 0


    override fun onAttach(context: Context) {
        super.onAttach(context)
        selectedTabIconTint = AndroidTool.getStyledColor(context, R.attr.colorPrimary)
        defaultTabIconTint = ContextCompat.getColor(context, R.color.colorTabIconTintDefault)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartBinding.inflate(inflater, container, false)

        fragmentPagerAdapter = StartFragmentPagerAdapter(requireContext(), childFragmentManager)
        binding.fragmentPager.adapter = fragmentPagerAdapter

        //берём старые фрагменты, если экран не создаётся с нуля
        if (savedInstanceState != null) {
            val tabFragments = arrayOfNulls<Fragment>(4)
            for (fragment in childFragmentManager.fragments) {
                if (fragment is TabArtistFragment) tabFragments[0] = fragment
                if (fragment is TabAlbumFragment) tabFragments[1] = fragment
                if (fragment is TabTrackFragment) tabFragments[2] = fragment
                if (fragment is TabPlaylistFragment) tabFragments[3] = fragment
            }
            fragmentPagerAdapter.initTabsFragments(tabFragments)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTabLayout()
        binding.fragmentPager.offscreenPageLimit = binding.tabLayout.tabCount

        binding.requestPermissionPanel.requestPermissionBtn
            .setOnClickListener { presenter.onClickRequestPermission() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun setPermissionPanelVisibility(visible: Boolean) {
        binding.requestPermissionPanel.root.visibility = if (visible) View.VISIBLE else View.GONE
    }

    fun goToTab(tabNumber: Int, smoothScroll: Boolean) {
        binding.fragmentPager.setCurrentItem(tabNumber, smoothScroll)
    }

    fun scrollOnTabTrackToCurrentTrack() {
        val tabTrackFragment = fragmentPagerAdapter.getItem(2) as TabTrackFragment
        tabTrackFragment.scrollToCurrentTrack()
    }

    private fun setupTabLayout() {
        binding.tabLayout.setupWithViewPager(binding.fragmentPager)
        buildTabItem(binding.tabLayout, 0, R.string.tab_artists, R.drawable.ic_artist)
        buildTabItem(binding.tabLayout, 1, R.string.tab_albums, R.drawable.ic_album)
        buildTabItem(binding.tabLayout, 2, R.string.tab_tracks, R.drawable.ic_clef)
        buildTabItem(binding.tabLayout, 3, R.string.tab_playlists, R.drawable.ic_playlist)
        binding.tabLayout.addOnTabSelectedListener(object : OnTabSelectedAdapter() {
            override fun onTabReselected(tab: TabLayout.Tab) {
                (fragmentPagerAdapter.getItem(tab.position) as Scrollable).smoothScrollToTop()
            }
        })
        binding.fragmentPager.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageScrolled(position: Int, offset: Float, positionOffsetPixels: Int) {
                for (i in 0 until binding.tabLayout.tabCount) {
                    var tabOffset = 0f
                    if (i == position) tabOffset =
                        1 - offset else if (i == position + 1 && position + 1 < binding.tabLayout.tabCount) tabOffset =
                        offset
                    refreshTabColor(binding.tabLayout.getTabAt(i)!!, tabOffset)
                }
            }

            private fun refreshTabColor(tab: TabLayout.Tab, offset: Float) {
                val color = ColorUtils.blendARGB(defaultTabIconTint, selectedTabIconTint, offset)
                val tabView = tab.customView!!
                (tabView.findViewById<View>(R.id.icon) as ImageView).setColorFilter(color)
                (tabView.findViewById<View>(R.id.title) as TextView).setTextColor(color)
            }
        })
    }

    private fun buildTabItem(
        tabLayout: TabLayout,
        tabIndex: Int,
        tabTitleResId: Int,
        tabIconResId: Int,
    ) {
        val layout = LayoutInflater.from(requireContext())
            .inflate(R.layout.tab_item_view, tabLayout, false) as LinearLayout
        val tab = tabLayout.getTabAt(tabIndex)!!

        //при долгом удержании на табе треков открывать контекстное меню
        if (tabIndex == 2) {
            tab.view.setOnLongClickListener {
                showTabTrackContextMenu()
                true
            }
        }
        layout.findViewById<TextView>(R.id.title).setText(tabTitleResId)
        layout.findViewById<ImageView>(R.id.icon).setImageResource(tabIconResId)
        tab.customView = layout
    }

    private fun showTabTrackContextMenu() {
        val menuAdapter = ListPopupWindowAdapter(requireContext(), R.menu.tab_track_menu)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.tab_track_menu_title)
            .setAdapter(menuAdapter) { _: DialogInterface?, which: Int ->
                handleSelectedMenu(
                    menuAdapter.getItem(which)
                )
            }
            .create()
        lifecycle.addObserver(DialogDismissLifecycleObserver(dialog))
        dialog.show()
    }

    private fun handleSelectedMenu(selectedMenuItem: MenuItem) {
        when (selectedMenuItem.itemId) {
            R.id.shuffle_all -> presenter.onClickMenuShuffleAll()
            R.id.excluded_folders -> presenter.onClickMenuExcludedFolders()
        }
    }

    val currentSelectedFragment: Fragment
        get() = fragmentPagerAdapter.getItem(binding.fragmentPager.currentItem)

    @ProvidePresenter
    fun providePresenter(): StartPresenter {
        val appComponent = (requireActivity().application as MainApplication).appComponent
        return StartPresenter(appComponent)
    }

    fun scrollToArtistInTab(artistId: Int) {
        (fragmentPagerAdapter.getItem(0) as TabArtistFragment)
            .scrollTo(artistId)
    }

    fun scrollToAlbumInTab(albumId: Int) {
        (fragmentPagerAdapter.getItem(1) as TabAlbumFragment)
            .scrollTo(albumId)
    }

}
