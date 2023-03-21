/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.crashes.list

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.azikar24.wormaceptor.BuildConfig
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.WormaCeptor
import com.azikar24.wormaceptor.databinding.FragmentCrashListBinding
import com.azikar24.wormaceptor.internal.data.CrashTransaction
import com.azikar24.wormaceptor.internal.support.ColorUtil
import com.azikar24.wormaceptor.internal.support.formatted
import com.azikar24.wormaceptor.internal.support.getApplicationName
import com.azikar24.wormaceptor.internal.support.getSystemDetail
import java.io.File
import java.util.UUID
import kotlinx.coroutines.flow.collectLatest

class CrashListFragment : Fragment() {
    private lateinit var mColorUtil: ColorUtil
    private lateinit var binding: FragmentCrashListBinding
    private val viewModel: CrashTransactionViewModel by viewModels()

    private lateinit var mCrashTransactionAdapter: CrashTransactionAdapter

    private val mCrashListDiffUtil: CrashListDiffUtil = CrashListDiffUtil()

    private var mCurrentSubscription: PagingData<CrashTransaction>? = null

    private val menuProvider
        get() = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                if (!WormaCeptor.reportCrashesToEmails.isNullOrEmpty()) {
                    menuInflater.inflate(R.menu.crashes_list_menu, menu)
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    android.R.id.home -> activity?.finish()
                    R.id.share -> shareAllCrashes()
                    else -> {
                        return false
                    }
                }
                return true
            }
        }

    private fun shareAllCrashes() {
        val stringBuilder = StringBuilder()
        mCrashTransactionAdapter.snapshot().items.forEach {
            stringBuilder.append("${it.crashDate?.formatted()} - ${it.throwable}")
            it.crashList?.forEach { stackTraceElement ->
                stringBuilder.append("at $stackTraceElement\n")
            }
            stringBuilder.append("\n\n\n-----------------------------------------\n\n\n")
        }

        val file = File(binding.root.context.cacheDir, "crashReport-${UUID.randomUUID()}.txt")
        file.writeText(stringBuilder.toString())
        val uri = FileProvider.getUriForFile(binding.root.context, "${BuildConfig.LIBRARY_PACKAGE_NAME}.provider", file)
        val shareIntent = Intent().apply {
            data = (Uri.parse("mailto:${WormaCeptor.reportCrashesToEmails?.joinToString { it }}"))
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, binding.root.context.getString(R.string.email_crash_title))
            putExtra(Intent.EXTRA_TEXT, binding.root.context.getSystemDetail())
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Share file"))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = FragmentCrashListBinding.inflate(inflater, container, false).also {
        binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mColorUtil = ColorUtil.getInstance(requireContext())
        setupToolBar()
        setupList()
        loadResults()
    }

    private fun setupToolBar() {
        (activity as? AppCompatActivity)?.supportActionBar?.run {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_back)
            this.setBackgroundDrawable(ColorDrawable(mColorUtil.colorPrimary))
            title = getString(R.string.crashes)
            subtitle = activity?.getApplicationName()
            requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner)
        }
    }

    private fun setupList() {
        mCrashTransactionAdapter = CrashTransactionAdapter(
            requireContext(), mCrashListDiffUtil,
            object : CrashTransactionAdapter.Listener {
                override fun onTransactionClicked(crashTransaction: CrashTransaction?) {
                    if (crashTransaction != null) {
                        findNavController().navigate(CrashListFragmentDirections.actionCrashListFragment2ToCrashDetailsFragment(crashTransaction))
                    }
                }
            }
        )

        binding.crashTransactionRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.crashTransactionRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        binding.crashTransactionRecyclerView.adapter = mCrashTransactionAdapter
    }

    private fun loadResults() {
        viewModel.fetchData()
        lifecycleScope.launchWhenStarted {
            viewModel.pageEventFlow.collectLatest {
                mCurrentSubscription = it
                mCrashTransactionAdapter.submitData(it)
            }
        }
    }
}