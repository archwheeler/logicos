package logicos.entities;

import logicos.actions.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Logico extends Entity {
  private int health;
  private int strength;
  private double accuracy;
  private int wealth;
  private Location location; // The logico's location

  public Logico(int strength, double accuracy, Location location, int health) {
    wealth = 0;
    this.health = health;
    this.location = location;
    this.accuracy = accuracy;
    this.strength = strength;
  }

  public Action advance(List<Entity> surroundings) { // TODO: make most 'logical' decision
    List<Location> possibleMoves = new LinkedList<>(); // keep track of empty tiles as potential move locations
    Logico target = null;
    for (Entity entity : surroundings) {
      if (entity instanceof Logico) { // TODO: can we get rid of ugly typecasts?
        if (target == null || ((Logico) entity).getWealth() < target.getWealth())
          target = (Logico) entity;
      }
      else if (entity instanceof Location)
        possibleMoves.add((Location) entity);
    }

    if (target != null)
      return new Attack(this, target);
    else if (possibleMoves.size() > 0) {
      Random r = new Random();
      // Choose a random location from the reachable locations in possibleMoves
      Location destination = possibleMoves.get(r.nextInt(possibleMoves.size()));
      return new Move(this, destination);
    }

    return new Skip(); // if we can't make a move, stay put
  }

  public int getStrength() {
    return strength;
  }

  public int getWealth() {
    return wealth;
  }

  public Location getLocation() {
    return location;
  }

  public double getAccuracy() {
    return accuracy;
  }

  public int getHealth() {
    return health;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public void setStrength(int strength) {
    this.strength = strength;
  }

  public int damage(int damage) {
    return this.health -= damage; // return new health
  }

  public void addWealth(int wealth) { this.wealth += wealth; }
}
