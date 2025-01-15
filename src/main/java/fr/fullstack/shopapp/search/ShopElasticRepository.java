package fr.fullstack.shopapp.search;

import fr.fullstack.shopapp.model.Shop;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.time.LocalDate;

public interface ShopElasticRepository extends ElasticsearchRepository<Shop, Long> {
    Page<Shop> findAllByNameContainingAndCreatedAtAfterAndCreatedAtBeforeAndInVacationsEquals(
            String name, LocalDate after, LocalDate before, Boolean inVacation, Pageable pageable);

}
