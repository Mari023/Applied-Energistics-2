package appeng.items.tools.powered;

import java.util.function.DoubleSupplier;

import net.minecraft.world.inventory.MenuType;

import appeng.menu.me.items.WirelessCraftingTermMenu;

public class WirelessCraftingTerminalItem extends WirelessTerminalItem {
    public WirelessCraftingTerminalItem(final DoubleSupplier powerCapacity, Properties props) {
        super(powerCapacity, props);
    }

    @Override
    public MenuType<?> getMenuType() {// TODO change to WirelessCraftingTermMenu or whatever
        return WirelessCraftingTermMenu.TYPE;
    }
}
