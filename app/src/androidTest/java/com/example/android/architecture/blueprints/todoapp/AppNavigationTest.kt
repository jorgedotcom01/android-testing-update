package com.example.android.architecture.blueprints.todoapp

import android.app.Activity
import android.view.Gravity
import androidx.appcompat.widget.Toolbar
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerMatchers.isClosed
import androidx.test.espresso.contrib.DrawerMatchers.isOpen
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.tasks.TasksActivity
import com.example.android.architecture.blueprints.todoapp.util.DataBindingIdlingResource
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource
import com.example.android.architecture.blueprints.todoapp.util.monitorActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@LargeTest
class AppNavigationTest {

    private lateinit var tasksRepository: TasksRepository
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun setUp() {
        tasksRepository =
            ServiceLocator.provideTasksRepository(ApplicationProvider.getApplicationContext())
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun tearDown() {
        ServiceLocator.resetRepository()
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun tasksScreen_clickOnDrawerIcon_opensNavigation() {
        // Given
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Check that drawer is closed at startup
        onView(withId(R.id.drawer_layout)).check(matches(isClosed(Gravity.START)))
        // Click drawer icon
        onView(withContentDescription(activityScenario.getToolbarNavigationContentDescription()))
            .perform(click())
        // Check that drawer is open
        onView(withId(R.id.drawer_layout)).check(matches(isOpen(Gravity.START)))

        activityScenario.close()
    }

    @Test
    fun taskDetailScreen_doubleUpButton() = runTest {
        // Given
        val task = Task("Up button", "Description")
        tasksRepository.saveTask(task)
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Click on task and then edit task FAB
        onView(withText("Up button")).perform(click())
        onView(withId(R.id.edit_task_fab)).perform(click())

        // Click on up button and check screen
        val navIconContentDescription = activityScenario.getToolbarNavigationContentDescription()
        onView(withContentDescription(navIconContentDescription)).perform(click())
        onView(withId(R.id.task_detail_title_text)).check(matches(isDisplayed()))

        // Click on up button again and check screen
        onView(withContentDescription(navIconContentDescription)).perform(click())
        onView(withId(R.id.tasks_container_layout)).check(matches(isDisplayed()))

        activityScenario.close()
    }

    @Test
    fun taskDetailScreen_doubleBackButton() = runTest {
        val task = Task("Back button", "Description")
        tasksRepository.saveTask(task)

        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // Click on task and then edit task FAB
        onView(withText("Back button")).perform(click())
        onView(withId(R.id.edit_task_fab)).perform(click())

        // Press back button ande check screen
        pressBack()
        onView(withId(R.id.task_detail_title_text)).check(matches(isDisplayed()))

        // Press back button again and check screen
        pressBack()
        onView(withId(R.id.tasks_container_layout)).check(matches(isDisplayed()))

        activityScenario.close()
    }
}

fun <T : Activity> ActivityScenario<T>.getToolbarNavigationContentDescription(): String {
    var description = ""
    onActivity {
        description = it.findViewById<Toolbar>(R.id.toolbar).navigationContentDescription as String
    }
    return description
}
