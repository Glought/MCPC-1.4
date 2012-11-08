package net.minecraftforge.common;

public class ForgeVersion
{
    public static final int majorVersion = 6;
    public static final int minorVersion = 0;
    public static final int revisionVersion = 1;
    public static final int buildVersion = 355;

    public static int getMajorVersion()
    {
        return majorVersion;
    }

    public static int getMinorVersion()
    {
        return minorVersion;
    }

    public static int getRevisionVersion()
    {
        return revisionVersion;
    }

    public static int getBuildVersion()
    {
        return buildVersion;
    }

    public static String getVersion()
    {
        return String.format("%d.%d.%d.%d", new Object[] {Integer.valueOf(getMajorVersion()), Integer.valueOf(getMinorVersion()), Integer.valueOf(getRevisionVersion()), Integer.valueOf(getBuildVersion())});
    }
}
