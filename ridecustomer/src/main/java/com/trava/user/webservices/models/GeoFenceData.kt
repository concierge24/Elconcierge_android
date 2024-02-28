package com.trava.user.webservices.models

data class GeoFenceData(var id: Int, var coordinates: String,
                        var airport_charges: String,
                        var airport_pickup_charge_fee: String,
                        var airport_drop_charge_fee: String,
                        var alreadyAdded: String="0",
                        var airport_charge_fee: String,
                        var toll_charges: String,
                        var toll_charges_fee: String,
                        var zone_charges: String,
                        var zone_charges_fee: String)