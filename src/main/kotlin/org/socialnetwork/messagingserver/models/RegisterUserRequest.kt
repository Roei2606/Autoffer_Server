package org.socialnetwork.messagingserver.models

class RegisterUserRequest(
    val username: String?
){
    constructor() : this(null)
}