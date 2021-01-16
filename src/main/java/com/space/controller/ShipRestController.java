package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest")
public class ShipRestController {

    @Autowired
    private ShipService shipService;

    @GetMapping("/ships")
    public ResponseEntity<List<Ship>> showAllShips(@RequestParam(value = "name", required = false) String name,
                                                   @RequestParam(value = "planet", required = false) String planet,
                                                   @RequestParam(value = "shipType", required = false) ShipType shipType,
                                                   @RequestParam(value = "after", required = false) Long after,
                                                   @RequestParam(value = "before", required = false) Long before,
                                                   @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                                                   @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                                                   @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                                                   @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                                                   @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                                                   @RequestParam(value = "minRating", required = false) Double minRating,
                                                   @RequestParam(value = "maxRating", required = false) Double maxRating,
                                                   @RequestParam(value = "order", required = false, defaultValue = "ID") ShipOrder order,
                                                   @RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
                                                   @RequestParam(value = "pageSize", required = false, defaultValue = "3") Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(order.getFieldName()));
        Specification<Ship> specification = Specification.where(shipService.selectByName(name)
                .and(shipService.selectByPlanet(planet).and(shipService.selectByShipType(shipType))
                        .and(shipService.selectByProdDate(after, before)).and(shipService.selectByUsed(isUsed))
                        .and(shipService.selectBySpeed(minSpeed, maxSpeed)).and(shipService.selectByCrew(minCrewSize, maxCrewSize))
                        .and(shipService.selectByRating(minRating, maxRating))));

        return new ResponseEntity<>(shipService.getShips(specification, pageable).getContent(), HttpStatus.OK);
    }

    @GetMapping("/ships/{id}")
    public ResponseEntity<Ship> getShipById(@PathVariable Long id) {
        if (id == 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!shipService.checkId(id.toString())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Ship ship = shipService.getShip(id);
        if (ship == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(ship, HttpStatus.OK);
    }

    @PostMapping("/ships")
    public ResponseEntity<Ship> createShip(@RequestBody Ship ship) {
        if (ship == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        this.shipService.saveNewShip(ship);
        return new ResponseEntity<>(ship, HttpStatus.OK);
    }

    @PostMapping("/ships/{id}")
    public ResponseEntity<Ship> updateShip(@RequestBody Ship ship, @PathVariable Long id) {
        if (ship == null || id == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!shipService.checkId(id.toString())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);//400
        }
        Ship editedShip = shipService.updateShip(ship, id);
        return new ResponseEntity<>(editedShip, HttpStatus.OK);
    }


    @DeleteMapping("/ships/{id}")
    public ResponseEntity<Ship> deleteShip(@PathVariable("id") Long id) {
        Ship ship = shipService.getShip(id);
        if (id == 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (ship == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!shipService.checkId(id.toString())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        shipService.deleteShip(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/ships/count")
    public ResponseEntity<Integer> count(@RequestParam(value = "name", required = false) String name,
                                         @RequestParam(value = "planet", required = false) String planet,
                                         @RequestParam(value = "shipType", required = false) ShipType shipType,
                                         @RequestParam(value = "after", required = false) Long after,
                                         @RequestParam(value = "before", required = false) Long before,
                                         @RequestParam(value = "isUsed", required = false) Boolean isUsed,
                                         @RequestParam(value = "minSpeed", required = false) Double minSpeed,
                                         @RequestParam(value = "maxSpeed", required = false) Double maxSpeed,
                                         @RequestParam(value = "minCrewSize", required = false) Integer minCrewSize,
                                         @RequestParam(value = "maxCrewSize", required = false) Integer maxCrewSize,
                                         @RequestParam(value = "minRating", required = false) Double minRating,
                                         @RequestParam(value = "maxRating", required = false) Double maxRating) {
        Specification<Ship> specification = Specification.where(shipService.selectByName(name)
                .and(shipService.selectByPlanet(planet).and(shipService.selectByShipType(shipType))
                        .and(shipService.selectByProdDate(after, before)).and(shipService.selectByUsed(isUsed))
                        .and(shipService.selectBySpeed(minSpeed, maxSpeed)).and(shipService.selectByCrew(minCrewSize, maxCrewSize))
                        .and(shipService.selectByRating(minRating, maxRating))));
        return new ResponseEntity<>(shipService.getShipCount(specification), HttpStatus.OK);

    }


}
