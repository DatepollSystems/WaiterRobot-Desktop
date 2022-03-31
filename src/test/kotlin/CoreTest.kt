import kotlin.test.Test
import kotlin.test.asserter

class CoreTest {

    @Test
    fun `test a special method`() {
        asserter.assertEquals("", testStringMethod(), "Yes")
    }
}