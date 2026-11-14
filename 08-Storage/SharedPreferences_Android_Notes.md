# SharedPreferences in Android (Kotlin)

## Beginner-to-Advanced Notes

> These notes use Kotlin and the classic Android `SharedPreferences`
> API. For new projects, also learn **DataStore**, which is the modern
> recommended option for small persistent key-value data.

------------------------------------------------------------------------

## 1. What is SharedPreferences?

`SharedPreferences` is an Android API for storing small amounts of
primitive key-value data persistently.

Examples:

-   Is the user logged in?
-   What theme did the user choose?
-   Has the user completed onboarding?
-   What language did the user select?
-   What notification setting did the user choose?

It stores data as **key-value pairs** in a preference file.

Example:

  Key            Value
  -------------- ------------
  `isLoggedIn`   `true`
  `userName`     `"Mazhar"`
  `fontSize`     `16`
  `isDarkMode`   `false`

The data remains available after an Activity is destroyed, the app is
closed, or the device is restarted. It normally remains until the app
clears or removes it, or the app is uninstalled (Android backup/restore
can affect data restoration).

### SharedPreferences is not:

-   A database for large or complex data.
-   A replacement for Room or SQLite.
-   A secure vault for passwords, access tokens, or sensitive personal
    information.
-   A way to share ordinary in-memory variables between Activities.

------------------------------------------------------------------------

## 2. How does it work?

Think of a preference file as a small notebook stored by Android.

``` text
Your Android app
      |
      v
SharedPreferences file: "AppSettings"
      |
      +-- "isLoggedIn"  -> true
      +-- "userName"    -> "Mazhar"
      +-- "darkMode"    -> false
```

Any component in the same app can open the same preference file by using
the same file name and read its values.

For example:

``` kotlin
val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)
```

Calling this in `LoginActivity`, `HomeActivity`, or `ProfileActivity`
opens the same preference file.

**Important:** Creating another `SharedPreferences` reference does not
create a new independent set of values if the same file name and mode
are used.

------------------------------------------------------------------------

## 3. Get a SharedPreferences instance

Inside an Activity:

``` kotlin
val prefs = getSharedPreferences(
    "AppSettings",
    Context.MODE_PRIVATE
)
```

Inside another class, use a Context:

``` kotlin
class SettingsManager(context: Context) {
    private val prefs = context.getSharedPreferences(
        "AppSettings",
        Context.MODE_PRIVATE
    )
}
```

### Parameters

**First parameter: file name**

`"AppSettings"` is the name of the preference file. Use the same name
wherever you want to access the same file.

**Second parameter: mode**

`Context.MODE_PRIVATE` is the normal mode. It means the preference file
is private to your application. Do not use deprecated world-readable or
world-writable modes.

### `getPreferences()` vs `getSharedPreferences()`

  --------------------------------------------------------------------------
  API                                    Meaning
  -------------------------------------- -----------------------------------
  `getSharedPreferences("name", mode)`   Opens a named preference file;
                                         useful across Activities

  `getPreferences(mode)`                 Opens the default preference file
                                         associated with that Activity
  --------------------------------------------------------------------------

For app-wide settings or login state, a named file with
`getSharedPreferences()` is usually clearer.

------------------------------------------------------------------------

## 4. Supported data types

The classic API supports:

-   `Boolean`
-   `Int`
-   `Long`
-   `Float`
-   `String`
-   `Set<String>`

It does not directly store custom objects, lists of arbitrary objects,
images, or complex nested structures.

### Save values

``` kotlin
val prefs = getSharedPreferences("AppSettings", MODE_PRIVATE)

prefs.edit()
    .putBoolean("isLoggedIn", true)
    .putString("userName", "Mazhar")
    .putInt("age", 25)
    .putLong("lastLogin", System.currentTimeMillis())
    .putFloat("fontSize", 16f)
    .putStringSet("favoriteTopics", setOf("Kotlin", "Android"))
    .apply()
```

### Read values

``` kotlin
val isLoggedIn = prefs.getBoolean("isLoggedIn", false)
val userName = prefs.getString("userName", "Guest")
val age = prefs.getInt("age", 0)
val lastLogin = prefs.getLong("lastLogin", 0L)
val fontSize = prefs.getFloat("fontSize", 14f)
val topics = prefs.getStringSet("favoriteTopics", emptySet())
```

The second argument is the **default value**. Android returns it when
the key has not been stored.

### Important note about StringSet

Treat a returned `StringSet` as read-only. Do not modify the returned
set directly. Make a copy before changing it:

``` kotlin
val topics = prefs.getStringSet("favoriteTopics", emptySet())
    ?.toMutableSet() ?: mutableSetOf()

topics.add("Jetpack Compose")

prefs.edit()
    .putStringSet("favoriteTopics", topics)
    .apply()
```

------------------------------------------------------------------------

## 5. Understand Editor, apply(), and commit()

To change preferences, call `edit()`.

``` kotlin
val editor = prefs.edit()

editor.putString("userName", "Mazhar")
editor.putBoolean("isLoggedIn", true)

editor.apply()
```

### What does `edit()` do?

It gives you a `SharedPreferences.Editor`. The editor collects changes
that you want to make.

### What does `apply()` do?

-   Updates the in-memory preference values immediately.
-   Writes the changes to persistent storage asynchronously.
-   Does not return a success Boolean.
-   Is generally preferred for normal app usage.

### What does `commit()` do?

``` kotlin
val success = prefs.edit()
    .putBoolean("isLoggedIn", true)
    .commit()
```

-   Writes synchronously.
-   Returns `true` if the write succeeded, otherwise `false`.
-   Can block the calling thread, so avoid using it on the main/UI
    thread.

**Rule of thumb:** Use `apply()` in normal app code. Use `commit()` only
when you specifically need its synchronous result and can safely perform
the operation off the main thread.

### Multiple values in one edit

``` kotlin
prefs.edit()
    .putString("name", "Mazhar")
    .putInt("age", 25)
    .putBoolean("darkMode", true)
    .apply()
```

This is preferable to opening an editor and applying separate writes for
every value.

------------------------------------------------------------------------

## 6. Update, remove, and clear

### Update a value

Use the same key with a new value:

``` kotlin
prefs.edit()
    .putString("userName", "Rahim")
    .apply()
```

If `userName` already existed, its value is replaced.

### Remove one key

``` kotlin
prefs.edit()
    .remove("userName")
    .apply()
```

### Clear all keys in that preference file

``` kotlin
prefs.edit()
    .clear()
    .apply()
```

Be careful: `clear()` removes every key in that preference file, not
just login information. Prefer `remove("specificKey")` when only one
value should be deleted.

------------------------------------------------------------------------

# Part 2 --- Practical Android Projects

## 7. Project 1: Remember a user's login state

### Goal

Create these Activities:

1.  `LoginActivity`
2.  `HomeActivity`
3.  `ProfileActivity`

Expected behavior:

-   First launch: show LoginActivity.
-   Successful login: save login state and open HomeActivity.
-   Open ProfileActivity: user remains logged in.
-   Close and reopen app: user goes directly to HomeActivity.
-   Logout: clear the login state and return to LoginActivity.

### Step 1: Create a helper class

Create `LoginPreferences.kt`:

``` kotlin
class LoginPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(
        "LoginPrefs",
        Context.MODE_PRIVATE
    )

    fun saveLoginStatus(isLoggedIn: Boolean) {
        prefs.edit()
            .putBoolean("isLoggedIn", isLoggedIn)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("isLoggedIn", false)
    }

    fun logout() {
        prefs.edit()
            .remove("isLoggedIn")
            .apply()
    }
}
```

Why use a helper class?

Without it, every Activity may repeat the same preference-file name, key
names, and read/write code. A helper centralizes that logic and makes it
easier to maintain.

### Step 2: Check login state when LoginActivity starts

``` kotlin
class LoginActivity : AppCompatActivity() {

    private lateinit var loginPrefs: LoginPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loginPrefs = LoginPreferences(this)

        if (loginPrefs.isLoggedIn()) {
            openHome()
            return
        }

        setContentView(R.layout.activity_login)

        // Set up your views and login button here.
    }

    private fun openHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
```

### Step 3: Save login state after successful authentication

Inside the login button's click listener:

``` kotlin
loginButton.setOnClickListener {
    val email = emailEditText.text.toString().trim()
    val password = passwordEditText.text.toString()

    // Demo only: replace this with real authentication.
    val loginSuccessful = email == "test@example.com" &&
                          password == "123456"

    if (loginSuccessful) {
        loginPrefs.saveLoginStatus(true)

        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    } else {
        Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
    }
}
```

The hard-coded credentials are only for learning. Real apps must
authenticate through a trusted backend or an authentication provider.

### Step 4: Protect HomeActivity and ProfileActivity

A simple repeated check:

``` kotlin
class HomeActivity : AppCompatActivity() {

    private lateinit var loginPrefs: LoginPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loginPrefs = LoginPreferences(this)

        if (!loginPrefs.isLoggedIn()) {
            goToLogin()
            return
        }

        setContentView(R.layout.activity_home)
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                       Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
```

Use the same guard in ProfileActivity.

### Step 5: Logout

``` kotlin
logoutButton.setOnClickListener {
    loginPrefs.logout()

    val intent = Intent(this, LoginActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                   Intent.FLAG_ACTIVITY_CLEAR_TASK

    startActivity(intent)
    finish()
}
```

The flags clear the old Activity back stack so pressing Back does not
return to the authenticated screens.

### Understand the flow

``` text
App starts
   |
   v
LoginActivity
   |
   v
Read isLoggedIn
   |
   +---- false ----> Show login screen
   |
   +---- true -----> HomeActivity
                         |
                         v
                    ProfileActivity
                         |
                         v
                       Logout
                         |
                         v
                 Remove login state
                         |
                         v
                    LoginActivity
```

### Important security distinction

A Boolean such as `isLoggedIn = true` is only a local UI flag. It does
**not** prove that a user is authenticated. A user can potentially
modify local app data or reinstall/restore an app.

For a real application:

-   Authenticate with Firebase Authentication or your backend.
-   Let the authentication SDK/backend manage session credentials.
-   Verify authorization on the server for protected data and
    operations.
-   Do not store plain-text passwords in SharedPreferences.
-   Do not treat a locally stored Boolean as an authorization mechanism.

------------------------------------------------------------------------

## 8. Project 2: Save and load a username

``` kotlin
class UserPreferences(context: Context) {

    private val prefs = context.getSharedPreferences(
        "UserPrefs",
        Context.MODE_PRIVATE
    )

    fun saveName(name: String) {
        prefs.edit()
            .putString("userName", name)
            .apply()
    }

    fun getName(): String {
        return prefs.getString("userName", "Guest") ?: "Guest"
    }
}
```

In an Activity:

``` kotlin
val userPrefs = UserPreferences(this)

saveButton.setOnClickListener {
    userPrefs.saveName(nameEditText.text.toString())
}

loadButton.setOnClickListener {
    nameTextView.text = userPrefs.getName()
}
```

The name remains stored after the Activity is recreated or the app is
restarted.

------------------------------------------------------------------------

## 9. Project 3: Save a dark/light theme preference

``` kotlin
class ThemePreferences(context: Context) {

    private val prefs = context.getSharedPreferences(
        "ThemePrefs",
        Context.MODE_PRIVATE
    )

    fun saveDarkMode(enabled: Boolean) {
        prefs.edit()
            .putBoolean("darkMode", enabled)
            .apply()
    }

    fun isDarkMode(): Boolean {
        return prefs.getBoolean("darkMode", false)
    }
}
```

Read the preference early in app startup and apply the selected theme
before rendering the UI.

For example, when using AppCompat:

``` kotlin
val themePrefs = ThemePreferences(this)

val mode = if (themePrefs.isDarkMode()) {
    AppCompatDelegate.MODE_NIGHT_YES
} else {
    AppCompatDelegate.MODE_NIGHT_NO
}

AppCompatDelegate.setDefaultNightMode(mode)
```

In a real app, apply the theme centrally (for example, in your
Application class or startup flow) so it is consistent across
Activities.

------------------------------------------------------------------------

# Part 3 --- Intermediate and Advanced Concepts

## 10. Check whether a key exists

``` kotlin
if (prefs.contains("userName")) {
    val name = prefs.getString("userName", null)
}
```

`contains()` checks whether a key exists. It is useful when the
difference between "not saved" and "saved with a default-looking value"
matters.

## 11. Get all stored values

``` kotlin
val allValues: Map<String, *> = prefs.all

allValues.forEach { (key, value) ->
    Log.d("SharedPrefs", "$key = $value")
}
```

Use this for debugging, not as a reason to expose private user data in
production logs.

## 12. Register a preference change listener

You can listen for changes to a preference file:

``` kotlin
private val listener =
    SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->

        if (key == "darkMode") {
            val enabled = sharedPreferences.getBoolean("darkMode", false)
            Log.d("Theme", "Dark mode: $enabled")
        }
    }
```

Register and unregister it:

``` kotlin
override fun onStart() {
    super.onStart()
    prefs.registerOnSharedPreferenceChangeListener(listener)
}

override fun onStop() {
    prefs.unregisterOnSharedPreferenceChangeListener(listener)
    super.onStop()
}
```

**Important:** Keep a strong reference to the listener. The API
documentation warns that the framework does not keep a strong reference
for you. Registering in `onStart()` and unregistering in `onStop()` is a
common Activity lifecycle pattern.

The listener is notified when a preference value changes. Do not assume
it will be called for a value that has not actually changed.

## 13. SharedPreferences across Activities

Example:

``` kotlin
// LoginActivity
val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)

prefs.edit()
    .putString("userName", "Mazhar")
    .apply()
```

``` kotlin
// ProfileActivity
val prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)

val name = prefs.getString("userName", "Guest")
```

Both Activities use:

-   Same application.
-   Same preference file name: `"MyAppPrefs"`.
-   Same access mode.

Therefore, they read the same stored value.

**Remember:** SharedPreferences is not an Activity-to-Activity message
mechanism. It is persistent app storage. Use Intent extras, Activity
Result APIs, ViewModels, or another suitable mechanism for passing
transient data.

## 14. Application context vs Activity context

A helper that keeps a preference reference for a long time should avoid
retaining an Activity unnecessarily.

Prefer:

``` kotlin
class LoginPreferences(context: Context) {

    private val appContext = context.applicationContext

    private val prefs = appContext.getSharedPreferences(
        "LoginPrefs",
        Context.MODE_PRIVATE
    )
}
```

This prevents the helper from holding an Activity context beyond the
Activity's lifecycle.

## 15. Organize preference keys in one place

Avoid repeating string literals throughout your project.

``` kotlin
object PreferenceKeys {
    const val IS_LOGGED_IN = "isLoggedIn"
    const val USER_NAME = "userName"
    const val DARK_MODE = "darkMode"
}
```

Then:

``` kotlin
prefs.edit()
    .putBoolean(PreferenceKeys.IS_LOGGED_IN, true)
    .apply()
```

This reduces spelling mistakes and makes key changes easier.

## 16. Create a centralized preference manager

For a small app, a single manager can keep preference access organized:

``` kotlin
class AppPreferences(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

    var isDarkMode: Boolean
        get() = prefs.getBoolean("darkMode", false)
        set(value) {
            prefs.edit().putBoolean("darkMode", value).apply()
        }

    var userName: String
        get() = prefs.getString("userName", "Guest") ?: "Guest"
        set(value) {
            prefs.edit().putString("userName", value).apply()
        }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
```

Usage:

``` kotlin
val appPrefs = AppPreferences(this)

appPrefs.isDarkMode = true
val currentName = appPrefs.userName
```

For larger apps, consider dependency injection and a repository
abstraction rather than creating managers independently in many places.

------------------------------------------------------------------------

# Part 4 --- Limitations, Security, and Modern Alternatives

## 17. When should you use SharedPreferences?

  -----------------------------------------------------------------------
  Requirement                         Suitable choice
  ----------------------------------- -----------------------------------
  Dark/light mode                     SharedPreferences or DataStore

  Onboarding completed flag           SharedPreferences or DataStore

  Small app settings                  SharedPreferences or DataStore

  Small simple key-value settings     DataStore is preferred for new
                                      projects

  Large structured records            Room

  Offline relational data             Room

  Images and documents                File or media storage

  Complex synchronized app data       Database/repository plus backend as
                                      needed
  -----------------------------------------------------------------------

## 18. SharedPreferences limitations

1.  It is designed for small key-value data.
2.  It does not provide relational queries.
3.  It is not designed for large collections or complex objects.
4.  `apply()` writes asynchronously, but the API still has lifecycle and
    disk-I/O considerations.
5.  It does not provide the same Flow-based reactive model as
    Preferences DataStore.
6.  It does not encrypt values automatically.
7.  It is not a cross-device synchronization mechanism.

## 19. Security: what should not be stored?

Do not store these in plain SharedPreferences:

-   Plain-text passwords.
-   Private keys.
-   Payment card details.
-   Sensitive authentication secrets.
-   Long-lived access tokens without an appropriate secure-storage
    strategy.

For authentication, use a reputable authentication SDK or backend
session design. If your app must store sensitive credentials locally,
follow current Android security guidance and use an appropriate
Keystore-backed encryption design. Encryption alone does not replace
server-side authentication and authorization.

## 20. SharedPreferences vs DataStore

Android recommends DataStore as a modern solution for new small-data
storage needs.

  -----------------------------------------------------------------------
  Feature                 SharedPreferences       Preferences DataStore
  ----------------------- ----------------------- -----------------------
  API style               Synchronous reads       Asynchronous Flow

  Write operation         `apply()` / `commit()`  `suspend` update

  Kotlin Flow             No built-in Flow        Yes

  Threading               `commit()` can block    Designed for
                                                  asynchronous use

  Error handling          Limited API surface     Flow-based read errors

  Complex relational data No                      No; use Room

  Best fit                Existing/simple legacy  New small key-value
                          code                    settings
  -----------------------------------------------------------------------

### Simple Preferences DataStore example

Add the dependency (use the current version recommended by the Android
documentation):

``` kotlin
implementation("androidx.datastore:datastore-preferences:<version>")
```

Create a DataStore:

``` kotlin
val Context.dataStore by preferencesDataStore(name = "settings")
```

Define a key:

``` kotlin
val DARK_MODE = booleanPreferencesKey("dark_mode")
```

Save a value:

``` kotlin
suspend fun saveDarkMode(context: Context, enabled: Boolean) {
    context.dataStore.edit { settings ->
        settings[DARK_MODE] = enabled
    }
}
```

Read a value:

``` kotlin
val darkModeFlow: Flow<Boolean> = context.dataStore.data
    .map { preferences ->
        preferences[DARK_MODE] ?: false
    }
```

Collect the Flow from a lifecycle-aware coroutine scope in your UI.
DataStore is asynchronous, so do not expect a normal immediate return
value like `prefs.getBoolean()`.

------------------------------------------------------------------------

# Part 5 --- Common Mistakes

## 21. Mistake: using different preference file names

``` kotlin
// LoginActivity
getSharedPreferences("LoginPrefs", MODE_PRIVATE)

// ProfileActivity
getSharedPreferences("ProfilePrefs", MODE_PRIVATE)
```

These are two different files. Use the same file name if both Activities
need the same data.

## 22. Mistake: using different keys

``` kotlin
// Saving
prefs.edit().putBoolean("isLoggedIn", true).apply()

// Reading
prefs.getBoolean("loginStatus", false)
```

These keys are different. Use the exact same key.

## 23. Mistake: forgetting the default value

Incorrect:

``` kotlin
val loggedIn = prefs.getBoolean("isLoggedIn")
```

Correct:

``` kotlin
val loggedIn = prefs.getBoolean("isLoggedIn", false)
```

## 24. Mistake: expecting SharedPreferences to store custom objects directly

This will not work:

``` kotlin
// Not supported by SharedPreferences:
prefs.edit().putString("user", userObject)
```

For a small object, you could serialize it to JSON and store the JSON
string, but consider whether DataStore or Room is more appropriate. Do
not use this approach for large or complex data.

## 25. Mistake: calling clear() to remove only login status

``` kotlin
prefs.edit().clear().apply()
```

This removes every key in the file. If you only want to remove login
state:

``` kotlin
prefs.edit()
    .remove("isLoggedIn")
    .apply()
```

------------------------------------------------------------------------

# Part 6 --- Practice Exercises

Try these without copying the solutions first.

### Beginner

1.  Create a preference file named `StudentPrefs`.
2.  Save a student's name.
3.  Save a student's age.
4.  Read and display both values.
5.  Change the student's name and verify the new value.
6.  Remove the age key.
7.  Check whether the name key exists using `contains()`.

### Intermediate

8.  Build a settings screen with a dark-mode Switch.
9.  Save the Switch value when it changes.
10. Restore the Switch value when the Activity opens.
11. Save a selected language (`Bangla` or `English`).
12. Display the saved language in another Activity.
13. Register a preference change listener and observe changes.

### Advanced

14. Build Login, Home, and Profile Activities with a shared preference
    manager.
15. Redirect an already authenticated user away from LoginActivity.
16. Add logout and clear the Activity back stack.
17. Centralize preference keys.
18. Refactor the settings feature to Preferences DataStore.
19. Observe settings through Kotlin Flow and collect them with lifecycle
    awareness.
20. Explain why a locally stored `isLoggedIn` Boolean is not sufficient
    for secure authentication.

------------------------------------------------------------------------

# Part 7 --- Interview Questions

**1. What is SharedPreferences?**

An Android API for storing small amounts of persistent key-value data.

**2. What is MODE_PRIVATE?**

It makes the preference file private to the application.

**3. What is the difference between apply() and commit()?**

`apply()` updates memory immediately and persists asynchronously.
`commit()` persists synchronously and returns a Boolean result.

**4. Can SharedPreferences store custom objects?**

Not directly. It supports primitive values and `Set<String>`. An object
can be serialized, but Room or DataStore may be more suitable depending
on the data.

**5. Can two Activities access the same SharedPreferences?**

Yes, when they use the same application and preference file name.

**6. Does SharedPreferences survive app restarts?**

Yes, normally it persists until the values are changed or removed, app
data is cleared, or the app is uninstalled. Backup/restore can affect
restoration.

**7. Is SharedPreferences secure?**

It is private to the app by default, but its values are not
automatically encrypted. Do not store sensitive secrets in plain
preferences.

**8. What is the difference between getPreferences() and
getSharedPreferences()?**

`getPreferences()` uses an Activity-specific default file;
`getSharedPreferences()` opens a named file.

**9. Why should you avoid commit() on the main thread?**

Because synchronous disk writes can block the UI and cause jank or an
Application Not Responding condition.

**10. What is the modern alternative to SharedPreferences?**

Preferences DataStore for small key-value settings; Room for structured
relational data.

------------------------------------------------------------------------

# Final Summary

Remember these five operations:

``` kotlin
// 1. Get preferences
val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)

// 2. Save
prefs.edit().putBoolean("isLoggedIn", true).apply()

// 3. Read
val loggedIn = prefs.getBoolean("isLoggedIn", false)

// 4. Remove one key
prefs.edit().remove("isLoggedIn").apply()

// 5. Clear all keys in this file
prefs.edit().clear().apply()
```

**The most important idea:** SharedPreferences is persistent key-value
storage. Different Activities can read the same values by opening the
same preference file. For new projects, learn Preferences DataStore as
well.
