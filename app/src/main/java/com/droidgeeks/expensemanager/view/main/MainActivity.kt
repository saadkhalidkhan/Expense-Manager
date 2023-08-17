package com.droidgeeks.expensemanager.view.main

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import com.droidgeeks.expensemanager.R
import com.droidgeeks.expensemanager.data.local.datastore.UIModeImpl
import com.droidgeeks.expensemanager.databinding.ActivityMainBinding
import com.droidgeeks.expensemanager.repo.TransactionRepo
import com.droidgeeks.expensemanager.services.exportcsv.ExportCsvService
import com.droidgeeks.expensemanager.view.main.listener.IToolBar
import com.droidgeeks.expensemanager.view.main.viewmodel.TransactionViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), IToolBar {
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var appBarConfiguration: AppBarConfiguration

    @Inject
    lateinit var repo: TransactionRepo

    @Inject
    lateinit var exportCsvService: ExportCsvService

    @Inject
    lateinit var themeManager: UIModeImpl
    private val viewModel: TransactionViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**
         * Just so the viewModel doesn't get removed by the compiler, as it isn't used
         * anywhere here for now
         */
        viewModel

        initViews(binding)
        observeThemeMode()
        observeNavElements(binding, navHostFragment.navController)
    }

    private fun observeThemeMode() {
        lifecycleScope.launchWhenStarted {
            viewModel.getUIMode.collect {
                val mode = when (it) {
                    true -> AppCompatDelegate.MODE_NIGHT_YES
                    false -> AppCompatDelegate.MODE_NIGHT_NO
                }
                AppCompatDelegate.setDefaultNightMode(mode)
            }
        }
        viewModel.visibleBackPress.observe(this) { visible ->
            if (visible) {
                binding.imageBackPress.visibility = View.VISIBLE
                binding.imageUIMode.visibility = View.INVISIBLE
            } else {
                binding.imageUIMode.visibility = View.VISIBLE
                binding.imageBackPress.visibility = View.INVISIBLE
            }
        }
    }

    private fun observeNavElements(
        binding: ActivityMainBinding,
        navController: NavController
    ) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {

                R.id.dashboardFragment -> {
                    binding.imageBackPress.visibility = View.INVISIBLE
                    binding.tvTitle.text = getString(R.string.text_dashboard)
                }

                R.id.transactionDetailsFragment -> {
                    binding.imageBackPress.visibility = View.VISIBLE
                    binding.tvTitle.text = getString(R.string.transaction_details)
                }

                R.id.editTransactionFragment -> {
                    binding.imageBackPress.visibility = View.VISIBLE
                    binding.tvTitle.text = getString(R.string.edit_transaction)
                }

                R.id.addTransactionFragment -> {
                    binding.imageBackPress.visibility = View.VISIBLE
                    binding.tvTitle.text = getString(R.string.text_add_transaction)
                }

                else -> {

                }
            }
        }
    }

    private fun initViews(binding: ActivityMainBinding) {

        binding.clickListener = this

        navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
            ?: return

        with(navHostFragment.navController) {
            appBarConfiguration = AppBarConfiguration(graph)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        navHostFragment.navController.navigateUp()
        return super.onSupportNavigateUp()
    }

    override fun onClickUIMode() {
        lifecycleScope.launchWhenStarted {
            val isChecked = !viewModel.getUIMode.first()
            setUIMode(isChecked)
        }
    }

    override fun onClickBackPress() {
        if (navHostFragment.navController.currentDestination?.id == R.id.addTransactionFragment
            || navHostFragment.navController.currentDestination?.id == R.id.transactionDetailsFragment
            || navHostFragment.navController.currentDestination?.id == R.id.editTransactionFragment
            || navHostFragment.navController.currentDestination?.id == R.id.errorDialog
        ) {
            viewModel.visibleBackPress.value = false
            navHostFragment.navController.navigateUp()
        } else {
            viewModel.visibleBackPress.value = false
        }
    }

    private fun setUIMode(isChecked: Boolean) {
        if (isChecked) {
            viewModel.setDarkMode(true)
            binding.imageUIMode.setImageResource(R.drawable.ic_night)
        } else {
            viewModel.setDarkMode(false)
            binding.imageUIMode.setImageResource(R.drawable.ic_day)
        }
    }

    override fun onBackPressed() {
        if (navHostFragment.navController.currentDestination?.id == R.id.dashboardFragment && viewModel.visibleBackPress.value == false) {
            super.onBackPressed()
        } else {
            onClickBackPress()
        }
    }
}
