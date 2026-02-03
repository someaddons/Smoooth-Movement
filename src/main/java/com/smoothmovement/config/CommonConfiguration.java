package com.smoothmovement.config;

import com.cupboard.config.CupboardConfig;
import com.cupboard.config.ICommonConfig;
import com.google.gson.JsonObject;
import com.smoothmovement.SmoothMovement;

public class CommonConfiguration implements ICommonConfig
{
    private static final String ENABLE_CLIENT = "enableClient";
    private static final String ENABLE_SERVER   = "enableServer";
    private static final String DESC_CLIENT     = "descClient:";
    private static final String DESC_SEVER      = "descServer";
    public static final  String ITEM_MOVEMENT   = "itemMovement";
    public static final  String ENTITY_MOVEMENT = "entityMovement";
    public static final String MINECART_MOVEMENT = "minecartMovement";
    public static final String EXPERIENCE_ORB_MOVEMENT = "experienceOrbMovement";
    public static final String FALLINGBLOCKMOVEMENT = "fallingblockmovement";
    public static final String PROJECTILE_MOVEMENT = "projectileMovement";
    public static final String SKY_MOVEMENT = "skyMovement";
    public static final String PLAYER_MOVEMENT = "playerMovement";

    public static CupboardConfig<CommonConfiguration> config = new CupboardConfig<>(SmoothMovement.MODID, new CommonConfiguration());

    public boolean enableItemSmoothing           = true;
    public boolean enableItemLagAdjustedMovement = true;

    public boolean enableLivingEntitySmoothing           = true;
    public boolean enableLivingEntityLagAdjustedMovement = true;

    public boolean enableMinecartSmoothing           = true;
    public boolean enableMinecartLagAdjustedMovement           = true;

    public boolean enableExpOrbLagAdjustedMovement = true;

    public boolean enableFallingBlockLagAdjustedMovement = true;

    public boolean enableProjectileLagAdjustedMovement = true;

    public boolean enableRubberbandingLagAdjustedMovement = true;

    public boolean enableSkySmoothing           = true;
    public boolean enableSkyLagAdjustedMovement = true;

    public CommonConfiguration()
    {
    }

    public JsonObject serialize()
    {
        final JsonObject root = new JsonObject();

        final JsonObject entry = new JsonObject();
        entry.addProperty(DESC_CLIENT, "Smooths item movement on the client during server or network lag. Default:true");
        entry.addProperty(ENABLE_CLIENT, enableItemSmoothing);
        entry.addProperty(DESC_SEVER, "Compensates item movement on the server during low TPS to preserve vanilla travel distance. Default: true");
        entry.addProperty(ENABLE_SERVER, enableItemLagAdjustedMovement);
        root.add(ITEM_MOVEMENT, entry);

        final JsonObject entry2 = new JsonObject();
        entry2.addProperty(DESC_CLIENT, "Smooths living entity movement on the client during server or network lag. Default: true");
        entry2.addProperty(ENABLE_CLIENT, enableLivingEntitySmoothing);
        entry2.addProperty(DESC_SEVER, "Compensates living entity movement on the server during low TPS to preserve vanilla behavior. Default: true");
        entry2.addProperty(ENABLE_SERVER, enableLivingEntityLagAdjustedMovement);
        root.add(ENTITY_MOVEMENT, entry2);

        final JsonObject entry3 = new JsonObject();
        entry3.addProperty(DESC_CLIENT, "Smooths minecart movement on the client during server or network lag. Default: true");
        entry3.addProperty(ENABLE_CLIENT, enableMinecartSmoothing);
        entry3.addProperty(DESC_SEVER, "Compensates minecart movement on the server during low TPS to preserve vanilla physics behavior. Default: true");
        entry3.addProperty(ENABLE_SERVER, enableMinecartLagAdjustedMovement);
        root.add(MINECART_MOVEMENT, entry3);

        final JsonObject entry4 = new JsonObject();
        entry4.addProperty(DESC_SEVER, "Compensates experience orb movement on the server during low TPS to preserve vanilla collection behavior. Default: true");
        entry4.addProperty(ENABLE_SERVER, enableExpOrbLagAdjustedMovement);
        root.add(EXPERIENCE_ORB_MOVEMENT, entry4);

        final JsonObject entry5 = new JsonObject();
        entry5.addProperty(DESC_SEVER, "Compensates falling block movement on the server during low TPS to preserve vanilla physics behavior. Default: true");
        entry5.addProperty(ENABLE_SERVER, enableFallingBlockLagAdjustedMovement);
        root.add(FALLINGBLOCKMOVEMENT, entry5);

        final JsonObject entry6 = new JsonObject();
        entry6.addProperty(DESC_SEVER, "Compensates projectile movement on the server during low TPS to preserve vanilla travel distance and timing. Default: true");
        entry6.addProperty(ENABLE_SERVER, enableProjectileLagAdjustedMovement);
        root.add(PROJECTILE_MOVEMENT, entry6);

        final JsonObject entry8 = new JsonObject();
        entry8.addProperty(DESC_SEVER, "Compensates player movement calculations during low TPS to reduce rubber-banding issues on the client. default:true");
        entry8.addProperty(ENABLE_SERVER, enableRubberbandingLagAdjustedMovement);
        root.add(PLAYER_MOVEMENT, entry8);

        final JsonObject entry7 = new JsonObject();
        entry7.addProperty(DESC_CLIENT, "Smooths sky/day movement on the client during server or network lag. Default: true");
        entry7.addProperty(ENABLE_CLIENT, enableSkySmoothing);
        entry7.addProperty(DESC_SEVER, "Compensates daytime progression on the server during low TPS to preserve vanilla day length. Default: true");
        entry7.addProperty(ENABLE_SERVER, enableSkyLagAdjustedMovement);
        root.add(SKY_MOVEMENT, entry7);

        return root;
    }

    public void deserialize(JsonObject data)
    {
        enableItemSmoothing = data.get(ITEM_MOVEMENT).getAsJsonObject().get(ENABLE_CLIENT).getAsBoolean();
        enableItemLagAdjustedMovement = data.get(ITEM_MOVEMENT).getAsJsonObject().get(ENABLE_SERVER).getAsBoolean();

        enableLivingEntitySmoothing = data.get(ENTITY_MOVEMENT).getAsJsonObject().get(ENABLE_CLIENT).getAsBoolean();
        enableLivingEntityLagAdjustedMovement = data.get(ENTITY_MOVEMENT).getAsJsonObject().get(ENABLE_SERVER).getAsBoolean();

        enableMinecartSmoothing = data.get(MINECART_MOVEMENT).getAsJsonObject().get(ENABLE_CLIENT).getAsBoolean();
        enableMinecartLagAdjustedMovement = data.get(MINECART_MOVEMENT).getAsJsonObject().get(ENABLE_SERVER).getAsBoolean();

        enableExpOrbLagAdjustedMovement = data.get(EXPERIENCE_ORB_MOVEMENT).getAsJsonObject().get(ENABLE_SERVER).getAsBoolean();

        enableFallingBlockLagAdjustedMovement = data.get(FALLINGBLOCKMOVEMENT).getAsJsonObject().get(ENABLE_SERVER).getAsBoolean();

        enableProjectileLagAdjustedMovement = data.get(PROJECTILE_MOVEMENT).getAsJsonObject().get(ENABLE_SERVER).getAsBoolean();

        enableRubberbandingLagAdjustedMovement = data.get(PLAYER_MOVEMENT).getAsJsonObject().get(ENABLE_SERVER).getAsBoolean();

        enableSkySmoothing = data.get(SKY_MOVEMENT).getAsJsonObject().get(ENABLE_CLIENT).getAsBoolean();
        enableSkyLagAdjustedMovement = data.get(SKY_MOVEMENT).getAsJsonObject().get(ENABLE_SERVER).getAsBoolean();
    }
}
