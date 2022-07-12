package appeng.helpers;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import appeng.api.exceptions.FailedConnectionException;
import appeng.api.features.Locatables;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.core.AELog;
import appeng.me.service.helpers.ConnectionWrapper;

public interface QuantumHost extends IActionHost {
    void onUnload(ServerLevel level);

    IGridNode getNode();

    boolean isActive();

    ConnectionWrapper getConnection();

    void setConnection(ConnectionWrapper connection);

    long getFrequency();

    long getThisSide();

    long getOtherSide();

    void setThisSide(long frequency);

    void setOtherSide(long frequency);

    Level getLevel();

    default void link() {
        final long qe = this.getFrequency();

        if (this.getThisSide() != qe && this.getThisSide() != -qe) {
            if (qe != 0) {
                if (this.getThisSide() != 0) {
                    Locatables.quantumNetworkBridges().unregister(getLevel(), getThisSide());
                }

                if (this.canUseNode(-qe)) {
                    this.setOtherSide(qe);
                    this.setThisSide(-qe);
                } else if (this.canUseNode(qe)) {
                    this.setThisSide(qe);
                    this.setOtherSide(-qe);
                }

                Locatables.quantumNetworkBridges().register(getLevel(), getThisSide(), this);
            } else {
                Locatables.quantumNetworkBridges().unregister(getLevel(), getThisSide());

                this.setOtherSide(0);
                this.setThisSide(0);
            }
        }

        var myOtherSide = this.getOtherSide() == 0 ? null
                : Locatables.quantumNetworkBridges().get(getLevel(), this.getOtherSide());

        boolean shutdown = false;

        if (myOtherSide instanceof QuantumHost sideB) {
            var sideA = this;

            if (sideA.isActive() && sideB.isActive()) {
                if (this.getConnection() != null && this.getConnection().getConnection() != null) {
                    final IGridNode a = this.getConnection().getConnection().a();
                    final IGridNode b = this.getConnection().getConnection().b();
                    final IGridNode sa = sideA.getNode();
                    final IGridNode sb = sideB.getNode();
                    if ((a == sa || b == sa) && (a == sb || b == sb)) {
                        return;
                    }
                }

                try {
                    if (sideA.getConnection() != null && sideA.getConnection().getConnection() != null) {
                        sideA.getConnection().getConnection().destroy();
                        sideA.setConnection(new ConnectionWrapper(null));
                    }

                    if (sideB.getConnection() != null && sideB.getConnection().getConnection() != null) {
                        sideB.getConnection().getConnection().destroy();
                        sideB.setConnection(new ConnectionWrapper(null));
                    }

                    var connection = new ConnectionWrapper(
                            GridHelper.createGridConnection(sideA.getNode(), sideB.getNode()));
                    sideA.setConnection(connection);
                    sideB.setConnection(connection);
                } catch (FailedConnectionException e) {
                    // :(
                    AELog.debug(e);
                }
            } else {
                shutdown = true;
            }
        } else {
            shutdown = true;
        }

        if (shutdown && this.getConnection() != null && this.getConnection().getConnection() != null) {
            this.getConnection().getConnection().destroy();
            this.getConnection().setConnection(null);
            this.setConnection(new ConnectionWrapper(null));
        }
    }

    default boolean canUseNode(long qe) {
        var locatable = Locatables.quantumNetworkBridges().get(getLevel(), qe);
        if (locatable instanceof QuantumHost qc) {
            return qc.canUseThisNode(qe);
        }
        return true;
    }

    boolean canUseThisNode(long qe);
}
