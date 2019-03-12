package com.jtutzo.katasimple.provider

import org.axonframework.modelling.saga.AssociationValue
import org.axonframework.modelling.saga.repository.jdbc.SagaSqlSchema
import org.axonframework.serialization.SerializedObject
import org.axonframework.serialization.SimpleSerializedObject
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class DefaultSagaSchema(
        var associationValueEntryTable: String = "associationValueEntry",
        var sagaEntryTable: String = "sagaEntry",
        var idColumn: String = "id",
        var associationKeyColumn: String = "associationKey",
        var associationValueColumn: String = "associationValue",
        var sagaIdColumn: String = "sagaId",
        var sagaTypeColumn: String = "sagaType",
        var revisionColumn: String = "revision",
        var serializedSagaColumn: String = "serializedSaga"
)

class SagaSqlSchemaImpl(private val sagaSchema: DefaultSagaSchema) : SagaSqlSchema {

    private val loadSagaSql = "SELECT ${sagaSchema.serializedSagaColumn}, ${sagaSchema.sagaTypeColumn}, ${sagaSchema.revisionColumn} FROM ${sagaSchema.sagaEntryTable} WHERE ${sagaSchema.sagaIdColumn} = ?"
    private val removeAssocValueSql = "DELETE FROM ${sagaSchema.associationValueEntryTable} WHERE ${sagaSchema.associationKeyColumn} = ? AND ${sagaSchema.associationValueColumn} = ? AND ${sagaSchema.sagaTypeColumn} = ? AND ${sagaSchema.sagaIdColumn} = ?"
    private val storeAssocValueSql = "INSERT INTO ${this.sagaSchema.associationValueEntryTable} (${sagaSchema.associationKeyColumn}, ${sagaSchema.associationValueColumn}, ${sagaSchema.sagaTypeColumn}, ${sagaSchema.sagaIdColumn}) VALUES(?, ?, ?, ?)"
    private val findAssocSagaIdentifiersSql = "SELECT ${sagaSchema.sagaIdColumn} FROM ${this.sagaSchema.associationValueEntryTable} WHERE ${sagaSchema.associationKeyColumn} = ? AND ${sagaSchema.associationValueColumn} = ? AND ${sagaSchema.sagaTypeColumn} = ?"
    private val findAssociationsSql = "SELECT ${sagaSchema.associationKeyColumn}, ${sagaSchema.associationValueColumn} FROM ${this.sagaSchema.associationValueEntryTable} WHERE ${sagaSchema.sagaIdColumn} = ? AND ${sagaSchema.sagaTypeColumn} = ?"
    private val deleteSagaEntrySql = "DELETE FROM ${sagaSchema.sagaEntryTable} WHERE ${sagaSchema.sagaIdColumn} = ?"
    private val deleteAssociationEntriesSql = "DELETE FROM ${sagaSchema.associationValueEntryTable} WHERE ${sagaSchema.sagaIdColumn} = ?"
    private val updateSagaSql = "UPDATE ${sagaSchema.sagaEntryTable} SET ${sagaSchema.serializedSagaColumn} = ?, ${sagaSchema.revisionColumn} = ? WHERE ${sagaSchema.sagaIdColumn} = ?"
    private val storeSagSsql = "INSERT INTO ${sagaSchema.sagaEntryTable} (${sagaSchema.sagaIdColumn}, ${sagaSchema.revisionColumn}, ${sagaSchema.sagaTypeColumn}, ${sagaSchema.serializedSagaColumn}) VALUES(?,?,?,?)"
    private val createTableAssocValueEntrySql = """create table ${this.sagaSchema.associationValueEntryTable} (
        ${sagaSchema.idColumn} int not null AUTO_INCREMENT,
        ${sagaSchema.associationKeyColumn} varchar(255),
        ${sagaSchema.associationValueColumn} varchar(255),
        ${sagaSchema.sagaIdColumn} varchar(255),
        ${sagaSchema.sagaTypeColumn} varchar(255),
        primary key (${sagaSchema.idColumn})
    );"""
    private val createTableSagaEntrySql = """create table ${this.sagaSchema.sagaEntryTable} (
        ${sagaSchema.sagaIdColumn} varchar(255) not null,
        ${sagaSchema.revisionColumn} varchar(255),
        ${sagaSchema.sagaTypeColumn} varchar(255),
        ${sagaSchema.serializedSagaColumn} blob,
        primary key (${sagaSchema.sagaIdColumn})
    );"""

    override fun sql_loadSaga(connection: Connection, sagaId: String): PreparedStatement = connection
            .prepareStatement(loadSagaSql).apply { this.setString(1, sagaId) }

    override fun sql_removeAssocValue(connection: Connection, key: String, value: String, sagaType: String, sagaIdentifier: String): PreparedStatement = connection
            .prepareStatement(removeAssocValueSql).apply {
                setString(1, key)
                setString(2, value)
                setString(3, sagaType)
                setString(4, sagaIdentifier)
            }

    override fun sql_storeAssocValue(connection: Connection, key: String, value: String, sagaType: String, sagaIdentifier: String): PreparedStatement = connection
            .prepareStatement(storeAssocValueSql).apply {
                setString(1, key)
                setString(2, value)
                setString(3, sagaType)
                setString(4, sagaIdentifier)
            }

    override fun sql_findAssocSagaIdentifiers(connection: Connection, key: String, value: String, sagaType: String): PreparedStatement = connection
            .prepareStatement(findAssocSagaIdentifiersSql).apply {
                setString(1, key)
                setString(2, value)
                setString(3, sagaType)
            }

    override fun sql_findAssociations(connection: Connection, sagaIdentifier: String, sagaType: String): PreparedStatement = connection
            .prepareStatement(findAssociationsSql).apply {
                setString(1, sagaIdentifier)
                setString(2, sagaType)
            }

    override fun readToken(resultSet: ResultSet): String = ""

    override fun readAssociationValues(resultSet: ResultSet): Set<AssociationValue> {
        val associationValues = mutableSetOf<AssociationValue>()
        while (resultSet.next()) {
            associationValues.add(AssociationValue(resultSet.getString(1), resultSet.getString(2)))
        }
        return associationValues
    }

    override fun sql_deleteSagaEntry(connection: Connection, sagaIdentifier: String): PreparedStatement = connection
            .prepareStatement(deleteSagaEntrySql).apply { setString(1, sagaIdentifier) }

    override fun sql_deleteAssociationEntries(connection: Connection, sagaIdentifier: String): PreparedStatement = connection
            .prepareStatement(deleteAssociationEntriesSql).apply { setString(1, sagaIdentifier) }

    override fun sql_updateSaga(connection: Connection, sagaIdentifier: String, serializedSaga: ByteArray, sagaType: String, revision: String): PreparedStatement = connection
            .prepareStatement(updateSagaSql).apply {
                setBytes(1, serializedSaga)
                setString(2, revision)
                setString(3, sagaIdentifier)
            }

    override fun sql_storeSaga(connection: Connection, sagaIdentifier: String, revision: String, sagaType: String, serializedSaga: ByteArray): PreparedStatement = connection
            .prepareStatement(storeSagSsql).apply {
                setString(1, sagaIdentifier)
                setString(2, revision)
                setString(3, sagaType)
                setBytes(4, serializedSaga)
            }

    override fun sql_createTableAssocValueEntry(conn: Connection): PreparedStatement = conn.prepareStatement(createTableAssocValueEntrySql)

    override fun sql_createTableSagaEntry(conn: Connection): PreparedStatement = conn.prepareStatement(createTableSagaEntrySql)

    override fun readSerializedSaga(resultSet: ResultSet): SerializedObject<ByteArray> = SimpleSerializedObject(
            resultSet.getBytes(1),
            ByteArray::class.java,
            resultSet.getString(2),
            resultSet.getString(3))

}
