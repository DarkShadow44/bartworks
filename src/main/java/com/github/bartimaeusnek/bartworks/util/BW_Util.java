/*
 * Copyright (c) 2019 bartimaeusnek
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.bartimaeusnek.bartworks.util;

import com.github.bartimaeusnek.bartworks.API.BioVatLogicAdder;
import gregtech.api.enums.Materials;
import gregtech.api.interfaces.ISubTagContainer;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Energy;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_MultiBlockBase;
import gregtech.api.util.GT_OreDictUnificator;
import gregtech.api.util.GT_Utility;
import net.minecraft.block.Block;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import javax.annotation.Nonnegative;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.UnaryOperator;

import static gregtech.api.enums.GT_Values.V;

public class BW_Util {

    public static final int STANDART = 0;
    public static final int CLEANROOM = -100;
    public static final int LOWGRAVITY = -200;

    public static byte specialToByte(int aSpecialValue) {
        byte special = 0;
        if (aSpecialValue == (CLEANROOM))
            special = 1;
        else if (aSpecialValue == (LOWGRAVITY))
            special = 2;
        else if (aSpecialValue == (CLEANROOM | LOWGRAVITY)) {
            special = 3;
        }
        return special;
    }

    public static boolean addBlockToMachine(int x, int y, int z, int offsetsize, IGregTechTileEntity aBaseMetaTileEntity, Block block) {
        int xDir = ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()).offsetX * offsetsize;
        int zDir = ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()).offsetZ * offsetsize;

        return aBaseMetaTileEntity.getBlockOffset(xDir + x, y, zDir + z).equals(block);
    }

    public static boolean addBlockToMachine(int x, int y, int z, int offsetsize, IGregTechTileEntity aBaseMetaTileEntity, Block block, int damage) {
        byte dmg = (byte) damage;
        int xDir = ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()).offsetX * offsetsize;
        int zDir = ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()).offsetZ * offsetsize;

        return aBaseMetaTileEntity.getBlockOffset(xDir + x, y, zDir + z).equals(block) && aBaseMetaTileEntity.getMetaIDOffset(xDir + x, y, zDir + z) == dmg;
    }

    public static int calculateSv(Materials materials) {
        Iterator it = BioVatLogicAdder.RadioHatch.getMaSv().iterator();
        while (it.hasNext()) {
            BioVatLogicAdder.MaterialSvPair pair = (BioVatLogicAdder.MaterialSvPair) it.next();
            if (pair.getMaterials().equals(materials))
                return pair.getSievert();
        }
        return (int) (materials.getProtons() == 43L ? (materials.equals(Materials.NaquadahEnriched) ? 140 : materials.equals(Materials.Naquadria) ? 150 : materials.equals(Materials.Naquadah) ? 130 : 43) : materials.getProtons());
    }

    public static boolean checkStackAndPrefix(ItemStack itemStack) {
        return itemStack != null && GT_OreDictUnificator.getAssociation(itemStack) != null && GT_OreDictUnificator.getAssociation(itemStack).mPrefix != null && GT_OreDictUnificator.getAssociation(itemStack).mMaterial != null && GT_OreDictUnificator.getAssociation(itemStack).mMaterial.mMaterial != null;
    }

    public static int getMachineVoltageFromTier(int tier) {
        return (int) (30 * Math.pow(4, (tier - 1)));
    }

    public static boolean areStacksEqual(ItemStack aStack1, ItemStack aStack2) {
        return (aStack1 == null && aStack2 == null) || GT_Utility.areStacksEqual(aStack1, aStack2);
    }

    public static byte getByteFromRarity(EnumRarity rarity) {
        if (rarity.equals(EnumRarity.uncommon))
            return 1;
        else if (rarity.equals(EnumRarity.epic))
            return 2;
        else if (rarity.equals(EnumRarity.rare))
            return 3;
        return 0;
    }

    public static byte getTierFromGlasMeta(int meta) {
        byte ret;
        switch (meta) {
            case 1:
                ret = 4;
                break;
            case 2:
                ret = 5;
                break;
            case 3:
                ret = 6;
                break;
            case 4:
                ret = 7;
                break;
            case 5:
                ret = 8;
                break;
            case 12:
                ret = 5;
                break;
            default:
                ret = 3;
        }
        return ret;
    }

    /**
     * Taken from the GTNH fork, made originally by Tec
     * Calcualtes overclocked ness using long integers
     *
     * @param aEUt      - recipe EUt
     * @param aDuration - recipe Duration
     * @param mAmperage - should be 1 ?
     */
    public static void calculateOverclockedNessMulti(@Nonnegative int aEUt, @Nonnegative int aDuration, @Nonnegative int mAmperage, @Nonnegative long maxInputVoltage, GT_MetaTileEntity_MultiBlockBase base) {
        byte mTier = (byte) Math.max(0, GT_Utility.getTier(maxInputVoltage));
        if (mTier == 0) {
            //Long time calculation
            long xMaxProgresstime = ((long) aDuration) << 1;
            if (xMaxProgresstime > Integer.MAX_VALUE - 1) {
                //make impossible if too long
                base.mEUt = Integer.MAX_VALUE - 1;
                base.mMaxProgresstime = Integer.MAX_VALUE - 1;
            } else {
                base.mEUt = aEUt >> 2;
                base.mMaxProgresstime = (int) xMaxProgresstime;
            }
        } else {
            //Long EUt calculation
            long xEUt = aEUt;
            //Isnt too low EUt check?
            long tempEUt = xEUt < V[1] ? V[1] : xEUt;

            base.mMaxProgresstime = aDuration;

            while (tempEUt <= V[mTier - 1] * mAmperage) {
                tempEUt <<= 2;//this actually controls overclocking
                //xEUt *= 4;//this is effect of everclocking
                base.mMaxProgresstime >>= 1;//this is effect of overclocking
                xEUt = base.mMaxProgresstime == 0 ? xEUt >> 1 : xEUt << 2;//U know, if the time is less than 1 tick make the machine use less power
            }
            if (xEUt > Integer.MAX_VALUE - 1) {
                base.mEUt = Integer.MAX_VALUE - 1;
                base.mMaxProgresstime = Integer.MAX_VALUE - 1;
            } else {
                base.mEUt = (int) xEUt;
                if (base.mEUt == 0)
                    base.mEUt = 1;
                if (base.mMaxProgresstime == 0)
                    base.mMaxProgresstime = 1;//set time to 1 tick
            }
        }
    }

    public static long getnominalVoltage(GT_MetaTileEntity_MultiBlockBase base) {
        long rVoltage = 0L;
        long rAmperage = 0L;
        Iterator var3 = base.mEnergyHatches.iterator();

        while (var3.hasNext()) {
            GT_MetaTileEntity_Hatch_Energy tHatch = (GT_MetaTileEntity_Hatch_Energy) var3.next();
            if (base.isValidMetaTileEntity(tHatch)) {
                if (rVoltage == 0 || rVoltage > tHatch.getBaseMetaTileEntity().getInputVoltage())
                    rVoltage = tHatch.getBaseMetaTileEntity().getInputVoltage();
                rAmperage += tHatch.getBaseMetaTileEntity().getInputAmperage();
            }
        }

        return rVoltage * rAmperage;
    }

    public static EnumRarity getRarityFromByte(byte b) {
        switch (b) {
            case 1:
                return EnumRarity.uncommon;
            case 2:
                return EnumRarity.rare;
            case 3:
                return EnumRarity.epic;
            default:
                return EnumRarity.common;
        }
    }


    public static boolean check_layer(IGregTechTileEntity aBaseMetaTileEntity, int radius, int yLevel, int height, Block block, int dmg, int offset, boolean insideCheck, Block inside, int dmginside, int aBaseCasingIndex) {
        return check_layer(aBaseMetaTileEntity, radius, yLevel, height, block, dmg, offset, false, insideCheck, inside, dmginside, aBaseCasingIndex);
    }

    public static boolean check_layer(IGregTechTileEntity aBaseMetaTileEntity, int radius, int yLevel, int height, Block block, int dmg, int offset, int aBaseCasingIndex) {
        return check_layer(aBaseMetaTileEntity, radius, yLevel, height, block, dmg, offset, false, aBaseCasingIndex);
    }

    public static boolean check_layer(IGregTechTileEntity aBaseMetaTileEntity, int radius, int yLevel, int height, Block block, int dmg, int offset, boolean controllerLayer, int aBaseCasingIndex) {
        return check_layer(aBaseMetaTileEntity, radius, yLevel, height, block, dmg, offset, controllerLayer, false, aBaseCasingIndex);
    }

    public static boolean check_layer(IGregTechTileEntity aBaseMetaTileEntity, int radius, int yLevel, int height, Block block, int dmg, int offset, boolean controllerLayer, boolean freeCorners, int aBaseCasingIndex) {
        return check_layer(aBaseMetaTileEntity, radius, yLevel, height, block, dmg, offset, controllerLayer, freeCorners, false, block, dmg, true, aBaseCasingIndex);
    }

    public static boolean check_layer(IGregTechTileEntity aBaseMetaTileEntity, int radius, int yLevel, int height, Block block, int dmg, int offset, boolean controllerLayer, boolean insideCheck, Block inside, int dmginside, int aBaseCasingIndex) {
        return check_layer(aBaseMetaTileEntity, radius, yLevel, height, block, dmg, offset, controllerLayer, false, insideCheck, inside, dmginside, true, aBaseCasingIndex);
    }

    /**
     * @param aBaseMetaTileEntity the Multiblock controller, usually a parameter
     * @param radius              the radius of the layer
     * @param yLevel              the starting y level of the Layer, referenced to the Multiblock
     * @param height              the height of the Layers, referenced to the Multiblock
     * @param block               the block for the walls
     * @param offset              the offset in most cases should be the same as the radius
     * @param controllerLayer     if the layer contains the controller
     * @param freeCorners         if the corners should be checked
     * @param insideCheck         if the inside should be empty/filled
     * @param inside              which block should be inside
     * @param allowHatches        if hatches are allowed in this Layer
     * @param aBaseCasingIndex    the Index for the hatches texture
     * @return if the layer check was completed
     */
    public static boolean check_layer(IGregTechTileEntity aBaseMetaTileEntity, int radius, int yLevel, int height, Block block, int dmg, int offset, boolean controllerLayer, boolean freeCorners, boolean insideCheck, Block inside, int dmginside, boolean allowHatches, int aBaseCasingIndex) {
        int xDir = ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()).offsetX * offset;
        int zDir = ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()).offsetZ * offset;
        for (int x = -radius; x <= radius; x++) {
            for (int y = yLevel; y < height; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (freeCorners && (((Math.abs(x) == radius && Math.abs(z) == radius))))
                        continue;
                    if (controllerLayer && (xDir + x == 0 && zDir + z == 0))
                        continue;
                    if (insideCheck && (Math.abs(x) < radius && Math.abs(z) != radius))
                        if (!aBaseMetaTileEntity.getBlockOffset(xDir + x, y, zDir + z).equals(inside) || (aBaseMetaTileEntity.getMetaIDOffset(xDir + x, y, zDir + z) != dmginside && dmginside > (-1))) {
                            return false;
                        }
                    if (((!(Math.abs(x) < radius && Math.abs(z) != radius))) && (!aBaseMetaTileEntity.getBlockOffset(xDir + x, y, zDir + z).equals(block) || (aBaseMetaTileEntity.getMetaIDOffset(xDir + x, y, zDir + z) != dmg && dmg > (-1)))) {
                        if (!(allowHatches && (
                                ((GT_MetaTileEntity_MultiBlockBase) aBaseMetaTileEntity.getMetaTileEntity()).addDynamoToMachineList(aBaseMetaTileEntity.getIGregTechTileEntityOffset(xDir + x, y, zDir + z), aBaseCasingIndex) ||
                                        ((GT_MetaTileEntity_MultiBlockBase) aBaseMetaTileEntity.getMetaTileEntity()).addEnergyInputToMachineList(aBaseMetaTileEntity.getIGregTechTileEntityOffset(xDir + x, y, zDir + z), aBaseCasingIndex) ||
                                        ((GT_MetaTileEntity_MultiBlockBase) aBaseMetaTileEntity.getMetaTileEntity()).addMaintenanceToMachineList(aBaseMetaTileEntity.getIGregTechTileEntityOffset(xDir + x, y, zDir + z), aBaseCasingIndex) ||
                                        ((GT_MetaTileEntity_MultiBlockBase) aBaseMetaTileEntity.getMetaTileEntity()).addMufflerToMachineList(aBaseMetaTileEntity.getIGregTechTileEntityOffset(xDir + x, y, zDir + z), aBaseCasingIndex) ||
                                        ((GT_MetaTileEntity_MultiBlockBase) aBaseMetaTileEntity.getMetaTileEntity()).addInputToMachineList(aBaseMetaTileEntity.getIGregTechTileEntityOffset(xDir + x, y, zDir + z), aBaseCasingIndex) ||
                                        ((GT_MetaTileEntity_MultiBlockBase) aBaseMetaTileEntity.getMetaTileEntity()).addOutputToMachineList(aBaseMetaTileEntity.getIGregTechTileEntityOffset(xDir + x, y, zDir + z), aBaseCasingIndex)
                        ))) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public static List<Byte> getMetasFromLayer(IGregTechTileEntity aBaseMetaTileEntity, int radius, int yLevel, int height, int offset, boolean controllerLayer, boolean freeCorners, boolean insideCheck) {
        ArrayList<Byte> ret = new ArrayList<>();
        int xDir = ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()).offsetX * offset;
        int zDir = ForgeDirection.getOrientation(aBaseMetaTileEntity.getBackFacing()).offsetZ * offset;
        for (int x = -radius; x <= radius; x++) {
            for (int y = yLevel; y < height; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (freeCorners && (((Math.abs(x) == radius && Math.abs(z) == radius))))
                        continue;
                    if (controllerLayer && (xDir + x == 0 && zDir + z == 0))
                        continue;
                    if (insideCheck && (Math.abs(x) < radius && Math.abs(z) != radius))
                        ret.add(aBaseMetaTileEntity.getMetaIDOffset(xDir + x, y, zDir + z));
                    if (!(Math.abs(x) < radius && Math.abs(z) != radius)) {
                        ret.add(aBaseMetaTileEntity.getMetaIDOffset(xDir + x, y, zDir + z));
                    }
                }
            }
        }
        return ret;
    }
}
