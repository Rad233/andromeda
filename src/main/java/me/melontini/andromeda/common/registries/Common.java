package me.melontini.andromeda.common.registries;

import net.minecraft.util.Identifier;

import static me.melontini.andromeda.util.CommonValues.MODID;

public class Common {

    public static Identifier id(String path) {
        return new Identifier(MODID, path);
    }
}
