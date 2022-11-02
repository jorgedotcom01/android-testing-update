package com.example.android.architecture.blueprints.todoapp.tasks

import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isNotChecked
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.ServiceLocator
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import com.example.android.architecture.blueprints.todoapp.util.DataBindingIdlingResource
import com.example.android.architecture.blueprints.todoapp.util.EspressoIdlingResource
import com.example.android.architecture.blueprints.todoapp.util.monitorActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@LargeTest
class TasksActivityTest {

    private lateinit var repository: TasksRepository
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun setUp() {
        repository =
            ServiceLocator.provideTasksRepository(ApplicationProvider.getApplicationContext())
        runBlocking {
            repository.deleteAllTasks()
        }
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
    fun editTask() = runTest {
        // Given
        val task = Task("Title", "Description")
        repository.saveTask(task)
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // When
        onView(withText("Title")).perform(click())

        // Then
        // Check views are displayed correctly
        onView(withId(R.id.task_detail_title_text)).check(matches(withText("Title")))
        onView(withId(R.id.task_detail_description_text)).check(matches(withText("Description")))
        onView(withId(R.id.task_detail_complete_checkbox)).check(matches(isNotChecked()))

        // Update task
        onView(withId(R.id.edit_task_fab)).perform(click())
        onView(withId(R.id.add_task_title_edit_text)).perform(replaceText("Updated Title"))
        onView(withId(R.id.add_task_description_edit_text)).perform(replaceText("Updated Description"))
        onView(withId(R.id.save_task_fab)).perform(click())

        // Check that the task displayed is the one just updated
        onView(withText("Updated Title")).check(matches(isDisplayed()))
        onView(withText("Title")).check(doesNotExist())

        activityScenario.close()
    }

    @Test
    fun createOneTask_deleteTask() {
        // Given
        val activityScenario = ActivityScenario.launch(TasksActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // When
        // Create a task
        onView(withId(R.id.add_task_fab)).perform(click())
        onView(withId(R.id.add_task_title_edit_text)).perform(typeText("Title"))
        onView(withId(R.id.add_task_description_edit_text)).perform(
            typeText("Description"),
            closeSoftKeyboard()
        )
        onView(withId(R.id.save_task_fab)).perform(click())

        // Delete the task
        onView(withText("Title")).perform(click())
        onView(withId(R.id.menu_delete)).perform(click())

        // Then
        // Check the task was deleted
        onView((withText("Title"))).check(doesNotExist())

        activityScenario.close()
    }
}
