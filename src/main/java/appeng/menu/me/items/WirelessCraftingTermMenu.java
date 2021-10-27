package appeng.menu.me.items;

import net.minecraft.Util;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.SecurityPermissions;
import appeng.core.AEConfig;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.WirelessCraftingTerminalGuiObject;
import appeng.menu.implementations.MenuTypeBuilder;

public class WirelessCraftingTermMenu extends CraftingTermMenu {

    public static final MenuType<WirelessCraftingTermMenu> TYPE = MenuTypeBuilder
            .create(WirelessCraftingTermMenu::new, WirelessCraftingTerminalGuiObject.class)
            .requirePermission(SecurityPermissions.CRAFT)
            .build("wirelesscraftingterm");
    private final WirelessCraftingTerminalGuiObject wirelessTerminalGUIObject;
    private int ticks = 0;
    private double powerMultiplier = 0.5;
    private final int slot;

    public WirelessCraftingTermMenu(int id, final Inventory ip, final WirelessCraftingTerminalGuiObject monitorable) {
        super(TYPE, id, ip, monitorable, false);
        this.slot = monitorable.getInventorySlot();
        this.lockPlayerInventorySlot(this.slot);
        this.createPlayerInventorySlots(ip);
        this.wirelessTerminalGUIObject = monitorable;
    }

    @Override
    public void broadcastChanges() {// TODO some of this is duplicated with WirelessTermMenu, but there is no way around
                                    // this as it extends MEPortableCellMenu. It could maybe be used in a common
                                    // subclass (ItemTerminalMenu, or probably even MEMonitorableMenu to account for
                                    // wireless fluid terminals)
        if (!ensureGuiItemIsInSlot(this.wirelessTerminalGUIObject, this.slot)) {
            this.setValidMenu(false);
            return;
        }

        if (!this.wirelessTerminalGUIObject.rangeCheck()) {
            if (isServer() && this.isValidMenu()) {
                this.getPlayerInventory().player.sendMessage(PlayerMessages.OutOfRange.get(), Util.NIL_UUID);
            }

            this.setValidMenu(false);
        } else {
            powerMultiplier = AEConfig.instance().wireless_getDrainRate(this.wirelessTerminalGUIObject.getRange());
        }

        this.ticks++;
        if (this.ticks > 10) {
            this.wirelessTerminalGUIObject.extractAEPower(this.powerMultiplier * this.ticks, Actionable.MODULATE,
                    PowerMultiplier.CONFIG);
            this.ticks = 0;
        }
        super.broadcastChanges();
    }
}
