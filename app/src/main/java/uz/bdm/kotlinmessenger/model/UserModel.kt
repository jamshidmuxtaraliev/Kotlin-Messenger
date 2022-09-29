package uz.bdm.kotlinmessenger.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserModel(
    val uid:String?,
    val username:String?,
    val profileImageUrl:String?
):Parcelable{
    constructor():this("","","")
}
