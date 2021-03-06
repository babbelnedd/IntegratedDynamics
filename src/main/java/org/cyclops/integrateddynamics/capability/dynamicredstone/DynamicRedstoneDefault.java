package org.cyclops.integrateddynamics.capability.dynamicredstone;

import org.cyclops.integrateddynamics.api.block.IDynamicRedstone;

/**
 * Default implementation of {@link IDynamicRedstone}.
 * @author rubensworks
 */
public class DynamicRedstoneDefault implements IDynamicRedstone {

    @Override
    public void setRedstoneLevel(int level, boolean strongPower) {

    }

    @Override
    public int getRedstoneLevel() {
        return 0;
    }

    @Override
    public boolean isStrong() {
        return false;
    }

    @Override
    public void setAllowRedstoneInput(boolean allow) {

    }

    @Override
    public boolean isAllowRedstoneInput() {
        return false;
    }
}
