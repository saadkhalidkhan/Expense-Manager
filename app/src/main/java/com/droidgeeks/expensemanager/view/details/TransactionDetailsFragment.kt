package com.droidgeeks.expensemanager.view.details

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.droidgeeks.expensemanager.R
import com.droidgeeks.expensemanager.data.local.model.Transaction
import com.droidgeeks.expensemanager.databinding.FragmentTransactionDetailsBinding
import com.droidgeeks.expensemanager.utils.cleanTextContent
import com.droidgeeks.expensemanager.utils.hide
import com.droidgeeks.expensemanager.utils.saveBitmap
import com.droidgeeks.expensemanager.utils.show
import com.droidgeeks.expensemanager.utils.snack
import com.droidgeeks.expensemanager.utils.usdCurrencyConvertor
import com.droidgeeks.expensemanager.utils.viewState.DetailState
import com.droidgeeks.expensemanager.view.base.BaseFragment
import com.droidgeeks.expensemanager.view.details.listener.ITransactionDetail
import com.droidgeeks.expensemanager.view.main.viewmodel.TransactionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransactionDetailsFragment : BaseFragment<FragmentTransactionDetailsBinding, TransactionViewModel>(),
    ITransactionDetail {

    private val args: TransactionDetailsFragmentArgs by navArgs()
    override val viewModel: TransactionViewModel by activityViewModels()

    private var transaction: Transaction ?= null

    // handle permission dialog
    private val requestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) shareImage() else showErrorDialog()
        }

    private fun showErrorDialog() =
        findNavController().navigate(
            TransactionDetailsFragmentDirections.actionTransactionDetailsFragmentToErrorDialog(
                getString(R.string.image_share_failed),
                getString(R.string.you_have_to_enable_storage_permission_to_share_transaction_as_image)
            )
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.clickListener = this
        val transaction = args.transaction
        getTransaction(transaction.id)
        observeTransaction()
    }

    private fun getTransaction(id: Int) {
        viewModel.getByID(id)
    }

    private fun observeTransaction() = lifecycleScope.launchWhenCreated {

        viewModel.detailState.collect { detailState ->

            when (detailState) {
                DetailState.Loading -> {
                }

                is DetailState.Success -> {
                    transaction = detailState.transaction
                    onDetailsLoaded(detailState.transaction)
                }

                is DetailState.Error -> {
                    binding.root.snack(
                        string = R.string.text_error
                    )
                }

                DetailState.Empty -> {
                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun onDetailsLoaded(transaction: Transaction) = with(binding.transactionDetails) {
        title.setText(transaction.title)
        amount.setText(usdCurrencyConvertor(transaction.amount).cleanTextContent)
        type.setText(transaction.transactionType)
        tag.text = transaction.tag
        note.setText(transaction.note)
        createdAt.setText(transaction.createdAtDateFormat)
        tagIcon.setImageResource(transaction.tagIcon)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_share, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete -> {
                viewModel.deleteByID(args.transaction.id)
                    .run {
                        findNavController().navigateUp()
                    }
            }

            R.id.action_share_text -> shareText()
            R.id.action_share_image -> shareImage()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun shareImage() {
        if (!isStoragePermissionGranted()) {
            requestLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }

        // unHide the app logo and name
        showAppNameAndLogo()
        val imageURI = binding.transactionDetails.detailView.drawToBitmap().let { bitmap ->
            hideAppNameAndLogo()
            saveBitmap(requireActivity(), bitmap)
        } ?: run {
            binding.root.snack(
                string = R.string.text_error_occurred
            )
            return
        }

        val intent = ShareCompat.IntentBuilder(requireActivity())
            .setType("image/jpeg")
            .setStream(imageURI)
            .intent

        startActivity(Intent.createChooser(intent, null))
    }

    private fun showAppNameAndLogo() = with(binding.transactionDetails) {
        appIconForShare.show()
        appNameForShare.show()
    }

    private fun hideAppNameAndLogo() = with(binding.transactionDetails) {
        appIconForShare.hide()
        appNameForShare.hide()
    }

    private fun isStoragePermissionGranted(): Boolean = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("StringFormatMatches")
    private fun shareText() = with(binding) {
        val shareMsg = getString(
            R.string.share_message,
            transactionDetails.title.text.toString(),
            transactionDetails.amount.text.toString(),
            transactionDetails.type.text.toString(),
            transactionDetails.tag.text.toString(),
            transactionDetails.note.text.toString(),
            transactionDetails.createdAt.text.toString()
        )

        val intent = ShareCompat.IntentBuilder(requireActivity())
            .setType("text/plain")
            .setText(shareMsg)
            .intent

        startActivity(Intent.createChooser(intent, null))
    }

    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentTransactionDetailsBinding.inflate(inflater, container, false)

    override fun onClickEdit() {
        val bundle = Bundle().apply {
            putSerializable("transaction", transaction)
        }
        findNavController().navigate(
            R.id.action_transactionDetailsFragment_to_editTransactionFragment,
            bundle
        )
    }

    override fun onClickDelete() {
        binding.root.snack(
            string = R.string.success_expense_deleted
        )
        viewModel.deleteByID(args.transaction.id)
            .run {
                findNavController().navigateUp()
            }
    }
}
