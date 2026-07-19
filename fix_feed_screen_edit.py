import os

with open('app/src/main/java/com/example/ui/screens/FeedScreen.kt', 'r') as f:
    content = f.read()

import re
# Remove the EditPostDialog entirely by replacing lines
lines = content.split('\n')
new_lines = []
skip = False
for line in lines:
    if "var showEditDialog by remember { mutableStateOf(false) }" in line:
        continue
    if "if (showEditDialog) {" in line:
        skip = True
        continue
    if skip and "}" in line and "EditPostDialog" not in line and "initialContent" not in line and "onDismiss" not in line and "onSubmit" not in line:
        skip = False
        continue
    if not skip:
        new_lines.append(line)

with open('app/src/main/java/com/example/ui/screens/FeedScreen.kt', 'w') as f:
    f.write('\n'.join(new_lines))

