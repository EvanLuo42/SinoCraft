package cx.rain.mc.forgemod.sinocraft.block.base;

import cx.rain.mc.forgemod.sinocraft.utility.enumerate.LogType;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;

public class BlockPlank extends Block {
    private LogType type = null;

    public BlockPlank(LogType typeIn) {
        super(Properties.create(Material.WOOD)
                .harvestTool(ToolType.AXE)
                .hardnessAndResistance(2.0F, 3.0F)
                .sound(SoundType.WOOD));
        type = typeIn;
    }
}
