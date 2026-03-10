package snd.komelia.image

suspend fun KomeliaImage.getEdgeColors(vertical: Boolean): Pair<Int, Int>? {
    return if (vertical) {
        val top = extractArea(ImageRect(0, 0, width, 10.coerceAtMost(height)))
        val topColor = top.averageColor()
        top.close()

        val bottom = extractArea(ImageRect(0, (height - 10).coerceAtLeast(0), width, height))
        val bottomColor = bottom.averageColor()
        bottom.close()

        if (topColor != null && bottomColor != null) topColor to bottomColor
        else null
    } else {
        val left = extractArea(ImageRect(0, 0, 10.coerceAtMost(width), height))
        val leftColor = left.averageColor()
        left.close()

        val right = extractArea(ImageRect((width - 10).coerceAtLeast(0), 0, width, height))
        val rightColor = right.averageColor()
        right.close()

        if (leftColor != null && rightColor != null) leftColor to rightColor
        else null
    }
}

suspend fun KomeliaImage.getEdgeColorLines(vertical: Boolean): Pair<ByteArray, ByteArray>? {
    return if (vertical) {
        val top = extractArea(ImageRect(0, 0, width, 10.coerceAtMost(height)))
        val topResized = top.resize(width, 1)
        val topBytes = topResized.getBytes()
        top.close()
        topResized.close()

        val bottom = extractArea(ImageRect(0, (height - 10).coerceAtLeast(0), width, height))
        val bottomResized = bottom.resize(width, 1)
        val bottomBytes = bottomResized.getBytes()
        bottom.close()
        bottomResized.close()

        if (topBytes.isNotEmpty() && bottomBytes.isNotEmpty()) topBytes to bottomBytes
        else null
    } else {
        val left = extractArea(ImageRect(0, 0, 10.coerceAtMost(width), height))
        val leftResized = left.resize(1, height)
        val leftBytes = leftResized.getBytes()
        left.close()
        leftResized.close()

        val right = extractArea(ImageRect((width - 10).coerceAtLeast(0), 0, width, height))
        val rightResized = right.resize(1, height)
        val rightBytes = rightResized.getBytes()
        right.close()
        rightResized.close()

        if (leftBytes.isNotEmpty() && rightBytes.isNotEmpty()) leftBytes to rightBytes
        else null
    }
}

suspend fun KomeliaImage.getEdgeSampling(): EdgeSampling {
    val top = sampleEdge(ImageRect(0, 0, width, 10.coerceAtMost(height)), true)
    val bottom = sampleEdge(ImageRect(0, (height - 10).coerceAtLeast(0), width, height), true)
    val left = sampleEdge(ImageRect(0, 0, 10.coerceAtMost(width), height), false)
    val right = sampleEdge(ImageRect((width - 10).coerceAtLeast(0), 0, width, height), false)

    return EdgeSampling(
        top = top,
        bottom = bottom,
        left = left,
        right = right
    )
}

private suspend fun KomeliaImage.sampleEdge(rect: ImageRect, horizontal: Boolean): EdgeSample? {
    if (rect.width <= 0 || rect.height <= 0) return null
    val edge = extractArea(rect)
    val color = edge.averageColor()
    val resized = if (horizontal) edge.resize(rect.width, 1) else edge.resize(1, rect.height)
    val bytes = resized.getBytes()
    edge.close()
    resized.close()

    return if (color != null && bytes.isNotEmpty()) EdgeSample(color, bytes)
    else null
}
