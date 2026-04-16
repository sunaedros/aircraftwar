package edu.hitsz.factory;

import edu.hitsz.aircraft.AbstractAircraft;

public interface AircraftFactory {
    AbstractAircraft createAircraft(int locationX, int locationY, int speedX, int speedY, int hp);
}