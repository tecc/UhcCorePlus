package com.gmail.val59000mc.schematics;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public class Lobby extends Schematic {

    private static final String SCHEMATIC_NAME = "lobby";

    private int width, length, height;
    private int widthRadius, lengthRadius, heightRadius;

    public Lobby(Location loc) {
        super(SCHEMATIC_NAME, loc);

        // Dimensions for glass box
        // note, these have to be divided
        width = 10;
        length = 10;
        height = 3;
    }

    @Override
    public void build() {
        // Paste schematic
        if (canBePasted()) {
            super.build();

            height = getHeight();
            length = getLength();
            width = getWidth();
        }
        // Build glass box
        else {
            int x = getLocation().getBlockX(), y = getLocation().getBlockY() + 2, z = getLocation().getBlockZ();
            int widthRadius = width / 2;
            int heightRadius = height / 2;
            int lengthRadius = length / 2;
            world = getLocation().getWorld();
            // floor and roof, respectively
            createCube(x, y - heightRadius, z, width, 1, length, true);
            createCube(x, y + heightRadius, z, width, 1, length, true);
            // note: walls are named by their orientation from the inside
            // -X
            createCube(x - widthRadius, y, z, 1, heightRadius, lengthRadius, true);
            // X
            createCube(x + widthRadius, y, z, 1, heightRadius, lengthRadius, true);
            // -Z
            createCube(x, y, z - lengthRadius, widthRadius, heightRadius, 1, true);
            // Z
            createCube(x, y, z + lengthRadius, widthRadius, heightRadius, 1, true);
        }
    }

    World world;
    Material buildingMaterial = Material.GLASS;
    int iX, iY, iZ;

    /**
     * Creates a cube at some location in the world, using {@link #buildingMaterial} as the block being set.
     * The location values ({@code x, y, z} are positioned at the centre of the block.
     *
     * @param x The X coordinate of the centre block.
     * @param y The Y coordinate of the centre block.
     * @param z The Z coordinate of the centre block.
     * @param widthr How many blocks to build out from the centre point in the X dimension.
     * @param heightr How many blocks to build out from the centre point in the Y dimension.
     * @param lengthr How many blocks to build out from the centre point in the Z dimension.
     * @param skipNonAirBlocks Whether or not to not place at non-air blocks.
     *                         If true, it will skip placing at non-air blocks.
     *                         If false, it will at non-air blocks.
     */
    private void createCube(int x, int y, int z, int widthr, int heightr, int lengthr, boolean skipNonAirBlocks) {
        if ((widthr & heightr & lengthr) == 0) // hella clever way of setting the blocks
            return;

        for (iX = -widthr; iX <= widthr; iX++) {
            for (iY = -heightr; iY <= heightr; iY++) {
                for (iZ = -lengthr; iZ <= lengthr; iZ++) {
                    Block block = world.getBlockAt(iX + x, iY + y, iZ + z);
                    if (skipNonAirBlocks && block.getType() != Material.AIR) // don't place if it's not air
                        block.setType(buildingMaterial);
                }
            }
        }
    }

    public void destroyBoundingBox() {
        int lobbyX = getLocation().getBlockX(), lobbyY = getLocation().getBlockY() + 2, lobbyZ = getLocation().getBlockZ();

        World world = getLocation().getWorld();
        createCube(lobbyX, lobbyY, lobbyZ, widthRadius, heightRadius, lengthRadius, false);
    }

}
