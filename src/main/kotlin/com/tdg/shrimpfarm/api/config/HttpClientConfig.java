package com.tdg.shrimpfarm.api.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;


@Getter
@Setter
@Slf4j
@Configuration
@ConfigurationProperties(prefix = "rest")
public class HttpClientConfig {

    private static final int DEFAULT_MAX_TOTAL_CONNECTIONS = 50;
    private static final int DEFAULT_MAX_PER_ROUTE = 10;
    private int keepAliveTimeMillis = 20000;
    private int closeIdleConnectionWaitTimeMillis = 30000;
    private boolean monitorIdle = false;

    public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory(CloseableHttpClient httpClient) {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setHttpClient(httpClient);
        return clientHttpRequestFactory;
    }

    public CloseableHttpClient closeableHttpClient(int requestTimeOutMillis, int connectionTimeOutMillis, int socketTimeoutMillis, HttpClientConnectionManager connectionManager) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(requestTimeOutMillis)
                .setConnectTimeout(connectionTimeOutMillis)
                .setSocketTimeout(socketTimeoutMillis).build();

        return HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .setConnectionManager(connectionManager)
                .setKeepAliveStrategy(connectionKeepAliveStrategy())
                .build();
    }

    @Bean
    @Scope("prototype")
    public PoolingHttpClientConnectionManager poolingConnectionManager() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
        builder.setProtocol("SSL");
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                .<ConnectionSocketFactory>create().register("https", sslConnectionSocketFactory)
                .register("http", new PlainConnectionSocketFactory())
                .build();

        PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        poolingConnectionManager.setMaxTotal(DEFAULT_MAX_TOTAL_CONNECTIONS);
        poolingConnectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_PER_ROUTE);
        return poolingConnectionManager;
    }

    @Bean
    public ConnectionKeepAliveStrategy connectionKeepAliveStrategy() {
        return (response, context) -> {
            HeaderElementIterator it = new BasicHeaderElementIterator
                    (response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                HeaderElement he = it.nextElement();
                String param = he.getName();
                String value = he.getValue();

                if (value != null && param.equalsIgnoreCase("timeout")) {
                    return Long.parseLong(value) * 1000;
                }
            }
            return keepAliveTimeMillis;
        };
    }

    @Bean
    @Scope("prototype")
    public Runnable idleConnectionMonitor(final PoolingHttpClientConnectionManager connectionManager) {
        return new Runnable() {
            @Override
            @Scheduled(fixedDelay = 10000)
            public void run() {
                try {
                    if (connectionManager != null) {
                        if (log.isTraceEnabled())
                            log.trace("run IdleConnectionMonitor - Closing expired and idle connections...");

                        if (connectionManager.getTotalStats().getLeased() >= ((connectionManager.getDefaultMaxPerRoute() * connectionManager.getRoutes().size()) * 0.8) && connectionManager.getRoutes().size() > 0 && monitorIdle)
                            log.info("idle connection monitor - route size: {}, max-connection: {}, usage-connection: {}, available-connection: {}", connectionManager.getRoutes().size(), connectionManager.getDefaultMaxPerRoute(), connectionManager.getTotalStats().getLeased(), connectionManager.getTotalStats().getAvailable());

                        connectionManager.closeExpiredConnections();
                        connectionManager.closeIdleConnections(closeIdleConnectionWaitTimeMillis, TimeUnit.MILLISECONDS);
                    } else {
                        if (log.isTraceEnabled())
                            log.trace("run IdleConnectionMonitor - Http Client Connection manager is not initialised");
                    }
                } catch (Exception e) {
                    log.error("run IdleConnectionMonitor - Exception occurred. msg={}, e={}", e.getMessage(), e);
                }
            }
        };
    }
}