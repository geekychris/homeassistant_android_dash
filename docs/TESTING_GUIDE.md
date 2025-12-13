# Testing Guide for SimpleHomeAssistant

## 📚 Overview

This guide covers how to create and run tests for your Home Assistant Android app.

## Types of Tests

### 1. Unit Tests (Fast, No Device Needed)

- **Location**: `app/src/test/java/`
- **Run on**: Your computer (JVM)
- **Test**: Pure logic, algorithms, data transformations
- **Speed**: Milliseconds per test
- **Example**: Test tab filtering logic, entity parsing

### 2. Instrumented Tests (Medium, Needs Device)

- **Location**: `app/src/androidTest/java/`
- **Run on**: Android device/emulator
- **Test**: Database, Android framework code
- **Speed**: Seconds per test
- **Example**: Test Room database operations

### 3. UI Tests (Slow, Full Integration)

- **Location**: `app/src/androidTest/java/`
- **Run on**: Android device/emulator
- **Test**: User interactions, complete workflows
- **Speed**: Seconds to minutes
- **Example**: Test "Create tab → Assign entities → View"

---

## 🎯 What to Test in This App

### High Priority Tests

**Repository Layer**:

- ✅ Save/load configurations
- ✅ Create/delete custom tabs
- ✅ Assign entities to tabs
- ✅ Get entities for a tab

**ViewModel Logic**:

- ✅ Tab filtering (show correct entities per tab)
- ✅ Entity state updates
- ✅ Configuration switching

**Database**:

- ✅ Tab CRUD operations
- ✅ Entity-tab relationships (many-to-many)
- ✅ Configuration persistence

**API Layer**:

- ✅ Entity parsing from JSON
- ✅ Control commands (turn on/off)
- ✅ State fetching

### Medium Priority Tests

**UI Components**:

- Tab selection behavior
- Entity adapter updates
- Search functionality

**Edge Cases**:

- Empty tabs
- Missing entities
- Network failures

---

## 🛠️ Setting Up Tests

### Step 1: Add Test Dependencies

Update `app/build.gradle.kts`:

```kotlin
dependencies {
    // ... existing dependencies ...
    
    // Unit testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.3.1")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    
    // Instrumented testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.room:room-testing:2.5.2")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}
```

### Step 2: Create Test Directory Structure

```
app/src/
├── test/java/com/example/simplehomeassistant/
│   ├── repository/
│   │   └── HomeAssistantRepositoryTest.kt
│   ├── viewmodel/
│   │   └── DashboardViewModelTest.kt
│   └── util/
│       └── EntityUtilsTest.kt
│
└── androidTest/java/com/example/simplehomeassistant/
    ├── database/
    │   ├── ConfigurationDaoTest.kt
    │   └── TabDaoTest.kt
    └── ui/
        └── DashboardFragmentTest.kt
```

---

## 📝 Example Tests

I'll create real test examples for your app in separate files.

---

## 🚀 Running Tests

### From Android Studio

**Unit Tests**:

1. Right-click on `test` folder → Run 'Tests in simplehomeassistant'
2. Or click the green play button next to a test class/method

**Instrumented Tests**:

1. Start emulator or connect device
2. Right-click on `androidTest` folder → Run
3. Tests run on the device

### From Command Line

**Run all unit tests**:

```bash
./gradlew test
```

**Run all instrumented tests**:

```bash
./gradlew connectedAndroidTest
```

**Run specific test class**:

```bash
./gradlew test --tests "*.TabDaoTest"
```

**Run with coverage report**:

```bash
./gradlew testDebugUnitTest jacocoTestReport
```

---

## 📊 Test Coverage Goals

**Good Coverage** (Aim for this):

- Repository: 80%+
- ViewModels: 70%+
- Database: 90%+
- Utils: 80%+

**Hard to Test** (Lower priority):

- UI Fragments: 40-60%
- Activities: 40-60%

---

## 🔍 Best Practices

### 1. **Follow AAA Pattern**

```kotlin
@Test
fun testName() {
    // Arrange - Set up test data
    val tab = Tab(...)
    
    // Act - Perform action
    repository.createTab(tab)
    
    // Assert - Verify result
    assertEquals(expected, actual)
}
```

### 2. **Use Descriptive Names**

```kotlin
// ❌ Bad
@Test fun test1() { }

// ✅ Good
@Test fun `when bedroom tab selected, should filter to 4 assigned entities`() { }
```

### 3. **Test One Thing**

```kotlin
// ❌ Bad - tests multiple things
@Test fun testEverything() {
    // test creation
    // test update
    // test deletion
}

// ✅ Good - focused tests
@Test fun `createTab should insert tab with correct name`() { }
@Test fun `updateTab should change tab name`() { }
@Test fun `deleteTab should remove tab and assignments`() { }
```

### 4. **Use Test Fixtures**

```kotlin
@Before
fun setup() {
    // Common test setup
    database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
}

@After
fun teardown() {
    database.close()
}
```

---

## 🎓 Learning Resources

- [Android Testing Codelab](https://developer.android.com/codelabs/advanced-android-kotlin-training-testing-basics)
- [Testing with Room](https://developer.android.com/training/data-storage/room/testing-db)
- [Testing Coroutines](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/)
- [Espresso UI Testing](https://developer.android.com/training/testing/espresso)

---

## 📁 Next Steps

I can create:

1. ✅ Example unit tests for repository
2. ✅ Example database tests for TABs
3. ✅ Example ViewModel tests
4. ✅ CI/CD script to run tests automatically

**Would you like me to create these example test files?**
