package ir.tapsell.handler.repos

import ir.tapsell.handler.models.AdEvent
import org.springframework.data.cassandra.repository.CassandraRepository
import org.springframework.stereotype.Repository


@Repository
interface AdEventRepository : CassandraRepository<AdEvent, String> {
}