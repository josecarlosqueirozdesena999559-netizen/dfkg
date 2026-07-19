import os

with open('app/src/main/java/com/example/ui/screens/FeedScreen.kt', 'r') as f:
    content = f.read()

# Removing the inline comments section
import re
# We look for "if (!isDetailMode && post.postComments.isNotEmpty()) {" and remove the whole block.
# That's probably around line 850. Let's just use string replacement if possible, or regex.

pattern = re.compile(r'if \(!isDetailMode && post\.postComments\.isNotEmpty\(\)\) \{.*?\n            \}', re.DOTALL)
content = re.sub(pattern, '', content)

with open('app/src/main/java/com/example/ui/screens/FeedScreen.kt', 'w') as f:
    f.write(content)
