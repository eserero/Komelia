
  Currently, the header area of the expanded immersive card is split into two parts:
   1. A Header Box containing the Thumbnail (on the left, with an offset top) and the Title/Authors (in a Column on the right).
   2. A Separate Grid Item below the Header Box that contains the Publisher Logo, aligned to the right.

  The Thumbnail's top is offset downwards by approximately 12dp relative to the Title because of extra padding.

  Current Diagram:
    1 [ Grid Item: Header Box ]
    2 +-------------------------------------------------------------+
    3 | (Padding: 20dp)                                             |
    4 |                                                             |
    5 |                   Book Title                                |
    6 |  (Thumbnail top)  (Authors/Year)                            |
    7 |  +-----------+                                              |
    8 |  |           |                                              |
    9 |  | Thumbnail |                                              |
   10 |  |           |                                              |
   11 |  |           |                                              |
   12 |  +-----------+                                              |
   13 +-------------------------------------------------------------+
   14
   15 [ Grid Item: Publisher ]
   16 +-------------------------------------------------------------+
   17 |                                          [Publisher Logo]   |
   18 +-------------------------------------------------------------+

  Goal State Explanation

  The goal is to unify these elements into a single cohesive header:
   1. Align Tops: The Thumbnail and the Title will start at the same vertical position.
   2. Unify Container: The Publisher Logo will move into the Header Box.
   3. Align Bottoms: The Publisher Logo will be positioned at the bottom-right of the Header Box, such that its bottom edge aligns with the bottom edge of the Thumbnail.
   4. Dynamic Sizing: The Publisher Logo's height will be exactly 25% of the Thumbnail's height to ensure it remains legible and proportional.

  Goal Diagram:
    1 [ Grid Item: Unified Header Box ]
    2 +-------------------------------------------------------------+
    3 | (Padding: 20dp)                                             |
    4 |                                                             |
    5 |  +-----------+    Book Title                                |
    6 |  |           |    (Authors/Year)                            |
    7 |  | Thumbnail |                                              |
    8 |  |           |                                              |
    9 |  |           |                                              |
   10 |  |           |                           [Publisher Logo]   |
   11 |  +-----------+                           (Aligned bottom)   |
   12 +-------------------------------------------------------------+

  