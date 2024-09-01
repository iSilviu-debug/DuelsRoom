package it.isilviu.duelsroom.utils;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockTypes;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class WorldEditUtils {


    public static List<BlockVector3> generateGlassPerimeter(List<BlockVector2> points, int minY, int maxY) {
        List<BlockVector3> glassBlocks = new ArrayList<>();
        int numPoints = points.size();

        for (int i = 0; i < numPoints; i++) {
            BlockVector2 currentPoint = points.get(i);
            BlockVector2 nextPoint = points.get((i + 1) % numPoints); // Wrap around to the first point

            // Calculate the positions between currentPoint and nextPoint
            int x1 = currentPoint.getX();
            int z1 = currentPoint.getZ();
            int x2 = nextPoint.getX();
            int z2 = nextPoint.getZ();

            // Use Bresenham's line algorithm to calculate the points between two coordinates
            List<BlockVector3> linePoints = bresenhamLine(x1, z1, x2, z2, minY, maxY);
            glassBlocks.addAll(linePoints);
        }

        // Add the roof
        glassBlocks.addAll(generateGlassRoof(points, maxY));

        return glassBlocks;
    }

    private static List<BlockVector3> bresenhamLine(int x1, int z1, int x2, int z2, int minY, int maxY) {
        List<BlockVector3> linePoints = new ArrayList<>();
        int dx = Math.abs(x2 - x1);
        int dz = Math.abs(z2 - z1);
        int sx = x1 < x2 ? 1 : -1;
        int sz = z1 < z2 ? 1 : -1;
        int err = dx - dz;

        while (true) {
            for (int y = minY; y <= maxY; y++) {
                linePoints.add(BlockVector3.at(x1, y, z1));
            }

            if (x1 == x2 && z1 == z2) break;
            int e2 = 2 * err;
            if (e2 > -dz) {
                err -= dz;
                x1 += sx;
            }
            if (e2 < dx) {
                err += dx;
                z1 += sz;
            }
        }

        return linePoints;
    }

    private static List<BlockVector3> generateGlassRoof(List<BlockVector2> points, int y) {
        List<BlockVector3> roofBlocks = new ArrayList<>();
        int minX = points.stream().mapToInt(BlockVector2::getX).min().orElseThrow(IllegalArgumentException::new);
        int maxX = points.stream().mapToInt(BlockVector2::getX).max().orElseThrow(IllegalArgumentException::new);
        int minZ = points.stream().mapToInt(BlockVector2::getZ).min().orElseThrow(IllegalArgumentException::new);
        int maxZ = points.stream().mapToInt(BlockVector2::getZ).max().orElseThrow(IllegalArgumentException::new);

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                if (isPointInPolygon(x, z, points)) { // SO, this is the method that checks if a point is inside a polygon.
                    roofBlocks.add(BlockVector3.at(x, y, z));
                }
            }
        }

        return roofBlocks;
    }

    private static boolean isPointInPolygon(int x, int z, List<BlockVector2> points) {
        boolean result = false;
        int j = points.size() - 1;
        for (int i = 0; i < points.size(); i++) {
            if ((points.get(i).getZ() < z && points.get(j).getZ() >= z || points.get(j).getZ() < z && points.get(i).getZ() >= z) &&
                    (points.get(i).getX() <= x || points.get(j).getX() <= x)) {
                if (points.get(i).getX() + (z - points.get(i).getZ()) / (double)(points.get(j).getZ() - points.get(i).getZ()) * (points.get(j).getX() - points.get(i).getX()) < x) {
                    result = !result;
                }
            }
            j = i;
        }
        return result;
    }

    public static EditSession placeGlassBlocks(World world, Material material, Material filter, List<BlockVector3> glassBlocks) throws MaxChangedBlocksException {
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
            for (BlockVector3 blockVector : glassBlocks) {
                if (editSession.getBlock(blockVector).getBlockType() != BlockTypes.parse(filter.name())) continue;
                editSession.setBlock(blockVector, BlockTypes.parse(material.name()).getDefaultState());
            }

            return editSession;
        }
    }
}
