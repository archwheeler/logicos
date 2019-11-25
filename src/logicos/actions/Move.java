package logicos.actions;

import logicos.World;
import logicos.entities.Location;
import logicos.entities.Logico;
import logicos.exceptions.EntityIsPresent;
import logicos.exceptions.OutOfWorld;

public class Move extends Action {
  Logico logico;
  Location destination;

  public Move(Logico logico, Location location) {
    this.logico = logico;
    destination = location;
  }

  @Override
  public void perform(World world) {
    try {
      if (world.getEntity(destination) != null) {
        throw new EntityIsPresent();
      }
    } catch (OutOfWorld outOfWorld) {
      System.err.println("[!] Attempt made to move out of the world.");
    } catch (EntityIsPresent entityIsPresent) {
      System.err.println("[!] Attempt made to move to non-free location.");
    }
    Location previous = logico.getLocation();
    world.setLocation(destination, logico);
    world.setLocation(previous, null);
  }
}
