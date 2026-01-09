# Progress Queries Usage Guide

## New Queries Added to ProgressDao

### 1. Get Total Completed Concepts

Returns the total number of concepts a student has completed.

```kotlin
// Usage example
val totalCompleted = progressDao.getTotalCompletedConcepts(studentId = "student123")
println("Total completed concepts: $totalCompleted")
```

### 2. Get Concepts Cleared in Last 7 Days (Day-wise)

Returns a list of daily concept completion counts for the last 7 days, ordered from most recent to oldest.

```kotlin
// Calculate timestamp for 7 days ago
val calendar = Calendar.getInstance()
calendar.add(Calendar.DAY_OF_YEAR, -7)
val sevenDaysAgo = calendar.timeInMillis

// Get day-wise data
val dailyProgress = progressDao.getConceptsClearedLast7Days(
    studentId = "student123",
    sevenDaysAgoTimestamp = sevenDaysAgo
)

// Example output format:
// dailyProgress = [
//   DailyConceptCount(date = "2026-01-08", count = 5),  // Today (Monday)
//   DailyConceptCount(date = "2026-01-07", count = 4),  // Sunday
//   DailyConceptCount(date = "2026-01-06", count = 3),  // Saturday
//   ...
// ]

// Display the results
dailyProgress.forEach { day ->
    println("${day.date}: ${day.count} concepts completed")
}
```

### Helper Function to Get Last 7 Days with Zero Padding

If you want to ensure all 7 days are represented (even days with 0 completions):

```kotlin
fun getCompleteWeeklyProgress(studentId: String): List<DailyConceptCount> {
    val calendar = Calendar.getInstance()
    val sevenDaysAgo = calendar.apply { 
        add(Calendar.DAY_OF_YEAR, -7) 
    }.timeInMillis
    
    // Get actual data from database
    val actualData = progressDao.getConceptsClearedLast7Days(studentId, sevenDaysAgo)
    val dataMap = actualData.associateBy { it.date }
    
    // Create list for all 7 days
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val result = mutableListOf<DailyConceptCount>()
    
    calendar.time = Date() // Reset to today
    for (i in 0 until 7) {
        val dateStr = dateFormat.format(calendar.time)
        val count = dataMap[dateStr]?.count ?: 0
        result.add(DailyConceptCount(date = dateStr, count = count))
        calendar.add(Calendar.DAY_OF_YEAR, -1)
    }
    
    return result
}
```

## Data Class

The `DailyConceptCount` data class is defined in ProgressDao.kt:

```kotlin
data class DailyConceptCount(
    val date: String,  // Format: YYYY-MM-DD
    val count: Int
)
```

## Notes

- Both queries filter for `itemType = 'CONCEPT'` and `status = 'COMPLETED'`
- The timestamp is stored in milliseconds since epoch
- The date format returned is `YYYY-MM-DD` (e.g., "2026-01-08")
- Results are ordered from most recent to oldest
