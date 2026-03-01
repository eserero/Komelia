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

suspend fun KomeliaImage.getEdgeSampling(vertical: Boolean): EdgeSampling? {
    return if (vertical) {
        val top = extractArea(ImageRect(0, 0, width, 10.coerceAtMost(height)))
        val topColor = top.averageColor()
        val topResized = top.resize(1, 1)
        val topBytes = topResized.getBytes()
        top.close()
        topResized.close()

        val bottom = extractArea(ImageRect(0, (height - 10).coerceAtLeast(0), width, height))
        val bottomColor = bottom.averageColor()
        val bottomResized = bottom.resize(1, 1)
        val bottomBytes = bottomResized.getBytes()
        bottom.close()
        bottomResized.close()

        if (topColor != null && topBytes.isNotEmpty() && bottomColor != null && bottomBytes.isNotEmpty()) {
            EdgeSampling(
                vertical = true,
                first = EdgeSample(topColor, topBytes),
                second = EdgeSample(bottomColor, bottomBytes)
            )
        } else null
    } else {
        val left = extractArea(ImageRect(0, 0, 10.coerceAtMost(width), height))
        val leftColor = left.averageColor()
        val leftResized = left.resize(1, 1)
        val leftBytes = leftResized.getBytes()
        left.close()
        leftResized.close()

        val right = extractArea(ImageRect((width - 10).coerceAtLeast(0), 0, width, height))
        val rightColor = right.averageColor()
        val rightResized = right.resize(1, 1)
        val rightBytes = rightResized.getBytes()
        right.close()
        rightResized.close()

        if (leftColor != null && leftBytes.isNotEmpty() && rightColor != null && rightBytes.isNotEmpty()) {
            EdgeSampling(
                vertical = false,
                first = EdgeSample(leftColor, leftBytes),
                second = EdgeSample(rightColor, rightBytes)
            )
        } else null
    }
}
