import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;

public class WadParser {
    private static final Integer HEADER_SIZE = 12;
    private static final Integer MAX_TRAVERSABLE_HEIGHT = 30;
    private static final List<Integer> LIFT_LINEDEF_TYPES = Arrays.asList(66, 15, 148, 143, 67, 14, 149, 144, 68, 20,
            95, 22, 47, 181, 162, 87, 53, 182, 163, 89, 54, 62, 21, 88, 10, 123, 122, 120, 121, 211, 212);
    private static final List<Integer> NORMAL_EXIT_TYPES = Arrays.asList(11, 52, 197);
    private static final List<Integer> SECRET_EXIT_TYPES = Arrays.asList(51, 124, 198);
    private static final List<Integer> EXIT_TYPES = Stream.concat(NORMAL_EXIT_TYPES.stream(), SECRET_EXIT_TYPES.stream())
            .collect(Collectors.toList());
    private static final List<Integer> DOOR_SWITCH_TYPES = Arrays.asList(103);
    private static final List<Integer> STAIRCASE_TYPES = Arrays.asList(258, 7, 256, 8, 259, 127, 257, 100);
    private static final List<Integer> NON_EXIT_TYPES = Stream.concat(DOOR_SWITCH_TYPES.stream(), STAIRCASE_TYPES.stream())
            .collect(Collectors.toList());
    private static final List<Integer> SWITCH_TYPES = Stream.concat(EXIT_TYPES.stream(), NON_EXIT_TYPES.stream())
            .collect(Collectors.toList());

    public Wad createWad(String fromFile) throws IOException {
        MappedByteBuffer byteStream = createStream(fromFile);
        String wadType = extractWadType(byteStream);
        extractNumLumps(byteStream);
        ByteBuffer data = extractData(byteStream);
        List<Lump> lumps = extractLumps(byteStream, data);
        Stream<Level> levels = extractLevels(lumps)
                .sorted(Comparator.comparing(Level::getName));
        return new Wad(wadType, levels.collect(Collectors.toList()));
    }

    private MappedByteBuffer createStream(String fromFile) throws IOException {
        File file = new File(fromFile);
        long fileSize = file.length();
        FileInputStream stream = new FileInputStream(file);
        MappedByteBuffer buffer = stream.getChannel().map(READ_ONLY, 0, fileSize);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer;
    }

    private String extractWadType(MappedByteBuffer byteStream) {
        byte[] wadTypeBytes = new byte[4];
        byteStream.get(wadTypeBytes, 0, 4);
        return new String(wadTypeBytes);
    }

    private void extractNumLumps(MappedByteBuffer byteStream) {
        // Unused but still needs to be popped
        byteStream.getInt();
    }

    private ByteBuffer extractData(MappedByteBuffer byteStream) {
        Integer dataEnd = byteStream.getInt();
        ByteBuffer dataBytes = byteStream.slice();
        byteStream.position(dataEnd);
        return dataBytes;
    }

    private List<Lump> extractLumps(MappedByteBuffer byteStream, ByteBuffer data) {
        if (byteStream.remaining() == 0) {
            return new ArrayList<>();
        } else {
            Lump lump = extractLump(byteStream, data);
            List<Lump> lumps = extractLumps(byteStream, data);
            lumps.add(lump);
            return lumps;
        }
    }

    private Lump extractLump(MappedByteBuffer byteStream, ByteBuffer data) {
        Integer filePos = byteStream.getInt() - HEADER_SIZE;
        Integer size = byteStream.getInt();
        byte[] nameBytes = new byte[8];
        byteStream.get(nameBytes, 0, 8);
        String name = new String(nameBytes);
        byte[] dataBytes = new byte[size];
        if (filePos >= 0) {
            data.position(filePos);
            data.get(dataBytes, 0, size);
            return new Lump(name, Arrays.asList(ArrayUtils.toObject(dataBytes)));
        } else {
            return new Lump(name, new ArrayList<>());
        }
    }

    private Map<String, Map<String, Lump>> createLumpMaps(List<Lump> lumps) {
        Optional<String> currentLevelName = Optional.empty();
        Map<String, Lump> currentLevelLumps = new HashMap<>();
        Map<String, Map<String, Lump>> lumpMap = new HashMap<>();
        Pattern levelNamePattern = Pattern.compile("(E[0-9]M[0-9])");

        for(Lump lump : lumps) {
            Matcher m = levelNamePattern.matcher(lump.getName());
            if (m.matches()){
               if (currentLevelName.isPresent()) {
                   lumpMap.put(currentLevelName.get(), currentLevelLumps);
               }
               currentLevelName = Optional.of(m.group());
               Map<String, Lump> levelLumps = new HashMap<>();
               levelLumps.put(lump.getName(), lump);
               currentLevelLumps = levelLumps;
            } else {
               if (currentLevelName.isPresent()){
                   currentLevelLumps.put(lump.getName(), lump);
               }
            }
        }

        lumpMap.put(currentLevelName.orElse("ERR"), currentLevelLumps);
        return lumpMap;
    }

    private Stream<Level> extractLevels(List<Lump> lumps) {
        Map<String, Map<String, Lump>> lumpMaps = createLumpMaps(lumps);
        return lumpMaps.keySet().stream().map(levelName -> extractLevel(levelName, lumpMaps.get(levelName)));
    }

    private Level extractLevel(String name, Map<String, Lump> lumps) {
        Stream<Vertex> vertices = extractVertices(lumps.get("VERTEXES"));
        Stream<Sector> sectors = extractSectors(lumps.get("SECTORS"));
        Stream<Sidedef> sidedefs = extractSidedefs(lumps.get("SIDEDEFS"), sectors.collect(Collectors.toList()));
        Stream<Linedef> linedefs = extractLinedefs(lumps.get("LINEDEFS"), vertices.collect(Collectors.toList()),
                sidedefs.collect(Collectors.toList()));
        Stream<Thing> things = extractThings(lumps.get("THINGS"));
        Vertex start = extractStart(things);
        Vertex exit = extractExit(linedefs, start);
        List<DoorSwitch> doorSwitches = extractDoorSwitches(linedefs);

        return new Level(name, linedefs.collect(Collectors.toList()), start, exit, doorSwitches);
    }

    private Stream<Vertex> extractVertices(Lump lump) {
        List<Byte> data = lump.getData();
        return IntStream.range(0, data.size() / 4)
                .mapToObj(i -> data.subList(i, i + 4))
                .map(this::extractVertex);
    }

    private Vertex extractVertex(List<Byte> bytes) {
        Byte[] xBytes = new Byte[2];
        Byte[] yBytes = new Byte[2];
        bytes.subList(0, 2).toArray(xBytes);
        bytes.subList(2, 4).toArray(yBytes);
        Integer x = ByteBuffer.wrap(ArrayUtils.toPrimitive(xBytes)).order(ByteOrder.LITTLE_ENDIAN).getInt();
        Integer y = ByteBuffer.wrap(ArrayUtils.toPrimitive(yBytes)).order(ByteOrder.LITTLE_ENDIAN).getInt();
        return new Vertex(x.doubleValue(), y.doubleValue());
    }

    private Stream<Sector> extractSectors(Lump lump) {
        List<Byte> data = lump.getData();
        return IntStream.range(0, data.size() / 26)
                .mapToObj(i -> data.subList(i, i + 26))
                .map(this::extractSector);
    }

    private Sector extractSector(List<Byte> bytes) {
        Byte[] floorHeightBytes = new Byte[2];
        Byte[] ceilingHeightBytes = new Byte[2];
        Byte[] sectorTypeBytes = new Byte[2];
        Byte[] tagBytes = new Byte[2];

        bytes.subList(0, 2).toArray(floorHeightBytes);
        bytes.subList(2, 4).toArray(ceilingHeightBytes);
        bytes.subList(22, 24).toArray(sectorTypeBytes);
        bytes.subList(24,26).toArray(tagBytes);

        Integer floorHeight = ByteBuffer.wrap(ArrayUtils.toPrimitive(floorHeightBytes))
                .order(ByteOrder.LITTLE_ENDIAN).getInt();
        Integer ceilingHeight = ByteBuffer.wrap(ArrayUtils.toPrimitive(ceilingHeightBytes))
                .order(ByteOrder.LITTLE_ENDIAN).getInt();
        Integer sectorType = ByteBuffer.wrap(ArrayUtils.toPrimitive(sectorTypeBytes))
                .order(ByteOrder.LITTLE_ENDIAN).getInt();
        Integer tag = ByteBuffer.wrap(ArrayUtils.toPrimitive(tagBytes))
                .order(ByteOrder.LITTLE_ENDIAN).getInt();

        return new Sector(sectorType, tag, floorHeight, ceilingHeight);
    }

    private Stream<Sidedef> extractSidedefs(Lump lump, List<Sector> sectors) {
        List<Byte> data = lump.getData();
        return IntStream.range(0, data.size() / 30)
                .mapToObj(i -> data.subList(i, i + 30))
                .map(b -> extractSidedef(b, sectors));
    }

    private Sidedef extractSidedef(List<Byte> bytes, List<Sector> sectors) {
        Byte[] sectorIdBytes = new Byte[2];
        bytes.subList(28, 30).toArray(sectorIdBytes);
        Integer sectorId = ByteBuffer.wrap(ArrayUtils.toPrimitive(sectorIdBytes)).order(ByteOrder.LITTLE_ENDIAN).getInt();

        return new Sidedef(sectorId, sectors.get(sectorId));
    }

    private Stream<Linedef> extractLinedefs(Lump lump, List<Vertex> vertices, List<Sidedef> sidedefs) {
        List<Byte> data = lump.getData();
        return IntStream.range(0, data.size() / 14)
                .mapToObj(i -> data.subList(i, i + 14))
                .map(b -> extractLinedef(b, vertices, sidedefs));
    }

    private Linedef extractLinedef(List<Byte> bytes, List<Vertex> vertices, List<Sidedef> sidedefs) {
        Byte[] aIndexBytes = new Byte[2];
        Byte[] bIndexBytes = new Byte[2];
        Byte[] flagsBytes = new Byte[2];
        Byte[] specialTypeBytes = new Byte[2];
        Byte[] sectorTagBytes = new Byte[2];
        Byte[] leftSideBytes = new Byte[2];
        Byte[] rightSideBytes = new Byte[2];

        bytes.subList(0, 2).toArray(aIndexBytes);
        bytes.subList(2, 4).toArray(bIndexBytes);
        bytes.subList(4, 6).toArray(flagsBytes);
        bytes.subList(6, 8).toArray(specialTypeBytes);
        bytes.subList(8, 10).toArray(sectorTagBytes);
        bytes.subList(10, 12).toArray(leftSideBytes);
        bytes.subList(12, 14).toArray(rightSideBytes);

        Integer aIndex = ByteBuffer.wrap(ArrayUtils.toPrimitive(aIndexBytes)).order(ByteOrder.LITTLE_ENDIAN).getInt();
        Integer bIndex = ByteBuffer.wrap(ArrayUtils.toPrimitive(bIndexBytes)).order(ByteOrder.LITTLE_ENDIAN).getInt();
        Integer flags = ByteBuffer.wrap(ArrayUtils.toPrimitive(flagsBytes)).order(ByteOrder.LITTLE_ENDIAN).getInt();
        Integer specialType = ByteBuffer.wrap(ArrayUtils.toPrimitive(specialTypeBytes)).order(ByteOrder.LITTLE_ENDIAN).getInt();
        Integer sectorTag = ByteBuffer.wrap(ArrayUtils.toPrimitive(sectorTagBytes)).order(ByteOrder.LITTLE_ENDIAN).getInt();
        Integer leftSideIndex = ByteBuffer.wrap(ArrayUtils.toPrimitive(leftSideBytes)).order(ByteOrder.LITTLE_ENDIAN).getInt();
        Integer rightSideIndex = ByteBuffer.wrap(ArrayUtils.toPrimitive(rightSideBytes)).order(ByteOrder.LITTLE_ENDIAN).getInt();

        Optional<Sidedef> leftSide = (leftSideIndex == -1) ? Optional.empty() : Optional.of(sidedefs.get(leftSideIndex));
        Optional<Sidedef> rightSide = (rightSideIndex == -1) ? Optional.empty() : Optional.of(sidedefs.get(rightSideIndex));
        Integer leftHeight = leftSide.orElse(rightSide.get()).getSector().getFloorHeight();
        Integer rightHeight = rightSide.orElse(leftSide.get()).getSector().getFloorHeight();
        Integer heightDifference = Math.abs(leftHeight - rightHeight);
        Boolean nonTraversable =
                        leftSideIndex == -1 ||
                        rightSideIndex == -1 ||
                        blocksPlayerAndMonsters(flags) ||
                                (heightDifference > MAX_TRAVERSABLE_HEIGHT && !isLift(specialType));

        return new Linedef(vertices.get(aIndex), vertices.get(bIndex), nonTraversable, sectorTag, specialType,
                rightSide.get(), leftSide.get());
    }

    private Boolean blocksPlayerAndMonsters(Integer flags) {
        return (flags & 0x0001) == 1;
    }

    private Boolean isLift(Integer specialType) {
        return LIFT_LINEDEF_TYPES.contains(specialType);
    }

    private Stream<Thing> extractThings(Lump lump) {
        List<Byte> data = lump.getData();
        return IntStream.range(0, data.size() / 10)
                .mapToObj(i -> data.subList(i, i + 10))
                .map(this::extractThing);
    }

    private Thing extractThing(List<Byte> bytes) {
        Vertex position = extractVertex(bytes.subList(0, 4));

        Byte[] angleBytes = new Byte[2];
        Byte[] doomIdBytes = new Byte[2];

        bytes.subList(4, 6).toArray(angleBytes);
        bytes.subList(6, 8).toArray(doomIdBytes);

        Integer angle = ByteBuffer.wrap(ArrayUtils.toPrimitive(angleBytes)).order(ByteOrder.LITTLE_ENDIAN).getInt();
        Integer doomId = ByteBuffer.wrap(ArrayUtils.toPrimitive(doomIdBytes)).order(ByteOrder.LITTLE_ENDIAN).getInt();

        return new Thing(position, angle, doomId);
    }

    private Vertex extractStart(Stream<Thing> things) {
        return things.filter(t -> t.getDoomId() == 1)
                .findFirst()
                .map(Thing::getPosition)
                .orElse(null);
    }

    private Vertex extractExit(Stream<Linedef> linedefs, Vertex start) {
        return linedefs.filter(l -> NORMAL_EXIT_TYPES.contains(l.getLineType()))
                .findFirst()
                .map(Linedef::midpoint)
                .orElse(start);
    }

    private List<DoorSwitch> extractDoorSwitches(Stream<Linedef> linedefs) {
        return linedefs.filter(l -> SWITCH_TYPES.contains(l.getLineType()))
                .map(DoorSwitch::fromWadLine)
                .collect(Collectors.toList());
    }
}
