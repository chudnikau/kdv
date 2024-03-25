package com.bookatop.property.reg.lookups;

import com.bookatop.catalog.book.api.model.City;
import com.bookatop.catalog.book.api.model.Country;
import com.bookatop.property.reg.api.model.properties.general.PropertyLookupItem;
import com.bookatop.property.reg.clients.CachedCatalogBookClient;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class PropertyLookup {

    private PropertyLookup() {
    }

    public static <T> void updatePropertyLookup(T item, Consumer<T> update) {
        if (Objects.nonNull(item)) {
            update.accept(item);
        }
    }

    public static PropertyLookupItem lookupCountry(Long countryId, String lang, CachedCatalogBookClient client) {
        if (countryId > 0) {
            Optional<Country> country = client.getAllCountries(lang).stream()
                    .filter(r -> r.getId().equals(countryId))
                    .findFirst();
            String countryName = country
                    .flatMap(r -> Optional.of(r.getTsName()))
                    .orElse(null);
            return new PropertyLookupItem(countryId, countryName);
        }
        return null;
    }

    public static PropertyLookupItem lookupCity(Long countryId, Long cityId, String lang, CachedCatalogBookClient client) {
        if (countryId > 0 && cityId > 0) {
            Optional<City> city = client.getAllCities(countryId, lang).stream()
                    .filter(r -> r.getId().equals(cityId))
                    .findFirst();
            String cityName = city
                    .flatMap(r -> Optional.of(r.getTsName()))
                    .orElse(null);
            return new PropertyLookupItem(cityId, cityName);
        }
        return null;
    }
}
