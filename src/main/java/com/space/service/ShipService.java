package com.space.service;

import com.space.controller.ShipOrder;
import com.space.exception.BadRequestException;
import com.space.exception.NotFoundException;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ShipService {

    @Autowired
    private ShipRepository shipRepository;

    @Transactional
    public List<Ship> getAllShips(String name, String planet, ShipType shipType, Long after, Long before,
                                  Boolean isUsed, Double minSpeed, Double maxSpeed,
                                  Integer minCrewSize, Integer maxCrewSize, Double minRating, Double maxRating) {

        List<Ship> allShips = shipRepository.findAll();

        if (name != null)
            allShips = allShips.stream().filter(s -> s.getName().contains(name)).collect(Collectors.toList());

        if (planet != null)
            allShips = allShips.stream().filter(s -> s.getPlanet().contains(planet)).collect(Collectors.toList());

        if (shipType != null)
            allShips = allShips.stream().filter(s -> s.getShipType().equals(shipType)).collect(Collectors.toList());

        if (after != null)
            allShips = allShips.stream().filter(ship -> ship.getProdDate().after(new Date(after))).collect(Collectors.toList());

        if (before != null)
            allShips = allShips.stream().filter(ship -> ship.getProdDate().before(new Date(before))).collect(Collectors.toList());

        if (isUsed != null)
            allShips = allShips.stream().filter(ship -> ship.isUsed().equals(isUsed)).collect(Collectors.toList());

        if (minSpeed != null)
            allShips = allShips.stream().filter(ship -> ship.getSpeed() >= minSpeed).collect(Collectors.toList());

        if (maxSpeed != null)
            allShips = allShips.stream().filter(ship -> ship.getSpeed() <= maxSpeed).collect(Collectors.toList());

        if (minCrewSize != null)
            allShips = allShips.stream().filter(ship -> ship.getCrewSize() >= minCrewSize).collect(Collectors.toList());

        if (maxCrewSize != null)
            allShips = allShips.stream().filter(ship -> ship.getCrewSize() <= maxCrewSize).collect(Collectors.toList());

        if (minRating != null)
            allShips = allShips.stream().filter(ship -> ship.getRating() >= minRating).collect(Collectors.toList());

        if (maxRating != null)
            allShips = allShips.stream().filter(ship -> ship.getRating() <= maxRating).collect(Collectors.toList());

        return allShips;
    }

    @Transactional
    public List<Ship> getShipsByPage(List<Ship> allShips, ShipOrder order, Integer pageNumber, Integer pageSize) {

        if (pageNumber == null) pageNumber = 0;
        if (pageSize == null) pageSize = 3;

        return allShips.stream().sorted(getComparator(order))
                .skip(pageNumber * pageSize).limit(pageSize)
                .collect(Collectors.toList());
    }

    @Transactional
    public Ship createShip(Ship newShip) {

        if (newShip == null || newShip.getName() == null || newShip.getPlanet() == null || newShip.getProdDate() == null
                || newShip.getCrewSize() == null || newShip.getShipType() == null || newShip.getSpeed() == null)
            throw new BadRequestException("Empty fields!");
        checkName(newShip.getName());
        checkName(newShip.getPlanet());
        checkDate(newShip.getProdDate());
        checkCrew(newShip.getCrewSize());
        checkAndRoundSpeed(newShip.getSpeed());

        if (newShip.isUsed() == null)
            newShip.setUsed(false);

        newShip.setRating(calculateRating(newShip));

        return shipRepository.save(newShip);
    }

    @Transactional
    public Ship updateShipById(Ship updatedShip, Long id) {

        Ship ship = getShipById(id);
        if (updatedShip == null)
            throw new BadRequestException("Empty update info!");

        String name = updatedShip.getName();
        String planet = updatedShip.getPlanet();
        ShipType shipType = updatedShip.getShipType();
        Date prodDate = updatedShip.getProdDate();
        Boolean isUsed = updatedShip.isUsed();
        Double speed = updatedShip.getSpeed();
        Integer crewSize = updatedShip.getCrewSize();

        if (name != null) {
            checkName(name);
            ship.setName(name);
        }

        if (planet != null) {
            checkName(planet);
            ship.setPlanet(planet);
        }

        if (crewSize != null) {
            checkCrew(crewSize);
            ship.setCrewSize(crewSize);
        }

        if (prodDate != null) {
            checkDate(prodDate);
            ship.setProdDate(prodDate);
        }

        if (speed != null)
            ship.setSpeed(checkAndRoundSpeed(speed));

        if (shipType != null)
            ship.setShipType(shipType);

        if (isUsed != null)
            ship.setUsed(isUsed);

        ship.setRating(calculateRating(ship));

        return shipRepository.save(ship);
    }

    @Transactional
    public Ship getShipById(Long id) {
        checkId(id);
        if (!shipRepository.existsById(id)) throw new NotFoundException();
        return shipRepository.findById(id).orElse(null);
    }

    @Transactional
    public void deleteShipById(Long id) {
        if (getShipById(id) != null)
            shipRepository.deleteById(id);
    }

    /*-------------------------SHIP_UTILS--------------------------------*/

    private void checkId(Long id) {
        if (id == null || id <= 0)
            throw new BadRequestException("Wrong id!");
    }

    private void checkName(String name) {
        if (name.length() > 50 || name.isEmpty())
            throw new BadRequestException("Wrong name!");
    }

    private void checkDate(Date prodDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(prodDate);
        if (calendar.get(Calendar.YEAR) < 2800 || calendar.get(Calendar.YEAR) > 3019)
            throw new BadRequestException("Wrong date!");
    }

    private double checkAndRoundSpeed(Double speed) {
        if (speed < 0.01d || speed > 0.99d)
            throw new BadRequestException("Wrong speed!");
        BigDecimal value = BigDecimal.valueOf(speed);
        value.setScale(2, RoundingMode.HALF_UP);
        return value.doubleValue();
    }

    private void checkCrew(Integer crewSize) {
        if (crewSize < 1 || crewSize > 9999)
            throw new BadRequestException("Wrong crew size!");
    }

    private Comparator<Ship> getComparator(ShipOrder order) {

        switch (order.getFieldName()) {
            case "speed":
                return Comparator.comparing(Ship::getSpeed);
            case "prodDate":
                return Comparator.comparing(Ship::getProdDate);
            case "rating":
                return Comparator.comparing(Ship::getRating);
            default:
                return Comparator.comparing(Ship::getId);
        }
    }

    private Double calculateRating(Ship ship) {

        double coefficient = ship.isUsed() ? 0.5d : 1.0d;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ship.getProdDate());
        double year = calendar.get(Calendar.YEAR);

        double result = (80 * ship.getSpeed() * coefficient) / (3019 - year + 1);
        return (double) Math.round(result * 100) / 100;
    }
}