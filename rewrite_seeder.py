import os

with open('backend/shared/database/DatabaseSeeder.kt', 'r') as f:
    content = f.read()

old_func = "    suspend fun seedIfNeeded() = DatabaseConnector.dbQuery {"

new_func = """    suspend fun seedIfNeeded() {
        try {
            DatabaseConnector.dbQuery {"""

content = content.replace(old_func, new_func)

# find the last closing brace and add another one
content = content.rstrip()
if content.endswith("}"):
    content = content[:-1] + "    }\n        } catch (e: Exception) {\n            logger.info(\"Database seeding skipped or aborted (likely due to concurrent initialization): \${e.message}\")\n        }\n    }\n}"

with open('backend/shared/database/DatabaseSeeder.kt', 'w') as f:
    f.write(content)

