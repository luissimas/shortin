package io.github.luissimas.core.shorturl.usecases

import io.github.luissimas.core.shorturl.domain.ShortUrl
import io.github.luissimas.core.shorturl.ports.driven.ShortUrlRepository
import io.github.luissimas.core.shorturl.ports.driver.GetShortUrlService

class GetShortUrlUseCase(
    private val repository: ShortUrlRepository,
) : GetShortUrlService {
    override fun invoke(shortCode: String): ShortUrl? = repository.getByShortCode(shortCode)
}
