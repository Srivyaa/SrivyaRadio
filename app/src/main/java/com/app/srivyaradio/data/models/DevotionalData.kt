package com.app.srivyaradio.data.models

import com.google.gson.annotations.SerializedName

data class DevotionalData(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("homepage")
    val homepage: String,
    
    @SerializedName("generator")
    val generator: String,
    
    @SerializedName("folders")
    val folders: List<DevotionalFolder>
)

data class DevotionalFolder(
    @SerializedName("folder_name")
    val folderName: String,
    
    @SerializedName("folder_uuid")
    val folderUuid: String,
    
    @SerializedName("cover")
    val cover: String,
    
    @SerializedName("year")
    val year: Int,
    
    @SerializedName("items")
    val items: List<DevotionalItem>
)