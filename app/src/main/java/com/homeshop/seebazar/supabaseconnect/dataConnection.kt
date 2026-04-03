package com.homeshop.seebazar.supabaseconnect
//YOUR_URL :  https://zyhjenekabfvdojknkhd.supabase.co
//
//YOUR_ANON_KEY : sb_publishable_FOtRAeWo9fhBVZwOQOMWDw_FqGwiIMn
//val supabase = createSupabaseClient(
//    supabaseUrl = "YOUR_URL",
//    supabaseKey = "YOUR_ANON_KEY"
//) {
//    install(Auth)
//    install(Postgrest)
//}
//
//val result = supabase.auth.signUpWith(Email) {
//    email = emailInput
//    password = passwordInput
//}
//
//val userId = result.user?.id
//
//
//val data = mapOf(
//    "id" to userId,
//    "name" to nameInput,
//    "email" to emailInput,
//    "usertype" to userTypeInput
//)
//
//supabase.postgrest["Profile_Auth"].insert(data)
//if (password != confirmPassword) {
//    // show error
//}
