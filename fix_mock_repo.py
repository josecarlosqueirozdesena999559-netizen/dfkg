import os

with open('app/src/main/java/com/example/repository/MockPostRepository.kt', 'r') as f:
    content = f.read()

# Just put `id = ""` as the first param
content = content.replace('UserProfile("Ana Clara"', 'UserProfile("", "Ana Clara"')
content = content.replace('UserProfile("Gabriel Ferreira"', 'UserProfile("", "Gabriel Ferreira"')
content = content.replace('UserProfile("Juliana Costa"', 'UserProfile("", "Juliana Costa"')
content = content.replace('UserProfile("Lucas Martins"', 'UserProfile("", "Lucas Martins"')
content = content.replace('UserProfile("Beatriz Santos"', 'UserProfile("", "Beatriz Santos"')

with open('app/src/main/java/com/example/repository/MockPostRepository.kt', 'w') as f:
    f.write(content)

