package biko.pougala.coinz

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.matcher.ViewMatchers.withId
import org.junit.Test

@Test
fun collectCoins() {
    onView(withId(R.id.add_friend_message))
}