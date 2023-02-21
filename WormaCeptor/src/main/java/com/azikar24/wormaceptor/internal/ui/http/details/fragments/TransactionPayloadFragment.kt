/*
 * Copyright AziKar24 21/2/2023.
 */

package com.azikar24.wormaceptor.internal.ui.http.details.fragments

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.*
import android.text.style.BackgroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.azikar24.wormaceptor.R
import com.azikar24.wormaceptor.databinding.FragmentTransactionPayloadBinding
import com.azikar24.wormaceptor.internal.HttpTransactionUIHelper
import com.azikar24.wormaceptor.internal.support.ColorUtil
import com.azikar24.wormaceptor.internal.support.FormatUtils
import com.azikar24.wormaceptor.internal.support.HighlightSpan
import com.azikar24.wormaceptor.internal.support.TextUtil
import com.azikar24.wormaceptor.internal.support.event.Callback
import com.azikar24.wormaceptor.internal.support.event.Debouncer
import java.util.concurrent.Executors

class TransactionPayloadFragment : Fragment() , TransactionFragment {
    lateinit var binding: FragmentTransactionPayloadBinding
    private var mSearchKey: String? = null
    lateinit var mColorUtil: ColorUtil

    private var mHeaderSearchIndices: List<Int> = ArrayList(0)
    private var mBodySearchIndices: List<Int> = ArrayList(0)
    private val searchHighLightSpan: BackgroundColorSpan by lazy {
        BackgroundColorSpan(mColorUtil.mSearchHighlightBackgroundColor)
    }
    private var mCurrentSearchIndex = 0
    private val mExecutor = Executors.newSingleThreadExecutor()
    private var mTransactionUIHelper: HttpTransactionUIHelper? = null

    private val mSearchDebouncer: Debouncer<String> = Debouncer(500, object : Callback<String> {
        override fun onEmit(searchKey: String) {
            mSearchKey = searchKey
            mHeaderSearchIndices = highlightSearchKeyword(binding.headersTextView, mSearchKey)
            mBodySearchIndices = highlightSearchKeyword(binding.bodyTextView, mSearchKey)
            updateSearchCount(1, searchKey)
        }
    })

    private var mType: Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mType = arguments?.getInt(ARG_TYPE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ) = FragmentTransactionPayloadBinding.inflate(inflater, null, false).also {
        binding = it
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mColorUtil = ColorUtil.getInstance(requireContext())
        populateUI()

        binding.searchFab.setOnClickListener {
            showSearch()
        }

        binding.searchCloseImageView.setOnClickListener {
            hideOrClearSearch()
        }

        binding.searchPrevImageView.setOnClickListener {
            mSearchKey?.let { it1 -> updateSearchCount(mCurrentSearchIndex - 1, it1) }
        }

        binding.searchNextImageView.setOnClickListener {
            mSearchKey?.let { it1 -> updateSearchCount(mCurrentSearchIndex + 1, it1) }
        }

        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mSearchDebouncer.consume(s.toString())
            }

            override fun afterTextChanged(s: Editable?) = Unit


        })
    }

    private fun showSearch() {
        binding.searchFab.hide()
        binding.searchBarLinearLayout.visibility = View.VISIBLE
        showKeyboard()
    }

    private fun showKeyboard() {
        binding.searchEditText.requestFocus()
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchEditText.windowToken, 0)
    }

    private fun hideOrClearSearch() {
        if (TextUtil.isNullOrWhiteSpace(mSearchKey)) {
            binding.searchFab.show()
            binding.searchBarLinearLayout.visibility = View.GONE
            binding.scrollViewParent.setPadding(0, 0, 0, binding.scrollViewParent.bottom)
            hideKeyboard()
        } else {
            binding.searchEditText.setText("")
        }
    }

    private fun updateSearchCount(moveToIndex: Int, searchKey: String) {
        var mMoveToIndex = moveToIndex
        val headerSearchIndices = mHeaderSearchIndices
        val bodySearchIndices = mBodySearchIndices
        val headerIndicesCount = headerSearchIndices.size
        val bodyIndicesCount = bodySearchIndices.size
        val totalCount = headerIndicesCount + bodyIndicesCount
        if (totalCount == 0) {
            mMoveToIndex = 0
        } else {
            if (mMoveToIndex > totalCount) {
                mMoveToIndex = 1
            } else if (mMoveToIndex <= 0) {
                mMoveToIndex = totalCount
            }
            // else moveToIndex will be unchanged
        }
        binding.searchCountTextView.text = "$mMoveToIndex/$totalCount"
        (binding.headersTextView.text as Spannable).removeSpan(searchHighLightSpan)
        (binding.bodyTextView.text as Spannable).removeSpan(searchHighLightSpan)
        if (mMoveToIndex > 0) {
            val scrollToY: Int
            if (mMoveToIndex <= headerIndicesCount) {
                val headerSearchIndex = headerSearchIndices[mMoveToIndex - 1]
                val lineNumber: Int = binding.headersTextView.layout.getLineForOffset(headerSearchIndex)
                scrollToY = binding.headersTextView.layout.getLineTop(lineNumber)
                (binding.headersTextView.text as Spannable).setSpan(searchHighLightSpan, headerSearchIndex, headerSearchIndex + searchKey.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else {
                val bodySearchIndex = bodySearchIndices[mMoveToIndex - headerIndicesCount - 1]
                val lineNumber: Int = binding.bodyTextView.layout.getLineForOffset(bodySearchIndex)
                scrollToY = binding.headersTextView.measuredHeight + binding.bodyTextView.layout.getLineTop(lineNumber)
                (binding.bodyTextView.text as Spannable).setSpan(searchHighLightSpan, bodySearchIndex, bodySearchIndex + searchKey.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            binding.scrollViewParent.scrollTo(0, scrollToY)
        }
        mCurrentSearchIndex = mMoveToIndex
    }

    private fun highlightSearchKeyword(textView: TextView?, searchKey: String?): List<Int> {
        if (textView != null) {
            val body = textView.text
            if (body is Spannable) {
                val spansToRemove: Array<HighlightSpan> = body.getSpans(0, body.length - 1, HighlightSpan::class.java)
                for (span in spansToRemove) {
                    body.removeSpan(span)
                }

                if (searchKey?.isNotEmpty() == true) {
                    val startIndexes: List<Int> = FormatUtils.indexOf(body.toString(), searchKey)
                    FormatUtils.applyHighlightSpan(requireContext(), body, startIndexes, searchKey.length)
                    return startIndexes
                }
            }
        }
        return ArrayList(0)
    }


    override fun transactionUpdated(transactionUIHelper: HttpTransactionUIHelper?) {
        mTransactionUIHelper = transactionUIHelper
        populateUI()
    }

    private fun populateUI() {
        if (!isAdded) return
        val transactionHelper = mTransactionUIHelper ?: return
        val color = mColorUtil.getTransactionColor(transactionHelper)
        binding.searchFab.backgroundTintList = ColorStateList(arrayOf(intArrayOf(0)), intArrayOf(color))
        binding.searchBarLinearLayout.setBackgroundColor(color)
        if (mType == TYPE_REQUEST) {
            binding.searchEditText.setHint(R.string.search_request_hint)
            populateHeaderText(transactionHelper.getRequestHeadersString(true))
            populateBody(transactionHelper.httpTransaction.requestBodyIsPlainText)
        } else if (mType == TYPE_RESPONSE) {
            binding.searchEditText.setHint(R.string.search_response_hint)
            populateHeaderText(transactionHelper.getResponseHeadersString(true))
            populateBody(transactionHelper.httpTransaction.responseBodyIsPlainText)

        }
    }

    private fun populateBody(isPlainText: Boolean) {
        if (!isPlainText) {
            binding.bodyTextView.setText(getString(R.string.body_omitted))
        } else {
            TextUtil.asyncSetText(mExecutor, object : TextUtil.AsyncTextProvider {
                override val text: CharSequence?
                    get() {
                        var body: CharSequence? = null
                        val searchKey = mSearchKey
                        if (mType == TYPE_REQUEST) {
                            body = mTransactionUIHelper?.getFormattedRequestBody()
                        } else if (mType == TYPE_RESPONSE) {
                            body = mTransactionUIHelper?.getFormattedResponseBody()
                        }
                        return if (TextUtil.isNullOrWhiteSpace(body) || TextUtil.isNullOrWhiteSpace(searchKey)) {
                            body
                        } else {
                            val startIndexes = FormatUtils.indexOf(body, searchKey)
                            val spannableBody = SpannableString(body)
                            FormatUtils.applyHighlightSpan(requireContext(), spannableBody, startIndexes, searchKey?.length)
                            mBodySearchIndices = startIndexes
                            spannableBody
                        }
                    }
                override val textView: TextView
                    get() = binding.bodyTextView
            })
        }
    }

    private fun populateHeaderText(headersString: CharSequence) {
        if (TextUtil.isNullOrWhiteSpace(headersString)) {
            binding.headersTextView.visibility = View.GONE
        } else {
            binding.headersTextView.visibility = View.VISIBLE
            binding.headersTextView.setText(headersString, TextView.BufferType.SPANNABLE)
        }
        mHeaderSearchIndices = highlightSearchKeyword(binding.headersTextView, mSearchKey)
    }


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (isAdded && !isVisibleToUser) {
            hideKeyboard()
        }
        super.setUserVisibleHint(isVisibleToUser)
    }

    companion object {
        private const val ARG_TYPE = "type"

        const val TYPE_REQUEST = 0
        const val TYPE_RESPONSE = 1

        fun newInstance(type: Int): TransactionPayloadFragment {
            val fragment = TransactionPayloadFragment()
            val b = Bundle()
            b.putInt(ARG_TYPE, type)
            fragment.arguments = b
            return fragment
        }
    }

}