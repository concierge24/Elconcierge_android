package com.trava.user.ui.home.dropofflocation

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.trava.user.R
import com.trava.user.ui.home.HomeContract
import com.trava.user.ui.home.HomePresenter
import com.trava.user.ui.home.dropofflocation.PlacesAutoCompleteAdapter.ClickListener
import com.trava.user.ui.home.services.ServicePresenter
import com.trava.user.utils.ConfigPOJO
import com.trava.user.webservices.ApiResponseNew
import com.trava.user.webservices.models.FindLocationPOJO
import com.trava.user.webservices.models.GeoFenceData
import com.trava.user.webservices.models.LocationPOJO
import com.trava.user.webservices.models.homeapi.ServiceDetails
import com.trava.utilities.Constants
import com.trava.utilities.DialogIndeterminate
import com.trava.utilities.LocaleManager
import com.trava.utilities.webservices.models.LoginModel
import com.trava.utilities.webservices.models.Service
import kotlinx.android.synthetic.main.activity_find_location.*
import org.json.JSONObject
import java.util.*

class FindLocationActivity : AppCompatActivity(), ClickListener, LocationInterface, LocationContract.View, ItemClickList {
    private var mAutoCompleteAdapter: PlacesAutoCompleteAdapter? = null
    private var nearLocation: NearLocationsAdapter? = null
    private var nearDataLocation: FindLocationsAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var list = arrayListOf<LocationPOJO>()
    private var listLocation = arrayListOf<FindLocationPOJO>()
    private val presenter = LocationPresenter()
    var dialogIndeterminate: DialogIndeterminate? = null

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        overrideConfiguration?.setLocale(Locale(LocaleManager.getLanguage(this)))
        super.applyOverrideConfiguration(overrideConfiguration)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_location)

        Places.initialize(applicationContext, ConfigPOJO.SECRET_API_KEY)
        presenter.attachView(this)
        dialogIndeterminate = DialogIndeterminate(this)

        recyclerView = findViewById<View>(R.id.places_recycler_view) as RecyclerView
        (findViewById<View>(R.id.place_search) as EditText).addTextChangedListener(filterTextWatcher)
        mAutoCompleteAdapter = PlacesAutoCompleteAdapter(this, intent.getDoubleExtra(Constants.LAT, 0.0),
                intent.getDoubleExtra(Constants.LNG, 0.0))
        recyclerView!!.layoutManager = LinearLayoutManager(this)
        mAutoCompleteAdapter!!.setClickListener(this)
        recyclerView!!.adapter = mAutoCompleteAdapter
        mAutoCompleteAdapter!!.notifyDataSetChanged()

        setAdapter()
    }

    private fun getList() {
        list.clear()
        list.add(LocationPOJO(getString(R.string.food_and_drink), "food", false))
        list.add(LocationPOJO(getString(R.string.restaurant), "restaurant", false))
        list.add(LocationPOJO(getString(R.string.bars), "bar", false))
        list.add(LocationPOJO(getString(R.string.coffee), "cafe", false))
        list.add(LocationPOJO(getString(R.string.take_away), "meal_takeaway", false))
        list.add(LocationPOJO(getString(R.string.delivery), "meal_delivery", false))

        list.add(LocationPOJO(getString(R.string.super_market), "supermarket", false))
        list.add(LocationPOJO(getString(R.string.beauty), "beauty_salon", false))
        list.add(LocationPOJO(getString(R.string.car_supply), "car_dealer", false))
        list.add(LocationPOJO(getString(R.string.home_garden), "home_goods_store", false))
        list.add(LocationPOJO(getString(R.string.shopping_malls), "shopping_mall", false))
        list.add(LocationPOJO(getString(R.string.convience_stores), "convenience_store", false))
//        list.add(LocationPOJO("Mail & shipping", "shopping_mall", false))
        list.add(LocationPOJO(getString(R.string.pharmacies), "pharmacy", false))
    }

    private fun setAdapter() {
        getList()
        nearLocation = NearLocationsAdapter(list, this)
        places_locations.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        places_locations.adapter = nearLocation
    }

    private val filterTextWatcher: TextWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            if (s.toString() != "") {
                mAutoCompleteAdapter!!.filter.filter(s.toString())
                if (recyclerView!!.visibility == View.GONE) {
                    recyclerView!!.visibility = View.VISIBLE
                    places_recycler.visibility = View.GONE
                }
            } else {
                if (recyclerView!!.visibility == View.VISIBLE) {
                    recyclerView!!.visibility = View.GONE
                    places_recycler.visibility = View.VISIBLE
                }
            }
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    }

    override fun click(place: Place?) {
        val intent = Intent()
        intent.putExtra("place", place)
        setResult(-1, intent)
        finish()
    }

    override fun getSelectedLocation(key: String, position: Int) {

        list[position].temp = true
        for (i in list.indices) {
            if (i != position) {
                list[i].temp = false
            }
        }
        nearLocation?.notifyDataSetChanged()
        presenter.nearByPlaces(LatLng(intent.getDoubleExtra(Constants.LAT, 0.0)
                , intent.getDoubleExtra(Constants.LNG, 0.0)), LatLng(0.0, 0.00), LocaleManager.getLanguage(this), key)
    }

    override fun nearByResponse(jsonRootObject: JSONObject, sourceLatLng: LatLng?, destLatLng: LatLng?) {
        val routeArray = jsonRootObject.getJSONArray("results")
        listLocation.clear()
        for (i in 0 until routeArray.length()) {
            val actor = routeArray.getJSONObject(i)
            val overviewPolylines = actor.getJSONObject("geometry")
            val latlng = overviewPolylines.getJSONObject("location")

            listLocation.add(FindLocationPOJO(actor.getString("name"),
                    actor.getString("vicinity"),
                    latlng.getString("lat"),
                    latlng.getString("lng")))
        }

        setLocationAdapter()
    }

    private fun setLocationAdapter() {
        recyclerView?.visibility = View.GONE
        places_recycler.visibility = View.VISIBLE
        nearDataLocation = FindLocationsAdapter(listLocation, this, intent.getDoubleExtra(Constants.LAT, 0.0),
                intent.getDoubleExtra(Constants.LNG, 0.0))
        places_recycler.layoutManager = LinearLayoutManager(this)
        places_recycler.adapter = nearDataLocation
    }

    override fun showLoader(isLoading: Boolean) {
        dialogIndeterminate?.show(isLoading)
    }

    override fun apiFailure() {
        TODO("Not yet implemented")
    }

    override fun handleApiError(code: Int?, error: String?) {
        TODO("Not yet implemented")
    }

    override fun itemSelect(key: FindLocationPOJO, position: Int) {
        val intent = Intent()

        intent.putExtra("address", key.address)
        intent.putExtra("lat", key.lat)
        intent.putExtra("lng", key.lng)
        setResult(-1, intent)
        finish()
    }
}