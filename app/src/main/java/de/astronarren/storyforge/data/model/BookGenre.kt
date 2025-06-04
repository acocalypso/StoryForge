package de.astronarren.storyforge.data.model

enum class BookGenre(val displayName: String) {
    FICTION("Fiction"),
    MYSTERY("Mystery"),
    THRILLER("Thriller"),
    ROMANCE("Romance"),
    FANTASY("Fantasy"),
    SCIENCE_FICTION("Science Fiction"),
    HORROR("Horror"),
    HISTORICAL_FICTION("Historical Fiction"),
    LITERARY_FICTION("Literary Fiction"),
    YOUNG_ADULT("Young Adult"),
    ADVENTURE("Adventure"),
    BIOGRAPHY("Biography"),
    MEMOIR("Memoir"),
    NON_FICTION("Non-Fiction"),
    SELF_HELP("Self-Help"),
    POETRY("Poetry"),
    DRAMA("Drama"),
    COMEDY("Comedy"),
    OTHER("Other");

    companion object {
        fun fromString(genre: String): BookGenre {
            return values().find { it.displayName.equals(genre, ignoreCase = true) } ?: OTHER
        }
        
        fun getAllGenres(): List<BookGenre> = values().toList()
    }
}

