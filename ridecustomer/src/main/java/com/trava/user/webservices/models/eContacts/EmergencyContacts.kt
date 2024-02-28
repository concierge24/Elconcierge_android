import com.google.gson.annotations.SerializedName

data class EmergencyContacts (

		@SerializedName("phone_code") val phone_code : String,
		@SerializedName("phone_number") val phone_number : String
)