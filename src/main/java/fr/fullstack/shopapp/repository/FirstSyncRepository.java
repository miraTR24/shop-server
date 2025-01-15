package fr.fullstack.shopapp.repository;

import fr.fullstack.shopapp.model.FirstSync;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface FirstSyncRepository extends JpaRepository<FirstSync, Long> {

    public Optional<FirstSync> findBySyncedEquals(Boolean bool);
}
