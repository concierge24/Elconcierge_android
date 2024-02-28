package com.trava.utilities

const val AUTHORISATION = "access_token"
const val MESSAGE_ID = "messageId"
const val RECEIVER_ID = "receiver_id"
const val USER_ID = "user_id"
const val LIMIT = "limit"
const val SKIP = "skip"
const val MESSAGE_ORDER = "messageOrder"
const val LAST_MESSAGE = "last_message"
const val PROFILE_PIC_URL = "profilePicURL"
const val USER_NAME = "userName"
const val PHONE = "phone"
const val USER_DETAIL_ID = "userDetailId"
const val CHAT_HISTORY = "CHAT_HISTORY"
const val ORDER_ID = "order_ID"
var EMAIL_ADDRESS = ""
var MOBILE_NUMBER = ""
var COUNTRY_CODE = ""
var LOGIN_TYPE = ""

object Constants {


    var SOCKET_URL =""
    var SECRET_DB_KEY = ""
    var APP_SERVER = ""

    val PLACEAUTOCOMPLETE_REQUESTCODE = 101
    val QR_CODE_REQUEST_CODE = 102
    val PROMOCODE_REQUEST_CODE = 103
    val REQUEST_CODE_PICK_CONTACT = 104
    val ADD_STOPS_REQUEST_CODE = 105
    val ADD_HOME_WORK_ADDRESS_CODE = 106
    val ADD_CARD_REQUEST_CODE = 107

    val TRAVE_APP_BASE = 0
    val SUMMER_APP_BASE = 1
    val GOMOVE_BASE = 2
    val MOVER = 3
    val MOBY = 4
    val CORSA = 5
    val DELIVERY20 = 6
    val EAGLE = 7
    val NO_TEMPLATE = 111

    val LATITUDE = "latitude"
    val LONGITUDE = "longitude"
    val ADDRESS = "address"
    val BOOKING_TYPE = "booking_type"
    val PICKUP_LAT = "pickupLat"
    val PICKUP_LON = "pickupLon"
    val PICKUP_ADDRESS = "pickup_address"
    val DEST_LAT = "Dest_Lat"
    val DEST_LON = "DEST_LON"
    val DEST_ADDRESS = "Dest_address"
    val DEFAULT_REASON_CUSTOMER_CANCELLED = "CANCELLING_WHILE_REQUESTING"
    val PAGE_LIMIT = 20
    val LAT = "latt"
    val LNG = "lngg"
    val PACKAGE_DATA = "packageData"
    val PROMO_DATA = "promoData"
    val COUNTRY_ISO = "COUNTRY_ISO"
    var CATEGORY_DATA = "Category_Data"
    var CARDS_DATA = "CardsData"
    var MAKE_PAYMENT = "makePayment"
    var DELETE_CARD = "deleteCard"
    var PROMO_ID = "p"
    var PROMO_AMMOUNT = "a"
    val DROP_IN_REQUEST: Int = 1001
}

object BOOKING_TYPE {
    val ROAD_PICKUP = "RoadPickup"
    val CARPOOL = "CarPool"
    val NORMAL = "Normal"
    val FRIEND = "Friend"
    val PACKAGE = "Package"
    val GIFT = "Gift"
}

object ChatType {
    const val TEXT = "text"
    const val IMAGE = "image"
}

object COUPEN_TYPE {
    val VALUE = "Value"
    val PERCENTAGE = "Percentage"
}

object StatusCode {
    val SUCCESS = 200
    val BAD_REQUEST = 400
    val UNAUTHORIZED = 401
    val SERVER_ERROR = 500
}

object CategoryId {
    val FREIGHT = "1"
}

object LanguageIds {
    val ENGLISH = 1
    val HINDI = 2
    val URDU = 3
    val CHINESE = 4
    val ARABIC = 5
    val SINHALA = 6
    val TAMIL = 7
}

object MediaUploadStatus {
    const val NOT_UPLOADED = "not_uploaded"
    const val UPLOADING = "uploading"
    const val CANCELED = "canceled"
    const val UPLOADED = "unloaded"
}

class MessageOrder {
    companion object {
        const val BEFORE = "BEFORE"
        const val AFTER = "AFTER"
    }
}

object PaymentType {
    val CASH = "Cash"
    val CARD = "Card"
    val WALLET = "Wallet"
    val E_TOKEN = "eToken"
}

object OrderStatus {
    val SEARCHING = "Searching"
    val ONGOING = "Ongoing"
    val CustCancelReq = "CustCancelReq"
    val CONFIRMED = "Confirmed"
    val REACHED = "Onreached"
    val DriverCancel = "DriverCancel"
    val UNASSIGNED = "Unassigned"
    val PENDING = "Pending"
    val CUSTOMER_CANCEL = "CustCancel"
    val SERVICE_COMPLETE = "SerComplete"
    val SERVICE_HALFWAY_STOP = "SerHalfWayStop"
    val SERVICE_INVOICED = "SerInvoiced"
    val SERVICE_TIMEOUT = "SerTimeout"
    val SERVICE_REJECT = "SerReject"
    val DRIVER_CANCELLED = "DriverCancel"
    val SCHEDULED = "Scheduled"
    val DRIVER_PENDING = "DPending"
    val DRIVER_APPROVED = "DApproved"
    val DRIVER_SCHEDULED_CANCELLED = "DSchCancelled"
    val DRIVER_ARROVED = "DApproved"
    val DRIVER_SCHEDULE_SERVICE = "DPending"
    val DRIVER_START = "About2Start"

    val DRIVER_SCHEDULED_TIMEOUT = "DSchTimeout"
    val SYS_SCHEDULED_CANCELLED = "SysSchCancelled"

    val E_TOKEN_START = "eTokenSerStart"                         //Driver Starts a etoken Ride for customer(Customer) No status only event
    val CUSTOMER_CONFIRMATION_PENDING_ETOKEN = "SerCustPending" //Waiting for customer confirmation(Customer)
    val CUSTOMER_CANCELLED_ETOKEN = "SerCustCancel"            //Customer Cancels etoken order(Driver)
    val CUSTOMER_CONFIRM_ETOKEN = "SerCustConfirm"            //Customer Confirm etoken order(Driver)
    val C_TIMEOUT_ETOKEN = "CTimeout"                        //Customer Do not confirm or rejects in 2 minutes(Driver)
}

object CommonEventTypes {
    val UPDATE_DATA = "UpdateData"
    val HOME_MAP_DRIVERS = "CustHomeMap"
}

object OrderEventType {
    //    User
    val DRIVER_RATED_SERVICE = "DriverRatedService"
    val SERVICE_ACCEPT = "SerAccept"
    val SERVICE_REACHED = "OnReached"
    val SERVICE_REJECT = "SerReject"
    val SERVICE_COMPLETE = "SerComplete"
    val SERVICE_TIMEOUT = "SerTimeout"
    val HELPER_ACCEPT = "HelperAccept"
    val CURRENT_ORDERS = "CurrentOrders"
    val CUSTOMER_SINGLE_ORDER = "CustSingleOrder"
    val VEHICLE_BREAK_DOWN = "SerBreakdown"
    val SERVICE_LONG_DISTANCE = "SerLongDistance"
    val SERVICE_BREAK_DOWN_ACCEPTED = "SerBreakDownAccepted"
    val SERVICE_BREAK_DOWN_REJECTED = "SerBreakDownRejected"

    val DRIVER_CANCELLED = "DriverCancel"
    val DRIVER_HALFWAY_REJECTED = "SerHalfWayStopRejected"

    //   Driver
    val SERVICE_REQUEST = "SerRequest"
    val SERVICE_CANCEL = "SerCancel"
    val SERVICE_OTHER_ACCEPT = "SerOAccept"
    val CUSTOMER_RATED_SERVICE = "CustRatedService"
    val DRIVER_SCHEDULE_SERVICE = "DPending"
    val DRIVER_TIME_OUT = "DSchTimeout"
    val DRIVER_ARROVED = "DApproved"
    val DRIVER_START = "About2Start"
    val SYSTEM_CANCELLED = "SysSchCancelled"
    val ONGOING = "Ongoing"

    //types
    val CUSTOMER_CANCEL = "CustCancel"
    val SCHEDULED = "Scheduled"
    val DRIVER_SCHEDULED_CANCELLED = "DSchCancelled"

    // e-tokens
    val E_TOKEN_START = "eTokenSerStart"                         //Driver Starts a etoken Ride for customer(Customer) No status only event
    val CUSTOMER_CONFIRMATION_PENDING_ETOKEN = "SerCustPending" //Waiting for customer confirmation(Customer)
    val CUSTOMER_CANCELLED_ETOKEN = "SerCustCancel"            //Customer Cancels etoken order(Driver)
    val CUSTOMER_CONFIRM_ETOKEN = "SerCustConfirm"            //Customer Confirm etoken order(Driver)
    val C_TIMEOUT_ETOKEN = "CTimeout"                        //Customer Do not confirm or rejects in 2 minutes(Driver)

}

object Events {
    val ORDER_EVENT = "OrderEvent"
    val COMMON_EVENT = "CommonEvent"

    //    var SEND_MESSAGE:String = "msg"
    var SEND_MESSAGE: String = "sendMessage"

    //  var RECEIVE_MESSAGE:String = "receive"
    var RECEIVE_MESSAGE: String = "receiveMessage"
    var DELIVER_MESSAGE = "deliverMessage"
}

object UtilityConstants {
    const val GALLERY = "GALLERY"
    const val VIDEO = "VIDEO"
    const val CAMERA = "CAMERA"
    const val ACTION_TAKE_VIDEO = 1
    const val PATH = "/capcrop"
    const val IMAGENAME = "imageName"
    const val PHOTO_REQUEST_CAMERA = 2
    const val PHOTO_REQUEST_GALLERY = 3
    const val PHOTO_REQUEST_CROP = 4
    const val IMAGES_PREFIX = "IMG_"
    const val IMAGES_SUFFIX = ".jpg"
}

object Broadcast {
    val NOTIFICATION = "notification"
}

object ADDRESS_TYPE {
    val HOME = "HOME"
    val WORK = "WORK"
}

