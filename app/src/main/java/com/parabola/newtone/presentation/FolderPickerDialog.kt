package com.parabola.newtone.presentation

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.parabola.newtone.R
import com.parabola.newtone.adapter.BaseAdapter
import com.parabola.newtone.adapter.FolderPickAdapter
import com.parabola.newtone.adapter.FolderPickAdapter.FolderPickerItem
import com.parabola.newtone.databinding.DialogFolderPickerBinding
import java.io.File
import java.io.FileFilter
import java.util.function.Function


private const val START_DIRECTORY_ARG_KEY = "START_DIRECTORY"


class FolderPickerDialog : DialogFragment(),
    BaseAdapter.OnItemClickListener {

    private var _binding: DialogFolderPickerBinding? = null
    private val binding get() = _binding!!

    private val folderPickAdapter = FolderPickAdapter()

    private lateinit var additionalInfoMapper: Function<FolderPickerItem, String>

    private lateinit var startDirectory: File

    private var currentDirectory: File? = null


    companion object {
        fun newInstance(
            startDirectory: String,
            additionalInfoMapper: Function<FolderPickerItem, String>,
        ) = FolderPickerDialog().apply {
            arguments = bundleOf(START_DIRECTORY_ARG_KEY to startDirectory)
            this.additionalInfoMapper = additionalInfoMapper
        }
    }


    init {
        retainInstance = true
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        startDirectory = File(requireArguments().getString(START_DIRECTORY_ARG_KEY)!!)
        _binding = DialogFolderPickerBinding.inflate(LayoutInflater.from(requireContext()))

        folderPickAdapter.setFolderAdditionalInfoMapper(additionalInfoMapper)
        binding.foldersListView.adapter = folderPickAdapter
        val itemDecoration = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        binding.foldersListView.addItemDecoration(itemDecoration)

        folderPickAdapter.setOnFolderPickListener { folderPath ->
            itemSelectionListener?.onSelectedFolderPath(folderPath)
            dismiss()
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .setNegativeButton(R.string.dialog_cancel, null)
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (currentDirectory == null) {
            currentDirectory = File(startDirectory.absolutePath)
        }
        binding.directoryPath.text = currentDirectory!!.absolutePath

        folderPickAdapter.replaceAll(prepareFolderListEntries(currentDirectory!!))
        folderPickAdapter.setOnItemClickListener(this)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun onItemClick(position: Int) {
        val item = folderPickAdapter[position]
        currentDirectory = File(item.location)
        binding.directoryPath.text = currentDirectory!!.absolutePath
        folderPickAdapter.replaceAll(prepareFolderListEntries(currentDirectory!!))
    }

    private fun prepareFolderListEntries(interDirectory: File): List<FolderPickerItem> {
        val internalList = mutableListOf<FolderPickerItem>()

        if (interDirectory.name != startDirectory.name) {
            val parent = FolderPickerItem()
            parent.filename = ".."
            parent.location = interDirectory.parentFile!!.absolutePath
            internalList.add(parent)
        }

        val filter = FileFilter { file -> file.canRead() && file.isDirectory && !file.isHidden }

        for (folder in interDirectory.listFiles(filter)!!) {
            val item = FolderPickerItem()
            item.filename = folder.name
            item.location = folder.absolutePath
            internalList.add(item)
        }
        internalList.sort()

        return internalList
    }


    private var itemSelectionListener: ItemSelectionListener? = null

    fun setItemSelectionListener(itemSelectionListener: ItemSelectionListener) {
        this.itemSelectionListener = itemSelectionListener
    }

}


interface ItemSelectionListener {
    fun onSelectedFolderPath(folderPath: String)
}
