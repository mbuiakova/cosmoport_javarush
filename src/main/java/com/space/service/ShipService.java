package com.space.service;

import com.space.model.Ship;
import com.space.model.ShipType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface ShipService {

    public Page<Ship> getShips(Specification<Ship> specification, Pageable sortedBy);

    public void saveNewShip(Ship ship);

    public Ship getShip(Long id);

    public void deleteShip(Long id);

    public Ship updateShip(Ship ship, Long id);

    public boolean checkId(String id);


    Specification<Ship> selectByName(String name);

    Specification<Ship> selectByPlanet(String planet);

    Specification<Ship> selectByShipType(ShipType shipType);

    Specification<Ship> selectByProdDate(Long after, Long before);

    Specification<Ship> selectByUsed(Boolean isUsed);

    Specification<Ship> selectBySpeed(Double minSpeed, Double maxSpeed);

    Specification<Ship> selectByCrew(Integer minCrew, Integer maxCrew);

    Specification<Ship> selectByRating(Double minRating, Double maxRating);

    Integer getShipCount(Specification<Ship> specification);
}
