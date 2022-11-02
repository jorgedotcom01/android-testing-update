package com.example.android.architecture.blueprints.todoapp.data.source.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.example.android.architecture.blueprints.todoapp.data.Task
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@SmallTest
class TasksDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: ToDoDatabase
    private lateinit var tasksDao: TasksDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ToDoDatabase::class.java
        ).allowMainThreadQueries().build()
        tasksDao = database.taskDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertTaskAndGetById() = runTest {
        // Given
        val task = Task("Title", "Description")
        tasksDao.insertTask(task)

        // When
        val loaded = tasksDao.getTaskById(task.id)

        // Then
        assertThat(loaded as Task, `is`(notNullValue()))
        assertThat(loaded.id, `is`(task.id))
        assertThat(loaded.title, `is`(task.title))
        assertThat(loaded.description, `is`(task.description))
        assertThat(loaded.isCompleted, `is`(task.isCompleted))
    }

    @Test
    fun updateTaskAndGetById() = runTest {
        // Given
        val task = Task("Title", "Description")
        tasksDao.insertTask(task)

        // When
        val updatedTask = task.copy(title = "Updated title", description = "Updated description")
        tasksDao.updateTask(updatedTask)

        // Then
        val loaded = tasksDao.getTaskById(task.id)
        assertThat(loaded as Task, `is`(notNullValue()))
        assertThat(loaded.title, `is`(updatedTask.title))
        assertThat(loaded.description, `is`(updatedTask.description))
        assertThat(loaded.isCompleted, `is`(updatedTask.isCompleted))
    }
}
