package net.minecraft.server;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityTeleportEvent;

public class EntityEnderman extends EntityMonster
{
  public static boolean[] d = new boolean[256];
  private int e = 0;
  private int f = 0;

  public EntityEnderman(World world) {
    super(world);
    this.texture = "/mob/enderman.png";
    this.bI = 0.2F;
    a(0.6F, 2.9F);
    this.X = 1.0F;
  }

  public int getMaxHealth() {
    return 40;
  }

  protected void a() {
    super.a();
    this.datawatcher.a(16, new Byte((byte)0));
    this.datawatcher.a(17, new Byte((byte)0));
    this.datawatcher.a(18, new Byte((byte)0));
  }

  public void b(NBTTagCompound nbttagcompound) {
    super.b(nbttagcompound);
    nbttagcompound.setShort("carried", (short)getCarriedId());
    nbttagcompound.setShort("carriedData", (short)getCarriedData());
  }

  public void a(NBTTagCompound nbttagcompound) {
    super.a(nbttagcompound);
    setCarriedId(nbttagcompound.getShort("carried"));
    setCarriedData(nbttagcompound.getShort("carriedData"));
  }

  protected Entity findTarget() {
    EntityHuman entityhuman = this.world.findNearbyVulnerablePlayer(this, 64.0D);

    if (entityhuman != null) {
      if (d(entityhuman)) {
        if (this.f == 0) {
          this.world.makeSound(entityhuman, "mob.endermen.stare", 1.0F, 1.0F);
        }

        if (this.f++ == 5) {
          this.f = 0;
          f(true);
          return entityhuman;
        }
      } else {
        this.f = 0;
      }
    }

    return null;
  }

  private boolean d(EntityHuman entityhuman) {
    ItemStack itemstack = entityhuman.inventory.armor[3];

    if ((itemstack != null) && (itemstack.id == Block.PUMPKIN.id)) {
      return false;
    }
    Vec3D vec3d = entityhuman.i(1.0F).a();
    Vec3D vec3d1 = this.world.getVec3DPool().create(this.locX - entityhuman.locX, this.boundingBox.b + this.length / 2.0F - (entityhuman.locY + entityhuman.getHeadHeight()), this.locZ - entityhuman.locZ);
    double d0 = vec3d1.b();

    vec3d1 = vec3d1.a();
    double d1 = vec3d.b(vec3d1);

    return d1 > 1.0D - 0.025D / d0 ? entityhuman.m(this) : false;
  }

  public void c()
  {
    if (G()) {
      damageEntity(DamageSource.DROWN, 1);
    }

    this.bI = (this.target != null ? 6.5F : 0.3F);

    if ((!this.world.isStatic) && (this.world.getGameRules().getBoolean("mobGriefing")))
    {
      if (getCarriedId() == 0) {
        if (this.random.nextInt(20) == 0) {
          int i = MathHelper.floor(this.locX - 2.0D + this.random.nextDouble() * 4.0D);
          int j = MathHelper.floor(this.locY + this.random.nextDouble() * 3.0D);
          int k = MathHelper.floor(this.locZ - 2.0D + this.random.nextDouble() * 4.0D);
          int l = this.world.getTypeId(i, j, k);
          if (d[l])
          {
            if (!CraftEventFactory.callEntityChangeBlockEvent(this, this.world.getWorld().getBlockAt(i, j, k), org.bukkit.Material.AIR).isCancelled()) {
              setCarriedId(this.world.getTypeId(i, j, k));
              setCarriedData(this.world.getData(i, j, k));
              this.world.setTypeId(i, j, k, 0);
            }
          }
        }
      }
      else if (this.random.nextInt(2000) == 0) {
        int i = MathHelper.floor(this.locX - 1.0D + this.random.nextDouble() * 2.0D);
        int j = MathHelper.floor(this.locY + this.random.nextDouble() * 2.0D);
        int k = MathHelper.floor(this.locZ - 1.0D + this.random.nextDouble() * 2.0D);
        int l = this.world.getTypeId(i, j, k);
        int i1 = this.world.getTypeId(i, j - 1, k);

        if ((l == 0) && (i1 > 0) && (Block.byId[i1].b()))
        {
          org.bukkit.block.Block bblock = this.world.getWorld().getBlockAt(i, j, k);

          if (!CraftEventFactory.callEntityChangeBlockEvent(this, bblock, bblock.getType()).isCancelled()) {
            this.world.setTypeIdAndData(i, j, k, getCarriedId(), getCarriedData());
            setCarriedId(0);
          }
        }
      }

    }

    for (int i = 0; i < 2; i++) {
      this.world.addParticle("portal", this.locX + (this.random.nextDouble() - 0.5D) * this.width, this.locY + this.random.nextDouble() * this.length - 0.25D, this.locZ + (this.random.nextDouble() - 0.5D) * this.width, (this.random.nextDouble() - 0.5D) * 2.0D, -this.random.nextDouble(), (this.random.nextDouble() - 0.5D) * 2.0D);
    }

    if ((this.world.t()) && (!this.world.isStatic)) {
      float f = c(1.0F);

      if ((f > 0.5F) && (this.world.j(MathHelper.floor(this.locX), MathHelper.floor(this.locY), MathHelper.floor(this.locZ))) && (this.random.nextFloat() * 30.0F < (f - 0.4F) * 2.0F)) {
        this.target = null;
        f(false);
        m();
      }
    }

    if (G()) {
      this.target = null;
      f(false);
      m();
    }

    this.bG = false;
    if (this.target != null) {
      a(this.target, 100.0F, 100.0F);
    }

    if ((!this.world.isStatic) && (isAlive())) {
      if (this.target != null) {
        if (((this.target instanceof EntityHuman)) && (d((EntityHuman)this.target))) {
          this.bD = (this.bE = 0.0F);
          this.bI = 0.0F;
          if (this.target.e(this) < 16.0D) {
            m();
          }

          this.e = 0;
        } else if ((this.target.e(this) > 256.0D) && (this.e++ >= 30) && (o(this.target))) {
          this.e = 0;
        }
      } else {
        f(false);
        this.e = 0;
      }
    }

    super.c();
  }

  protected boolean m() {
    double d0 = this.locX + (this.random.nextDouble() - 0.5D) * 64.0D;
    double d1 = this.locY + (this.random.nextInt(64) - 32);
    double d2 = this.locZ + (this.random.nextDouble() - 0.5D) * 64.0D;

    return j(d0, d1, d2);
  }

  protected boolean o(Entity entity) {
    Vec3D vec3d = this.world.getVec3DPool().create(this.locX - entity.locX, this.boundingBox.b + this.length / 2.0F - entity.locY + entity.getHeadHeight(), this.locZ - entity.locZ);

    vec3d = vec3d.a();
    double d0 = 16.0D;
    double d1 = this.locX + (this.random.nextDouble() - 0.5D) * 8.0D - vec3d.c * d0;
    double d2 = this.locY + (this.random.nextInt(16) - 8) - vec3d.d * d0;
    double d3 = this.locZ + (this.random.nextDouble() - 0.5D) * 8.0D - vec3d.e * d0;

    return j(d1, d2, d3);
  }

  protected boolean j(double d0, double d1, double d2) {
    double d3 = this.locX;
    double d4 = this.locY;
    double d5 = this.locZ;

    this.locX = d0;
    this.locY = d1;
    this.locZ = d2;
    boolean flag = false;
    int i = MathHelper.floor(this.locX);
    int j = MathHelper.floor(this.locY);
    int k = MathHelper.floor(this.locZ);

    if (this.world.isLoaded(i, j, k)) {
      boolean flag1 = false;

      while ((!flag1) && (j > 0)) {
        int l = this.world.getTypeId(i, j - 1, k);
        if ((l != 0) && (Block.byId[l].material.isSolid())) {
          flag1 = true; continue;
        }
        this.locY -= 1.0D;
        j--;
      }

      if (flag1)
      {
        EntityTeleportEvent teleport = new EntityTeleportEvent(getBukkitEntity(), new Location(this.world.getWorld(), d3, d4, d5), new Location(this.world.getWorld(), this.locX, this.locY, this.locZ));
        this.world.getServer().getPluginManager().callEvent(teleport);
        if (teleport.isCancelled()) {
          return false;
        }

        Location to = teleport.getTo();
        setPosition(to.getX(), to.getY(), to.getZ());

        if ((this.world.getCubes(this, this.boundingBox).isEmpty()) && (!this.world.containsLiquid(this.boundingBox))) {
          flag = true;
        }
      }
    }

    if (!flag) {
      setPosition(d3, d4, d5);
      return false;
    }
    short short1 = 128;

    for (int l = 0; l < short1; l++) {
      double d6 = l / (short1 - 1.0D);
      float f = (this.random.nextFloat() - 0.5F) * 0.2F;
      float f1 = (this.random.nextFloat() - 0.5F) * 0.2F;
      float f2 = (this.random.nextFloat() - 0.5F) * 0.2F;
      double d7 = d3 + (this.locX - d3) * d6 + (this.random.nextDouble() - 0.5D) * this.width * 2.0D;
      double d8 = d4 + (this.locY - d4) * d6 + this.random.nextDouble() * this.length;
      double d9 = d5 + (this.locZ - d5) * d6 + (this.random.nextDouble() - 0.5D) * this.width * 2.0D;

      this.world.addParticle("portal", d7, d8, d9, f, f1, f2);
    }

    this.world.makeSound(d3, d4, d5, "mob.endermen.portal", 1.0F, 1.0F);
    this.world.makeSound(this, "mob.endermen.portal", 1.0F, 1.0F);
    return true;
  }

  protected String aW()
  {
    return q() ? "mob.endermen.scream" : "mob.endermen.idle";
  }

  protected String aX() {
    return "mob.endermen.hit";
  }

  protected String aY() {
    return "mob.endermen.death";
  }

  protected int getLootId() {
    return Item.ENDER_PEARL.id;
  }

  protected void dropDeathLoot(boolean flag, int i) {
    int j = getLootId();

    if (j > 0)
    {
      List loot = new ArrayList();
      int count = this.random.nextInt(2 + i);

      if ((j > 0) && (count > 0)) {
        loot.add(new org.bukkit.inventory.ItemStack(j, count));
      }

      CraftEventFactory.callEntityDeathEvent(this, loot);
    }
  }

  public void setCarriedId(int i)
  {
    this.datawatcher.watch(16, Byte.valueOf((byte)(i & 0xFF)));
  }

  public int getCarriedId() {
    return this.datawatcher.getByte(16);
  }

  public void setCarriedData(int i) {
    this.datawatcher.watch(17, Byte.valueOf((byte)(i & 0xFF)));
  }

  public int getCarriedData() {
    return this.datawatcher.getByte(17);
  }

  public boolean damageEntity(DamageSource damagesource, int i) {
    if ((damagesource instanceof EntityDamageSourceIndirect)) {
      for (int j = 0; j < 64; j++) {
        if (m()) {
          return true;
        }
      }

      return false;
    }
    if ((damagesource.getEntity() instanceof EntityHuman)) {
      f(true);
    }

    return super.damageEntity(damagesource, i);
  }

  public boolean q()
  {
    return this.datawatcher.getByte(18) > 0;
  }

  public void f(boolean flag) {
    this.datawatcher.watch(18, Byte.valueOf((byte)(flag ? 1 : 0)));
  }

  public int c(Entity entity) {
    return 7;
  }

  static {
    d[Block.GRASS.id] = true;
    d[Block.DIRT.id] = true;
    d[Block.SAND.id] = true;
    d[Block.GRAVEL.id] = true;
    d[Block.YELLOW_FLOWER.id] = true;
    d[Block.RED_ROSE.id] = true;
    d[Block.BROWN_MUSHROOM.id] = true;
    d[Block.RED_MUSHROOM.id] = true;
    d[Block.TNT.id] = true;
    d[Block.CACTUS.id] = true;
    d[Block.CLAY.id] = true;
    d[Block.PUMPKIN.id] = true;
    d[Block.MELON.id] = true;
    d[Block.MYCEL.id] = true;
  }
}