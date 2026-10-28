# Bottom Navigation Bar in Android (Kotlin + XML)

## 1. What is a Bottom Navigation Bar?

A **Bottom Navigation Bar** lets users switch between the main destinations of an app. It usually appears at the bottom of the screen and contains 3–5 destinations.

Examples:
- Home
- Search
- Notifications
- Profile

Use it when destinations are equally important and users need to move between them frequently.

**Do not confuse it with:**
- **Navigation drawer:** a side panel for more destinations.
- **Bottom app bar:** a bar that may contain actions or a Floating Action Button.
- **Tabs:** usually switch between related views within one destination.

---

## 2. Add the Material Components dependency

In `app/build.gradle.kts`, use your project's existing Material dependency if it is already configured:

```kotlin
dependencies {
    implementation(libs.material)
}
```

If your project does not use a version catalog, you can instead declare a Material Components dependency directly:

```kotlin
implementation("com.google.android.material:material:1.12.0")
```

**Do not add both declarations** if they resolve to the same library. Sync Gradle after changing dependencies.

---

## 3. Make sure the app uses a compatible theme

Material Components views require a compatible AppCompat-based theme. A typical Material 3 theme is:

`res/values/themes.xml`

```xml
<resources>
    <style name="Base.Theme.MyApp"
        parent="Theme.Material3.DayNight.NoActionBar">
        <!-- Add your theme colors here. -->
    </style>

    <style name="Theme.MyApp" parent="Base.Theme.MyApp" />
</resources>
```

Set the theme in `AndroidManifest.xml`:

```xml
<application
    android:theme="@style/Theme.MyApp"
    ... >
</application>
```

Replace `MyApp` with your actual theme name. If you have `values-night/themes.xml`, keep its theme definitions compatible too.

---

## 4. Create menu items

Create this file:

`res/menu/bottom_nav_menu.xml`

If the `menu` folder does not exist, create it under `res`.

```xml
<?xml version="1.0" encoding="utf-8"?>
<menu xmlns:android="http://schemas.android.com/apk/res/android">

    <item
        android:id="@+id/navigation_home"
        android:icon="@drawable/ic_home"
        android:title="Home" />

    <item
        android:id="@+id/navigation_search"
        android:icon="@drawable/ic_search"
        android:title="Search" />

    <item
        android:id="@+id/navigation_profile"
        android:icon="@drawable/ic_profile"
        android:title="Profile" />

</menu>
```

### Important: icons

The names `ic_home`, `ic_search`, and `ic_profile` are examples. Add matching vector drawables to `res/drawable`, or replace the references with icons already in your project.

In Android Studio, you can create a vector asset using **res → New → Vector Asset**.

---

## 5. Add the BottomNavigationView to XML

Example `res/layout/activity_main.xml` using `ConstraintLayout`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <FrameLayout
        android:id="@+id/fragmentContainer"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toTopOf="@id/bottomNavigation"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <com.google.android.material.bottomnavigation.BottomNavigationView
        android:id="@+id/bottomNavigation"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        app:menu="@menu/bottom_nav_menu"
        app:labelVisibilityMode="labeled"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintBottom_toBottomOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

`FrameLayout` is a simple content container. In a real multi-screen app, you can use a `FragmentContainerView` and AndroidX Navigation.

---

## 6. Handle item clicks in Kotlin

For a beginner demo, you can show a message when the user selects an item.

`MainActivity.kt`:

```kotlin
package com.example.myapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigation =
            findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    showMessage("Home selected")
                    true
                }

                R.id.navigation_search -> {
                    showMessage("Search selected")
                    true
                }

                R.id.navigation_profile -> {
                    showMessage("Profile selected")
                    true
                }

                else -> false
            }
        }

        // Select Home when the screen first opens.
        bottomNavigation.selectedItemId = R.id.navigation_home
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
```

Replace the package name with the package name from your own project.

### What does `true` mean?

Returning `true` tells the view that the selection was handled. Returning `false` means the selection was not handled.

This demo only shows a Toast; it does **not** switch screen content yet.

---

## 7. Switch actual screens

For a real app, create a Fragment for each destination, for example:
- `HomeFragment`
- `SearchFragment`
- `ProfileFragment`

Then replace the content in the container when an item is selected:

```kotlin
private fun openFragment(fragment: androidx.fragment.app.Fragment) {
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragmentContainer, fragment)
        .commit()
}
```

Use it in the listener:

```kotlin
bottomNavigation.setOnItemSelectedListener { item ->
    when (item.itemId) {
        R.id.navigation_home -> {
            openFragment(HomeFragment())
            true
        }

        R.id.navigation_search -> {
            openFragment(SearchFragment())
            true
        }

        R.id.navigation_profile -> {
            openFragment(ProfileFragment())
            true
        }

        else -> false
    }
}
```

This requires the three Fragment classes and their layouts to exist. For a larger app, prefer **AndroidX Navigation** with a `NavHostFragment` and `NavigationUI` so back-stack behavior is handled consistently.

---

## 8. Useful attributes

| Attribute | Purpose |
|---|---|
| `app:menu` | Connects the menu XML file |
| `app:labelVisibilityMode` | Controls when item labels appear |
| `app:itemIconTint` | Sets icon tint |
| `app:itemTextColor` | Sets label text color |
| `app:itemActiveIndicatorEnabled` | Controls the active indicator where supported |
| `android:background` | Sets the view background |

For example:

```xml
app:labelVisibilityMode="labeled"
```

Common label modes include:
- `labeled`: show every label.
- `selected`: emphasize the selected item's label.
- `unlabeled`: hide labels.
- `auto`: choose a mode based on the number of items and available space.

Exact appearance and available attributes depend on the Material Components version.

---

## 9. Common errors

### Error: `The style on this component requires your app theme to be Theme.AppCompat (or a descendant).`

Check that:
1. The app actually uses your custom theme in `AndroidManifest.xml`.
2. The theme inherits from a compatible Material Components theme.
3. `values-night/themes.xml` does not override it with an incompatible parent.
4. Gradle is resolving the intended Material Components version.

### Error: `resource menu/bottom_nav_menu not found`

Make sure the file is located at:

`app/src/main/res/menu/bottom_nav_menu.xml`

### Error: `resource drawable/ic_home not found`

Add the icon drawable or update the menu item to use an existing resource.

### Tapping an item does not change the screen

A `BottomNavigationView` only provides the navigation controls. Your Kotlin listener or Navigation component must display the destination content.

### Too many items

Keep the bar focused on **3–5 top-level destinations**. Put less frequently used destinations elsewhere.

---

## 10. Best practices

- Use clear, short labels and recognizable icons.
- Keep the same bottom navigation across top-level screens.
- Do not use it for a linear next/previous workflow.
- Preserve each destination's state where appropriate.
- Use fragments or AndroidX Navigation for real screen changes.
- Test with larger font settings, light/dark themes, and different screen sizes.
- Avoid manually adding system navigation-bar padding without checking edge-to-edge behavior; follow the edge-to-edge setup appropriate for your target SDK.

---

## Quick recap

1. Add or verify the Material Components dependency.
2. Use a compatible app theme.
3. Create `res/menu/bottom_nav_menu.xml`.
4. Add `BottomNavigationView` to your layout.
5. Handle selection in Kotlin.
6. Use Fragments or AndroidX Navigation to switch real screen content.

**Beginner exercise:** Build an app with Home, Search, and Profile destinations. First show a Toast for each selection, then replace the Toast behavior with three Fragments.
