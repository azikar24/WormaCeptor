package com.azikar24.wormaceptor.feature.logs.vm

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LogQueryTest {

    @Nested
    inner class `empty query` {

        @Test
        fun `blank string matches everything`() {
            val q = LogQuery.parse("")
            q.matches("AnyTag", "anything") shouldBe true
        }

        @Test
        fun `whitespace-only string matches everything`() {
            val q = LogQuery.parse("   \t  ")
            q.matches("AnyTag", "anything") shouldBe true
        }
    }

    @Nested
    inner class `bare tokens` {

        @Test
        fun `single token matches when tag contains it`() {
            val q = LogQuery.parse("net")
            q.matches("Network", "irrelevant") shouldBe true
        }

        @Test
        fun `single token matches when message contains it`() {
            val q = LogQuery.parse("crash")
            q.matches("Foo", "Detected crash on startup") shouldBe true
        }

        @Test
        fun `single token does not match when neither contains it`() {
            val q = LogQuery.parse("xyz")
            q.matches("Foo", "Bar") shouldBe false
        }

        @Test
        fun `bare match is case-insensitive`() {
            val q = LogQuery.parse("NETWORK")
            q.matches("network", "lower") shouldBe true
        }

        @Test
        fun `multiple bare tokens are AND-ed`() {
            val q = LogQuery.parse("net foo")
            q.matches("Network", "foo bar") shouldBe true
            q.matches("Network", "no second token") shouldBe false
            q.matches("UnrelatedTag", "foo only") shouldBe false
        }
    }

    @Nested
    inner class `tag clause` {

        @Test
        fun `tag include matches only tag, not message`() {
            val q = LogQuery.parse("tag:View")
            q.matches("ViewRootImpl", "irrelevant") shouldBe true
            q.matches("Foo", "View is mentioned in the message but not the tag") shouldBe false
        }

        @Test
        fun `tag exclude hides matching tag`() {
            val q = LogQuery.parse("-tag:ViewRootImpl")
            q.matches("ViewRootImpl", "anything") shouldBe false
            q.matches("MyApp", "anything") shouldBe true
        }

        @Test
        fun `tag exclude is substring-based`() {
            val q = LogQuery.parse("-tag:View")
            q.matches("ViewRootImpl", "x") shouldBe false
            q.matches("OverviewView", "x") shouldBe false
            q.matches("Other", "x") shouldBe true
        }
    }

    @Nested
    inner class `message clause` {

        @Test
        fun `message include matches only message, not tag`() {
            val q = LogQuery.parse("message:setRequestedFrameRate")
            q.matches("ViewRootImpl", "setRequestedFrameRate frameRate=NaN") shouldBe true
            q.matches("setRequestedFrameRateTag", "unrelated") shouldBe false
        }

        @Test
        fun `message exclude hides matching message`() {
            val q = LogQuery.parse("-message:setRequestedFrameRate")
            q.matches("VRI", "setRequestedFrameRate frameRate=-4.0") shouldBe false
            q.matches("VRI", "anything else") shouldBe true
        }
    }

    @Nested
    inner class `composite queries` {

        @Test
        fun `the user's exact query hides Compose frame-rate noise`() {
            val q = LogQuery.parse("-tag:ViewRootImpl -message:setRequestedFrameRate -tag:VRI")

            // Should hide all three flavours of the noise
            q.matches("ViewRootImpl", "setRequestedFrameRate frameRate=NaN") shouldBe false
            q.matches("VRI", "setRequestedFrameRate frameRate=-4.0") shouldBe false
            q.matches("OtherTag", "setRequestedFrameRate frameRate=120.0") shouldBe false

            // Should let real app logs through
            q.matches("MyAppNetwork", "Request started") shouldBe true
        }

        @Test
        fun `mixing include and exclude AND-s correctly`() {
            val q = LogQuery.parse("tag:Network -message:cache")
            q.matches("Network", "Request started") shouldBe true
            q.matches("Network", "served from cache") shouldBe false
            q.matches("Other", "Request started") shouldBe false
        }
    }

    @Nested
    inner class `malformed input` {

        @Test
        fun `bare dash is ignored`() {
            val q = LogQuery.parse("-")
            q.matches("Foo", "Bar") shouldBe true
        }

        @Test
        fun `bare prefix without needle is ignored`() {
            LogQuery.parse("tag:").matches("Foo", "Bar") shouldBe true
            LogQuery.parse("-message:").matches("Foo", "Bar") shouldBe true
        }

        @Test
        fun `prefix is matched case-insensitively`() {
            val q = LogQuery.parse("TAG:network")
            q.matches("Network", "x") shouldBe true
            q.matches("Other", "network in message") shouldBe false
        }
    }
}
