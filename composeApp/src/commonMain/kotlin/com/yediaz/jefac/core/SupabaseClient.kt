package com.yediaz.jefac.core

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

val supabase = createSupabaseClient(
    supabaseUrl = SupabaseConfig.url,
    supabaseKey = SupabaseConfig.key
) {
    install(Auth)
    install(Postgrest)
    install(Realtime)
}
