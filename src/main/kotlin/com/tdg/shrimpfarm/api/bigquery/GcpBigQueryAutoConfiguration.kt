package com.tdg.shrimpfarm.api.bigquery

import com.google.api.gax.core.CredentialsProvider
import com.google.cloud.bigquery.BigQuery
import com.google.cloud.bigquery.BigQueryOptions
import com.google.cloud.spring.autoconfigure.bigquery.GcpBigQueryProperties
import com.google.cloud.spring.autoconfigure.core.GcpContextAutoConfiguration
import com.google.cloud.spring.core.DefaultCredentialsProvider
import com.google.cloud.spring.core.GcpProjectIdProvider
import com.google.cloud.spring.core.UserAgentHeaderProvider
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.cloud.gcp.bigquery.core.BigQueryTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.IOException

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(GcpContextAutoConfiguration::class)
@ConditionalOnProperty(value = ["spring.cloud.gcp.bigquery.enabled"], matchIfMissing = true)
@ConditionalOnClass(
    BigQuery::class,
    BigQueryTemplate::class
)
@EnableConfigurationProperties(GcpBigQueryProperties::class)
class GcpBigQueryAutoConfiguration internal constructor(
    gcpBigQueryProperties: GcpBigQueryProperties,
    projectIdProvider: GcpProjectIdProvider,
    credentialsProvider: CredentialsProvider?
) {
    private val projectId: String
    private val credentialsProvider: CredentialsProvider
    private val datasetName: String
    @Bean
    @ConditionalOnMissingBean
    @Throws(IOException::class)
    fun bigQuery(): BigQuery {
        val bigQueryOptions = BigQueryOptions.newBuilder()
            .setProjectId(projectId)
            .setCredentials(credentialsProvider.credentials)
            .setHeaderProvider(UserAgentHeaderProvider(GcpBigQueryAutoConfiguration::class.java))
            .build()
        return bigQueryOptions.service
    }

    @Bean
    @ConditionalOnMissingBean
    fun bigQueryTemplate(bigQuery: BigQuery?): BigQueryTemplate {
        return BigQueryTemplate(bigQuery, datasetName)
    }

    init {
        projectId =
            if (gcpBigQueryProperties.projectId != null) gcpBigQueryProperties.projectId else projectIdProvider.projectId
        this.credentialsProvider = if (gcpBigQueryProperties.credentials.hasKey()) DefaultCredentialsProvider(
            gcpBigQueryProperties
        ) else credentialsProvider!!
        datasetName = gcpBigQueryProperties.datasetName
    }
}