package com.tdg.shrimpfarm.api.service

import com.google.api.gax.rpc.NotFoundException
import com.google.cloud.bigtable.admin.v2.BigtableTableAdminClient
import com.google.cloud.bigtable.admin.v2.BigtableTableAdminSettings
import com.google.cloud.bigtable.admin.v2.models.CreateTableRequest
import com.google.cloud.bigtable.data.v2.BigtableDataClient
import com.google.cloud.bigtable.data.v2.BigtableDataSettings
import com.google.cloud.bigtable.data.v2.models.Query
import com.google.cloud.bigtable.data.v2.models.Row
import com.google.cloud.bigtable.data.v2.models.RowCell
import com.google.cloud.bigtable.data.v2.models.RowMutation
import org.springframework.stereotype.Service

@Service
class FarmService {

    // Instantiates a client
    private val projectId = "tdg-tds-shrimpmaster-prod"
    //private val projectId = "true-336013"
    private val instanceId = "shrimpmaster-uat-mim"
    private val tableId = "farm2"
    private var dataClient: BigtableDataClient? = null
    private var adminClient: BigtableTableAdminClient? = null
    private val COLUMN_FAMILY = "cf1"
    private val COLUMN_QUALIFIER_GREETING = "greeting"
    private val COLUMN_QUALIFIER_NAME = "name"
    private val ROW_KEY_PREFIX = "rowKey"

    fun init() {
//        val scopes: MutableList<String> = ArrayList()
//
//        var credentials = GoogleCredentials.
//        fromStream(FileInputStream("/Users/vorrawut/TRUE/shrimp-farm-api/src/main/resources/true-bigtable.json"))
//        credentials.refreshIfExpired()
//        credentials = credentials.createScoped(scopes);
//        val token = credentials.accessToken

        // Create the client.
        // Please note that creating the client is a very expensive operation
        // and should only be done once and shared in an application.
        val settings = BigtableDataSettings.newBuilder().setProjectId(projectId).setInstanceId(instanceId).build()
        // Creates a bigtable data client.
        // Creates a bigtable data client.
        dataClient = BigtableDataClient.create(settings)

        // Creates the settings to configure a bigtable table admin client.

        // Creates the settings to configure a bigtable table admin client.
        val adminSettings = BigtableTableAdminSettings.newBuilder()
            .setProjectId(projectId)
            .setInstanceId(instanceId)
            .build()

        val dataClient = BigtableDataClient.create(projectId, instanceId)
        // Creates a bigtable table admin client.
        adminClient = BigtableTableAdminClient.create(adminSettings);

//        try {
//            // Query a table
//            val query = Query.create(tableId)
//                .range("a", "z")
//                .limit(26)
//            for (row in dataClient.readRows(query)) {
//                println(row.key)
//            }
//        } finally {
//            dataClient.close()
//        }
        createTable()
        writeToTable()
        readSingleRow()
    }

    /** Demonstrates how to create a table.  */
    fun createTable() {
        // [START bigtable_hw_create_table]
        // Checks if table exists, creates table if does not exist.
        if (!adminClient!!.exists(tableId)) {
            println("Creating table: $tableId")
            val createTableRequest: CreateTableRequest = CreateTableRequest.of(tableId).addFamily(COLUMN_FAMILY)
            adminClient!!.createTable(createTableRequest)
            System.out.printf("Table %s created successfully%n", tableId)
        }
        // [END bigtable_hw_create_table]
    }

    /** Demonstrates how to write some rows to a table.  */
    fun writeToTable() {
        // [START bigtable_hw_write_rows]
        try {
            println("\nWriting some greetings to the table")
            val names = arrayOf("World", "Bigtable", "Java")
            for (i in names.indices) {
                val greeting = "Hello " + names[i] + "!"
                val rowMutation = RowMutation.create(tableId, ROW_KEY_PREFIX + i)
                    .setCell(COLUMN_FAMILY, COLUMN_QUALIFIER_NAME, names[i])
                    .setCell(COLUMN_FAMILY, COLUMN_QUALIFIER_GREETING, greeting)
                dataClient!!.mutateRow(rowMutation)
                println(greeting)
            }
        } catch (e: NotFoundException) {
            System.err.println("Failed to write to non-existent table: " + e.message)
        }
        // [END bigtable_hw_write_rows]
    }

    /** Demonstrates how to read a single row from a table.  */
    fun readSingleRow(): Row? {
        // [START bigtable_hw_get_by_key]
        return try {
            println("\nReading a single row by row key")
            val row = dataClient!!.readRow(tableId, ROW_KEY_PREFIX + 0)
            println("Row: " + row.key.toStringUtf8())
            for (cell in row.cells) {
                System.out.printf(
                    "Family: %s    Qualifier: %s    Value: %s%n",
                    cell.family, cell.qualifier.toStringUtf8(), cell.value.toStringUtf8()
                )
            }
            row
        } catch (e: NotFoundException) {
            System.err.println("Failed to read from a non-existent table: " + e.message)
            null
        }
        // [END bigtable_hw_get_by_key]
    }

    /** Demonstrates how to access specific cells by family and qualifier.  */
    fun readSpecificCells(): kotlin.collections.List<RowCell>? {
        // [START bigtable_hw_get_by_key]
        return try {
            println("\nReading specific cells by family and qualifier")
            val row = dataClient!!.readRow(tableId, ROW_KEY_PREFIX + 0)
            println("Row: " + row.key.toStringUtf8())
            val cells = row.getCells(COLUMN_FAMILY, COLUMN_QUALIFIER_NAME)
            for (cell in cells) {
                System.out.printf(
                    "Family: %s    Qualifier: %s    Value: %s%n",
                    cell.family, cell.qualifier.toStringUtf8(), cell.value.toStringUtf8()
                )
            }
            cells
        } catch (e: NotFoundException) {
            System.err.println("Failed to read from a non-existent table: " + e.message)
            null
        }
        // [END bigtable_hw_get_by_key]
    }

    /** Demonstrates how to read an entire table.  */
    fun readTable(): kotlin.collections.List<Row>? {
        // [START bigtable_hw_scan_all]
        return try {
            println("\nReading the entire table")
            val query = Query.create(tableId)
            val rowStream = dataClient!!.readRows(query)
            val tableRows: MutableList<Row> = ArrayList()
            for (r in rowStream) {
                println("Row Key: " + r.key.toStringUtf8())
                tableRows.add(r)
                for (cell in r.cells) {
                    System.out.printf(
                        "Family: %s    Qualifier: %s    Value: %s%n",
                        cell.family, cell.qualifier.toStringUtf8(), cell.value.toStringUtf8()
                    )
                }
            }
            tableRows
        } catch (e: NotFoundException) {
            System.err.println("Failed to read a non-existent table: " + e.message)
            null
        }
        // [END bigtable_hw_scan_all]
    }

    /** Demonstrates how to delete a table.  */
    fun deleteTable() {
        // [START bigtable_hw_delete_table]
        println("\nDeleting table: $tableId")
        try {
            adminClient!!.deleteTable(tableId)
            System.out.printf("Table %s deleted successfully%n", tableId)
        } catch (e: NotFoundException) {
            System.err.println("Failed to delete a non-existent table: " + e.message)
        }
        // [END bigtable_hw_delete_table]
    }
}