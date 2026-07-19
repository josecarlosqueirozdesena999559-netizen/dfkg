import os

with open('app/src/main/java/com/example/ui/screens/PostDetailScreen.kt', 'r') as f:
    content = f.read()

# Update signature
old_sig = """fun PostDetailScreen(
    post: FeedPost,"""
new_sig = """fun PostDetailScreen(
    post: FeedPost,
    comments: List<Comment>,"""

content = content.replace(old_sig, new_sig)

# Update comments mapping
content = content.replace("post.postComments", "comments")

with open('app/src/main/java/com/example/ui/screens/PostDetailScreen.kt', 'w') as f:
    f.write(content)
