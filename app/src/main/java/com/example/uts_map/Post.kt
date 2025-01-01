data class Post(
    val postId: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrls: List<String> = emptyList(), // Tambahkan properti ini
    val timestamp: Long = 0
)
