package cpw.mods.fml.common.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.network.FMLPacket$Type;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.EntityRegistry$EntityRegistration;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.common.registry.IThrowableEntity;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import net.minecraft.server.DataWatcher;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.INetworkManager;
import net.minecraft.server.MathHelper;
import net.minecraft.server.NetHandler;

public class EntitySpawnPacket extends FMLPacket
{
    public int networkId;
    public int modEntityId;
    public int entityId;
    public double scaledX;
    public double scaledY;
    public double scaledZ;
    public float scaledYaw;
    public float scaledPitch;
    public float scaledHeadYaw;
    public List metadata;
    public int throwerId;
    public double speedScaledX;
    public double speedScaledY;
    public double speedScaledZ;
    public ByteArrayDataInput dataStream;
    public int rawX;
    public int rawY;
    public int rawZ;

    public EntitySpawnPacket()
    {
        super(FMLPacket$Type.ENTITYSPAWN);
    }

    public byte[] generatePacket(Object ... var1)
    {
        EntityRegistry$EntityRegistration var2 = (EntityRegistry$EntityRegistration)var1[0];
        Entity var3 = (Entity)var1[1];
        NetworkModHandler var4 = (NetworkModHandler)var1[2];
        ByteArrayDataOutput var5 = ByteStreams.newDataOutput();
        var5.writeInt(var4.getNetworkId());
        var5.writeInt(var2.getModEntityId());
        var5.writeInt(var3.id);
        var5.writeInt(MathHelper.floor(var3.locX * 32.0D));
        var5.writeInt(MathHelper.floor(var3.locY * 32.0D));
        var5.writeInt(MathHelper.floor(var3.locZ * 32.0D));
        var5.writeByte((byte)((int)(var3.yaw * 256.0F / 360.0F)));
        var5.writeByte((byte)((int)(var3.pitch * 256.0F / 360.0F)));

        if (var3 instanceof EntityLiving)
        {
            var5.writeByte((byte)((int)(((EntityLiving)var3).ay * 256.0F / 360.0F)));
        }
        else
        {
            var5.writeByte(0);
        }

        ByteArrayOutputStream var6 = new ByteArrayOutputStream();
        DataOutputStream var7 = new DataOutputStream(var6);

        var3.getDataWatcher().a(var7);

        var5.write(var6.toByteArray());

        if (var3 instanceof IThrowableEntity)
        {
            Entity var8 = ((IThrowableEntity)var3).getThrower();
            var5.writeInt(var8 == null ? var3.id : var8.id);
            double var9 = 3.9D;
            double var11 = var3.motX;
            double var13 = var3.motY;
            double var15 = var3.motZ;

            if (var11 < -var9)
            {
                var11 = -var9;
            }

            if (var13 < -var9)
            {
                var13 = -var9;
            }

            if (var15 < -var9)
            {
                var15 = -var9;
            }

            if (var11 > var9)
            {
                var11 = var9;
            }

            if (var13 > var9)
            {
                var13 = var9;
            }

            if (var15 > var9)
            {
                var15 = var9;
            }

            var5.writeInt((int)(var11 * 8000.0D));
            var5.writeInt((int)(var13 * 8000.0D));
            var5.writeInt((int)(var15 * 8000.0D));
        }
        else
        {
            var5.writeInt(0);
        }

        if (var3 instanceof IEntityAdditionalSpawnData)
        {
            ((IEntityAdditionalSpawnData)var3).writeSpawnData(var5);
        }

        return var5.toByteArray();
    }

    public FMLPacket consumePacket(byte[] var1)
    {
        ByteArrayDataInput var2 = ByteStreams.newDataInput(var1);
        this.networkId = var2.readInt();
        this.modEntityId = var2.readInt();
        this.entityId = var2.readInt();
        this.rawX = var2.readInt();
        this.rawY = var2.readInt();
        this.rawZ = var2.readInt();
        this.scaledX = (double)this.rawX / 32.0D;
        this.scaledY = (double)this.rawY / 32.0D;
        this.scaledZ = (double)this.rawZ / 32.0D;
        this.scaledYaw = (float)var2.readByte() * 360.0F / 256.0F;
        this.scaledPitch = (float)var2.readByte() * 360.0F / 256.0F;
        this.scaledHeadYaw = (float)var2.readByte() * 360.0F / 256.0F;
        ByteArrayInputStream var3 = new ByteArrayInputStream(var1, 27, var1.length - 27);
        DataInputStream var4 = new DataInputStream(var3);

        this.metadata = DataWatcher.a(var4);

        var2.skipBytes(var1.length - var3.available() - 27);
        this.throwerId = var2.readInt();

        if (this.throwerId != 0)
        {
            this.speedScaledX = (double)var2.readInt() / 8000.0D;
            this.speedScaledY = (double)var2.readInt() / 8000.0D;
            this.speedScaledZ = (double)var2.readInt() / 8000.0D;
        }

        this.dataStream = var2;
        return this;
    }

    public void execute(INetworkManager var1, FMLNetworkHandler var2, NetHandler var3, String var4)
    {
        NetworkModHandler var5 = var2.findNetworkModHandler(Integer.valueOf(this.networkId));
        ModContainer var6 = var5.getContainer();
        EntityRegistry$EntityRegistration var7 = EntityRegistry.instance().lookupModSpawn(var6, this.modEntityId);
        Class var8 = var7.getEntityClass();

        if (var8 == null)
        {
            FMLLog.log(Level.WARNING, "Missing mod entity information for %s : %d", new Object[] {var6.getModId(), Integer.valueOf(this.modEntityId)});
        }
        else
        {
            FMLCommonHandler.instance().spawnEntityIntoClientWorld(var7, this);
        }
    }
}
