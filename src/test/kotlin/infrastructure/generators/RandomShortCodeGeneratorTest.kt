package infrastructure.generators

import io.github.luissimas.infrastructure.generators.RandomShortCodeGenerator
import io.kotest.core.spec.style.FunSpec
import io.kotest.property.Arb
import io.kotest.property.arbitrary.positiveInt
import io.kotest.property.forAll
import java.net.URLEncoder
import java.nio.charset.Charset

class RandomShortCodeGeneratorTest :
    FunSpec({
        val generator = RandomShortCodeGenerator()

        test("Created short code is URL safe") {
            forAll { _: Int ->
                val shortCode = generator.generate()
                val encoded = URLEncoder.encode(shortCode, Charset.defaultCharset())

                shortCode == encoded
            }
        }

        test("Created short code has the specified length") {
            forAll(Arb.Companion.positiveInt(100)) { codeLength: Int ->
                val generator = RandomShortCodeGenerator(codeLength)
                val shortCode = generator.generate()

                shortCode.length == codeLength
            }
        }
    })
