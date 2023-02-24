/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.crashes.details

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.databinding.FragmentCrashDetailsBinding
import com.azikar24.wormaceptor.internal.data.CrashTransaction
import com.azikar24.wormaceptor.internal.support.formatted

class CrashDetailsFragment : Fragment() {
    lateinit var binding: FragmentCrashDetailsBinding
    private val args: CrashDetailsFragmentArgs by navArgs()
    lateinit var currentData: CrashTransaction

    private val menuProvider
        get() = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) = Unit

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if (menuItem.itemId == android.R.id.home) findNavController().navigateUp()
                return true
            }

        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = FragmentCrashDetailsBinding.inflate(inflater, container, false).also {
        binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentData = args.crashTransaction
        populateUI()
    }

    private fun populateUI() {
        setupToolbar()


        binding.crashTitleTextView.text = getString(R.string.bracket_string_newline_string, currentData.crashDate?.formatted(), currentData.throwable)
        binding.crashesRecyclerView.layoutManager = LinearLayoutManager(requireContext()).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
        binding.crashesRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))

        val adapter = currentData.crashList?.let { CrashAdapter(items = it) }
        binding.crashesRecyclerView.adapter = adapter

    }

    private fun setupToolbar() {
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.run {
            currentData.throwable?.let {
                val colonPosition = it.indexOf(":")
                val title = if (colonPosition > -1) it.substring(0, colonPosition) else it
                subtitle = title.substring(title.lastIndexOf(".") + 1)
            }

            currentData.crashList?.getOrNull(0)?.fileName.let {
                title = it
            }

            requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner)
        }
    }

}