package ru.practicum.mapper;

import ru.practicum.dto.location.LocationDto;
import ru.practicum.model.Location;

public class LocationMapper {

    public static LocationDto toLocationDto(Location location) {
        return new LocationDto(location.getLat(), location.getLon());
    }

    public static Location toLocation(LocationDto dto) {
        Location location = new Location();
        location.setLat(dto.getLat());
        location.setLon(dto.getLon());
        return location;
    }
}
