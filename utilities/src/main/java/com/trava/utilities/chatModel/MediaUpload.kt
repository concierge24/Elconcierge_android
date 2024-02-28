package com.trava.utilities.chatModel

import com.trava.utilities.MediaUploadStatus
import java.io.File

data class MediaUpload(
        var mediaUploadStatus: String = MediaUploadStatus.UPLOADED,
        var transferId: Int? = -1,
        var file: File?
)