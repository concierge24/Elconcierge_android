package com.trava.user.ui.signup.moby.resgister_screens.fragments.institution

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import com.trava.user.R
import com.trava.user.databinding.FragmentInstitutionSignupBinding
import com.trava.user.utils.ConfigPOJO
import com.trava.user.webservices.moby.response.InstituteListing
import com.trava.utilities.CheckNetworkConnection
import com.trava.utilities.DialogIndeterminate
import com.trava.utilities.Utils
import com.trava.utilities.showSnack
import kotlinx.android.synthetic.main.fragment_institution_signup.*

/**
 * A simple [Fragment] subclass.
 */
class Step1_InstitutionSignup : Fragment() , InstituteListingContractor.View{

    var binding: FragmentInstitutionSignupBinding ?= null
    var presenter = InstituteListingPresenter()
    private lateinit var dialogIndeterminate: DialogIndeterminate
    var typeList = ArrayList<InstituteListing>()
    var nameList = ArrayList<InstituteListing>()
    private var typeLIstingAdapter: ArrayAdapter<String>? = null
    private var nameListingAdapter: ArrayAdapter<String>? = null
    var typeListing = ArrayList<String>()
    var nameListing = ArrayList<String>()
    var title : String ?= null
    var titlePos : Int ?= null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_institution_signup, container, false)
        binding?.color = ConfigPOJO.Companion

        return  binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.attachView(this)
        dialogIndeterminate = DialogIndeterminate(activity)
        binding?.tvProceedFab?.background?.mutate()?.setTint(Color.parseColor(ConfigPOJO.primary_color))

        if (CheckNetworkConnection.isOnline(activity)) {
            presenter.getInstituteListing("Type","")
        } else {
            CheckNetworkConnection.showNetworkError(binding?.root!!)
        }

        binding?.tvProceedFab?.setOnClickListener {
            if(!TextUtils.isEmpty(title)){
            val fragment = Step2_InstitutionSignup()
            val bundle = Bundle()
            bundle.putString("title", title)
            bundle.putString("inst_id", nameList.get(titlePos!!).getCooperationId().toString())

                fragment.arguments = bundle
                fragmentManager?.let { it1 ->
                    Utils.replaceFragment(it1, fragment, R.id.container, Step2_InstitutionSignup::class.java.simpleName)
                }
            }else{
                binding?.root?.showSnack(getString(R.string.please_select_value))

            }
        }


        sp_type.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do Nothing
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (CheckNetworkConnection.isOnline(activity)) {
                    if(position!=0){
                        presenter.getInstituteListing("Name",parent?.adapter?.getItem(position).toString().toUpperCase())
                    }else{
                        title = ""
                        nameListing.clear()
                        nameListing.add("Select Institution name".toString().toUpperCase())
                        nameListingAdapter = activity?.let { ArrayAdapter(it, R.layout.layout_spinner_capacity, nameListing) }
                        nameListingAdapter?.setDropDownViewResource(R.layout.item_capacity)
                        sp_name.adapter = nameListingAdapter

                    }
                } else {
                    CheckNetworkConnection.showNetworkError(binding?.root!!)
                }

            }
        }

        sp_name.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do Nothing
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (CheckNetworkConnection.isOnline(activity)) {
                    if(position!=0){
                        title = parent?.adapter?.getItem(position).toString().toUpperCase();
                        titlePos = position-1
                    }
                } else {
                    CheckNetworkConnection.showNetworkError(binding?.root!!)
                }

            }
        }
    }

    override fun onApiSuccess(response: ArrayList<InstituteListing>?) {
       if(!response?.get(0)?.getType().isNullOrEmpty()){
           typeList.clear()
           for(i in 0..(response?.size)!!-1){
              typeList.add(response?.get(i))
           }
           setTypeSpinner(typeList)
       }else{
           nameList.clear()
           for(i in 0..response?.size!!-1){
               nameList.add(response?.get(i))
           }
           setNameSpinner(nameList)
       }
    }

    private fun setNameSpinner(nameList: ArrayList<InstituteListing>) {
        nameListing?.clear()
        nameListing.add("Select Institution name".toString().toUpperCase())

        for(i in 0..nameList.size-1){
            nameListing?.add(nameList.get(i).getName()!!)
        }

        nameListingAdapter = activity?.let { ArrayAdapter(it, R.layout.layout_spinner_capacity, nameListing) }
        nameListingAdapter?.setDropDownViewResource(R.layout.item_capacity)
        sp_name.adapter = nameListingAdapter

    }

    private fun setTypeSpinner(typeList: ArrayList<InstituteListing>) {
        typeListing?.clear()
        typeListing.add("Select Your Institution type".toString().toUpperCase())
        for(i in 0..typeList.size-1){
            typeListing?.add(typeList.get(i).getType()!!)
        }

        typeLIstingAdapter = activity?.let { ArrayAdapter(it, R.layout.layout_spinner_capacity, typeListing) }
        typeLIstingAdapter?.setDropDownViewResource(R.layout.item_capacity)
        sp_type.adapter = typeLIstingAdapter

    }


    override fun showLoader(isLoading: Boolean) {
        dialogIndeterminate.show(isLoading)
    }

    override fun apiFailure() {
        binding?.root?.showSnack(getString(R.string.sww_error))
    }

    override fun handleApiError(code: Int?, error: String?) {
        binding?.root?.showSnack(error.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        presenter.detachView()
    }


}
