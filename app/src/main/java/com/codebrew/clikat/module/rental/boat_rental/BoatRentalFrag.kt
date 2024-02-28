package com.codebrew.clikat.module.rental.boat_rental

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.codebrew.clikat.BR
import com.codebrew.clikat.R
import com.codebrew.clikat.app_utils.*
import com.codebrew.clikat.app_utils.extension.onSnackbar
import com.codebrew.clikat.app_utils.extension.setSafeOnClickListener
import com.codebrew.clikat.base.BaseFragment
import com.codebrew.clikat.data.RentalDataType
import com.codebrew.clikat.data.model.api.PortData
import com.codebrew.clikat.data.model.others.HomeRentalParam
import com.codebrew.clikat.databinding.CalendarDayLayoutBinding
import com.codebrew.clikat.databinding.Example5CalendarHeaderBinding
import com.codebrew.clikat.databinding.FragmentBoatRentalBinding
import com.codebrew.clikat.di.ViewModelProviderFactory
import com.codebrew.clikat.module.rental.HomeRentalNavigator
import com.codebrew.clikat.module.rental.HomeRentalViewModel
import com.codebrew.clikat.module.social_post.bottom_sheet.BottomSocialSheetFrag
import com.codebrew.clikat.module.social_post.custom_model.BottomDataItem
import com.codebrew.clikat.module.social_post.interfaces.BottomActionListener
import com.codebrew.clikat.utils.configurations.Configurations
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.model.ScrollMode
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.kizitonwose.calendarview.ui.ViewContainer
import com.kizitonwose.calendarview.utils.next
import com.kizitonwose.calendarview.utils.previous
import kotlinx.android.synthetic.main.fragment_boat_rental.*
import kotlinx.android.synthetic.main.fragment_boat_rental.imageView12
import kotlinx.android.synthetic.main.fragment_boat_rental.textView14
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

private const val ARG_PARAM1 = "param1"

private const val START_TYPE = "start"
private const val END_TYPE = "end"

class BoatRentalFrag : BaseFragment<FragmentBoatRentalBinding, HomeRentalViewModel>(),
    HomeRentalNavigator, BottomActionListener {

    private var param1: String? = null

    private lateinit var viewModel: HomeRentalViewModel

    private var mBinding: FragmentBoatRentalBinding? = null
    private var mPortList: MutableList<PortData>? = mutableListOf()
    private var mPortType = START_TYPE


    var mType = "pickUp"

    private var source_port_id: PortData? = null
    private var destination_port_id: PortData? = null


    @Inject
    lateinit var factory: ViewModelProviderFactory

    @Inject
    lateinit var appUtils: AppUtils

    private var selectedDate: LocalDate? = null
    private val today = LocalDate.now()
    private val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM")

    private var startDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.navigator = this

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }

        portObserver()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding = viewDataBinding
        viewDataBinding.color = Configurations.colors

        if (isNetworkConnected) {
            viewModel.getPortList()
        }

        imageView12.setOnClickListener {
            activity?.finish()
        }

        customCalendar()

        textView14.text = getString(R.string.boat_rental)

        start_port_lyt.setSafeOnClickListener {
            if (mPortList?.isEmpty() == true) return@setSafeOnClickListener

            mPortType = START_TYPE
            val mBottomData = BottomDataItem(portList = ArrayList(mPortList))
            BottomSocialSheetFrag.newInstance(mBottomData).show(childFragmentManager, "dialog")
        }

        end_port_lyt.setSafeOnClickListener {
            if (mPortList?.isEmpty() == true) return@setSafeOnClickListener

            mPortType = END_TYPE
            val mBottomData = BottomDataItem(portList = ArrayList(mPortList))
            BottomSocialSheetFrag.newInstance(mBottomData).show(childFragmentManager, "dialog")
        }
    }


    override fun getBindingVariable(): Int {
        return BR.viewModel
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_boat_rental
    }

    override fun getViewModel(): HomeRentalViewModel {
        viewModel = ViewModelProviders.of(this, factory).get(HomeRentalViewModel::class.java)
        return viewModel
    }

    private fun portObserver() {
        // Create the observer which updates the UI.
        val catObserver = Observer<MutableList<PortData>> { resource ->
            mPortList?.clear()
            mPortList?.addAll(resource)
        }
        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.portLiveData.observe(this, catObserver)
    }


    override fun onErrorOccur(message: String) {
        mBinding?.root?.onSnackbar(message)
    }

    override fun onSessionExpire() {
        openActivityOnTokenExpire()
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BoatRentalFrag().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }

    override fun onPortSelect(portData: PortData?) {

        if (mPortType == START_TYPE) {
            source_port_id = portData

            select_start_port.visibility = View.GONE
            group_start_point.visibility = View.VISIBLE

            tv_port_name?.text = getString(R.string.port_name_hint, portData?.name)
            tv_port_location?.text = getString(R.string.port_name_location, portData?.address)
        } else {
            destination_port_id = portData

            select_end_port.visibility = View.GONE
            group_end_point.visibility = View.VISIBLE

            end_port_name?.text = getString(R.string.port_name_hint, portData?.name)
            end_port_location?.text = getString(R.string.port_name_location, portData?.address)
        }


    }

    override fun onHomeRental() {
        if (isNetworkConnected && validateHomeData(startDate)) {

            val mHomeParam = HomeRentalParam(
                booking_from_date = startDate,
                cartId = "",
                totalAmt = "",
                source_port_id = source_port_id,
                destination_port_id = destination_port_id,
                mRentalType = RentalDataType.Hourly,
            )

            val bundle = bundleOf("intputData" to mHomeParam)
            navController(this@BoatRentalFrag).navigate(
                R.id.action_boatRental_to_productListFrag,
                bundle
            )
        }
    }


    private fun validateHomeData(
        startDate: String?
    ): Boolean {


        return when {
            startDate?.isEmpty() == true -> {
                mBinding?.root?.onSnackbar(getString(R.string.select_valid_date))
                false
            }
            source_port_id == null -> {
                mBinding?.root?.onSnackbar(getString(R.string.choose_start_port_point))
                false
            }
            destination_port_id == null -> {
                mBinding?.root?.onSnackbar(getString(R.string.choose_end_port_point))
                false
            }
            else -> {
                true
            }
        }
    }

    private fun customCalendar() {
        val daysOfWeek = daysOfWeekFromLocale()

        val currentMonth = YearMonth.now()
        mBinding?.calendarView?.setup(
            currentMonth,
            currentMonth.plusMonths(10),
            daysOfWeek.first()
        )
        mBinding?.calendarView?.scrollToMonth(currentMonth)
        mBinding?.calendarView?.scrollMode = ScrollMode.PAGED

        class DayViewContainer(view: View) : ViewContainer(view) {
            // Will be set when this container is bound. See the dayBinder.
            lateinit var day: CalendarDay
            val textView = CalendarDayLayoutBinding.bind(view).calendarDayText

            init {
                textView.setOnClickListener {
                    if (day.owner == DayOwner.THIS_MONTH && (day.date == today || day.date.isAfter(
                            today
                        ))
                    ) {
                        if (day.owner == DayOwner.THIS_MONTH) {
                            if (selectedDate == day.date) {
                                selectedDate = null
                                mBinding?.calendarView?.notifyDayChanged(day)
                            } else {
                                val oldDate = selectedDate
                                selectedDate = day.date
                                mBinding?.calendarView?.notifyDateChanged(day.date)
                                oldDate?.let { mBinding?.calendarView?.notifyDateChanged(oldDate) }
                            }

                            startDate = selectedDate.toString()
                            //menuItem.isVisible = selectedDate != null
                        }
                    }
                }
            }
        }

        mBinding?.calendarView?.dayBinder = object : DayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, day: CalendarDay) {
                container.day = day
                val textView = container.textView
                textView.text = day.date.dayOfMonth.toString()

                if (day.owner == DayOwner.THIS_MONTH) {
                    textView.makeVisible()

                    if (day.date.isBefore(today)) {
                        textView.setTextColorRes(R.color.edit_text_tint)
                    } else {
                        when (day.date) {
                            selectedDate -> {
                                textView.setTextColorRes(R.color.white)
                                textView.setBackgroundResource(R.drawable.black_circle)
                            }
                            today -> {
                                textView.setTextColorRes(R.color.red)
                                textView.background = null
                            }
                            else -> {
                                textView.setTextColorRes(R.color.black)
                                textView.background = null
                            }
                        }
                    }
                } else {
                    textView.makeInVisible()
                }
            }
        }

        class MonthViewContainer(view: View) : ViewContainer(view) {
            val legendLayout = Example5CalendarHeaderBinding.bind(view).legendLayout.root
        }
        mBinding?.calendarView?.monthHeaderBinder =
            object : MonthHeaderFooterBinder<MonthViewContainer> {
                override fun create(view: View) = MonthViewContainer(view)
                override fun bind(container: MonthViewContainer, month: CalendarMonth) {
                    // Setup each header day text if we have not done that already.
                    if (container.legendLayout.tag == null) {
                        container.legendLayout.tag = month.yearMonth
                        container.legendLayout.children.map { it as TextView }
                            .forEachIndexed { index, tv ->
                                tv.text = daysOfWeek[index].getDisplayName(
                                    TextStyle.SHORT,
                                    Locale.ENGLISH
                                )
                                    .toUpperCase(Locale.ENGLISH)
                                tv.setTextColorRes(R.color.grey)
                                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                            }
                        month.yearMonth
                    }
                }
            }

        mBinding?.calendarView?.monthScrollListener = { month ->
            val title = "${monthTitleFormatter.format(month.yearMonth)} ${month.yearMonth.year}"
            mBinding?.exFiveMonthYearText?.text = title

            selectedDate?.let {
                // Clear selection if we scroll to a new month.
                selectedDate = null
                mBinding?.calendarView?.notifyDateChanged(it)
                //  updateAdapterForDate(null)
            }
        }

        mBinding?.tvNextMonth?.setOnClickListener {
            mBinding?.calendarView?.findFirstVisibleMonth()?.let {
                mBinding?.calendarView?.smoothScrollToMonth(it.yearMonth.next)
            }
        }

        mBinding?.tvPreviousMonth?.setOnClickListener {
            mBinding?.calendarView?.findFirstVisibleMonth()?.let {
                mBinding?.calendarView?.smoothScrollToMonth(it.yearMonth.previous)
            }
        }
    }

}