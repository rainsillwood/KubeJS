package com.latmod.mods.kubejs.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.latmod.mods.kubejs.KubeJS;
import com.latmod.mods.kubejs.item.IIngredientJS;
import com.latmod.mods.kubejs.item.IngredientListJS;
import com.latmod.mods.kubejs.item.ItemStackJS;
import com.latmod.mods.kubejs.item.OreDictionaryIngredientJS;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import jdk.nashorn.api.scripting.JSObject;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public enum UtilsJS
{
	INSTANCE;

	private Map<String, StatBase> statMap;
	public final Random random = new Random();
	public final List emptyList = Collections.EMPTY_LIST;
	public final Map emptyMap = Collections.EMPTY_MAP;

	public void init()
	{
		statMap = new HashMap<>(StatList.ALL_STATS.size());

		for (StatBase stat : StatList.ALL_STATS)
		{
			statMap.put(id(stat.statId).toString(), stat);
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T cast(Object o)
	{
		return (T) o;
	}

	public ID id(String namespace, String path)
	{
		return new ID(namespace, path);
	}

	public ID id(Object id)
	{
		return id instanceof ID ? (ID) id : new ID(String.valueOf(id));
	}

	public ResourceLocation idMC(ID id)
	{
		return new ResourceLocation(id.namespace, id.path);
	}

	public String simpleClassName(Class c)
	{
		String s = c.getSimpleName();
		return s.isEmpty() ? c.getName().substring(c.getName().lastIndexOf('.') + 1) : s;
	}

	public int parseInt(String s, int def)
	{
		try
		{
			return Integer.parseInt(s);
		}
		catch (Exception ex)
		{
			return def;
		}
	}

	public double parseDouble(String s, double def)
	{
		try
		{
			return Double.parseDouble(s);
		}
		catch (Exception ex)
		{
			return def;
		}
	}

	@Nullable
	public SoundEvent sound(Object id)
	{
		return ForgeRegistries.SOUND_EVENTS.getValue(idMC(id(id)));
	}

	public List<String> listFieldsAndMethods(Class clazz, int flags, String... exclude)
	{
		List<String> list = new ObjectArrayList<>();
		StringBuilder builder = new StringBuilder();
		Set<String> excludeSet = new ObjectOpenHashSet<>(Arrays.asList(exclude));
		excludeSet.add("equals()");
		excludeSet.add("toString()");
		excludeSet.add("hashCode()");
		excludeSet.add("getClass()");
		excludeSet.add("wait()");
		excludeSet.add("notify()");
		excludeSet.add("notifyAll()");

		for (Field field : clazz.getFields())
		{
			if (excludeSet.contains(field.getName()))
			{
				continue;
			}

			if ((field.getModifiers() & Modifier.PUBLIC) != 0)
			{
				if ((flags & 2) == 0)
				{
					if (field.isAnnotationPresent(Deprecated.class))
					{
						builder.append("@Deprecated ");
					}

					if (field.isAnnotationPresent(Nullable.class))
					{
						builder.append("@Nullable ");
					}
				}

				if ((flags & 1) == 0)
				{
					String m = Modifier.toString(field.getModifiers() & ~Modifier.PUBLIC);
					builder.append(m);

					if (!m.isEmpty())
					{
						builder.append(' ');
					}
				}

				if ((flags & 4) == 0)
				{
					builder.append(simpleClassName(field.getType()));
					builder.append(' ');
				}

				builder.append(field.getName());

				list.add(builder.toString());
				builder.setLength(0);
			}
		}

		for (Method method : clazz.getMethods())
		{
			if (excludeSet.contains(method.getName() + "()"))
			{
				continue;
			}

			if ((method.getModifiers() & Modifier.PUBLIC) != 0)
			{
				if ((flags & 2) == 0)
				{
					if (method.isAnnotationPresent(Deprecated.class))
					{
						builder.append("@Deprecated ");
					}

					if (method.isAnnotationPresent(Nullable.class))
					{
						builder.append("@Nullable ");
					}
				}

				if ((flags & 1) == 0)
				{
					String m = Modifier.toString(method.getModifiers() & ~Modifier.PUBLIC);
					builder.append(m);

					if (!m.isEmpty())
					{
						builder.append(' ');
					}
				}

				if ((flags & 4) == 0)
				{
					builder.append(simpleClassName(method.getReturnType()));
					builder.append(' ');
				}

				builder.append(method.getName());
				builder.append('(');

				boolean first = true;

				for (Class c : method.getParameterTypes())
				{
					if (first)
					{
						first = false;
					}
					else
					{
						builder.append(',');
						builder.append(' ');
					}

					builder.append(simpleClassName(c));
				}

				builder.append(')');

				list.add(builder.toString());
				builder.setLength(0);
			}
		}

		return list;
	}

	public List<String> listFieldsAndMethods(Object object, int flags, String... exclude)
	{
		return listFieldsAndMethods(object.getClass(), flags, exclude);
	}

	public JsonElement toJson(@Nullable Object object)
	{
		if (object == null)
		{
			return JsonNull.INSTANCE;
		}

		System.out.println(object.getClass() + ", " + object.toString());

		if (object instanceof JsonElement)
		{
			return (JsonElement) object;
		}
		else if (object instanceof String)
		{
			return new JsonPrimitive((String) object);
		}
		else if (object instanceof Boolean)
		{
			return new JsonPrimitive((Boolean) object);
		}
		else if (object instanceof Number)
		{
			return new JsonPrimitive((Number) object);
		}
		else if (object instanceof Character)
		{
			return new JsonPrimitive((Character) object);
		}
		else if (object instanceof Map)
		{
			Map<String, Object> map = (Map<String, Object>) object;

			if (map.isEmpty())
			{
				return new JsonObject();
			}

			Object[] array = new Object[map.size()];

			for (int i = 0; i < map.size(); i++)
			{
				Object value = map.get(Integer.toString(i));

				if (value != null)
				{
					array[i] = value;
				}
				else
				{
					JsonObject json = new JsonObject();

					for (Map.Entry<String, Object> entry : map.entrySet())
					{
						json.add(entry.getKey(), toJson(entry.getValue()));
					}

					return json;
				}
			}

			JsonArray json = new JsonArray();

			for (Object o : array)
			{
				json.add(toJson(o));
			}

			return json;
		}

		return JsonNull.INSTANCE;
	}

	@Nullable
	public NBTTagCompound toNBT(@Nullable Object o)
	{
		if (o == null)
		{
			return null;
		}
		else if (o instanceof NBTTagCompound)
		{
			return (NBTTagCompound) o;
		}
		else if (o instanceof Map)
		{
			try
			{
				return JsonToNBT.getTagFromJson(toJson(o).toString());
			}
			catch (Exception ex)
			{
				return null;
			}
		}

		try
		{
			return JsonToNBT.getTagFromJson(String.valueOf(o));
		}
		catch (Exception ex)
		{
			return null;
		}
	}

	public String toString(@Nullable UUID id)
	{
		if (id != null)
		{
			long msb = id.getMostSignificantBits();
			long lsb = id.getLeastSignificantBits();
			StringBuilder sb = new StringBuilder(32);
			digitsUUID(sb, msb >> 32, 8);
			digitsUUID(sb, msb >> 16, 4);
			digitsUUID(sb, msb, 4);
			digitsUUID(sb, lsb >> 48, 4);
			digitsUUID(sb, lsb, 12);
			return sb.toString();
		}

		return "";
	}

	private void digitsUUID(StringBuilder sb, long val, int digits)
	{
		long hi = 1L << (digits * 4);
		String s = Long.toHexString(hi | (val & (hi - 1)));
		sb.append(s, 1, s.length());
	}

	@Nullable
	public UUID toUUID(@Nullable String s)
	{
		if (s == null || !(s.length() == 32 || s.length() == 36))
		{
			return null;
		}

		try
		{
			if (s.indexOf('-') != -1)
			{
				return UUID.fromString(s);
			}

			int l = s.length();
			StringBuilder sb = new StringBuilder(36);
			for (int i = 0; i < l; i++)
			{
				sb.append(s.charAt(i));
				if (i == 7 || i == 11 || i == 15 || i == 19)
				{
					sb.append('-');
				}
			}

			return UUID.fromString(sb.toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public ItemStackJS item(@Nullable Object o)
	{
		if (o == null)
		{
			return ItemStackJS.EMPTY;
		}
		else if (o instanceof ItemStackJS)
		{
			return (ItemStackJS) o;
		}
		else if (o instanceof ItemStack)
		{
			ItemStack stack = (ItemStack) o;
			return stack.isEmpty() ? ItemStackJS.EMPTY : new ItemStackJS.Bound(stack);
		}

		String s0 = String.valueOf(o).trim();

		if (s0.isEmpty() || s0.equals("air"))
		{
			return ItemStackJS.EMPTY;
		}

		if (s0.startsWith("{") && s0.endsWith("}"))
		{
			try
			{
				ItemStack stack = new ItemStack(JsonToNBT.getTagFromJson(s0));

				if (!stack.isEmpty())
				{
					return new ItemStackJS.Bound(stack);
				}
			}
			catch (Exception ex)
			{
				return ItemStackJS.EMPTY;
			}
		}

		String[] s = s0.split("\\s", 4);
		ResourceLocation id = new ResourceLocation(s[0]);

		Item item = Item.REGISTRY.getObject(id);

		if (item != null && item != Items.AIR)
		{
			ItemStackJS stack = new ItemStackJS.Unbound(item);

			if (s.length >= 2)
			{
				stack.count(Integer.parseInt(s[1]));
			}

			if (s.length >= 3)
			{
				stack.data(Integer.parseInt(s[2]));
			}

			if (s.length >= 4)
			{
				stack.nbt(s[3]);
			}

			return stack;
		}

		return ItemStackJS.EMPTY;
	}

	public IIngredientJS ingredient(@Nullable Object object)
	{
		if (object instanceof String)
		{
			if (object.toString().startsWith("ore:"))
			{
				return new OreDictionaryIngredientJS(object.toString().substring(4));
			}

			return item(KubeJS.ID_CONTEXT.appendModId(object.toString()));
		}
		else if (object instanceof JSObject)
		{
			JSObject js = (JSObject) object;

			if (js.isArray())
			{
				IngredientListJS list = new IngredientListJS();

				for (String key : js.keySet())
				{
					IIngredientJS ingredient = ingredient(js.getMember(key));

					if (ingredient != ItemStackJS.EMPTY)
					{
						list.ingredients.add(ingredient);
					}
				}

				return list.ingredients.isEmpty() ? ItemStackJS.EMPTY : list;
			}
		}

		return item(object);
	}

	public JsonElement toJsonElement(@Nullable Object object)
	{
		if (object == null)
		{
			return JsonNull.INSTANCE;
		}
		else if (object instanceof Number)
		{
			return new JsonPrimitive((Number) object);
		}
		else if (object instanceof String)
		{
			return new JsonPrimitive((String) object);
		}
		else if (object instanceof Character)
		{
			return new JsonPrimitive((Character) object);
		}
		else if (object instanceof Boolean)
		{
			return new JsonPrimitive((Boolean) object);
		}
		else if (object instanceof JSObject)
		{
			JSObject js = (JSObject) object;

			if (js.isArray())
			{
				JsonArray a = new JsonArray();

				for (String s : js.keySet())
				{
					a.add(toJsonElement(js.getMember(s)));
				}

				return a;
			}
			else
			{
				JsonObject o = new JsonObject();

				for (String s : js.keySet())
				{
					o.add(s, toJsonElement(js.getMember(s)));
				}

				return o;
			}
		}

		return JsonNull.INSTANCE;
	}

	@Nullable
	public StatBase stat(Object id)
	{
		return statMap.get(id(id).toString());
	}
}