package fr.fullstack.shopapp;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.validation.Errors;

@EnableElasticsearchRepositories("fr.fullstack.shopapp.search")
@EnableJpaRepositories("fr.fullstack.shopapp.repository")
@SpringBootApplication
public class ShopAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopAppApplication.class, args);
    }

    @Bean
    public GroupedOpenApi shopAppApi() {
        return GroupedOpenApi.builder()
                .group("shopapp") // Nom du groupe, utile pour distinguer plusieurs APIs
                .packagesToScan("fr.fullstack.shopapp.controller") // Remplace RequestHandlerSelectors.basePackage
                .pathsToMatch("/**") // Remplace PathSelectors.any
                .build();
    }

}
