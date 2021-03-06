package cpw.mods.fml.common.modloader;

import cpw.mods.fml.common.IWorldGenerator;
import java.util.Random;
import net.minecraft.server.ChunkProviderGenerate;
import net.minecraft.server.ChunkProviderHell;
import net.minecraft.server.IChunkProvider;
import net.minecraft.server.World;

public class ModLoaderWorldGenerator implements IWorldGenerator
{
    private BaseModProxy mod;

    public ModLoaderWorldGenerator(BaseModProxy var1)
    {
        this.mod = var1;
    }

    public void generate(Random var1, int var2, int var3, World var4, IChunkProvider var5, IChunkProvider var6)
    {
        if (var5 instanceof ChunkProviderGenerate)
        {
            this.mod.generateSurface(var4, var1, var2 << 4, var3 << 4);
        }
        else if (var5 instanceof ChunkProviderHell)
        {
            this.mod.generateNether(var4, var1, var2 << 4, var3 << 4);
        }
    }
}
