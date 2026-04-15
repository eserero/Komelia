# Navigation (Voyager)

Use [Voyager](https://voyager.adriel.cafe/) for navigation. Do NOT use Jetpack Compose Navigation.

## Screens

- Screens implement `Screen` (or `ReloadableScreen` if they support pull-to-refresh/reload)
- Use the domain ID as `ScreenKey` to prevent duplicate screens on the back stack
- Define constructors that accept both an ID and a full domain object (object is `@Transient` — not serialized)

```kotlin
class BookScreen(
    val bookId: KomgaBookId,
    @Transient val book: KomeliaBook? = null,
) : ReloadableScreen {
    override val key: ScreenKey = bookId.toString()
    constructor(book: KomeliaBook) : this(book.id, book)
}
```

## Factory Functions

Use a top-level factory function when navigation logic must choose between Screen subclasses:

```kotlin
fun bookScreen(book: KomeliaBook): Screen =
    if (book.oneshot) OneshotScreen(book) else BookScreen(book)
```

- Call the factory, not the Screen constructor, at call sites
- If there's no routing logic, calling the constructor directly is fine
