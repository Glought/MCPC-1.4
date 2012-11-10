package cpw.mods.fml.relauncher;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minecraft.server.DedicatedServer;
import net.minecraft.server.MinecraftServer;

import org.bukkit.craftbukkit.Main;
import org.bukkit.craftbukkit.libs.jline.console.ConsoleReader;
import org.bukkit.craftbukkit.util.TerminalConsoleHandler;

public class FMLRelaunchLog$ConsoleHandler extends ConsoleHandler 
{
	public static ConsoleReader reader = null;
	private static Method preCall;
	private static Method postCall;
	
	public synchronized void flush()
	{
		/*
		if (!(this.getClass().getClassLoader() instanceof RelaunchClassLoader))
			return;
		*/
		if (preCall == null)
		{
			try 
			{
				preCall = FMLRelauncher.instance().classLoader.loadClass("net.minecraft.server.FMLLogJLineBreakProxy").getMethod("consoleReaderResetPreLog", new Class[0]);
			} 
			catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) 
			{
				e.printStackTrace();
			}
			try 
			{
				postCall = FMLRelauncher.instance().classLoader.loadClass("net.minecraft.server.FMLLogJLineBreakProxy").getMethod("consoleReaderResetPostLog", new Class[0]);
			} 
			catch (NoSuchMethodException | SecurityException | ClassNotFoundException e) 
			{
				e.printStackTrace();
			}
		}
			
		try {
			if (preCall != null)
				preCall.invoke(null);
			
			super.flush();
			
			if (postCall != null)
				postCall.invoke(null);

		} catch (Exception ex) {
			Logger.getLogger(TerminalConsoleHandler.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}



