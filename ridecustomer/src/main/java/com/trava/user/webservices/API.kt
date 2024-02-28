package com.trava.user.webservices

import com.trava.driver.webservices.models.WalletBalModel
import com.trava.driver.webservices.models.WalletHisResponse
import com.trava.user.ui.home.orderdetails.omcoproducts.ProductPakageRequest
import com.trava.user.webservices.moby.response.*
import com.trava.user.webservices.models.*
import com.trava.user.webservices.models.add_address_home_work.HomeWorkResponse
import com.trava.user.webservices.models.appsettings.SettingItems
import com.trava.user.webservices.models.cards_model.AddCardResponse
import com.trava.user.webservices.models.categories.CategoriesBannerResponse
import com.trava.user.webservices.models.etoken.PurchasedEToken
import com.trava.user.webservices.models.etokens.ETokensModel
import com.trava.user.webservices.models.homeapi.Category
import com.trava.user.webservices.models.homeapi.ServiceDetails
import com.trava.user.webservices.models.homeapi.TerminologyData
import com.trava.user.webservices.models.order.Order
import com.trava.user.webservices.models.paymentdetails.PaymentDetail
import com.trava.user.webservices.models.nearestroad.RoadPoints
import com.trava.user.webservices.models.noticeBoard.NotificationData
import com.trava.user.webservices.models.promocodes.CouponsItem
import com.trava.user.webservices.models.promocodes.PromoCodeResponse
import com.trava.user.webservices.models.roadPickup.RoadPickupResponse
import com.trava.user.webservices.models.travelPackages.PackagesItem
import com.trava.user.webservices.models.SendMoneyResponseModel
import com.trava.user.webservices.models.applabels.AppLabels
import com.trava.user.webservices.models.appsettings.LanguageSets
import com.trava.user.webservices.models.contacts.*
import com.trava.user.webservices.models.eContacts.EContactsListing
import com.trava.user.webservices.models.earnings.AdsEarningResponse
import com.trava.user.webservices.models.gift.CRequest
import com.trava.user.webservices.models.gift.GiftListingResponse
import com.trava.user.webservices.models.locationaddress.GetAddressesPojo
import com.trava.user.webservices.models.stories.ResultItem
import com.trava.user.webservices.models.stories.StoriesPojo
import com.trava.user.webservices.models.storybonus.StoryBonusPojo
import com.trava.utilities.*
import com.trava.utilities.chatModel.ChatMessageListing
import com.trava.utilities.webservices.models.AppDetail
import com.trava.utilities.webservices.models.LoginModel
import com.trava.utilities.webservices.models.Service
import com.trava.utilities.webservices.models.StoriesPOJO
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONStringer
import retrofit2.Call
import retrofit2.http.*
import java.math.BigInteger

interface API {
    @FormUrlEncoded
    @POST("user/sendOtp")
    fun sendOtp(@FieldMap map: Map<String, String>): Call<ApiResponse<SendOtp>>

    @FormUrlEncoded
    @POST("user/checkuserExists")
    fun checkuserExistance(@Field("social_key") social_key: String,
                           @Field("login_as") login_as: String): Call<ApiResponse<UserExistResult>>

    @FormUrlEncoded
    @POST("user/socailLogin")
    fun socialLogin(@FieldMap map: Map<String, String>): Call<ApiResponse<LoginModel>>

    @FormUrlEncoded
    @POST("user/verifyOTP")
    fun verifyOtp(@FieldMap map: Map<String, String>): Call<ApiResponse<LoginModel>>

    @Multipart
    @POST("user/privateCooperationRegForum")
    fun pvtCooperativeRegfirm(@PartMap map: HashMap<String, RequestBody>): Call<ApiResponse<PvtCooperativeRegResponseModel>>


    @GET("common/appSettings")
    fun appSettingApi(): Call<ApiResponse<SettingItems>>

    @GET("common/appFourmSettings")
    fun getAppLables(): Call<AppLabels<com.trava.user.webservices.models.applabels.Data>>

    @Multipart
    @POST("user/addName")
    fun addName(@PartMap map: HashMap<String, RequestBody>): Call<ApiResponse<AppDetail>>

    @FormUrlEncoded
    @POST("user/service/homeApi")
    fun homeApi(@FieldMap map: Map<String, String>): Call<ApiResponse<ServiceDetails>>

    @FormUrlEncoded
    @POST("user/emailLogin")
    fun emailLogin(@FieldMap map: Map<String, String>): Call<ApiResponse<LoginModel>>


    @GET("common/privateCooperationListing")
    fun getCooperationListing(@Query("items") items : String,@Query("cooperation_type") cooperation_type : String ):Call<ApiResponse<ArrayList<InstituteListing>>>

    @Multipart
    @POST("user/service/Request")
    fun requestService(@PartMap map: Map<String, @JvmSuppressWildcards RequestBody>,
                       @Part imageArray: List<MultipartBody.Part>): Call<ApiResponse<Order>>

    @POST("order/request")
    fun requestPickupReceiveService(@Body productPackageReq: ProductPakageRequest): Call<ApiResponse<com.trava.user.webservices.models.orderrequest.ResultItem>>

    @POST("/order/receiver/Request")
    fun requestReceiveService(@Body productPackageReq: ProductPakageRequest): Call<ApiResponse<com.trava.user.webservices.models.orderrequest.ResultItem>>

    @GET("service/order/getAllZones")
    fun getPolygon() : Call<ApiResponse<ArrayList<GeoFenceData>>>

    @GET("common/languages")
    fun getLanguages() : Call<ApiResponse<ArrayList<LanguageSets>>>

    @FormUrlEncoded
    @POST("user/service/Cancel")
    fun cancelService(@FieldMap map: Map<String, String>): Call<ApiResponse<Any>>

    @POST("user/service/Ongoing")
    fun onGoingOrder(): Call<ApiResponse<List<Order>>>

    /* @GET("directions/json?sensor=false&mode=driving&alternatives=true&units=metric&key=AIzaSyD3dKkbrcH_FWYcnRzoibDJOlw6caer9x4")
     fun getPolYLine(@Query("origin") origin: String,
                     @Query("destination") destination: String,
                     @Query("language") language: String?): Call<ResponseBody> */

    @GET()
    fun getPolYLine(@Url url: String,@Query("origin") origin: String,
                    @Query("destination") destination: String,
                    @Query("language") language: String?): Call<ResponseBody>


    @GET
    fun getPolyLine(@Url url: String): Call<ResponseBody>

    @GET
    fun getOmanCurrency(@Url url: String): Call<ResponseBody>

    @GET
    fun termsnology(@Url url: String): Call<ApiResponse<TerminologyData>>

    @FormUrlEncoded
    @POST("user/other/payment/details")
    fun paymentDetail(@Field("category_brand_id") categoryBrandId: String?,
                      @Field("category_brand_product_id") categoryBrandProductId: String?)
            : Call<ApiResponse<PaymentDetail>>

    @FormUrlEncoded
    @POST("user/service/Rate")
    fun rateService(@Field("ratings") rating: Int?,
                    @Field("order_id") orderId: BigInteger?,
                    @Field("comments") comments: String?): Call<Any>

    @FormUrlEncoded
    @POST("common/updateData")
    fun updateData(@FieldMap map: Map<String, String>): Call<ApiResponse<LoginModel>>

    @FormUrlEncoded
    @POST("user/other/order/history")
    fun getBookingsHistory(@Field("skip") skip: Int,
                           @Field("take") take: Int,
                           @Field("type") type: Int,
                           @Field("start_dt") startdate: String,
                           @Field("end_dt") emdDAte: String,
                           @Field("category_id") category_id: String): Call<ApiResponse<ArrayList<Order?>>>

    @FormUrlEncoded
    @POST("user/other/order/gift_list")
    fun getSentGiftList(@Field("skip") skip: Int,
                           @Field("take") take: Int): Call<ApiResponse<ArrayList<com.trava.user.webservices.models.gift.ResultItem?>>>

    @FormUrlEncoded
    @POST("user/other/order/gift_requested_list")
    fun getReceivedGiftList(@Field("skip") skip: Int,
                           @Field("take") take: Int): Call<ApiResponse<ArrayList<com.trava.user.webservices.models.gift.ResultItem?>>>

    @FormUrlEncoded
    @POST("user/other/order/gift_requested/action")
    fun acceptRejectGift(@Field("order_id") order_id: String,
                           @Field("action") action: String): Call<ApiResponse<Any>>

    @FormUrlEncoded
    @POST("user/other/order/details")
    fun getOrderDetails(@Field("order_id") orderId: Long): Call<ApiResponse<Order>>

//    @FormUrlEncoded
    @POST("user/service/createCheckList")
    fun setCheckListApi(@Body requestBody: RequestBody): Call<ApiResponse<CheckListResponseModel>>

    @POST("common/eContacts")
    fun eContactsList(): Call<ApiResponse<ArrayList<EContact>>>

    @FormUrlEncoded
    @POST("common/contactus")
    fun sendMessage(@Field("message") message: String): Call<Any>

    @Multipart
    @POST("user/profileUpdate")
    fun updateProfile(@PartMap map: HashMap<String, RequestBody>): Call<ApiResponse<AppDetail>>

    @FormUrlEncoded
    @POST("common/settingUpdate")
    fun updateSettings(@Field("language_id") languageId: String?,
                       @Field("notifications") notificationsFlag: String?): Call<ApiResponse<Any>>

    @POST("common/support/list")
    fun supportList(): Call<ApiResponse<ArrayList<Service>>>

    @POST("common/logout")
    fun logout(): Call<Any>

    @GET()
    fun getRoadPoints(@Url url: String,@Query("points") points: String): Call<RoadPoints>

    @POST("user/service/packageListing")
    fun getPackageListing(): Call<ApiResponse<List<PackagesItem>>>

    @FormUrlEncoded
    @POST("user/service/scanQrCode")
    fun getQRCodeScanData(@Field("user_id") scanQRString: String): Call<ApiResponse<RoadPickupResponse>>

    @POST("user/other/coupons")
    fun getPromoCodesList(): Call<ApiResponse<PromoCodeResponse>>

    @FormUrlEncoded
    @POST("user/other/checkCoupons")
    fun checkCoupenCode(@Field("code") couponCode: String): Call<ApiResponse<CouponsItem>>

    @FormUrlEncoded
    @POST("service/order/halfWayStop")
    fun halfWayStopByUser(@FieldMap map: HashMap<String, String>): Call<ApiResponse<Any>>

    @FormUrlEncoded
    @POST("service/order/breakdownRequest")
    fun vehicleBreakdownApi(@FieldMap map: HashMap<String, String>): Call<ApiResponse<Any>>

    @POST("user/other/shareRide")
    fun shareRideApi(@Body jsonObject: ShareRideReqModel): Call<ApiResponse<RideShareResponse>>

    @FormUrlEncoded
    @POST("user/other/cancelShareRide")
    fun cancelRideShareApi(@Field("order_id") orderId: BigInteger): Call<ApiResponse<Any>>

    @FormUrlEncoded
    @POST("service/order/cancelHalfWayStop")
    fun cancleHalfwayStopByUser(@Field("order_id") orderId: BigInteger): Call<ApiResponse<Any>>

    @POST("user/getCreditPoints")
    fun getUserCreditPoints(): Call<ApiResponse<RideShareResponse>>

    @FormUrlEncoded
    @POST("user/panic")
    fun panicClick(@FieldMap map: HashMap<String, Any>): Call<ApiResponse<Any>>

    @FormUrlEncoded
    @POST("service/order/cancelVehicleBreakDown")
    fun cancleVehicleBreakdownByUser(@Field("order_id") orderId: BigInteger): Call<ApiResponse<Any>>

    @FormUrlEncoded
    @POST("user/service/addStops")
    fun addStopsDuringRide(@FieldMap map: HashMap<String, String>): Call<ApiResponse<ArrayList<Order>>>

    @GET("v1/user/other/getAddresses")
    fun requestLocationsData(@QueryMap map: HashMap<String, String>): Call<GetAddressesPojo>

    @FormUrlEncoded
    @POST("user/service/addAddress")
    fun addAddressForHomeWork(@FieldMap map: HashMap<String, String>): Call<ApiResponse<HomeWorkResponse>>

    @FormUrlEncoded
    @POST("user/service/editAddress")
    fun editAddressForHomeWork(@FieldMap map : HashMap<String,String>) : Call<ApiResponse<HomeWorkResponse>>

    @POST("user/service/bannerAndServices")
    fun getBannerAndServiceCategories() : Call<ApiResponse<CategoriesBannerResponse>>

    @POST("common/addCard")
    fun addCard() : Call<ApiResponse<AddCardResponse>>

    @FormUrlEncoded
    @POST("common/removeCard")
    fun removeCard(@Field("user_card_id") cardId : Int) : Call<ApiResponse<Any>>

    @FormUrlEncoded
    @POST("common/payPendingAmount")
    fun payPendingAmountWithCard(@Field("user_card_id") cardId : Int, @Field("amount")amount : Double) : Call<ApiResponse<Any>>

    @POST("user/notificationListing")
    fun getNotificationListing(): Call<ApiResponse<ArrayList<NotificationData>>>

    @POST("user/other/chatlogs")
    @FormUrlEncoded
    fun getChatLogs(
            @Field(LIMIT) limit: Int?,
            @Field(SKIP) skip: Int?): Call<ApiResponse<ArrayList<ChatMessageListing>>>

    @GET("user/other/getChat")
    fun getChatMessages(
            @Query("order_id") orderId: String,
            @Query(MESSAGE_ID) messageId: String,
            @Query(RECEIVER_ID) receiverId: String,
            @Query(LIMIT) limit: Int?,
            @Query(SKIP) skip: Int?,
            @Query(MESSAGE_ORDER) messageOrder: String): Call<ApiResponse<ArrayList<ChatMessageListing>>>

    @FormUrlEncoded
    @POST("user/other/getStories")
    fun getStories(@FieldMap map: HashMap<String , Any>): Call<StoriesPojo<ArrayList<ResultItem>>>

    @FormUrlEncoded
    @POST("user/other/earnings")
    fun requestEarnings(@FieldMap map: HashMap<String , Any>): Call<AdsEarningResponse>

    @FormUrlEncoded
//    @POST("user/other/user_earned")
    @POST("user/other/user_earned_with_time")
    fun saveStoryBonous(@FieldMap map: HashMap<String , Any>): Call<StoryBonusPojo>

    @Multipart
    @POST("common/fileUpload")
    fun uploadImageChat(@PartMap map: HashMap<String, @JvmSuppressWildcards RequestBody>): Call<ApiResponse<ChatMessageListing>>

    @FormUrlEncoded
    @POST("common/addCard")
    fun addCreditCard(@FieldMap map: HashMap<String, Any>): Call<ApiResponse<CardModel>>

    @GET("common/getCardList")
    fun getCreditCardList(): Call<ApiResponse<ArrayList<CardModel>>>

    @FormUrlEncoded
    @POST("common/removeCard")
    fun deleteCreditCard(@FieldMap map: HashMap<String, Any>): Call<DeleteCardResponseModel>

    @FormUrlEncoded
    @POST("user/service/Tip")
    fun tipApi(@FieldMap map: HashMap<String, Any>): Call<ApiResponseNew>

    @FormUrlEncoded
    @POST("common/brainTree/checkout")
    fun paymentApi(@Field("order_id") order_id: String,
                   @Field("amount") amount: String,
                   @Field("nonce") nonce: String): Call<ApiResponseNew>

    @FormUrlEncoded
    @POST("user/other/viewWebsite")
    fun websiteClick(@Field("story_id") story_id: String): Call<ApiResponseNew>

    @FormUrlEncoded
    @POST("common/payTabReturnUrl")
    fun paymentApiPayTab(@Field("order_id") order_id: String,
                   @Field("transaction_id") transaction_id: String): Call<ApiResponseNew>

    @FormUrlEncoded
    @POST("common/braintree/user_token")
    fun getPaypalToken(@FieldMap map: HashMap<String, Any>): Call<PaypalToken>

    @FormUrlEncoded
    @POST("common/dataTrans/return_url")
    fun saveDatatranseData(@FieldMap map: HashMap<String, Any>): Call<ApiResponseNew>

    @FormUrlEncoded
    @POST("/common/razorPayReturnUrl")
    fun saveRazorPaymentData(@FieldMap map: HashMap<String, Any>): Call<ApiResponseNew>

    @FormUrlEncoded
    @POST("common/paymayaReturnUrl")
    fun getPayMayaUrl(@FieldMap map: HashMap<String, Any>): Call<PayMayaResponse>

    @POST("user/service/removeCheckListItem")
    @FormUrlEncoded
    fun deleteCheckList(@Field("check_list_ids") limit: String): Call<ApiResponseReq>

    @POST("user/service/editCheckList")
    @FormUrlEncoded
    fun updateCheckList(@Field("check_lists")check_lists: JSONArray): Call<ApiResponseReq>

    @FormUrlEncoded
//    @POST("/service/other/walletRechargeHistory")
    @POST("service/walletLogs")
    fun getWalletHistory(@Field("skip") skip: Int,
                         @Field("limit") take: Int): Call<WalletHisResponse>

    @FormUrlEncoded
    @POST("service/addMoneyInWallet")
    fun addToWallet(@Field("amount") amount: Int,
                    @Field("user_card_id") card_id: String,
                    @Field("gateway_unique_id") gateway_unique_id: String): Call<ApiResponse<Order?>>


    @FormUrlEncoded
    @POST("service/walletTransfer")
    fun walletTransfer(@Field("amount") amount: Int,
                       @Field("phone_code") phone_code: String,
                       @Field("phone_number") phone_number: String): Call<ApiResponse<SendMoneyResponseModel>>


    @GET("service/getWalletBalance")
    fun getWalletBalance(): Call<ApiResponse<WalletBalModel?>>

    @FormUrlEncoded
    @POST("user/addEmergencyContacts")
    fun addEContatcs(@Field("emergencyContacts") emergencyContacts: JSONArray)
                    :  Call<ApiResponseReq>


    @POST("user/emergencyContactListing")
    fun getEContactsList(): Call<ApiResponse<ArrayList<EContactsListing>>>


    @FormUrlEncoded
    @POST("user/removeEmergencyContacts")
    fun removeEContact(@Field("emergency_contact_id") emergency_contact_id : String): Call<ApiResponseReq>


    @GET("/v1/common/getThiwaniPaymentUrl")
    fun getThiwaniUrl(@QueryMap map: HashMap<String, Any>): Call<ApiResponse<ThawaniData>>


    @FormUrlEncoded
    @POST("/v1/common/razorPayReturnUrl")
    fun saveThiwaniData(@FieldMap map: HashMap<String, Any>): Call<ApiResponseNew>
}