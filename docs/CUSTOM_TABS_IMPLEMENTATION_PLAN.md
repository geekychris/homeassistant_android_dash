# Custom Tab Management - Implementation Plan

## Current Status

✅ **Phase 1: Database Layer** - STARTED

- Created `Tab.kt` model
- Created `EntityTab.kt` junction table
- Created `TabDao.kt` with CRUD operations
- Updated `AppDatabase` to version 2

⚠️ **Remaining Work** - NEEDS COMPLETION

## What You Requested

You want to be able to:

1. **Create custom tabs** - Add new tabs with custom names
2. **Rename tabs** - Edit tab names
3. **Assign devices to tabs** - Choose which entities appear in each tab
4. **Delete tabs** - Remove tabs you don't need
5. **Reorder tabs** (bonus) - Drag and drop tab order

## Complete Implementation Required

### 1. Tab Management Screen

**Purpose**: Central place to create, edit, and manage tabs

**Features**:

- List of all tabs for current configuration
- "+" button to create new tab
- Each tab shows: name, device count
- Edit button → Rename tab
- Delete button → Remove tab
- "Manage" button → Assign devices

**Files to Create**:

```
TabManagementFragment.kt
TabManagementViewModel.kt
fragment_tab_management.xml ✅ CREATED
item_tab.xml ✅ CREATED
```

### 2. Tab Dialog (Create/Edit)

**Purpose**: Dialog to create new tab or rename existing

**UI**:

```
┌─────────────────────────┐
│  Enter Tab Name         │
│  ┌───────────────────┐  │
│  │ Living Room       │  │
│  └───────────────────┘  │
│                         │
│  [Cancel]     [Save]    │
└─────────────────────────┘
```

**Files to Create**:

```
dialog_tab_name.xml
```

### 3. Entity Assignment Screen

**Purpose**: Select which devices appear in a tab

**UI**:

```
┌──────────────────────────────┐
│  Assign Devices to "Kitchen" │
├──────────────────────────────┤
│  Search devices...           │
├──────────────────────────────┤
│  ☑ Kitchen Light             │
│  ☑ Kitchen Switch            │
│  ☐ Bedroom Light             │
│  ☐ Living Room Fan           │
│  ☑ Kitchen Thermostat        │
└──────────────────────────────┘
```

**Files to Create**:

```
TabEntityAssignmentFragment.kt
TabEntityAssignmentViewModel.kt  
fragment_tab_entity_assignment.xml
```

### 4. Update Dashboard

**Modifications Needed**:

- Load custom tabs from database instead of automatic room tabs
- Show "All" tab + custom user tabs
- Filter entities based on EntityTab assignments
- Handle empty tabs gracefully

**Files to Modify**:

```
DashboardViewModel.kt - Change tab loading logic
DashboardFragment.kt - Wire up custom tabs
```

### 5. Navigation Integration

**Add to Menu**:

```xml
<item
    android:id="@+id/nav_tab_management"
    android:title="Manage Tabs"
    android:icon="@android:drawable/ic_menu_sort_by_size" />
```

**Add to Navigation Graph**:

```xml
<fragment
    android:id="@+id/nav_tab_management"
    android:name=".ui.tabs.TabManagementFragment"
    android:label="Manage Tabs" />
```

## User Workflow

### Creating a Tab

```
1. User → Bottom Nav → "Settings" or new "Tabs" button
2. Tap "Manage Tabs"
3. Tap "+ Add Tab"
4. Enter name: "Kitchen"
5. Tap "Save"
6. Tab created!
7. Tap "Manage" button on tab
8. Check Kitchen Light, Kitchen Switch, etc.
9. Devices now assigned to Kitchen tab
```

### Using Tabs on Dashboard

```
1. User → Dashboard
2. See tabs: "All" | "Kitchen" | "Bedroom" | "Living Room"
3. Tap "Kitchen"
4. See only devices assigned to Kitchen tab
5. Control devices
```

### Editing a Tab

```
1. User → Manage Tabs
2. Find tab
3. Tap "Edit" button
4. Change name from "Kitchen" to "Main Kitchen"
5. Tap "Save"
6. Tab renamed!
```

### Deleting a Tab

```
1. User → Manage Tabs
2. Find tab
3. Tap "Delete" button
4. Confirm deletion
5. Tab and all assignments deleted
6. Devices still exist, just not in tab anymore
```

## Database Schema

### Tables

**tabs**

```sql
CREATE TABLE tabs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    configurationId INTEGER NOT NULL,
    name TEXT NOT NULL,
    displayOrder INTEGER NOT NULL
)
```

**entity_tabs**

```sql
CREATE TABLE entity_tabs (
    tabId INTEGER NOT NULL,
    entityId TEXT NOT NULL,
    PRIMARY KEY (tabId, entityId)
)
```

### Relationships

```
Configuration ────< Tab ────< EntityTab >──── Entity (from HA)
   (1)          (many)    (many)         (many)
```

## Repository Methods Needed

```kotlin
class HomeAssistantRepository {
    // Tabs
    fun getTabsForConfig(configId: Long): Flow<List<Tab>>
    suspend fun createTab(configId: Long, name: String): Long
    suspend fun updateTab(tab: Tab)
    suspend fun deleteTab(tabId: Long)
    
    // Entity Assignments
    suspend fun getEntitiesForTab(tabId: Long): List<String>
    suspend fun assignEntityToTab(tabId: Long, entityId: String)
    suspend fun removeEntityFromTab(tabId: Long, entityId: String)
    suspend fun getTabsForEntity(entityId: String): List<Tab>
}
```

## UI Components Needed

### 1. TabManagementFragment

- RecyclerView with tab list
- FAB or button to add new tab
- Empty state when no tabs

### 2. TabAdapter

- ViewHolder showing tab name, device count
- Edit button → opens rename dialog
- Delete button → shows confirmation
- Manage button → opens entity assignment

### 3. TabNameDialog

- EditText for tab name
- Save/Cancel buttons
- Validation (non-empty, unique)

### 4. TabEntityAssignmentFragment

- SearchView at top
- RecyclerView with checkboxes
- Shows all entities, checks assigned ones
- Save button to persist changes

### 5. TabEntityAssignmentAdapter

- Checkbox list of entities
- Shows entity name and type
- Filterable by search

## Integration with Dashboard

### Current: Auto-generated Room Tabs

```kotlin
// Generate tabs from rooms
val rooms = entities.map { it.room }.distinct()
tabs = ["All"] + rooms
```

### New: Custom User Tabs

```kotlin
// Load user-defined tabs from database
val userTabs = repository.getTabsForConfig(configId)
tabs = ["All"] + userTabs.map { it.name }

// Filter by selected tab
fun selectTab(tabName: String) {
    if (tabName == "All") {
        show all entities
    } else {
        val tab = userTabs.find { it.name == tabName }
        val entityIds = repository.getEntitiesForTab(tab.id)
        show only entities in entityIds
    }
}
```

## Migration Strategy

### For Existing Users

**Option 1: Start Fresh**

- User creates tabs manually
- No automatic migration

**Option 2: Auto-create Room Tabs**

- On first launch, create tabs from current rooms
- Assign entities to room-based tabs automatically
- User can then customize

**Option 3: Hybrid**

- Keep "All" tab as special (always shows everything)
- Add custom tabs alongside
- User gradually organizes into custom tabs

## Testing Checklist

- [ ] Create a new tab
- [ ] Rename a tab
- [ ] Delete a tab
- [ ] Assign entities to tab
- [ ] Remove entities from tab
- [ ] View tab on dashboard
- [ ] Switch between tabs
- [ ] Empty tab shows correct message
- [ ] Tab with 1 entity works
- [ ] Tab with many entities works
- [ ] Deleting tab doesn't delete entities
- [ ] Multiple configurations have separate tabs

## Future Enhancements

1. **Drag-and-drop tab reordering**
2. **Tab icons** - Custom icons for each tab
3. **Tab colors** - Color coding
4. **Smart tabs** - Auto-assign based on rules (all lights, all switches, etc.)
5. **Tab templates** - Pre-made tab sets (Kitchen, Bedroom, Basement, etc.)
6. **Export/Import tabs** - Share tab configurations
7. **Tab groups** - Nested tabs for complex setups

## Estimated Implementation Time

- Database layer: ✅ Done
- Tab Management UI: 2-3 hours
- Entity Assignment UI: 2-3 hours
- Dashboard Integration: 1-2 hours
- Testing & Polish: 1-2 hours

**Total**: 6-10 hours of development

## Current State

**What's Done**:

- ✅ Database models (Tab, EntityTab)
- ✅ DAO interface
- ✅ Database updated to version 2
- ✅ Layout files for tab management
- ✅ Layout files for tab items

**What's Needed**:

- ⏳ ViewModel classes
- ⏳ Fragment classes
- ⏳ Adapter classes
- ⏳ Dialog for creating/editing tabs
- ⏳ Entity assignment screen
- ⏳ Dashboard integration
- ⏳ Repository methods
- ⏳ Navigation setup

## Next Steps

To complete this feature, you would need:

1. **TabManagementViewModel** - Business logic for tab CRUD
2. **TabManagementFragment** - UI for tab list
3. **TabAdapter** - RecyclerView adapter for tabs
4. **TabEntityAssignmentViewModel** - Logic for assigning entities
5. **TabEntityAssignmentFragment** - UI for entity selection
6. **Update DashboardViewModel** - Use custom tabs instead of auto-generated
7. **Add repository methods** - Wire up database operations
8. **Add navigation** - Connect tab management to app navigation

Would you like me to continue implementing the complete feature, or would you prefer a simplified
version first to test the concept?

---

**Note**: This is a significant feature that requires careful UX design. The foundation (database
layer) is in place, but the UI layer needs substantial work to make it user-friendly and intuitive.
