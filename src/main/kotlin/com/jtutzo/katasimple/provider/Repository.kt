package com.jtutzo.katasimple.provider

import com.jtutzo.katasimple.domaine.*
import org.jooq.DSLContext
import org.jooq.example.db.h2.Tables.USER_PROJECTION
import org.jooq.example.db.h2.tables.records.UserProjectionRecord
import org.springframework.stereotype.Repository
import java.util.*
import javax.inject.Inject

@Repository
class JooqUserWriteRepository @Inject constructor(private val dsl: DSLContext) : UserWriteRepository {

    override fun create(evt: UserCreatedEvent) {
        dsl.insertInto(USER_PROJECTION)
                .columns(USER_PROJECTION.ID,
                        USER_PROJECTION.USERNAME,
                        USER_PROJECTION.EMAIL,
                        USER_PROJECTION.TEAM_ID)
                .values(evt.id,
                        evt.username,
                        evt.email,
                        evt.teamId)
                .execute()
    }

    override fun update(evt: UserUpdatedEvent) {
        dsl.update(USER_PROJECTION)
                .set(USER_PROJECTION.USERNAME, evt.username)
                .set(USER_PROJECTION.EMAIL, evt.email)
                .set(USER_PROJECTION.TEAM_ID, evt.teamId)
                .execute()
    }

    override fun deleteAll() {
        dsl.truncate(USER_PROJECTION).execute()
    }

}

@Repository
class JooqUserReadRepository @Inject constructor(private val dsl: DSLContext) : UserReadRepository {

    override fun findAll(): Set<UserProjection> = dsl.selectFrom(USER_PROJECTION).fetch(::map).toSet()

    override fun findById(id: UUID): Optional<UserProjection> = dsl
            .selectFrom(USER_PROJECTION)
            .where(USER_PROJECTION.ID.eq(id))
            .fetchOptional(::map)

    override fun findByUsername(username: String): Optional<UserProjection> = dsl
            .selectFrom(USER_PROJECTION)
            .where(USER_PROJECTION.USERNAME.eq(username))
            .fetchOptional(::map)

    override fun findByEmail(email: String): Optional<UserProjection> = dsl
            .selectFrom(USER_PROJECTION)
            .where(USER_PROJECTION.EMAIL.eq(email))
            .fetchOptional(::map)

    private fun map(record: UserProjectionRecord) = UserProjection(record[USER_PROJECTION.ID],
            record[USER_PROJECTION.USERNAME],
            record[USER_PROJECTION.EMAIL],
            record[USER_PROJECTION.TEAM_ID])

}