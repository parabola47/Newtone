package com.parabola.newtone.ui.fragment.settings

import android.os.Bundle
import android.os.Environment
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.parabola.domain.repository.ExcludedFolderRepository
import com.parabola.domain.repository.FolderRepository
import com.parabola.newtone.MainApplication
import com.parabola.newtone.R
import com.parabola.newtone.adapter.ExcludedFolderAdapter
import com.parabola.newtone.adapter.FolderPickAdapter.FolderPickerItem
import com.parabola.newtone.databinding.FragmentExcludedFoldersBinding
import com.parabola.newtone.ui.base.BaseSwipeToBackFragment
import com.parabola.newtone.ui.dialog.FolderPickerDialog
import com.parabola.newtone.ui.router.MainRouter
import java.util.function.Function
import javax.inject.Inject

class ExcludedFoldersFragment : BaseSwipeToBackFragment() {

    private var _binding: FragmentExcludedFoldersBinding? = null
    private val binding get() = _binding!!

    private val adapter = ExcludedFolderAdapter()

    @Inject
    lateinit var router: MainRouter

    @Inject
    lateinit var excludedFolderRepo: ExcludedFolderRepository

    @Inject
    lateinit var folderRepo: FolderRepository


    init {
        retainInstance = true
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentExcludedFoldersBinding.inflate(inflater, container, false)

        rootBinding.container.addView(binding.root)

        val appComponent = (requireActivity().application as MainApplication).appComponent
        appComponent.inject(this)

        initAddFolderButton()

        binding.excludedFoldersView.adapter = adapter
        adapter.setOnRemoveClickListener(adapter::remove)
        rootBinding.main.setText(R.string.setting_excluded_folders_title)
        rootBinding.additionalInfo.visibility = View.GONE
        rootBinding.otherInfo.visibility = View.GONE
        if (savedInstanceState == null) {
            adapter.addAll(excludedFolderRepo.excludedFolders)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        if (isRemoving)
            onFinishing()

        super.onDestroy()
    }


    private fun initAddFolderButton() {
        val addFolderButton = AppCompatImageButton(requireContext())
        addFolderButton.setImageResource(R.drawable.ic_add)
        val imageSize = resources.getDimension(R.dimen.add_new_excluded_folder_button_size).toInt()
        addFolderButton.layoutParams = LinearLayout.LayoutParams(imageSize, imageSize)
        rootBinding.actionBar.addView(addFolderButton)
        addFolderButton.setOnClickListener {
            val tracksCountInFolderMapper = Function { folderItem: FolderPickerItem ->
                val tracksCountInFolder =
                    folderRepo.tracksCountInFolderRecursively(folderItem.location)
                resources.getQuantityString(
                    R.plurals.tracks_count,
                    tracksCountInFolder.toInt(),
                    tracksCountInFolder
                )
            }

            val dialog = FolderPickerDialog.newInstance(
                Environment.getExternalStorageDirectory().absolutePath, tracksCountInFolderMapper
            )
            dialog.setItemSelectionListener { folderPath ->
                val isNew = adapter.all.stream()
                    .noneMatch(folderPath::equals)

                if (isNew) adapter.add(folderPath)
            }
            dialog.show(requireActivity().supportFragmentManager, null)
        }

        val typedValue = TypedValue()
        requireContext().theme.resolveAttribute(
            R.attr.selectableItemBackgroundBorderless,
            typedValue,
            true
        )
        addFolderButton.setBackgroundResource(typedValue.resourceId)

        val imageTintList =
            ContextCompat.getColorStateList(requireContext(), R.color.colorNewtoneIconTint)
        ImageViewCompat.setImageTintList(addFolderButton, imageTintList)
    }

    override fun onClickBackButton() {
        router.goBack()
    }

    private fun onFinishing() {
        //сохраняем новые исключённые папки
        excludedFolderRepo.refreshExcludedFolders(adapter.all)
            .subscribe()
    }

    companion object {
        fun newInstance() = ExcludedFoldersFragment()
    }
}
