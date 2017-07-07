package net.arez0101.tabf;

import net.arez0101.tabf.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = TABF.MODID, name = TABF.NAME, version = TABF.VERSION)
public class TABF {
	
	public static final String MODID = "tabf";
	public static final String NAME = "That's A Big Furnace";
	public static final String VERSION = "0.1.0";
	public static final String COMMON_PROXY = "net.arez0101.tabf.proxy.CommonProxy";
	public static final String CLIENT_PROXY = "net.arez0101.tabf.proxy.ClientProxy";
	
	@Mod.Instance(MODID)
	public static TABF INSTANCE;
	
	@SidedProxy(serverSide = COMMON_PROXY, clientSide = CLIENT_PROXY)
	public static CommonProxy PROXY;
	
	@EventHandler
	public static void preInit(FMLPreInitializationEvent event) {
		PROXY.preInit(event);
	}
	
	@EventHandler
	public static void init(FMLInitializationEvent event) {
		PROXY.init(event);
	}
	
	@EventHandler
	public static void postInit(FMLPostInitializationEvent event) {
		PROXY.postInit(event);
	}
}
