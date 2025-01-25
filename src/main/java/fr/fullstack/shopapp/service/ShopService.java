package fr.fullstack.shopapp.service;

import fr.fullstack.shopapp.model.OpeningHoursShop;
import fr.fullstack.shopapp.model.Product;
import fr.fullstack.shopapp.model.Shop;
import fr.fullstack.shopapp.repository.ShopRepository;
import fr.fullstack.shopapp.search.ShopElasticRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShopService {
    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ShopElasticRepository shopElasticRepository;

    @Transactional
    public Shop createShop(Shop shop) throws Exception {
       validateOpeningHours(shop.getOpeningHours());
        try {
            Shop shop1 = shopRepository.save(shop);
            em.flush();
            em.refresh(shop1);
            shopElasticRepository.save(shop1);
            return shop1;
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    private void validateOpeningHours(List<OpeningHoursShop> openingHours) {
        Map<Long, List<OpeningHoursShop>> openingHoursByDay = openingHours.stream()
                .collect(Collectors.groupingBy(OpeningHoursShop::getDay));

        for (List<OpeningHoursShop> dayOpeningHours : openingHoursByDay.values()) {
            if (dayOpeningHours.size() > 1) {
                checkForOverlap(dayOpeningHours);
            }
        }
    }

    private void checkForOverlap(List<OpeningHoursShop> dayOpeningHours) {
        for (int i = 0; i < dayOpeningHours.size(); i++) {
            for (int j = i + 1; j < dayOpeningHours.size(); j++) {
                OpeningHoursShop hours1 = dayOpeningHours.get(i);
                OpeningHoursShop hours2 = dayOpeningHours.get(j);

                if (isOverlapping(hours1, hours2)) {
                    throw new IllegalArgumentException("Les horaires d'ouverture se chevauchent pour le jour " + hours1.getDay());
                }
            }
        }
    }

    private boolean isOverlapping(OpeningHoursShop hours1, OpeningHoursShop hours2) {
        return !(hours1.getCloseAt().isBefore(hours2.getOpenAt()) || hours2.getCloseAt().isBefore(hours1.getOpenAt()));
    }


    @Transactional
    public void deleteShopById(long id) throws Exception {
        try {
            Shop shop = getShop(id);
            // delete nested relations with products
            deleteNestedRelations(shop);
            shopRepository.deleteById(id);
            shopElasticRepository.deleteById(id);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public Shop getShopById(long id) throws Exception {
        try {
            return getShop(id);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public Page<Shop> getShopList(
            Optional<String> name,
            Optional<String> sortBy,
            Optional<Boolean> inVacations,
            Optional<String> createdBefore,
            Optional<String> createdAfter,
            Optional<String> search,
            Pageable pageable
    ) {
        // SEARCH
        if (search.isPresent()) {
            return shopRepository.findByNameContainingIgnoreCase(search.get(), pageable);
        }

        // SORT
        if (sortBy.isPresent()) {
            switch (sortBy.get()) {
                case "name":
                    return shopRepository.findByOrderByNameAsc(pageable);
                case "createdAt":
                    return shopRepository.findByOrderByCreatedAtAsc(pageable);
                default:
                    return shopRepository.findByOrderByNbProductsAsc(pageable);
            }
        }

        // FILTERS
        Page<Shop> shopList = getShopListWithFilter(name, inVacations, createdBefore, createdAfter, pageable);
        if (shopList != null) {
            return shopList;
        }

        // NONE
        return shopRepository.findByOrderByIdAsc(pageable);
    }


    @Transactional
    public Shop updateShop(Shop shop) throws Exception {
        try {
            getShop(shop.getId());
            return this.createShop(shop);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    private void deleteNestedRelations(Shop shop) {
        List<Product> products = shop.getProducts();
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            product.setShop(null);
            em.merge(product);
            em.flush();
        }
    }

    private Shop getShop(Long id) throws Exception {
        Optional<Shop> shop = shopRepository.findById(id);
        if (!shop.isPresent()) {
            throw new Exception("Shop with id " + id + " not found");
        }
        return shop.get();
    }

    private Page<Shop> getShopListWithFilter(
            Optional<String> name,
            Optional<Boolean> inVacations,
            Optional<String> createdAfter,
            Optional<String> createdBefore,
            Pageable pageable
    ) {

        // Search And Filters
        if (name.isPresent()) {
            LocalDate after; LocalDate before;
            after = createdAfter.map(LocalDate::parse).orElse(LocalDate.EPOCH);
            before = createdBefore.map(LocalDate::parse).orElseGet(() -> LocalDate.EPOCH.plusYears(99));
            if (inVacations.isEmpty()) {
                inVacations = Optional.of(false);
            }
            return shopElasticRepository.findAllByNameContainingAndCreatedAtAfterAndCreatedAtBeforeAndInVacationsEquals(
                    name.get(), after, before, inVacations.get(), pageable );
        }

        if (inVacations.isPresent() && createdBefore.isPresent() && createdAfter.isPresent()) {
            return shopRepository.findByInVacationsAndCreatedAtGreaterThanAndCreatedAtLessThan(
                    inVacations.get(),
                    LocalDate.parse(createdAfter.get()),
                    LocalDate.parse(createdBefore.get()),
                    pageable
            );
        }

        if (inVacations.isPresent() && createdBefore.isPresent()) {
            return shopRepository.findByInVacationsAndCreatedAtLessThan(
                    inVacations.get(), LocalDate.parse(createdBefore.get()), pageable
            );
        }

        if (inVacations.isPresent() && createdAfter.isPresent()) {
            return shopRepository.findByInVacationsAndCreatedAtGreaterThan(
                    inVacations.get(), LocalDate.parse(createdAfter.get()), pageable
            );
        }

        if (inVacations.isPresent()) {
            return shopRepository.findByInVacations(inVacations.get(), pageable);
        }

        if (createdBefore.isPresent() && createdAfter.isPresent()) {
            return shopRepository.findByCreatedAtBetween(
                    LocalDate.parse(createdAfter.get()), LocalDate.parse(createdBefore.get()), pageable
            );
        }

        if (createdBefore.isPresent()) {
            return shopRepository.findByCreatedAtLessThan(
                    LocalDate.parse(createdBefore.get()), pageable
            );
        }

        if (createdAfter.isPresent()) {
            return shopRepository.findByCreatedAtGreaterThan(
                    LocalDate.parse(createdAfter.get()), pageable
            );
        }

        return null;
    }

    @Transactional
    public void createShopWithoutChekingHours(Shop shop) throws Exception {
            Shop shop1 = shopRepository.save(shop);
            em.flush();
            em.refresh(shop1);
            shopElasticRepository.save(shop1);
    }
}
