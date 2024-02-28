package com.trava.user.ui.menu.mygifts

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.trava.user.R
import com.trava.user.databinding.ActivityMyGiftsBinding
import com.trava.user.utils.AppUtils
import com.trava.user.utils.ConfigPOJO
import com.trava.user.utils.StaticFunction
import com.trava.user.webservices.ApiResponse
import com.trava.user.webservices.models.gift.ResultItem
import com.trava.utilities.*
import kotlinx.android.synthetic.main.activity_my_gifts.*
import kotlinx.android.synthetic.main.mygift_adapter_item_layout.*

class MyGiftsActivity : AppCompatActivity(), MyGiftContract.View, OnGiftClicked {
    var type: String = "SENT"
    private var positionValue: Int = 0
    private var binding: ActivityMyGiftsBinding? = null
    private var presenter = MyGiftPresenter()
    private var dialogIndeterminate: DialogIndeterminate? = null
    private var giftListing: ArrayList<ResultItem> = ArrayList<ResultItem>()
    private var giftAdapter: MyGiftsAdapter? = null
    private var skip = 0
    private var isLoading = false
    private var isLastPage = false
    private var isFiltered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_my_gifts)
        binding!!.color = ConfigPOJO.Companion

        val statusColor = Color.parseColor(ConfigPOJO.headerColor)
        StaticFunction.setStatusBarColor(this, statusColor)

        presenter.attachView(this)
        dialogIndeterminate = DialogIndeterminate(this)

        binding?.headerTitle?.text = getString(R.string.my_gifts)
        setAdapter()
        setListener()
        binding?.sentRl?.performClick()
    }

    fun setAdapter() {
        binding?.rvGifts?.layoutManager = LinearLayoutManager(binding?.rvGifts?.context)
        giftAdapter = MyGiftsAdapter(this, giftListing, type, this)
        binding?.rvGifts?.adapter = giftAdapter

//        binding?.rvGifts?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                val layoutManager = binding?.rvGifts?.layoutManager as LinearLayoutManager
//                val visibleItemCount = layoutManager.childCount
//                val totalItemCount = layoutManager.itemCount
//                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
//
//                if (!isLoading && !isLastPage) {
////                    if (layoutManager.findLastVisibleItemPosition() == adapter?.itemCount){
////                        bookingHistoryApiCall()
////                    }
//                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount
//                            && firstVisibleItemPosition >= 0
//                            && totalItemCount >= Constants.PAGE_LIMIT) {
//                        giftApiCall(type)
//                    }
//                }
//            }
// })
    }


    private fun setListener() {
        sentRl.setOnClickListener {
            sentIndicator.visibility = View.VISIBLE
            receiveIndicator.visibility = View.GONE
            giftListing.clear()
            giftAdapter?.notifyDataSetChanged()
            type = "SENT"
            resetPagination(type)
        }

        receiveRl.setOnClickListener {
            sentIndicator.visibility = View.GONE
            receiveIndicator.visibility = View.VISIBLE
            giftListing.clear()
            giftAdapter?.notifyDataSetChanged()
            type = "RECEIVE"
            resetPagination(type)
        }

        ivBack.setOnClickListener {
            finish()
        }
    }

    private fun resetPagination(type: String) {
        skip = 0
        isLastPage = false
        giftAdapter?.setAllItemsLoaded(false)
        giftApiCall(type)
        this.type = type
    }

    private fun giftApiCall(type: String) {
        if (!CheckNetworkConnection.isOnline(this)) {
            CheckNetworkConnection.showNetworkError(binding?.root!!)
            return
        }
        when (type) {
            "SENT" -> {
                isLoading = true
                presenter.getSentGiftList(Constants.PAGE_LIMIT, skip)//type= SENT
            }

            "RECEIVE" -> {
                isLoading = true
                presenter.getReceivedGiftList(Constants.PAGE_LIMIT, skip)//type= RECEIVE
            }
        }
    }

    override fun onReceivedGiftSuccess(listGifts: ArrayList<ResultItem?>) {
        isLoading = false
        binding?.swipeRefreshLayout?.isRefreshing = false
        giftListing.clear()
        if (listGifts.size > 0) {
            giftListing = listGifts as ArrayList<ResultItem>
            setAdapter()
        }
//        else{
//            if (skip == 0 && giftListing.size == 0) {
//                binding?.viewFlipperBooking?.displayedChild = 1
//            } else {
//                if (skip == 0) {
//                    this.giftListing.clear()
//                }
//                if(isFiltered){
//                    this.giftListing.clear()
//                }
//                if (giftListing.size < Constants.PAGE_LIMIT) {
//                    isLastPage = true
//                    giftAdapter?.setAllItemsLoaded(true)
//                }
//                skip += Constants.PAGE_LIMIT
//                binding?.viewFlipperBooking?.displayedChild = 1
//                this.giftListing.addAll(giftListing)
//                giftAdapter?.notifyDataSetChanged()
//
//            }
//            isFiltered = false
//        }
    }

    override fun onSentGiftSuccess(listGifts: ArrayList<ResultItem?>) {
        isLoading = false
        binding?.swipeRefreshLayout?.isRefreshing = false
        isLoading = false
        binding?.swipeRefreshLayout?.isRefreshing = false
        giftListing.clear()
        if (listGifts.size > 0) {
            giftListing = listGifts as ArrayList<ResultItem>
            setAdapter()
        }
//        if(giftListing.size==0){
//            this.giftListing.clear()
//            this.giftListing.addAll(giftListing)
//            giftAdapter?.notifyDataSetChanged()
////            binding?.viewFlipperBooking?.displayedChild = 1
//        }else{
//            if (skip == 0 && giftListing.size == 0) {
////                binding?.viewFlipperBooking?.displayedChild = 1
//            } else {
//                if (skip == 0) {
//                    this.giftListing.clear()
//                }
//                if(isFiltered){
//                    this.giftListing.clear()
//                }
//                if (giftListing.size < Constants.PAGE_LIMIT) {
//                    isLastPage = true
//                    giftAdapter?.setAllItemsLoaded(true)
//                }
//                skip += Constants.PAGE_LIMIT
//                binding?.viewFlipperBooking?.displayedChild = 1
//                this.giftListing.addAll(giftListing)
//                giftAdapter?.notifyDataSetChanged()
//            }
//            isFiltered = false
//        }
    }

    override fun acceptRejectGiftSuccess(response: ApiResponse<Any>) {
        btnAccept.showSnack(response.msg ?: "")
        resetPagination(type)
    }

    override fun showLoader(isLoading: Boolean) {
        dialogIndeterminate?.show(isLoading)
    }

    override fun apiFailure() {
        isLoading = false
        binding?.swipeRefreshLayout?.isRefreshing = false
        binding?.root?.showSWWerror()
        isFiltered = false
    }

    override fun handleApiError(code: Int?, error: String?) {
        isLoading = false
        binding?.swipeRefreshLayout?.isRefreshing = false
        if (code == StatusCode.UNAUTHORIZED) {
            AppUtils.logout(this)
        } else {
            binding?.root?.showSnack(error.toString())
        }
        isFiltered = false
    }

    override fun onGiftSelected(position: Int, type: String) {
        positionValue = position
        when (type) {
            "1" -> {
                presenter.acceptRejectGiftSuccess(giftListing[position].order_id.toString(), type)
            }
            "2" -> {
                presenter.acceptRejectGiftSuccess(giftListing[position].order_id.toString(), type)
            }
        }
    }
}