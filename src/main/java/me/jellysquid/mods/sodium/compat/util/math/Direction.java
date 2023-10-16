package me.jellysquid.mods.sodium.compat.util.math;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public enum Direction {
    DOWN(0, 1, -1, "down", Direction.AxisDirection.NEGATIVE, Direction.Axis.Y, new Vec3i(0, -1, 0)),
    UP(1, 0, -1, "up", Direction.AxisDirection.POSITIVE, Direction.Axis.Y, new Vec3i(0, 1, 0)),
    NORTH(2, 3, 2, "north", Direction.AxisDirection.NEGATIVE, Direction.Axis.Z, new Vec3i(0, 0, -1)),
    SOUTH(3, 2, 0, "south", Direction.AxisDirection.POSITIVE, Direction.Axis.Z, new Vec3i(0, 0, 1)),
    WEST(4, 5, 1, "west", Direction.AxisDirection.NEGATIVE, Direction.Axis.X, new Vec3i(-1, 0, 0)),
    EAST(5, 4, 3, "east", Direction.AxisDirection.POSITIVE, Direction.Axis.X, new Vec3i(1, 0, 0));

    private final int index;
    private final int opposite;
    private final int horizontalIndex;
    private final String name;
    private final Direction.Axis axis;
    private final Direction.AxisDirection axisDirection;
    private final Vec3i directionVec;
    public static final Direction[] VALUES = new Direction[6];
    public static final Direction[] HORIZONTALS = new Direction[4];
    private static final Map<String, Direction> NAME_LOOKUP = Maps.<String, Direction>newHashMap();

    private Direction(int indexIn, int oppositeIn, int horizontalIndexIn, String nameIn, Direction.AxisDirection axisDirectionIn, Direction.Axis axisIn, Vec3i directionVecIn)
    {
        this.index = indexIn;
        this.horizontalIndex = horizontalIndexIn;
        this.opposite = oppositeIn;
        this.name = nameIn;
        this.axis = axisIn;
        this.axisDirection = axisDirectionIn;
        this.directionVec = directionVecIn;
    }

    public int getIndex()
    {
        return this.index;
    }

    public int getHorizontalIndex()
    {
        return this.horizontalIndex;
    }

    public Direction.AxisDirection getAxisDirection()
    {
        return this.axisDirection;
    }

    public Direction getOpposite()
    {
        return byIndex(this.opposite);
    }

    public Direction rotateAround(Direction.Axis axis)
    {
        switch (axis)
        {
            case X:

                if (this != WEST && this != EAST)
                {
                    return this.rotateX();
                }

                return this;
            case Y:

                if (this != UP && this != DOWN)
                {
                    return this.rotateY();
                }

                return this;
            case Z:

                if (this != NORTH && this != SOUTH)
                {
                    return this.rotateZ();
                }

                return this;
            default:
                throw new IllegalStateException("Unable to get CW facing for axis " + axis);
        }
    }

    public Direction rotateY()
    {
        switch (this)
        {
            case NORTH:
                return EAST;
            case EAST:
                return SOUTH;
            case SOUTH:
                return WEST;
            case WEST:
                return NORTH;
            default:
                throw new IllegalStateException("Unable to get Y-rotated facing of " + this);
        }
    }

    private Direction rotateX()
    {
        switch (this)
        {
            case NORTH:
                return DOWN;
            case EAST:
            case WEST:
            default:
                throw new IllegalStateException("Unable to get X-rotated facing of " + this);
            case SOUTH:
                return UP;
            case UP:
                return NORTH;
            case DOWN:
                return SOUTH;
        }
    }

    private Direction rotateZ()
    {
        switch (this)
        {
            case EAST:
                return DOWN;
            case SOUTH:
            default:
                throw new IllegalStateException("Unable to get Z-rotated facing of " + this);
            case WEST:
                return UP;
            case UP:
                return EAST;
            case DOWN:
                return WEST;
        }
    }

    public Direction rotateYCCW()
    {
        switch (this)
        {
            case NORTH:
                return WEST;
            case EAST:
                return NORTH;
            case SOUTH:
                return EAST;
            case WEST:
                return SOUTH;
            default:
                throw new IllegalStateException("Unable to get CCW facing of " + this);
        }
    }

    public int getOffsetX()
    {
        return this.axis == Direction.Axis.X ? this.axisDirection.getOffset() : 0;
    }

    public int getOffsetY()
    {
        return this.axis == Direction.Axis.Y ? this.axisDirection.getOffset() : 0;
    }

    public int getOffsetZ()
    {
        return this.axis == Direction.Axis.Z ? this.axisDirection.getOffset() : 0;
    }

    public String getName2()
    {
        return this.name;
    }

    public Direction.Axis getAxis()
    {
        return this.axis;
    }

    @Nullable
    public static Direction byName(String name)
    {
        return name == null ? null : (Direction)NAME_LOOKUP.get(name.toLowerCase(Locale.ROOT));
    }

    public static Direction byIndex(int index)
    {
        return VALUES[MathHelper.abs(index % VALUES.length)];
    }

    public static Direction byHorizontalIndex(int horizontalIndexIn)
    {
        return HORIZONTALS[MathHelper.abs(horizontalIndexIn % HORIZONTALS.length)];
    }

    public static Direction fromAngle(double angle)
    {
        return byHorizontalIndex(MathHelper.floor(angle / 90.0D + 0.5D) & 3);
    }

    public float getHorizontalAngle()
    {
        return (float)((this.horizontalIndex & 3) * 90);
    }

    public static Direction random(Random rand)
    {
        return values()[rand.nextInt(values().length)];
    }

    public static Direction getFacingFromVector(float x, float y, float z)
    {
        Direction Direction = NORTH;
        float f = Float.MIN_VALUE;

        for (Direction Direction1 : values())
        {
            float f1 = x * (float)Direction1.directionVec.getX() + y * (float)Direction1.directionVec.getY() + z * (float)Direction1.directionVec.getZ();

            if (f1 > f)
            {
                f = f1;
                Direction = Direction1;
            }
        }

        return Direction;
    }

    public String toString()
    {
        return this.name;
    }

    public String getName()
    {
        return this.name;
    }

    public static Direction getFacingFromAxis(Direction.AxisDirection axisDirectionIn, Direction.Axis axisIn)
    {
        for (Direction Direction : values())
        {
            if (Direction.getAxisDirection() == axisDirectionIn && Direction.getAxis() == axisIn)
            {
                return Direction;
            }
        }

        throw new IllegalArgumentException("No such direction: " + axisDirectionIn + " " + axisIn);
    }

    public static Direction getDirectionFromEntityLiving(BlockPos pos, EntityLivingBase placer)
    {
        if (Math.abs(placer.posX - (double)((float)pos.getX() + 0.5F)) < 2.0D && Math.abs(placer.posZ - (double)((float)pos.getZ() + 0.5F)) < 2.0D)
        {
            double d0 = placer.posY + (double)placer.getEyeHeight();

            if (d0 - (double)pos.getY() > 2.0D)
            {
                return UP;
            }

            if ((double)pos.getY() - d0 > 0.0D)
            {
                return DOWN;
            }
        }

        return of(placer.getHorizontalFacing().getOpposite());
    }

    public static Direction of(EnumFacing facing){
        return Direction.valueOf(facing.toString());
    }

    public Vec3i getDirectionVec()
    {
        return this.directionVec;
    }

    static
    {
        for (Direction Direction : values())
        {
            VALUES[Direction.index] = Direction;

            if (Direction.getAxis().isHorizontal())
            {
                HORIZONTALS[Direction.horizontalIndex] = Direction;
            }

            NAME_LOOKUP.put(Direction.getName2().toLowerCase(Locale.ROOT), Direction);
        }
    }

    public static enum Axis implements Predicate<Direction>, IStringSerializable {
        X("x", Direction.Plane.HORIZONTAL),
        Y("y", Direction.Plane.VERTICAL),
        Z("z", Direction.Plane.HORIZONTAL);

        private static final Map<String, Direction.Axis> NAME_LOOKUP = Maps.<String, Direction.Axis>newHashMap();
        private final String name;
        private final Direction.Plane plane;

        private Axis(String name, Direction.Plane plane)
        {
            this.name = name;
            this.plane = plane;
        }

        @Nullable
        public static Direction.Axis byName(String name)
        {
            return name == null ? null : (Direction.Axis)NAME_LOOKUP.get(name.toLowerCase(Locale.ROOT));
        }

        public String getName2()
        {
            return this.name;
        }

        public boolean isVertical()
        {
            return this.plane == Direction.Plane.VERTICAL;
        }

        public boolean isHorizontal()
        {
            return this.plane == Direction.Plane.HORIZONTAL;
        }

        public String toString()
        {
            return this.name;
        }

        public boolean apply(@Nullable Direction p_apply_1_)
        {
            return p_apply_1_ != null && p_apply_1_.getAxis() == this;
        }

        public Direction.Plane getPlane()
        {
            return this.plane;
        }

        public String getName()
        {
            return this.name;
        }

        static
        {
            for (Direction.Axis Direction$axis : values())
            {
                NAME_LOOKUP.put(Direction$axis.getName2().toLowerCase(Locale.ROOT), Direction$axis);
            }
        }
    }

    public static enum AxisDirection {
        POSITIVE(1, "Towards positive"),
        NEGATIVE(-1, "Towards negative");

        private final int offset;
        private final String description;

        private AxisDirection(int offset, String description)
        {
            this.offset = offset;
            this.description = description;
        }

        public int getOffset()
        {
            return this.offset;
        }

        public String toString()
        {
            return this.description;
        }
    }

    public static enum Plane implements Predicate<Direction>, Iterable<Direction> {
        HORIZONTAL,
        VERTICAL;

        public Direction[] facings()
        {
            switch (this)
            {
                case HORIZONTAL:
                    return new Direction[] {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
                case VERTICAL:
                    return new Direction[] {Direction.UP, Direction.DOWN};
                default:
                    throw new Error("Someone's been tampering with the universe!");
            }
        }

        public Direction random(Random rand)
        {
            Direction[] aDirection = this.facings();
            return aDirection[rand.nextInt(aDirection.length)];
        }

        public boolean apply(@Nullable Direction p_apply_1_)
        {
            return p_apply_1_ != null && p_apply_1_.getAxis().getPlane() == this;
        }

        public Iterator<Direction> iterator()
        {
            return Iterators.<Direction>forArray(this.facings());
        }
    }
}
