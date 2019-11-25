package logicos.actions;

import logicos.World;
import logicos.entities.Logico;
import logicos.exceptions.EntityNotPresent;
import logicos.exceptions.OutOfWorld;

import java.util.Random;

public class Attack extends Action {
  private Logico attacker;
  private Logico victim;

  public Attack(Logico attacker, Logico victim) {
    this.attacker = attacker;
    this.victim = victim;
  }

  @Override
  public void perform(World world) {
    Random r = new Random();
    // Damage is sampled from a normal distribution with mean=strength and variance=inaccuracy
    int damage = (int) Math.round(attacker.getAccuracy() * r.nextGaussian() + attacker.getStrength());
    if (damage < 0)
      damage = 0;
    if (victim.damage(damage) <= 0) // The attacker has killed the victim
      try {
        world.removeEntity(victim.getLocation());
        attacker.addWealth(1 + victim.getWealth()); // +1 wealth for the kill as well as stealing the victim's wealth
      } catch (EntityNotPresent entityNotPresent) {
        System.err.println("[!] Attempt made to attack non-existant logico.");
      } catch (OutOfWorld outOfWorld) {
        System.err.println("[!] Attempt made to attack a logico located outside of the map.");
      }
  }
}
