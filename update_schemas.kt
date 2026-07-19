import java.io.File

fun main() {
    val file = File("backend/shared/database/Schemas.kt")
    var content = file.readText()
    
    // Add init blocks
    content = content.replace(
        "    override val primaryKey = PrimaryKey(id)\n}",
        "    override val primaryKey = PrimaryKey(id)\n    init {\n        index(false, authorId)\n        index(false, createdAt)\n    }\n}"
    ) // Needs to be targeted

}
