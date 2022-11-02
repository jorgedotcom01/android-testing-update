package com.example.android.architecture.blueprints.todoapp.data.source.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.android.architecture.blueprints.todoapp.data.Result
import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.succeeded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@MediumTest
class TasksLocalDataSourceTests {

    private lateinit var localDataSource: TasksLocalDataSource
    private lateinit var database: ToDoDatabase

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ToDoDatabase::class.java
        ).allowMainThreadQueries().build()
        localDataSource = TasksLocalDataSource(database.taskDao(), Dispatchers.Main)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun saveTask_retrievesTask() = runTest {
        // Given
        val task = Task("Title", "Description")
        localDataSource.saveTask(task)

        // When
        val result = localDataSource.getTask(task.id)

        // Then
        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat(result.data.title, `is`(task.title))
        assertThat(result.data.description, `is`(task.description))
        assertThat(result.data.isCompleted, `is`(task.isCompleted))
    }

    @Test
    fun completeTask_retrievedTaskIsComplete() = runTest {
        // Given
        val task = Task("Title", "Description")
        localDataSource.saveTask(task)

        // When
        localDataSource.completeTask(task.id)
        val result = localDataSource.getTask(task.id)

        // Then
        assertThat(result.succeeded, `is`(true))
        result as Result.Success
        assertThat(result.data.isCompleted, `is`(true))
    }
}
