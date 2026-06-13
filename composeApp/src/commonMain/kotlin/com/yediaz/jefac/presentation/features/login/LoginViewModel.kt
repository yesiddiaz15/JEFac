package com.yediaz.jefac.presentation.features.login

import com.yediaz.jefac.presentation.components.base.BaseViewModel

class LoginViewModel : BaseViewModel<LoginState, LoginIntent, LoginEffect>(
    initialState = LoginState()
) {
    override fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.OnGetStartedClicked -> {
                emitEffect(LoginEffect.NavigateToFirstPeriod)
            }
        }
    }
}
