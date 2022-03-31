import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.asserter

class CoreTest {

    @Test
    fun `test a special method`() {
        asserter.assertEquals("", testStringMethod(), "Yes")
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Test
    fun `test window`(){
        val scene = ImageComposeScene(
            width = 100,
            height = 200,
            content = {
                App()
            }
        )

        val image = scene.render(1)

        assertNotNull(image)
    }
}