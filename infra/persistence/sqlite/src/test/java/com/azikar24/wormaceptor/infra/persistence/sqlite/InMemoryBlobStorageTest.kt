package com.azikar24.wormaceptor.infra.persistence.sqlite

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

class InMemoryBlobStorageTest {

    private val storage = InMemoryBlobStorage()

    @Nested
    inner class `saveBlob` {

        @Test
        fun `returns a non-blank blob ID`() = runTest {
            val id = storage.saveBlob(ByteArrayInputStream("hello".toByteArray()))

            id.shouldNotBeBlank()
        }

        @Test
        fun `stores the content for later retrieval`() = runTest {
            val content = "test content bytes"
            val id = storage.saveBlob(ByteArrayInputStream(content.toByteArray()))

            val result = storage.readBlob(id)
            result.shouldNotBeNull()
            String(result.readBytes()) shouldBe content
        }

        @Test
        fun `generates unique IDs for different blobs`() = runTest {
            val id1 = storage.saveBlob(ByteArrayInputStream("first".toByteArray()))
            val id2 = storage.saveBlob(ByteArrayInputStream("second".toByteArray()))

            (id1 != id2) shouldBe true
        }

        @Test
        fun `handles empty input stream`() = runTest {
            val id = storage.saveBlob(ByteArrayInputStream(ByteArray(0)))

            val result = storage.readBlob(id)
            result.shouldNotBeNull()
            result.readBytes().size shouldBe 0
        }

        @Test
        fun `preserves binary data exactly`() = runTest {
            val binary = byteArrayOf(0x00, 0x01, 0x7F, -1, -128, 127)
            val id = storage.saveBlob(ByteArrayInputStream(binary))

            val result = storage.readBlob(id)
            result.shouldNotBeNull()
            result.readBytes() shouldBe binary
        }
    }

    @Nested
    inner class `readBlob` {

        @Test
        fun `returns null for non-existent blob ID`() = runTest {
            val result = storage.readBlob("non-existent-id")

            result.shouldBeNull()
        }

        @Test
        fun `returns the stored content`() = runTest {
            val content = "stored data"
            val id = storage.saveBlob(ByteArrayInputStream(content.toByteArray()))

            val result = storage.readBlob(id)
            result.shouldNotBeNull()
            String(result.readBytes()) shouldBe content
        }
    }

    @Nested
    inner class `deleteBlob` {

        @Test
        fun `removes the blob so readBlob returns null`() = runTest {
            val id = storage.saveBlob(ByteArrayInputStream("to-delete".toByteArray()))

            storage.deleteBlob(id)

            storage.readBlob(id).shouldBeNull()
        }

        @Test
        fun `does not throw when deleting non-existent blob`() = runTest {
            storage.deleteBlob("does-not-exist")
        }

        @Test
        fun `does not affect other blobs`() = runTest {
            val id1 = storage.saveBlob(ByteArrayInputStream("keep".toByteArray()))
            val id2 = storage.saveBlob(ByteArrayInputStream("remove".toByteArray()))

            storage.deleteBlob(id2)

            storage.readBlob(id1).shouldNotBeNull()
            storage.readBlob(id2).shouldBeNull()
        }
    }
}
