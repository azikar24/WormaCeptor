package com.azikar24.wormaceptor.feature.database.data

import com.azikar24.wormaceptor.domain.entities.ColumnInfo
import com.azikar24.wormaceptor.domain.entities.DatabaseInfo
import com.azikar24.wormaceptor.domain.entities.QueryResult
import com.azikar24.wormaceptor.domain.entities.TableInfo
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DatabaseRepositoryImplTest {

    private val sampleDatabases = listOf(
        DatabaseInfo("app.db", "/data/data/com.example/databases/app.db", 1024L, 5),
        DatabaseInfo("cache.db", "/data/data/com.example/databases/cache.db", 512L, 2),
    )

    private val sampleTables = listOf(
        TableInfo("users", 100L, 5),
        TableInfo("posts", 200L, 8),
    )

    private val sampleSchema = listOf(
        ColumnInfo("id", "INTEGER", isPrimaryKey = true, isNullable = false),
        ColumnInfo("name", "TEXT", isPrimaryKey = false, isNullable = true),
    )

    private val sampleQueryResult = QueryResult(
        columns = listOf("id", "name"),
        rows = listOf(listOf(1L, "Alice"), listOf(2L, "Bob")),
        rowCount = 2,
    )

    private val dataSource = mockk<DatabaseDataSource>(relaxed = true) {
        every { findDatabases() } returns sampleDatabases
        every { getTables(any()) } returns sampleTables
        every { getTableSchema(any(), any()) } returns sampleSchema
        every { queryTable(any(), any(), any(), any()) } returns sampleQueryResult
        every { executeQuery(any(), any()) } returns sampleQueryResult
    }

    private lateinit var repository: DatabaseRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = DatabaseRepositoryImpl(dataSource)
    }

    @Nested
    inner class `getDatabases` {

        @Test
        fun `returns databases from data source`() {
            val databases = repository.getDatabases()

            databases shouldHaveSize 2
            databases.first().name shouldBe "app.db"
            databases.last().name shouldBe "cache.db"
            verify { dataSource.findDatabases() }
        }

        @Test
        fun `caches database paths for subsequent calls`() {
            repository.getDatabases()

            repository.getTables("app.db")

            verify {
                dataSource.getTables("/data/data/com.example/databases/app.db")
            }
        }
    }

    @Nested
    inner class `getTables` {

        @Test
        fun `returns tables for known database`() {
            repository.getDatabases()

            val tables = repository.getTables("app.db")

            tables shouldHaveSize 2
            tables.first().name shouldBe "users"
            verify { dataSource.getTables("/data/data/com.example/databases/app.db") }
        }

        @Test
        fun `returns empty list for unknown database`() {
            val tables = repository.getTables("unknown.db")

            tables.shouldBeEmpty()
        }

        @Test
        fun `does not call data source for unknown database`() {
            repository.getTables("unknown.db")

            verify(exactly = 0) { dataSource.getTables(any()) }
        }
    }

    @Nested
    inner class `getTableSchema` {

        @Test
        fun `returns schema for known database and table`() {
            repository.getDatabases()

            val schema = repository.getTableSchema("app.db", "users")

            schema shouldHaveSize 2
            schema.first().name shouldBe "id"
            schema.first().isPrimaryKey shouldBe true
            verify {
                dataSource.getTableSchema(
                    "/data/data/com.example/databases/app.db",
                    "users",
                )
            }
        }

        @Test
        fun `returns empty list for unknown database`() {
            val schema = repository.getTableSchema("unknown.db", "users")

            schema.shouldBeEmpty()
        }
    }

    @Nested
    inner class `queryTable` {

        @Test
        fun `delegates to data source with correct path`() {
            repository.getDatabases()

            val result = repository.queryTable("app.db", "users", 100, 0)

            result shouldBe sampleQueryResult
            verify {
                dataSource.queryTable(
                    "/data/data/com.example/databases/app.db",
                    "users",
                    100,
                    0,
                )
            }
        }

        @Test
        fun `returns error result for unknown database`() {
            val result = repository.queryTable("unknown.db", "users", 100, 0)

            result.error shouldBe "Database not found"
            result.columns.shouldBeEmpty()
            result.rows.shouldBeEmpty()
            result.rowCount shouldBe 0
        }

        @Test
        fun `passes limit and offset to data source`() {
            repository.getDatabases()

            repository.queryTable("cache.db", "posts", 50, 200)

            verify {
                dataSource.queryTable(
                    "/data/data/com.example/databases/cache.db",
                    "posts",
                    50,
                    200,
                )
            }
        }
    }

    @Nested
    inner class `executeQuery` {

        @Test
        fun `delegates to data source with correct path`() {
            repository.getDatabases()

            val result = repository.executeQuery("app.db", "SELECT * FROM users")

            result shouldBe sampleQueryResult
            verify {
                dataSource.executeQuery(
                    "/data/data/com.example/databases/app.db",
                    "SELECT * FROM users",
                )
            }
        }

        @Test
        fun `returns error result for unknown database`() {
            val result = repository.executeQuery("unknown.db", "SELECT 1")

            result.error shouldBe "Database not found"
            result.columns.shouldBeEmpty()
            result.rows.shouldBeEmpty()
        }
    }

    @Nested
    inner class `path caching` {

        @Test
        fun `uses cached paths after getDatabases call`() {
            repository.getDatabases()

            repository.getTables("app.db")
            repository.getTableSchema("app.db", "users")
            repository.queryTable("app.db", "users", 10, 0)
            repository.executeQuery("app.db", "SELECT 1")

            val expectedPath = "/data/data/com.example/databases/app.db"
            verify { dataSource.getTables(expectedPath) }
            verify { dataSource.getTableSchema(expectedPath, "users") }
            verify { dataSource.queryTable(expectedPath, "users", 10, 0) }
            verify { dataSource.executeQuery(expectedPath, "SELECT 1") }
        }

        @Test
        fun `refreshes paths on subsequent getDatabases calls`() {
            repository.getDatabases()

            val updatedDatabases = listOf(
                DatabaseInfo(
                    "app.db",
                    "/new/path/app.db",
                    2048L,
                    10,
                ),
            )
            every { dataSource.findDatabases() } returns updatedDatabases
            repository.getDatabases()

            repository.getTables("app.db")

            verify { dataSource.getTables("/new/path/app.db") }
        }
    }
}
