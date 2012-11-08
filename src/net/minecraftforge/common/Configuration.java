package net.minecraftforge.common;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.relauncher.FMLInjectionData;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.server.Block;
import net.minecraft.server.Item;
import net.minecraftforge.common.Configuration$UnicodeInputStreamReader;
import net.minecraftforge.common.Property$Type;

public class Configuration
{
    private static boolean[] configBlocks = new boolean[Block.byId.length];
    private static boolean[] configItems = new boolean[Item.byId.length];
    private static final int ITEM_SHIFT = 256;
    public static final String CATEGORY_GENERAL = "general";
    public static final String CATEGORY_BLOCK = "block";
    public static final String CATEGORY_ITEM = "item";
    public static final String ALLOWED_CHARS = "._-";
    public static final String DEFAULT_ENCODING = "UTF-8";
    private static final Pattern CONFIG_START = Pattern.compile("START: \"([^\\\"]+)\"");
    private static final Pattern CONFIG_END = Pattern.compile("END: \"([^\\\"]+)\"");
    private static final CharMatcher allowedProperties = CharMatcher.JAVA_LETTER_OR_DIGIT.or(CharMatcher.anyOf("._-"));
    private static Configuration PARENT = null;
    File file;
    public Map categories;
    private Map children;
    private Map customCategoryComments;
    private boolean caseSensitiveCustomCategories;
    public String defaultEncoding;
    private String fileName;
    public boolean isChild;

    public Configuration()
    {
        this.categories = new TreeMap();
        this.children = new TreeMap();
        this.customCategoryComments = Maps.newHashMap();
        this.defaultEncoding = "UTF-8";
        this.fileName = null;
        this.isChild = false;
    }

    public Configuration(File var1)
    {
        this.categories = new TreeMap();
        this.children = new TreeMap();
        this.customCategoryComments = Maps.newHashMap();
        this.defaultEncoding = "UTF-8";
        this.fileName = null;
        this.isChild = false;
        this.file = var1;
        String var2 = ((File)((File)FMLInjectionData.data()[6])).getAbsolutePath().replace(File.separatorChar, '/').replace("/.", "");
        String var3 = var1.getAbsolutePath().replace(File.separatorChar, '/').replace("/./", "/").replace(var2, "");

        if (PARENT != null)
        {
            PARENT.setChild(var3, this);
            this.isChild = true;
        }
        else
        {
            this.load();
        }
    }

    public Configuration(File var1, boolean var2)
    {
        this(var1);
        this.caseSensitiveCustomCategories = var2;
    }

    public Property getBlock(String var1, int var2)
    {
        return this.getBlock("block", var1, var2);
    }

    public Property getBlock(String var1, String var2, int var3)
    {
        Property var4 = this.get(var1, var2, -1);

        if (var4.getInt() != -1)
        {
            configBlocks[var4.getInt()] = true;
            return var4;
        }
        else if (Block.byId[var3] == null && !configBlocks[var3])
        {
            var4.value = Integer.toString(var3);
            configBlocks[var3] = true;
            return var4;
        }
        else
        {
            for (int var5 = configBlocks.length - 1; var5 > 0; --var5)
            {
                if (Block.byId[var5] == null && !configBlocks[var5])
                {
                    var4.value = Integer.toString(var5);
                    configBlocks[var5] = true;
                    return var4;
                }
            }

            throw new RuntimeException("No more block ids available for " + var2);
        }
    }

    public Property getItem(String var1, int var2)
    {
        return this.getItem("item", var1, var2);
    }

    public Property getItem(String var1, String var2, int var3)
    {
        Property var4 = this.get(var1, var2, -1);
        int var5 = var3 + 256;

        if (var4.getInt() != -1)
        {
            configItems[var4.getInt() + 256] = true;
            return var4;
        }
        else if (Item.byId[var5] == null && !configItems[var5] && var5 > Block.byId.length)
        {
            var4.value = Integer.toString(var3);
            configItems[var5] = true;
            return var4;
        }
        else
        {
            for (int var6 = configItems.length - 1; var6 >= 256; --var6)
            {
                if (Item.byId[var6] == null && !configItems[var6])
                {
                    var4.value = Integer.toString(var6 - 256);
                    configItems[var6] = true;
                    return var4;
                }
            }

            throw new RuntimeException("No more item ids available for " + var2);
        }
    }

    public Property get(String var1, String var2, int var3)
    {
        Property var4 = this.get(var1, var2, Integer.toString(var3), Property$Type.INTEGER);

        if (!var4.isIntValue())
        {
            var4.value = Integer.toString(var3);
        }

        return var4;
    }

    public Property get(String var1, String var2, boolean var3)
    {
        Property var4 = this.get(var1, var2, Boolean.toString(var3), Property$Type.BOOLEAN);

        if (!var4.isBooleanValue())
        {
            var4.value = Boolean.toString(var3);
        }

        return var4;
    }

    public Property get(String var1, String var2, String var3)
    {
        return this.get(var1, var2, var3, Property$Type.STRING);
    }

    public Property get(String var1, String var2, String var3, Property$Type var4)
    {
        if (!this.caseSensitiveCustomCategories)
        {
            var1 = var1.toLowerCase(Locale.ENGLISH);
        }

        Object var5 = (Map)this.categories.get(var1);

        if (var5 == null)
        {
            var5 = new TreeMap();
            this.categories.put(var1, var5);
        }

        if (((Map)var5).containsKey(var2))
        {
            return (Property)((Map)var5).get(var2);
        }
        else if (var3 != null)
        {
            Property var6 = new Property(var2, var3, var4);
            ((Map)var5).put(var2, var6);
            return var6;
        }
        else
        {
            return null;
        }
    }

    public boolean hasCategory(String var1)
    {
        return this.categories.get(var1) != null;
    }

    public boolean hasKey(String var1, String var2)
    {
        Map var3 = (Map)this.categories.get(var1);
        return var3 != null && var3.get(var2) != null;
    }

  public void load()
    {
        if (PARENT == null || PARENT == this)
        {
            BufferedReader var1 = null;

            try
            {
                if (this.file.getParentFile() != null)
                {
                    this.file.getParentFile().mkdirs();
                }

                if (!this.file.exists() && !this.file.createNewFile())
                {
                    return;
                }

                if (this.file.canRead())
                {
                    Configuration$UnicodeInputStreamReader var2 = new Configuration$UnicodeInputStreamReader(new FileInputStream(this.file), this.defaultEncoding);
                    this.defaultEncoding = var2.getEncoding();
                    var1 = new BufferedReader(var2);
                    Object var4 = null;

                    while (true)
                    {
                        String var3 = var1.readLine();

                        if (var3 == null)
                        {
                            break;
                        }

                        Matcher var5 = CONFIG_START.matcher(var3);
                        Matcher var6 = CONFIG_END.matcher(var3);

                        if (var5.matches())
                        {
                            this.fileName = var5.group(1);
                            this.categories = new TreeMap();
                            this.customCategoryComments = Maps.newHashMap();
                        }
                        else if (var6.matches())
                        {
                            this.fileName = var6.group(1);
                            Configuration var26 = new Configuration();
                            var26.categories = this.categories;
                            var26.customCategoryComments = this.customCategoryComments;
                            this.children.put(this.fileName, var26);
                        }
                        else
                        {
                            int var7 = -1;
                            int var8 = -1;
                            boolean var9 = false;
                            boolean var10 = false;

                            for (int var11 = 0; var11 < var3.length() && !var9; ++var11)
                            {
                                if (!Character.isLetterOrDigit(var3.charAt(var11)) && "._-".indexOf(var3.charAt(var11)) == -1 && (!var10 || var3.charAt(var11) == 34))
                                {
                                    if (!Character.isWhitespace(var3.charAt(var11)))
                                    {
                                        switch (var3.charAt(var11))
                                        {
                                            case 34:
                                                if (var10)
                                                {
                                                    var10 = false;
                                                }

                                                if (!var10 && var7 == -1)
                                                {
                                                    var10 = true;
                                                }

                                                break;

                                            case 35:
                                                var9 = true;
                                                break;

                                            case 61:
                                                String var13 = var3.substring(var7, var8 + 1);

                                                if (var4 == null)
                                                {
                                                    throw new RuntimeException("property " + var13 + " has no scope");
                                                }

                                                Property var14 = new Property();
                                                var14.setName(var13);
                                                var14.value = var3.substring(var11 + 1);
                                                var11 = var3.length();
                                                ((Map)var4).put(var13, var14);
                                                break;

                                            case 123:
                                                String var12 = var3.substring(var7, var8 + 1);
                                                var4 = (Map)this.categories.get(var12);

                                                if (var4 == null)
                                                {
                                                    var4 = new TreeMap();
                                                    this.categories.put(var12, var4);
                                                }

                                                break;

                                            case 125:
                                                var4 = null;
                                                break;

                                            default:
                                                throw new RuntimeException("unknown character " + var3.charAt(var11));
                                        }
                                    }
                                }
                                else
                                {
                                    if (var7 == -1)
                                    {
                                        var7 = var11;
                                    }

                                    var8 = var11;
                                }
                            }

                            if (var10)
                            {
                                throw new RuntimeException("unmatched quote");
                            }
                        }
                    }
                }
            }
            catch (IOException var24)
            {
                var24.printStackTrace();
            }
            finally
            {
                if (var1 != null)
                {
                    try
                    {
                        var1.close();
                    }
                    catch (IOException var23)
                    {
                        ;
                    }
                }
            }
        }
    }
  
    public void save()
    {
        if (PARENT != null && PARENT != this)
        {
            PARENT.save();
        }
        else
        {
            try
            {
                if (this.file.getParentFile() != null)
                {
                    this.file.getParentFile().mkdirs();
                }

                if (!this.file.exists() && !this.file.createNewFile())
                {
                    return;
                }

                if (this.file.canWrite())
                {
                    FileOutputStream var1 = new FileOutputStream(this.file);
                    BufferedWriter var2 = new BufferedWriter(new OutputStreamWriter(var1, this.defaultEncoding));
                    var2.write("# Configuration file\r\n");
                    var2.write("# Generated on " + DateFormat.getInstance().format(new Date()) + "\r\n");
                    var2.write("\r\n");

                    if (this.children.isEmpty())
                    {
                        this.save(var2);
                    }
                    else
                    {
                        Iterator var3 = this.children.entrySet().iterator();

                        while (var3.hasNext())
                        {
                            Entry var4 = (Entry)var3.next();
                            var2.write("START: \"" + (String)var4.getKey() + "\"\r\n");
                            ((Configuration)var4.getValue()).save(var2);
                            var2.write("END: \"" + (String)var4.getKey() + "\"\r\n\r\n");
                        }
                    }

                    var2.close();
                    var1.close();
                }
            }
            catch (IOException var5)
            {
                var5.printStackTrace();
            }
        }
    }

    private void save(BufferedWriter var1) throws IOException
    {
        Iterator var2 = this.categories.entrySet().iterator();

        while (var2.hasNext())
        {
            Entry var3 = (Entry)var2.next();
            var1.write("####################\r\n");
            var1.write("# " + (String)var3.getKey() + " \r\n");
            String var4;

            if (this.customCategoryComments.containsKey(var3.getKey()))
            {
                var1.write("#===================\r\n");
                var4 = (String)this.customCategoryComments.get(var3.getKey());
                Splitter var5 = Splitter.onPattern("\r?\n");
                Iterator var6 = var5.split(var4).iterator();

                while (var6.hasNext())
                {
                    String var7 = (String)var6.next();
                    var1.write("# ");
                    var1.write(var7 + "\r\n");
                }
            }

            var1.write("####################\r\n\r\n");
            var4 = (String)var3.getKey();

            if (!allowedProperties.matchesAllOf(var4))
            {
                var4 = '\"' + var4 + '\"';
            }

            var1.write(var4 + " {\r\n");
            this.writeProperties(var1, ((Map)var3.getValue()).values());
            var1.write("}\r\n\r\n");
        }
    }

    public void addCustomCategoryComment(String var1, String var2)
    {
        if (!this.caseSensitiveCustomCategories)
        {
            var1 = var1.toLowerCase(Locale.ENGLISH);
        }

        this.customCategoryComments.put(var1, var2);
    }

    private void writeProperties(BufferedWriter var1, Collection var2) throws IOException
    {
        Iterator var3 = var2.iterator();

        while (var3.hasNext())
        {
            Property var4 = (Property)var3.next();

            if (var4.comment != null)
            {
                Splitter var5 = Splitter.onPattern("\r?\n");
                Iterator var6 = var5.split(var4.comment).iterator();

                while (var6.hasNext())
                {
                    String var7 = (String)var6.next();
                    var1.write("   # " + var7 + "\r\n");
                }
            }

            String var8 = var4.getName();

            if (!allowedProperties.matchesAllOf(var8))
            {
                var8 = '\"' + var8 + '\"';
            }

            var1.write("   " + var8 + "=" + var4.value);
            var1.write("\r\n");
        }
    }

    private void setChild(String var1, Configuration var2)
    {
        if (!this.children.containsKey(var1))
        {
            this.children.put(var1, var2);
        }
        else
        {
            Configuration var3 = (Configuration)this.children.get(var1);
            var2.categories = var3.categories;
            var2.customCategoryComments = var3.customCategoryComments;
            var2.fileName = var3.fileName;
        }
    }

    public static void enableGlobalConfig()
    {
        PARENT = new Configuration(new File(Loader.instance().getConfigDir(), "global.cfg"));
        PARENT.load();
    }

    static
    {
        Arrays.fill(configBlocks, false);
        Arrays.fill(configItems, false);
    }
}