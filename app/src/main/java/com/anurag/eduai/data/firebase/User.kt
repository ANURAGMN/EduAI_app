package com.anurag.eduai.data.firebase

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val id: String = "", // from google
    val email: String = "", // from google
    val displayName: String? = "", // from google
    val profilePictureUri: String? = "", // link: from google
    val schoolName: String = "", // input from user
    val phoneNumber: String = "", // input from user
    val studentClass: String= "" // input from user
) : Parcelable