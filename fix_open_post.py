import os

with open('app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt', 'r') as f:
    content = f.read()

old_open = """    fun openPostDetail(post: FeedPost) {
        _selectedPost.value = post
        _currentScreen.value = ScreenType.POST_DETAIL
    }"""

new_open = """    fun openPostDetail(post: FeedPost) {
        _selectedPost.value = post
        loadComments(post.id)
        _currentScreen.value = ScreenType.POST_DETAIL
    }"""

content = content.replace(old_open, new_open)

with open('app/src/main/java/com/example/viewmodel/DecisaoViewModel.kt', 'w') as f:
    f.write(content)
