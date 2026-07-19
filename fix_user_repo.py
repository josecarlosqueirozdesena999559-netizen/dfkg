import os

with open('app/src/main/java/com/example/repository/UserProfileRepository.kt', 'r') as f:
    content = f.read()

content = content.replace("return UserProfile(", "return UserProfile(\n            id = dto.profile.userId,")

with open('app/src/main/java/com/example/repository/UserProfileRepository.kt', 'w') as f:
    f.write(content)
