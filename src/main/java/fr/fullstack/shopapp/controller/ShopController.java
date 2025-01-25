package fr.fullstack.shopapp.controller;

import fr.fullstack.shopapp.model.Shop;
import fr.fullstack.shopapp.service.ShopService;
import fr.fullstack.shopapp.util.ErrorValidation;
import io.swagger.v3.oas.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/shops")
public class ShopController {
    // TODO ADD PLAIN TEXT SEARCH FOR SHOP
    @Autowired
    private ShopService service;

    @Operation(summary = "Create a shop")
    @PostMapping
    public ResponseEntity<Shop> createShop(@Valid @RequestBody Shop shop, Errors errors) {
        if (errors.hasErrors()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, ErrorValidation.getErrorValidationMessage(errors));
        }

        try {
            return ResponseEntity.ok(service.createShop(shop));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Delete a shop by its id")
    @DeleteMapping("/{id}")
    public HttpStatus deleteShop(@PathVariable long id) {
        try {
            service.deleteShopById(id);
            return HttpStatus.NO_CONTENT;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(
            summary = "Get shops",
            description = "Retrieve a paginated list of shops with options for sorting, filtering, and searching"
    )
    @GetMapping
    public ResponseEntity<Page<Shop>> getAllShops(
            @Parameter(description = "Page number (0..N)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of records per page", example = "5")
            @RequestParam(defaultValue = "5") int size,

            @Parameter(description = "Recherche par nom", example = "name")
            @RequestParam(required = false) Optional<String> name,

            @Parameter(description = "Sort the shops. Possible values are 'name', 'nbProducts', 'createdAt'", example = "name")
            @RequestParam(required = false) Optional<String> sortBy,

            @Parameter(description = "Whether the shops must be in vacations or not", example = "true")
            @RequestParam(required = false) Optional<Boolean> inVacations,

            @Parameter(description = "Filter shops created after this date (YYYY-MM-DD)", example = "2022-11-15")
            @RequestParam(required = false) Optional<String> createdAfter,

            @Parameter(description = "Filter shops created before this date (YYYY-MM-DD)", example = "2022-11-15")
            @RequestParam(required = false) Optional<String> createdBefore,

            @Parameter(description = "Search term to filter shops by name", example = "bakery")
            @RequestParam(required = false) Optional<String> search,

            Pageable pageable
    ) {
        return ResponseEntity.ok(
                service.getShopList(name, sortBy, inVacations, createdAfter, createdBefore, search, pageable)
        );
    }



    @Operation(summary = "Get a shop by id")
    @GetMapping("/{id}")
    public ResponseEntity<Shop> getShopById(@PathVariable long id) {
        try {
            return ResponseEntity.ok().body(service.getShopById(id));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Update a shop")
    @PutMapping
    public ResponseEntity<Shop> updateShop(@Valid @RequestBody Shop shop, Errors errors) {
        if (errors.hasErrors()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, ErrorValidation.getErrorValidationMessage(errors));
        }

        try {
            return ResponseEntity.ok().body(service.updateShop(shop));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
