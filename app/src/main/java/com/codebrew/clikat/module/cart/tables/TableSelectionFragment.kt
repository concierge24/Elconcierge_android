package com.codebrew.clikat.module.cart.tables

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.codebrew.clikat.BR
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.base.BaseFragment
import com.codebrew.clikat.data.model.api.ListItem
import com.codebrew.clikat.databinding.ItemTimeslotViewBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.item_timeslot_view.*
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject

class TableSelectionFragment : BaseFragment<ItemTimeslotViewBinding, TablesViewModel>(), TablesListNavigator {

    val adapter = RestaurantsRecyclerAdapter()
    val list = ArrayList<ListItem?>()
    private var requestedFromCart = "1"
    private lateinit var viewModel: TablesViewModel

    private var mBinding: ItemTimeslotViewBinding? = null

    @Inject
    lateinit var factory: ViewModelProviderFactory

    override fun getBindingVariable(): Int = BR.viewModel

    override fun getLayoutId(): Int = R.layout.item_timeslot_view

    override fun getViewModel(): TablesViewModel {
        viewModel = ViewModelProviders.of(this, factory).get(TablesViewModel::class.java)
        return viewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.navigator = this
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding = viewDataBinding
        tv_title?.text = getString(R.string.select_table)
        tv_sub_title?.visibility = View.INVISIBLE
        tvSelect?.visibility = View.VISIBLE

        adapter.setListData(list)
        val layoutManager = GridLayoutManager(context, 2)
        rv_timeperiod_slot?.layoutManager = layoutManager
        rv_timeperiod_slot?.adapter = adapter



        if (arguments != null) {
            loadAvailableTableList(true)
            requestedFromCart = arguments?.getString("requestFromCart").toString()
        }

        tvSelect?.setOnClickListener {
            if (adapter.selectedPosition < 0) {
                Toasty.error(requireActivity(), getString(R.string.table_selection_error_message))
                return@setOnClickListener
            }
            val selectedModel = list[adapter.selectedPosition]
            selectedModel?.requestedFrom = requestedFromCart
            EventBus.getDefault().post(selectedModel)
            requireActivity().onBackPressed()
        }

        onRecyclerViewScrolled()
    }

    private fun loadAvailableTableList(loader:Boolean) {
        val tempRequestHolder = hashMapOf(
                "slot_id" to arguments?.get("slot_id"),
                "offset" to list.size,
                "limit" to 20,
                "supplier_id" to arguments?.get("supplierId"),
                "branch_id" to arguments?.get("branchId")
        )
        viewModel.getListOfTablesAccordingToSlot(tempRequestHolder, loader)
    }

    override fun onTableListReceived(list: List<ListItem?>?) {
        list?.let { this.list.addAll(it) }
        adapter.notifyDataSetChanged()
    }

    override fun onErrorOccur(message: String) {
        mBinding?.root?.onSnackbar(message)
    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }

    private fun onRecyclerViewScrolled() {
        rv_timeperiod_slot?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val isPagingActive = list.size % 20 == 0
                if (!recyclerView.canScrollVertically(1) && isPagingActive) {
                    loadAvailableTableList(false)
                }
            }
        })
    }
}