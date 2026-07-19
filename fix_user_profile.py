import os

with open('app/src/main/java/com/example/model/Models.kt', 'r') as f:
    content = f.read()

content = content.replace("data class UserProfile(", "data class UserProfile(\n    val id: String = \"\",")

with open('app/src/main/java/com/example/model/Models.kt', 'w') as f:
    f.write(content)

