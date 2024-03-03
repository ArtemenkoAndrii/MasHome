package com.mas.mobile.service.domain.message

import com.mas.mobile.domain.message.*
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QualifierServiceTest {
    private val mockMessageRuleRepository = mockk<QualifierRepository>(relaxed = true)

    private val testInstance = QualifierService(mockMessageRuleRepository)

    @BeforeAll
    fun setUp() {
        every { mockMessageRuleRepository.getSkipQualifiers() }
            .returns(listOf(SkipQualifier("confirmation"), SkipQualifier("code")))
        every { mockMessageRuleRepository.getCatchQualifiers() }
            .returns(listOf(CatchQualifier("paid"), CatchQualifier("payment")))
    }

    @Test
    fun `should recommend`() = assertTrue(
        testInstance.isRecommended("Payment of 4,10 EUR in aliexpress with your card ending in 8850 accepted.")
    )

    @Test
    fun `Not recommended because skip word`() = assertFalse(
        testInstance.isRecommended("Your confirmation code is 8429. Please enter this code within the next 10 minutes to complete your verification process. Do not share this code with anyone.")
    )

    @Test
    fun `Not recommended because no amount`() = assertFalse(
        testInstance.isRecommended("Payment in aliexpress with your card ending in 8850 accepted.")
    )

    @Test
    fun `Not recommended because too long`() = assertFalse(
        testInstance.isRecommended("""
            Alert: Your account XXXX-XXXX was charged ${'$'}5.75 on 04 Feb 2024 at Grind & Brew Cafe. Your new balance is ${'$'}1,024.33. This purchase includes a ${'$'}0.25 eco-contribution for our new biodegradable cups. Thank you for choosing sustainable options! For queries, call 1800-XXX-XXXX. Msg&Data rates may apply. To unsubscribe, reply STOP.
        """.trimIndent())
    )

    @Test
    fun `Not recommended because no currency`() = assertFalse(
        testInstance.isRecommended("Payment of 4,10 in aliexpress with your card ending in 8850 accepted.")
    )
}