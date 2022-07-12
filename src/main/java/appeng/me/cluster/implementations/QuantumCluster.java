/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.me.cluster.implementations;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.annotation.Nullable;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import appeng.api.features.Locatables;
import appeng.api.networking.IGridNode;
import appeng.blockentity.qnb.QuantumBridgeBlockEntity;
import appeng.core.AELog;
import appeng.helpers.QuantumHost;
import appeng.me.cluster.IAECluster;
import appeng.me.cluster.MBCalculator;
import appeng.me.service.helpers.ConnectionWrapper;
import appeng.util.iterators.ChainedIterator;

public class QuantumCluster implements IAECluster, QuantumHost {

    public static final Set<QuantumHost> ACTIVE_CLUSTERS = new HashSet<>();

    static {
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            ACTIVE_CLUSTERS.clear();
        });
        ServerWorldEvents.UNLOAD.register((server, level) -> {
            var iteration = new ArrayList<>(ACTIVE_CLUSTERS);
            for (QuantumHost activeCluster : iteration) {
                activeCluster.onUnload(level);
            }
        });
    }

    private final BlockPos boundsMin;
    private final BlockPos boundsMax;
    private boolean isDestroyed = false;
    private boolean updateStatus = true;
    private QuantumBridgeBlockEntity[] Ring;
    private boolean registered = false;
    private ConnectionWrapper connection;
    private long thisSide;
    private long otherSide;
    private QuantumBridgeBlockEntity center;

    public QuantumCluster(BlockPos min, BlockPos max) {
        this.boundsMin = min.immutable();
        this.boundsMax = max.immutable();
        this.setRing(new QuantumBridgeBlockEntity[8]);
    }

    public void onUnload(ServerLevel level) {
        if (this.center.getLevel() == level) {
            this.setUpdateStatus(false);
            this.destroy();
        }
    }

    @Override
    public void updateStatus(boolean updateGrid) {
        link();
    }

    public boolean canUseThisNode(long qe) {
        var level = this.center.getLevel();
        if (!this.isDestroyed) {
            // In future versions, we might actually want to delay the entire registration
            // until the center
            // block entity begins ticking normally.
            if (level.hasChunkAt(this.center.getBlockPos())) {
                final Level cur = level.getServer().getLevel(level.dimension());

                final BlockEntity te = level.getBlockEntity(this.center.getBlockPos());
                return te != this.center || level != cur;
            } else {
                AELog.warn("Found a registered QNB with serial %s whose chunk seems to be unloaded: %s", qe, this);
            }
        }
        return true;
    }

    public boolean isActive() {
        if (this.isDestroyed || !this.registered) {
            return false;
        }

        return this.center.isPowered() && this.hasQES();
    }

    public IGridNode getNode() {
        return this.center.getGridNode();
    }

    private boolean hasQES() {
        return this.thisSide != 0;
    }

    @Override
    public BlockPos getBoundsMin() {
        return boundsMin;
    }

    @Override
    public BlockPos getBoundsMax() {
        return boundsMax;
    }

    @Override
    public boolean isDestroyed() {
        return isDestroyed;
    }

    @Override
    public void destroy() {
        if (this.isDestroyed) {
            return;
        }
        this.isDestroyed = true;

        MBCalculator.setModificationInProgress(this);
        try {
            if (this.registered) {
                ACTIVE_CLUSTERS.remove(this);
                this.registered = false;
            }

            if (this.thisSide != 0) {
                this.updateStatus(true);
                Locatables.quantumNetworkBridges().unregister(center.getLevel(), getThisSide());
            }

            this.center.updateStatus(null, (byte) -1, this.isUpdateStatus());

            for (var r : this.getRing()) {
                r.updateStatus(null, (byte) -1, this.isUpdateStatus());
            }

            this.center = null;
            this.setRing(new QuantumBridgeBlockEntity[8]);
        } finally {
            MBCalculator.setModificationInProgress(null);
        }
    }

    @Override
    public Iterator<QuantumBridgeBlockEntity> getBlockEntities() {
        return new ChainedIterator<>(this.getRing()[0], this.getRing()[1], this.getRing()[2], this.getRing()[3],
                this.getRing()[4], this.getRing()[5], this.getRing()[6], this.getRing()[7], this.center);
    }

    public boolean isCorner(QuantumBridgeBlockEntity quantumBridge) {
        return this.getRing()[0] == quantumBridge || this.getRing()[2] == quantumBridge
                || this.getRing()[4] == quantumBridge || this.getRing()[6] == quantumBridge;
    }

    public QuantumBridgeBlockEntity getCenter() {
        return this.center;
    }

    void setCenter(QuantumBridgeBlockEntity c) {
        this.registered = true;
        ACTIVE_CLUSTERS.add(this);
        this.center = c;
    }

    private boolean isUpdateStatus() {
        return this.updateStatus;
    }

    public void setUpdateStatus(boolean updateStatus) {
        this.updateStatus = updateStatus;
    }

    QuantumBridgeBlockEntity[] getRing() {
        return this.Ring;
    }

    private void setRing(QuantumBridgeBlockEntity[] ring) {
        this.Ring = ring;
    }

    @Override
    public String toString() {
        if (center == null) {
            return "QuantumCluster{no-center}";
        }

        Level level = center.getLevel();
        BlockPos pos = center.getBlockPos();

        return "QuantumCluster{" + level + "," + pos + "}";
    }

    @Nullable
    @Override
    public IGridNode getActionableNode() {
        return center.getMainNode().getNode();
    }

    public ConnectionWrapper getConnection() {
        return connection;
    }

    public void setConnection(ConnectionWrapper connection) {
        this.connection = connection;
    }

    public long getFrequency() {
        return center.getQEFrequency();
    }

    public long getThisSide() {
        return thisSide;
    }

    public long getOtherSide() {
        return otherSide;
    }

    public void setThisSide(long frequency) {
        thisSide = frequency;
    }

    public void setOtherSide(long frequency) {
        otherSide = frequency;
    }

    public Level getLevel() {
        return center.getLevel();
    }
}
