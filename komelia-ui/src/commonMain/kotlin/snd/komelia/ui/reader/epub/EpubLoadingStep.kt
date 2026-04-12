package snd.komelia.ui.reader.epub

data class EpubLoadingStep(val label: String, val status: EpubLoadingStepStatus)

enum class EpubLoadingStepStatus { Pending, InProgress, Complete }
