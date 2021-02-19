package ir.tapsell.handler.repos

import ir.tapsell.handler.models.MaliciousEvent
import org.springframework.data.cassandra.repository.CassandraRepository
import org.springframework.stereotype.Repository

@Repository
interface MaliciousEventRepository : CassandraRepository<MaliciousEvent, String> {
}