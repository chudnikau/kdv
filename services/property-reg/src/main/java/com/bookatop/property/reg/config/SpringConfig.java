package com.bookatop.property.reg.config;

import com.bookatop.catalog.book.api.serializers.CategoryTypesSerializer;
import com.bookatop.catalog.book.api.serializers.PropertyTypesSerializer;
import com.bookatop.property.reg.clients.CachedCatalogBookClient;
import com.bookatop.property.reg.clients.CatalogBookClient;
import com.bookatop.security.modules.HttpSensitiveModule;
import com.bookatop.service.config.AccessRolesSupportConfigurer;
import com.bookatop.storage.ImageStorage;
import com.bookatop.storage.Storage;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.netflix.discovery.DiscoveryClient;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import feign.Client;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.context.annotation.RequestScope;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableFeignClients(basePackages = "com.bookatop.property.reg.clients")
@PropertySource("classpath:application.properties")
public class SpringConfig extends AccessRolesSupportConfigurer {

    @Value("${registry.client.user.name}")
    private String registryClientUserName;

    @Value("${registry.client.user.password}")
    private String registryClientUserPassword;

    @Value("${images.storage.path}")
    private String imageStoragePath;

    @Bean
    public DiscoveryClient.DiscoveryClientOptionalArgs getTrustStoredEurekaClient() {
        List<ClientFilter> filters = new ArrayList<>();

        HTTPBasicAuthFilter httpBasicAuthFilter =
                new HTTPBasicAuthFilter(registryClientUserName, registryClientUserPassword);

        filters.add(httpBasicAuthFilter);

        DiscoveryClient.DiscoveryClientOptionalArgs args =
                new DiscoveryClient.DiscoveryClientOptionalArgs();
        args.setAdditionalFilters(filters);

        return args;
    }

    @Bean
    public Storage imageStorage() {
        return new ImageStorage(imageStoragePath);
    }

    @Bean
    @RequestScope
    public CachedCatalogBookClient catalogBookClient(Client client,
                                                     HttpServletRequest request,
                                                     JacksonEncoder jacksonEncoder,
                                                     JacksonDecoder jacksonDecoder) {
        return new CachedCatalogBookClient(
                Feign.builder()
                        .client(client)
                        .encoder(jacksonEncoder)
                        .decoder(jacksonDecoder)
                        .requestInterceptor(requestClientInterceptor(request))
                        .target(CatalogBookClient.class, "http://catalog-book"));
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder
                .serializers(new PropertyTypesSerializer(), new CategoryTypesSerializer())
                .modulesToInstall(new HttpSensitiveModule())
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .failOnEmptyBeans(false);
    }
}
