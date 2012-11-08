package net.minecraftforge.oredict;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import net.minecraft.server.Block;
import net.minecraft.server.Item;
import net.minecraft.server.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary$OreRegisterEvent;

public class OreDictionary
{
    private static int maxID = 0;
    private static HashMap oreIDs = new HashMap();
    private static HashMap oreStacks = new HashMap();

    public static void initVanillaEntries()
    {
        registerOre("woodLog", new ItemStack(Block.LOG, 1, 0));
        registerOre("woodLog", new ItemStack(Block.LOG, 1, 1));
        registerOre("woodLog", new ItemStack(Block.LOG, 1, 2));
        registerOre("woodLog", new ItemStack(Block.LOG, 1, 3));
        registerOre("woodPlank", new ItemStack(Block.WOOD, 1, 0));
        registerOre("woodPlank", new ItemStack(Block.WOOD, 1, 1));
        registerOre("woodPlank", new ItemStack(Block.WOOD, 1, 2));
        registerOre("woodPlank", new ItemStack(Block.WOOD, 1, 3));
        registerOre("woodSlab", new ItemStack(Block.WOOD_STEP, 1, 0));
        registerOre("woodSlab", new ItemStack(Block.WOOD_STEP, 1, 1));
        registerOre("woodSlab", new ItemStack(Block.WOOD_STEP, 1, 2));
        registerOre("woodSlab", new ItemStack(Block.WOOD_STEP, 1, 3));
        registerOre("woodStair", Block.WOOD_STAIRS);
        registerOre("woodStair", Block.BIRCH_WOOD_STAIRS);
        registerOre("woodStair", Block.JUNGLE_WOOD_STAIRS);
        registerOre("woodStair", Block.SPRUCE_WOOD_STAIRS);
        registerOre("dyeBlack", new ItemStack(Item.INK_SACK, 1, 0));
        registerOre("dyeRed", new ItemStack(Item.INK_SACK, 1, 1));
        registerOre("dyeGreen", new ItemStack(Item.INK_SACK, 1, 2));
        registerOre("dyeBrown", new ItemStack(Item.INK_SACK, 1, 3));
        registerOre("dyeBlue", new ItemStack(Item.INK_SACK, 1, 4));
        registerOre("dyePurple", new ItemStack(Item.INK_SACK, 1, 5));
        registerOre("dyeCyan", new ItemStack(Item.INK_SACK, 1, 6));
        registerOre("dyeLightGrey", new ItemStack(Item.INK_SACK, 1, 7));
        registerOre("dyeGrey", new ItemStack(Item.INK_SACK, 1, 8));
        registerOre("dyePink", new ItemStack(Item.INK_SACK, 1, 9));
        registerOre("dyeLime", new ItemStack(Item.INK_SACK, 1, 10));
        registerOre("dyeYellow", new ItemStack(Item.INK_SACK, 1, 11));
        registerOre("dyeLightBlue", new ItemStack(Item.INK_SACK, 1, 12));
        registerOre("dyeMagenta", new ItemStack(Item.INK_SACK, 1, 13));
        registerOre("dyeOrange", new ItemStack(Item.INK_SACK, 1, 14));
        registerOre("dyeWhite", new ItemStack(Item.INK_SACK, 1, 15));
    }
    
    public static int getOreID(String var0)
    {
        Integer var1 = (Integer)oreIDs.get(var0);

        if (var1 == null)
        {
            var1 = Integer.valueOf(maxID++);
            oreIDs.put(var0, var1);
            oreStacks.put(var1, new ArrayList());
        }

        return var1.intValue();
    }

    public static String getOreName(int var0)
    {
        Iterator var1 = oreIDs.entrySet().iterator();
        Entry var2;

        do
        {
            if (!var1.hasNext())
            {
                return "Unknown";
            }

            var2 = (Entry)var1.next();
        }
        while (var0 != ((Integer)var2.getValue()).intValue());

        return (String)var2.getKey();
    }

    public static ArrayList getOres(String var0)
    {
        return getOres(Integer.valueOf(getOreID(var0)));
    }

    public static String[] getOreNames()
    {
        return (String[])oreIDs.keySet().toArray(new String[0]);
    }

    public static ArrayList getOres(Integer var0)
    {
        ArrayList var1 = (ArrayList)oreStacks.get(var0);

        if (var1 == null)
        {
            var1 = new ArrayList();
            oreStacks.put(var0, var1);
        }

        return var1;
    }

    public static void registerOre(String var0, Item var1)
    {
        registerOre(var0, new ItemStack(var1));
    }

    public static void registerOre(String var0, Block var1)
    {
        registerOre(var0, new ItemStack(var1));
    }

    public static void registerOre(String var0, ItemStack var1)
    {
        registerOre(var0, getOreID(var0), var1);
    }

    public static void registerOre(int var0, Item var1)
    {
        registerOre(var0, new ItemStack(var1));
    }

    public static void registerOre(int var0, Block var1)
    {
        registerOre(var0, new ItemStack(var1));
    }

    public static void registerOre(int var0, ItemStack var1)
    {
        registerOre(getOreName(var0), var0, var1);
    }

    private static void registerOre(String var0, int var1, ItemStack var2)
    {
        ArrayList var3 = getOres(Integer.valueOf(var1));
        var2 = var2.cloneItemStack();
        var3.add(var2);
        MinecraftForge.EVENT_BUS.post(new OreDictionary$OreRegisterEvent(var0, var2));
    }
    
    static
    {
        initVanillaEntries();
    }
}
