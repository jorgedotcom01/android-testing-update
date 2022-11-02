package com.example.android.architecture.blueprints.todoapp.statistics

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.android.architecture.blueprints.todoapp.MainCoroutineRule
import com.example.android.architecture.blueprints.todoapp.data.source.FakeTestRepository
import com.example.android.architecture.blueprints.todoapp.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModelTest {

    private lateinit var statisticsViewModel: StatisticsViewModel
    private lateinit var tasksRepository: FakeTestRepository

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        tasksRepository = FakeTestRepository()
        statisticsViewModel = StatisticsViewModel(tasksRepository)
    }

    /**
     * Since MainCoroutineRule uses UnconfinedTestDispatcher by default, we override it here with
     * StandardTestDispatcher so tasks are not dispatched immediately in order to perform checks
     * previously done using pauseDispatcher() and resumeDispatcher()
     */
    @Test
    fun loadTasks_loading() = runTest {
        Dispatchers.setMain(StandardTestDispatcher())

        // When the refresh() function is called _dataLoading value is set to true
        statisticsViewModel.refresh()

        // Because the coroutine that updates the _dataLoading value back to false has not yet being
        // executed we can check for the initial update
        assertThat(statisticsViewModel.dataLoading.getOrAwaitValue(), `is`(true))
        // We then signal the TestCoroutineScheduler to execute all pending tasks
        advanceUntilIdle()
        // Now we can check for the value updated by the coroutine
        assertThat(statisticsViewModel.dataLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun loadStatisticsWhenTasksAreUnavailable_callErrorToDisplay() {
        // Given
        tasksRepository.setReturnError(true)

        // When
        statisticsViewModel.refresh()

        // Then
        assertThat(statisticsViewModel.error.getOrAwaitValue(), `is`(true))
        assertThat(statisticsViewModel.empty.getOrAwaitValue(), `is`(true))
    }
}
