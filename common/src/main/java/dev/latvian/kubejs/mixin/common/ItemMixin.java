package dev.latvian.kubejs.mixin.common;

import dev.latvian.kubejs.KubeJSRegistries;
import dev.latvian.kubejs.core.ItemKJS;
import dev.latvian.mods.rhino.util.RemapForJS;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author LatvianModder
 */
@Mixin(Item.class)
public abstract class ItemMixin implements ItemKJS {
	@Override
	@Accessor("maxStackSize")
	public abstract void setMaxStackSizeKJS(int i);

	@Override
	@Accessor("maxDamage")
	public abstract void setMaxDamageKJS(int i);

	@Override
	@Accessor("craftingRemainingItem")
	public abstract void setCraftingReminderKJS(Item i);

	@Override
	@Accessor("isFireResistant")
	public abstract void setFireResistantKJS(boolean b);

	@Override
	@Accessor("rarity")
	public abstract void setRarityKJS(Rarity r);

	@RemapForJS("getId")
	public String getIdKJS() {
		return KubeJSRegistries.items().getId((Item) (Object) this).toString();
	}
}
