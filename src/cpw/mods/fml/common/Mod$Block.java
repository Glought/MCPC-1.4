package cpw.mods.fml.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import net.minecraft.server.ItemBlock;

@Retention(RetentionPolicy.RUNTIME)
@Target( {ElementType.FIELD})
public @interface Mod$Block
{
    String name();

Class itemTypeClass() default ItemBlock.class;
}
