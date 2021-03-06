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

package com.github.bartimaeusnek.bartworks.system.worldgen;

import com.github.bartimaeusnek.bartworks.util.Pair;
import gregtech.api.GregTech_API;
import gregtech.api.metatileentity.BaseMetaPipeEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.MetaPipeEntity;
import gregtech.api.objects.XSTR;
import gregtech.api.util.GT_Utility;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.common.ForgeHooks;

import java.util.Random;

import static net.minecraftforge.common.ChestGenHooks.DUNGEON_CHEST;
import static net.minecraftforge.common.ChestGenHooks.PYRAMID_DESERT_CHEST;
import static net.minecraftforge.common.ChestGenHooks.PYRAMID_JUNGLE_CHEST;

public abstract class MapGenRuins extends WorldGenerator {

    protected Pair<Block,Integer>[][] ToBuildWith = new Pair[4][0];

    @Override
    public boolean generate(World p_76484_1_, Random p_76484_2_, int p_76484_3_, int p_76484_4_, int p_76484_5_) {
        return false;
    }

    protected void setFloorBlocks(int[] metas, Block... blocks){
        ToBuildWith[0]=new Pair[metas.length];
        for (int i = 0; i < metas.length; i++) {
            ToBuildWith[0][i]=new Pair<>(blocks[i%blocks.length],metas[i]);
        }
    }

    protected void setWallBlocks(int[] metas,Block... blocks){
        ToBuildWith[1]=new Pair[metas.length];
        for (int i = 0; i < metas.length; i++) {
            ToBuildWith[1][i]=new Pair<>(blocks[i%blocks.length],metas[i]);
        }
    }

    protected void setRoofBlocks(int[] metas,Block... blocks){
        ToBuildWith[2]=new Pair[metas.length];
        for (int i = 0; i < metas.length; i++) {
            ToBuildWith[2][i]=new Pair<>(blocks[i%blocks.length],metas[i]);
        }
    }

    protected void setMiscBlocks(int[] metas,Block... blocks){
        ToBuildWith[3]=new Pair[metas.length];
        for (int i = 0; i < metas.length; i++) {
            ToBuildWith[3][i]=new Pair<>(blocks[i%blocks.length],metas[i]);
        }
    }

    int[] statBlocks = new int[4];

    protected void setRandomBlockWAirChance(World worldObj, int x, int y, int z, Random rand, int airchance, Pair<Block,Integer>... blocks){
        if (rand.nextInt(100) > airchance)
            setRandomBlock(worldObj,x,y,z,rand,blocks);
        else
            setBlock(worldObj,x,y,z,Blocks.air,0);
    }

    protected void setRandomBlock(World worldObj, int x, int y, int z, Random rand, Pair<Block,Integer>... blocks){
        Block toSet = blocks[rand.nextInt(blocks.length)].getKey();
        int meta = blocks[rand.nextInt(blocks.length)].getValue();
        this.setBlock(worldObj,x,y,z,toSet,meta);
    }

    protected void setBlock(World worldObj, int x, int y, int z, Block block,int meta){
        this.setBlockAndNotifyAdequately(worldObj,x,y,z,block,meta);
    }
    protected void setBlock(World worldObj, int x, int y, int z, Pair<Block,Integer> pair){
        this.setBlockAndNotifyAdequately(worldObj,x,y,z,pair.getKey(),pair.getValue());
    }

    public static class RuinsBase extends MapGenRuins{

        @Override
        public boolean generate(World worldObj, Random rand1, int x, int y, int z) {

            for (int i = 0; i < rand1.nextInt(144); i++) {
                rand1.nextLong();
            }

            Random rand = new XSTR(rand1.nextLong());


            x=x+5;
            z=z+5;

            if (worldObj.getBlock(x,y,z) == Blocks.air) {
                while (worldObj.getBlock(x, y, z) == Blocks.air) {
                    y--;
                }
            } else {
                while (worldObj.getBlock(x, y, z) != Blocks.air) {
                    y++;
                }
                y--;
            }

            setFloorBlocks(new int[]{0,0,0},Blocks.brick_block,Blocks.double_stone_slab,Blocks.stonebrick);
            setWallBlocks(new int[]{0,1,2,1,1},Blocks.stonebrick);
            setRoofBlocks(new int[]{9},Blocks.log);
            setMiscBlocks(new int[]{1},Blocks.log);
            statBlocks= new int[]{rand.nextInt(ToBuildWith[0].length)};
            int colored=rand.nextInt(15);
            int tier = rand.nextInt(6);
            boolean useColor = rand.nextBoolean();
            byte set = 0;
            byte toSet = (byte) (rand.nextInt(6-tier)+1);
            short cablemeta = GT_WorldgenUtil.getCable(rand,tier);
            byte treeinaRow = 0;
            boolean lastset = rand.nextBoolean();
            for (int dx = -6; dx <= 6; dx++) {
                for (int dy = 0; dy <= 8; dy++) {
                    for (int dz = -6; dz <= 6; dz++) {
                        this.setBlock(worldObj,x+dx,y+dy,z+dz,Blocks.air,0);
                        if (dy == 0){
                            Pair<Block,Integer> floor = ToBuildWith[0][statBlocks[0]];
                            this.setBlock(worldObj,x+dx,y+dy,z+dz, floor.getKey(),floor.getValue());
                        }
                        else if (dy > 0 && dy < 4){
                            if (Math.abs(dx) == 5 && Math.abs(dz) == 5){
                                setRandomBlockWAirChance(worldObj,x+dx,y+dy,z+dz,rand,5, ToBuildWith[3][0]);
                            }
                            else if ((dx == 0) && dz == -5 && (dy == 1 || dy == 2)){
                                if (dy == 1)
                                    this.setBlock(worldObj,x+dx,y+dy,z+dz, Blocks.iron_door,1);
                                if (dy == 2)
                                    this.setBlock(worldObj,x+dx,y+dy,z+dz, Blocks.iron_door,8);
                            }
                            else if (Math.abs(dx)== 5 && Math.abs(dz) < 5 || Math.abs(dz)== 5 && Math.abs(dx) < 5){
                                setRandomBlockWAirChance(worldObj,x+dx,y+dy,z+dz,rand,25,ToBuildWith[1]);
                                if (dy == 2) {
                                    if (rand.nextInt(100)<12)
                                        if (useColor)
                                            setRandomBlockWAirChance(worldObj,x+dx,y+dy,z+dz,rand,25,new Pair<>(Blocks.stained_glass_pane,colored));
                                        else
                                            setRandomBlockWAirChance(worldObj,x+dx,y+dy,z+dz,rand,25,new Pair<>(Blocks.glass_pane,0));
                                }
                            }

                            if (dy == 3 && Math.abs(dx)== 6){
                                setRandomBlockWAirChance(worldObj,x+dx,y+dy,z+dz,rand,25,ToBuildWith[2]);
                            }

                            if (dy == 1){
                                if (dx == 3 && dz == -3){
                                    setBlock(worldObj,x + dx, y + dy, z + dz,Blocks.crafting_table,0);
                                }
                                if (dx == -3 && (dz == -3 || dz == -2)){
                                    setBlock(worldObj,x + dx, y + dy, z + dz,Blocks.chest,5);
                                    IInventory chest = (IInventory)worldObj.getTileEntity(x + dx, y + dy, z + dz);
                                    if (chest != null) {
                                        WeightedRandomChestContent.generateChestContents(rand, ChestGenHooks.getItems(PYRAMID_JUNGLE_CHEST, rand), chest, ChestGenHooks.getCount(PYRAMID_JUNGLE_CHEST, rand));
                                    }
                                }

                                if (dx == 4 && dz==4) {
                                    short meta = GT_WorldgenUtil.getGenerator(rand,tier);
                                    setBlock(worldObj, x + dx, y + dy, z + dz, GT_WorldgenUtil.GT_TILES,GregTech_API.METATILEENTITIES[meta].getTileEntityBaseType());
                                    BaseMetaTileEntity BTE = (BaseMetaTileEntity) worldObj.getTileEntity(x + dx, y + dy, z + dz);
                                    BTE.setInitialValuesAsNBT(null,meta);
                                    BTE.setOwnerName("Ancient Cultures");
                                    BTE.setFrontFacing(tier > 0 ? (byte)4 : (byte) 2);

                                }
                                else if (dx == 3 && dz==4) {
                                    if (tier>0) {
                                        short meta = GT_WorldgenUtil.getBuffer(rand, tier);
                                        setBlock(worldObj, x + dx, y + dy, z + dz, GT_WorldgenUtil.GT_TILES, GregTech_API.METATILEENTITIES[meta].getTileEntityBaseType());
                                        BaseMetaTileEntity BTE = (BaseMetaTileEntity) worldObj.getTileEntity(x + dx, y + dy, z + dz);
                                        BTE.setInitialValuesAsNBT(null, meta);
                                        BTE.setOwnerName("Ancient Cultures");
                                        BTE.setFrontFacing((byte) 4);
                                    }else{
                                        short meta = cablemeta;
                                        setRandomBlockWAirChance(worldObj, x + dx, y + dy, z + dz, rand, 33, new Pair<Block, Integer>(GT_WorldgenUtil.GT_TILES, (int) GregTech_API.METATILEENTITIES[meta].getTileEntityBaseType()));
                                        BaseMetaPipeEntity BTE = (BaseMetaPipeEntity) worldObj.getTileEntity(x + dx, y + dy, z + dz);
                                        if (BTE != null) {
                                            BTE.setInitialValuesAsNBT(null, meta);
                                            BTE.setOwnerName("Ancient Cultures");
                                            BTE.setFrontFacing((byte) 4);
                                            BTE.mConnections = (byte) (BTE.mConnections | 1 << (byte) 4);
                                            BTE.mConnections = (byte) (BTE.mConnections | 1 << (byte) GT_Utility.getOppositeSide(4));
                                            ((MetaPipeEntity) BTE.getMetaTileEntity()).mConnections = BTE.mConnections;
                                        }
                                    }
                                }
                                else if (dx < 3 && dx > -5 && dz == 4) {
                                    short meta = cablemeta;
                                    setRandomBlockWAirChance(worldObj, x + dx, y + dy, z + dz, rand, 33, new Pair<Block, Integer>(GT_WorldgenUtil.GT_TILES, (int) GregTech_API.METATILEENTITIES[meta].getTileEntityBaseType()));

                                    BaseMetaPipeEntity BTE = (BaseMetaPipeEntity) worldObj.getTileEntity(x + dx, y + dy, z + dz);
                                    if (BTE != null) {
                                        BTE.setInitialValuesAsNBT(null, meta);
                                        BTE.setOwnerName("Ancient Cultures");
                                        BTE.setFrontFacing((byte) 4);
                                        BTE.mConnections = (byte)(BTE.mConnections | 1 << (byte)4);
                                        BTE.mConnections = (byte)(BTE.mConnections | 1 << (byte) GT_Utility.getOppositeSide(4));

                                        BaseMetaTileEntity BPE = (BaseMetaTileEntity) worldObj.getTileEntity(x + dx, y + dy, z + dz-1);
                                        if (BPE != null) {
                                            BTE.mConnections = (byte)(BTE.mConnections | 1 << (byte)2);
                                        }
                                        ((MetaPipeEntity)BTE.getMetaTileEntity()).mConnections=BTE.mConnections;
                                    }

                                }
                                else if (dx < 3 && dx > -5 && dz == 3 && set < toSet){
                                    if (!lastset || treeinaRow > 2) {
                                        short meta = GT_WorldgenUtil.getMachine(rand, tier);
                                        setBlock(worldObj, x + dx, y + dy, z + dz, GT_WorldgenUtil.GT_TILES, GregTech_API.METATILEENTITIES[meta].getTileEntityBaseType());
                                        BaseMetaTileEntity BTE = (BaseMetaTileEntity) worldObj.getTileEntity(x + dx, y + dy, z + dz);
                                        BTE.setInitialValuesAsNBT(null, meta);
                                        BTE.setOwnerName("Ancient Cultures");
                                        BTE.setFrontFacing((byte) 2);

                                        set++;
                                        treeinaRow = 0;
                                        lastset=true;
                                    }
                                    else {
                                        lastset = rand.nextBoolean();
                                        if (lastset)
                                            treeinaRow++;
                                    }
                                }
                            }
                        }
                        else if(dy == 4){
                            if (Math.abs(dx)== 5)
                                setRandomBlockWAirChance(worldObj,x+dx,y+dy,z+dz,rand,25,ToBuildWith[2]);
                            else if (Math.abs(dz) == 5 && Math.abs(dx) < 5)
                                setRandomBlockWAirChance(worldObj,x+dx,y+dy,z+dz,rand,25,ToBuildWith[1]);
                        }
                        else if(dy == 5){
                            if (Math.abs(dx)== 4)
                                setRandomBlockWAirChance(worldObj,x+dx,y+dy,z+dz,rand,25,ToBuildWith[2]);
                            else if (Math.abs(dz) == 5 && Math.abs(dx) < 4)
                                setRandomBlockWAirChance(worldObj,x+dx,y+dy,z+dz,rand,25,ToBuildWith[1]);
                        }
                        else if(dy == 6){
                            if (Math.abs(dx)== 3)
                                setRandomBlockWAirChance(worldObj,x+dx,y+dy,z+dz,rand,25,ToBuildWith[2]);
                            else if (Math.abs(dz) == 5 && Math.abs(dx) < 3)
                                setRandomBlockWAirChance(worldObj,x+dx,y+dy,z+dz,rand,25,ToBuildWith[1]);
                        }
                        else if(dy == 7){
                            if (Math.abs(dx)== 2)
                                setRandomBlockWAirChance(worldObj,x+dx,y+dy,z+dz,rand,25,ToBuildWith[2]);
                            else if (Math.abs(dz) == 5 && Math.abs(dx) < 2)
                                setRandomBlockWAirChance(worldObj,x+dx,y+dy,z+dz,rand,25,ToBuildWith[1]);
                        }
                        else if(dy == 8) {
                            if (Math.abs(dx) == 1 || Math.abs(dx) == 0)
                                setRandomBlockWAirChance(worldObj, x + dx, y + dy, z + dz, rand, 25, ToBuildWith[2]);
                        }
                    }
                }
            }
            tosetloop:
            while (set < toSet){
                int dy = 1;
                int dz = 3;
                for (int dx = 2; dx > -5; dx--) {
                    if (set < toSet){
                        if (!lastset || treeinaRow > 2 && worldObj.getTileEntity(x + dx, y + dy, z + dz) == null) {
                            short meta = GT_WorldgenUtil.getMachine(rand, tier);
                            setBlock(worldObj, x + dx, y + dy, z + dz, GT_WorldgenUtil.GT_TILES, GregTech_API.METATILEENTITIES[meta].getTileEntityBaseType());
                            BaseMetaTileEntity BTE = (BaseMetaTileEntity) worldObj.getTileEntity(x + dx, y + dy, z + dz);
                            BTE.setInitialValuesAsNBT(null, meta);
                            BTE.setOwnerName("Ancient Cultures");
                            BTE.setFrontFacing((byte) 2);

                            set++;
                            treeinaRow = 0;
                            lastset=true;
                        }
                        else {
                            lastset = rand.nextBoolean();
                            if (lastset)
                                treeinaRow++;
                        }
                    }else
                        break tosetloop;
                }
            }
            return true;
        }
    }

}
