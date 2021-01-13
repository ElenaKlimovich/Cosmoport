package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/ships")
public class ShipController {

    @Autowired
    private ShipService shipService;

    @GetMapping
    public List<Ship> getShipsList (@RequestParam(required = false) String name,
                                    @RequestParam(required = false) String planet,
                                    @RequestParam(required = false) ShipType shipType,
                                    @RequestParam(required = false) Long after,
                                    @RequestParam(required = false) Long before,
                                    @RequestParam(required = false) Boolean isUsed,
                                    @RequestParam(required = false) Double minSpeed,
                                    @RequestParam(required = false) Double maxSpeed,
                                    @RequestParam(required = false) Integer minCrewSize,
                                    @RequestParam(required = false) Integer maxCrewSize,
                                    @RequestParam(required = false) Double minRating,
                                    @RequestParam(required = false) Double maxRating,
                                    @RequestParam(value = "order", required = false, defaultValue = "ID") ShipOrder order,
                                    @RequestParam(required = false, defaultValue = "0") Integer pageNumber,
                                    @RequestParam(required = false, defaultValue = "3") Integer pageSize) {

        List<Ship> allShips = shipService.getAllShips(name, planet, shipType, after, before, isUsed,
                                minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating);

        List<Ship> resultList = shipService.getShipsByPage(allShips, order, pageNumber, pageSize);

        return resultList;
    }


    @GetMapping("/count")
    public long getShipsCount(@RequestParam(required = false) String name,
                              @RequestParam(required = false) String planet,
                              @RequestParam(required = false) ShipType shipType,
                              @RequestParam(required = false) Long after,
                              @RequestParam(required = false) Long before,
                              @RequestParam(required = false) Boolean isUsed,
                              @RequestParam(required = false) Double minSpeed,
                              @RequestParam(required = false) Double maxSpeed,
                              @RequestParam(required = false) Integer minCrewSize,
                              @RequestParam(required = false) Integer maxCrewSize,
                              @RequestParam(required = false) Double minRating,
                              @RequestParam(required = false) Double maxRating) {

        return shipService.getAllShips(name, planet, shipType, after, before, isUsed,
                                        minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating)
                                        .size();
    }

    @PostMapping
    public Ship createShip(@RequestBody Ship newShip) {

        return shipService.createShip(newShip);
    }


    @GetMapping("/{id}")
    public Ship getShipById(@PathVariable Long id) {

        return shipService.getShipById(id);
    }


    @PostMapping("/{id}")
    public Ship updateShipById(@RequestBody Ship updatedShip, @PathVariable Long id) {

        return shipService.updateShipById(updatedShip, id);
    }

    @DeleteMapping("/{id}")
    public void deleteShipById(@PathVariable Long id) {

        shipService.deleteShipById(id);
    }
}
