package com.example.android.architecture.blueprints.todoapp.tasks

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.android.architecture.blueprints.todoapp.R
import com.example.android.architecture.blueprints.todoapp.ServiceLocator
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.FakeAndroidTestRepository
import com.example.android.architecture.blueprints.todoapp.data.source.TasksRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@MediumTest
@OptIn(ExperimentalCoroutinesApi::class)
class TasksFragmentTest {

    private lateinit var repository: TasksRepository

    @Before
    fun setUp() {
        repository = FakeAndroidTestRepository()
        ServiceLocator.tasksRepository = repository
    }

    @After
    fun tearDown() = runTest {
        ServiceLocator.resetRepository()
    }

    @Test
    fun clickTask_navigateToDetailFragmentOne() = runTest {
        // Given
        repository.saveTask(Task("Title1", "Description1", false, "id1"))
        repository.saveTask(Task("Title2", "Description2", true, "id2"))

        val navController = mock(NavController::class.java)
        launchFragmentInContainer<TasksFragment>(Bundle(), R.style.AppTheme).also {
            it.onFragment { fragment ->
                Navigation.setViewNavController(fragment.view!!, navController)
            }
        }

        // When
        onView(withId(R.id.tasks_list)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText("Title1")),
                click()
            )
        )

        // Then
        verify(navController).navigate(
            TasksFragmentDirections.actionTasksFragmentToTaskDetailFragment("id1")
        )
    }

    @Test
    fun clickAddTaskButton_navigateToAddEditFragment() = runTest {
        // Given
        val navController = mock(NavController::class.java)
        launchFragmentInContainer<TasksFragment>(Bundle(), R.style.AppTheme).also {
            it.onFragment { fragment ->
                Navigation.setViewNavController(fragment.view!!, navController)
            }
        }

        // When
        onView(withId(R.id.add_task_fab)).perform(click())

        // Then
        verify(navController).navigate(
            TasksFragmentDirections.actionTasksFragmentToAddEditTaskFragment(
                null,
                getApplicationContext<Context>().getString(R.string.add_task)
            )
        )
    }
}
