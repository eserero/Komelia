package snd.komelia.settings.model

enum class ReaderTapNavigationMode {
    LEFT_RIGHT,           // Default: Left=Prev, Right=Next
    RIGHT_LEFT,           // Reversed: Left=Next, Right=Prev
    HORIZONTAL_SPLIT,     // Sides Split: Top=Prev, Bottom=Next
    REVERSED_HORIZONTAL_SPLIT // Sides Split: Top=Next, Bottom=Prev
}
