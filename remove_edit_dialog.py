import os
import re

with open('app/src/main/java/com/example/ui/screens/FeedScreen.kt', 'r') as f:
    content = f.read()

pattern = re.compile(r'                var showEditDialog by remember \{ mutableStateOf\(false\) \}\n                if \(showEditDialog\) \{\n                    EditPostDialog\([\s\S]*?\}\n', re.DOTALL)
content = re.sub(pattern, '', content)

with open('app/src/main/java/com/example/ui/screens/FeedScreen.kt', 'w') as f:
    f.write(content)
