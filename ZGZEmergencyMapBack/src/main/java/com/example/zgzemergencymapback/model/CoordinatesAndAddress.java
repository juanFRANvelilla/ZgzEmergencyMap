package com.example.zgzemergencymapback.model;

import lombok.*;

import java.util.List;
import java.util.Objects;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoordinatesAndAddress {
    private List<Double> coordinates;
    private String address;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoordinatesAndAddress that = (CoordinatesAndAddress) o;
        return Objects.equals(coordinates, that.coordinates) && Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(coordinates, address);
    }

}
