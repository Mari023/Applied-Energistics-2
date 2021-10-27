package appeng.items.tools.powered;

import appeng.menu.me.items.WirelessTermMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.function.DoubleSupplier;

public class WirelessCraftingTerminalItem extends WirelessTerminalItem {
    public WirelessCraftingTerminalItem(final DoubleSupplier powerCapacity, Properties props) {
        super(powerCapacity, props);
    }

    @Override
    public MenuType<?> getMenuType() {// TODO change to WirelessCraftingTermMenu or whatever
        return WirelessTermMenu.TYPE;
    }
}
