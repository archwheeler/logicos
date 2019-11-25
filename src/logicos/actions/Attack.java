package logicos.actions;

import logicos.World;
import logicos.entities.Logico;
import logicos.exceptions.EntityNotPresent;
import logicos.exceptions.OutOfWorld;

public class Attack extends Action {
  private Logico attacker;
  private Logico victim;

  public Attack(Logico attacker, Logico victim) {
    this.attacker = attacker;
    this.victim = victim;
  }

  @Override
  public void perform(World world) {
    int victimNewStrength = victim.getStrength() - attacker.getStrength();
    if (victimNewStrength <= 0) // The attacker has killed the victim
      try {
        world.removeEntity(victim.getLocation());
        attacker.addWealth(1 + victim.getWealth()); // +1 wealth for the kill as well as stealing the victim's wealth
      } catch (EntityNotPresent entityNotPresent) {
        System.err.println("[!] Attempt made to attack non-existant logico.");
      } catch (OutOfWorld outOfWorld) {
        System.err.println("[!] Attempt made to attack a logico located outside of the map.");
      }
    else
      victim.setStrength(victimNewStrength);
  }
}
