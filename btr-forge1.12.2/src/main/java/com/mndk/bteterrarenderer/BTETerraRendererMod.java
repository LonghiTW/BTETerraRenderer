package com.mndk.bteterrarenderer;

import com.mndk.bteterrarenderer.connector.terraplusplus.projection.Proj4jProjectionImpl;
import com.mndk.bteterrarenderer.network.ServerWelcomeMessageImpl;
import com.mndk.bteterrarenderer.network.ServerWelcomeMsgHandler;
import com.mndk.bteterrarenderer.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(
        modid = BTETerraRendererCore.MODID,
        name = BTETerraRendererCore.NAME,
        dependencies = "required-after:terraplusplus@[1.0.569,)"
)
public class BTETerraRendererMod {
    public static final SimpleNetworkWrapper NETWORK_WRAPPER =
            NetworkRegistry.INSTANCE.newSimpleChannel(BTETerraRendererCore.MODID);

    @SidedProxy(clientSide="com.mndk.bteterrarenderer.proxy.ClientProxy", serverSide="com.mndk.bteterrarenderer.proxy.ServerProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }

    static {
        NETWORK_WRAPPER.registerMessage(ServerWelcomeMsgHandler.class, ServerWelcomeMessageImpl.class, 0, Side.CLIENT);
        Proj4jProjectionImpl.registerProjection();
    }
}
