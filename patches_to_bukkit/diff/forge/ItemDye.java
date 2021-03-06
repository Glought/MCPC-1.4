package net.minecraft.server;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;
import java.util.List;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event$Result;
import net.minecraftforge.event.entity.player.BonemealEvent;

public class ItemDye extends Item
{
    /** List of dye color names */
    public static final String[] a = new String[] {"black", "red", "green", "brown", "blue", "purple", "cyan", "silver", "gray", "pink", "lime", "yellow", "lightBlue", "magenta", "orange", "white"};
    public static final int[] b = new int[] {1973019, 11743532, 3887386, 5320730, 2437522, 8073150, 2651799, 2651799, 4408131, 14188952, 4312372, 14602026, 6719955, 12801229, 15435844, 15790320};

    public ItemDye(int var1)
    {
        super(var1);
        this.a(true);
        this.setMaxDurability(0);
        this.a(CreativeModeTab.l);
    }

    @SideOnly(Side.CLIENT)
    public int b(int var1)
    {
        int var2 = MathHelper.a(var1, 0, 15);
        return this.textureId + var2 % 8 * 16 + var2 / 8;
    }

    public String c_(ItemStack var1)
    {
        int var2 = MathHelper.a(var1.getData(), 0, 15);
        return super.getName() + "." + a[var2];
    }

    /**
     * Callback for item usage. If the item does something special on right clicking, he will have one of those. Return
     * True if something happen and false if it don't. This is for ITEMS, not BLOCKS
     */
    public boolean interactWith(ItemStack var1, EntityHuman var2, World var3, int var4, int var5, int var6, int var7, float var8, float var9, float var10)
    {
        if (!var2.func_82247_a(var4, var5, var6, var7, var1))
        {
            return false;
        }
        else
        {
            int var11;
            int var12;

            if (var1.getData() == 15)
            {
                var11 = var3.getTypeId(var4, var5, var6);
                BonemealEvent var13 = new BonemealEvent(var2, var3, var11, var4, var5, var6);

                if (MinecraftForge.EVENT_BUS.post(var13))
                {
                    return false;
                }

                if (var13.getResult() == Event$Result.ALLOW)
                {
                    if (!var3.isStatic)
                    {
                        --var1.count;
                    }

                    return true;
                }

                if (var11 == Block.SAPLING.id)
                {
                    if (!var3.isStatic)
                    {
                        ((BlockSapling)Block.SAPLING).grow(var3, var4, var5, var6, var3.random);
                        --var1.count;
                    }

                    return true;
                }

                if (var11 == Block.BROWN_MUSHROOM.id || var11 == Block.RED_MUSHROOM.id)
                {
                    if (!var3.isStatic && ((BlockMushroom)Block.byId[var11]).grow(var3, var4, var5, var6, var3.random))
                    {
                        --var1.count;
                    }

                    return true;
                }

                if (var11 == Block.MELON_STEM.id || var11 == Block.PUMPKIN_STEM.id)
                {
                    if (var3.getData(var4, var5, var6) == 7)
                    {
                        return false;
                    }

                    if (!var3.isStatic)
                    {
                        ((BlockStem)Block.byId[var11]).l(var3, var4, var5, var6);
                        --var1.count;
                    }

                    return true;
                }

                if (var11 > 0 && Block.byId[var11] instanceof BlockCrops)
                {
                    if (var3.getData(var4, var5, var6) == 7)
                    {
                        return false;
                    }

                    if (!var3.isStatic)
                    {
                        ((BlockCrops)Block.byId[var11]).c_(var3, var4, var5, var6);
                        --var1.count;
                    }

                    return true;
                }

                if (var11 == Block.COCOA.id)
                {
                    if (!var3.isStatic)
                    {
                        var3.setData(var4, var5, var6, 8 | BlockDirectional.e(var3.getData(var4, var5, var6)));
                        --var1.count;
                    }

                    return true;
                }

                if (var11 == Block.GRASS.id)
                {
                    if (!var3.isStatic)
                    {
                        --var1.count;
                        label139:

                        for (var12 = 0; var12 < 128; ++var12)
                        {
                            int var14 = var4;
                            int var15 = var5 + 1;
                            int var16 = var6;

                            for (int var17 = 0; var17 < var12 / 16; ++var17)
                            {
                                var14 += d.nextInt(3) - 1;
                                var15 += (d.nextInt(3) - 1) * d.nextInt(3) / 2;
                                var16 += d.nextInt(3) - 1;

                                if (var3.getTypeId(var14, var15 - 1, var16) != Block.GRASS.id || var3.s(var14, var15, var16))
                                {
                                    continue label139;
                                }
                            }

                            if (var3.getTypeId(var14, var15, var16) == 0)
                            {
                                if (d.nextInt(10) != 0)
                                {
                                    if (Block.LONG_GRASS.d(var3, var14, var15, var16))
                                    {
                                        var3.setTypeIdAndData(var14, var15, var16, Block.LONG_GRASS.id, 1);
                                    }
                                }
                                else
                                {
                                    ForgeHooks.plantGrass(var3, var14, var15, var16);
                                }
                            }
                        }
                    }

                    return true;
                }
            }
            else if (var1.getData() == 3)
            {
                var11 = var3.getTypeId(var4, var5, var6);
                var12 = var3.getData(var4, var5, var6);

                if (var11 == Block.LOG.id && BlockLog.e(var12) == 3)
                {
                    if (var7 == 0)
                    {
                        return false;
                    }

                    if (var7 == 1)
                    {
                        return false;
                    }

                    if (var7 == 2)
                    {
                        --var6;
                    }

                    if (var7 == 3)
                    {
                        ++var6;
                    }

                    if (var7 == 4)
                    {
                        --var4;
                    }

                    if (var7 == 5)
                    {
                        ++var4;
                    }

                    if (var3.isEmpty(var4, var5, var6))
                    {
                        var3.setTypeId(var4, var5, var6, Block.COCOA.id);

                        if (var3.getTypeId(var4, var5, var6) == Block.COCOA.id)
                        {
                            Block.byId[Block.COCOA.id].postPlace(var3, var4, var5, var6, var7, var8, var9, var10);
                        }

                        if (!var2.abilities.canInstantlyBuild)
                        {
                            --var1.count;
                        }
                    }

                    return true;
                }
            }

            return false;
        }
    }

    /**
     * Called when a player right clicks a entity with a item.
     */
    public boolean a(ItemStack var1, EntityLiving var2)
    {
        if (var2 instanceof EntitySheep)
        {
            EntitySheep var3 = (EntitySheep)var2;
            int var4 = BlockCloth.e_(var1.getData());

            if (!var3.isSheared() && var3.getColor() != var4)
            {
                var3.setColor(var4);
                --var1.count;
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    @SideOnly(Side.CLIENT)
    public void a(int var1, CreativeModeTab var2, List var3)
    {
        for (int var4 = 0; var4 < 16; ++var4)
        {
            var3.add(new ItemStack(var1, 1, var4));
        }
    }
}
