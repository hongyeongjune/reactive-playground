package com.example.playground.coroutine

import kotlinx.coroutines.delay

class WebtoonService {
    private val episodeRepository = EpisodeRepository()
    private val thumbnailRepository = ThumbnailRepository()

    suspend fun findWebtoon(): DTO {
        println("1번 중단")
        val episode = episodeRepository.findEpisode()
        println("2번 중단")
        val thumbnail = thumbnailRepository.findThumbnail()
        return DTO(episode, thumbnail)
    }
}

data class DTO(
    val episode: Episode,
    val thumbnail: Thumbnail,
)

class Episode

class Thumbnail

class EpisodeRepository {
    suspend fun findEpisode(): Episode {
        delay(1000L)
        return Episode()
    }
}

class ThumbnailRepository {
    suspend fun findThumbnail(): Thumbnail {
        delay(1000L)
        return Thumbnail()
    }
}

suspend fun main() {
    WebtoonService().findWebtoon()
}