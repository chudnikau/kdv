package com.bookatop.property.reg.clients;

import com.bookatop.catalog.book.api.model.*;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

public class CachedCatalogBookClient implements CatalogBookClient {

    private final CatalogBookClient catalogBookClient;

    public CachedCatalogBookClient(CatalogBookClient catalogBookClient) {
        this.catalogBookClient = catalogBookClient;
    }

    @Cacheable("propertyTypes")
    public List<PropertyType> getPropertyTypes() {
        return catalogBookClient.getPropertyTypes();
    }

    @Cacheable("propertyCategories")
    public List<PropertyCategory> getPropertyCategories(Long propTypeId) {
        return catalogBookClient.getPropertyCategories(propTypeId);
    }

    @Cacheable("propertyCategory")
    public PropertyCategory getPropertyCategory(Long propCatId) {
        return catalogBookClient.getPropertyCategory(propCatId);
    }

    @Cacheable("propertyRoomTypes")
    public List<RoomType> getPropertyRoomTypes(Long propCatId, String lang) {
        return catalogBookClient.getPropertyRoomTypes(propCatId, lang);
    }

    @Cacheable("allCountries")
    public List<Country> getAllCountries(String lang) {
        return catalogBookClient.getAllCountries(lang);
    }

    @Cacheable("allCities")
    public List<City> getAllCities(Long countryId, String lang) {
        return catalogBookClient.getAllCities(countryId, lang);
    }
}
