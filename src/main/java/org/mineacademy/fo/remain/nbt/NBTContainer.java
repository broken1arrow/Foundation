package org.mineacademy.fo.remain.nbt;

import java.io.InputStream;

/**
 * A Standalone {@link NBTCompound} implementation. All data is just kept inside
 * this Object.
 *
 * @author tr7zw
 *
 */
public class NBTContainer extends NBTCompound {

    private Object nbt;
    private boolean closed;
    private boolean readOnly;

    /**
     * Creates an empty, standalone NBTCompound
     *
     * @deprecated use {@link NBT#createNBTObject()}
     */
    @Deprecated
    public NBTContainer() {
        super(null, null);
        this.nbt = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance();
    }

    /**
     * Takes in any NMS Compound to wrap it
     *
     * @param nbt
     * @deprecated Use NBT.wrapNMSTag
     */
    @Deprecated
    public NBTContainer(Object nbt) {
        super(null, null);
        if (nbt == null)
			nbt = ObjectCreator.NMS_NBTTAGCOMPOUND.getInstance();
        if (!ClassWrapper.NMS_NBTTAGCOMPOUND.getClazz().isAssignableFrom(nbt.getClass()))
			throw new NbtApiException("The object '" + nbt.getClass() + "' is not a valid NBT-Object!");
        this.nbt = nbt;
    }

    /**
     * Reads in a NBT InputStream
     *
     * @param inputsteam
     * @deprecated Use NBT.readNBT
     */
    @Deprecated
    public NBTContainer(InputStream inputsteam) {
        super(null, null);
        this.nbt = NBTReflectionUtil.readNBT(inputsteam);
    }

    /**
     * Parses in a NBT String to a standalone {@link NBTCompound}. Can throw a
     * {@link NbtApiException} in case something goes wrong.
     *
     * @param nbtString
     * @deprecated Use NBT.parseNBT
     */
    @Deprecated
    public NBTContainer(String nbtString) {
        super(null, null);
        if (nbtString == null)
			throw new NullPointerException("The String can't be null!");
        try {
            this.nbt = ReflectionMethod.PARSE_NBT.run(null, nbtString);
        } catch (Exception ex) {
            throw new NbtApiException("Unable to parse Malformed Json!", ex);
        }
    }

    @Override
    public Object getCompound() {
        return this.nbt;
    }

    @Override
    public void setCompound(Object tag) {
        this.nbt = tag;
    }

    @Override
    protected void setClosed() {
        this.closed = true;
    }

    @Override
    protected boolean isClosed() {
        return this.closed;
    }

    @Override
	protected boolean isReadOnly() {
        return this.readOnly;
    }

    protected NBTContainer setReadOnly(boolean readOnly) {
        this.readOnly = true;
        return this;
    }

}
