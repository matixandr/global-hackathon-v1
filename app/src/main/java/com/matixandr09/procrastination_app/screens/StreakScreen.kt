package com.matixandr09.procrastination_app.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.matixandr09.procrastination_app.R
import com.matixandr09.procrastination_app.data.AppViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class StreakViewModel(private val appViewModel: AppViewModel) : ViewModel() {
    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak

    val completedDates: StateFlow<Set<LocalDate>> = appViewModel.completedDates

    init {
        processTasks(appViewModel.completedDates.value.toList())
    }

    private fun processTasks(dates: List<LocalDate>) {
        val sortedDates = dates.distinct().sortedDescending()

        if (sortedDates.isEmpty()) {
            _streak.value = 0
            return
        }

        var currentStreak = 0
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val latestDate = sortedDates.first()

        if (latestDate == today || latestDate == yesterday) {
            currentStreak = 1
            for (i in 0 until sortedDates.size - 1) {
                if (sortedDates[i].minusDays(1) == sortedDates[i + 1]) {
                    currentStreak++
                } else {
                    break
                }
            }
        }

        _streak.value = currentStreak
    }
}

@Composable
fun StreakScreen(
    navController: NavController,
    appViewModel: AppViewModel,
) {
    val streakViewModel: StreakViewModel = viewModel(factory = StreakViewModelFactory(appViewModel))
    val streak by streakViewModel.streak.collectAsState()
    val completedDates by streakViewModel.completedDates.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top orange banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFFF98404))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WindowInsets.systemBars.asPaddingValues())
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            navController.popBackStack()
                        }
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = streak.toString(),
                            color = Color.White,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "current streak!",
                            color = Color.White,
                            fontSize = 20.sp,
                        )
                    }
                    Image(
                        painter = painterResource(id = R.drawable.fire),
                        contentDescription = "Streak Fire",
                        modifier = Modifier.size(70.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Calendar
        val today = LocalDate.now()
        val yearMonth = YearMonth.from(today)
        val daysInMonth = yearMonth.lengthOfMonth()
        val firstDayOfMonth = today.withDayOfMonth(1)
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value // Monday is 1, Sunday is 7

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF0F0F0))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "${today.month.getDisplayName(TextStyle.FULL, Locale.getDefault()).uppercase()} ${today.year}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            val daysOfWeek = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Add empty cells for days before the 1st of the month
                items(firstDayOfWeek - 1) {
                    Box(modifier = Modifier.size(40.dp))
                }
                items(daysInMonth) { day ->
                    val date = today.withDayOfMonth(day + 1)
                    val isCompleted = completedDates.contains(date)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCompleted) Color(0xFFF98404) else Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (day + 1).toString(),
                            fontSize = 14.sp,
                            color = if (isCompleted) Color.White else Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StreakScreenPreview() {
    StreakScreen(rememberNavController(), AppViewModel())
}

class StreakViewModelFactory(private val appViewModel: AppViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StreakViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StreakViewModel(appViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}