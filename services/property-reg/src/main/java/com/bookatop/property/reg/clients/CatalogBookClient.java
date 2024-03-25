package com.bookatop.property.reg.clients;

import com.bookatop.catalog.book.api.model.*;
import feign.Param;
import feign.RequestLine;

import java.util.List;

public interface CatalogBookClient {

    @RequestLine("GET /catalog-book/property/types")
    List<PropertyType> getPropertyTypes();

    @RequestLine("GET /catalog-book/property/categories/{propTypeId}")
    List<PropertyCategory> getPropertyCategories(@Param Long propTypeId);

    @RequestLine("GET /catalog-book/property/category/{propCatId}")
    PropertyCategory getPropertyCategory(@Param Long propCatId);

    @RequestLine("GET /catalog-book/property/room-types/{propCatId}?lang={lang}")
    List<RoomType> getPropertyRoomTypes(@Param Long propCatId, @Param String lang);

    @RequestLine("GET /catalog-book/country/all?lang={lang}")
    List<Country> getAllCountries(@Param String lang);

    @RequestLine("GET /catalog-book/country/{countryId}/cities?lang={lang}")
    List<City> getAllCities(@Param Long countryId, @Param String lang);
}
