package com.parabola.newtone.presentation.playlist.queue

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import com.parabola.domain.model.Track
import com.parabola.domain.settings.ViewSettingsInteractor.TrackItemView
import com.parabola.domain.utils.TracklistTool.isTracklistsIdentical
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import com.parabola.newtone.adapter.TrackAdapter
import com.parabola.newtone.databinding.ListTrackBinding
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

class QueueFragment : BaseSwipeToBackFragment(),
    QueueView {

    @InjectPresenter
    lateinit var presenter: QueuePresenter

    private var _binding: ListTrackBinding? = null
    private val binding get() = _binding!!

    private val queueAdapter = TrackAdapter()
    private lateinit var itemDecoration: DividerItemDecoration

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        _binding = ListTrackBinding.inflate(inflater, container, false)
        rootBinding.container.addView(binding.root)
        rootBinding.main.setText(R.string.playlist_queue)

        binding.tracksList.adapter = queueAdapter
        itemDecoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)

        queueAdapter.setMoveItemIconVisibility(true)
        queueAdapter.setOnItemClickListener { position: Int ->
            presenter.onClickTrackItem(queueAdapter.all, position)
        }
        queueAdapter.setOnMoveItemListener(presenter::onMoveItem)
        queueAdapter.setOnSwipeItemListener { position: Int ->
            queueAdapter.remove(position)
            presenter.onRemoveItem(position)
        }
        rootBinding.actionBar.setOnClickListener { presenter.onClickActionBar() }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onClickBackButton() {
        presenter.onClickBack()
    }

    override fun onEndSlidingAnimation() {
        presenter.onEnterSlideAnimationEnded()
    }

    @ProvidePresenter
    fun providePresenter(): QueuePresenter {
        val appComponent = (requireActivity().application as MainApplication).appComponent
        return QueuePresenter(appComponent)
    }

    override fun refreshTracks(tracks: List<Track>) {
        if (!isTracklistsIdentical(tracks, queueAdapter.all)) {
            queueAdapter.replaceAll(tracks)
        }
    }

    override fun setItemViewSettings(viewSettings: TrackItemView) {
        queueAdapter.setViewSettings(viewSettings)
    }

    override fun setItemDividerShowing(showed: Boolean) {
        binding.tracksList.removeItemDecoration(itemDecoration)
        if (showed) binding.tracksList.addItemDecoration(itemDecoration)
    }

    override fun setTrackCount(tracksCount: Int) {
        val tracksCountStr = resources
            .getQuantityString(R.plurals.tracks_count, tracksCount, tracksCount)

        rootBinding.additionalInfo.text = tracksCountStr
    }

    override fun setCurrentTrackPosition(currentTrackPosition: Int) {
        val oldSelectedPosition = queueAdapter.selectedPosition
        if (oldSelectedPosition.isPresent
            && oldSelectedPosition.asInt == currentTrackPosition
        ) {
            return
        }
        try {
            queueAdapter.setSelected(currentTrackPosition)
        } catch (e: IndexOutOfBoundsException) {
            queueAdapter.clearSelected()
        }
    }

    override fun goToItem(itemPosition: Int) {
        binding.tracksList.scrollToPosition(itemPosition)
        binding.tracksList.stopScroll()
    }
}
