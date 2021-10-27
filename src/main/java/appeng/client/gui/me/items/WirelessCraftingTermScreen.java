package appeng.client.gui.me.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import appeng.api.config.ActionItems;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ActionButton;
import appeng.menu.me.items.WirelessCraftingTermMenu;

public class WirelessCraftingTermScreen extends ItemTerminalScreen<WirelessCraftingTermMenu> {

    public WirelessCraftingTermScreen(WirelessCraftingTermMenu menu, Inventory playerInventory,
            Component title, ScreenStyle style) {
        super(menu, playerInventory, title, style);

        ActionButton clearBtn = new ActionButton(ActionItems.STASH, btn -> menu.clearCraftingGrid());
        clearBtn.setHalfSize(true);
        widgets.add("clearCraftingGrid", clearBtn);
    }

}
