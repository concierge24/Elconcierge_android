package com.trava.user.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.SupportMapFragment
import com.trava.user.R
import com.trava.utilities.DialogIndeterminate

class MapFragment : Fragment() {

    private val presenter = HomePresenter()
    private var dialogIndeterminate: DialogIndeterminate? = null
    var mapFragment: SupportMapFragment? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        intialize()

    }

    private fun intialize() {
        mapFragment = activity?.supportFragmentManager?.findFragmentById(R.id.map) as SupportMapFragment
    }
}