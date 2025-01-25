package fr.fullstack.shopapp.service;

import fr.fullstack.shopapp.model.FirstSync;
import fr.fullstack.shopapp.model.Shop;
import fr.fullstack.shopapp.repository.FirstSyncRepository;
import fr.fullstack.shopapp.repository.ShopRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class SyncService {

    @PersistenceContext
    EntityManager em;

    private static final Logger log = LoggerFactory.getLogger(SyncService.class);

    @Autowired
    private FirstSyncRepository firstSyncRepository;

    @Autowired
    private ShopService shopService;

    @PostConstruct
    @Transactional
    public boolean initSync() {
        if(firstSyncRepository.findBySyncedEquals(true).isPresent()) {
            return false;
        };

        Page<Shop> shops = shopService.getShopList(Optional.empty(), Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty(),
                Optional.empty(),  Pageable.unpaged());

        for (Shop shop : shops) {
            try {
                log.debug("Processing shop: {}", shop.getId());
                shopService.createShopWithoutChekingHours(shop);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        FirstSync firstSync = new FirstSync();
        firstSync.setSynced(true);
        firstSyncRepository.save(firstSync);

        return true;
    }
}
