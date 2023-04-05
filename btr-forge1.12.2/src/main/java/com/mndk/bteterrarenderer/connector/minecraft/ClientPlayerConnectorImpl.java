package com.mndk.bteterrarenderer.connector.minecraft;

import com.mndk.bteterrarenderer.connector.ConnectorImpl;
import net.minecraft.client.Minecraft;

@ConnectorImpl
public class ClientPlayerConnectorImpl implements ClientPlayerConnector {
    public double getRotationYaw() {
        return Minecraft.getMinecraft().player.rotationYaw;
    }
}