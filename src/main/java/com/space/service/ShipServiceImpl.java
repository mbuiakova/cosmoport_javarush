package com.space.service;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repositoryDAO.ShipRepository;
import com.space.validation.BadRequestException;
import com.space.validation.NotFountShipException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

@Service
public class ShipServiceImpl implements ShipService {

    @Autowired
    private ShipRepository repository;

    @Override
    public Page<Ship> getShips(Specification<Ship> specification, Pageable sortedBy) {
        return repository.findAll(specification, sortedBy);
    }

    @Override
    public void saveNewShip(Ship ship) {
        checkShip(ship);
        if (ship.isUsed() == null) {
            ship.setUsed(false);
        }
        ship.setRating(countRating(ship.getSpeed(), ship.isUsed(), ship.getProdDate()));
        repository.save(ship);
    }


    @Override
    public Ship getShip(Long id) {
        Ship ship = null;
        Optional<Ship> optional = repository.findById(id);
        if (optional.isPresent()) {
            ship = optional.get();
        }
        return ship;
    }

    @Override
    public void deleteShip(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Ship updateShip(Ship ship, Long id) {
        if (!repository.existsById(id)) {
            throw new NotFountShipException();//404
        }

        Ship editedShip = repository.findById(id).get();

        if (ship.getName() != null) {
            checkNameAndPlanet(ship.getName());
            editedShip.setName(ship.getName());
        }

        if (ship.getPlanet() != null) {
            checkNameAndPlanet(ship.getPlanet());
            editedShip.setPlanet(ship.getPlanet());
        }

        if (ship.getShipType() != null) {
            checkShipType(ship.getShipType());
            editedShip.setShipType(ship.getShipType());
        }

        if (ship.getProdDate() != null) {
            checkProdDate(ship.getProdDate());
            editedShip.setProdDate(ship.getProdDate());
        }

        if (ship.getSpeed() != null) {
            checkSpeed(ship.getSpeed());
            editedShip.setSpeed(ship.getSpeed());
        }

        if (ship.isUsed() != null) {
            editedShip.setUsed(ship.isUsed());
        }

        if (ship.getCrewSize() != null) {
            checkCrew(ship.getCrewSize());
            editedShip.setCrewSize(ship.getCrewSize());
        }
        Double rating = countRating(editedShip.getSpeed(), editedShip.isUsed(), editedShip.getProdDate());
        editedShip.setRating(rating);

        return repository.saveAndFlush(editedShip);
    }

    public void checkShip(Ship ship) {
        if (ship.getName() == null || ship.getPlanet() == null || ship.getShipType() == null || ship.getProdDate() == null ||
                ship.getSpeed() == null || ship.getCrewSize() == null) {
            throw new BadRequestException("One of the parameters was null");
        }
        checkNameAndPlanet(ship.getName());
        checkNameAndPlanet(ship.getPlanet());
        checkProdDate(ship.getProdDate());
        checkSpeed(ship.getSpeed());
        checkCrew(ship.getCrewSize());
    }

    @Override
    public boolean checkId(String id) {
        Long checkId = null;

        try {
            checkId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return false;
        }
        if (checkId <= 0) {
            return false;
        }
//        if (checkId % 1 != 0) {
//            return false;
//        }
        return true;
    }

    @Override
    public Specification<Ship> selectByName(String name) {
        return new Specification<Ship>() {
            @Override
            public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (name == null) {
                    return null;
                }
                return criteriaBuilder.like(root.get("name"), "%" + name + "%");
            }
        };
    }

    @Override
    public Specification<Ship> selectByPlanet(String planet) {
        return new Specification<Ship>() {
            @Override
            public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (planet == null) {
                    return null;
                }
                return criteriaBuilder.like(root.get("planet"), "%" + planet + "%");
            }
        };
    }

    @Override
    public Specification<Ship> selectByShipType(ShipType shipType) {
        return new Specification<Ship>() {
            @Override
            public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (shipType == null) {
                    return null;
                }
                return criteriaBuilder.equal(root.get("shipType"), shipType);
            }
        };
    }

    @Override
    public Specification<Ship> selectByProdDate(Long after, Long before) {
        return new Specification<Ship>() {
            @Override
            public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (after == null && before == null) {
                    return null;
                }
                if (after == null) {
                    Date tempDate = new Date(before);
                    return criteriaBuilder.lessThanOrEqualTo(root.get("prodDate"), tempDate);
                }
                if (before == null) {
                    Date tempDate = new Date(after);
                    return criteriaBuilder.greaterThanOrEqualTo(root.get("prodDate"), tempDate);
                }
                Calendar beforeCalendar = new GregorianCalendar();
                beforeCalendar.setTime(new Date(before));
                beforeCalendar.set(Calendar.HOUR, 0);
                beforeCalendar.add(Calendar.MILLISECOND, -1);

                Date tempAfter = new Date(after);
                Date tempBefore = beforeCalendar.getTime();

                return criteriaBuilder.between(root.get("prodDate"), tempAfter, tempBefore);
            }
        };
    }

    @Override
    public Specification<Ship> selectByUsed(Boolean isUsed) {
        return new Specification<Ship>() {
            @Override
            public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (isUsed == null) {
                    return null;
                }
                if (isUsed) {
                    return criteriaBuilder.isTrue(root.get("isUsed"));
                } else {
                    return criteriaBuilder.isFalse(root.get("isUsed"));
                }
            }
        };
    }

    @Override
    public Specification<Ship> selectBySpeed(Double minSpeed, Double maxSpeed) {
        return new Specification<Ship>() {
            @Override
            public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (minSpeed == null && maxSpeed == null) {
                    return null;
                }
                if (minSpeed == null) {
                    return criteriaBuilder.lessThanOrEqualTo(root.get("speed"), maxSpeed);
                }
                if (maxSpeed == null) {
                    return criteriaBuilder.greaterThanOrEqualTo(root.get("speed"), minSpeed);
                }
                return criteriaBuilder.between(root.get("speed"), minSpeed, maxSpeed);
            }
        };
    }

    @Override
    public Specification<Ship> selectByCrew(Integer minCrew, Integer maxCrew) {
        return new Specification<Ship>() {
            @Override
            public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (minCrew == null && maxCrew == null) {
                    return null;
                }
                if (minCrew == null) {
                    return criteriaBuilder.lessThanOrEqualTo(root.get("crewSize"), maxCrew);
                }
                if (maxCrew == null) {
                    return criteriaBuilder.greaterThanOrEqualTo(root.get("crewSize"), minCrew);
                }
                return criteriaBuilder.between(root.get("crewSize"), minCrew, maxCrew);
            }
        };
    }

    @Override
    public Specification<Ship> selectByRating(Double minRating, Double maxRating) {
        return new Specification<Ship>() {
            @Override
            public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                if (maxRating == null && minRating == null) {
                    return null;
                }
                if (minRating == null) {
                    return criteriaBuilder.lessThanOrEqualTo(root.get("rating"), maxRating);
                }
                if (maxRating == null) {
                    return criteriaBuilder.greaterThanOrEqualTo(root.get("rating"), minRating);
                }
                return criteriaBuilder.between(root.get("rating"), minRating, maxRating);
            }
        };
    }

    @Override
    public Integer getShipCount(Specification<Ship> specification) {
        return repository.findAll(specification).size();
    }

    public void checkNameAndPlanet(String name) {
        if (name.length() == 0 || name.length() > 51) {
            throw new BadRequestException("The name or planet more 50 characters");
        }
    }

    public void checkShipType(ShipType shipType) {
        for (ShipType s : ShipType.values()) {
            if (s.name().equals(shipType.toString())) {
                return;
            }
        }
        throw new BadRequestException("Incorrect ship type");
    }

    public void checkProdDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        if (year < 2799 || year > 3020) {
            throw new BadRequestException("Incorrect production data");
        }
    }

    public void checkSpeed(Double speed) {
        if (speed < 0.01 || speed > 0.99) {
            throw new BadRequestException("Incorrect speed");
        }
    }

    public void checkCrew(Integer crew) {
        if (crew < 1 || crew > 9999) {
            throw new BadRequestException("Incorrect crew size");
        }
    }

    private double countRating(double speed, boolean isUsed, Date date) {
        double k;
        if (isUsed) {
            k = 0.5;
        } else k = 1.0;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        double rating = (80 * speed * k) / (3019 - year + 1);

        return Math.round(rating * 100.0) / 100.0;
    }

}
