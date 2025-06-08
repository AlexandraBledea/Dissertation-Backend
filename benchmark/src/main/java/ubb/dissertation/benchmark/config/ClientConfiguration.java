package ubb.dissertation.benchmark.config;

import feign.Retryer;
import feign.httpclient.ApacheHttpClient;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.openfeign.support.FeignHttpClientProperties;
import org.springframework.context.annotation.Bean;

import java.io.IOException;


@Slf4j
public class ClientConfiguration {

    @Value("${client.config.retry.minPeriod}")
    private Long minPeriod;

    @Value("${client.config.retry.maxPeriod}")
    private Long maxPeriod;

    @Value("${client.config.retry.maxAttempts}")
    private int maxAttempts;

    private CloseableHttpClient httpClient;



    @Bean
    public Retryer retryer() {
        log.info("Retry for feign client is set to minPeriod={}, maxPeriod={} and maxAttempts={}", minPeriod, maxPeriod, maxAttempts);
        return new Retryer.Default(minPeriod, maxPeriod, maxAttempts);
    }

    @Bean
    public feign.Client cLient(FeignHttpClientProperties httpClientProperties) {
        var httpClientBuilder = HttpClientBuilder.create()
                .setDefaultRequestConfig(getDefaultRequestConfig(httpClientProperties))
                .setConnectionTimeToLive(httpClientProperties.getTimeToLive(), httpClientProperties.getTimeToLiveUnit())
                .evictExpiredConnections()
                .setMaxConnPerRoute(httpClientProperties.getMaxConnectionsPerRoute())
                .setMaxConnTotal(httpClientProperties.getMaxConnections());

        if (httpClientProperties.isDisableSslValidation()) {
            disableSssl(httpClientBuilder);
        }

        this.httpClient = httpClientBuilder.build();
        log.info("Feign client configured");
        return new ApacheHttpClient(httpClient);
    }

    private static RequestConfig getDefaultRequestConfig(FeignHttpClientProperties properties) {
        return RequestConfig.custom()
                .setConnectTimeout(properties.getConnectionTimeout())
                .setRedirectsEnabled(properties.isFollowRedirects())
                .build();
    }

    private static void disableSssl(HttpClientBuilder httpClientBuilder) {
        try {
            httpClientBuilder.setSSLContext(SSLContextBuilder.create().loadTrustMaterial(TrustAllStrategy.INSTANCE).build());
            httpClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
        } catch (Exception e) {
            log.warn("Failed to disable secure SSL context", e);
        }
    }

    @PreDestroy
    public void destroy() {
        log.info("Closing http client");
        if (httpClient != null) {
            try {
                httpClient.close();
            } catch (IOException e) {
                log.warn("Failed to close http client", e);
            }
        }
    }
}

