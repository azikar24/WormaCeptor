package com.azikar24.wormaceptor.feature.logs.vm

internal data class LogQuery(private val clauses: List<Clause>) {

    fun matches(
        tag: String,
        message: String,
    ): Boolean = clauses.all { it.matches(tag, message) }

    sealed interface Clause {
        fun matches(
            tag: String,
            message: String,
        ): Boolean
    }

    private data class TagInclude(val needle: String) : Clause {
        override fun matches(
            tag: String,
            message: String,
        ): Boolean = tag.contains(needle, ignoreCase = true)
    }

    private data class TagExclude(val needle: String) : Clause {
        override fun matches(
            tag: String,
            message: String,
        ): Boolean = !tag.contains(needle, ignoreCase = true)
    }

    private data class MessageInclude(val needle: String) : Clause {
        override fun matches(
            tag: String,
            message: String,
        ): Boolean = message.contains(needle, ignoreCase = true)
    }

    private data class MessageExclude(val needle: String) : Clause {
        override fun matches(
            tag: String,
            message: String,
        ): Boolean = !message.contains(needle, ignoreCase = true)
    }

    private data class AnyInclude(val needle: String) : Clause {
        override fun matches(
            tag: String,
            message: String,
        ): Boolean = tag.contains(needle, ignoreCase = true) || message.contains(needle, ignoreCase = true)
    }

    private data class AnyExclude(val needle: String) : Clause {
        override fun matches(
            tag: String,
            message: String,
        ): Boolean = !tag.contains(needle, ignoreCase = true) && !message.contains(needle, ignoreCase = true)
    }

    companion object {
        val EMPTY = LogQuery(emptyList())

        private const val TAG_PREFIX = "tag:"
        private const val MESSAGE_PREFIX = "message:"
        private val WHITESPACE = Regex("\\s+")

        fun parse(raw: String): LogQuery {
            val tokens = tokenize(raw)
            if (tokens.isEmpty()) return EMPTY
            return LogQuery(tokens.mapNotNull(::toClause))
        }

        private fun tokenize(raw: String): List<String> = raw.trim().split(WHITESPACE).filter { it.isNotBlank() }

        @Suppress("ReturnCount")
        private fun toClause(token: String): Clause? {
            val negated = token.startsWith('-')
            val body = if (negated) token.drop(1) else token
            if (body.isEmpty()) return null

            val lower = body.lowercase()
            if (lower.startsWith(TAG_PREFIX)) {
                val needle = body.substring(TAG_PREFIX.length)
                if (needle.isEmpty()) return null
                return if (negated) TagExclude(needle) else TagInclude(needle)
            }
            if (lower.startsWith(MESSAGE_PREFIX)) {
                val needle = body.substring(MESSAGE_PREFIX.length)
                if (needle.isEmpty()) return null
                return if (negated) MessageExclude(needle) else MessageInclude(needle)
            }
            return if (negated) AnyExclude(body) else AnyInclude(body)
        }
    }
}
