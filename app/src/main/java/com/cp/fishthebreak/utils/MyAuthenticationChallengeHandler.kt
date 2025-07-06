package com.cp.fishthebreak.utils

import android.util.Log
import com.esri.arcgisruntime.security.AuthenticationChallenge
import com.esri.arcgisruntime.security.AuthenticationChallengeHandler
import com.esri.arcgisruntime.security.AuthenticationChallengeResponse
import com.esri.arcgisruntime.security.UserCredential


class MyAuthenticationChallengeHandler(private val username: String, private val password: String) :
    AuthenticationChallengeHandler {
    override fun handleChallenge(challenge: AuthenticationChallenge): AuthenticationChallengeResponse {
        // Log the type of challenge
        Log.d("AuthHandler", "Authentication challenge received: " + challenge.type.name)

        // Provide the credentials when an authentication challenge occurs
        val userCredential = UserCredential(
            username,
            password
        )
        val response = AuthenticationChallengeResponse(
            AuthenticationChallengeResponse.Action.CONTINUE_WITH_CREDENTIAL, userCredential
        )

        // Log the response action
        Log.d("AuthHandler", "Responding to authentication challenge with credentials.")
        return response
    }
}

