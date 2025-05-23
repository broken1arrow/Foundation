package org.mineacademy.fo.remain.nbt;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Level;

import javax.annotation.Nullable;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

/**
 * General utility class for a clean and simple nbt access.
 *
 * @author tr7zw
 *
 */
public class NBT {

	private NBT() {
		// No instances of NBT. Utility class
	}

	/**
	 * Utility method for shaded versions to preload and check the API during
	 * onEnable. This method does not throw an exception and instead logs them. Will
	 * return false if something fundamentally is wrong and the NBTAPI is in a non
	 * functioning state. The shading plugin then needs to handle this. Note:
	 * Calling this method during onLoad will cause an failure, so please wait till
	 * onEnable.
	 *
	 * @return true if everything went fine
	 */
	public static boolean preloadApi() {
		try {
			// boiled down version of the plugin selfcheck without tests
			if (MinecraftVersion.getVersion() == MinecraftVersion.UNKNOWN) {
				NbtApiException.confirmedBroken = true;
				return false;
			}
			for (final ClassWrapper c : ClassWrapper.values())
				if (c.isEnabled() && c.getClazz() == null) {
					NbtApiException.confirmedBroken = true;
					return false;
				}
			for (final ReflectionMethod method : ReflectionMethod.values())
				if (method.isCompatible() && !method.isLoaded()) {
					NbtApiException.confirmedBroken = true;
					return false;
				}
			// not settings NbtApiException.confirmedBroken = false, as no actual tests were done.
			// This just means the version was found, and all reflections seem to work.
			return true;
		} catch (final Exception ex) {
			NbtApiException.confirmedBroken = true;
			MinecraftVersion.getLogger().log(Level.WARNING, "[NBTAPI] Error during the selfcheck!", ex);
			return false;
		}
	}

	/**
	 * Get a read only instance of the items NBT. This method is slightly slower
	 * than calling NBT.get due to having to create a copy of the ItemStack, but
	 * allows context free access to the data.
	 *
	 * @param item
	 * @return
	 */
	public static ReadableNBT readNbt(final ItemStack item) {
		return new NBTItem(item.clone(), false, true, false);
	}

	/**
	 * It takes an ItemStack, and a function that takes a ReadableNBT and returns a
	 * generic type T. It then returns the result of the function applied to a new
	 * NBTItem
	 *
	 * @param item   The itemstack you want to get the NBT from
	 * @param getter A function that takes a ReadableNBT and returns a value of type
	 *               T.
	 * @return The function is being returned.
	 */
	public static <T> T get(final ItemStack item, final Function<ReadableItemNBT, T> getter) {
		final NBTItem nbt = new NBTItem(item, false, true, false);
		final T ret = getter.apply(nbt);
		if (ret instanceof ReadableNBT || ret instanceof ReadableNBTList<?>)
			throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
		nbt.setClosed();
		return ret;
	}

	/**
	 * It takes an ItemStack, and a Consumer that takes a ReadableNBT. Applies the
	 * Consumer on the NBT of the item
	 *
	 * @param item The itemstack you want to get the NBT from
	 * @param getter
	 */
	public static void get(final ItemStack item, final Consumer<ReadableItemNBT> getter) {
		final NBTItem nbt = new NBTItem(item, false, true, false);
		getter.accept(nbt);
		nbt.setClosed();
	}

	/**
	 * It takes an entity and a function that takes a ReadableNBT and returns a
	 * generic type T, and returns the result of the function
	 *
	 * @param entity The entity to get the NBT from
	 * @param getter A function that takes a ReadableNBT and returns a value.
	 * @return The NBTEntity class is being returned.
	 */
	public static <T> T get(final Entity entity, final Function<ReadableNBT, T> getter) {
		final NBTEntity nbt = new NBTEntity(entity, true);
		final T ret = getter.apply(nbt);
		if (ret instanceof ReadableNBT || ret instanceof ReadableNBTList<?>)
			throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
		nbt.setClosed();
		return ret;
	}

	/**
	 * It takes an Entity, and a Consumer that takes a ReadableNBT. Applies the
	 * Consumer on the NBT of the Entity
	 *
	 * @param entity The entity to get the NBT from
	 * @param getter
	 */
	public static void get(final Entity entity, final Consumer<ReadableNBT> getter) {
		final NBTEntity nbt = new NBTEntity(entity, true);
		getter.accept(nbt);
		nbt.setClosed();
	}

	/**
	 * It takes a block state and a function that takes a readable NBT and returns a
	 * value of type T. It then returns the value of the function applied to a new
	 * NBTTileEntity created from the block state
	 *
	 * @param blockState The block state of the block you want to get the NBT from.
	 * @param getter     A function that takes a ReadableNBT and returns a value of
	 *                   type T.
	 * @return The return type is the same as the type of the getter function.
	 */
	public static <T> T get(final BlockState blockState, final Function<ReadableNBT, T> getter) {
		final NBTTileEntity nbt = new NBTTileEntity(blockState, true);
		final T ret = getter.apply(nbt);
		if (ret instanceof ReadableNBT || ret instanceof ReadableNBTList<?>)
			throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
		nbt.setClosed();
		return ret;
	}

	/**
	 * It takes an BlockEntity, and a Consumer that takes a ReadableNBT. Applies the
	 * Consumer on the NBT of the BlockEntity
	 *
	 * @param blockState The block state of the block you want to get the NBT from.
	 * @param getter
	 */
	public static void get(final BlockState blockState, final Consumer<ReadableNBT> getter) {
		final NBTTileEntity nbt = new NBTTileEntity(blockState, true);
		getter.accept(nbt);
		nbt.setClosed();
	}

	/**
	 * It takes an entity and a function that takes a ReadableNBT and returns a
	 * generic type T, and returns the result of the function, applied to the
	 * entities persistent data container
	 *
	 * @param entity The entity to get the data from
	 * @param getter A function that takes a ReadableNBT and returns a value of type
	 *               T.
	 * @return The return type is T, which is a generic type.
	 */
	public static <T> T getPersistentData(final Entity entity, final Function<ReadableNBT, T> getter) {
		final T ret = getter.apply(new NBTEntity(entity).getPersistentDataContainer());
		if (ret instanceof ReadableNBT || ret instanceof ReadableNBTList<?>)
			throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
		return ret;
	}

	/**
	 * It takes a block entity and a function that takes a ReadableNBT and returns a
	 * generic type T, and returns the result of the function, applied to the block
	 * entities persistent data container
	 *
	 * @param blockState The block state of the block you want to get the data from.
	 * @param getter     A function that takes a ReadableNBT and returns a value of
	 *                   type T.
	 * @return The value of the NBT tag.
	 */
	public static <T> T getPersistentData(final BlockState blockState, final Function<ReadableNBT, T> getter) {
		final T ret = getter.apply(new NBTTileEntity(blockState).getPersistentDataContainer());
		if (ret instanceof ReadableNBT || ret instanceof ReadableNBTList<?>)
			throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
		return ret;
	}

	/**
	 * It takes an ItemStack, applies a function to its NBT, and returns the result
	 * of the function
	 *
	 * @param item     The item you want to modify
	 * @param function The function that will be applied to the item.
	 * @return The return value of the function.
	 */
	public static <T> T modify(final ItemStack item, final Function<ReadWriteItemNBT, T> function) {
		final NBTItem nbti = new NBTItem(item, false, false, true);
		final T val = function.apply(nbti);
		nbti.finalizeChanges();
		if (val instanceof ReadableNBT || val instanceof ReadableNBTList<?>)
			throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
		nbti.setClosed();
		return val;
	}

	/**
	 * It takes an ItemStack and a Consumer&lt;ReadWriteNBT&gt;, and then applies
	 * the Consumer to the ItemStacks NBT
	 *
	 * @param item     The item you want to modify
	 * @param consumer The consumer that will be used to modify the NBT.
	 */
	public static void modify(final ItemStack item, final Consumer<ReadWriteItemNBT> consumer) {
		final NBTItem nbti = new NBTItem(item, false, false, true);
		consumer.accept(nbti);
		nbti.finalizeChanges();
		nbti.setClosed();
	}

	/**
	 * It takes an entity and a function that takes a ReadWriteNBT and returns a
	 * generic type T. It then returns the result of the function
	 *
	 * @param entity   The entity to modify
	 * @param function The function that will be called.
	 * @return The return type is the same as the return type of the function.
	 */
	public static <T> T modify(final Entity entity, final Function<ReadWriteNBT, T> function) {
		final NBTEntity nbtEnt = new NBTEntity(entity);
		final NBTContainer cont = new NBTContainer(nbtEnt.getCompound());
		final T ret = function.apply(cont);
		nbtEnt.setCompound(cont.getCompound());
		if (ret instanceof ReadableNBT || ret instanceof ReadableNBTList<?>)
			throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
		nbtEnt.setClosed();
		return ret;
	}

	/**
	 * It takes an ItemStack and a Consumer&lt;ReadWriteNBT&gt;, and then applies
	 * the Consumer to the ItemStacks Components as NBT. This is for 1.20.5+ only.
	 * This method is quite expensive, so don't overuse it.
	 *
	 * @param item     The item you want to modify the components of
	 * @param consumer The consumer that will be used to modify the components.
	 */
	public static void modifyComponents(final ItemStack item, final Consumer<ReadWriteNBT> consumer) {
		if (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4))
			throw new NbtApiException("This method only works for 1.20.5+!");
		final ReadWriteNBT nbti = NBT.itemStackToNBT(item);
		consumer.accept(nbti.getOrCreateCompound("components"));
		final ItemStack tmp = NBT.itemStackFromNBT(nbti);
		item.setItemMeta(tmp.getItemMeta());
	}

	/**
	 * It takes an ItemStack and a Consumer&lt;ReadWriteNBT&gt;, and then applies
	 * the Consumer to the ItemStacks Components as NBT. This is for 1.20.5+ only.
	 * This method is quite expensive, so don't overuse it.
	 *
	 * @param item     The item you want to modify the components of
	 * @param function The consumer that will be used to modify the components.
	 * @return The return type is the same as the return type of the function.
	 */
	public static <T> T modifyComponents(final ItemStack item, final Function<ReadWriteNBT, T> function) {
		if (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4))
			throw new NbtApiException("This method only works for 1.20.5+!");
		final ReadWriteNBT nbti = NBT.itemStackToNBT(item);
		final T ret = function.apply(nbti.getOrCreateCompound("components"));
		final ItemStack tmp = NBT.itemStackFromNBT(nbti);
		item.setItemMeta(tmp.getItemMeta());
		return ret;
	}

	/**
	 * It takes an ItemStack and a Consumer&lt;ReadWriteNBT&gt;, and then applies
	 * the Consumer to the ItemStacks Components as NBT. This is for 1.20.5+ only.
	 * This method is quite expensive, try to cache the results/use it smartly.
	 *
	 * @param item     The item you want to read the components of
	 * @param consumer The consumer that will be used to read the components.
	 */
	public static void getComponents(final ItemStack item, final Consumer<ReadableNBT> consumer) {
		if (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4))
			throw new NbtApiException("This method only works for 1.20.5+!");
		final ReadWriteNBT nbti = NBT.itemStackToNBT(item);
		consumer.accept(nbti.getOrCreateCompound("components"));
	}

	/**
	 * It takes an ItemStack and a Consumer&lt;ReadWriteNBT&gt;, and then applies
	 * the Consumer to the ItemStacks Components as NBT. This is for 1.20.5+ only.
	 * This method is quite expensive, try to cache the results/use it smartly.
	 *
	 * @param item     The item you want to read the components of
	 * @param function The consumer that will be used to read the components.
	 * @return The return type is the same as the return type of the function.
	 */
	public static <T> T getComponents(final ItemStack item, final Function<ReadableNBT, T> function) {
		if (!MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_20_R4))
			throw new NbtApiException("This method only works for 1.20.5+!");
		final ReadWriteNBT nbti = NBT.itemStackToNBT(item);
		return function.apply(nbti.getOrCreateCompound("components"));
	}

	/**
	 * It takes an entity and a function that takes a ReadWriteNBT and applies the
	 * function to the entity
	 *
	 * @param entity   The entity to modify
	 * @param consumer The consumer that will be called with the NBTEntity.
	 */
	public static void modify(final Entity entity, final Consumer<ReadWriteNBT> consumer) {
		final NBTEntity nbtEnt = new NBTEntity(entity);
		final NBTContainer cont = new NBTContainer(nbtEnt.getCompound());
		consumer.accept(cont);
		nbtEnt.setCompound(cont.getCompound());
		nbtEnt.setClosed();
	}

	/**
	 * It takes an entity and a function that takes a ReadWriteNBT from the entities
	 * persistent data and returns a generic type T. It then returns the result of
	 * the function
	 *
	 * @param entity   The entity to modify the data of.
	 * @param function The function that will be called.
	 * @return The return type is the same as the return type of the function.
	 */
	public static <T> T modifyPersistentData(final Entity entity, final Function<ReadWriteNBT, T> function) {
		final T ret = function.apply(new NBTEntity(entity).getPersistentDataContainer());
		if (ret instanceof ReadableNBT || ret instanceof ReadableNBTList<?>)
			throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
		return ret;
	}

	/**
	 * It allows you to modify the persistent data of an entity without any return
	 * value
	 *
	 * @param entity   The entity to modify
	 * @param consumer The consumer that will be used to modify the persistent data.
	 */
	public static void modifyPersistentData(final Entity entity, final Consumer<ReadWriteNBT> consumer) {
		consumer.accept(new NBTEntity(entity).getPersistentDataContainer());
	}

	/**
	 * It takes a block state and a function that takes a ReadWriteNBT and returns a
	 * generic type T. It then returns the result of the function
	 *
	 * @param blockState The blockstate you want to modify
	 * @param function   The function that will be called.
	 * @return The return type is the same as the return type of the function.
	 */
	public static <T> T modify(final BlockState blockState, final Function<ReadWriteNBT, T> function) {
		final NBTTileEntity blockEnt = new NBTTileEntity(blockState);
		final NBTContainer cont = new NBTContainer(blockEnt.getCompound());
		final T ret = function.apply(cont);
		blockEnt.setCompound(cont.getCompound());
		if (ret instanceof ReadableNBT || ret instanceof ReadableNBTList<?>)
			throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
		blockEnt.setClosed();
		return ret;
	}

	/**
	 * It takes a block state and a consumer, and then it creates a new
	 * NBTTileEntity object with the block state, and then it passes that
	 * NBTTileEntity object to the consumer
	 *
	 * @param blockState The blockstate you want to modify
	 * @param consumer   A Consumer&lt;ReadWriteNBT&gt;. This is a function that
	 *                   takes a ReadWriteNBT and does something with it.
	 */
	public static void modify(final BlockState blockState, final Consumer<ReadWriteNBT> consumer) {
		final NBTTileEntity blockEnt = new NBTTileEntity(blockState);
		final NBTContainer cont = new NBTContainer(blockEnt.getCompound());
		consumer.accept(cont);
		blockEnt.setCompound(cont.getCompound());
		blockEnt.setClosed();
	}

	/**
	 * It takes a block state and a function that takes a ReadWriteNBT of the block
	 * entities persistent data and returns a generic type T. It then returns the
	 * result of the function
	 *
	 * @param blockState The block state of the block you want to modify.
	 * @param function   The function that will be called to modify the NBT data.
	 * @return The return type is the same as the return type of the function.
	 */
	public static <T> T modifyPersistentData(final BlockState blockState, final Function<ReadWriteNBT, T> function) {
		final T ret = function.apply(new NBTTileEntity(blockState).getPersistentDataContainer());
		if (ret instanceof ReadableNBT || ret instanceof ReadableNBTList<?>)
			throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
		return ret;
	}

	/**
	 * It takes a block state and a consumer, and then it calls the consumer with
	 * the persistent data container of the block entity
	 *
	 * @param blockState The block state of the block you want to modify.
	 * @param consumer   A Consumer&lt;ReadWriteNBT&gt;. This is a function that
	 *                   takes a ReadWriteNBT and does something with it.
	 */
	public static void modifyPersistentData(final BlockState blockState, final Consumer<ReadWriteNBT> consumer) {
		consumer.accept(new NBTTileEntity(blockState).getPersistentDataContainer());
	}

	/**
	 * It converts an ItemStack to a ReadWriteNBT object
	 *
	 * @param itemStack The item stack you want to convert to NBT.
	 * @return A ReadWriteNBT object.
	 */
	public static ReadWriteNBT itemStackToNBT(final ItemStack itemStack) {
		return NBTItem.convertItemtoNBT(itemStack);
	}

	/**
	 * It converts a ReadableNBT object into an ItemStack
	 *
	 * @param compound The NBT tag to convert to an ItemStack
	 * @return An ItemStack
	 */
	@Nullable
	public static ItemStack itemStackFromNBT(final ReadableNBT compound) {
		return NBTItem.convertNBTtoItem((NBTCompound) compound);
	}

	/**
	 * It converts an array of ItemStacks into a ReadWriteNBT object
	 *
	 * @param itemStacks The ItemStack[] you want to convert to NBT
	 * @return An NBTItem object.
	 */
	public static ReadWriteNBT itemStackArrayToNBT(final ItemStack[] itemStacks) {
		return NBTItem.convertItemArraytoNBT(itemStacks);
	}

	/**
	 * It converts a ReadableNBT object into an array of ItemStacks
	 *
	 * @param compound The NBT tag to convert to an ItemStack array.
	 * @return An array of ItemStacks.
	 */
	@Nullable
	public static ItemStack[] itemStackArrayFromNBT(final ReadableNBT compound) {
		return NBTItem.convertNBTtoItemArray((NBTCompound) compound);
	}

	/**
	 * Create a new NBTContainer object and return it.
	 *
	 * @return A new instance of the NBTContainer class.
	 */
	public static ReadWriteNBT createNBTObject() {
		return new NBTContainer();
	}

	/**
	 * It takes a nbt json string, and returns a ReadWriteNBT object
	 *
	 * @param nbtString The NBT string to parse.
	 * @return A new ReadWriteNBT object.
	 */
	public static ReadWriteNBT parseNBT(final String nbtString) {
		return new NBTContainer(nbtString);
	}

	/**
	 * Reads in an NBT stream and returns a ReadWriteNBT object
	 *
	 * @param stream The NBT stream to read.
	 * @return A new ReadWriteNBT object.
	 */
	public static ReadWriteNBT readNBT(final InputStream stream) {
		return new NBTContainer(stream);
	}

	/**
	 * Helper method for other developers using NMS. Allows to wrap any
	 * net.minecraft.nbt.CompoundTag to a NBTAPI ReadWriteNBT object.
	 *
	 * @param nmsNbtTag Needs to be a valid net.minecraft.nbt.CompoundTag
	 * @return A new ReadWriteNBT object.
	 */
	public static ReadWriteNBT wrapNMSTag(final Object nmsNbtTag) {
		return new NBTContainer(nmsNbtTag);
	}

	/**
	 * Creates a NBTFileHandle that uses @param file to store its data. If this file
	 * exists, the data will be loaded, otherwise a new file gets created.
	 *
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static NBTFileHandle getFileHandle(final File file) throws IOException {
		return new NBTFile(file);
	}

	/**
	 * Reads NBT data from the provided file.
	 * <p>
	 * Returns an empty tag if the file does not exist.
	 *
	 * @param file file to read
	 * @return ReadWriteNBT of the files data
	 * @throws IOException exception
	 */
	public static ReadWriteNBT readFile(final File file) throws IOException {
		return NBTFile.readFrom(file);
	}

	/**
	 * Saves NBT data to the provided file.
	 * <p>
	 * Will fully override the file if it already exists.
	 *
	 * @param file file
	 * @param nbt  NBT data
	 * @throws IOException exception
	 */
	public static void writeFile(final File file, final ReadWriteNBT nbt) throws IOException {
		NBTFile.saveTo(file, (NBTCompound) nbt);
	}

	/**
	 * Create a read only proxy class for NBT, given an annotated interface.
	 *
	 * @param <T>
	 * @param item
	 * @param wrapper
	 * @return
	 */
	public static <T extends NBTProxy> T readNbt(final ItemStack item, final Class<T> wrapper) {
		return new ProxyBuilder<>(new NBTItem(item, false, true, false), wrapper).readOnly().build();
	}

	/**
	 * Create a read only proxy class for NBT, given an annotated interface.
	 *
	 * @param <T>
	 * @param entity
	 * @param wrapper
	 * @return
	 */
	public static <T extends NBTProxy> T readNbt(final Entity entity, final Class<T> wrapper) {
		return new ProxyBuilder<>(new NBTEntity(entity, true), wrapper).readOnly().build();
	}

	/**
	 * Create a read only proxy class for NBT, given an annotated interface.
	 *
	 * @param <T>
	 * @param blockState
	 * @param wrapper
	 * @return
	 */
	public static <T extends NBTProxy> T readNbt(final BlockState blockState, final Class<T> wrapper) {
		return new ProxyBuilder<>(new NBTTileEntity(blockState, true), wrapper).readOnly().build();
	}

	/**
	 * It takes an ItemStack, applies a function to its NBT wrapped in a proxy, and
	 * returns the result of the function
	 *
	 * @param item     The item you want to
	 * @param wrapper  The target Proxy class
	 * @param function The function that will be applied to the item.
	 * @return The return value of the function.
	 */
	public static <T, X extends NBTProxy> T modify(final ItemStack item, final Class<X> wrapper, final Function<X, T> function) {
		final NBTItem nbti = new NBTItem(item, false, false, true);
		final T val = function.apply(new ProxyBuilder<>(nbti, wrapper).build());
		nbti.finalizeChanges();
		if (val instanceof ReadableNBT || val instanceof ReadableNBTList<?>)
			throw new NbtApiException("Tried returning part of the NBT to outside of the NBT scope!");
		nbti.setClosed();
		return val;
	}

	/**
	 * It takes an ItemStack, applies a function to its NBT wrapped in a proxy.
	 *
	 * @param item     The item you want to modify
	 * @param wrapper  The target Proxy class
	 * @param consumer The consumer that will be used to modify the NBT.
	 */
	public static <X extends NBTProxy> void modify(final ItemStack item, final Class<X> wrapper, final Consumer<X> consumer) {
		final NBTItem nbti = new NBTItem(item, false, false, true);
		consumer.accept(new ProxyBuilder<>(nbti, wrapper).build());
		nbti.finalizeChanges();
		nbti.setClosed();
	}

	/**
	 * It takes an entity and a function to modify the entity via the proxy
	 *
	 * @param entity   The entity to modify
	 * @param wrapper  The target Proxy class
	 * @param consumer The consumer that will be called with the proxy.
	 */
	public static <X extends NBTProxy> void modify(final Entity entity, final Class<X> wrapper, final Consumer<X> consumer) {
		final NBTEntity nbtEnt = new NBTEntity(entity);
		final NBTContainer cont = new NBTContainer(nbtEnt.getCompound());
		consumer.accept(new ProxyBuilder<>(cont, wrapper).build());
		nbtEnt.setCompound(cont.getCompound());
		cont.setClosed();
	}

	/**
	 * It takes an entity and a function to modify the entity via the proxy
	 *
	 * @param entity   The entity to modify
	 * @param wrapper  The target Proxy class
	 * @param function The Function that will be called with the proxy.
	 * @return The return value of the function.
	 */
	public static <T, X extends NBTProxy> T modify(final Entity entity, final Class<X> wrapper, final Function<X, T> function) {
		final NBTEntity nbtEnt = new NBTEntity(entity);
		final NBTContainer cont = new NBTContainer(nbtEnt.getCompound());
		final T val = function.apply(new ProxyBuilder<>(cont, wrapper).build());
		nbtEnt.setCompound(cont.getCompound());
		cont.setClosed();
		return val;
	}

	/**
	 * It takes an block entity and a function to modify the entity via the proxy
	 *
	 * @param blockState The blockstate you want to modify
	 * @param wrapper    The target Proxy class
	 * @param consumer   The Consumer that will be called.
	 */
	public static <X extends NBTProxy> void modify(final BlockState blockState, final Class<X> wrapper, final Consumer<X> consumer) {
		final NBTTileEntity blockEnt = new NBTTileEntity(blockState);
		final NBTContainer cont = new NBTContainer(blockEnt.getCompound());
		consumer.accept(new ProxyBuilder<>(cont, wrapper).build());
		blockEnt.setCompound(cont);
		cont.setClosed();
	}

	/**
	 * It takes an block entity and a function to modify the entity via the proxy
	 *
	 * @param blockState The blockstate you want to modify
	 * @param wrapper    The target Proxy class
	 * @param function   The function that will be called.
	 * @return The return value of the function.
	 */
	public static <T, X extends NBTProxy> T modify(final BlockState blockState, final Class<X> wrapper, final Function<X, T> function) {
		final NBTTileEntity blockEnt = new NBTTileEntity(blockState);
		final NBTContainer cont = new NBTContainer(blockEnt.getCompound());
		final T val = function.apply(new ProxyBuilder<>(cont, wrapper).build());
		blockEnt.setCompound(cont);
		cont.setClosed();
		return val;
	}

}
