package com.trava.user.webservices.models

data class VersioningDetail(
        var force: Float,
        var normal: Float,
        var user: VersioningDetail
)