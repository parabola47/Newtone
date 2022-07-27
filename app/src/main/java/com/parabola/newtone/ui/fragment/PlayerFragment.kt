package com.parabola.newtone.ui.fragment

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.widget.ListPopupWindow
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.shape.CornerFamily
import com.parabola.domain.interactor.player.PlayerInteractor.RepeatMode
import com.parabola.domain.model.Track
import com.parabola.domain.utils.TracklistTool.isTracklistsIdentical
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import com.parabola.newtone.adapter.ListPopupWindowAdapter
import com.parabola.newtone.databinding.FragmentPlayerBinding
import com.parabola.newtone.mvp.presenter.PlayerPresenter
import com.parabola.newtone.mvp.view.PlayerView
import com.parabola.newtone.util.AndroidTool
import com.parabola.newtone.util.TimeFormatterTool.formatMillisecondsToMinutes
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.internal.observers.ConsumerSingleObserver
import io.reactivex.schedulers.Schedulers
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class PlayerFragment : MvpAppCompatFragment(), PlayerView {

    @InjectPresenter
    lateinit var presenter: PlayerPresenter

    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!

    private val albumCoverAdapter = AlbumCoverPagerAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)

        binding.durationProgress.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val currentTimeFormatted = formatMillisecondsToMinutes(progress.toLong())
                binding.currentTime.text = currentTimeFormatted
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                presenter.onStartSeekbarPressed()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                presenter.onStopSeekbarPressed(seekBar.progress)
            }
        })
        binding.albumCoverContainer.adapter = albumCoverAdapter
        binding.albumCoverContainer.addOnPageChangeListener(object : SimpleOnPageChangeListener() {

            private var lastPosition = 0

            override fun onPageSelected(position: Int) {
                lastPosition = position
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    presenter.onSwipeImage(lastPosition)
                }
            }
        })

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.queue.setOnClickListener { presenter.onClickQueue() }
        binding.audioEffects.setOnClickListener { presenter.onClickAudioEffects() }
        binding.audioEffects.setOnLongClickListener {
            presenter.onLongClickAudioEffects()
            true
        }
        binding.dropDown.setOnClickListener { presenter.onClickDropDown() }
        binding.favourite.setOnClickListener { presenter.onClickFavourite() }
        binding.favourite.setOnLongClickListener {
            presenter.onLongClickFavorite()
            true
        }
        binding.timer.setOnClickListener { presenter.onClickTimerButton() }
        binding.timer.setOnLongClickListener {
            presenter.onLongClickTimerButton()
            true
        }
        binding.trackSettings.setOnClickListener { onClickTrackSettings() }


        binding.artist.setOnClickListener { presenter.onClickArtist() }
        binding.artist.setOnLongClickListener {
            presenter.onClickArtist()
            true
        }
        binding.album.setOnClickListener { presenter.onClickAlbum() }
        binding.album.setOnLongClickListener {
            presenter.onClickAlbum()
            true
        }
        binding.title.setOnClickListener { presenter.onClickTrackTitle() }
        binding.title.setOnLongClickListener {
            presenter.onClickTrackTitle()
            true
        }


        binding.playerToggle.setOnClickListener { presenter.onClickPlayButton() }
        binding.prevTrack.setOnClickListener { presenter.onClickPrevTrack() }
        binding.nextTrack.setOnClickListener { presenter.onClickNextTrack() }
        binding.loop.setOnClickListener { presenter.onClickLoop() }
        binding.shuffle.setOnClickListener { presenter.onClickShuffle() }
    }

    private fun onClickTrackSettings() {
        val popupWindow = ListPopupWindow(requireContext()).apply {
            val adapter = ListPopupWindowAdapter(requireContext(), R.menu.player_menu)
            setAdapter(adapter)
            anchorView = requireView().findViewById(R.id.menu_tmp)
            isModal = true
            width = adapter.measureContentWidth()
            setOnItemClickListener { _, _, position, _ ->
                handleSelectedMenu(adapter.getItem(position))
                dismiss()
            }
        }
        popupWindow.show()
    }

    private fun handleSelectedMenu(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.add_to_playlist -> presenter.onClickMenuAddTrackToPlaylist()
            R.id.timer -> presenter.onClickMenuTimer()
            R.id.lyrics -> presenter.onClickMenuLyrics()
            R.id.share -> presenter.onClickMenuShareTrack()
            R.id.additional_info -> presenter.onClickMenuAdditionalInfo()
            R.id.delete -> presenter.onClickMenuDelete()
            R.id.settings -> presenter.onClickMenuSettings()
        }
    }

    @ProvidePresenter
    fun providePresenter(): PlayerPresenter {
        val appComponent = (requireActivity().application as MainApplication).appComponent
        return PlayerPresenter(appComponent)
    }

    override fun setArtist(artistName: String) {
        binding.artist.text = artistName
    }

    override fun setAlbum(albumTitle: String) {
        binding.album.text = albumTitle
    }

    override fun setTitle(trackTitle: String) {
        binding.title.text = trackTitle
    }

    override fun setDurationText(durationFormatted: String) {
        binding.durationTxt.text = durationFormatted
    }

    override fun setDurationMs(durationMs: Int) {
        binding.durationProgress.max = durationMs
    }

    override fun setIsFavourite(isFavourite: Boolean) {
        if (isFavourite) binding.favourite.setImageResource(R.drawable.ic_favourite_select)
        else binding.favourite.setImageResource(R.drawable.ic_favourite)
    }

    override fun setPlaybackButtonAsPause() {
        binding.playerToggle.setImageResource(R.drawable.ic_pause)
    }

    override fun setPlaybackButtonAsPlay() {
        binding.playerToggle.setImageResource(R.drawable.ic_play)
    }

    override fun setRepeatMode(repeatMode: RepeatMode) {
        when (repeatMode) {
            RepeatMode.OFF -> {
                binding.loop.setImageResource(R.drawable.ic_loop)
                binding.loop.setColorFilter(
                    ContextCompat.getColor(requireContext(), R.color.colorPlayerActionIconDefault)
                )
            }
            RepeatMode.ALL -> {
                binding.loop.setImageResource(R.drawable.ic_loop)
                binding.loop.setColorFilter(
                    AndroidTool.getStyledColor(requireContext(), R.attr.colorPrimary)
                )
            }
            RepeatMode.ONE -> {
                binding.loop.setImageResource(R.drawable.ic_loop_one)
                binding.loop.setColorFilter(
                    AndroidTool.getStyledColor(requireContext(), R.attr.colorPrimary)
                )
            }
        }
    }

    override fun setShuffleEnabling(enable: Boolean) {
        val color =
            if (enable) AndroidTool.getStyledColor(requireContext(), R.attr.colorPrimary)
            else ContextCompat.getColor(requireContext(), R.color.colorPlayerActionIconDefault)
        binding.shuffle.setColorFilter(color)
    }

    override fun setCurrentTimeMs(currentTimeMs: Int) {
        binding.durationProgress.progress = currentTimeMs

        val currentTimeFormatted = formatMillisecondsToMinutes(currentTimeMs.toLong())
        binding.currentTime.text = currentTimeFormatted
    }

    override fun setTimerButtonVisibility(visible: Boolean) {
        binding.timer.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun setViewPagerSlide(lock: Boolean) {
        binding.albumCoverContainer.swipeLocked = lock
    }

    override fun refreshTracks(tracks: List<Track>) {
        if (isTracklistsIdentical(tracks, albumCoverAdapter.tracks))
            return

        albumCoverAdapter.tracks.clear()
        albumCoverAdapter.tracks.addAll(tracks)
        // обёрнут в try, потому что бывают сценарии, когда при обновлении адаптер на экране плеера
        // не привязан к ViewPager и выдаёт NPE
        try {
            albumCoverAdapter.notifyDataSetChanged()
        } catch (ignored: NullPointerException) {
        }
    }

    override fun setAlbumImagePosition(currentTrackPosition: Int, smoothScroll: Boolean) {
        binding.albumCoverContainer.setCurrentItem(currentTrackPosition, smoothScroll)
    }

    override fun setTrackSettingsRotation(rotation: Float) {
        binding.trackSettings.rotation = rotation
    }

    override fun setRootViewOpacity(alpha: Float) {
        view?.alpha = alpha
    }

    override fun setRootViewVisibility(visible: Boolean) {
        view?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private class AlbumCoverPagerAdapter : PagerAdapter() {
        val tracks = mutableListOf<Track>()

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val albumCover = ShapeableImageView(container.context)
            val cornerSizePx =
                container.context.resources.getDimension(R.dimen.player_fragment_album_cover_corner_size)
            albumCover.shapeAppearanceModel = albumCover.shapeAppearanceModel.toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, cornerSizePx)
                .build()

            Single.fromCallable { tracks[position].getArtImage<Any>() as Bitmap }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(ConsumerSingleObserver(
                    { bm: Bitmap -> albumCover.setImageBitmap(bm) }
                ) { albumCover.setImageResource(R.drawable.album_default) })

            container.addView(albumCover, 0)

            return albumCover
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            container.removeView(`object` as View)
        }

        override fun getCount(): Int = tracks.size

        override fun isViewFromObject(view: View, `object`: Any): Boolean {
            return view === `object`
        }

        override fun getItemPosition(`object`: Any): Int = POSITION_NONE
    }

}
