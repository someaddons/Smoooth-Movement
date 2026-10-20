package com.smoothmovement.mixin.livingentity;

import com.smoothmovement.config.CommonConfiguration;
import com.smoothmovement.time.ServerTime;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PathNavigation.class)
public abstract class ServerPathNavigationMixin
{
    @Shadow
    @Final
    protected Mob   mob;
    @Shadow
    protected Path  path;
    @Shadow
    protected float maxDistanceToWaypoint;

    @Unique
    private Path previousPath;
    @Unique
    private Vec3 previousPosition;
    @Unique
    private int previousNodeIndex;
    @Unique
    private int previousTick;

    @Inject(method = "tick", at = @At("RETURN"))
    private void smoothmovement$recordMovementStart(final CallbackInfo ci)
    {
        previousPath = null;
        if (path == null || path.isDone() || !needsAdjustment())
        {
            return;
        }

        // Navigation is called before travel
        previousPath = path;
        previousPosition = mob.position();
        previousNodeIndex = path.getNextNodeIndex();
        previousTick = mob.tickCount;
    }

    @ModifyVariable(method = "followThePath", at = @At("STORE"), ordinal = 0)
    private boolean smoothmovement$acceptCrossedWaypoint(final boolean reached)
    {
        if (reached || path == null || path != previousPath || path.getNextNodeIndex() != previousNodeIndex
            || mob.tickCount != previousTick + 1 || !needsAdjustment() || mob.horizontalCollision)
        {
            return reached;
        }

        final Vec3 start = previousPosition;
        final Vec3 end = mob.position();
        final Vec3 waypoint = path.getNextEntityPos(mob);
        // vanilla height restriction
        if (start.distanceToSqr(end) > 64.0 || Math.abs(start.y - waypoint.y) >= 1.0 || Math.abs(end.y - waypoint.y) >= 1.0)
        {
            return false;
        }

        final double radius = maxDistanceToWaypoint;
        if (Math.abs(start.x - waypoint.x) <= radius && Math.abs(start.z - waypoint.z) <= radius)
        {
            return true;
        }

        // Check if the waypoint was passed on the way from old to new position
        return new AABB(waypoint.x - radius, waypoint.y - 1.0, waypoint.z - radius, waypoint.x + radius, waypoint.y + 1.0, waypoint.z + radius).clip(start, end).isPresent();
    }

    @Unique
    private boolean needsAdjustment()
    {
        return ServerTime.hasLag() && CommonConfiguration.config.getCommonConfig().enableLivingEntityLagAdjustedMovement && !mob.level().isClientSide && mob.isAlive()
            && !mob.isPassenger() && mob.isControlledByLocalInstance() && !(mob.getControllingPassenger() instanceof Player);
    }
}
