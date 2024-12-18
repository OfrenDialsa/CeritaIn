package com.dicoding.picodiploma.loginwithanimation.view.screen.auth.login

import android.app.Application
import androidx.test.InstrumentationRegistry
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.ComponentNameMatchers.hasClassName
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.util.EspressoIdlingResource
import com.dicoding.picodiploma.loginwithanimation.util.WaitActivityIsResumedIdlingResource
import com.dicoding.picodiploma.loginwithanimation.view.screen.main.MainActivity
import com.dicoding.picodiploma.loginwithanimation.view.screen.welcome.WelcomeActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginActivityTest {
    @get:Rule
    val activity = ActivityScenarioRule(LoginActivity::class.java)

    private lateinit var homeActivityClassName: String
    private lateinit var waitActivityHome: WaitActivityIsResumedIdlingResource

    @Before
    fun setUp() {
        val application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
        homeActivityClassName = MainActivity::class.java.name
        waitActivityHome = WaitActivityIsResumedIdlingResource(application, homeActivityClassName)

        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        Intents.init()
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        Intents.release()
    }

    @Test
    fun loginWithInvalidData() {
        val invalidEmail = "email"
        val invalidPassword = "pass"

        onView(withId(R.id.ed_login_email))
            .perform(click())
            .perform(typeText(invalidEmail), closeSoftKeyboard())

        onView(withId(R.id.ed_login_password))
            .perform(click())
            .perform(
                typeText(invalidPassword),
                closeSoftKeyboard()
            )

        onView(withId(R.id.loginButton)).perform(click())

        onView(withId(R.id.ed_login_email)).perform(clearText())

        onView(withId(R.id.ed_login_password)).perform(clearText())

        onView(withId(R.id.container_home_activity)).check(doesNotExist())
    }

    @Test
    fun loginWithValidDataAndLogout() {
        val validEmail = "nero24@gmail.com"
        val validPassword = "12345678"

        onView(withId(R.id.ed_login_email)).check(matches(isDisplayed()))

        onView(withId(R.id.ed_login_email))
            .perform(click())
            .perform(typeText(validEmail), closeSoftKeyboard())

        onView(withId(R.id.ed_login_password)).check(matches(isDisplayed()))
        onView(withId(R.id.ed_login_password))
            .perform(click())
            .perform(typeText(validPassword), closeSoftKeyboard())

        onView(withId(R.id.loginButton)).perform(click())

        IdlingRegistry.getInstance().register(waitActivityHome)
        try {
            intended(hasComponent(hasClassName(homeActivityClassName)))
        } finally {
            IdlingRegistry.getInstance().unregister(waitActivityHome)
        }

        onView(withId(R.id.container_home_activity)).check(matches(isDisplayed()))
        onView(withId(R.id.menu_logout)).perform(click())

        onView(withText("Keluar")).check(matches(isDisplayed()))
        onView(withText("Log out")).perform(click())

        EspressoIdlingResource.increment()
        Thread.sleep(1000)
        EspressoIdlingResource.decrement()

        intended(hasComponent(WelcomeActivity::class.java.name))
    }
}