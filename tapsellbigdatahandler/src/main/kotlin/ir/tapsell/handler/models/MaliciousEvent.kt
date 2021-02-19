package ir.tapsell.handler.models

import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.Table


@Table("MaliciousEvent")
data class MaliciousEvent(
        @PrimaryKey
        val requestId: String,
        val adId: String,
        val adTitle: String,
        val advertiserCost: Double,
        val appId: String,
        val appTitle: String,
        val impressionTime: Long,
        var count: Long)
